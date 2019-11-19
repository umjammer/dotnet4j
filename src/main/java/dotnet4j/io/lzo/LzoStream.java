//
// Copyright (c) 2017, Bianco Veigel
//
// Permission is hereby granted, free of charge, to any person obtaining a
// copy of this software and associated documentation files (the "Software"),
// to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense,
// and/or sell copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
// DEALINGS IN THE SOFTWARE.
//

package dotnet4j.io.lzo;

import java.util.Optional;

import dotnet4j.io.BufferedStream;
import dotnet4j.io.EndOfStreamException;
import dotnet4j.io.IOException;
import dotnet4j.io.SeekOrigin;
import dotnet4j.io.Stream;
import dotnet4j.io.compression.CompressionMode;


/**
 * Wrapper Stream for lzo compression
 */
public class LzoStream extends Stream {
    protected Stream _source;

    private Optional<Long> _length;

    private final boolean _leaveOpen;

    protected byte[] _decodedBuffer;

    protected static final int MaxWindowSize = (1 << 14) + ((255 & 8) << 11) + (255 << 6) + (255 >> 2);

    protected RingBuffer _ringBuffer = new RingBuffer(MaxWindowSize);

    protected long _outputPosition;

    protected int _instruction;

    protected LzoState _state = LzoState.ZeroCopy;

    protected enum LzoState {
        /**
         * last instruction did not copy any literal
         */
        ZeroCopy,
        /**
         * last instruction used to copy between 1 literal
         */
        SmallCopy1,
        /**
         * last instruction used to copy between 2 literals
         */
        SmallCopy2,
        /**
         * last instruction used to copy between 3 literals
         */
        SmallCopy3,
        /**
         * last instruction used to copy 4 or more literals
         */
        LargeCopy
    }

    /**
     * creates a new lzo stream for decompression
     *
     * @param stream the compressed stream @param mode currently only
     *            decompression is supported
     */
    public LzoStream(Stream stream, CompressionMode mode) {
        this(stream, mode, false);
    }

    /**
     * creates a new lzo stream for decompression
     *
     * @param stream the compressed stream
     * @param mode currently only decompression is supported
     * @param leaveOpen true to leave the stream open after disposing the
     *            LzoStream object; otherwise, false
     */
    public LzoStream(Stream stream, CompressionMode mode, boolean leaveOpen) {
        if (mode != CompressionMode.Decompress)
            throw new IllegalArgumentException("Compression is not supported");
        if (!stream.canRead())
            throw new IllegalArgumentException("write-only stream cannot be used for decompression");
        _source = stream;
        if (!BufferedStream.class.isInstance(stream))
            _source = new BufferedStream(stream);
        _leaveOpen = leaveOpen;
        decodeFirstByte();
    }

    private void decodeFirstByte() {
        _instruction = _source.readByte();
        if (_instruction == -1)
            throw new EndOfStreamException();
        if (_instruction > 15 && _instruction <= 17) {
            throw new IllegalStateException();
        }
    }

    private void copy(byte[] buffer, int offset, int count) {
        assert (count > 0);
        do {
            int read = _source.read(buffer, offset, count);
            if (read == 0)
                throw new EndOfStreamException();
            _ringBuffer.write(buffer, offset, read);
            offset += read;
            count -= read;
        } while (count > 0);
    }

    protected int decode(byte[] buffer, int offset, int count) {
        assert (count > 0);
        assert (_decodedBuffer == null);
        int read;
        int i = _instruction >> 4;
        switch (i) {
        case 0: { // Instruction <= 15
            /*
             * Depends on the number of literals copied by the last instruction.
             */
            switch (_state) {
            case ZeroCopy: {
                /*
                 * this encoding will be a copy of 4 or more literal, and must
                 * be interpreted like this : * 0 0 0 0 L L L L (0..15) : copy
                 * long literal string length = 3 + (L ?: 15 + (zero_bytes *
                 * 255) + non_zero_byte) state = 4 (no extra literals are
                 * copied)
                 */
                int length = 3;
                if (_instruction != 0) {
                    length += _instruction;
                } else {
                    length += 15 + readLength();
                }
                _state = LzoState.LargeCopy;
                if (length <= count) {
                    copy(buffer, offset, length);
                    read = length;
                } else {
                    copy(buffer, offset, count);
                    _decodedBuffer = new byte[length - count];
                    copy(_decodedBuffer, 0, length - count);
                    read = count;
                }
                break;
            }
            case SmallCopy1:
            case SmallCopy2:
            case SmallCopy3:
                read = smallCopy(buffer, offset, count);
                break;
            case LargeCopy:
                read = largeCopy(buffer, offset, count);
                break;
            default:
                throw new IllegalArgumentException();
            }
            break;
        }
        case 1: // Instruction < 32
        {
            /*
             * 0 0 0 1 H L L L (16..31) Copy of a block within 16..48kB distance
             * (preferably less than 10B) length = 2 + (L ?: 7 + (zero_bytes *
             * 255) + non_zero_byte) Always followed by exactly one LE16 : D D D
             * D D D D D : D D D D D D S S distance = 16384 + (H << 14) + D
             * state = S (copy S literals after this block) End of stream is
             * reached if distance == 16384
             */
            int length = (_instruction & 0x7) + 2;
            if (length == 2) {
                length += 7 + readLength();
            }
            int s = _source.readByte();
            int d = _source.readByte();
            if (s != -1 && d != -1) {
                d = ((d << 8) | s) >> 2;
                int distance = 16384 + ((_instruction & 0x8) << 11) | d;
                if (distance == 16384)
                    return -1;

                read = copyFromRingBuffer(buffer, offset, count, distance, length, s & 0x3);
                break;
            }
            throw new EndOfStreamException();
        }
        case 2: // Instruction < 48
        case 3: // Instruction < 64
        {
            /*
             * 0 0 1 L L L L L (32..63) Copy of small block within 16kB distance
             * (preferably less than 34B) length = 2 + (L ?: 31 + (zero_bytes *
             * 255) + non_zero_byte) Always followed by exactly one LE16 : D D D
             * D D D D D : D D D D D D S S distance = D + 1 state = S (copy S
             * literals after this block)
             */
            int length = (_instruction & 0x1f) + 2;
            if (length == 2) {
                length += 31 + readLength();
            }
            int s = _source.readByte();
            int d = _source.readByte();
            if (s != -1 && d != -1) {
                d = ((d << 8) | s) >> 2;
                int distance = d + 1;

                read = copyFromRingBuffer(buffer, offset, count, distance, length, s & 0x3);
                break;
            }
            throw new EndOfStreamException();
        }
        case 4:// Instruction < 80
        case 5:// Instruction < 96
        case 6:// Instruction < 112
        case 7:// Instruction < 128
        {
            /*
             * 0 1 L D D D S S (64..127) Copy 3-4 bytes from block within 2kB
             * distance state = S (copy S literals after this block) length = 3
             * + L Always followed by exactly one byte : H H H H H H H H
             * distance = (H << 3) + D + 1
             */
            int length = 3 + ((_instruction >> 5) & 0x1);
            int result = _source.readByte();
            if (result != -1) {
                int distance = (result << 3) + ((_instruction >> 2) & 0x7) + 1;

                read = copyFromRingBuffer(buffer, offset, count, distance, length, _instruction & 0x3);
                break;
            }
            throw new EndOfStreamException();
        }
        default: {
            /*
             * 1 L L D D D S S (128..255) Copy 5-8 bytes from block within 2kB
             * distance state = S (copy S literals after this block) length = 5
             * + L Always followed by exactly one byte : H H H H H H H H
             * distance = (H << 3) + D + 1
             */
            int length = 5 + ((_instruction >> 5) & 0x3);
            int result = _source.readByte();
            if (result != -1) {
                int distance = (result << 3) + ((_instruction & 0x1c) >> 2) + 1;

                read = copyFromRingBuffer(buffer, offset, count, distance, length, _instruction & 0x3);
                break;
            }
            throw new EndOfStreamException();
        }
        }
        _instruction = _source.readByte();
        if (_instruction != -1) {
            _outputPosition += read;
            return read;
        }
        throw new EndOfStreamException();
    }

    private int largeCopy(byte[] buffer, int offset, int count) {
        /*
         * the instruction becomes a copy of a 3-byte block from the dictionary
         * from a 2..3kB distance, and must be interpreted like this : 0 0 0 0 D
         * D S S (0..15) : copy 3 bytes from 2..3 kB distance length = 3 state =
         * S (copy S literals after this block) Always followed by exactly one
         * byte : H H H H H H H H distance = (H << 2) + D + 2049
         */
        int result = _source.readByte();
        if (result != -1) {
            int distance = (result << 2) + ((_instruction & 0xc) >> 2) + 2049;

            return copyFromRingBuffer(buffer, offset, count, distance, 3, _instruction & 0x3);
        }
        throw new EndOfStreamException();
    }

    private int smallCopy(byte[] buffer, int offset, int count) {
        /*
         * the instruction is a copy of a 2-byte block from the dictionary
         * within a 1kB distance. It is worth noting that this instruction
         * provides little savings since it uses 2 bytes to encode a copy of 2
         * other bytes but it encodes the number of following literals for free.
         * It must be interpreted like this : 0 0 0 0 D D S S (0..15) : copy 2
         * bytes from <= 1kB distance length = 2 state = S (copy S literals
         * after this block) Always followed by exactly one byte : H H H H H H H
         * H distance = (H << 2) + D + 1
         */
        int h = _source.readByte();
        if (h != -1) {
            int distance = (h << 2) + ((_instruction & 0xc) >> 2) + 1;

            return copyFromRingBuffer(buffer, offset, count, distance, 2, _instruction & 0x3);
        }

        throw new EndOfStreamException();
    }

    private int readLength() {
        int b;
        int length = 0;
        while ((b = _source.readByte()) == 0) {
            if (length >= Integer.MAX_VALUE - 1000) {
                throw new IllegalStateException();
            }
            length += 255;
        }
        if (b != -1)
            return length + b;
        throw new EndOfStreamException();
    }

    private int copyFromRingBuffer(byte[] buffer, int offset, int count, int distance, int copy, int state) {
        assert (copy >= 0);
        int result = copy + state;
        _state = LzoState.values()[state];
        if (count >= result) {
            int size = copy;
            if (copy > distance) {
                size = distance;
                _ringBuffer.copy(buffer, offset, distance, size);
                copy -= size;
                int copies = copy / distance;
                for (int i = 0; i < copies; i++) {
                    System.arraycopy(buffer, offset, buffer, offset + size, size);
                    offset += size;
                    copy -= size;
                }
                if (copies > 0) {
                    int length = size * copies;
                    _ringBuffer.write(buffer, offset - length, length);
                }
                offset += size;
            }
            if (copy > 0) {
                if (copy < size)
                    size = copy;
                _ringBuffer.copy(buffer, offset, distance, size);
                offset += size;
            }
            if (state > 0) {
                copy(buffer, offset, state);
            }
            return result;
        }

        if (count <= copy) {
            copyFromRingBuffer(buffer, offset, count, distance, count, 0);
            _decodedBuffer = new byte[result - count];
            copyFromRingBuffer(_decodedBuffer, 0, _decodedBuffer.length, distance, copy - count, state);
            return count;
        }
        copyFromRingBuffer(buffer, offset, count, distance, copy, 0);
        int remaining = count - copy;
        _decodedBuffer = new byte[state - remaining];
        copy(buffer, offset + copy, remaining);
        copy(_decodedBuffer, 0, state - remaining);
        return count;
    }

    private int readInternal(byte[] buffer, int offset, int count) {
        assert count > 0;
        if (_length.isPresent() && _outputPosition >= _length.get())
            return -1;
        int read;
        if (_decodedBuffer == null) {
            if ((read = decode(buffer, offset, count)) >= 0)
                return read;
            _length = Optional.of(_outputPosition);
            return -1;
        }
        int decodedLength = _decodedBuffer.length;
        if (count > decodedLength) {
            System.arraycopy(_decodedBuffer, 0, buffer, offset, decodedLength);
            _decodedBuffer = null;
            _outputPosition += decodedLength;
            return decodedLength;
        }
        System.arraycopy(_decodedBuffer, 0, buffer, offset, count);
        if (decodedLength > count) {
            byte[] remaining = new byte[decodedLength - count];
            System.arraycopy(_decodedBuffer, count, remaining, 0, remaining.length);
            _decodedBuffer = remaining;
        } else {
            _decodedBuffer = null;
        }
        _outputPosition += count;
        return count;
    }

    public boolean canRead() {
        return true;
    }

    public boolean canSeek() {
        return false;
    }

    public boolean canWrite() {
        return false;
    }

    public long getLength() {
        if (_length.isPresent())
            return _length.get();
        throw new UnsupportedOperationException();
    }

    public long getPosition() {
        return _outputPosition;
    }

    public void setPosition(long value) {
        if (_outputPosition == value)
            return;
        seek(value, SeekOrigin.Begin);
    }

    public void flush() {
    }

    public int read(byte[] buffer, int offset, int count) {
        if (_length.isPresent() && _outputPosition >= _length.get())
            return 0;
        int result = 0;
        while (count > 0) {
            int read = readInternal(buffer, offset, count);
            if (read == -1)
                return result;
            result += read;
            offset += read;
            count -= read;
        }
        return result;
    }

    public long seek(long offset, SeekOrigin origin) {
        throw new UnsupportedOperationException();
    }

    public void setLength(long value) {
        _length = Optional.of(value);
    }

    public void write(byte[] buffer, int offset, int count) {
        throw new IOException("cannot write to final stream");
    }

    public void close() throws java.io.IOException {
        if (!_leaveOpen)
            _source.close();
    }
}
