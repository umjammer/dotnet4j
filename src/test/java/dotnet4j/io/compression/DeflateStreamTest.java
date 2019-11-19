//
// (C) 2004 Novell, Inc. <http://www.novell.com>
//

package dotnet4j.io.compression;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dotnet4j.io.MemoryStream;
import dotnet4j.io.SeekOrigin;
import dotnet4j.io.Stream;
import dotnet4j.io.StreamReader;


/**
 * Test Cases for the System.IO.Compression.DeflateStream class
 *
 * *** WARNING ***
 * this library's DeflateStream decompression needs zip header (0x78, 0x9c)
 * so spec. is different from original C# DeflateStream
 *
 * @author Christopher James Lahey <clahey@ximian.com>
 */
public class DeflateStreamTest {
    private static void copyStream(Stream src, Stream dest) {
        byte[] array = new byte[1024];
        int bytes_read;
        bytes_read = src.read(array, 0, 1024);
        while (bytes_read != 0) {
            dest.write(array, 0, bytes_read);
            bytes_read = src.read(array, 0, 1024);
        }
    }

    private static boolean compare_buffers(byte[] first, byte[] second, int length) {
        if (first.length < length || second.length < length) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (first[i] != second[i]) {
                return false;
            }
        }
        return true;
    }

    @Test
    public void constructor_Null() {
        assertThrows(NullPointerException.class, () -> {
            DeflateStream ds = new DeflateStream(null, CompressionMode.Compress);
        });
    }

    @Test
    public void checkCompressDecompress() throws Exception {
        byte[] data = new byte[100000];
        for (int i = 0; i < 100000; i++) {
            data[i] = (byte) i;
        }
        MemoryStream dataStream = new MemoryStream(data);
        MemoryStream backing = new MemoryStream();
        DeflateStream compressing = new DeflateStream(backing, CompressionMode.Compress, true);
        copyStream(dataStream, compressing);
        dataStream.close();
        compressing.close();
        backing.seek(0, SeekOrigin.Begin);
        DeflateStream decompressing = new DeflateStream(backing, CompressionMode.Decompress);
        MemoryStream output = new MemoryStream();
        copyStream(decompressing, output);
        assertTrue(compare_buffers(data, output.getBuffer(), (int) output.getLength()));
        decompressing.close();
        output.close();
    }

    @Test
    public void checkDecompress() throws Exception {
        MemoryStream backing = new MemoryStream(compressed_data);
        DeflateStream decompressing = new DeflateStream(backing, CompressionMode.Decompress);
        StreamReader reader = new StreamReader(decompressing);
        assertEquals("Hello", reader.readLine());
        decompressing.close();
    }

    @Test
    public void checkNullRead() {
        assertThrows(NullPointerException.class, () -> {
            MemoryStream backing = new MemoryStream(compressed_data);
            DeflateStream decompressing = new DeflateStream(backing, CompressionMode.Decompress);
            decompressing.read(null, 0, 20);
        });
    }

    @Test
    public void checkCompressingRead() {
        assertThrows(dotnet4j.io.IOException.class, () -> {
            byte[] dummy = new byte[20];
            MemoryStream backing = new MemoryStream();
            DeflateStream compressing = new DeflateStream(backing, CompressionMode.Compress);
            compressing.read(dummy, 0, 20);
        });
    }

    @Test
    public void checkRangeRead() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            byte[] dummy = new byte[20];
            MemoryStream backing = new MemoryStream(compressed_data);
            DeflateStream decompressing = new DeflateStream(backing, CompressionMode.Decompress);
            decompressing.read(dummy, 10, 20);
        });
    }

    @Test
    @Tag("NotWorking")
    public void checkInvalidDataRead() {
        assertThrows(dotnet4j.io.IOException.class, () -> {
            byte[] data = {
                0x11, 0x78, (byte) 0x89, (byte) 0x91, (byte) 0xbe, (byte) 0xf3, 0x48, (byte) 0xcd, (byte) 0xc9, (byte) 0xc9,
                (byte) 0xe7, 0x02, 0x00
            };
            byte[] dummy = new byte[20];
            MemoryStream backing = new MemoryStream(data);
            DeflateStream decompressing = new DeflateStream(backing, CompressionMode.Decompress);
            decompressing.read(dummy, 0, 20);
        });
    }

    @Test
    public void checkClosedRead() {
        assertThrows(dotnet4j.io.IOException.class, () -> {
            byte[] dummy = new byte[20];
            MemoryStream backing = new MemoryStream(compressed_data);
            DeflateStream decompressing = new DeflateStream(backing, CompressionMode.Decompress);
            decompressing.close();
            decompressing.read(dummy, 0, 20);
        });
    }

    @Test
    public void checkClosedFlush() {
        assertThrows(dotnet4j.io.IOException.class, () -> {
            MemoryStream backing = new MemoryStream();
            DeflateStream compressing = new DeflateStream(backing, CompressionMode.Compress);
            compressing.close();
            compressing.flush();
        });
    }

    @Test
    public void checkSeek() {
        assertThrows(UnsupportedOperationException.class, () -> {
            MemoryStream backing = new MemoryStream(compressed_data);
            DeflateStream decompressing = new DeflateStream(backing, CompressionMode.Decompress);
            decompressing.seek(20, SeekOrigin.Current);
        });
    }

    @Test
    public void checkSetLength() {
        assertThrows(UnsupportedOperationException.class, () -> {
            MemoryStream backing = new MemoryStream(compressed_data);
            DeflateStream decompressing = new DeflateStream(backing, CompressionMode.Decompress);
            decompressing.setLength(20);
        });
    }

    @Test
    public void checkGetCanSeekProp() throws Exception {
        MemoryStream backing = new MemoryStream(compressed_data);
        DeflateStream decompress = new DeflateStream(backing, CompressionMode.Decompress);
        assertFalse(decompress.canSeek(), "#A1");
        assertTrue(backing.canSeek(), "#A2");
        decompress.close();
        assertFalse(decompress.canSeek(), "#A3");
        assertFalse(backing.canSeek(), "#A4");

        backing = new MemoryStream();
        DeflateStream compress = new DeflateStream(backing, CompressionMode.Compress);
        assertFalse(compress.canSeek(), "#B1");
        assertTrue(backing.canSeek(), "#B2");
        compress.close();
        assertFalse(decompress.canSeek(), "#B3");
        assertFalse(backing.canSeek(), "#B4");
    }

    @Test
    public void checkGetCanReadProp() throws Exception {
        MemoryStream backing = new MemoryStream(compressed_data);
        DeflateStream decompress = new DeflateStream(backing, CompressionMode.Decompress);
        assertTrue(decompress.canRead(), "#A1");
        assertTrue(backing.canRead(), "#A2");
        decompress.close();
        assertFalse(decompress.canRead(), "#A3");
        assertFalse(backing.canRead(), "#A4");

        backing = new MemoryStream();
        DeflateStream compress = new DeflateStream(backing, CompressionMode.Compress);
        assertFalse(compress.canRead(), "#B1");
        assertTrue(backing.canRead(), "#B2");
        compress.close();
        assertFalse(decompress.canRead(), "#B3");
        assertFalse(backing.canRead(), "#B4");
    }

    @Test
    public void checkGetCanWriteProp() throws Exception {
        MemoryStream backing = new MemoryStream();
        DeflateStream decompress = new DeflateStream(backing, CompressionMode.Decompress);
        assertFalse(decompress.canWrite(), "#A1");
        assertTrue(backing.canWrite(), "#A2");
        decompress.close();
        assertFalse(decompress.canWrite(), "#A3");
        assertFalse(backing.canWrite(), "#A4");

        backing = new MemoryStream();
        DeflateStream compress = new DeflateStream(backing, CompressionMode.Compress);
        assertTrue(compress.canWrite(), "#B1");
        assertTrue(backing.canWrite(), "#B2");
        compress.close();
        assertFalse(decompress.canWrite(), "#B3");
        assertFalse(backing.canWrite(), "#B4");
    }

    @Test
    public void checkSetLengthProp() {
        assertThrows(UnsupportedOperationException.class, () -> {
            MemoryStream backing = new MemoryStream(compressed_data);
            DeflateStream decompressing = new DeflateStream(backing, CompressionMode.Decompress);
            decompressing.setLength(20);
        });
    }

    @Test
    public void checkGetLengthProp() {
        assertThrows(UnsupportedOperationException.class, () -> {
            MemoryStream backing = new MemoryStream(compressed_data);
            DeflateStream decompressing = new DeflateStream(backing, CompressionMode.Decompress);
            long length = decompressing.getLength();
        });
    }

    @Test
    public void checkGetPositionProp() {
        assertThrows(UnsupportedOperationException.class, () -> {
            MemoryStream backing = new MemoryStream(compressed_data);
            DeflateStream decompressing = new DeflateStream(backing, CompressionMode.Decompress);
            long position = decompressing.getPosition();
        });
    }

    @Test
    public void disposeTest() throws Exception {
        MemoryStream backing = new MemoryStream(compressed_data);
        DeflateStream decompress = new DeflateStream(backing, CompressionMode.Decompress);
        decompress.close();
        decompress.close();
    }

    // *** WARNING ***
    // this library's DeflateStream decompression needs zip header (0x78, 0x9c)
    // so spec. is different from original C# DeflateStream
    static byte[] compressed_data = {
        (byte) 0x78, (byte) 0x9C,
        (byte) 0xf3, 0x48, (byte) 0xcd, (byte) 0xc9, (byte) 0xc9, (byte) 0xe7, 0x02, 0x00
    };

    @Test
    public void junkAtTheEnd() throws Exception {
        // Write a deflated stream, then some additional data...
        try (MemoryStream ms = new MemoryStream()) {
            // The compressed stream
            try (DeflateStream stream = new DeflateStream(ms, CompressionMode.Compress, true)) {
                stream.writeByte((byte) 1);
                stream.flush();
            }
            // Junk
            ms.writeByte((byte) 2);

            ms.setPosition(0);
            // Reading: this should not hang
            try (DeflateStream stream = new DeflateStream(ms, CompressionMode.Decompress)) {
                byte[] buffer = new byte[512];
                int len = stream.read(buffer, 0, buffer.length);
                System.err.println(len == 1);
            }
        }
    }

    class Bug19313Stream extends MemoryStream {
        public Bug19313Stream(byte[] buffer) {
            super(buffer);
        }

        public int read(byte[] buffer, int offset, int count) {
            // Thread was blocking when DeflateStream uses a NetworkStream.
            // Because the NetworkStream.read calls Socket.Receive that
            // blocks the thread waiting for at least a byte to return.
            // This assert guarantees that Read is called only when there
            // is something to be read.
            assertTrue(getPosition() < getLength(), "Trying to read empty stream.");

            return super.read(buffer, offset, count);
        }
    }

    @Test
    public void bug19313() throws Exception {
        byte[] buffer = new byte[512];
        try (Stream backing = new Bug19313Stream(compressed_data);
             Stream decompressing = new DeflateStream(backing, CompressionMode.Decompress)) {
            decompressing.read(buffer, 0, buffer.length);
        }
    }
}
