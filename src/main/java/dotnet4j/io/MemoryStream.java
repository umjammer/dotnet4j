//
// Copyright (c) Microsoft Corporation.  All rights reserved.
//

package dotnet4j.io;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;


/**
 * Purpose: A Stream whose backing store is memory. Great for temporary storage
 * without creating a temp file. Also lets users expose a byte[] as a stream.
 *
 * @author Microsoft
 * @author ft on 28.03.17.
 */
public class MemoryStream extends Stream implements Serializable {

    private static final long serialVersionUID = -7301303954168934077L;

    private byte[] buffer;

    private int capacity;

    private int position;

    private int origin;

    private int length;

    private boolean seekable;

    private boolean writable;

    private boolean expandable;

    private boolean closed;

    private boolean publiclyVisible;

    public MemoryStream() {
        this(0);
    }

    public MemoryStream(int capacity) {
        if (capacity < 0)
            throw new IndexOutOfBoundsException("capacity is negative");

        this.buffer = new byte[capacity];
        this.position = 0;
        this.origin = 0;
        this.capacity = capacity;

        this.publiclyVisible = true;
        this.seekable = true;
        this.writable = true;
        this.expandable = true;
    }

    public MemoryStream(byte[] buffer) {
        this(buffer, 0, buffer.length, true, false);
    }

    public MemoryStream(byte[] buffer, boolean writable) {
        this(buffer, 0, buffer.length, writable);
    }

    /** */
    public MemoryStream(byte[] buffer, int index, int count) {
        this(buffer, index, count, true);
    }

    public MemoryStream(byte[] buffer, int index, int count, boolean writable) {
        this(buffer, index, count, writable, false);
    }

    /** */
    public MemoryStream(byte[] buffer, int index, int count, boolean writable, boolean publiclyVisible) {
        if (buffer == null)
            throw new NullPointerException("buffer");
        if (index < 0 || count < 0)
            throw new IndexOutOfBoundsException("index or count is negative");
        if (buffer.length - index < count)
            throw new IndexOutOfBoundsException("buffer.length - index < count");

        this.buffer = buffer;
        this.position = index;
        this.origin = index;
        this.length = index + count;
        this.publiclyVisible = publiclyVisible;

        this.expandable = false;
        this.seekable = true;
        this.writable = writable;
        this.capacity = index + count;
    }

    @Override
    public boolean canRead() {
        if (closed)
            return false;
        return true;
    }

    @Override
    public boolean canSeek() {
        if (closed)
            return false;
        return seekable;
    }

    @Override
    public boolean canWrite() {
        if (closed)
            return false;
        return writable;
    }

    private boolean ensureCapacity(int value) {
        if (value < 0)
            throw new dotnet4j.io.IOException("value is negative");

        if (value > capacity) {
            int newCapacity = value;
            if (newCapacity < 256)
                newCapacity = 256;
            if (newCapacity < capacity * 2)
                newCapacity = capacity * 2;
            if ((long) capacity * 2 > Integer.MAX_VALUE)
                newCapacity = Integer.MAX_VALUE;

            setCapacity(newCapacity);
            return true;
        }
        return false;
    }

    public int getCapacity() {
        if (closed) {
            throw new dotnet4j.io.IOException("object disposed");
        }
        return capacity - origin;
    }

    public void setCapacity(int value) {
        if (value < getLength())
            throw new IndexOutOfBoundsException("value");
        if (closed)
            throw new dotnet4j.io.IOException("object disposed");
        if (!expandable && value != getCapacity())
            throw new dotnet4j.io.IOException("not expandable");

        if (expandable && value != capacity) {
            if (value > 0) {
                byte[] newBuffer = new byte[value];
                if (length > 0) {
                    System.arraycopy(buffer, 0, newBuffer, 0, length);
                }
                buffer = newBuffer;
            } else {
                buffer = null;
            }
            capacity = value;
        }
    }

    @Override
    public long getLength() {
        if (closed)
            throw new dotnet4j.io.IOException("object disposed");

        return length - origin;
    }

    @Override
    public void setLength(long value) {
        if (value < 0 || value > Integer.MAX_VALUE)
            throw new IndexOutOfBoundsException("value is negative or overflown");
        if (value > (Integer.MAX_VALUE - origin))
            throw new IndexOutOfBoundsException("value is overflown");
        if (!canWrite())
            throw new dotnet4j.io.IOException("not writable");

        int newLength = origin + (int) value;
        boolean allocatedNewArray = ensureCapacity(newLength);
        if (!allocatedNewArray && newLength > length)
            Arrays.fill(buffer, length, newLength, (byte) 0);
        length = newLength;
        if (position > newLength) {
            position = newLength;
        }
    }

    @Override
    public long getPosition() {
        if (closed)
            throw new dotnet4j.io.IOException("object disposed");

        return position - origin;
    }

    @Override
    public void setPosition(long value) {
        if (value < 0 || value > Integer.MAX_VALUE)
            throw new IndexOutOfBoundsException("value is negative or overflown");
        if (closed)
            throw new dotnet4j.io.IOException("object disposed");

        position = origin + (int) value;
    }

    @Override
    public void flush() {
    }

    @Override
    public long seek(long offset, SeekOrigin loc) {
        if (closed)
            throw new dotnet4j.io.IOException("object disposed");
        if (offset > Integer.MAX_VALUE)
            throw new IndexOutOfBoundsException("value is overflown");

        switch (loc) {
        case Begin: {
            int tempPosition = origin + (int) offset;
            if (offset < 0 || tempPosition < origin)
                throw new dotnet4j.io.IOException("invalid value");
            position = tempPosition;
        }
            break;
        case Current: {
            int tempPosition = position + (int) offset;
            if (position + offset < origin || tempPosition < origin)
                throw new dotnet4j.io.IOException("invalid value");
            position = tempPosition;
        }
            break;
        case End: {
            int tempPosition = length + (int) offset;
            if (length + offset < origin || tempPosition < origin)
                throw new dotnet4j.io.IOException("invalid value");
            position = tempPosition;
        }
            break;
        }

        assert position >= 0 : "position >= 0";
        return this.position;
    }

    @Override
    public int read(byte[] buffer, int offset, int count) {
        if (buffer == null)
            throw new NullPointerException("buffer");
        if (offset < 0)
            throw new IndexOutOfBoundsException("offset");
        if (count < 0)
            throw new IndexOutOfBoundsException("count");
        if (buffer.length - offset < count)
            throw new IllegalArgumentException("buffer.length - offset <= count");
        if (closed)
            throw new dotnet4j.io.IOException("object disposed");

        int n = length - position;
        if (n > count) {
            n = count;
        }
        if (n <= 0) {
            return 0;
        }

        assert position + n >= 0 : "position + n >= 0";

        System.arraycopy(this.buffer, position, buffer, offset, n);
        position += n;

        return n;
    }

    @Override
    public void write(byte[] buffer, int offset, int count) {
        if (buffer == null)
            throw new NullPointerException("buffer");
        if (offset < 0)
            throw new IndexOutOfBoundsException("offset");
        if (count < 0)
            throw new IndexOutOfBoundsException("count");
        if (buffer.length - offset < count)
            throw new IllegalArgumentException("buffer.length - offset <= count");
        if (closed)
            throw new dotnet4j.io.IOException("object disposed");
        if (!canWrite())
            throw new dotnet4j.io.IOException("not writable");

        int i = position + count;
        // Check for overflow
        if (i < 0)
            throw new dotnet4j.io.IOException("overflow");

        if (i > length) {
            boolean mustZero = position > length;
            if (i > capacity) {
                boolean allocatedNewArray = ensureCapacity(i);
                if (allocatedNewArray)
                    mustZero = false;
            }
            if (mustZero)
                Arrays.fill(this.buffer, length, i, (byte) 0);
            length = i;
        }
        System.arraycopy(buffer, offset, this.buffer, position, count);
        position = i;
    }

    @Override
    public void close() throws IOException {
        buffer = null;
        writable = false;
        expandable = false;
        closed = true;
    }

    public int readByte() {
        if (closed)
            throw new dotnet4j.io.IOException("object disposed");

        if (position >= length) {
            return -1;
        }

        return this.buffer[position++] & 0xff;
    }

    public void writeByte(byte value) {
        if (closed)
            throw new dotnet4j.io.IOException("object disposed");
        if (!canWrite())
            throw new dotnet4j.io.IOException("not writable");

        if (position >= length) {
            int newLength = position + 1;
            boolean mustZero = position > length;
            if (newLength >= capacity) {
                boolean allocatedNewArray = ensureCapacity(newLength);
                if (allocatedNewArray)
                    mustZero = false;
            }
            if (mustZero)
                Arrays.fill(this.buffer, length, position, (byte) 0);
            length = newLength;
        }
        buffer[position++] = value;
    }

    // Writes this MemoryStream to another stream.
    public void writeTo(Stream stream) {
        if (stream == null)
            throw new NullPointerException("stream");
        if (closed)
            throw new dotnet4j.io.IOException("object disposed");

        stream.write(buffer, origin, length - origin);
    }

    /** */
    public byte[] toArray() {
        if (buffer != null) {
            byte[] copy = new byte[length - origin];
            System.arraycopy(buffer, origin, copy, 0, length - origin);
            return copy;
        } else {
            return new byte[0];
        }
    }

    /**
     * @return
     */
    public byte[] getBuffer() {
        if (!publiclyVisible) {
            throw new dotnet4j.io.IOException("not publiclyVisible");
        }
        return buffer;
    }
}
