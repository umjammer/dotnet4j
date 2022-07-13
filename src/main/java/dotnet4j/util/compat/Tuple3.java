package dotnet4j.util.compat;

public class Tuple3<A, B, C> extends Tuple<A, B> {

    protected C Item3;

    public Tuple3(A a, B b, C c) {
        super(a, b);
        Item2 = b;
        Item3 = c;
    }

    public C getItem3() { return Item3; }

    public void setItem3(C c) { Item3 = c; }
}
