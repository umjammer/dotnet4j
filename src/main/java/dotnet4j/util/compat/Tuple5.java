package dotnet4j.util.compat;

public class Tuple5<A, B, C, D, E> extends Tuple4<A, B, C, D> {

    protected E Item5;

    public Tuple5(A a, B b, C c, D d, E e) {
        super(a, b, c, d);
        Item5 = e;
    }

    public E getItem5() {
        return Item5;
    }

    public void setItem5(E e) {
        Item5 = e;
    }
}

