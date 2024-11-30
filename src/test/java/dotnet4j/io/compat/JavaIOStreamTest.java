/*
 * https://claude.ai/chat/5cb0054b-dc67-4dbd-91d7-960966aec653
 */

package dotnet4j.io.compat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import dotnet4j.io.SeekOrigin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * JavaIOStreamTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2024-11-25 nsano initial version <br>
 */
class JavaIOStreamTest {

    private ByteArrayInputStream inputStream;
    private ByteArrayOutputStream outputStream;
    private JavaIOStream stream;
    private final byte[] testData = "Hello, World!".getBytes();

    @BeforeEach
    void setUp() {
        inputStream = new ByteArrayInputStream(testData);
        outputStream = new ByteArrayOutputStream();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (stream != null) {
            stream.close();
        }
        inputStream.close();
        outputStream.close();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        @Test
        @DisplayName("Input-only constructor should initialize correctly")
        void testInputOnlyConstructor() {
            stream = new JavaIOStream(inputStream);
            assertTrue(stream.canRead());
            assertFalse(stream.canWrite());
        }

        @Test
        @DisplayName("Input-output constructor should initialize correctly")
        void testInputOutputConstructor() {
            stream = new JavaIOStream(inputStream, outputStream);
            assertTrue(stream.canRead());
            assertTrue(stream.canWrite());
        }

        @Test
        @DisplayName("Constructor with leaveOpen should initialize correctly")
        void testLeaveOpenConstructor() {
            stream = new JavaIOStream(inputStream, outputStream, true);
            assertTrue(stream.canRead());
            assertTrue(stream.canWrite());
        }
    }

    @Nested
    @DisplayName("Stream Capability Tests")
    class StreamCapabilityTests {
        @Test
        @DisplayName("Stream capabilities should be correctly reported")
        void testStreamCapabilities() {
            stream = new JavaIOStream(inputStream, outputStream);
            assertAll(
                () -> assertTrue(stream.canRead()),
                () -> assertTrue(stream.canWrite()),
                () -> assertFalse(stream.canSeek())
            );
        }

        @Test
        @DisplayName("Stream length should match input data")
        void testGetLength() {
            stream = new JavaIOStream(inputStream);
            assertEquals(testData.length, stream.getLength());
        }

        @Test
        @DisplayName("Position should be tracked correctly")
        void testPosition() {
            stream = new JavaIOStream(inputStream);
            assertEquals(0, stream.position());
            
            byte[] buffer = new byte[5];
            stream.read(buffer, 0, 5);
            assertEquals(5, stream.position());
        }
    }

    @Nested
    @DisplayName("Unsupported Operation Tests")
    class UnsupportedOperationTests {
        @BeforeEach
        void init() {
            stream = new JavaIOStream(inputStream);
        }

        @Test
        @DisplayName("Setting position should throw UnsupportedOperationException")
        void testSetPositionThrowsException() {
            assertThrows(UnsupportedOperationException.class, 
                () -> stream.position(5));
        }

        @Test
        @DisplayName("Seek should throw UnsupportedOperationException")
        void testSeekThrowsException() {
            assertThrows(UnsupportedOperationException.class, 
                () -> stream.seek(5, SeekOrigin.Begin));
        }

        @Test
        @DisplayName("SetLength should throw UnsupportedOperationException")
        void testSetLengthThrowsException() {
            assertThrows(UnsupportedOperationException.class, 
                () -> stream.setLength(100));
        }
    }

    @Nested
    @DisplayName("Read Operation Tests")
    class ReadOperationTests {
        @Test
        @DisplayName("Reading buffer should work correctly")
        void testRead() {
            stream = new JavaIOStream(inputStream);
            byte[] buffer = new byte[testData.length];
            int bytesRead = stream.read(buffer, 0, buffer.length);
            
            assertAll(
                () -> assertEquals(testData.length, bytesRead),
                () -> assertArrayEquals(testData, buffer)
            );
        }

        @Test
        @DisplayName("Reading single byte should work correctly")
        void testReadByte() {
            stream = new JavaIOStream(inputStream);
            assertAll(
                () -> assertEquals(testData[0], stream.readByte()),
                () -> assertEquals(1, stream.position())
            );
        }

        @Test
        @DisplayName("Reading from closed stream should throw exception")
        void testReadOnClosedStream() throws IOException {
            stream = new JavaIOStream(inputStream);
            stream.close();
            
            Exception exception = assertThrows(dotnet4j.io.IOException.class, 
                () -> stream.read(new byte[1], 0, 1));
            assertEquals("closed", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Write Operation Tests")
    class WriteOperationTests {
        @Test
        @DisplayName("Writing buffer should work correctly")
        void testWrite() {
            stream = new JavaIOStream(new ByteArrayInputStream(new byte[0]), outputStream);
            stream.write(testData, 0, testData.length);
            
            assertAll(
                () -> assertArrayEquals(testData, outputStream.toByteArray()),
                () -> assertEquals(testData.length, stream.position())
            );
        }

        @Test
        @DisplayName("Writing single byte should work correctly")
        void testWriteByte() {
            stream = new JavaIOStream(new ByteArrayInputStream(new byte[0]), outputStream);
            stream.writeByte((byte) 65); // ASCII 'A'
            
            assertAll(
                () -> assertArrayEquals(new byte[] { 65 }, outputStream.toByteArray()),
                () -> assertEquals(2, stream.position(), "position must be count up 1")
            );
        }

        @Test
        @DisplayName("Writing to closed stream should throw exception")
        void testWriteOnClosedStream() throws IOException  {
            stream = new JavaIOStream(inputStream, outputStream);
            stream.close();
            
            Exception exception = assertThrows(dotnet4j.io.IOException.class, 
                () -> stream.write(new byte[1], 0, 1));
            assertEquals("closed", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Stream Management Tests")
    class StreamManagementTests {
        @Test
        @DisplayName("Flush should work correctly")
        void testFlush() {
            stream = new JavaIOStream(inputStream, outputStream);
            stream.write(testData, 0, testData.length);
            stream.flush();
            assertArrayEquals(testData, outputStream.toByteArray());
        }

        @Test
        @DisplayName("Flush on closed stream should throw exception")
        void testFlushOnClosedStream() throws IOException  {
            stream = new JavaIOStream(inputStream, outputStream);
            stream.close();
            
            Exception exception = assertThrows(dotnet4j.io.IOException.class, 
                () -> stream.flush());
            assertEquals("closed", exception.getMessage());
        }

        @Test
        @DisplayName("Close with leaveOpen=false should close streams")
        void testClose() throws IOException {
            stream = new JavaIOStream(inputStream, outputStream, false);
            stream.close();
            
            Exception exception = assertThrows(dotnet4j.io.IOException.class, 
                () -> stream.read(new byte[1], 0, 1));
            assertEquals("closed", exception.getMessage());
        }

        @Test
        @DisplayName("Close with leaveOpen=true should keep streams open")
        void testLeaveOpenTrue() throws IOException {
            stream = new JavaIOStream(inputStream, outputStream, true);
            stream.close();
            
            // Verify that the underlying streams are still usable
            assertAll(
                () -> assertDoesNotThrow(() -> inputStream.read()),
                () -> assertDoesNotThrow(() -> outputStream.write(65))
            );
        }
    }
}
