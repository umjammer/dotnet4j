/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package dotnet4j.io;


/**
 * FileOptions.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/10/08 umjammer initial version <br>
 */
public enum FileOptions {
    Asynchronous(1073741824),
    DeleteOnClose(67108864),
    Encrypted(16384),
    None(0),
    RandomAccess(268435456),
    SequentialScan(134217728),
    WriteThrough(-2147483648);

    private int value;

    public int getValue() {
        return value;
    }

    private FileOptions(int value) {
        this.value = value;
    }
}

/* */
