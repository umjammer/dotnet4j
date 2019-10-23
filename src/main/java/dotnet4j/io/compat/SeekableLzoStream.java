/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package dotnet4j.io.compat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.anarres.lzo.LzoCompressor1x_1;
import org.anarres.lzo.LzopInputStream;
import org.anarres.lzo.LzopOutputStream;

import dotnet4j.io.Stream;
import dotnet4j.io.compression.CompressionMode;


/**
 * SeekableLzoStream.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/10/09 umjammer initial version <br>
 */
public class SeekableLzoStream extends JavaIOStream {

    static InputStream toInputStream(Stream stream) {
        try {
            return new LzopInputStream(new StreamInputStream(stream));
        } catch (IOException e) {
            throw new dotnet4j.io.IOException(e);
        }
    }

    static OutputStream toOutputStream(Stream stream) {
        try {
            return new LzopOutputStream(new StreamOutputStream(stream), new LzoCompressor1x_1());
        } catch (IOException e) {
            throw new dotnet4j.io.IOException(e);
        }
    }

    /**
     */
    public SeekableLzoStream(Stream stream, CompressionMode decompress, boolean b) {
        super(toInputStream(stream), toOutputStream(stream));
    }

    @Override
    public boolean canSeek() {
        return true;
    }
}

/* */
