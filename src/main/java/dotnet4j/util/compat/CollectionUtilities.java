package dotnet4j.util.compat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;


/**
 * CollectionUtilities.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class CollectionUtilities {

    private CollectionUtilities() {
    }

    public static <K, V> Map<K, V> toMap(String[] ss, Function<String, K> k, Function<String, V> v) {
        Map<K, V> map = new HashMap<>();
        for (String s : ss) {
            map.put(k.apply(s), v.apply(s));
        }
        return map;
    }

    public static byte[] toByteArray(Collection<Byte> o) {
        byte[] a = new byte[o.size()];
        int i = 0;
        for (byte b : o) a[i++] = b;
        return a;
    }

    public static List<Byte> toList(byte[] o) {
        List<Byte> a = new ArrayList<>(o.length);
        IntStream.range(0, o.length).forEach(i -> a.add(o[i]));
        return a;
    }

    public static byte[] toByteArray(List<Byte> o) {
        byte[] a = new byte[o.size()];
        IntStream.range(0, o.size()).forEach(i -> a[i] = o.get(i));
        return a;
    }

    public static int[] toIntArray(List<Integer> o) {
        int[] a = new int[o.size()];
        IntStream.range(0, o.size()).forEach(i -> a[i] = o.get(i));
        return a;
    }

    public static float[] toFloatArray(List<Float> o) {
        float[] a = new float[o.size()];
        IntStream.range(0, o.size()).forEach(i -> a[i] = o.get(i));
        return a;
    }
}
