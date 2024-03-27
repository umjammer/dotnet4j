/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package dotnet4j.util.compat;


/**
 * Tuple.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/07/25 umjammer initial version <br>
 */
public class Tuple<A, B> {

    protected A Item1;

    protected B Item2;

    public Tuple(A a, B b) {
        Item1 = a;
        Item2 = b;
    }

    public A getKey() {
        return Item1;
    }

    public B getValue() {
        return Item2;
    }

    public A getItem1() {
        return Item1;
    }

    public B getItem2() {
        return Item2;
    }

    public void setItem1(A a) {
        Item1 = a;
    }

    public void setItem2(B b) {
        Item2 = b;
    }
}
