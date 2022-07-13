package dotnet4j.util.compat;

public class Tuple4<A, B, C, D> extends Tuple3< A, B, C> {

    protected D Item4;

    public Tuple4(A a, B b, C c, D d) {
        super(a, b, c);
        Item4 = d;
    }

    public D getItem4() { return Item4; }

    public void setItem4(D d) { Item4 = d; }
}

