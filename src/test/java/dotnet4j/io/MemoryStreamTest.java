//
// (c) 2003 Ximian, Inc. (http://www.ximian.com)
// Copyright (C) 2004 Novell (http://www.novell.com)
// Copyright 2011 Xamarin, Inc (http://www.xamarin.com)
//

package dotnet4j.io;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import vavi.io.LittleEndianDataOutputStream;
import vavi.util.StringUtil;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import dotnet4j.io.compat.StreamInputStream;
import dotnet4j.io.compat.StreamOutputStream;


/**
 * System.IO.MemoryStreamTest
 *
 * @author Marcin Szczepanski (marcins@zipworld.com.au)
 * @author Gonzalo Paniagua Javier (gonzalo@ximian.com)
 * @author Sebastien Pouliot <sebastien@ximian.com>
 * @author Marek Safar (marek.safar@gmail.com)
 */
public class MemoryStreamTest {
//    class SignaledMemoryStream extends MemoryStream {
//        WaitHandle w;
//
//        public SignaledMemoryStream(byte[] buffer, WaitHandle w) {
//            super(buffer);
//
//            this.w = w;
//        }
//
//        public int Read(byte[] buffer, int offset, int count) {
//            if (!w.WaitOne(2000))
//                return -1;
//
//            assertTrue(Thread.currentThread().IsThreadPoolThread, "IsThreadPoolThread");
//            return super.read(buffer, offset, count);
//        }
//    }

    class ExceptionalStream extends MemoryStream {
        public static final String Message = "ExceptionalMessage";

        public boolean _throw = false;

        public ExceptionalStream() {
            allowRead = true;
            allowWrite = true;
        }

        public ExceptionalStream(byte[] buffer, boolean writable) {
            super(buffer, writable);

            allowRead = true;
            allowWrite = true; // we are testing the inherited write property
        }

        public int read(byte[] buffer, int offset, int count) {
            if (_throw)
                throw new IOException(Message);

            return super.read(buffer, offset, count);
        }

        public void write(byte[] buffer, int offset, int count) {
            if (_throw)
                throw new IOException(Message);

            super.write(buffer, offset, count);
        }

        private boolean allowRead;

        public boolean getAllowRead() {
            return allowRead;
        }

        public void setAllowRead(boolean value) {
            allowRead = value;
        }

        public boolean canRead() {
            return allowRead;
        }

        private boolean allowWrite;

        public boolean getAllowWrite() {
            return allowWrite;
        }

        public void setAllowWrite(boolean value) {
            allowWrite = value;
        }

        public boolean canWrite() {
            return allowWrite;
        }

        public void flush() {
            if (_throw)
                throw new IOException(Message);

            super.flush();
        }
    }

    MemoryStream testStream;

    byte[] testStreamData;

    @BeforeEach
    public void setUp() {
        testStreamData = new byte[100];

        for (int i = 0; i < 100; i++)
            testStreamData[i] = (byte) (100 - i);

        testStream = new MemoryStream(testStreamData);
    }

    //
    // Verify that the first count bytes in testBytes are the same as
    // the count bytes from index start in testStreamData
    //
    void verifyTestData(String id, byte[] testBytes, int start, int count) {
        if (testBytes == null)
            fail(id + "+1 testBytes is null");

        if (start < 0 || count < 0 || start + count > testStreamData.length || start > testStreamData.length)
            throw new IndexOutOfBoundsException(id + "+2");

        for (int test = 0; test < count; test++) {
            if (testBytes[test] == testStreamData[start + test])
                continue;

            String failStr = "testByte {0} (testStream {1}) was <{2}>, expecting <{3}>";
            failStr = String.format(failStr, test, start + test, testBytes[test], testStreamData[start + test]);
            fail(id + "-3" + failStr);
        }
    }

    @Test
    public void constructorsOne() {
        MemoryStream ms = new MemoryStream();

        assertEquals(0L, ms.getLength(), "#01");
        assertEquals(0, ms.getCapacity(), "#02");
        assertEquals(true, ms.canWrite(), "#03");
    }

    @Test
    public void constructorsTwo() {
        MemoryStream ms = new MemoryStream(10);

        assertEquals(0L, ms.getLength(), "#01");
        assertEquals(10, ms.getCapacity(), "#02");
        ms.setCapacity(0);
        byte[] buffer = ms.getBuffer();
        // Begin: wow!!!
        assertEquals(-1, ms.readByte(), "#03");
        assertNull(buffer, "#04"); // <--
        ms.read(new byte[5], 0, 5);
        assertEquals(0, ms.getPosition(), "#05");
        assertEquals(0, ms.getLength(), "#06");
        // End
    }

    @Test
    public void constructorsThree() {
        MemoryStream ms = new MemoryStream(testStreamData);
        assertEquals(100, ms.getLength(), "#01");
        assertEquals(0, ms.getPosition(), "#02");
    }

    @Test
    public void constructorsFour() {
        MemoryStream ms = new MemoryStream(testStreamData, true);
        assertEquals(100, ms.getLength(), "#01");
        assertEquals(0, ms.getPosition(), "#02");
        ms.setPosition(50);
        byte saved = testStreamData[50];
        try {
            ms.writeByte((byte) 23);
            assertEquals(testStreamData[50], 23, "#03");
        } finally {
            testStreamData[50] = saved;
        }
        ms.setPosition(100);
        try {
            ms.writeByte((byte) 23);
        } catch (Exception e) {
            return;
        }
        fail("#04");
    }

    @Test
    public void constructorsFive() {
        MemoryStream ms = new MemoryStream(testStreamData, 50, 50);
        assertEquals(50, ms.getLength(), "#01");
        assertEquals(0, ms.getPosition(), "#02");
        assertEquals(50, ms.getCapacity(), "#03");
        ms.setPosition(1);
        byte saved = testStreamData[51];
        try {
            ms.writeByte((byte) 23);
            assertEquals(testStreamData[51], 23, "#04");
        } finally {
            testStreamData[51] = saved;
        }
        ms.setPosition(100);

        try {
            ms.writeByte((byte) 23);
            fail("#05");
        } catch (IOException e) {}

        try {
            ms.setCapacity(100);
            fail("#06");
        } catch (IOException e) {}

        try {
            ms.setCapacity(51);
            fail("#07");
        } catch (IOException e) {}

        assertEquals(50, ms.toArray().length, "#08");
    }

    @Test
    public void constructorsSix() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            MemoryStream ms = new MemoryStream(-2);
        });
    }

    @Test
    public void read() {
        byte[] readBytes = new byte[20];

        /* Test simple read */
        testStream.read(readBytes, 0, 10);
        verifyTestData("R1", readBytes, 0, 10);

        /* Seek back to beginning */

        testStream.seek(0, SeekOrigin.Begin);

        /* Read again, bit more this time */
        testStream.read(readBytes, 0, 20);
        verifyTestData("R2", readBytes, 0, 20);

        /* Seek to 20 bytes from End */
        testStream.seek(-20, SeekOrigin.End);
        testStream.read(readBytes, 0, 20);
        verifyTestData("R3", readBytes, 80, 20);

        int readByte = testStream.readByte();
        assertEquals(-1, readByte, "R4");
    }

//    @Test
//    @Tag("MultiThreaded")
//    public void beginRead() {
//        byte[] readBytes = new byte[5];
//
//        IAsyncResult res = testStream.beginRead(readBytes, 0, 5, null, null);
//        assertTrue(res.AsyncWaitHandle.WaitOne(1000), "#1");
//        assertArrayEquals(5, testStream.EndRead(res), "#2");
//    }

//    @Test
//    @Tag("MultiThreaded")
//    public void beginRead_WithState() {
//        byte[] readBytes = new byte[5];
//        String async_state = null;
//        ManualResetEvent wh = new ManualResetEvent(false);
//
//        IAsyncResult res = testStream.beginRead(readBytes, 0, 5, l -> {
//            async_state = (String) l.asyncState;
//            wh.Set();
//        }, "state");
//
//        assertTrue(res.AsyncWaitHandle.WaitOne(1000), "#1");
//        assertArrayEquals("state", res.AsyncState, "#2");
//        assertTrue(res.IsCompleted, "#3");
//        assertArrayEquals(5, testStream.EndRead(res), "#4");
//
//        wh.WaitOne(1000);
//        assertEquals("state", async_state, "#5");
//        wh.close();
//    }

//    @Test
//    @Tag("MultiThreaded")
//    public void beginReadAsync() {
//        byte[] readBytes = new byte[5];
//        ManualResetEvent wh = new ManualResetEvent(false);
//        try (SignaledMemoryStream testStream = new SignaledMemoryStream(testStreamData, wh)) {
//            IAsyncResult res = testStream.beginRead(readBytes, 0, 5, null, null);
//            assertFalse(res.IsCompleted, "#1");
//            assertFalse(res.CompletedSynchronously, "#2");
//            wh.Set();
//            assertTrue(res.AsyncWaitHandle.WaitOne(2000), "#3");
//            assertTrue(res.IsCompleted, "#4");
//            assertArrayEquals(5, testStream.EndRead(res), "#5");
//        }
//
//        wh.close();
//    }

//    @Test
//    @Tag("MultiThreaded")
//    public void beginReadIsBlockingNextRead() {
//        byte[] readBytes = new byte[5];
//        byte[] readBytes2 = new byte[3];
//        ManualResetEvent begin_read_unblock = new ManualResetEvent(false);
//        ManualResetEvent begin_read_blocking = new ManualResetEvent(false);
//        Task begin_read_task = null;
//
//        try {
//            try (SignaledMemoryStream testStream = new SignaledMemoryStream(testStreamData, begin_read_unblock)) {
//                IAsyncResult begin_read_1_ares = testStream.beginRead(readBytes, 0, 5, null, null);
//
//                begin_read_task = Task.Factory.StartNew(() -> {
//                    IAsyncResult begin_read_2_ares = testStream.BeginRead(readBytes2, 0, 3, null, null);
//                    begin_read_blocking.Set();
//
//                    assertTrue(begin_read_2_ares.AsyncWaitHandle.WaitOne(2000), "#10");
//                    assertTrue(begin_read_2_ares.IsCompleted, "#11");
//                    assertArrayEquals(3, testStream.EndRead(begin_read_2_ares), "#12");
//                    assertArrayEquals(95, readBytes2[0], "#13");
//                });
//
//                assertFalse(begin_read_1_ares.IsCompleted, "#1");
//                assertFalse(begin_read_blocking.WaitOne(500), "#2");
//
//                begin_read_unblock.Set();
//
//                assertTrue(begin_read_1_ares.AsyncWaitHandle.WaitOne(2000), "#3");
//                assertTrue(begin_read_1_ares.IsCompleted, "#4");
//                assertArrayEquals(5, testStream.EndRead(begin_read_1_ares), "#5");
//                assertTrue(begin_read_task.Wait(2000), "#6");
//                assertEquals(100, readBytes[0], "#7");
//            }
//        } finally {
//            if (begin_read_task != null)
//                begin_read_task.Wait();
//        }
//    }

//    @Test
//    @Tag("MultiThreaded")
//    public void beginRead_Read() {
//        byte[] readBytes = new byte[5];
//        ManualResetEvent wh = new ManualResetEvent(false);
//        try (SignaledMemoryStream testStream = new SignaledMemoryStream(testStreamData, wh)) {
//            IAsyncResult res = testStream.beginRead(readBytes, 0, 5, null, null);
//            assertEquals(100, testStream.readByte(), "#0");
//            assertFalse(res.IsCompleted, "#1");
//            assertFalse(res.CompletedSynchronously, "#2");
//            wh.Set();
//            assertTrue(res.AsyncWaitHandle.WaitOne(2000), "#3");
//            assertTrue(res.IsCompleted, "#4");
//            assertArrayEquals(5, testStream.EndRead(res), "#5");
//            assertEquals(99, readBytes[0], "#6");
//        }
//
//        wh.close();
//    }

//    @Test
//    @Tag("MultiThreaded")
//    public void beginRead_BeginWrite() {
//        byte[] readBytes = new byte[5];
//        byte[] readBytes2 = new byte[] {
//            1, 2, 3
//        };
//        ManualResetEvent begin_read_unblock = new ManualResetEvent(false);
//        ManualResetEvent begin_write_blocking = new ManualResetEvent(false);
//        Task begin_write_task = null;
//
//        try {
//            try (MemoryStream stream = new SignaledMemoryStream(testStreamData, begin_read_unblock)) {
//                IAsyncResult begin_read_ares = stream.BeginRead(readBytes, 0, 5, null, null);
//
//                begin_write_task = Task.Factory.StartNew(() -> {
//                    var begin_write_ares = stream.BeginWrite(readBytes2, 0, 3, null, null);
//                    begin_write_blocking.Set();
//                    assertTrue(begin_write_ares.AsyncWaitHandle.WaitOne(2000), "#10");
//                    assertTrue(begin_write_ares.IsCompleted, "#11");
//                    stream.EndWrite(begin_write_ares);
//                });
//
//                assertFalse(begin_read_ares.IsCompleted, "#1");
//                assertFalse(begin_write_blocking.WaitOne(500), "#2");
//
//                begin_read_unblock.Set();
//
//                assertTrue(begin_read_ares.AsyncWaitHandle.WaitOne(2000), "#3");
//                assertTrue(begin_read_ares.IsCompleted, "#4");
//                assertArrayEquals(5, stream.EndRead(begin_read_ares), "#5");
//                assertTrue(begin_write_task.Wait(2000), "#6");
//            }
//        } finally {
//            if (begin_write_task != null)
//                begin_write_task.Wait();
//        }
//    }

//    @Test
//    @Tag("MultiThreaded")
//    public void beginWrite() {
//        byte[] writeBytes = new byte[] {
//            2, 3, 4, 10, 12
//        };
//
//        IAsyncResult res = testStream.beginWrite(writeBytes, 0, 5, null, null);
//        assertTrue(res.AsyncWaitHandle.WaitOne(1000), "#1");
//        testStream.EndWrite(res);
//    }

//    @Test
//    @Tag("MultiThreaded")
//    public void beginWrite_WithState() {
//        byte[] writeBytes = new byte[] {
//            2, 3, 4, 10, 12
//        };
//        String async_state = null;
//        ManualResetEvent wh = new ManualResetEvent(false);
//
//        IAsyncResult res = testStream.beginWrite(writeBytes, 0, 5, l -> {
//            async_state = (String) l.asyncState;
//            wh.Set();
//        }, "state");
//
//        assertTrue(res.AsyncWaitHandle.WaitOne(1000), "#1");
//        assertTrue(res.IsCompleted, "#2");
//        assertArrayEquals("state", res.AsyncState, "#3");
//        testStream.EndWrite(res);
//
//        wh.WaitOne(1000);
//        assertArrayEquals("state", async_state, "#4");
//        wh.close();
//    }

//    @Test
//    @Tag("MultiThreaded")
//    public void endRead_Twice() {
//        byte[] readBytes = new byte[5];
//
//        IAsyncResult res = testStream.beginRead(readBytes, 0, 5, null, null);
//        assertTrue(res.AsyncWaitHandle.WaitOne(1000), "#1");
//        assertArrayEquals(5, testStream.EndRead(res), "#2");
//
//        try {
//            testStream.EndRead(res);
//            fail("#3");
//        } catch (ArgumentException e) {
//            return;
//        }
//    }

//    @Test
//    @Tag("MultiThreaded")
//    public void endRead_Disposed() {
//        byte[] readBytes = new byte[5];
//
//        IAsyncResult res = testStream.beginRead(readBytes, 0, 5, null, null);
//        assertTrue(res.AsyncWaitHandle.WaitOne(1000), "#1");
//        testStream.close();
//        assertArrayEquals(5, testStream.EndRead(res), "#2");
//    }

//    @Test
//    @Tag("MultiThreaded")
//    public void endWrite_OnBeginRead() {
//        byte[] readBytes = new byte[5];
//
//        IAsyncResult res = testStream.beginRead(readBytes, 0, 5, null, null);
//        assertTrue(res.AsyncWaitHandle.WaitOne(1000), "#1");
//
//        try {
//            testStream.EndWrite(res);
//            fail("#2");
//        } catch (ArgumentException e) {}
//
//        testStream.EndRead(res);
//    }

//    @Test
//    @Tag("MultiThreaded")
//    public void endWrite_Twice() {
//        byte[] wBytes = new byte[5];
//
//        IAsyncResult res = testStream.beginWrite(wBytes, 0, 5, null, null);
//        assertTrue(res.AsyncWaitHandle.WaitOne(1000), "#1");
//        testStream.EndWrite(res);
//
//        try {
//            testStream.EndWrite(res);
//            fail("#2");
//        } catch (ArgumentException e) {
//            return;
//        }
//    }

    @Test
    public void writeBytes() {
        byte[] readBytes = new byte[100];

        MemoryStream ms = new MemoryStream(100);

        for (int i = 0; i < 100; i++)
            ms.writeByte(testStreamData[i]);

        ms.seek(0, SeekOrigin.Begin);
        testStream.read(readBytes, 0, 100);
        verifyTestData("W1", readBytes, 0, 100);
    }

    @Test
    public void writeBlock() {
        byte[] readBytes = new byte[100];

        MemoryStream ms = new MemoryStream(100);

        ms.write(testStreamData, 0, 100);
        ms.seek(0, SeekOrigin.Begin);
        testStream.read(readBytes, 0, 100);
        verifyTestData("WB1", readBytes, 0, 100);
        byte[] arrayBytes = testStream.toArray();
        assertEquals(100, arrayBytes.length, "#01");
        verifyTestData("WB2", arrayBytes, 0, 100);
    }

    @Test
    public void positionLength() {
        MemoryStream ms = new MemoryStream();
        ms.setPosition(4);
        ms.writeByte((byte) 'M');
        ms.writeByte((byte) 'O');
        assertEquals(6, ms.getLength(), "#01");
        assertEquals(6, ms.getPosition(), "#02");
        ms.setPosition(0);
        assertEquals(0, ms.getPosition(), "#03");
    }

    @Test
    public void morePositionLength() {
        assertThrows(IOException.class, () -> {
            MemoryStream ms = new MemoryStream(testStreamData);
            ms.setPosition(101);
            assertEquals(101, ms.getPosition(), "#01");
            assertEquals(100, ms.getLength(), "#02");
            ms.writeByte((byte) 1); // This should throw the exception
        });
    }

    @Test
    public void getBufferOne() {
        MemoryStream ms = new MemoryStream();
        byte[] buffer = ms.getBuffer();
        assertEquals(0, buffer.length, "#01");
    }

    @Test
    public void getBufferTwo() {
        MemoryStream ms = new MemoryStream(100);
        byte[] buffer = ms.getBuffer();
        assertEquals(100, buffer.length, "#01");

        ms.write(testStreamData, 0, 100);
        ms.write(testStreamData, 0, 100);
        assertEquals(200, ms.getLength(), "#02");
        buffer = ms.getBuffer();
        assertEquals(256, buffer.length, "#03"); // Minimun size after writing
    }

    @Test
    public void closed() throws Exception {
        MemoryStream ms = new MemoryStream(100);
        ms.close();
        boolean thrown = false;
        try {
            int x = ms.getCapacity();
        } catch (IOException e) {
            thrown = true;
        }

        if (!thrown)
            fail("#01");

        thrown = false;
        try {
            ms.setCapacity(1);
        } catch (IOException e) {
            thrown = true;
        }

        if (!thrown)
            fail("#02");

        try {
            ms.read(null, 0, 1);
            fail("#03");
        } catch (NullPointerException e) {}

        try {
            ms.write(null, 0, 1);
            fail("#04");
        } catch (NullPointerException e) {
            thrown = true;
        }
    }

    @Test
    public void close_get_Length() {
        assertThrows(IOException.class, () -> {
            MemoryStream ms = new MemoryStream(100);
            ms.close();
            long x = ms.getLength();
        });
    }

    @Test
    public void close_get_Position() {
        assertThrows(IOException.class, () -> {
            MemoryStream ms = new MemoryStream(100);
            ms.close();
            long x = ms.getPosition();
            ;
        });
    }

    @Test
    public void close_set_Position() {
        assertThrows(IOException.class, () -> {
            MemoryStream ms = new MemoryStream(100);
            ms.close();
            ms.setPosition(0);
        });
    }

    @Test
    public void seek() {
        MemoryStream ms = new MemoryStream(100);
        ms.write(testStreamData, 0, 100);
        ms.seek(0, SeekOrigin.Begin);
        ms.setPosition(50);
        ms.seek(-50, SeekOrigin.Current);
        ms.seek(-50, SeekOrigin.End);

        boolean thrown = false;
        ms.setPosition(49);
        try {
            ms.seek(-50, SeekOrigin.Current);
        } catch (IOException e) {
            thrown = true;
        }
        if (!thrown)
            fail("#01");

        thrown = false;
        try {
            ms.seek(Long.MAX_VALUE, SeekOrigin.Begin);
        } catch (IndexOutOfBoundsException e) {
            thrown = true;
        }

        if (!thrown)
            fail("#02");

        thrown = false;
        try {
            // Oh, yes. They throw IOException for this one, but ArgumentOutOfRange for the
            // previous one
            ms.seek(Long.MIN_VALUE, SeekOrigin.Begin);
        } catch (IOException e) {
            thrown = true;
        }

        if (!thrown)
            fail("#03");

        ms = new MemoryStream(256);

        ms.write(testStreamData, 0, 100);
        ms.setPosition(0);
        assertEquals(100, ms.getLength(), "#01");
        assertEquals(0, ms.getPosition(), "#02");

        ms.setPosition(128);
        assertEquals(100, ms.getLength(), "#03");
        assertEquals(128, ms.getPosition(), "#04");

        ms.setPosition(768);
        assertEquals(100, ms.getLength(), "#05");
        assertEquals(768, ms.getPosition(), "#06");

        ms.writeByte((byte) 0);
        assertEquals(769, ms.getLength(), "#07");
        assertEquals(769, ms.getPosition(), "#08");
    }

    @Test
    public void seek_Disposed() throws Exception {
        MemoryStream ms = new MemoryStream();
        ms.close();
        try {
            ms.seek(0, SeekOrigin.Begin);
            fail();
        } catch (IOException e) {}
    }

    @Test
    public void setLength() {
        MemoryStream ms = new MemoryStream();
        ms.write(testStreamData, 0, 100);
        ms.setPosition(100);
        ms.setLength(150);
        assertEquals(150, ms.getLength(), "#01");
        assertEquals(100, ms.getPosition(), "#02");
        ms.setLength(80);
        assertEquals(80, ms.getLength(), "#03");
        assertEquals(80, ms.getPosition(), "#04");
    }

    @Test
    public void setLength_ReadOnly() {
        assertThrows(IOException.class, () -> {
            MemoryStream ms = new MemoryStream(testStreamData, false);
            ms.setLength(10);
        });
    }

    @Test
    public void capacity() {
        MemoryStream ms = new MemoryStream();

        assertEquals(0, ms.getCapacity(), "#A1");
        assertEquals(0, ms.getBuffer().length, "#A2");

        ms.writeByte((byte) '6');
        assertEquals(256, ms.getCapacity(), "#B1");
        assertEquals(256, ms.getBuffer().length, "#B2");

        // Shrink
        ms.setCapacity(100);
        assertEquals(100, ms.getCapacity(), "#C1");
        assertEquals(100, ms.getBuffer().length, "#C2");

        // Grow
        ms.setCapacity(120);
        assertEquals(120, ms.getCapacity(), "#D1");
        assertEquals(120, ms.getBuffer().length, "#D2");

        // Grow the buffer, reduce length -so we have a dirty area-
        // and then we assign capacity to the same. The idea is that we should
        // avoid creating a new internal buffer it's not needed.

        ms = new MemoryStream();
        ms.setCapacity(8);
        byte[] buff = new byte[] {
            0x01, 0x02, 0x03, 0x04, 0x05
        };
        ms.write(buff, 0, buff.length);
        assertEquals(8, ms.getCapacity(), "#E1");
        assertEquals(8, ms.getBuffer().length, "#E2");

        // Reduce *length*, not capacity
        byte[] buff_copy = ms.getBuffer();
        ms.setLength(3);
        assertEquals(3, ms.getLength(), "#F1");
        assertArrayEquals(buff_copy, ms.getBuffer(), "#F2");

        // Set Capacity to the very same value it has now
        ms.setCapacity(ms.getCapacity());
        assertArrayEquals(buff_copy, ms.getBuffer(), "#G1"); // keep the same buffer

        // Finally, growing it discards the prev buff
        ms.setCapacity(ms.getCapacity() + 1);
        assertFalse(Arrays.equals(buff_copy, ms.getBuffer()), "#H1");
    }

    boolean areBuffersEqual(byte[] buff1, byte[] buff2) {
        if ((buff1 == null) != (buff2 == null))
            return false;

        if (buff1.length != buff2.length)
            return false;

        for (int i = 0; i < buff1.length; i++)
            if (buff1[i] != buff2[i])
                return false;

        return true;
    }

    @Test // bug #327053
    public void zeroingOnExpand() {
        byte[] values = {
            3, 2, 1
        };
        byte[] reference = {
            3, 2, 1
        };
        byte[] cropped = {
            3, 0, 0
        };
        MemoryStream ms = new MemoryStream(values);
        assertArrayEquals(values, reference, "#A1");
        ms.seek(3, SeekOrigin.Begin);
        assertArrayEquals(reference, values, "#A2");
        ms.setLength(1);
        assertArrayEquals(reference, values, "#B1");
        byte[] read = new byte[5];
        ms.read(read, 0, 5);
        assertArrayEquals(new byte[] {
            0, 0, 0, 0, 0
        }, read, "#B2");
        assertArrayEquals(reference, values, "#B3");
        ms.setLength(3);
        assertArrayEquals(cropped, values, "#C1");
        ms.seek(0, SeekOrigin.Begin);
        read = new byte[3];
        ms.read(read, 0, 3);
        assertArrayEquals(cropped, read, "#C2");
        assertArrayEquals(cropped, values, "#C3");
    }

    @Test
    public void writeNonWritable() {
        assertThrows(IOException.class, () -> {
            MemoryStream ms = new MemoryStream(testStreamData, false);
            ms.write(testStreamData, 0, 100);
        });
    }

    @Test
    public void writeExpand() {
        assertThrows(IOException.class, () -> {
            MemoryStream ms = new MemoryStream(testStreamData);
            ms.write(testStreamData, 0, 100);
            ms.write(testStreamData, 0, 100); // This one throws the exception
        });
    }

    @Test
    public void writeByte() {
        MemoryStream ms = new MemoryStream(100);
        ms.write(testStreamData, 0, 100);
        ms.setPosition(100);
        ms.writeByte((byte) 101);
        assertEquals(101, ms.getPosition(), "#01");
        assertEquals(101, ms.getLength(), "#02");
        assertEquals(256, ms.getCapacity(), "#03");
        ms.write(testStreamData, 0, 100);
        ms.write(testStreamData, 0, 100);
        // 301
        assertEquals(301, ms.getPosition(), "#04");
        assertEquals(301, ms.getLength(), "#05");
        assertEquals(512, ms.getCapacity(), "#06");
    }

    @Test
    public void writeLengths() throws Exception {
        MemoryStream ms = new MemoryStream(256);
        LittleEndianDataOutputStream writer = new LittleEndianDataOutputStream(new StreamOutputStream(ms));

        writer.writeByte('1');
        assertEquals(1, ms.getLength(), "#01");
        assertEquals(256, ms.getCapacity(), "#02");

        writer.writeShort(0);
        assertEquals(3, ms.getLength(), "#03");
        assertEquals(256, ms.getCapacity(), "#04");

        writer.write(testStreamData, 0, 23);
        assertEquals(26, ms.getLength(), "#05");
        assertEquals(256, ms.getCapacity(), "#06");

        writer.write(testStreamData);
        writer.write(testStreamData);
        writer.write(testStreamData);
        assertEquals(326, ms.getLength(), "#07");
    }

    @Test
    public void moreWriteByte() throws Exception {
        byte[] buffer = new byte[44];

        MemoryStream ms = new MemoryStream(buffer);
        LittleEndianDataOutputStream bw = new LittleEndianDataOutputStream(new StreamOutputStream(ms));
        for (int i = 0; i < 44; i++)
            bw.writeByte(1);
    }

    @Test
    public void moreWriteByte2() {
        assertThrows(IOException.class, () -> {
            byte[] buffer = new byte[43]; // Note the 43 here

            MemoryStream ms = new MemoryStream(buffer);
            LittleEndianDataOutputStream bw = new LittleEndianDataOutputStream(new StreamOutputStream(ms));
            for (int i = 0; i < 44; i++)
                bw.writeByte(1);
        });
    }

    @Test
    public void expand() {
        byte[] array = new byte[] {
            0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01
        };
        MemoryStream ms = new MemoryStream();
        ms.write(array, 0, array.length);
        ms.setLength(4);
        ms.seek(4, SeekOrigin.End);
        ms.writeByte((byte) 0xFF);
        assertEquals("[1, 1, 1, 1, 0, 0, 0, 0, -1]", Arrays.toString(ms.toArray()), "Result");
    }

    @Test
    public void publiclyVisible() {
        MemoryStream ms = new MemoryStream();
        assertNotNull(ms.getBuffer(), "ctor()");

        ms = new MemoryStream(1);
        assertNotNull(ms.getBuffer(), "ctor(1)");

        ms = new MemoryStream(new byte[1], 0, 1, true, true);
        assertNotNull(ms.getBuffer(), "ctor(byte[],int,int,bool,bool");
    }

    @Test
    public void publiclyVisible_Ctor_ByteArray() {
        assertThrows(IOException.class, () -> {
            MemoryStream ms = new MemoryStream(new byte[0]);
            assertNotNull(ms.getBuffer());
        });
    }

    @Test
    public void publiclyVisible_Ctor_ByteArray_Boolean() {
        assertThrows(IOException.class, () -> {
            MemoryStream ms = new MemoryStream(new byte[0], true);
            assertNotNull(ms.getBuffer());
        });
    }

    @Test
    public void publiclyVisible_Ctor_ByteArray_Int_Int() {
        assertThrows(IOException.class, () -> {
            MemoryStream ms = new MemoryStream(new byte[1], 0, 1);
            assertNotNull(ms.getBuffer());
        });
    }

    @Test
    public void publiclyVisible_Ctor_ByteArray_Int_Int_Boolean() {
        assertThrows(IOException.class, () -> {
            MemoryStream ms = new MemoryStream(new byte[1], 0, 1, true);
            assertNotNull(ms.getBuffer());
        });
    }

    @Test
    public void publiclyVisible_Ctor_ByteArray_Int_Int_Boolean_Boolean() {
        assertThrows(IOException.class, () -> {
            MemoryStream ms = new MemoryStream(new byte[1], 0, 1, true, false);
            assertNotNull(ms.getBuffer());
        });
    }

    @Test // bug #350860
    public void toArray_Empty() {
        MemoryStream ms = new MemoryStream(1);
        ms.setCapacity(0);
        ms.toArray();
    }

    @Test // bug #80205
    @Tag("NotWorking")
    public void serializeTest() throws Exception {
        MemoryStream input = new MemoryStream();
        byte[] bufferIn = "some test".getBytes(Charset.forName("UTF8"));
        input.write(bufferIn, 0, bufferIn.length);
        input.setPosition(0);

        MemoryStream ms = new MemoryStream();
        ObjectOutputStream bf = new ObjectOutputStream(new StreamOutputStream(ms));
        bf.writeObject(input);

        byte[] bufferOut = new byte[(int) ms.getLength()];
        ms.setPosition(0);
        ms.read(bufferOut, 0, bufferOut.length);
        assertArrayEquals(_serialized, bufferOut);
    }

    @Test // bug #676060
    public void zeroCapacity() {
        MemoryStream ms = new MemoryStream();
        ms.writeByte((byte) 1);
        ms.setPosition(0);
        ms.setLength(0);
        ms.setCapacity(0);
        ms.writeByte((byte) 1);
        byte[] bytes = ms.toArray();
    }

    @Test // bug #80205
    @Tag("NotWorking")
    public void deserializeTest() throws Exception {
        MemoryStream ms = new MemoryStream();
        ms.write(_serialized, 0, _serialized.length);
        ms.setPosition(0);

        ObjectInputStream bf = new ObjectInputStream(new StreamInputStream(ms));
        MemoryStream output = (MemoryStream) bf.readObject();
        try (StreamReader sr = new StreamReader(output)) {
            assertEquals("some test", sr.readToEnd());
        }
    }

    private static byte[] _serialized = new byte[] {
        (byte) 0xAC, (byte) 0xED, 0x00, 0x05, 0x73, 0x72, 0x00, 0x18, 0x64, 0x6F, 0x74, 0x6E, 0x65, 0x74, 0x34, 0x6A, 0x2E,
        0x69, 0x6F, 0x2E, 0x4D, 0x65, 0x6D, 0x6F, 0x72, 0x79, 0x53, 0x74, 0x72, 0x65, 0x61, 0x6D, (byte) 0x9A, (byte) 0xAC,
        (byte) 0x8E, (byte) 0xBC, (byte) 0xD6, (byte) 0x9B, 0x11, 0x43, 0x02, 0x00, 0x0A, 0x49, 0x00, 0x08, 0x63, 0x61, 0x70,
        0x61, 0x63, 0x69, 0x74, 0x79, 0x5A, 0x00, 0x06, 0x63, 0x6C, 0x6F, 0x73, 0x65, 0x64, 0x5A, 0x00, 0x0A, 0x65, 0x78, 0x70,
        0x61, 0x6E, 0x64, 0x61, 0x62, 0x6C, 0x65, 0x49, 0x00, 0x06, 0x6C, 0x65, 0x6E, 0x67, 0x74, 0x68, 0x49, 0x00, 0x06, 0x6F,
        0x72, 0x69, 0x67, 0x69, 0x6E, 0x49, 0x00, 0x08, 0x70, 0x6F, 0x73, 0x69, 0x74, 0x69, 0x6F, 0x6E, 0x5A, 0x00, 0x0F, 0x70,
        0x75, 0x62, 0x6C, 0x69, 0x63, 0x6C, 0x79, 0x56, 0x69, 0x73, 0x69, 0x62, 0x6C, 0x65, 0x5A, 0x00, 0x08, 0x73, 0x65, 0x65,
        0x6B, 0x61, 0x62, 0x6C, 0x65, 0x5A, 0x00, 0x08, 0x77, 0x72, 0x69, 0x74, 0x61, 0x62, 0x6C, 0x65, 0x5B, 0x00, 0x06, 0x62,
        0x75, 0x66, 0x66, 0x65, 0x72, 0x74, 0x00, 0x02, 0x5B, 0x42, 0x78, 0x70, 0x00, 0x00, 0x01, 0x00, 0x00, 0x01, 0x00, 0x00,
        0x00, 0x09, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x01, 0x01, 0x75, 0x72, 0x00, 0x02, 0x5B, 0x42,
        (byte) 0xAC, (byte) 0xF3, 0x17, (byte) 0xF8, 0x06, 0x08, 0x54, (byte) 0xE0, 0x02, 0x00, 0x00, 0x78, 0x70, 0x00, 0x00,
        0x01, 0x00, 0x73, 0x6F, 0x6D, 0x65, 0x20, 0x74, 0x65, 0x73, 0x74, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,

        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00,
    };

    class MyMemoryStream extends MemoryStream {

        public boolean disposedCalled = false;

        public void close() {
            disposedCalled = true;
        }
    }

    @Test // https://bugzilla.novell.com/show_bug.cgi?id=322672
    public void baseDisposeCalled() throws Exception {
        MyMemoryStream ms = new MyMemoryStream();
        assertFalse(ms.disposedCalled, "Before");
        ms.close();
        assertTrue(ms.disposedCalled, "After");
    }

//    @Test
//    public void ReadAsync() {
//        byte[] buffer = new byte[3];
//        IAsyncResult t = testStream.readAsync(buffer, 0, buffer.length);
//        assertArrayEquals(t.Result, 3, "#1");
//        assertArrayEquals(99, buffer[1], "#2");
//
//        testStream.seek(99, SeekOrigin.Begin);
//        t = testStream.readAsync(buffer, 0, 1);
//        assertArrayEquals(t.Result, 1, "#3");
//        assertArrayEquals(1, buffer[0], "#4");
//    }

//    @Test
//    public void TestAsyncReadExceptions() {
//        byte[] buffer = new byte[3];
//        try (ExceptionalStream stream = new ExceptionalStream()) {
//            stream.write(buffer, 0, buffer.length);
//            stream.write(buffer, 0, buffer.length);
//            stream.setPosition(0);
//            IAsyncResult task = stream.readAsync(buffer, 0, buffer.length);
//            assertArrayEquals(TaskStatus.RanToCompletion, task.Status, "#1");
//
//            stream.Throw = true;
//            task = stream.readAsync(buffer, 0, buffer.length);
//            assertTrue(task.IsFaulted, "#2");
//            assertArrayEquals(ExceptionalStream.Message, task.Exception.InnerException.Message, "#3");
//        }
//    }

//    @Test
//    public void TestAsyncWriteExceptions() {
//        byte[] buffer = new byte[3];
//        try (ExceptionalStream stream = new ExceptionalStream()) {
//            IAsyncResult task = stream.writeAsync(buffer, 0, buffer.length);
//            assertArrayEquals(TaskStatus.RanToCompletion, task.Status, "#1");
//
//            stream.Throw = true;
//            task = stream.writeAsync(buffer, 0, buffer.length);
//            assertTrue(task.IsFaulted, "#2");
//            assertArrayEquals(ExceptionalStream.Message, task.Exception.InnerException.Message, "#3");
//        }
//    }

//    @Test
//    public void TestAsyncArgumentExceptions() {
//        byte[] buffer = new byte[3];
//        try (ExceptionalStream stream = new ExceptionalStream()) {
//            IAsyncResult task = stream.writeAsync(buffer, 0, buffer.length);
//            assertTrue(task.IsCompleted);
//
//            assertTrue(Throws < ArgumentException > (() -> { stream.writeAsync(buffer, 0, 1000); }), "#2");
//            assertTrue(Throws < ArgumentException > (() -> { stream.readAsync(buffer, 0, 1000); }), "#3");
//            assertTrue(Throws < ArgumentException >
//                (() -> { stream.writeAsync(buffer, 0, 1000, new CancellationToken(true)); }), "#4");
//            assertTrue(Throws < ArgumentException > (() -> { stream.readAsync(buffer, 0, 1000, new CancellationToken(true)); }),
//                       "#5");
//            assertTrue(Throws < ArgumentException >
//                (() -> { stream.writeAsync(null, 0, buffer.length, new CancellationToken(true)); }), "#6");
//            assertTrue(Throws < ArgumentException >
//                (() -> { stream.readAsync(null, 0, buffer.length, new CancellationToken(true)); }), "#7");
//            assertTrue(Throws < ArgumentException >
//                (() -> { stream.writeAsync(buffer, 1000, buffer.length, new CancellationToken(true)); }), "#8");
//            assertTrue(Throws < ArgumentException >
//                (() -> { stream.readAsync(buffer, 1000, buffer.length, new CancellationToken(true)); }), "#9");
//
//            stream.AllowRead = false;
//            IAsyncResult read_task = stream.readAsync(buffer, 0, buffer.length);
//            assertArrayEquals(TaskStatus.RanToCompletion, read_task.Status, "#8");
//            assertArrayEquals(0, read_task.Result, "#9");
//
//            stream.setPosition(0);
//            read_task = stream.readAsync(buffer, 0, buffer.length);
//            assertArrayEquals(TaskStatus.RanToCompletion, read_task.Status, "#9");
//            assertArrayEquals(3, read_task.Result, "#10");
//
//            IAsyncResult write_task = stream.writeAsync(buffer, 0, buffer.length);
//            assertArrayEquals(TaskStatus.RanToCompletion, write_task.Status, "#10");
//
//            // test what happens when CanRead is overridden
//            try (var norm = new ExceptionalStream(buffer, false)) {
//                write_task = norm.writeAsync(buffer, 0, buffer.length);
//                assertArrayEquals(TaskStatus.RanToCompletion, write_task.Status, "#11");
//            }
//
//            stream.AllowWrite = false;
//            assertTrue(Throws < NotSupportedException > (() -> { stream.write(buffer, 0, buffer.length); }), "#12");
//            write_task = stream.writeAsync(buffer, 0, buffer.length);
//            assertArrayEquals(TaskStatus.Faulted, write_task.Status, "#13");
//        }
//    }

//    @Test
//    public void TestAsyncFlushExceptions() {
//        try (ExceptionalStream stream = new ExceptionalStream()) {
//            IAsyncResult task = stream.flushAsync();
//            assertTrue(task.IsCompleted, "#1");
//
//            task = stream.flushAsync(new CancellationToken(true));
//            assertTrue(task.IsCanceled, "#2");
//
//            stream.Throw = true;
//            task = stream.flushAsync();
//            assertTrue(task.IsFaulted, "#3");
//            assertArrayEquals(ExceptionalStream.Message, task.Exception.InnerException.Message, "#4");
//
//            task = stream.flushAsync(new CancellationToken(true));
//            assertTrue(task.IsCanceled, "#5");
//        }
//    }

//    @Test
//    public void TestCopyAsync() {
//        try (ExceptionalStream stream = new ExceptionalStream()) {
//            try (ExceptionalStream dest = new ExceptionalStream()) {
//                byte[] buffer = new byte[] {
//                    12, 13, 8
//                };
//
//                stream.write(buffer, 0, buffer.length);
//                stream.setPosition(0);
//                IAsyncResult task = stream.copyToAsync(dest, 1);
//                assertArrayEquals(TaskStatus.RanToCompletion, task.Status);
//                assertEquals(3, stream.getLength());
//                assertEquals(3, dest.getLength());
//
//                stream.setPosition(0);
//                dest.Throw = true;
//                task = stream.copyToAsync(dest, 1);
//                assertArrayEquals(TaskStatus.Faulted, task.Status);
//                assertEquals(3, stream.getLength());
//                assertEquals(3, dest.getLength());
//            }
//        }
//    }

    @Test
    public void writableOverride() throws Exception {
        byte[] buffer = new byte[3];
        final MemoryStream stream = new MemoryStream(buffer, false);
        assertThrows(IOException.class, () -> { stream.write(buffer, 0, buffer.length); }, "#1");
        assertThrows(NullPointerException.class, () -> { stream.write(null, 0, buffer.length); }, "#1.1");
        stream.close();
        assertThrows(IOException.class, () -> { stream.write(buffer, 0, buffer.length); }, "#2");
        MemoryStream stream2 = new MemoryStream(buffer, true);
        stream2.close();
        assertFalse(stream2.canWrite(), "#3");

        ExceptionalStream estream = new ExceptionalStream(buffer, false);
        assertDoesNotThrow(() -> { estream.write(buffer, 0, buffer.length); }, "#4");
        estream.allowWrite = false;
        estream.setPosition(0);
        assertThrows(IOException.class, () -> { estream.write(buffer, 0, buffer.length); }, "#5");
        estream.allowWrite = true;
        estream.close();
        assertTrue(estream.canWrite(), "#6");
        assertThrows(IOException.class, () -> { stream.write(buffer, 0, buffer.length); }, "#7");
    }

//    @Test
//    public void readAsync_Canceled() {
//        byte[] buffer = new byte[3];
//        IAsyncResult t = testStream.readAsync(buffer, 0, buffer.length, new CancellationToken(true));
//        assertTrue(t.IsCanceled);
//
//        t = testStream.readAsync(buffer, 0, buffer.length);
//        assertArrayEquals(t.Result, 3, "#1");
//        assertEquals(99, buffer[1], "#2");
//    }

//    @Test
//    public void WriteAsync() {
//        byte[] buffer = new byte[] {
//            3, 5, 9
//        };
//
//        MemoryStream ms = new MemoryStream();
//        IAsyncResult t = ms.writeAsync(buffer, 0, buffer.length);
//        assertTrue(t.IsCompleted, "#1");
//
//        ms.seek(0, SeekOrigin.Begin);
//        assertEquals(3, ms.readByte(), "#2");
//    }

//    @Test
//    public void writeAsync_Canceled() {
//        byte[] buffer = new byte[] {
//            1, 2, 3
//        };
//        IAsyncResult t = testStream.writeAsync(buffer, 0, buffer.length, new CancellationToken(true));
//        assertTrue(t.IsCanceled);
//
//        t = testStream.writeAsync(buffer, 0, buffer.length);
//        assertTrue(t.IsCompleted, "#1");
//    }

//    <T extends Exception> boolean _throws(Class<T> clazz, Runnable a) {
//        try {
//            a.run();
//            return false;
//        } catch (clazz e) {
//            return true;
//        }
//    }
}
