package dotnet4j.io;

import org.jetbrains.annotations.Contract;

import java.io.*;
import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Created by schiemas on 14.07.16.
 */
public abstract class Stream implements Closeable, AutoCloseable
{
    public static final Stream Null = new NullStream();

    private static ResourceBundle resourceBundle;
    public abstract boolean canRead();
    public abstract boolean canSeek();
    public boolean canTimeout()
    {
        return false;
    }
    public abstract boolean canWrite();
    public abstract long getLength();
    public abstract long getPosition();
    public abstract void setPosition(long value);
    public int getReadTimeout()
    {
        throw new RuntimeException("timeout not supported");
    }
    public void setReadTimeout(int value)
    {
        throw new RuntimeException("timeout not supported");
    }
    public int getWriteTimeout()
    {
        throw new RuntimeException("timeout not supported");
    }
    public void setWriteTimeout(int value)
    {
        throw new RuntimeException("timeout not supported");
    }
    public void copyTo(Stream destination)
    {
        copyTo(destination,2048);
    }
    public void copyTo(Stream destination, int bufferSize)
    {
        if (destination == null)
            throw new RuntimeException("destination is null");
        if (!this.canRead())
            throw new RuntimeException("source is not readable");
        if (!destination.canWrite())
            throw new RuntimeException("destination is not writeable");

        byte[] buffer = new byte[bufferSize];
        int lastBlockSize = 0;
        do {
            lastBlockSize = read(buffer,0,bufferSize);
            destination.write(buffer,0,lastBlockSize);
        } while(lastBlockSize != 0);
    }
    @Override
    public abstract void close() throws IOException;
    public abstract void flush();
    public abstract long seek(long offset, SeekOrigin origin);
    public abstract void setLength(long value);

    /**
     * @return The total number of bytes read into the buffer. This can be less
     *         than the number of bytes requested if that many bytes are not
     *         currently available, or zero (0) if the end of the stream has
     *         been reached.
     */
    public abstract int read(byte[] buffer, int offset, int length);
    /**
     * The byte cast to a int, or -1 if the end of the stream has been reached.
     */
    public int readByte()
    {
        byte[] one = new byte[1];
        int result = read(one,0,1);
        if (result == 0)
            return -1;
        return one[0];
    }
    public abstract void write(byte[] buffer, int offset, int count);
    public void writeByte(byte value)
    {
        write(new byte[] {value},0,1);
    }
    @Contract("null -> fail")
    public static Stream synchronize(Stream stream)
    {
        if (stream == null)
            throw new RuntimeException("stream is null");
        if (stream instanceof SyncStream)
            return stream;

        return new SyncStream(stream);
    }
}
