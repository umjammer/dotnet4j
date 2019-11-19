/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package dotnet4j.io;


/**
 * EOFException.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/11/19 umjammer initial version <br>
 */
public class EndOfStreamException extends IOException {
    public EndOfStreamException() {
        super("end of file.");
    }

    public EndOfStreamException(String message) {
        super(message);
    }

    public EndOfStreamException(Throwable cause) {
        super(cause);
    }
}
