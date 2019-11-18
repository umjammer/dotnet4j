/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package dotnet4j.io.compat;

import java.io.IOException;
import java.io.OutputStream;

import dotnet4j.io.Stream;


/**
 * StreamOutputStream.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/30 umjammer initial version <br>
 */
public class StreamOutputStream extends OutputStream {

    private Stream stream;

    public StreamOutputStream(Stream stream) {
        this.stream = stream;
    }

    @Override
    public void write(int b) throws IOException {
        stream.writeByte((byte) b);
    }

    @Override
    public void write(byte[] buffer, int offset, int count) {
        stream.write(buffer, offset, count);
    }
}

/* */
