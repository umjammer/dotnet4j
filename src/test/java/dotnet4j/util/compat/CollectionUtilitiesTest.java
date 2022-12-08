package dotnet4j.util.compat;

import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;


/**
 * CollectionUtilitiesTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-09 nsano initial version <br>
 */
public class CollectionUtilitiesTest {

    @Test
    void test1() throws Exception {
        byte[] r = CollectionUtilities.toByteArray(Arrays.asList((byte) 0x30, (byte) 0x31, (byte) 0x32));
        assertArrayEquals(new byte[] {'0', '1', '2'}, r);
    }

    @Test
    void test2() throws Exception {
        SortedSet<Byte> ss = new TreeSet<>();
        ss.add((byte) 0x30);
        ss.add((byte) 0x31);
        ss.add((byte) 0x32);
        byte[] r = CollectionUtilities.toByteArray(ss);
        assertArrayEquals(new byte[] {'0', '1', '2'}, r);
    }

    @Test
    void test3() throws Exception {
        List<Byte> r = CollectionUtilities.toList(new byte[] {'0', '1', '2'});
        assertIterableEquals(Arrays.asList((byte) '0', (byte) '1', (byte) '2'), r);
    }

    @Test
    void test4() throws Exception {
        int[] r = CollectionUtilities.toIntArray(Arrays.asList(0x30, 0x31, 0x32));
        assertArrayEquals(new int[] {'0', '1', '2'}, r);
    }

    @Test
    void test5() throws Exception {
        float[] r = CollectionUtilities.toFloatArray(Arrays.asList(1.1f, 1.2f, 1.3f));
        assertArrayEquals(new float[] {1.1f, 1.2f, 1.3f}, r);
    }
}
