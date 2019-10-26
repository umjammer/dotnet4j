/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package dotnet4j.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * StreamTest.
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2019/10/24 nsano initial version <br>
 */
class StreamTest {

    @Test
    void test() {
        byte [] bytes = new byte[256];
        for (int i = 0; i < 256; i++) {
            bytes[i] = (byte) i;
            int x = bytes[i];
            assertEquals((byte) i, (byte) x);
        }

        MemoryStream ms = new MemoryStream();
        for (int i = 0; i < 256; i++) {
            ms.writeByte((byte) i);
        }
        ms.setPosition(0);
        for (int i = 0; i < 256; i++) {
            int x = ms.readByte();
            assertEquals(i, x);
        }
    }

}

/* */
