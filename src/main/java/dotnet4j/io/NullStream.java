package dotnet4j.io;

import java.io.IOException;

/**
 * Created by schiemas on 14.07.16.
 */
class NullStream extends Stream
{

    public NullStream()
    {

    }

    @Override
    public boolean canRead() {
        return true;
    }

    @Override
    public boolean canSeek() {
        return true;
    }

    @Override
    public boolean canTimeout() {
        return false;
    }

    @Override
    public boolean canWrite() {
        return true;
    }

    @Override
    public long getLength() {
        return 0;
    }

    @Override
    public long getPosition() {
        return 0;
    }

    @Override
    public void setPosition(long value) {

    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public void flush() {

    }

    @Override
    public long seek(long offset, SeekOrigin origin) {
        return 0;
    }

    @Override
    public void setLength(long value) {

    }

    @Override
    public int read(byte[] buffer, int offset, int count) {
        return 0;
    }

    @Override
    public int readByte() {
        return -1;
    }

    @Override
    public void write(byte[] buffer, int offset, int count) {

    }

    @Override
    public void writeByte(byte value) {

    }


}
