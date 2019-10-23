//
// Copyright (c) 2008-2011, Kenneth Bell
//
// Permission is hereby granted, free of charge, to any person obtaining a
// copy of this software and associated documentation files (the "Software"),
// to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense,
// and/or sell copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
// DEALINGS IN THE SOFTWARE.
//

package dotnet4j.io.compat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

public class Utilities {
    public static void writeBytesLittleEndian(short val, byte[] buffer, int offset) {
        buffer[offset] = (byte) (val & 0xFF);
        buffer[offset + 1] = (byte) ((val >>> 8) & 0xFF);
    }

    public static void writeBytesLittleEndian(int val, byte[] buffer, int offset) {
        buffer[offset] = (byte) (val & 0xFF);
        buffer[offset + 1] = (byte) ((val >>> 8) & 0xFF);
        buffer[offset + 2] = (byte) ((val >>> 16) & 0xFF);
        buffer[offset + 3] = (byte) ((val >>> 24) & 0xFF);
    }

    public static void writeBytesLittleEndian(long val, byte[] buffer, int offset) {
        buffer[offset] = (byte) (val & 0xFF);
        buffer[offset + 1] = (byte) ((val >>> 8) & 0xFF);
        buffer[offset + 2] = (byte) ((val >>> 16) & 0xFF);
        buffer[offset + 3] = (byte) ((val >>> 24) & 0xFF);
        buffer[offset + 4] = (byte) ((val >>> 32) & 0xFF);
        buffer[offset + 5] = (byte) ((val >>> 40) & 0xFF);
        buffer[offset + 6] = (byte) ((val >>> 48) & 0xFF);
        buffer[offset + 7] = (byte) ((val >>> 56) & 0xFF);
    }

    public static void writeBytesBigEndian(short val, byte[] buffer, int offset) {
        buffer[offset] = (byte) (val >>> 8);
        buffer[offset + 1] = (byte) (val & 0xFF);
    }

    public static void writeBytesBigEndian(int val, byte[] buffer, int offset) {
        buffer[offset] = (byte) ((val >>> 24) & 0xFF);
        buffer[offset + 1] = (byte) ((val >>> 16) & 0xFF);
        buffer[offset + 2] = (byte) ((val >>> 8) & 0xFF);
        buffer[offset + 3] = (byte) (val & 0xFF);
    }

    public static void writeBytesBigEndian(long val, byte[] buffer, int offset) {
        buffer[offset] = (byte) ((val >>> 56) & 0xFF);
        buffer[offset + 1] = (byte) ((val >>> 48) & 0xFF);
        buffer[offset + 2] = (byte) ((val >>> 40) & 0xFF);
        buffer[offset + 3] = (byte) ((val >>> 32) & 0xFF);
        buffer[offset + 4] = (byte) ((val >>> 24) & 0xFF);
        buffer[offset + 5] = (byte) ((val >>> 16) & 0xFF);
        buffer[offset + 6] = (byte) ((val >>> 8) & 0xFF);
        buffer[offset + 7] = (byte) (val & 0xFF);
    }

    public static int toInt16LittleEndian(byte[] buffer, int offset) {
        int val = ((buffer[offset + 1] << 8) & 0xFF00) | ((buffer[offset + 0] << 0) & 0x00FF);
        return val;
    }

    public static int toInt32LittleEndian(byte[] buffer, int offset) {
        int val = ((buffer[offset + 3] << 24) & 0xFF000000) | ((buffer[offset + 2] << 16) & 0x00FF0000) |
            ((buffer[offset + 1] << 8) & 0x0000FF00) | ((buffer[offset + 0] << 0) & 0x000000FF);
        return val;
    }

    public static long toInt64LittleEndian(byte[] buffer, int offset) {
        return ((long) toInt32LittleEndian(buffer, offset + 4) << 32) | toInt32LittleEndian(buffer, offset + 0);
    }

    public static short toInt16BigEndian(byte[] buffer, int offset) {
        short val = (short) (((buffer[offset] << 8) & 0xFF00) | ((buffer[offset + 1] << 0) & 0x00FF));
        return val;
    }

    public static int toUInt32BigEndian(byte[] buffer, int offset) {
        int val = ((buffer[offset + 0] << 24) & 0xFF000000) | ((buffer[offset + 1] << 16) & 0x00FF0000) |
            ((buffer[offset + 2] << 8) & 0x0000FF00) | ((buffer[offset + 3] << 0) & 0x000000FF);
        return val;
    }

    public static long toInt64BigEndian(byte[] buffer, int offset) {
        return ((long) toUInt32BigEndian(buffer, offset + 0) << 32) | toUInt32BigEndian(buffer, offset + 4);
    }

    public static void writeBytesLittleEndian(UUID val, byte[] buffer, int offset) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]).order(ByteOrder.LITTLE_ENDIAN);
        bb.putLong(val.getMostSignificantBits());
        bb.putLong(val.getLeastSignificantBits());
        byte[] le = bb.array();
        System.arraycopy(le, 0, buffer, offset, 16);
    }

    public static UUID toGuidLittleEndian(byte[] buffer, int offset) {
        byte[] temp = new byte[16];
        System.arraycopy(buffer, offset, temp, 0, 16);
        ByteBuffer bb = ByteBuffer.wrap(temp).order(ByteOrder.LITTLE_ENDIAN);
        long msb = bb.getLong();
        long lsb = bb.getLong();
        return new UUID(msb, lsb);
    }

    /**
     * @param obj1 nullable
     * @param obj2 nullable
     */
    public static boolean equals(Object obj1, Object obj2) {
        if (obj1 == null) {
            return obj2 == null;
        } else {
            return obj1.equals(obj2);
        }
    }
}
