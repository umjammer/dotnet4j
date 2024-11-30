/*
 * https://claude.ai/chat/5cb0054b-dc67-4dbd-91d7-960966aec653
 */

package dotnet4j.io.compression;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.GZIPOutputStream;

import dotnet4j.io.MemoryStream;
import dotnet4j.io.SeekOrigin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import vavi.util.Debug;
import vavi.util.StringUtil;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * GZipStreamTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2024-11-25 nsano initial version <br>
 */
class GZipStreamTest {

    private MemoryStream memoryStream;
    private final String testString = """
            This test suite covers:

            Compression Tests


            Basic compression functionality
            Compression followed by decompression
            Handling empty data
            Handling large data sets


            Decompression Tests


            Error handling for invalid data
            Multiple read operations
            Partial reads


            Stream Capability Tests


            Verification of supported operations
            Testing unsupported operations throw correct exceptions


            Resource Management Tests


            Proper stream closing behavior
            Flush operations

            Key features of the test suite:

            Organized using nested test classes for better readability and organization
            Comprehensive test coverage for both compression and decompression
            Testing of edge cases and error conditions
            Resource cleanup using try-with-resources and @AfterEach
            Use of assertAll() for multiple related assertions
            Clear test names using @DisplayName

            The tests use a MemoryStream as the underlying stream for predictable behavior and easy verification. Error conditions and edge cases are also tested to ensure robust behavior.
            Would you like me to add any additional test cases or modify the existing ones?
            """;
    private final byte[] testData = testString.getBytes();

    @BeforeEach
    void setUp() {
        memoryStream = new MemoryStream();
    }

    @AfterEach
    void tearDown() throws IOException {
        memoryStream.close();
    }

    @Nested
    @DisplayName("Compression Tests")
    class CompressionTests {

        @Test
        @Disabled("just confirmation")
        void testX() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzos = new GZIPOutputStream(baos);
            gzos.write(testData, 0, testData.length);
            gzos.flush();
            gzos.close();
Debug.print("compressed size: " + baos.size() + "\n" + StringUtil.getDump(baos.toByteArray()));
        }

        @Test
        @DisplayName("Should compress data correctly")
        void testCompression() throws IOException {
            // Write test data to memory stream using GZipStream
            try (GZipStream compressionStream = new GZipStream(memoryStream, CompressionMode.Compress)) {
                compressionStream.write(testData, 0, testData.length);
                compressionStream.flush();
            }

            byte[] compressedData = memoryStream.toArray();
//Debug.print("compressed size: " + compressedData.length + "\n" + StringUtil.getDump(compressedData));

            // Verify compression actually occurred
            assertAll(
                    () -> assertTrue(compressedData.length > 0),
                    () -> assertTrue(compressedData.length < testData.length, "expect " + compressedData.length + " < " + testData.length),
                    () -> assertThrows(AssertionFailedError.class, () -> assertArrayEquals(testData, compressedData)) // TODO there isn't assertArrayNotEquals?
            );
        }

        @Test
        @DisplayName("Should compress and decompress data correctly")
        void testCompressionDecompression() throws IOException {
            // First compress the data
            // TODO leave open doesn't work, GZIPOutputStream outputs something when close?
            try (GZipStream compressionStream = new GZipStream(memoryStream, CompressionMode.Compress)) {
                compressionStream.write(testData, 0, testData.length);
                compressionStream.flush();
            }

//            memoryStream.position(0); // TODO ditto
            MemoryStream memoryStream2 = new MemoryStream(memoryStream.toArray());

            // Now decompress the data
            byte[] decompressedData = new byte[testData.length];
            try (GZipStream decompressionStream = new GZipStream(memoryStream2, CompressionMode.Decompress)) {
                int bytesRead = 0;
                while (true) {
                    int r = decompressionStream.read(decompressedData, bytesRead, decompressedData.length - bytesRead);
Debug.println("decompressionStream: " + r);
                    if (r <= 0) break; // TODO GZipStream doesn't return -1 (not java spec), EOF is 0 (c# spec)
                    bytesRead += r;
                }
                assertEquals(testData.length, bytesRead);
            }

            assertArrayEquals(testData, decompressedData);
        }

        @Test
        @DisplayName("Should compress empty data correctly")
        void testCompressEmptyData() throws IOException {
            byte[] emptyData = new byte[0];

            try (GZipStream compressionStream = new GZipStream(memoryStream, CompressionMode.Compress)) {
                compressionStream.write(emptyData, 0, 0);
            }

            byte[] compressedData = memoryStream.toArray();
            assertTrue(compressedData.length > 0); // GZip header should still be present
        }

        @Test
        @DisplayName("Should compress large data correctly")
        void testCompressLargeData() throws IOException {
            // Create large data set
            byte[] largeData = new byte[1000000]; // 1MB of data
            Arrays.fill(largeData, (byte) 'A');

            try (GZipStream compressionStream = new GZipStream(memoryStream, CompressionMode.Compress)) {
                compressionStream.write(largeData, 0, largeData.length);
            }

            byte[] compressedData = memoryStream.toArray();

            // Highly compressible data should compress well
            assertTrue(compressedData.length < largeData.length / 10);
        }
    }

    @Nested
    @DisplayName("Decompression Tests")
    class DecompressionTests {

        @Test
        @DisplayName("Should throw exception for invalid compressed data")
        void testDecompressInvalidData() {
            byte[] invalidData = {1, 2, 3, 4, 5}; // Invalid GZip data
            memoryStream.write(invalidData, 0, invalidData.length);
            memoryStream.position(0);

            assertThrows(dotnet4j.io.IOException.class, () -> {
                try (GZipStream decompressionStream = new GZipStream(memoryStream, CompressionMode.Decompress)) {
                    decompressionStream.read(new byte[10], 0, 10);
                }
            });
        }

        @Test
        @DisplayName("Should handle multiple read operations correctly")
        void testMultipleReads() throws IOException {
            // First compress some data
            // TODO leave open doesn't work, GZIPOutputStream outputs something when close?
            try (GZipStream compressionStream = new GZipStream(memoryStream, CompressionMode.Compress)) {
                compressionStream.write(testData, 0, testData.length);
                compressionStream.flush();
            }

//            memoryStream.position(0); // TODO ditto
            MemoryStream memoryStream2 = new MemoryStream(memoryStream.toArray());

            // Read in small chunks
            try (GZipStream decompressionStream = new GZipStream(memoryStream2, CompressionMode.Decompress)) {
                byte[] decompressedData = new byte[testData.length];
                int totalBytesRead = 0;
                int bytesRead;
                int chunkSize = 4;

                while ((bytesRead = decompressionStream.read(decompressedData, totalBytesRead,
                        Math.min(chunkSize, testData.length - totalBytesRead))) > 0) {
                    totalBytesRead += bytesRead;
                }

                assertEquals(testData.length, totalBytesRead);
                assertArrayEquals(testData, decompressedData);
            }
        }
    }

    @Nested
    @DisplayName("Stream Capability Tests")
    class StreamCapabilityTests {

        @Test
        @DisplayName("Should report correct stream capabilities")
        void testStreamCapabilities() throws IOException {
            try (GZipStream gzipStream = new GZipStream(memoryStream, CompressionMode.Compress)) {
                assertAll(
                        () -> assertTrue(gzipStream.canWrite()),
                        () -> assertFalse(gzipStream.canSeek())
                );
            }
        }

        @Test
        @DisplayName("Should throw exception for unsupported operations")
        void testUnsupportedOperations() {
            GZipStream gzipStream = new GZipStream(memoryStream, CompressionMode.Compress);

            assertAll(
                    () -> assertThrows(UnsupportedOperationException.class,
                            () -> gzipStream.seek(0, SeekOrigin.Begin)),
                    () -> assertThrows(UnsupportedOperationException.class,
                            () -> gzipStream.setLength(100)),
                    () -> assertThrows(UnsupportedOperationException.class,
                            () -> gzipStream.position(50))
            );
        }
    }

    @Nested
    @DisplayName("Resource Management Tests")
    class ResourceManagementTests {

        @Test
        @DisplayName("Should close underlying streams correctly")
        void testStreamClosing() throws IOException {
            GZipStream gzipStream = new GZipStream(memoryStream, CompressionMode.Compress);
            gzipStream.write(testData, 0, testData.length);
            gzipStream.close();

            // Verify that operations on closed stream throw exceptions
            assertThrows(dotnet4j.io.IOException.class,
                    () -> gzipStream.write(testData, 0, testData.length));
        }

        @Test
        @DisplayName("Should flush compressed data correctly")
        void testFlush() throws IOException {
            try (GZipStream gzipStream = new GZipStream(memoryStream, CompressionMode.Compress)) {
                gzipStream.write(testData, 0, testData.length);
                gzipStream.flush();

                // Verify that data was written to underlying stream
                assertTrue(memoryStream.getLength() > 0);
            }
        }
    }
}
