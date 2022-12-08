package dotnet4j.util.compat;

public class Tuple6<A, B, C, D, E, F> extends Tuple5<A, B, C, D, E> {

    protected F Item6;

    public Tuple6(A a, B b, C c, D d, E e, F f) {
        super(a, b, c, d, e);
        Item6 = f;
    }

    public F getItem6() {
        return Item6;
    }

    public void setItem6(F f) {
        Item6 = f;
    }
}

