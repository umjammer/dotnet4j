package dotnet4j.io;

import java.io.*;
import java.io.IOException;

/**
 * Created by schiemas on 14.07.16.
 */
class SyncStream extends Stream implements Closeable
{
    private Stream _stream;

    public SyncStream(Stream stream) {
        if (stream == null)
            throw new RuntimeException("stream is null");
    }

    @Override
    public boolean canRead() {
        return _stream.canRead();
    }

    @Override
    public boolean canSeek() {
        return _stream.canSeek();
    }

    @Override
    public boolean canWrite() {
        return _stream.canWrite();
    }

    @Override
    public boolean canTimeout() {
        return _stream.canTimeout();
    }

    @Override
    public long getLength() {
        synchronized (_stream)
        {
            return _stream.getLength();
        }
    }

    @Override
    public long getPosition() {
        synchronized (_stream)
        {
            return _stream.getPosition();
        }
    }

    @Override
    public void setPosition(long value) {
        synchronized (_stream)
        {
            _stream.setPosition(value);
        }
    }

    @Override
    public int getReadTimeout() {
        return _stream.getReadTimeout();
    }

    @Override
    public void setReadTimeout(int value) {
        _stream.setReadTimeout(value);
    }

    @Override
    public int getWriteTimeout() {
        return _stream.getWriteTimeout();
    }

    @Override
    public void setWriteTimeout(int value) {
        _stream.setWriteTimeout(value);
    }

    @Override
    public void close() throws IOException {
        synchronized (_stream)
        {
            _stream.close();
        }
    }

    @Override
    public void flush() {
        synchronized (_stream) {
            _stream.flush();
        }
    }

    @Override
    public int read(byte[] buffer, int offset, int count) {
        synchronized (_stream) {
            return _stream.read(buffer, offset, count);
        }
    }

    @Override
    public int readByte() {
        synchronized (_stream)
        {
            return _stream.readByte();
        }
    }

    @Override
    public long seek(long offset, SeekOrigin origin) {
        synchronized (_stream)
        {
            return _stream.seek(offset,origin);
        }
    }

    @Override
    public void setLength(long value) {
        synchronized (_stream)
        {
            _stream.setLength(value);
        }
    }

    @Override
    public void write(byte[] buffer, int offset, int count) {
        synchronized (_stream)
        {
            _stream.write(buffer, offset, count);
        }
    }

    @Override
    public void writeByte(byte value) {
        synchronized (_stream)
        {
            _stream.writeByte(value);
        }
    }
}
