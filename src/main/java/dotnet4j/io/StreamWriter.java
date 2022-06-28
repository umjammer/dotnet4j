/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package dotnet4j.io;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


/**
 * StreamOutputStream.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/30 umjammer initial version <br>
 */
public class StreamWriter extends Writer {

    private Stream stream;

    // TODO
    private Charset encoding = StandardCharsets.UTF_8;

    public StreamWriter(Stream stream) {
        this.stream = stream;
    }

    /** */
    public StreamWriter(Stream stream, Charset encoding) {
        this.stream = stream;
        this.encoding = encoding;
    }

    @Override
    public void write(char[] cbuf, int off, int len) {
        for (char c : cbuf) {
            stream.writeByte((byte) c);
            stream.writeByte((byte) c); // TODO
        }
    }

    @Override
    public void flush() {
        stream.flush();
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

    /** TODO */
    public void writeLine(Object obj) {
        byte[] bytes = obj.toString().getBytes(encoding);
        stream.write(bytes, 0, bytes.length);
        
    }

    /**
     * TODO
     */
    public void println(String s) {
        writeLine(s);
    }
}

/* */
