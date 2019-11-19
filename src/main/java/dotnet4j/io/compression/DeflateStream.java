/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package dotnet4j.io.compression;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import dotnet4j.io.Stream;
import dotnet4j.io.compat.JavaIOStream;
import dotnet4j.io.compat.StreamInputStream;
import dotnet4j.io.compat.StreamOutputStream;


/**
 * DeflateStream.
 *
 * *** WARNING ***
 * this class decompression needs zip header (0x78, 0x9c)
 * so spec. is different from original C# DeflateStream
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/30 umjammer initial version <br>
 */
public class DeflateStream extends JavaIOStream {

    /** */
    private Stream stream;

    /** */
    private static InputStream toInputStream(Stream stream, CompressionMode compressionMode) {
        if (stream == null) {
            throw new NullPointerException("stream");
        }

        InputStream is = new StreamInputStream(stream);
        return compressionMode == CompressionMode.Decompress ? new InflaterInputStream(is) : null;
    }

    /** */
    private static OutputStream toOutputStream(Stream stream, CompressionMode compressionMode) {
        if (stream == null) {
            throw new NullPointerException("stream");
        }

        OutputStream os = new StreamOutputStream(stream);
        return compressionMode == CompressionMode.Compress ? new DeflaterOutputStream(os) : null;
    }

    /** */
    public DeflateStream(Stream stream, CompressionMode compressionMode) {
        this(stream, compressionMode, false);
    }

    /** */
    public DeflateStream(Stream stream, CompressionMode compressionMode, boolean leaveOpen) {
        super(toInputStream(stream, compressionMode), toOutputStream(stream, compressionMode), leaveOpen);
        this.stream = stream;
    }

    public long getLength() {
        throw new UnsupportedOperationException();
    }

    public long getPosition() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
        if (os != null) {
            // DeflaterOutputStream#flush doesn't flush
            DeflaterOutputStream.class.cast(os).finish();
        }
        super.close();
        if (!leaveOpen) {
            stream.close();
        }
    }
}

/* */
