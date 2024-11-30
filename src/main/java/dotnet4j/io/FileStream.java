package dotnet4j.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static java.lang.System.getLogger;


/**
 * Created by FT on 27.11.14.
 */
public class FileStream extends Stream {

    private static final Logger logger = getLogger(FileStream.class.getName());

    private FileChannel channel;
    private final FileMode myMode;
    private final FileAccess myAccess;
    private final FileShare myShare;
    private final String myPath;

    public FileStream(String path, FileMode mode) {
        this(path, mode, (mode == FileMode.Append ? FileAccess.Write : FileAccess.ReadWrite));
    }

    public FileStream(String path, FileMode mode, FileAccess access) {
        this(path, mode, access, access == FileAccess.Write ? FileShare.None : FileShare.Read);
    }

    public FileStream(String path, FileMode mode, FileAccess access, FileShare share) {
        myPath = path;
        myMode = mode;
        myAccess = access;
        myShare = share;

        String rafMode = switch (access) {
            case Read -> "r";
            case Write -> "rw";
            case ReadWrite -> "rw";
        };
        try {
            java.io.File f = new java.io.File(path);
            if (mode == FileMode.Create || mode == FileMode.CreateNew || mode == FileMode.OpenOrCreate) {
                if (!f.exists()) {
                    f.createNewFile();
                }
            }
            RandomAccessFile raf = new RandomAccessFile(path, rafMode);
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

    /** */
    public FileStream(String path,
                      FileMode mode,
                      FileAccess access,
                      FileShare share,
                      int bufferSize,
                      boolean b,
                      FileOptions options) {
        this(path, mode, access, share);
        // TODO Auto-generated constructor stub
    }

    public String getName() {
        return myPath;
    }

    @Override
    public boolean canRead() {
        if (channel == null) return false;
        return myAccess == FileAccess.Read || myAccess == FileAccess.ReadWrite;
    }

    @Override
    public boolean canSeek() {
        return channel != null;
    }

    @Override
    public boolean canWrite() {
        if (channel == null) return false;
        return myAccess == FileAccess.Write || myAccess == FileAccess.ReadWrite;
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
    public long position() {
        try {
            return channel.position();
        } catch (IOException e) {
            throw new dotnet4j.io.IOException(e);
        }
    }

    @Override
    public void position(long value) {
        try {
            channel.position(value);
        } catch (IOException e) {
            throw new dotnet4j.io.IOException(e);
        }
    }

    @Override
    public void close() {
        try {
            if (channel != null) {
                channel.close();
                channel = null;
            }
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
        ByteBuffer tmp = ByteBuffer.wrap(buffer, offset, length);
        int m;
        try {
            m = channel.read(tmp);
//logger.log(Level.TRACE, m + ", " + offset + ", " + length + " / " + channel.size() + ", " + channel.size() + ", " + channel.position());
            if (m == -1) {
                return 0;
            }
        } catch (IOException e) {
            logger.log(Level.TRACE, e.getMessage(), e);
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
        } catch (IOException e) {
            throw new dotnet4j.io.IOException(e);
        }
        return position();
    }

    @Override
    public void setLength(long value) {
        throw new RuntimeException("This method is not implemented (yet).");
    }

    @Override
    public void write(byte[] buffer, int offset, int count) {
        ByteBuffer temp = ByteBuffer.wrap(buffer, offset, count);
        try {
            channel.write(temp);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
