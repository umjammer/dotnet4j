//
// Copyright (c) Microsoft Corporation.  All rights reserved.
//

package dotnet4j.io;

import java.io.Closeable;
import java.io.EOFException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import vavi.util.ByteUtil;


/**
 * Wraps a stream and provides convenient read functionality
 * for strings and primitive types.
 *
 * @author gpaperin
 */
public class BinaryReader implements Closeable {

    private static final int MaxCharBytesSize = 128;

    private Stream stream;
    private byte[] buffer;
    private Charset decoder;
    private byte[] charBytes;
    private char[] singleChar;
    private char[] charBuffer;
    private final int maxCharsSize;  // From MaxCharBytesSize & Encoding

    // Performance optimization for read() w/ Unicode.  Speeds us up by ~40%
    private final boolean twoBytesPerChar;
    // "do we sit on MemoryStream?" for read/ReadInt32 perf
    private final boolean isMemoryStream;
    private final boolean leaveOpen;

    public BinaryReader(Stream input) {
        this(input, StandardCharsets.UTF_8, false);
    }

    public BinaryReader(Stream input, Charset encoding) {
        this(input, encoding, false);
    }

    public BinaryReader(Stream input, Charset encoding, boolean leaveOpen) {
        if (input == null) {
            throw new NullPointerException("input");
        }
        if (encoding == null) {
            throw new NullPointerException("encoding");
        }
        if (!input.canRead())
            throw new IllegalArgumentException("Argument_StreamNotReadable");
        this.stream = input;
        this.decoder = encoding;
        this.maxCharsSize = Math.max((int) encoding.newDecoder().maxCharsPerByte(), MaxCharBytesSize);
        int minBufferSize = Math.max((int) encoding.newDecoder().maxCharsPerByte(), 1); // max bytes per one char
        if (minBufferSize < 16)
            minBufferSize = 16;
        this.buffer = new byte[minBufferSize];
        // this.charBuffer and this.charBytes will be left null.

        // For Encodings that always use 2 bytes per char (or more),
        // special case them here to make read() & Peek() faster.
        this.twoBytesPerChar = encoding.contains(StandardCharsets.UTF_16); // is UnicodeEncoding;
        // check if BinaryReader is based on MemoryStream, and keep this for its life
        // we cannot use "as" operator, since derived classes are not allowed
        this.isMemoryStream = this.stream instanceof MemoryStream;
        this.leaveOpen = leaveOpen;

        assert this.decoder != null : "[BinaryReader.ctor]this.decoder!=null";
    }

    private static int getMaxCharCount(byte[] bytes, String charsetName) {
        Charset charset = Charset.forName(charsetName);
        float maxBytesPerChar = charset.newDecoder().maxCharsPerByte();
        return (int) Math.ceil(bytes.length * maxBytesPerChar);
    }

    public Stream getBaseStream() {
        return this.stream;
    }

    @Override
    public void close() throws java.io.IOException {
        Stream copyOfStream = this.stream;
        this.stream = null;
        if (copyOfStream != null && !this.leaveOpen)
            copyOfStream.close();
        this.stream = null;
        this.buffer = null;
        this.decoder = null;
        this.charBytes = null;
        this.singleChar = null;
        this.charBuffer = null;
    }

    public int peekChar() throws java.io.IOException {
        if (this.stream == null) throw new java.io.IOException("FileNotOpen");

        if (!this.stream.canSeek())
            return -1;
        long origPos = this.stream.position();
        int ch = this.read();
        this.stream.position(origPos);
        assert ch >= -1;
        return ch;
    }

    public int read() throws java.io.IOException {
        if (this.stream == null) {
            throw new java.io.IOException("FileNotOpen");
        }
        int r = internalReadOneChar();
        assert r  >= -1;
        return r;
    }

    public boolean readBoolean() throws java.io.IOException {
        fillBuffer(1);
        return (this.buffer[0] != 0);
    }

    public byte readByte() throws java.io.IOException {
        // Inlined to avoid some method call overhead with FillBuffer.
        if (this.stream == null) throw new java.io.IOException("FileNotOpen");

        int b = this.stream.readByte();
        if (b == -1)
            throw new EOFException();
        return (byte) b;
    }

    // @CLSCompliant false
    public byte readSByte() throws java.io.IOException {
        fillBuffer(1);
        return this.buffer[0];
    }

    public char readChar() throws java.io.IOException {
        int value = read();
        if (value == -1) {
            throw new EOFException();
        }
        return (char) value;
    }

    public short readInt16() throws java.io.IOException {
        fillBuffer(2);
//        return (short) (this.buffer[0] | this.buffer[1] << 8);
        return ByteUtil.readLeShort(this.buffer);
    }

    // @CLSCompliant false
    public short readUInt16() throws java.io.IOException {
//        fillBuffer(2);
//        return (short) (this.buffer[0] | this.buffer[1] << 8);
        return readInt16();
    }

    public int readInt32() throws java.io.IOException {
        fillBuffer(4);
//        return this.buffer[0] | this.buffer[1] << 8 | this.buffer[2] << 16 | this.buffer[3] << 24;
        return ByteUtil.readLeInt(this.buffer);
    }

    // [CLSCompliant(false)]
    public int readUInt32() throws java.io.IOException {
//        fillBuffer(4);
//        return this.buffer[0] | this.buffer[1] << 8 | this.buffer[2] << 16 | this.buffer[3] << 24;
        return readInt32();
    }

    public long readInt64() throws java.io.IOException {
        fillBuffer(8);
//        int lo = this.buffer[0] | this.buffer[1] << 8 |
//                this.buffer[2] << 16 | this.buffer[3] << 24;
//        int hi = this.buffer[4] | this.buffer[5] << 8 |
//                this.buffer[6] << 16 | this.buffer[7] << 24;
//        return (long) hi << 32 | lo;
        return ByteUtil.readLeLong(this.buffer);
    }

    // [CLSCompliant(false)]
    public long readUInt64() throws java.io.IOException {
//        fillBuffer(8);
//        int lo = this.buffer[0] | this.buffer[1] << 8 |
//                this.buffer[2] << 16 | this.buffer[3] << 24;
//        int hi = this.buffer[4] | this.buffer[5] << 8 |
//                this.buffer[6] << 16 | this.buffer[7] << 24;
//        return ((long) hi) << 32 | lo;
        return readInt64();
    }

    // [System.Security.SecuritySafeCritical]  // auto-generated
    public float readSingle() throws java.io.IOException {
        fillBuffer(4);
        int tmpBuffer = this.buffer[0] | this.buffer[1] << 8 | this.buffer[2] << 16 | this.buffer[3] << 24;
        return Float.intBitsToFloat(tmpBuffer);
    }

    // [System.Security.SecuritySafeCritical]  // auto-generated
    public double readDouble() throws java.io.IOException {
        fillBuffer(8);
        int lo = this.buffer[0] | this.buffer[1] << 8 |
                this.buffer[2] << 16 | this.buffer[3] << 24;
        int hi = this.buffer[4] | this.buffer[5] << 8 |
                this.buffer[6] << 16 | this.buffer[7] << 24;

        long tmpBuffer = ((long) hi) << 32 | lo;
        return Double.longBitsToDouble(tmpBuffer);
    }

//    public Decimal readDecimal() throws java.io.IOException {
//        FillBuffer(16);
//        try {
//            return Decimal.ToDecimal(this.buffer);
//        } catch (IllegalArgumentException e) {
//            // ReadDecimal cannot leak out ArgumentException
//            throw new IOException("Arg_DecBitCtor", e);
//        }
//    }

    public String readString() throws java.io.IOException {
        if (this.stream == null)
            throw new java.io.IOException("FileNotOpen");

        int currPos = 0;
        int n;
        int stringLength;
        int readLength;
        int charsRead;

        // Length of the string in bytes, not chars
        stringLength = read7BitEncodedInt();
        if (stringLength < 0) {
            throw new IOException("IO.IO_InvalidStringLen_Len: " + stringLength);
        }

        if (stringLength == 0) {
            return "";
        }

        if (this.charBytes == null) {
            this.charBytes = new byte[MaxCharBytesSize];
        }

        if (this.charBuffer == null) {
            this.charBuffer = new char[this.maxCharsSize];
        }

        StringBuilder sb = null;
        do {
            readLength = Math.min((stringLength - currPos), MaxCharBytesSize);

            n = this.stream.read(this.charBytes, 0, readLength);
            if (n == 0) {
                throw new EOFException();
            }

            this.charBuffer = this.decoder.decode(ByteBuffer.wrap(this.charBytes)).array();
            charsRead = this.charBuffer.length;

            if (currPos == 0 && n == stringLength)
                return new String(this.charBuffer, 0, charsRead);

            if (sb == null)
                sb = new StringBuilder(stringLength); // Actual string length in chars may be smaller.
            sb.append(this.charBuffer, 0, charsRead);
            currPos += n;

        } while (currPos < stringLength);

        return sb.toString();
    }

    // [SecuritySafeCritical]
    public int read(char[] buffer, int index, int count) throws java.io.IOException {
        if (buffer == null) {
            throw new NullPointerException("buffer");
        }
        if (index < 0) {
            throw new IndexOutOfBoundsException("index: " + index);
        }
        if (count < 0) {
            throw new IndexOutOfBoundsException("count: " + count);
        }
        if (buffer.length - index < count) {
            throw new IllegalArgumentException("Argument_InvalidOffLen");
        }

        if (this.stream == null)
            throw new java.io.IOException("FileNotOpen");

        // SafeCritical: index and count have already been verified to be a valid range for the buffer
        int r = internalReadChars(buffer, index, count);
        assert r >= 0;
        assert r <= count;
        return r;
    }

    // [SecurityCritical]
    private int internalReadChars(char[] buffer, int index, int count) {
        assert buffer != null;
        assert index >= 0 && count >= 0;
        assert this.stream != null;

        int numBytes = 0;
        int charsRemaining = count;

        if (this.charBytes == null) {
            this.charBytes = new byte[MaxCharBytesSize];
        }

        while (charsRemaining > 0) {
            int charsRead = 0;
            // We really want to know what the minimum number of bytes per char
            // is for our encoding.  Otherwise for UnicodeEncoding we'd have to
            // do ~1+log(n) reads to read n characters.
            numBytes = charsRemaining;

            // special case for DecoderNLS subclasses when there is a hanging byte from the previous loop
//            DecoderNLS decoder = this.decoder as DecoderNLS;
//            if (decoder != null && decoder.HasState && numBytes > 1) {
//                numBytes -= 1;
//            }

            if (this.twoBytesPerChar)
                numBytes <<= 1;
            if (numBytes > MaxCharBytesSize)
                numBytes = MaxCharBytesSize;

            int position = 0;
            byte[] byteBuffer = null;
            if (this.isMemoryStream) {
                MemoryStream mStream = (MemoryStream) this.stream;
                assert mStream != null : "this.stream as MemoryStream != null";

                position = (int) mStream.position();
                byteBuffer = new byte[numBytes];
                numBytes = mStream.read(byteBuffer, 0, numBytes);
            } else {
                numBytes = this.stream.read(this.charBytes, 0, numBytes);
                byteBuffer = this.charBytes;
            }

            if (numBytes == 0) {
                return count - charsRemaining;
            }

            assert byteBuffer != null : "expected byteBuffer to be non-null";

            if (position < 0 || numBytes < 0 || position + numBytes > byteBuffer.length) {
                throw new IndexOutOfBoundsException("byteCount");
            }

            if (index < 0 || charsRemaining < 0 || index + charsRemaining > buffer.length) {
                throw new IndexOutOfBoundsException("charsRemaining");
            }

            // charsRead = this.decoder.GetChars(pBytes + position, numBytes, pChars + index, charsRemaining, false);
            char[] pChars = this.decoder.decode(ByteBuffer.wrap(byteBuffer, index, numBytes)).array();
            System.arraycopy(buffer, position, pChars, 0, charsRemaining);

            charsRemaining -= charsRead;
            index += charsRead;
        }

        // this should never fail
        assert charsRemaining >= 0 : "We read too many characters.";

        // we may have read fewer than the number of characters requested if end of stream reached
        // or if the encoding makes the char count too big for the buffer (e.g. fallback sequence)
        return count - charsRemaining;
    }

    private int internalReadOneChar() {
        // I know having a separate InternalReadOneChar method seems a little
        // redundant, but this makes a scenario like the security parser code
        // 20% faster, in addition to the optimizations for UnicodeEncoding I
        // put in InternalReadChars.
        int charsRead = 0;
        int numBytes = 0;
        long posSav = posSav = 0;

        if (this.stream.canSeek())
            posSav = this.stream.position();

        if (this.charBytes == null) {
            this.charBytes = new byte[MaxCharBytesSize]; //
        }
        if (this.singleChar == null) {
            this.singleChar = new char[1];
        }

        while (charsRead == 0) {
            // We really want to know what the minimum number of bytes per char
            // is for our encoding.  Otherwise for UnicodeEncoding we'd have to
            // do ~1+log(n) reads to read n characters.
            // Assume 1 byte can be 1 char unless this.2BytesPerChar is true.
            numBytes = this.twoBytesPerChar ? 2 : 1;

            int r = this.stream.readByte();
            this.charBytes[0] = (byte) r;
            if (r == -1)
                numBytes = 0;
            if (numBytes == 2) {
                r = this.stream.readByte();
                this.charBytes[1] = (byte) r;
                if (r == -1)
                    numBytes = 1;
            }

            if (numBytes == 0) {
                // Console.WriteLine("Found no bytes.  We're outta here.");
                return -1;
            }

            assert numBytes == 1 || numBytes == 2 : "BinaryReader::InternalReadOneChar assumes it's reading one or 2 bytes only.";

            this.singleChar = this.decoder.decode(ByteBuffer.wrap(this.charBytes, 0, numBytes)).array();
            charsRead = this.singleChar.length;

            assert charsRead < 2 : "InternalReadOneChar - assuming we only got 0 or 1 char, not 2!";
//logger.log(Level.TRACE, "That became: " + charsRead + " characters.");
        }
        if (charsRead == 0)
            return -1;
        return this.singleChar[0];
    }

    // [SecuritySafeCritical]
    public char[] readChars(int count) throws java.io.IOException {
        if (count < 0) {
            throw new IndexOutOfBoundsException("count: " + count);
        }
        char[] chars = null;
        try {
            if (this.stream == null) {
                throw new java.io.IOException("FileNotOpen");
            }

            if (count == 0) {
                return new char[0];
            }

            // SafeCritical: we own the chars buffer, and therefore can guarantee that the index and count are valid
            chars = new char[count];
            int n = internalReadChars(chars, 0, count);
            if (n != count) {
                char[] copy = new char[n];
                System.arraycopy(chars, 0, copy, 0, 2 * n); // sizeof(char)
                chars = copy;
            }

            return chars;
        } finally {
            assert chars != null;
            assert chars.length <= count;
        }
    }

    public int read(byte[] buffer, int index, int count) throws java.io.IOException {
        if (buffer == null)
            throw new NullPointerException("buffer");
        if (index < 0)
            throw new IndexOutOfBoundsException("index");
        if (count < 0)
            throw new IndexOutOfBoundsException("count");
        if (buffer.length - index < count)
            throw new IllegalArgumentException("Argument_InvalidOffLen");

        if (this.stream == null) throw new java.io.IOException("FileNotOpen");
        int r = 0;
        try {
            r = this.stream.read(buffer, index, count);
            return r;
        } finally {
            assert r >= 0;
            assert r <= count;
        }
    }

    public byte[] readBytes(int count) throws java.io.IOException {
        if (count < 0)
            throw new IndexOutOfBoundsException("count: " + count);
        if (this.stream == null) throw new java.io.IOException("FileNotOpen");

        if (count == 0) {
            return new byte[0];
        }

        byte[] result = new byte[count];

        int numRead = 0;
        do {
            int n = this.stream.read(result, numRead, count);
            if (n == 0)
                break;
            numRead += n;
            count -= n;
        } while (count > 0);

        if (numRead != result.length) {
            // Trim array.  This should happen on EOF & possibly net streams.
            byte[] copy = new byte[numRead];
            System.arraycopy(result, 0, copy, 0, numRead);
            result = copy;
        }

        return result;
    }

    protected void fillBuffer(int numBytes) throws java.io.IOException {
        if (this.buffer != null && (numBytes < 0 || numBytes > this.buffer.length)) {
            throw new IndexOutOfBoundsException("numBytes: " + numBytes);
        }
        int bytesRead = 0;
        int n = 0;

        if (this.stream == null) throw new java.io.IOException("FileNotOpen");

        // Need to find a good threshold for calling ReadByte() repeatedly
        // vs. calling read(byte[], int, int) for both buffered & unbuffered
        // streams.
        if (numBytes == 1) {
            n = this.stream.readByte();
            if (n == -1)
                throw new EOFException();
            this.buffer[0] = (byte) n;
            return;
        }

        do {
            n = this.stream.read(this.buffer, bytesRead, numBytes - bytesRead);
            if (n == 0) {
                throw new EOFException();
            }
            bytesRead += n;
        } while (bytesRead < numBytes);
    }

    int read7BitEncodedInt() throws java.io.IOException {
        // read out an Int32 7 bits at a time.  The high bit
        // of the byte when on means to continue reading more bytes.
        int count = 0;
        int shift = 0;
        byte b;
        do {
            // Check for a corrupted stream.  read a max of 5 bytes.
            // In a future version, add a DataFormatException.
            if (shift == 5 * 7)  // 5 bytes max per Int32, shift += 7
                throw new NumberFormatException("Format_Bad7BitInt32");

            // ReadByte handles end of stream cases for us.
            b = readByte();
            count |= (b & 0x7F) << shift;
            shift += 7;
        } while ((b & 0x80) != 0);
        return count;
    }
}
