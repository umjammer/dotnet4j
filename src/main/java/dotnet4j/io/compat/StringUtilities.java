/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package dotnet4j.io.compat;

import java.nio.CharBuffer;
import java.util.Comparator;
import java.util.Optional;


/**
 * StringUtilities.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/10/28 umjammer initial version <br>
 */
public class StringUtilities {

    private StringUtilities() {
    }

    /** currently only '\' is replaced */
    public static String escapeForRegex(String str) {
        return str.replace("\\", "\\\\");
    }

    /** */
    public static boolean isNullOrWhiteSpace(String str) {
        if (str == null)
            return true;
        return str.matches("^\\\\s*$");
    }

    public static int compare(String strA, String strB) {
        return compare(strA, strB, false);
    }

    /**
     * @param strA nullable
     * @param strB nullable
     */
    public static int compare(String strA, String strB, boolean ignoreCase) {
        if (strA == null) {
            if (strB == null) {
                return 0;
            } else {
                return -1;
            }
        }
        if (strB == null) {
            return 1;
        }
        if (ignoreCase) {
            return strA.compareToIgnoreCase(strB);
        } else {
            return strA.compareTo(strB);
        }
    }

    /** */
    public static int compare(String strA, int indexA, String strB, int indexB, int length) {
        return compare(strA, indexA, strB, indexB, length, true);
    }

    /** */
    public static int compare(String strA, int indexA, String strB, int indexB, int length, boolean ignoreCase) {
        return compare(strA.substring(indexA, Math.min(indexA + length, strA.length())),
                       strB.substring(indexB, Math.min(indexB + length, strB.length())),
                       ignoreCase);
    }

    /** */
    public static int indexOfAny(String str, char[] anyOf) {
        return indexOfAny(str, anyOf, 0, str.length());
    }

    /** */
    public static int indexOfAny(String str, char[] anyOf, int startIndex) {
        return indexOfAny(str, anyOf, startIndex, str.length() - startIndex);
    }

    /** */
    public static int indexOfAny(String str, char[] anyOf, int startIndex, int count) {
        if (anyOf == null) {
            throw new NullPointerException("anyOf");
        }
        if (startIndex > str.length()) {
            throw new IndexOutOfBoundsException("startIndex");
        }
        if (count > str.length() - startIndex) {
            throw new IndexOutOfBoundsException("count");
        }

        String substr = str.substring(startIndex, startIndex + count);
        Optional<Integer> r = CharBuffer.wrap(anyOf)
                .chars()
                .mapToObj(c -> substr.indexOf(c))
                .filter(i -> i >= 0)
                .min(Comparator.naturalOrder());
        return r.isPresent() ? r.get() + startIndex : -1;
    }

    /** */
    public static int lastIndexOfAny(String str, char[] anyOf) {
        return lastIndexOfAny(str, anyOf, str.length() - 1, str.length());
    }

    /** */
    public static int lastIndexOfAny(String str, char[] anyOf, int startIndex) {
        return lastIndexOfAny(str, anyOf, startIndex, startIndex + 1);
    }

    /** */
    public static int lastIndexOfAny(String str, char[] anyOf, int startIndex, int count) {
        if (anyOf == null) {
            throw new NullPointerException("anyOf");
        }
        if (str.length() == 0) {
            return -1;
        }
        if ((startIndex & 0xffffffffl) >= str.length()) {
            throw new IndexOutOfBoundsException("startIndex");
        }
        if (count < 0 || (count - 1) > startIndex) {
            throw new IndexOutOfBoundsException("count");
        }

        String substr = str.substring(startIndex - count + 1, startIndex + 1);
        Optional<Integer> r = CharBuffer.wrap(anyOf)
                .chars()
                .mapToObj(c -> substr.lastIndexOf(c))
                .filter(i -> i >= 0)
                .max(Comparator.naturalOrder());
        return r.isPresent() ? r.get() + (startIndex - count + 1) : -1;
    }
}

/* */
