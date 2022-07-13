package dotnet4j.util.compat;

@FunctionalInterface
public interface HexaFunction<T, U, V, W, X, Y, R> {
    R apply(T t, U u, V v, W w, X x, Y y);
}
