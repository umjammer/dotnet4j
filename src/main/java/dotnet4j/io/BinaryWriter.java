//
// Copyright (c) Microsoft Corporation.  All rights reserved.
//

package dotnet4j.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;


/**
 * Provides a way to write primitives types in
 * binary from a Stream, while also supporting writing Strings
 * in a particular encoding.
 * <p>
 * This abstract base class represents a writer that can write
 * primitives to an arbitrary stream. A subclass can override methods to
 * give unique encodings.
 * </p>
 *
 * @author gpaperin
 */
public class BinaryWriter implements Serializable, Closeable {

    public static final BinaryWriter Null;

    static {
        Null = new BinaryWriter();
    }

    protected Stream outStream;
    /** temp space for writing primitives to. */
    private final byte[] buffer;
    private final Charset encoding;
    private final CharsetEncoder encoder;

    //[OptionalField]  // New in .NET FX 4.5.  False is the right default value.
    private boolean leaveOpen;

    // Perf optimization stuff

    // temp space for writing chars.
    private byte[] largeByteBuffer;
    // max # of chars we can put in this.largeByteBuffer
    private int maxChars;
    // Size should be around the max number of chars/string * Charset's max bytes/char
    private static final int LargeByteBufferSize = 256;

    // Protected default constructor that sets the output stream
    // to a null stream (a bit bucket).
    protected BinaryWriter() {
        outStream = Stream.Null;
        this.buffer = new byte[16];
        this.encoding = StandardCharsets.UTF_8;
        this.encoder = this.encoding.newEncoder();
    }

    public BinaryWriter(Stream output) {
        this(output, StandardCharsets.UTF_8, false);
    }

    public BinaryWriter(Stream output, Charset encoding) {
        this(output, encoding, false);
    }

    public BinaryWriter(Stream output, Charset encoding, boolean leaveOpen) {
        if (output == null)
            throw new NullPointerException("output");
        if (encoding == null)
            throw new NullPointerException("encoding");
        if (!output.canWrite())
            throw new IllegalArgumentException("Argumentthis.StreamNotWritable");

        outStream = output;
        this.buffer = new byte[16];
        this.encoding = encoding;
        this.encoder = this.encoding.newEncoder();
        this.leaveOpen = leaveOpen;
    }

    // Closes this writer and releases any system resources associated with the
    // writer. Following a call to Close, any operations on the writer
    // may raise exceptions.
    @Override
    public void close() throws IOException {
        if (this.leaveOpen)
            outStream.flush();
        else
            outStream.close();
    }

    /**
     * Returns the stream associate with the writer. It flushes all pending
     * writes before returning. All subclasses should override Flush to
     * ensure that all buffered data is sent to the stream.
     */
    public Stream getBaseStream() {
        flush();
        return outStream;
    }

    // Clears all buffers for this writer and causes any buffered data to be
    // written to the underlying device.
    public void flush() {
        outStream.flush();
    }

    public long seek(int offset, SeekOrigin origin) {
        return outStream.seek(offset, origin);
    }

    /**
     * Writes a booleanean to this stream. A single byte is written to the stream
     * with the value 0 representing false or the value 1 representing true.
     */
    public void write(boolean value) {
        this.buffer[0] = (byte) (value ? 1 : 0);
        outStream.write(this.buffer, 0, 1);
    }

    // Writes a byte to this stream. The current position of the stream is
    // advanced by one.
    //
    public void write(byte value) {
        outStream.writeByte(value);
    }

    // Writes a signed byte to this stream. The current position of the stream
    // is advanced by one.
    //
    //[CLSCompliant(false)]
//    public void Write(byte value) {
//        outStream.writeByte((byte) value);
//    }

    // Writes a byte array to this stream.
    //
    // This default implementation calls the Write(Object, int, int)
    // method to write the byte array.
    //
    public void write(byte[] buffer) {
        if (buffer == null)
            throw new NullPointerException("buffer");
        outStream.write(buffer, 0, buffer.length);
    }

    // Writes a section of a byte array to this stream.
    //
    // This default implementation calls the Write(Object, int, int)
    // method to write the byte array.
    //
    public void write(byte[] buffer, int index, int count) {
        outStream.write(buffer, index, count);
    }

    // Writes a character to this stream. The current position of the stream is
    // advanced by two.
    // Note this method cannot handle surrogates properly in UTF-8.
    //
    // [System.Security.SecuritySafeCritical]  // auto-generated
    public void write(char ch) throws IOException {
        if (Character.isLowSurrogate(ch))
            throw new IllegalArgumentException("Argthis.SurrogatesNotAllowedAsSingleChar");

        assert this.encoding.newEncoder().maxBytesPerChar() <= 16 : "this.encoding.GetMaxByteCount(1) <= 16)";
        int numBytes = 0;
        byte[] pBuffer = this.encoder.encode(CharBuffer.wrap(new char[] { ch })).array();
        System.arraycopy(pBuffer, 0, this.buffer, 0, pBuffer.length);
        outStream.write(this.buffer, 0, numBytes);
    }

    // Writes a character array to this stream.
    //
    // This default implementation calls the Write(Object, int, int)
    // method to write the character array.
    //
    public void write(char[] chars) {
        if (chars == null)
            throw new NullPointerException("chars");

        byte[] bytes = this.encoding.encode(CharBuffer.wrap(chars)).array();
        outStream.write(bytes, 0, bytes.length);
    }

    // Writes a section of a character array to this stream.
    //
    // This default implementation calls the Write(Object, int, int)
    // method to write the character array.
    //
    public void write(char[] chars, int index, int count) {
        byte[] bytes = this.encoding.encode(CharBuffer.wrap(chars, index, count)).array();
        outStream.write(bytes, 0, bytes.length);
    }


    // Writes a double to this stream. The current position of the stream is
    // advanced by eight.
    //
    // [System.Security.SecuritySafeCritical]  // auto-generated
    public void write(double value) {
        long TmpValue = Double.doubleToLongBits(value);
        this.buffer[0] = (byte) TmpValue;
        this.buffer[1] = (byte) (TmpValue >> 8);
        this.buffer[2] = (byte) (TmpValue >> 16);
        this.buffer[3] = (byte) (TmpValue >> 24);
        this.buffer[4] = (byte) (TmpValue >> 32);
        this.buffer[5] = (byte) (TmpValue >> 40);
        this.buffer[6] = (byte) (TmpValue >> 48);
        this.buffer[7] = (byte) (TmpValue >> 56);
        outStream.write(this.buffer, 0, 8);
    }

//    public void write(decimal value) {
//        Decimal.GetBytes(value, this.buffer);
//        outStream.Write(this.buffer, 0, 16);
//    }

    // Writes a two-byte signed integer to this stream. The current position of
    // the stream is advanced by two.
    //
    public void write(short value) {
        this.buffer[0] = (byte) value;
        this.buffer[1] = (byte) (value >> 8);
        outStream.write(this.buffer, 0, 2);
    }

//    /**
//     * Writes a two-byte unsigned integer to this stream. The current position
//     * of the stream is advanced by two.
//     */
    // [CLSCompliant(false)]
//    public void Write(short value) {
//        this.buffer[0] = (byte) value;
//        this.buffer[1] = (byte) (value >> 8);
//        outStream.write(this.buffer, 0, 2);
//    }

    /**
    // Writes a four-byte signed integer to this stream. The current position
    // of the stream is advanced by four.
     */
    public void write(int value) {
        this.buffer[0] = (byte) value;
        this.buffer[1] = (byte) (value >> 8);
        this.buffer[2] = (byte) (value >> 16);
        this.buffer[3] = (byte) (value >> 24);
        outStream.write(this.buffer, 0, 4);
    }

//    /**
//     * Writes a four-byte unsigned integer to this stream. The current position
//     * of the stream is advanced by four.
//     */
    //[CLSCompliant(false)]
//    public void Write(int value) {
//        this.buffer[0] = (byte) value;
//        this.buffer[1] = (byte) (value >> 8);
//        this.buffer[2] = (byte) (value >> 16);
//        this.buffer[3] = (byte) (value >> 24);
//        outStream.write(this.buffer, 0, 4);
//    }

    /**
    // Writes an eight-byte signed integer to this stream. The current position
    // of the stream is advanced by eight.
     */
    public void write(long value) {
        this.buffer[0] = (byte) value;
        this.buffer[1] = (byte) (value >> 8);
        this.buffer[2] = (byte) (value >> 16);
        this.buffer[3] = (byte) (value >> 24);
        this.buffer[4] = (byte) (value >> 32);
        this.buffer[5] = (byte) (value >> 40);
        this.buffer[6] = (byte) (value >> 48);
        this.buffer[7] = (byte) (value >> 56);
        outStream.write(this.buffer, 0, 8);
    }

    // Writes an eight-byte unsigned integer to this stream. The current
    // position of the stream is advanced by eight.
    //
    //[CLSCompliant(false)]
//    public void write(long value) {
//        this.buffer[0] = (byte) value;
//        this.buffer[1] = (byte) (value >> 8);
//        this.buffer[2] = (byte) (value >> 16);
//        this.buffer[3] = (byte) (value >> 24);
//        this.buffer[4] = (byte) (value >> 32);
//        this.buffer[5] = (byte) (value >> 40);
//        this.buffer[6] = (byte) (value >> 48);
//        this.buffer[7] = (byte) (value >> 56);
//        outStream.write(this.buffer, 0, 8);
//    }

    // Writes a float to this stream. The current position of the stream is
    // advanced by four.
    //
    //[System.Security.SecuritySafeCritical]  // auto-generated
    public void write(float value) {
        int TmpValue = Float.floatToIntBits(value);
        this.buffer[0] = (byte) TmpValue;
        this.buffer[1] = (byte) (TmpValue >> 8);
        this.buffer[2] = (byte) (TmpValue >> 16);
        this.buffer[3] = (byte) (TmpValue >> 24);
        outStream.write(this.buffer, 0, 4);
    }

    // Writes a length-prefixed string to this stream in the BinaryWriter's
    // current Charset. This method first writes the length of the string as
    // a four-byte unsigned integer, and then writes that many characters
    // to the stream.
    //
    //[System.Security.SecuritySafeCritical]  // auto-generated
    public void write(String value) throws IOException {
        if (value == null)
            throw new NullPointerException("value");

        int len = this.encoding.newEncoder().encode(CharBuffer.wrap(value.toCharArray())).array().length;
        write7BitEncodedInt(len);

        if (this.largeByteBuffer == null) {
            this.largeByteBuffer = new byte[LargeByteBufferSize];
            this.maxChars = (int) (this.largeByteBuffer.length / this.encoding.newEncoder().maxBytesPerChar());
        }

        if (len <= this.largeByteBuffer.length) {
            //Contract.Assert(len == this.encoding.GetBytes(chars, 0, chars.length, this.largeByteBuffer, 0), "encoding's GetByteCount & GetBytes gave different answers!  encoding type: "+this.encoding.GetType().Name);
            this.largeByteBuffer = this.encoding.encode(CharBuffer.wrap(value.toCharArray())).array();
            outStream.write(this.largeByteBuffer, 0, len);
        } else {
            // Aggressively try to not allocate memory in this loop for
            // runtime performance reasons.  Use an Encoder to write out
            // the string correctly (handling surrogates crossing buffer
            // boundaries properly).
            int charStart = 0;
            int numLeft = value.length();
int totalBytes = 0;
            while (numLeft > 0) {
                // Figure out how many chars to process this round.
                int charCount = (numLeft > this.maxChars) ? this.maxChars : numLeft;
                int byteLen;

                if (charStart < 0 || charCount < 0 || charStart + charCount > value.length()) {
                    throw new IndexOutOfBoundsException("charCount");
                }

                this.largeByteBuffer = this.encoder.encode(CharBuffer.wrap(value, charStart, charCount)).array();
                byteLen = this.largeByteBuffer.length;

totalBytes += byteLen;
assert totalBytes <= len && byteLen <= this.largeByteBuffer.length : "BinaryWriter::Write(String) - More bytes encoded than expected!";
                outStream.write(this.largeByteBuffer, 0, byteLen);
                charStart += charCount;
                numLeft -= charCount;
            }
assert totalBytes == len : "BinaryWriter::Write(String) - Didn't write out all the bytes!";
        }
    }

    // Write out an int 7 bits at a time.  The high bit of the byte,
    // when on, tells reader to continue reading more bytes.
    protected void write7BitEncodedInt(int value) {
        int v = value;   // support negative numbers
        while (v >= 0x80) {
            write((byte) (v | 0x80));
            v >>= 7;
        }
        write((byte) v);
    }
}
