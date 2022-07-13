package dotnet4j.util.compat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * TupleTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-02 nsano initial version <br>
 */
class TupleTest {

    @Test
    void test1() {
        Tuple<Integer, Integer> t = new Tuple<>(1, 2);
        assertEquals(1, (int) t.getItem1());
        assertEquals(2, (int) t.getItem2());
    }

    @Test
    void test2() {
        Tuple3<Integer, Integer, Integer> t = new Tuple3<>(1, 2, 3);
        assertEquals(1, (int) t.getItem1());
        assertEquals(2, (int) t.getItem2());
        assertEquals(3, (int) t.getItem3());
    }

    @Test
    void test3() {
        Tuple4<Integer, Integer, Integer, Integer> t = new Tuple4<>(1, 2, 3, 4);
        assertEquals(1, (int) t.getItem1());
        assertEquals(2, (int) t.getItem2());
        assertEquals(3, (int) t.getItem3());
        assertEquals(4, (int) t.getItem4());
    }

    @Test
    void test4() {
        Tuple5<Integer, Integer, Integer, Integer, Integer> t = new Tuple5<>(1, 2, 3, 4, 5);
        assertEquals(1, (int) t.getItem1());
        assertEquals(2, (int) t.getItem2());
        assertEquals(3, (int) t.getItem3());
        assertEquals(4, (int) t.getItem4());
        assertEquals(5, (int) t.getItem5());
    }

    @Test
    void test5() {
        Tuple6<Integer, Integer, Integer, Integer, Integer, Integer> t = new Tuple6<>(1, 2, 3, 4, 5, 6);
        assertEquals(1, (int) t.getItem1());
        assertEquals(2, (int) t.getItem2());
        assertEquals(3, (int) t.getItem3());
        assertEquals(4, (int) t.getItem4());
        assertEquals(5, (int) t.getItem5());
        assertEquals(6, (int) t.getItem6());
    }
}
