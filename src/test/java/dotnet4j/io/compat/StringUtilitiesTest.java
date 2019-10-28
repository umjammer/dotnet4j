//
// (C) Ximian, Inc.  http://www.ximian.com
// Copyright (C) 2006 Kornel Pal
// Copyright (C) 2006 Novell (http://www.novell.com)
//

package dotnet4j.io.compat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


/**
 *StringTest.cs - NUnit Test Cases for the System.String class
 *
 * @author Jeffrey Stedfast <fejj@ximian.com>
 * @author David Brandt <bucky@keystreams.com>
 * @author Kornel Pal <http://www.kornelpal.hu/>
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 */
class StringUtilitiesTest {

    @Test // bug #316666
    @Tag("ManagedCollator")
    @Disabled
    public void compareNotWorking() {
        assertEquals(StringUtilities.compare("A", "a"), 1, "A03");
        assertEquals(StringUtilities.compare("a", "A"), -1, "A04");
    }

    @Test
    @Disabled
    public void compareNotWorking2() {
        String needle = "ab";
        String haystack = "abbcbacab";
        assertEquals(0, StringUtilities.compare(needle, 0, haystack, 0, 2, false), "basic subStringUtilities check #9");
        for (int i = 1; i <= (haystack.length() - needle.length()); i++) {
            if (i != 7) {
                assertEquals(-1,
                             StringUtilities.compare(needle, 0, haystack, i, 2, false),
                             "loop subStringUtilities check #8/" + i);
            }
        }
    }

    @Test
    public void compare() {
        String lesser = "abc";
        String medium = "abcd";
        String greater = "xyz";
        String caps = "ABC";

        assertEquals(0, StringUtilities.compare(null, null));
        assertEquals(1, StringUtilities.compare(lesser, null));

        assertTrue(StringUtilities.compare(lesser, greater) < 0);
        assertTrue(StringUtilities.compare(greater, lesser) > 0);
        assertTrue(StringUtilities.compare(lesser, lesser) == 0);
        assertTrue(StringUtilities.compare(lesser, medium) < 0);

        assertTrue(StringUtilities.compare(lesser, caps, true) == 0);
        assertTrue(StringUtilities.compare(lesser, caps, false) != 0);
        assertEquals(StringUtilities.compare("a", "b"), -1, "A01");
        assertEquals(StringUtilities.compare("b", "a"), 1, "A02");

        // TODO - test with CultureInfo

        String needle = "ab";
        String haystack = "abbcbacab";
        assertEquals(0, StringUtilities.compare(needle, 0, haystack, 0, 2), "basic subStringUtilities check #1");
        assertEquals(-1, StringUtilities.compare(needle, 0, haystack, 0, 3), "basic subStringUtilities check #2");
        assertEquals(0, StringUtilities.compare("ab", 0, "ab", 0, 2), "basic subStringUtilities check #3");
        assertEquals(0, StringUtilities.compare("ab", 0, "ab", 0, 3), "basic subStringUtilities check #4");
        assertEquals(0, StringUtilities.compare("abc", 0, "ab", 0, 2), "basic subStringUtilities check #5");
        assertEquals(1, StringUtilities.compare("abc", 0, "ab", 0, 5), "basic subStringUtilities check #6");
        assertEquals(-1, StringUtilities.compare("ab", 0, "abc", 0, 5), "basic subStringUtilities check #7");

        for (int i = 1; i <= (haystack.length() - needle.length()); i++) {
            if (i != 7) {
                assertTrue(StringUtilities.compare(needle, 0, haystack, i, 2) != 0, "loop subStringUtilities check #1/" + i);
                assertTrue(StringUtilities.compare(needle, 0, haystack, i, 3) != 0, "loop subStringUtilities check #2/" + i);
            } else {
                assertEquals(0, StringUtilities.compare(needle, 0, haystack, i, 2), "loop subStringUtilities check #3/" + i);
                assertEquals(0, StringUtilities.compare(needle, 0, haystack, i, 3), "loop subStringUtilities check #4/" + i);
            }
        }

        needle = "AB";
        assertEquals(0, StringUtilities.compare(needle, 0, haystack, 0, 2, true), "basic subStringUtilities check #8");
        for (int i = 1; i <= (haystack.length() - needle.length()); i++) {
            if (i != 7) {
                assertTrue(StringUtilities.compare(needle, 0, haystack, i, 2, true) != 0,
                           "loop subStringUtilities check #5/" + i);
                assertTrue(StringUtilities.compare(needle, 0, haystack, i, 2, false) != 0,
                           "loop subStringUtilities check #6/" + i);
            } else {
                assertEquals(0,
                             StringUtilities.compare(needle, 0, haystack, i, 2, true),
                             "loop subStringUtilities check #7/" + i);
            }
        }

        assertEquals(0, StringUtilities.compare(needle, 0, haystack, 0, 0), "Compare with 0 length");

        // TODO - extended format call with CultureInfo
    }

    @Test
    public void indexOfAny1() {
        String s = "abcdefghijklmd";
        char[] c;

        c = new char[] {
            'a', 'e', 'i', 'o', 'u'
        };
        assertEquals(0, StringUtilities.indexOfAny(s, c), "#1");
        c = new char[] {
            'd', 'z'
        };
        assertEquals(3, StringUtilities.indexOfAny(s, c), "#1");
        c = new char[] {
            'q', 'm', 'z'
        };
        assertEquals(12, StringUtilities.indexOfAny(s, c), "#2");
        c = new char[0];
        assertEquals(-1, StringUtilities.indexOfAny(s, c), "#3");

    }

    @Test // IndexOfAny (Char [])
    public void indexOfAny1_AnyOf_Null() {
        try {
            StringUtilities.indexOfAny("mono", (char[]) null);
            fail("#1");
        } catch (NullPointerException ex) {
            assertTrue(NullPointerException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
        }
    }

    @Test // indexOfAny (Char [], Int32)
    public void IndexOfAny2() {
        String s = "abcdefghijklmd";
        char[] c;

        c = new char[] {
            'a', 'e', 'i', 'o', 'u'
        };
        assertEquals(0, StringUtilities.indexOfAny(s, c, 0), "#A1");
        assertEquals(4, StringUtilities.indexOfAny(s, c, 1), "#A1");
        assertEquals(-1, StringUtilities.indexOfAny(s, c, 9), "#A2");
        assertEquals(-1, StringUtilities.indexOfAny(s, c, s.length()), "#A3");

        c = new char[] {
            'd', 'z'
        };
        assertEquals(3, StringUtilities.indexOfAny(s, c, 0), "#B1");
        assertEquals(3, StringUtilities.indexOfAny(s, c, 3), "#B2");
        assertEquals(13, StringUtilities.indexOfAny(s, c, 4), "#B3");
        assertEquals(13, StringUtilities.indexOfAny(s, c, 9), "#B4");
        assertEquals(-1, StringUtilities.indexOfAny(s, c, s.length()), "#B5");
        assertEquals(13, StringUtilities.indexOfAny(s, c, s.length() - 1), "#B6");

        c = new char[] {
            'q', 'm', 'z'
        };
        assertEquals(12, StringUtilities.indexOfAny(s, c, 0), "#C1");
        assertEquals(12, StringUtilities.indexOfAny(s, c, 4), "#C2");
        assertEquals(12, StringUtilities.indexOfAny(s, c, 12), "#C3");
        assertEquals(-1, StringUtilities.indexOfAny(s, c, s.length()), "#C4");

        c = new char[0];
        assertEquals(-1, StringUtilities.indexOfAny(s, c, 0), "#D1");
        assertEquals(-1, StringUtilities.indexOfAny(s, c, 4), "#D2");
        assertEquals(-1, StringUtilities.indexOfAny(s, c, 9), "#D3");
        assertEquals(-1, StringUtilities.indexOfAny(s, c, s.length()), "#D4");
    }

    @Test // IndexOfAny (Char [], Int32)
    public void indexOfAny2_AnyOf_Null() {
        try {
            StringUtilities.indexOfAny("mono", (char[]) null, 0);
            fail("#1");
        } catch (NullPointerException ex) {
            assertTrue(NullPointerException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
        }
    }

    @Test // IndexOfAny (Char [], Int32)
    public void indexOfAny2_StartIndex_Negative() {
        String s = "abcdefghijklm";

        try {
            StringUtilities.indexOfAny(s,
                                       new char[] {
                                           'd'
                                       },
                                       -1,
                                       1);
            fail("#1");
        } catch (IndexOutOfBoundsException ex) {
            // Specified argument was out of the range of valid
            // values
            assertTrue(IndexOutOfBoundsException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
        }
    }

    @Test // IndexOfAny (Char [], Int32, Int32)
    public void indexOfAny2_StartIndex_Overflow() {
        String s = "abcdefghijklm";

        try {
            StringUtilities.indexOfAny(s,
                                       new char[] {
                                           'd'
                                       },
                                       s.length() + 1);
            fail("#1");
        } catch (IndexOutOfBoundsException ex) {
            // Specified argument was out of the range of valid
            // values
            assertTrue(IndexOutOfBoundsException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
        }
    }

    @Test // IndexOfAny (Char [], Int32, Int32)
    public void indexOfAny3() {
        String s = "abcdefghijklmd";
        char[] c;

        c = new char[] {
            'a', 'e', 'i', 'o', 'u'
        };
        assertEquals(0, StringUtilities.indexOfAny(s, c, 0, 2), "#A1");
        assertEquals(-1, StringUtilities.indexOfAny(s, c, 1, 2), "#A2");
        assertEquals(-1, StringUtilities.indexOfAny(s, c, 1, 3), "#A3");
        assertEquals(4, StringUtilities.indexOfAny(s, c, 1, 4), "#A3");
        assertEquals(4, StringUtilities.indexOfAny(s, c, 1, s.length() - 1), "#A4");

        c = new char[] {
            'd', 'z'
        };
        assertEquals(-1, StringUtilities.indexOfAny(s, c, 0, 2), "#B1");
        assertEquals(-1, StringUtilities.indexOfAny(s, c, 1, 2), "#B2");
        assertEquals(3, StringUtilities.indexOfAny(s, c, 1, 3), "#B3");
        assertEquals(3, StringUtilities.indexOfAny(s, c, 0, s.length()), "#B4");
        assertEquals(3, StringUtilities.indexOfAny(s, c, 1, s.length() - 1), "#B5");
        assertEquals(-1, StringUtilities.indexOfAny(s, c, s.length(), 0), "#B6");

        c = new char[] {
            'q', 'm', 'z'
        };
        assertEquals(-1, StringUtilities.indexOfAny(s, c, 0, 10), "#C1");
        assertEquals(12, StringUtilities.indexOfAny(s, c, 10, 4), "#C2");
        assertEquals(-1, StringUtilities.indexOfAny(s, c, 1, 3), "#C3");
        assertEquals(12, StringUtilities.indexOfAny(s, c, 0, s.length()), "#C4");
        assertEquals(12, StringUtilities.indexOfAny(s, c, 1, s.length() - 1), "#C5");

        c = new char[0];
        assertEquals(-1, StringUtilities.indexOfAny(s, c, 0, 3), "#D1");
        assertEquals(-1, StringUtilities.indexOfAny(s, c, 4, 9), "#D2");
        assertEquals(-1, StringUtilities.indexOfAny(s, c, 9, 5), "#D3");
        assertEquals(-1, StringUtilities.indexOfAny(s, c, 13, 1), "#D4");
    }

    @Test // IndexOfAny (Char [], Int32, Int32)
    public void indexOfAny3_AnyOf_Null() {
        try {
            StringUtilities.indexOfAny("mono", (char[]) null, 0, 0);
            fail("#1");
        } catch (NullPointerException ex) {
            assertTrue(NullPointerException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
        }
    }

    @Test // IndexOfAny (Char [], Int32, Int32)
    public void indexOfAny3_Count_Negative() {
        try {
            StringUtilities.indexOfAny("Mono",
                                       new char[] {
                                           'o'
                                       },
                                       1,
                                       -1);
            fail("#1");
        } catch (IndexOutOfBoundsException ex) {
            // Count must be positive and count must refer to a
            // location within the String/array/collection
            assertTrue(IndexOutOfBoundsException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
        }
    }

    @Test // IndexOfAny (Char [], Int32, Int32)
    public void indexOfAny3_Length_Overflow() {
        String s = "abcdefghijklm";

        try {
            StringUtilities.indexOfAny(s,
                                       new char[] {
                                           'd'
                                       },
                                       1,
                                       s.length());
            fail("#1");
        } catch (IndexOutOfBoundsException ex) {
            // Count must be positive and count must refer to a
            // location within the String/array/collection
            assertTrue(IndexOutOfBoundsException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
        }
    }

    @Test // IndexOfAny (Char [], Int32, Int32)
    public void indexOfAny3_StartIndex_Negative() {
        try {
            StringUtilities.indexOfAny("Mono",
                                       new char[] {
                                           'o'
                                       },
                                       -1,
                                       1);
            fail("#1");
        } catch (IndexOutOfBoundsException ex) {
            // Specified argument was out of the range of valid
            // values
            assertTrue(IndexOutOfBoundsException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
        }
    }

    @Test // IndexOfAny (Char [], Int32, Int32)
    public void indexOfAny3_StartIndex_Overflow() {
        String s = "abcdefghijklm";

        try {
            StringUtilities.indexOfAny(s,
                                       new char[] {
                                           'o'
                                       },
                                       s.length() + 1,
                                       1);
            fail("#1");
        } catch (IndexOutOfBoundsException ex) {
            // Specified argument was out of the range of valid
            // values
            assertTrue(IndexOutOfBoundsException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
        }
    }

    @Test
    public void lastIndexOfAny() {
        String s1 = ".bcdefghijklm";

        try {
            StringUtilities.lastIndexOfAny(s1, null);
            fail("#A1");
        } catch (NullPointerException ex) {
            assertTrue(NullPointerException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
        }

        try {
            StringUtilities.lastIndexOfAny(s1, null, s1.length());
            fail("#B1");
        } catch (NullPointerException ex) {
            assertTrue(NullPointerException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
        }

        try {
            StringUtilities.lastIndexOfAny(s1, null, s1.length(), 1);
            fail("#C1");
        } catch (NullPointerException ex) {
            assertTrue(NullPointerException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
        }

        char[] c1 = {
            'a', 'e', 'i', 'o', 'u'
        };
        assertEquals(8, StringUtilities.lastIndexOfAny(s1, c1), "#D1");
        assertEquals(4, StringUtilities.lastIndexOfAny(s1, c1, 7), "#D2");
        assertEquals(-1, StringUtilities.lastIndexOfAny(s1, c1, 3), "#D3");
        assertEquals(4, StringUtilities.lastIndexOfAny(s1, c1, s1.length() - 6, 4), "#D4");
        assertEquals(-1, StringUtilities.lastIndexOfAny(s1, c1, s1.length() - 6, 3), "#D5");

        try {
            StringUtilities.lastIndexOfAny(s1, c1, -1);
            fail("#E1");
        } catch (IndexOutOfBoundsException ex) {
            // Index was out of range. Must be non-negative and
            // less than the size of the collection
            assertTrue(IndexOutOfBoundsException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
        }

        try {
            StringUtilities.lastIndexOfAny(s1, c1, -1, 1);
            fail("#F1");
        } catch (IndexOutOfBoundsException ex) {
            // Index was out of range. Must be non-negative and
            // less than the size of the collection
            assertTrue(IndexOutOfBoundsException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
        }
    }

    @Test
    public void lastIndexOfAny_Length_Overflow() {
        try {
            StringUtilities.lastIndexOfAny("Mono",
                                           new char[] {
                                               'o'
                                           },
                                           1,
                                           Integer.MAX_VALUE);
            fail("#1");
        } catch (IndexOutOfBoundsException ex) {
            // Count must be positive and count must refer to a
            // location within the String/array/collection
            assertTrue(IndexOutOfBoundsException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
        }
    }

    @Test
    public void lastIndexOfAny_StartIndex_Overflow() {
        try {
            StringUtilities.lastIndexOfAny("Mono",
                                           new char[] {
                                               'o'
                                           },
                                           Integer.MAX_VALUE,
                                           1);
            fail("#1");
        } catch (IndexOutOfBoundsException ex) {
            // Index was out of range. Must be non-negative and
            // less than the size of the collection
            assertTrue(IndexOutOfBoundsException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
        }
    }
}

/* */
