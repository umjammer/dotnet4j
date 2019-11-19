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


/**
 * fixed sized ring buffer
 */
public class RingBuffer {
    private final byte[] _buffer;

    private int _position;

    private final int _size;

    /**
     * create a new RingBuffer with the specified size
     *
     * @param size the size of the buffer
     */
    public RingBuffer(int size) {
        _buffer = new byte[size];
        _size = size;
    }

    /**
     * set the position relative to the current position
     * 
     * <remarks>wraps the position of the end is reached</remarks>
     * 
     * @param offset relative offset
     */
    public void seek(int offset) {
        _position += offset;
        if (_position > _size) {
            do {
                _position -= _size;
            } while (_position > _size);
            return;
        }
        while (_position < 0) {
            _position += _size;
        }
    }

    /**
     * copies as sequence of bytes from the RingBuffer at the specified distance
     * into the buffer and also the RingBuffer itself
     *
     * @param buffer An array of bytes. When this method returns, the buffer
     *            contains the specified byte array with the values between
     *            offset and (offset + count - 1) replaced by the bytes read
     *            from the RingBuffer
     * @param offset The zero-based byte offset in buffer at which to begin
     *            storing the data read from the RingBuffer
     * @param distance The distance to seek backwards before starting to copy
     * @param count The maximum number of bytes to be read from the RingBuffer
     */
    public void copy(byte[] buffer, int offset, int distance, int count) {
        if (_position - distance > 0 && _position + count < _size) {
            if (count < 10) {
                do {
                    byte value = _buffer[_position - distance];
                    _buffer[_position++] = value;
                    buffer[offset++] = value;
                } while (--count > 0);
            } else {
                System.arraycopy(_buffer, _position - distance, buffer, offset, count);
                System.arraycopy(buffer, offset, _buffer, _position, count);
                _position += count;
            }
        } else {
            seek(-distance);
            read(buffer, offset, count);
            seek(distance - count);
            write(buffer, offset, count);
        }
    }

    /**
     * reads a sequence of bytes from the RingBuffer and advances the position
     * within the RingBuffer by the number of bytes read
     *
     * @param buffer An array of bytes. When this method returns, the buffer
     *            contains the specified byte array with the values between
     *            offset and (offset + count - 1) replaced by the bytes read
     *            from the RingBuffer
     * @param offset The zero-based byte offset in buffer at which to begin
     *            storing the data read from the RingBuffer
     * @param count The maximum number of bytes to be read from the RingBuffer
     */
    public void read(byte[] buffer, int offset, int count) {
        if (count < 10 && (_position + count) < _size) {
            do {
                buffer[offset++] = _buffer[_position++];
            } while (--count > 0);
        } else {
            while (count > 0) {
                int copy = _size - _position;
                if (copy > count) {
                    System.arraycopy(_buffer, _position, buffer, offset, count);
                    _position += count;
                    break;
                }
                System.arraycopy(_buffer, _position, buffer, offset, copy);
                _position = 0;
                count -= copy;
                offset += copy;
            }
        }
    }

    /**
     * writes a sequence of bytes to the RingBuffer and advances the current
     * position within this RingBuffer by the number of bytes written
     * 
     * @param buffer An array of bytes. This method copies count bytes from
     *            buffer to the RingBuffer.
     * @param offset The zero-based byte offset in buffer at which to begin
     *            copying bytes to the RingBuffer.
     * @param count The number of bytes to be written to the RingBuffer.
     */
    public void write(byte[] buffer, int offset, int count) {
        if (count < 10 && (_position + count) < _size) {
            do {
                _buffer[_position++] = buffer[offset++];
            } while (--count > 0);
        } else {
            while (count > 0) {
                int cnt = _size - _position;
                if (cnt > count) {
                    System.arraycopy(buffer, offset, _buffer, _position, count);
                    _position += count;
                    return;
                }
                System.arraycopy(buffer, offset, _buffer, _position, cnt);
                _position = 0;
                offset += cnt;
                count -= cnt;
            }
        }
    }

    /**
     * creates a deep clone
     */
    public RingBuffer clone() {
        RingBuffer result = new RingBuffer(_size);
        result._position = _position;
        System.arraycopy(_buffer, 0, result._buffer, 0, _size);
        return result;
    }
}
