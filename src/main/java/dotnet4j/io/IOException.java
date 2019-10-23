package dotnet4j.io;

/**
 * Created by ft on 28.03.17.
 */
public class IOException extends RuntimeException
{
    public IOException() {
        super("dotnetIo4j encountered an I/O error!");
    }

    public IOException(String message) {
        super(message);
    }

    public IOException(Throwable cause) {
        super(cause);
    }

    public IOException(String message, Throwable cause) {
        super(message, cause);
    }
}
