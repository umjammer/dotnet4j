package dotnet4j.io;

import java.io.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.*;
import java.nio.channels.FileChannel;

/**
 * Created by FT on 27.11.14.
 */
public class FileStream extends Stream
{
    private RandomAccessFile raf;
    private FileChannel channel;
    private FileMode myMode;
    private FileAccess myAccess;
    private FileShare myShare;
    private String myPath;

    public FileStream(String path, FileMode mode)
    {
        this(path,mode,(mode == FileMode.Append ? FileAccess.Write : FileAccess.ReadWrite));
    }

    public FileStream(String path, FileMode mode, FileAccess access)
    {
        this(path,mode,access,access == FileAccess.Write ? FileShare.None : FileShare.Read);
    }

    public FileStream(String path, FileMode mode, FileAccess access, FileShare share)
    {
        myPath = path;
        myMode = mode;
        myAccess = access;
        myShare = share;

        String rafMode = "";
        switch (access)
        {

            case Read:
                rafMode = "r";
                break;
            case Write:
                rafMode = "rw";
                break;
            case ReadWrite:
                rafMode = "rw";
                break;
        }
        try
        {
            java.io.File f = new java.io.File(path);
            if (mode == FileMode.Create || mode == FileMode.CreateNew || mode == FileMode.OpenOrCreate)
            {
                if (!f.exists())
                {
                    f.createNewFile();
                }
            }
            raf = new RandomAccessFile(path,rafMode);
            channel = raf.getChannel();

        } catch (FileNotFoundException e) {
            throw new dotnet4j.io.FileNotFoundException(e);
        } catch (IOException e) {
            throw new dotnet4j.io.IOException(e);
        }
    }

    public FileStream(String path,
            FileMode mode,
            FileAccess access,
            FileShare share,
            int bufferSize,
            FileOptions options) {
        this(path, mode, access, share);
        if (options == FileOptions.DeleteOnClose) {
            java.io.File f = new java.io.File(path);
            f.deleteOnExit();
        }
        // TODO bufferSize
    }

    public String getName() {
        return myPath;
    }

    @Override
    public boolean canRead()
    {
        if (channel == null) return false;
        if (myAccess == FileAccess.Read || myAccess == FileAccess.ReadWrite)
        {
            return true;
        }
        return false;
    }

    @Override
    public boolean canSeek()
    {
        if (channel == null) return false;
        return true;
    }

    @Override
    public boolean canWrite()
    {
        if (channel == null) return false;
        if (myAccess == FileAccess.Write || myAccess == FileAccess.ReadWrite)
        {
            return true;
        }
        return false;
    }

    @Override
    public long getLength() {
        try {
            return channel.size();
        } catch (IOException e) {
            throw new dotnet4j.io.IOException(e);
        }
    }

    @Override
    public long getPosition() {
        try {
            return channel.position();
        } catch (IOException e) {
            throw new dotnet4j.io.IOException(e);
        }
    }

    @Override
    public void setPosition(long value)
    {
        try {
            channel.position(value);
        } catch (IOException e) {
            throw new dotnet4j.io.IOException(e);
        }
    }

    @Override
    public void close() {
        try {
            channel.close();
            channel = null;
        } catch (IOException e) {
            throw new dotnet4j.io.IOException(e);
        }
    }

    @Override
    public void flush() {
        try {
            channel.force(false);
        } catch (IOException e) {
            throw new dotnet4j.io.IOException(e);
        }
    }

    @Override
    public int read(byte[] buffer, int offset, int length) {
        ByteBuffer tmp = ByteBuffer.wrap(buffer,offset,length);
        int m = 0;
        try {
            m = channel.read(tmp);
        } catch (IOException e) {
            e.printStackTrace();
            throw new dotnet4j.io.IOException(e);
        }
        return m;
    }

    @Override
    public long seek(long offset, SeekOrigin origin) {
        try {
            switch (origin) {
                case Begin:
                    channel.position(offset);
                case Current:
                    channel.position(offset + channel.position());
                    break;
                case End:
                    channel.position(channel.size() + offset);
                    break;
            }
        }
        catch (IOException e)
        {
            throw new dotnet4j.io.IOException(e);
        }
        return getPosition();
    }

    @Override
    public void setLength(long value)
    {
        throw new RuntimeException("This method is not implemented (yet).");
    }

    @Override
    public void write(byte[] buffer, int offset, int count)
    {
        ByteBuffer temp = ByteBuffer.wrap(buffer,offset,count);
        try {
            channel.write(temp);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
