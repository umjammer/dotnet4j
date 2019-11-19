//
// (c) Marcin Szczepanski 
// (c) 2002 Ximian, Inc. (http://www.ximian.com)
// (c) 2003 Ben Maurer
// (c) 2003 Gilles Freart
// Copyright (C) 2005 Novell, Inc (http://www.novell.com)
//

package dotnet4j.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.opentest4j.AssertionFailedError;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


/**
 * System.IO.Path Test Cases
 *
 * @author Marcin Szczepanski (marcins@zipworld.com.au)
 * @author Gonzalo Paniagua Javier (gonzalo@ximian.com)
 * @author Ben Maurer (bmaurer@users.sf.net)
 * @author Gilles Freart (gfr@skynet.be)
 * @author Atsushi Enomoto (atsushi@ximian.com)
 * @author Sebastien Pouliot <sebastien@ximian.com>
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 */
@Disabled
public class PathTest {
    enum OsType {
        Windows,
        Unix,
        Mac
    }

    static String path1;

    static String path2;

    static String path3;

    static OsType os;

    static char DSC = Path.DirectorySeparatorChar;

    static char ADSC = Path.AltDirectorySeparatorChar;

    @BeforeAll
    public static void setUp() {
        if ('/' == DSC) {
            os = OsType.Unix;
            path1 = "/foo/test.txt";
            path2 = "/etc";
            path3 = "init.d";
        } else if ('\\' == DSC) {
            os = OsType.Windows;
            path1 = "c:\\foo\\test.txt";
            path2 = "c:\\windows";// System.getenv("SYSTEMROOT");
            path3 = "system32";
        } else {
            os = OsType.Mac;
            // FIXME: For Mac. figure this out when we need it
            path1 = "foo\\test.txt";
            path2 = "foo";
            path3 = "bar";
        }
        System.err.println(os);
    }

    boolean isWindows() {
        return os == OsType.Windows;
    }

    boolean isUnix() {
        return os == OsType.Unix;
    }

    boolean isMac() {
        return os == OsType.Mac;
    }

    @Test
    public void changeExtension() {
        String[] files = new String[3];
        files[OsType.Unix.ordinal()] = "/foo/test.doc";
        files[OsType.Windows.ordinal()] = "c:\\foo\\test.doc";
        files[OsType.Mac.ordinal()] = "foo:test.doc";

        String testPath = Path.changeExtension(path1, "doc");
        assertEquals(files[os.ordinal()], testPath, "changeExtension #01");

        testPath = Path.changeExtension("", ".extension");
        assertEquals("", testPath, "changeExtension #02");

        testPath = Path.changeExtension(null, ".extension");
        assertEquals(null, testPath, "changeExtension #03");

        testPath = Path.changeExtension("path", null);
        assertEquals("path", testPath, "changeExtension #04");

        testPath = Path.changeExtension("path.ext", "doc");
        assertEquals("path.doc", testPath, "changeExtension #05");

        testPath = Path.changeExtension("path.ext1.ext2", "doc");
        assertEquals("path.ext1.doc", testPath, "changeExtension #06");

        testPath = Path.changeExtension("hogehoge.xml", ".xsl");
        assertEquals("hogehoge.xsl", testPath, "changeExtension #07");
        testPath = Path.changeExtension("hogehoge", ".xsl");
        assertEquals("hogehoge.xsl", testPath, "changeExtension #08");
        testPath = Path.changeExtension("hogehoge.xml", "xsl");
        assertEquals("hogehoge.xsl", testPath, "changeExtension #09");
        testPath = Path.changeExtension("hogehoge", "xsl");
        assertEquals("hogehoge.xsl", testPath, "changeExtension #10");
        testPath = Path.changeExtension("hogehoge.xml", "");
        assertEquals("hogehoge.", testPath, "changeExtension #11");
        testPath = Path.changeExtension("hogehoge", "");
        assertEquals("hogehoge.", testPath, "changeExtension #12");
        testPath = Path.changeExtension("hogehoge.", null);
        assertEquals("hogehoge", testPath, "changeExtension #13");
        testPath = Path.changeExtension("hogehoge", null);
        assertEquals("hogehoge", testPath, "changeExtension #14");
        testPath = Path.changeExtension("", null);
        assertEquals("", testPath, "changeExtension #15");
        testPath = Path.changeExtension("", "bashrc");
        assertEquals("", testPath, "changeExtension #16");
        testPath = Path.changeExtension("", ".bashrc");
        assertEquals("", testPath, "changeExtension #17");
        testPath = Path.changeExtension(null, null);
        assertNull(testPath, "changeExtension #18");
    }

    @Test
    public void changeExtension_Extension_InvalidPathChars() {
        String fn = Path.changeExtension("file.ext", "<");
        assertEquals("file.<", fn, "Invalid filename");
    }

    @Test
    public void changeExtension_Path_InvalidPathChars() {
        try {
            Path.changeExtension("fi\0le.ext", ".extension");
            fail("#1");
        } catch (IllegalArgumentException ex) {
            // Illegal characters : path
            assertTrue(IllegalArgumentException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
            assertNotNull(ex.getMessage(), "#4");
//            assertNull(ex.ParamName, "#5");
        }
    }

    @Test
    public void combine() {
        String[] files = new String[3];
        files[OsType.Unix.ordinal()] = "/etc/init.d";
        files[OsType.Windows.ordinal()] = /* System.getenv("SYSTEMROOT") */"c:\\windows" + "\\system32";
        files[OsType.Mac.ordinal()] = "foo:bar";

        String testPath = Path.combine(path2, path3);
        assertEquals(files[os.ordinal()], testPath, "Combine #01");

        testPath = Path.combine("one", "");
        assertEquals("one", testPath, "Combine #02");

        testPath = Path.combine("", "one");
        assertEquals("one", testPath, "Combine #03");

        String current = System.getProperty("user.dir");
        boolean currentIsDSC = current.length() == 1 && current.charAt(0) == DSC;
        testPath = Path.combine(current, "one");

        String expected = (currentIsDSC ? "" : current) + DSC + "one";
        assertEquals(expected, testPath, "Combine #04");

        testPath = Path.combine("one", current);
        // LAMESPEC noted : Path.cs
        assertEquals(current, testPath, "Combine #05");

        testPath = Path.combine(current, expected);
        assertEquals(expected, testPath, "Combine #06");

        testPath = DSC + "one";
        testPath = Path.combine(testPath, "two" + DSC);
        expected = DSC + "one" + DSC + "two" + DSC;
        assertEquals(expected, testPath, "Combine #06");

        testPath = "one" + DSC;
        testPath = Path.combine(testPath, DSC + "two");
        expected = DSC + "two";
        assertEquals(expected, testPath, "Combine #06");

        testPath = "one" + DSC;
        testPath = Path.combine(testPath, "two" + DSC);
        expected = "one" + DSC + "two" + DSC;
        assertEquals(expected, testPath, "Combine #07");

        assertEquals("a",
                     Path.combine(new String[] {
                         "a", ""
                     }),
                     "Combine #08");
    }

    @Test
    public void combine_Path1_InvalidPathChars() {
        try {
            Path.combine("a\0", "one");
            fail("#1");
        } catch (IllegalArgumentException ex) {
            // Illegal characters : path
            assertTrue(IllegalArgumentException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
            assertNotNull(ex.getMessage(), "#4");
//            assertNull(ex.ParamName, "#5");
        }
    }

    @Test
    public void combine_Path1_Null() {
        try {
            Path.combine(null, "one");
            fail("#1");
        } catch (NullPointerException ex) {
            assertTrue(NullPointerException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
            assertNotNull(ex.getMessage(), "#4");
//            assertEquals("path1", ex.ParamName, "#5");
        }
    }

    @Test
    public void combine_Path2_InvalidPathChars() {
        try {
            Path.combine("one", "a\0");
            fail("#1");
        } catch (IllegalArgumentException ex) {
            // Illegal characters : path
            assertTrue(IllegalArgumentException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
            assertNotNull(ex.getMessage(), "#4");
//            assertNull(ex.ParamName, "#5");
        }
    }

    @Test
    public void combine_Path2_Null() {
        try {
            Path.combine("one", null);
            fail("#1");
        } catch (NullPointerException ex) {
            assertTrue(NullPointerException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
            assertNotNull(ex.getMessage(), "#4");
//            assertEquals("path2", ex.ParamName, "#5");
        }
    }

    @Test
    public void getDirectoryName() {
        String[] files = new String[3];
        files[OsType.Unix.ordinal()] = "/foo";
        files[OsType.Windows.ordinal()] = "c:\\foo";
        files[OsType.Mac.ordinal()] = "foo";

        String testDirName = Path.getDirectoryName(path1);
        assertEquals(files[os.ordinal()], testDirName, "#A1");
        testDirName = Path.getDirectoryName(files[os.ordinal()] + DSC);
        assertEquals(files[os.ordinal()], testDirName, "#A2");

        if (isWindows()) {
            assertEquals("C:\\foo", Path.getDirectoryName("C:\\foo\\foo.txt"), "#B1");
            assertEquals(null, Path.getDirectoryName("C:"), "#B2");
            assertEquals(null, Path.getDirectoryName("C:\\"), "#B3");
            assertEquals("C:\\", Path.getDirectoryName("C:\\dir"), "#B4");
            assertEquals("C:\\dir", Path.getDirectoryName("C:\\dir\\"), "#B5");
            assertEquals("C:\\dir", Path.getDirectoryName("C:\\dir\\dir"), "#B6");
            assertEquals("C:\\dir\\dir", Path.getDirectoryName("C:\\dir\\dir\\"), "#B7");
            assertEquals("C:", Path.getDirectoryName("C:foo.txt"), "#B8");
            assertEquals("C:dir", Path.getDirectoryName("C:dir\\"), "#B9");

            assertEquals("\\foo\\bar", Path.getDirectoryName("/foo//bar/dingus"), "#C1");
            assertEquals("foo\\bar", Path.getDirectoryName("foo/bar/"), "#C2");
            assertEquals("foo\\bar", Path.getDirectoryName("foo/bar\\xxx"), "#C3");
            assertEquals("\\\\host\\dir\\dir2", Path.getDirectoryName("\\\\host\\dir\\\\dir2\\path"), "#C4");

            // UNC tests
            assertEquals(null, Path.getDirectoryName("\\\\"), "#D1");
            assertEquals(null, Path.getDirectoryName("\\\\server"), "#D2");
            assertEquals(null, Path.getDirectoryName("\\\\server\\share"), "#D3");
            assertEquals("\\\\server\\share", Path.getDirectoryName("\\\\server\\share\\"), "#D4");
            assertEquals("\\\\server\\share", Path.getDirectoryName("\\\\server\\share\\dir"), "#D5");
            assertEquals("\\\\server\\share\\dir", Path.getDirectoryName("\\\\server\\share\\dir\\subdir"), "#D6");
        } else {
            assertEquals("/etc", Path.getDirectoryName("/etc/hostname"), "#B1");
            assertEquals("/foo/bar", Path.getDirectoryName("/foo//bar/dingus"), "#B2");
            assertEquals("foo/bar", Path.getDirectoryName("foo/bar/"), "#B3");
            assertEquals("/", Path.getDirectoryName("/tmp"), "#B4");
            assertNull(Path.getDirectoryName("/"), "#B5");
            assertEquals("a", Path.getDirectoryName("a//b"), "#B6");
        }
    }

    @Test
    public void getDirectoryName_Path_Empty() {
        try {
            Path.getDirectoryName("");
            fail("#1");
        } catch (IllegalArgumentException ex) {
            // The path is not of a legal form
            assertTrue(IllegalArgumentException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
            assertNotNull(ex.getMessage(), "#4");
//            assertNull(ex.ParamName, "#5");
        }
    }

    @Test
    public void getDirectoryName_Path_InvalidPathChars() {
        try {
            Path.getDirectoryName("hi\0world");
            fail("#1");
        } catch (IllegalArgumentException ex) {
            // Illegal characters : path
            assertTrue(IllegalArgumentException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
            assertNotNull(ex.getMessage(), "#4");
//            assertNull(ex.ParamName, "#5");
        }
    }

    @Test
    public void getDirectoryName_Path_Null() {
        assertNull(Path.getDirectoryName(null));
    }

    @Test
    public void getDirectoryName_Path_Whitespace() {
        try {
            Path.getDirectoryName("   ");
            fail("#1");
        } catch (IllegalArgumentException ex) {
            // The path is not of a legal form
            assertTrue(IllegalArgumentException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
            assertNotNull(ex.getMessage(), "#4");
//            assertNull(ex.ParamName, "#5");
        }
    }

    @Test
    public void getDirectoryName_Replaces_AltDirectorySeparatorChar() {
        assertEquals(String.format("foo%sbar", DSC), Path.getDirectoryName(String.format("foo%1$sbar%1$sdingus", ADSC)), "#1");
    }

    @Test
    public void getExtension() {
        String testExtn = Path.getExtension(path1);

        assertEquals(".txt", testExtn, "GetExtension #01");

        testExtn = Path.getExtension(path2);
        assertEquals("", testExtn, "GetExtension #02");

        testExtn = Path.getExtension("");
        assertEquals("", testExtn, "GetExtension #03");

        testExtn = Path.getExtension(null);
        assertEquals(null, testExtn, "GetExtension #04");

        testExtn = Path.getExtension(" ");
        assertEquals("", testExtn, "GetExtension #05");

        testExtn = Path.getExtension(path1 + ".doc");
        assertEquals(".doc", testExtn, "GetExtension #06");

        testExtn = Path.getExtension(path1 + ".doc" + DSC + "a.txt");
        assertEquals(".txt", testExtn, "GetExtension #07");

        testExtn = Path.getExtension(".");
        assertEquals("", testExtn, "GetExtension #08");

        testExtn = Path.getExtension("end.");
        assertEquals("", testExtn, "GetExtension #09");

        testExtn = Path.getExtension(".start");
        assertEquals(".start", testExtn, "GetExtension #10");

        testExtn = Path.getExtension(".a");
        assertEquals(".a", testExtn, "GetExtension #11");

        testExtn = Path.getExtension("a.");
        assertEquals("", testExtn, "GetExtension #12");

        testExtn = Path.getExtension("a");
        assertEquals("", testExtn, "GetExtension #13");

        testExtn = Path.getExtension("makefile");
        assertEquals("", testExtn, "GetExtension #14");
    }

    @Test
    public void getExtension_Path_InvalidPathChars() {
        try {
            Path.getExtension("hi\0world.txt");
            fail("#1");
        } catch (IllegalArgumentException ex) {
            // Illegal characters : path.
            assertTrue(IllegalArgumentException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
            assertNotNull(ex.getMessage(), "#4");
//            assertNull(ex.ParamName, "#5");
        }
    }

    @Test
    public void getFileName() {
        String testFileName = Path.getFileName(path1);

        assertEquals("test.txt", testFileName, "#1");
        testFileName = Path.getFileName((String) null);
        assertEquals(null, testFileName, "#2");
        testFileName = Path.getFileName("");
        assertEquals("", testFileName, "#3");
        testFileName = Path.getFileName(" ");
        assertEquals(" ", testFileName, "#4");
    }

    @Test
    public void getFileName_Path_InvalidPathChars() {
        try {
            Path.getFileName("hi\0world");
            fail("#1");
        } catch (IllegalArgumentException ex) {
            // Illegal characters : path
            assertTrue(IllegalArgumentException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
            assertNotNull(ex.getMessage(), "#4");
//            assertNull(ex.ParamName, "#5");
        }
    }

    @Test
    public void getFileNameWithoutExtension() {
        String testFileName = Path.getFileNameWithoutExtension(path1);

        assertEquals("test", testFileName, "GetFileNameWithoutExtension #01");

        testFileName = Path.getFileNameWithoutExtension(null);
        assertEquals(null, testFileName, "GetFileNameWithoutExtension #02");

        testFileName = Path.getFileNameWithoutExtension("");
        assertEquals("", testFileName, "GetFileNameWithoutExtension #03");
    }

    @Test
    public void getFileNameWithoutExtension_Path_InvalidPathChars() {
        try {
            Path.getFileNameWithoutExtension("hi\0world");
            fail("#1");
        } catch (IllegalArgumentException ex) {
            // Illegal characters : path
            assertTrue(IllegalArgumentException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
            assertNotNull(ex.getMessage(), "#4");
//            assertNull(ex.ParamName, "#5");
        }
    }

    @Test
    public void getFullPath() {
        String current = System.getProperty("user.dir");
        boolean currentIsDSC = current.length() == 1 && current.charAt(0) == DSC;
        String testFullPath = Path.getFullPath("foo.txt");
        String expected = (currentIsDSC ? "" : current) + DSC + "foo.txt";
        assertEquals(expected, testFullPath, "GetFullPath #01");

        testFullPath = Path.getFullPath("a//./.././foo.txt");
        assertEquals(expected, testFullPath, "GetFullPath #02");

        if (!isWindows()) {
            assertEquals("/bin/bash", Path.getFullPath("/../bin/bash"));
        }

    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    public void getFullPath_Unix() {
        String root = "/";
        String[][] test = new String[][] {
            // @formatter:off
            { "root////././././././../root/././../root", "root" },
            { "root/", "root/" },
            { "root/./", "root/" },
            { "root/./", "root/" },
            { "root/../", "" },
            { "root/../", "" },
            { "root/../..", "" },
            { "root/.hiddenfile", "root/.hiddenfile" },
            { "root/. /", "root/. /" },
            { "root/.. /", "root/.. /" },
            { "root/..weirdname", "root/..weirdname" },
            { "root/..", "" },
            { "root/../a/b/../../..", "" },
            { "root/./..", "" },
            { "..", "" },
            { ".", "" },
            { "root//dir", "root/dir" },
            { "root/.              /", "root/.              /" },
            { "root/..             /", "root/..             /" },
            { "root/      .              /", "root/      .              /" },
            { "root/      ..             /", "root/      ..             /" },
            { "root/./", "root/" },
            // ERROR! Paths are trimmed
            // I don't understand this comment^^.
            // No trimming occurs but the paths are not equal. That's why the
            // test fails. Commented out.
            // {"root/.. /", "root/.. /"},
            { ".//", "" }
            // @formatter:on
        };

        for (int i = 0; i < test.length; i++) {
            assertEquals(root + test[i][1], Path.getFullPath(root + test[i][0]), String.format("GetFullPathUnix #%d", i));
        }

        assertEquals("/", Path.getFullPath("/"), "#01");
        assertEquals("/hey", Path.getFullPath("/hey"), "#02");
        assertEquals(System.getProperty("user.dir"), Path.getFullPath("."), "#03");
        assertEquals(Path.combine(System.getProperty("user.dir"), "hey"), Path.getFullPath("hey"), "#04");
        assertEquals("/", Path.getFullPath("/"), "#01");

        String curdir = System.getProperty("user.dir");
        try {
            System.setProperty("user.dir", "/");
            assertEquals("/test.txt", Path.getFullPath("test.txt"), "xambug #833");
        } finally {
            System.setProperty("user.dir", curdir);
        }
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void getFullPath_Windows() {

        String root = "C:\\";
        String[][] test = new String[][] {
            // @formatter:off
            { "root////././././././../root/././../root", "root" },
            { "root/", "root\\" },
            { "root/./", "root\\" },
            { "root/./", "root\\" },
            { "root/../", "" },
            { "root/../", "" },
            { "root/../..", "" },
            { "root/.hiddenfile", "root\\.hiddenfile" },
            { "root/. /", "root\\" },
            { "root/.. /", "" },
            { "root/..weirdname", "root\\..weirdname" },
            { "root/..", "" },
            { "root/../a/b/../../..", "" },
            { "root/./..", "" },
            { "..", "" },
            { ".", "" },
            { "root//dir", "root\\dir" },
            { "root/.              /", "root\\" },
            { "root/..             /", "" },
            { "root/./", "root\\" },
            { "root/..                      /", "" },
            { ".//", "" }
            // @formatter:on
        };

        for (int i = 0; i < test.length; i++) {
            try {
                assertEquals(root + test[i][1],
                             Path.getFullPath(root + test[i][0]),
                             String.format("GetFullPathWindows #%d", i));
            } catch (Exception ex) {
                fail(String.format("GetFullPathWindows #%d (\"%s\") failed: %s", i, root + test[i][0], ex.getClass()));
            }
        }

        // UNC tests
        String root2 = "\\\\server\\share";
        root = "\\\\server\\share\\";
        test = new String[][] {
            // @formatter:off
            { "root////././././././../root/././../root", "root" },
            { "root/", "root\\\\" },
            { "root/./", "root\\\\" },
            { "root/./", "root\\\\" },
            { "root/../", "" },
            { "root/../", "" },
            { "root/../..", null },
            { "root/.hiddenfile", "root\\\\.hiddenfile" },
            { "root/. /", "root\\\\" },
            { "root/.. /", "" },
            { "root/..weirdname", "root\\\\..weirdname" },
            { "root/..", null },
            { "root/../a/b/../../..", null },
            { "root/./..", null },
            { "..", null },
            { ".", null },
            { "root//dir", "root\\\\dir" },
            { "root/.              /", "root\\\\" },
            { "root/..             /", "" },
            { "root/./", "root\\\\" },
            { "root/..                      /", "" },
            { ".//", "" }
            // @formatter:on
        };

        for (int i = 0; i < test.length; i++) {
            // "null" means we have to compare against "root2"
            String res = test[i][1] != null ? root + test[i][1] : root2;
            try {
                assertEquals(res, Path.getFullPath(root + test[i][0]), String.format("GetFullPathWindows UNC #%d", i));
            } catch (AssertionFailedError e) {
                throw e;
            } catch (Exception ex) {
                fail(String.format("GetFullPathWindows UNC #%d (\"%s\") failed: %s", i, root + test[i][0], ex.getClass()));
            }
        }

        test = new String[][] {
            // @formatter:off
            { "root////././././././../root/././../root", "root" },
            { "root/", "root\\\\" },
            { "root/./", "root\\\\" },
            { "root/./", "root\\\\" },
            { "root/../", "" },
            { "root/../", "" },
            { "root/../..", null },
            { "root/.hiddenfile", "root\\\\.hiddenfile" },
            { "root/. /", "root\\\\" },
            { "root/.. /", "" },
            { "root/..weirdname", "root\\\\..weirdname" },
            { "root/..", null },
            { "root/../a/b/../../..", null },
            { "root/./..", null },
            { "..", null },
            { ".", null },
            { "root//dir", "root\\\\dir" },
            { "root/.              /", "root\\\\" },
            { "root/..             /", "" },
            { "root/./", "root\\\\" },
            { "root/..                      /", "" },
            { ".//", "" }
            // @formatter:on
        };

        String root3 = "//server/share";
        root = "//server/share/";
        boolean needSlashConvert = Path.DirectorySeparatorChar != '/';

        for (int i = 0; i < test.length; i++) {
            // "null" means we have to compare against "root2"
            String res = test[i][1] != null ? root + test[i][1] : root3;
            if (needSlashConvert)
                res = res.replace('/', Path.DirectorySeparatorChar);
            try {
                assertEquals(res, Path.getFullPath(root + test[i][0]), String.format("GetFullPathWindows UNC[2] #%d", i));
            } catch (AssertionFailedError e) {
                throw e;
            } catch (Exception ex) {
                fail(String.format("GetFullPathWindows UNC[2] #%d (\"%s\") failed: %s", i, root + test[i][0], ex.getClass()));
            }
        }

        // These cases require that we don't pass a root to GetFullPath - it
        // should return the proper drive root.
        String root4 = Path.getPathRoot(System.getProperty("user.dir"));
        assertEquals(root4, Path.getFullPath("\\"));
        assertEquals(root4, Path.getFullPath("/"));
    }

    @Test
    public void getFullPath_Path_Empty() {
        try {
            Path.getFullPath("");
            fail("#1");
        } catch (IllegalArgumentException ex) {
            // The path is not of a legal form
            assertTrue(IllegalArgumentException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
            assertNotNull(ex.getMessage(), "#4");
//            assertNull(ex.ParamName, "#5");
        }
    }

    @Test
    public void getFullPath_Path_EndingSeparator() {
        String fp = Path.getFullPath("something/");
        char end = fp.charAt(fp.length() - 1);
        assertTrue(end == Path.DirectorySeparatorChar);
    }

    @Test
    public void getFullPath_Path_InvalidPathChars() {
        try {
            Path.getFullPath("hi\0world");
            fail("#1");
        } catch (IllegalArgumentException ex) {
            // Illegal characters : path
            assertTrue(IllegalArgumentException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
            assertNotNull(ex.getMessage(), "#4");
//            assertNull(ex.ParamName, "#5");
        }
    }

    @Test
    public void getFullPath_Path_Null() {
        try {
            Path.getFullPath(null);
            fail("#1");
        } catch (NullPointerException ex) {
            assertTrue(NullPointerException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
            assertNotNull(ex.getMessage(), "#4");
//            assertEquals("path", ex.ParamName, "#5");
        }
    }

    @Test
    public void getFullPath_Path_Whitespace() {
        try {
            Path.getFullPath("  ");
            fail("#1");
        } catch (IllegalArgumentException ex) {
            // The path is not of a legal form
            assertTrue(IllegalArgumentException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
            assertNotNull(ex.getMessage(), "#4");
//            assertNull(ex.ParamName, "#5");
        }
    }

    @Test
    public void getFullPath2() {
        if (isWindows()) {
            assertEquals("Z:\\", Path.getFullPath("Z:"), "GetFullPath w#01");
            assertEquals("c:\\abc\\def", Path.getFullPath("c:\\abc\\def"), "GetFullPath w#02");
            assertTrue(Path.getFullPath("\\").endsWith("\\"), "GetFullPath w#03");
            // "\\\\\\\\" is not allowed
            assertTrue(Path.getFullPath("/").endsWith("\\"), "GetFullPath w#05");
            // "//" is not allowed
            assertTrue(Path.getFullPath("readme.txt").endsWith("\\readme.txt"), "GetFullPath w#07");
            assertTrue(Path.getFullPath("c").endsWith("\\c"), "GetFullPath w#08");
            assertTrue(Path.getFullPath("abc\\def").endsWith("abc\\def"), "GetFullPath w#09");
            assertTrue(Path.getFullPath("\\abc\\def").endsWith("\\abc\\def"), "GetFullPath w#10");
            assertEquals("\\\\abc\\def", Path.getFullPath("\\\\abc\\def"), "GetFullPath w#11");
            assertEquals(System.getProperty("user.dir") + "\\abc\\def", Path.getFullPath("abc//def"), "GetFullPath w#12");
            assertEquals(System.getProperty("user.dir").substring(0, 2) + "\\abc\\def",
                         Path.getFullPath("/abc/def"),
                         "GetFullPath w#13");
            assertEquals("\\\\abc\\def", Path.getFullPath("//abc/def"), "GetFullPath w#14");
        } else {
            assertEquals("/", Path.getFullPath("/"), "#01");
            assertEquals("/hey", Path.getFullPath("/hey"), "#02");
            assertEquals(System.getProperty("user.dir"), Path.getFullPath("."), "#03");
            assertEquals(Path.combine(System.getProperty("user.dir"), "hey"), Path.getFullPath("hey"), "#04");
        }
    }

    @Test
    public void getPathRoot() {
        String current;
        String expected;
        if (!isWindows()) {
            current = System.getProperty("user.dir");
            expected = String.valueOf(current.charAt(0));
        } else {
            current = "J:\\Some\\Strange Directory\\Name";
            expected = "J:\\";
        }

        String pathRoot = Path.getPathRoot(current);
        assertEquals(expected, pathRoot, "GetPathRoot #01");
    }

    @Test
    public void getPathRoot2() {
        // note: this method doesn't call Directory.getCurrentDirectory so it
        // can be
        // reused for partial trust unit tests : PathCas.cs

        String pathRoot;

        pathRoot = Path.getPathRoot("hola");
        assertEquals("", pathRoot, "#A1");
        pathRoot = Path.getPathRoot(null);
        assertEquals(null, pathRoot, "#A2");

        if (isWindows()) {
            assertEquals("z:", Path.getPathRoot("z:"), "GetPathRoot w#01");
            assertEquals("c:\\", Path.getPathRoot("c:\\abc\\def"), "GetPathRoot w#02");
            assertEquals("\\", Path.getPathRoot("\\"), "GetPathRoot w#03");
            assertEquals("\\\\", Path.getPathRoot("\\\\"), "GetPathRoot w#04");
            assertEquals("\\", Path.getPathRoot("/"), "GetPathRoot w#05");
            assertEquals("\\\\", Path.getPathRoot("//"), "GetPathRoot w#06");
            assertEquals("", Path.getPathRoot("readme.txt"), "GetPathRoot w#07");
            assertEquals("", Path.getPathRoot("c"), "GetPathRoot w#08");
            assertEquals("", Path.getPathRoot("abc\\def"), "GetPathRoot w#09");
            assertEquals("\\", Path.getPathRoot("\\abc\\def"), "GetPathRoot w#10");
            assertEquals("\\\\abc\\def", Path.getPathRoot("\\\\abc\\def"), "GetPathRoot w#11");
            assertEquals("", Path.getPathRoot("abc//def"), "GetPathRoot w#12");
            assertEquals("\\", Path.getPathRoot("/abc/def"), "GetPathRoot w#13");
            assertEquals("\\\\abc\\def", Path.getPathRoot("//abc/def"), "GetPathRoot w#14");
            assertEquals("C:\\", Path.getPathRoot("C:\\"), "GetPathRoot w#15");
            assertEquals("C:\\", Path.getPathRoot("C:\\\\"), "GetPathRoot w#16");
            assertEquals("\\\\abc\\def", Path.getPathRoot("\\\\abc\\def\\ghi"), "GetPathRoot w#17");
        } else {
            // TODO: Same tests for Unix.
        }
    }

    @Test
    public void getPathRoot_Path_Empty() {
        try {
            Path.getPathRoot("");
            fail("#1");
        } catch (IllegalArgumentException ex) {
            // The path is not of a legal form
            assertTrue(IllegalArgumentException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
            assertNotNull(ex.getMessage(), "#4");
//            assertNull(ex.ParamName, "#5");
        }
    }

    @Test
    public void getPathRoot_Path_InvalidPathChars() {
        try {
            Path.getPathRoot("hi\\0world");
            fail("#1");
        } catch (IllegalArgumentException ex) {
            // Illegal characters : path
            assertTrue(IllegalArgumentException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
            assertNotNull(ex.getMessage(), "#4");
//            assertNull(ex.ParamName, "#5");
        }
    }

    @Test
    public void getPathRoot_Path_Whitespace() {
        try {
            Path.getPathRoot("  ");
            fail("#1");
        } catch (IllegalArgumentException ex) {
            // The path is not of a legal form
            assertTrue(IllegalArgumentException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
            assertNotNull(ex.getMessage(), "#4");
//            assertNull(ex.ParamName, "#5");
        }
    }

    @Test
    public void getTempPath() {
        String getTempPath = Path.getTempPath();
        assertTrue(getTempPath != "", "GetTempPath #01");
        assertTrue(Path.isPathRooted(getTempPath), "GetTempPath #02");
        assertEquals(Path.DirectorySeparatorChar, getTempPath.charAt(getTempPath.length() - 1), "GetTempPath #03");
    }

    @Test
    public void getTempFileName() {
        String getTempFileName = null;
        try {
            getTempFileName = Path.getTempFileName();
            assertTrue(getTempFileName != "", "GetTempFileName #01");
            assertTrue(Files.exists(Paths.get(getTempFileName)), "GetTempFileName #02");
        } finally {
            if (getTempFileName != null && getTempFileName != "") {
                try {
                    Files.delete(Paths.get(getTempFileName));
                } catch (IOException e) {
                    throw new dotnet4j.io.IOException(e);
                }
            }
        }
    }

    @Test
    public void hasExtension() {
        assertEquals(true, Path.hasExtension("foo.txt"), "hasExtension #01");
        assertEquals(false, Path.hasExtension("foo"), "hasExtension #02");
        assertEquals(true, Path.hasExtension(path1), "hasExtension #03");
        assertEquals(false, Path.hasExtension(path2), "hasExtension #04");
        assertEquals(false, Path.hasExtension(null), "hasExtension #05");
        assertEquals(false, Path.hasExtension(""), "hasExtension #06");
        assertEquals(false, Path.hasExtension(" "), "hasExtension #07");
        assertEquals(false, Path.hasExtension("."), "hasExtension #08");
        assertEquals(false, Path.hasExtension("end."), "hasExtension #09");
        assertEquals(true, Path.hasExtension(".start"), "hasExtension #10");
        assertEquals(true, Path.hasExtension(".a"), "hasExtension #11");
        assertEquals(false, Path.hasExtension("a."), "hasExtension #12");
        assertEquals(false, Path.hasExtension("Makefile"), "hasExtension #13");
    }

    @Test
    public void hasExtension_Path_InvalidPathChars() {
        try {
            Path.hasExtension("hi\\0world.txt");
            fail("#1");
        } catch (IllegalArgumentException ex) {
            // Illegal characters : path
            assertEquals(IllegalArgumentException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
            assertNotNull(ex.getMessage(), "#4");
//            assertNull(ex.ParamName, "#5");
        }
    }

    @Test
    public void isPathRooted() {
        assertTrue(Path.isPathRooted(path2), "IsPathRooted #01");
        assertTrue(!Path.isPathRooted(path3), "IsPathRooted #02");
        assertTrue(!Path.isPathRooted(null), "IsPathRooted #03");
        assertTrue(!Path.isPathRooted(""), "IsPathRooted #04");
        assertTrue(!Path.isPathRooted(" "), "IsPathRooted #05");
        assertTrue(Path.isPathRooted("/"), "IsPathRooted #06");
        assertTrue(Path.isPathRooted("//"), "IsPathRooted #07");
        assertTrue(!Path.isPathRooted(":"), "IsPathRooted #08");

        if (isWindows()) {
            assertTrue(Path.isPathRooted("\\"), "IsPathRooted #09");
            assertTrue(Path.isPathRooted("\\\\"), "IsPathRooted #10");
            assertTrue(Path.isPathRooted("z:"), "IsPathRooted #11");
            assertTrue(Path.isPathRooted("z:\\"), "IsPathRooted #12");
            assertTrue(Path.isPathRooted("z:\\topdir"), "IsPathRooted #13");
            // This looks MS BUG. It is treated as absolute path
            assertTrue(Path.isPathRooted("z:curdir"), "IsPathRooted #14");
            assertTrue(Path.isPathRooted("\\abc\\def"), "IsPathRooted #15");
        } else {
            if (System.getenv("MONO_IOMAP") == "all") {
                assertTrue(Path.isPathRooted("\\"), "IsPathRooted #16");
                assertTrue(Path.isPathRooted("\\\\"), "IsPathRooted #17");
            } else {
                assertTrue(!Path.isPathRooted("\\"), "IsPathRooted #09");
                assertTrue(!Path.isPathRooted("\\\\"), "IsPathRooted #10");
                assertTrue(!Path.isPathRooted("z:"), "IsPathRooted #11");
            }
        }
    }

    @Test
    public void isPathRooted_Path_Empty() {
        assertTrue(!Path.isPathRooted(""));
    }

    @Test
    public void isPathRooted_Path_InvalidPathChars() {
        try {
            Path.isPathRooted("hi\0world");
            fail("#1");
        } catch (IllegalArgumentException ex) {
            // Illegal characters : path.
            assertTrue(IllegalArgumentException.class.isInstance(ex), "#2");
            assertNull(ex.getCause(), "#3");
            assertNotNull(ex.getMessage(), "#4");
//            assertNull(ex.ParamName, "#5");
        }
    }

    @Test
    public void isPathRooted_Path_Null() {
        assertTrue(!Path.isPathRooted(null));
    }

    @Test
    public void isPathRooted_Path_Whitespace() {
        assertTrue(!Path.isPathRooted("  "));
    }

    @Test
    public void canonicalizeDots() {
        String current = Path.getFullPath(".");
        assertTrue(!current.endsWith("."), "TestCanonicalizeDotst #01");
        String parent = Path.getFullPath("..");
        assertTrue(!current.endsWith(".."), "TestCanonicalizeDotst #02");
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void windowsSystem32_76191() {
        // check for Unix platforms - see FAQ for more details
        // http://www.mono-project.com/FAQ:_Technical#How_to_detect_the_execution_platform_.3F

        String curdir = System.getProperty("user.dir");
        try {
            String system = System.getProperty("SYSTEMROOT");
            System.setProperty("user.dir", system);
            String drive = system.substring(0, 2);
            assertEquals(system, Path.getFullPath(drive), "current dir");
        } finally {
            System.setProperty("user.dir", curdir);
        }
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void windowsSystem32_77007() {
        // check for Unix platforms - see FAQ for more details
        // http://www.mono-project.com/FAQ:_Technical#How_to_detect_the_execution_platform_.3F

        String curdir = System.getProperty("user.dir");
        try {
            String system = System.getProperty("SYSTEMROOT");
            System.setProperty("user.dir", system);
            // e.g. C:dir (no backslash) will return CurrentDirectory + dir
            String dir = system.substring(0, 2) + "dir";
            assertEquals(Path.combine(system, "dir"), Path.getFullPath(dir), "current dir");
        } finally {
            System.setProperty("user.dir", curdir);
        }
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void windowsDriveC14N_77058() {
        // check for Unix platforms - see FAQ for more details
        // http://www.mono-project.com/FAQ:_Technical#How_to_detect_the_execution_platform_.3F

        assertEquals("C:\\Windows\\dir", Path.getFullPath("C:\\Windows\\System32\\..\\dir"), "1");
        assertEquals("C:\\dir", Path.getFullPath("C:\\Windows\\System32\\..\\..\\dir"), "2");
        assertEquals("C:\\dir", Path.getFullPath("C:\\Windows\\System32\\..\\..\\..\\dir"), "3");
        assertEquals("C:\\dir", Path.getFullPath("C:\\Windows\\System32\\..\\..\\..\\..\\dir"), "4");
        assertEquals("C:\\dir\\", Path.getFullPath("C:\\Windows\\System32\\..\\.\\..\\.\\..\\dir\\"), "5");
    }

    @Test
    public void InvalidPathChars_Values() {
        char[] invalid = Path.InvalidPathChars;
        if (isWindows()) {
            if (System.getProperty("os.name").startsWith("Windows")) {
                assertEquals(36, invalid.length, "Length");
            } else {
                assertEquals(1, invalid.length, "Length");
            }

            for (char c : invalid) {
                int i = c;

                if (i < 32)
                    continue;

                // : both 1.1 SP1 and 2.0
                if ((i == 34) || (i == 60) || (i == 62) || (i == 124))
                    continue;
                fail(String.format("'%x' (#%d) is invalid", c, i));
            }
        } else {
            for (char c : invalid) {
                int i = c;
                if (i == 0)
                    continue;
                fail(String.format("'%c' (#%d) is invalid", c, i));
            }
        }
    }

    @Test
    public void invalidPathChars_Modify() {
        char[] expected = Path.InvalidPathChars;
        char[] invalid = Path.InvalidPathChars;
        char original = invalid[0];
        try {
            invalid[0] = 'a';
            // kind of scary
            assertTrue(expected[0] == 'a', "expected");
            assertEquals(expected[0], Path.InvalidPathChars[0], "readonly");
        } finally {
            invalid[0] = original;
        }
    }

    @Test
    public void getInvalidFileNameChars_Values() {
        char[] invalid = Path.getInvalidFileNameChars();
        if (isWindows()) {
            assertEquals(41, invalid.length);
            for (char c : invalid) {
                int i = c;
                if (i < 32)
                    continue;
                if ((i == 34) || (i == 60) || (i == 62) || (i == 124))
                    continue;
                // ':', '*', '?', '\\', '/'
                if ((i == 58) || (i == 42) || (i == 63) || (i == 92) || (i == 47))
                    continue;
                fail(String.format("'%c' (#%d) is invalid", c, i));
            }
        } else {
            for (char c : invalid) {
                int i = c;
                // null or '/'
                if ((i == 0) || (i == 47))
                    continue;
                fail(String.format("'%c' (#%d) is invalid", c, i));
            }
        }
    }

    @Test
    public void getInvalidFileNameChars_Modify() {
        char[] expected = Path.getInvalidFileNameChars();
        char[] invalid = Path.getInvalidFileNameChars();
        invalid[0] = 'a';
        assertTrue(expected[0] != 'a', "expected");
        assertEquals(expected[0], Path.getInvalidFileNameChars()[0], "readonly");
    }

    @Test
    public void getInvalidPathChars_Values() {
        char[] invalid = Path.getInvalidPathChars();
        if (isWindows()) {
            if (System.getProperty("os.name").startsWith("Windows")) {
                assertEquals(36, invalid.length);
            } else {
                assertEquals(1, invalid.length);
            }
            for (char c : invalid) {
                int i = c;
                if (i < 32)
                    continue;
                if ((i == 34) || (i == 60) || (i == 62) || (i == 124))
                    continue;
                fail(String.format("'%c' (#%d) is invalid", c, i));
            }
        } else {
            for (char c : invalid) {
                int i = c;
                if (i == 0)
                    continue;
                fail(String.format("'%c' (#%d) is invalid", c, i));
            }
        }
    }

    @Test
    public void getInvalidPathChars_Order() {
        if (isWindows()) {
            char[] invalid = Path.getInvalidPathChars();
            char[] expected1 = new char[] {
                '\u0022', '\u003C', '\u003E', '\u007C', '\u0000', '\u0001', '\u0002', '\u0003', '\u0004', '\u0005', '\u0006',
                '\u0007', '\u0008', '\u0009', '\n', '\u000B', '\u000C', '\r', '\u000E', '\u000F', '\u0010', '\u0011', '\u0012',
                '\u0013', '\u0014', '\u0015', '\u0016', '\u0017', '\u0018', '\u0019', '\u001A', '\u001B', '\u001C', '\u001D',
                '\u001E', '\u001F'
            };
            char[] expected2 = new char[] {
                '\u0000'
            };
            char[] expected;
            if (System.getProperty("os.name").startsWith("Windows")) {
                expected = expected1;
            } else {
                expected = expected2;
            }
            assertEquals(expected.length, invalid.length);
            for (int i = 0; i < expected.length; i++) {
                assertEquals(expected[i], invalid[i], "Character at position " + i);
            }
        }
    }

    @Test
    public void getInvalidPathChars_Modify() {
        char[] expected = Path.getInvalidPathChars();
        char[] invalid = Path.getInvalidPathChars();
        invalid[0] = 'a';
        assertTrue(expected[0] != 'a', "expected");
        assertEquals(expected[0], Path.getInvalidPathChars()[0], "readonly");
    }

    @Test
    public void getRandomFileName() {
        String s = Path.getRandomFileName();
        assertEquals(12, s.length(), "Length");
        char[] invalid = Path.getInvalidFileNameChars();
        for (int i = 0; i < s.length(); i++) {
            if (i == 8)
                assertEquals('.', s.charAt(i), "8");
            else
                assertTrue(Arrays.binarySearch(invalid, s.charAt(i)) < 0, String.valueOf(s.charAt(i)));
        }
    }

    @Test
    public void getRandomFileNameIsAlphaNumerical() {
        String[] names = new String[1000];
        for (int i = 0; i < names.length; i++)
            names[i] = Path.getRandomFileName();

        for (String name : names) {
            assertEquals(12, name.length());
            assertEquals('.', name.charAt(8));

            for (int i = 0; i < 12; i++) {
                if (i == 8)
                    continue;

                char c = name.charAt(i);
                assertTrue(('a' <= c && c <= 'z') || ('0' <= c && c <= '9'));
            }
        }
    }

    String concat(String sep, String... parms) {
        return String.join(sep, parms);
    }

    @Test
    public void combine_3Params() {
        String sep = String.valueOf(Path.DirectorySeparatorChar);

        try {
            Path.combine(null, "two", "three");
            fail("#A1-1");
        } catch (Exception e) {
            // success
        }

        try {
            Path.combine("one", null, "three");
            fail("#A1-2");
        } catch (Exception e) {
            // success
        }

        try {
            Path.combine("one", "two", null);
            fail("#A1-3");
        } catch (Exception e) {
            // success
        }

        assertEquals(concat(sep, "one", "two", "three"), Path.combine("one", "two", "three"), "#A2-1");
        assertEquals(concat(sep, sep + "one", "two", "three"), Path.combine(sep + "one", "two", "three"), "#A2-2");
        assertEquals(concat(sep, sep + "one", "two", "three"), Path.combine(sep + "one" + sep, "two", "three"), "#A2-3");
        assertEquals(concat(sep, sep + "two", "three"), Path.combine(sep + "one" + sep, sep + "two", "three"), "#A2-4");
        assertEquals(concat(sep, sep + "three"), Path.combine(sep + "one" + sep, sep + "two", sep + "three"), "#A2-5");

        assertEquals(concat(sep, sep + "one" + sep, "two", "three"),
                     Path.combine(sep + "one" + sep + sep, "two", "three"),
                     "#A3");

        assertEquals("", Path.combine("", "", ""), "#A4");
    }

    @Test
    public void combine_4Params() {
        String sep = String.valueOf(Path.DirectorySeparatorChar);

        try {
            Path.combine(null, "two", "three", "four");
            fail("#A1-1");
        } catch (Exception e) {
            // success
        }

        try {
            Path.combine("one", null, "three", "four");
            fail("#A1-2");
        } catch (Exception e) {
            // success
        }

        try {
            Path.combine("one", "two", null, "four");
            fail("#A1-3");
        } catch (Exception e) {
            // success
        }

        try {
            Path.combine("one", "two", "three", null);
            fail("#A1-4");
        } catch (Exception e) {
            // success
        }

        assertEquals(concat(sep, "one", "two", "three", "four"), Path.combine("one", "two", "three", "four"), "#A2-1");
        assertEquals(concat(sep, sep + "one", "two", "three", "four"),
                     Path.combine(sep + "one", "two", "three", "four"),
                     "#A2-2");
        assertEquals(concat(sep, sep + "one", "two", "three", "four"),
                     Path.combine(sep + "one" + sep, "two", "three", "four"),
                     "#A2-3");
        assertEquals(concat(sep, sep + "two", "three", "four"),
                     Path.combine(sep + "one" + sep, sep + "two", "three", "four"),
                     "#A2-4");
        assertEquals(concat(sep, sep + "three", "four"),
                     Path.combine(sep + "one" + sep, sep + "two", sep + "three", "four"),
                     "#A2-5");
        assertEquals(concat(sep, sep + "four"),
                     Path.combine(sep + "one" + sep, sep + "two", sep + "three", sep + "four"),
                     "#A2-6");

        assertEquals(concat(sep, sep + "one" + sep, "two", "three", "four"),
                     Path.combine(sep + "one" + sep + sep, "two", "three", "four"),
                     "#A3");

        assertEquals("", Path.combine("", "", "", ""), "#A4");
    }

    @Test
    public void combine_ManyParams() {
        String sep = String.valueOf(Path.DirectorySeparatorChar);

        try {
            Path.combine(null, "two", "three", "four", "five");
            fail("#A1-1");
        } catch (Exception e) {
            // success
        }

        try {
            Path.combine("one", null, "three", "four", "five");
            fail("#A1-2");
        } catch (Exception e) {
            // success
        }

        try {
            Path.combine("one", "two", null, "four", "five");
            fail("#A1-3");
        } catch (Exception e) {
            // success
        }

        try {
            Path.combine("one", "two", "three", null, "five");
            fail("#A1-4");
        } catch (Exception e) {
            // success
        }

        try {
            Path.combine("one", "two", "three", "four", null);
            fail("#A1-5");
        } catch (Exception e) {
            // success
        }

        assertEquals(concat(sep, "one", "two", "three", "four", "five"),
                     Path.combine("one", "two", "three", "four", "five"),
                     "#A2-1");
        assertEquals(concat(sep, sep + "one", "two", "three", "four", "five"),
                     Path.combine(sep + "one", "two", "three", "four", "five"),
                     "#A2-2");
        assertEquals(concat(sep, sep + "one", "two", "three", "four", "five"),
                     Path.combine(sep + "one" + sep, "two", "three", "four", "five"),
                     "#A2-3");
        assertEquals(concat(sep, sep + "two", "three", "four", "five"),
                     Path.combine(sep + "one" + sep, sep + "two", "three", "four", "five"),
                     "#A2-4");
        assertEquals(concat(sep, sep + "three", "four", "five"),
                     Path.combine(sep + "one" + sep, sep + "two", sep + "three", "four", "five"),
                     "#A2-5");
        assertEquals(concat(sep, sep + "four", "five"),
                     Path.combine(sep + "one" + sep, sep + "two", sep + "three", sep + "four", "five"),
                     "#A2-6");
        assertEquals(concat(sep, sep + "five"),
                     Path.combine(sep + "one" + sep, sep + "two", sep + "three", sep + "four", sep + "five"),
                     "#A2-6");

        assertEquals(concat(sep, sep + "one" + sep, "two", "three", "four", "five"),
                     Path.combine(sep + "one" + sep + sep, "two", "three", "four", "five"),
                     "#A3");

        assertEquals("", Path.combine("", "", "", "", ""), "#A4");
    }
}
