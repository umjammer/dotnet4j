/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package dotnet4j.io.compat;

import java.io.IOException;
import java.io.InputStream;

import dotnet4j.io.Stream;


/**
 * StreamInputStream.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/30 umjammer initial version <br>
 */
public class StreamInputStream extends InputStream {

    private Stream stream;

    public StreamInputStream(Stream stream) {
        this.stream = stream;
    }

    @Override
    public int read() throws IOException {
         int r = stream.readByte();
         return r;
    }

    @Override
    public int read(byte[] b, int ofs, int len) throws IOException {
         int r = stream.read(b, ofs, len);
         return r == 0 ? -1 : r;
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }
}

/* */
