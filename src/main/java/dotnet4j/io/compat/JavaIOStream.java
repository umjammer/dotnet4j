/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package dotnet4j.io.compat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import dotnet4j.io.SeekOrigin;
import dotnet4j.io.Stream;


/**
 * JavaIOStream.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/10/09 umjammer initial version <br>
 */
public class JavaIOStream extends Stream {

    protected boolean leaveOpen;

    protected InputStream is;

    protected OutputStream os;

    private int position = 0;

    /** */
    public JavaIOStream(InputStream is) {
        this(is, null, false);
    }

    /** */
    public JavaIOStream(InputStream is, OutputStream os) {
        this(is, os, false);
    }

    /** */
    public JavaIOStream(InputStream is, OutputStream os, boolean leaveOpen) {
        this.is = is;
        this.os = os;
        this.leaveOpen = leaveOpen;
    }

    @Override
    public boolean canRead() {
        return is != null && getLength() > 0;
    }

    @Override
    public boolean canSeek() {
        return false;
    }

    @Override
    public boolean canWrite() {
        return os != null;
    }

    @Override
    public long getLength() {
        try {
            return is.available();
        } catch (IOException e) {
            throw new dotnet4j.io.IOException(e);
        }
    }

    @Override
    public long getPosition() {
        return position;
    }

    @Override
    public void setPosition(long value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
        if (!leaveOpen) {
            if (is != null) {
                is.close();
                is = null;
            }
            if (is != null) {
                os.close();
                os = null;
            }
        }
    }

    @Override
    public void flush() {
        try {
            os.flush();
        } catch (IOException e) {
            throw new dotnet4j.io.IOException(e);
        }
    }

    @Override
    public long seek(long offset, SeekOrigin origin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLength(long value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int read(byte[] buffer, int offset, int length) {
        try {
            int r = is.read(buffer, offset, length);
//System.err.println(StringUtil.getDump(buffer, 16));
            if (r > 0) {
                position += r;
            }
            if (r == -1) {
//System.err.println("EOF");
                return 0; // C# Spec.
            }
//System.err.println("position: " + position);
            return r;
        } catch (IOException e) {
            throw new dotnet4j.io.IOException(e);
        }
    }

    @Override
    public int readByte() {
        try {
            int r = is.read();
            if (r > 0) {
                position += r;
            }
            return r;
        } catch (IOException e) {
            throw new dotnet4j.io.IOException(e);
        }
    }

    @Override
    public void write(byte[] buffer, int offset, int count) {
        try {
            os.write(buffer, offset, count);
            position += count;
        } catch (IOException e) {
            throw new dotnet4j.io.IOException(e);
        }
    }

    @Override
    public void writeByte(byte value) {
        super.writeByte(value);
        position++;
    }
}

/* */
