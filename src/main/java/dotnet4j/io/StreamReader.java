/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package dotnet4j.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import dotnet4j.io.compat.StreamInputStream;


/**
 * StreamReader.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/30 umjammer initial version <br>
 */
public class StreamReader extends BufferedReader {

    public StreamReader(Stream stream) {
        this(stream, Charset.forName("utf-8"), false);
    }

    /** TODO */
    public StreamReader(Stream stream, boolean detectEncodingFromByteOrderMarks) {
        this(stream, Charset.forName("utf-8"), detectEncodingFromByteOrderMarks);
    }

    /**
     */
    public StreamReader(Stream stream, Charset encoding) {
        this(stream, encoding, false);
    }

    /**
     */
    public StreamReader(Stream stream, Charset encoding, boolean detectEncodingFromByteOrderMarks) {
        super(new InputStreamReader(new StreamInputStream(stream), encoding));
    }

    /** */
    public String readLine() {
        try {
            return super.readLine();
        } catch (IOException e) {
            throw new dotnet4j.io.IOException(e);
        }
    }

    /** */
    public String readToEnd() {
        try {
            StringBuilder sb = new StringBuilder();
            do {
                int c = read();
                sb.append((char) c);
            } while (ready());
            return sb.toString();
        } catch (IOException e) {
            throw new dotnet4j.io.IOException(e);
        }
    }

    /**
     * @return
     */
    public boolean isEndOfStream() {
        try {
            return super.ready();
        } catch (IOException e) {
            throw new dotnet4j.io.IOException(e);
        }
    }
}

/* */
