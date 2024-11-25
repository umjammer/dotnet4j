/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package dotnet4j.io.compat;

import java.io.IOException;
import java.io.OutputStream;

import dotnet4j.io.Stream;
import vavi.io.Seekable;


/**
 * StreamOutputStream.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/30 umjammer initial version <br>
 */
public class StreamOutputStream extends OutputStream implements Seekable {

    private final Stream stream;

    public StreamOutputStream(Stream stream) {
        this.stream = stream;
    }

    @Override
    public void write(int b) {
        stream.writeByte((byte) b);
    }

    @Override
    public void write(byte[] buffer, int offset, int count) {
//logger.log(Level.TRACE, "w: " + count + ", " + stream);
        stream.write(buffer, offset, count);
    }

    @Override
    public void flush() {
        stream.flush();
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

    @Override
    public void position(long l) throws IOException {
        stream.position(l);
    }

    @Override
    public long position() throws IOException {
        return stream.position();
    }
}
