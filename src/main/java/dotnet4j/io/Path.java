//
// Copyright (C) 2004-2005 Novell, Inc (http://www.novell.com)
//
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions:
// 
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//

package dotnet4j.io;

import java.util.Arrays;
import java.util.Random;

import vavi.util.StringUtil;

import dotnet4j.io.compat.StringUtilities;


/**
 * System.IO.Path.cs
 *
 * Copyright (C) 2001 Moonlight Enterprises, All Rights Reserved Copyright (C)
 * 2002 Ximian, Inc. (http://www.ximian.com) Copyright (C) 2003 Ben Maurer
 * Copyright 2011 Xamarin Inc (http://www.xamarin.com).
 *
 * @author Jim Richardson, develop@wtfo-guru.com
 * @author Dan Lewis (dihlewis@yahoo.co.uk)
 * @author Gonzalo Paniagua Javier (gonzalo@ximian.com)
 * @author Ben Maurer (bmaurer@users.sourceforge.net)
 * @author Sebastien Pouliot <sebastien@ximian.com>
 * @version Saturday, August 11, 2001
 *
 */
public class Path {

    /** see getInvalidPathChars and getInvalidFileNameChars methods. */
    @Deprecated
    public static final char[] InvalidPathChars;

    public static final char AltDirectorySeparatorChar;

    public static final char DirectorySeparatorChar;

    public static final char PathSeparator;

    static final String DirectorySeparatorStr;

    public static final char VolumeSeparatorChar;

    static final char[] PathSeparatorChars;

    private static final boolean dirEqualsVolume;

    // class methods
    public static String changeExtension(String path, String extension) {
        if (path == null)
            return null;

        if (StringUtilities.indexOfAny(path, InvalidPathChars) != -1)
            throw new IllegalArgumentException("Illegal characters in path.");

        int iExt = findExtension(path);

        if (extension == null)
            return iExt < 0 ? path : path.substring(0, iExt);
        else if (extension.length() == 0)
            return iExt < 0 ? path + '.' : path.substring(0, iExt + 1);

        else if (path.length() != 0) {
            if (extension.length() > 0 && extension.charAt(0) != '.')
                extension = "." + extension;
        } else
            extension = "";

        if (iExt < 0) {
            return path + extension;
        } else if (iExt > 0) {
            String temp = path.substring(0, iExt);
            return temp + extension;
        }

        return extension;
    }

    public static String combine(String path1, String path2) {
        if (path1 == null)
            throw new NullPointerException("path1");

        if (path2 == null)
            throw new NullPointerException("path2");

        if (path1.length() == 0)
            return path2;

        if (path2.length() == 0)
            return path1;

        if (StringUtilities.indexOfAny(path1, InvalidPathChars) != -1)
            throw new IllegalArgumentException("Illegal characters in path.");

        if (StringUtilities.indexOfAny(path2, InvalidPathChars) != -1)
            throw new IllegalArgumentException("Illegal characters in path.");

        // TODO???: UNC names
        if (isPathRooted(path2))
            return path2;

        char p1end = path1.charAt(path1.length() - 1);
        if (p1end != DirectorySeparatorChar && p1end != AltDirectorySeparatorChar && p1end != VolumeSeparatorChar)
            return path1 + DirectorySeparatorStr + path2;

        return path1 + path2;
    }

    /**
     * This routine:
     * <li> Removes duplicat path separators from a string
     * <li> If the String starts with \\, preserves the first two (hostname on
     * Windows)
     * <li> Removes the trailing path separator.
     * <li> Returns the DirectorySeparatorChar for the single input
     * DirectorySeparatorChar or AltDirectorySeparatorChar
     *
     * Unlike CanonicalizePath, this does not do any path resolution
     * (which GetDirectoryName is not supposed to do).
     */
    static String cleanPath(String s) {
        int l = s.length();
        int sub = 0;
        int alt = 0;
        int start = 0;

        // Host prefix?
        char s0 = s.charAt(0);
        if (l > 2 && s0 == '\\' && s.charAt(1) == '\\') {
            start = 2;
        }

        // We are only left with root
        if (l == 1 && (s0 == DirectorySeparatorChar || s0 == AltDirectorySeparatorChar))
            return s;

        // Cleanup
        for (int i = start; i < l; i++) {
            char c = s.charAt(i);

            if (c != DirectorySeparatorChar && c != AltDirectorySeparatorChar)
                continue;
            if (DirectorySeparatorChar != AltDirectorySeparatorChar && c == AltDirectorySeparatorChar)
                alt++;
            if (i + 1 == l)
                sub++;
            else {
                c = s.charAt(i + 1);
                if (c == DirectorySeparatorChar || c == AltDirectorySeparatorChar)
                    sub++;
            }
        }

        if (sub == 0 && alt == 0)
            return s;

        char[] copy = new char[l - sub];
        if (start != 0) {
            copy[0] = '\\';
            copy[1] = '\\';
        }
        for (int i = start, j = start; i < l && j < copy.length; i++) {
            char c = s.charAt(i);

            if (c != DirectorySeparatorChar && c != AltDirectorySeparatorChar) {
                copy[j++] = c;
                continue;
            }

            // For non-trailing cases.
            if (j + 1 != copy.length) {
                copy[j++] = DirectorySeparatorChar;
                for (; i < l - 1; i++) {
                    c = s.charAt(i + 1);
                    if (c != DirectorySeparatorChar && c != AltDirectorySeparatorChar)
                        break;
                }
            }
        }
        return new String(copy);
    }

    public static String getDirectoryName(String path) {
        // LAMESPEC: For empty String MS docs say both
        // return null AND throw exception. Seems .NET throws.
        if (path == "")
            throw new IllegalArgumentException("Invalid path");

        if (path == null || getPathRoot(path) == path)
            return null;

        if (path.trim().length() == 0)
            throw new IllegalArgumentException("Argument String consists of whitespace characters only.");

        if (StringUtilities.indexOfAny(path, InvalidPathChars) > -1)
            throw new IllegalArgumentException("Path contains invalid characters");

        int nLast = StringUtilities.lastIndexOfAny(path, PathSeparatorChars);
        if (nLast == 0)
            nLast++;

        if (nLast > 0) {
            String ret = path.substring(0, nLast);
            int l = ret.length();

            if (l >= 2 && DirectorySeparatorChar == '\\' && ret.charAt(l - 1) == VolumeSeparatorChar)
                return ret + DirectorySeparatorChar;
            else if (l == 1 && DirectorySeparatorChar == '\\' && path.length() >= 2 &&
                     path.charAt(nLast) == VolumeSeparatorChar)
                return ret + VolumeSeparatorChar;
            else {
                //
                // Important: do not use CanonicalizePath here, use
                // the custom CleanPath here, as this should not
                // return absolute paths
                //
                return cleanPath(ret);
            }
        }

        return "";
    }

    public static CharSequence getDirectoryName(CharSequence path) {
        return Path.getDirectoryName(path.toString());
    }

    public static String getExtension(String path) {
        if (path == null)
            return null;

        if (StringUtilities.indexOfAny(path, InvalidPathChars) != -1)
            throw new IllegalArgumentException("Illegal characters in path.");

        int iExt = findExtension(path);

        if (iExt > -1) {
            if (iExt < path.length() - 1)
                return path.substring(iExt);
        }
        return "";
    }

    public static String getFileName(String path) {
        if (path == null || path.length() == 0)
            return path;

        if (StringUtilities.indexOfAny(path, InvalidPathChars) != -1)
            throw new IllegalArgumentException("Illegal characters in path.");

        int nLast = StringUtilities.lastIndexOfAny(path, PathSeparatorChars);
        if (nLast >= 0)
            return path.substring(nLast + 1);

        return path;
    }

    public static String getFileNameWithoutExtension(String path) {
        return changeExtension(getFileName(path), null);
    }

    public static String getFullPath(String path) {
        String fullpath = insecureGetFullPath(path);

        return fullpath;
    }

    static String getFullPathInternal(String path) {
        return insecureGetFullPath(path);
    }

    // insecure - do not call directly
    static String insecureGetFullPath(String path) {
        if (path == null)
            throw new NullPointerException("path");

        if (path.trim().length() == 0) {
            String msg = "The specified path is not of a legal form (empty).";
            throw new IllegalArgumentException(msg);
        }
        // if the supplied path ends with a separator...
        char end = path.charAt(path.length() - 1);

        boolean canonicalize = true;
        if (path.length() >= 2 && isDirectorySeparator(path.charAt(0)) && isDirectorySeparator(path.charAt(1))) {
            if (path.length() == 2 || path.indexOf(path.charAt(0), 2) < 0)
                throw new IllegalArgumentException("UNC paths should be of the form \\\\server\\share.");

            if (path.charAt(0) != DirectorySeparatorChar)
                path = path.replace(AltDirectorySeparatorChar, DirectorySeparatorChar);

        } else {
            if (!isPathRooted(path)) {

                String cwd = System.getProperty("user.dir").replace(java.io.File.separatorChar, DirectorySeparatorChar);
                if (cwd.charAt(cwd.length() - 1) == DirectorySeparatorChar)
                    path = cwd + path;
                else
                    path = cwd + DirectorySeparatorChar + path;
            } else if (DirectorySeparatorChar == '\\' && path.length() >= 2 && isDirectorySeparator(path.charAt(0)) &&
                       !isDirectorySeparator(path.charAt(1))) { // like `\abc\def'
                String current = System.getProperty("user.dir").replace(java.io.File.separatorChar, DirectorySeparatorChar);
                if (current.charAt(1) == VolumeSeparatorChar)
                    path = current.substring(0, 2) + path;
                else
                    path = current.substring(0, current.indexOf('\\', current.indexOf("\\\\") + 1));
            }
        }

        if (canonicalize)
            path = canonicalizePath(path);

        // if the original ended with a [Alt]DirectorySeparatorChar then ensure
        // the full path also ends with one
        if (isDirectorySeparator(end) && (path.charAt(path.length() - 1) != DirectorySeparatorChar))
            path += DirectorySeparatorChar;

        return path;
    }

    static boolean isDirectorySeparator(char c) {
        return c == DirectorySeparatorChar || c == AltDirectorySeparatorChar;
    }

    public static String getPathRoot(String path) {
        if (path == null)
            return null;

        if (path.length() == 0)
            throw new IllegalArgumentException("The specified path is not of a legal form.");

        if (!isPathRooted(path))
            return "";

        if (DirectorySeparatorChar == '/') {
            // UNIX
            return isDirectorySeparator(path.charAt(0)) ? DirectorySeparatorStr : "";
        } else {
            // Windows
            int len = 2;

            if (path.length() == 1 && isDirectorySeparator(path.charAt(0)))
                return DirectorySeparatorStr;
            else if (path.length() < 2)
                return "";

            if (isDirectorySeparator(path.charAt(0)) && isDirectorySeparator(path.charAt(1))) {
                // UNC: \\server or \\server\share
                // Get server
                while (len < path.length() && !isDirectorySeparator(path.charAt(len)))
                    len++;

                // Get share
                if (len < path.length()) {
                    len++;
                    while (len < path.length() && !isDirectorySeparator(path.charAt(len)))
                        len++;
                }

                return DirectorySeparatorStr + DirectorySeparatorStr +
                       path.substring(2, len).replace(AltDirectorySeparatorChar, DirectorySeparatorChar);
            } else if (isDirectorySeparator(path.charAt(0))) {
                // path starts with '\' or '/'
                return DirectorySeparatorStr;
            } else if (path.charAt(1) == VolumeSeparatorChar) {
                // C:\folder
                if (path.length() >= 3 && (isDirectorySeparator(path.charAt(2))))
                    len++;
            } else
                return System.getenv("user.dir").replace(java.io.File.separatorChar, DirectorySeparatorChar).substring(0, 2);
                // + path.substring(0, len);
            return path.substring(0, len);
        }
    }

    public static String getTempFileName() {
        FileStream f = null;
        String path;
        Random rnd;
        int num;
        int count = 0;

        rnd = new Random();
        String tmp_path = getTempPath();
        do {
            num = rnd.nextInt();
            num++;
            path = Path.combine(tmp_path, "tmp" + String.format("%x", num) + ".tmp");

            try {
                f = new FileStream(path,
                                   FileMode.CreateNew,
                                   FileAccess.ReadWrite,
                                   FileShare.Read,
                                   8192,
                                   false,
                                   FileOptions.values()[1]);
            } catch (IOException ex) {
                if (count++ > 65536)
                    throw ex;
            }
        } while (f == null);

        f.close();
        return path;
    }

    public static String getTempPath() {
        String p = System.getenv("TMPDIR");
        if (p.length() > 0 && p.charAt(p.length() - 1) != DirectorySeparatorChar)
            return p + DirectorySeparatorChar;

        return p;
    }

    public static boolean hasExtension(String path) {
        if (path == null || path.trim().length() == 0)
            return false;

        if (StringUtilities.indexOfAny(path, InvalidPathChars) != -1)
            throw new IllegalArgumentException("Illegal characters in path.");

        int pos = findExtension(path);
        return 0 <= pos && pos < path.length() - 1;
    }

    public static boolean isPathRooted(CharSequence path) {
        if (path.length() == 0)
            return false;

        char c = path.charAt(0);
        return (c == DirectorySeparatorChar || c == AltDirectorySeparatorChar ||
                (!dirEqualsVolume && path.length() > 1 && path.charAt(1) == VolumeSeparatorChar));
    }

    public static boolean isPathRooted(String path) {
        if (path == null || path.length() == 0)
            return false;

        if (StringUtilities.indexOfAny(path, InvalidPathChars) != -1)
            throw new IllegalArgumentException("Illegal characters in path.");
        return isPathRooted((CharSequence) path);
    }

    public static char[] getInvalidFileNameChars() {
        // return a new array as we do not want anyone to be able to change the
        // values
        if (System.getProperty("os.name").startsWith("Windows")) {
            return new char[] {
                '\u0000', '\u0001', '\u0002', '\u0003', '\u0004', '\u0005', '\u0006', '\u0007', '\u0008', '\u0009', '\n',
                '\u000B', '\u000C', '\r', '\u000E', '\u000F', '\u0010', '\u0011', '\u0012', '\u0013', '\u0014', '\u0015',
                '\u0016', '\u0017', '\u0018', '\u0019', '\u001A', '\u001B', '\u001C', '\u001D', '\u001E', '\u001F', '\u0022',
                '\u003C', '\u003E', '\u007C', ':', '*', '?', '\\', '/'
            };
        } else {
            return new char[] {
                '\u0000', '/'
            };
        }
    }

    public static char[] getInvalidPathChars() {
        // return a new array as we do not want anyone to be able to change the
        // values
        if (System.getProperty("os.name").startsWith("Windows")) {
            return new char[] {
                '\u0022', '\u003C', '\u003E', '\u007C', '\u0000', '\u0001', '\u0002', '\u0003', '\u0004', '\u0005', '\u0006',
                '\u0007', '\u0008', '\u0009', '\n', '\u000B', '\u000C', '\r', '\u000E', '\u000F', '\u0010', '\u0011', '\u0012',
                '\u0013', '\u0014', '\u0015', '\u0016', '\u0017', '\u0018', '\u0019', '\u001A', '\u001B', '\u001C', '\u001D',
                '\u001E', '\u001F'
            };
        } else {
            return new char[] {
                '\u0000'
            };
        }
    }

    public static String getRandomFileName() {
        // returns a 8.3 filename (total size 12)
        StringBuilder sb = new StringBuilder(12);
        // using strong crypto but without creating the file
        byte[] buffer = StringUtil.getRandomString().substring(0, 11).getBytes();

        for (int i = 0; i < buffer.length; i++) {
            if (sb.length() == 8)
                sb.append('.');

            // restrict to length of range [a..z0..9]
            int b = (buffer[i] % 36);
            char c = (char) (b < 26 ? (b + 'a') : (b - 26 + '0'));
            sb.append(c);
        }

        return sb.toString();
    }

    // private class methods

    private static int findExtension(String path) {
        // method should return the index of the path extension
        // start or -1 if no valid extension
        if (path != null) {
            int iLastDot = path.lastIndexOf('.');
            int iLastSep = StringUtilities.lastIndexOfAny(path, PathSeparatorChars);

            if (iLastDot > iLastSep)
                return iLastDot;
        }
        return -1;
    }

    static {
        VolumeSeparatorChar = ':';
        DirectorySeparatorChar = '\\';
        AltDirectorySeparatorChar = '¥';

        PathSeparator = java.io.File.pathSeparatorChar;
        // this copy will be modifiable ("by design")
        InvalidPathChars = getInvalidPathChars();
        // fields

        DirectorySeparatorStr = String.valueOf(DirectorySeparatorChar);
        PathSeparatorChars = new char[] {
            DirectorySeparatorChar, AltDirectorySeparatorChar, VolumeSeparatorChar
        };

        dirEqualsVolume = (DirectorySeparatorChar == VolumeSeparatorChar);
    }

    // returns the server and share part of a UNC. Assumes "path" is a UNC.
    static String getServerAndShare(String path) {
        int len = 2;
        while (len < path.length() && !isDirectorySeparator(path.charAt(len)))
            len++;

        if (len < path.length()) {
            len++;
            while (len < path.length() && !isDirectorySeparator(path.charAt(len)))
                len++;
        }

        return path.substring(2, len).replace(AltDirectorySeparatorChar, DirectorySeparatorChar);
    }

    // assumes Environment.IsRunningOnWindows == true
    static boolean sameRoot(String root, String path) {
        // compare root - if enough details are available
        if ((root.length() < 2) || (path.length() < 2))
            return false;

        // UNC handling
        if (isDirectorySeparator(root.charAt(0)) && isDirectorySeparator(root.charAt(1))) {
            if (!(isDirectorySeparator(path.charAt(0)) && isDirectorySeparator(path.charAt(1))))
                return false;

            String rootShare = getServerAndShare(root);
            String pathShare = getServerAndShare(path);

            return StringUtilities.compare(rootShare, pathShare, true) == 0;
        }

        // same volume/drive
        if (root.charAt(0) != path.charAt(0))
            return false;
        // presence of the separator
        if (path.charAt(1) != Path.VolumeSeparatorChar)
            return false;
        if ((root.length() > 2) && (path.length() > 2)) {
            // but don't directory compare the directory separator
            return (isDirectorySeparator(root.charAt(2)) && isDirectorySeparator(path.charAt(2)));
        }
        return true;
    }

    static String canonicalizePath(String path) {
        // STEP 1: Check for empty string
        if (path == null)
            return path;

        if (path.length() == 0)
            return path;

        // STEP 2: Check to see if this is only a root
        String root = Path.getPathRoot(path);
        // it will return '\' for path '\', while it should return 'c:\' or so.
        // Note: commenting this out makes the need for the (target == 1...)
        // check in step 5
        // if (root == path) return path;

        // STEP 3: split the directories, this gets rid of consecutative "/"'s
        String[] dirs = path.split(StringUtilities
                .escapeForRegex(String.valueOf(Path.DirectorySeparatorChar) + Path.AltDirectorySeparatorChar));
        // STEP 4: Get rid of directories containing . and ..
        int target = 0;

        // Set an overwrite limit for UNC paths since '\' + server + share
        // must not be eliminated by the '..' elimination algorithm.
        int limit = 0;

        for (int i = 0; i < dirs.length; i++) {

            if ((i != 0 && dirs[i].length() == 0))
                continue;
            else if (dirs[i] == "..") {
                // don't overwrite path segments below the limit
                if (target > limit)
                    target--;
            } else
                dirs[target++] = dirs[i];
        }

        // STEP 5: Combine everything.
        if (target == 0 || (target == 1 && dirs[0] == ""))
            return root;
        else {
            String ret = String.join(DirectorySeparatorStr, Arrays.copyOfRange(dirs, 0, target));
            if (root != "" && ret.length() > 0 && ret.charAt(0) != '/')
                ret = root + ret;
            return ret;
        }
    }

    // required for FileIOPermission (and most proibably reusable elsewhere too)
    // both path MUST be "full paths"
    static boolean isPathSubsetOf(String subset, String path) {
        if (subset.length() > path.length())
            return false;

        // check that everything up to the last separator match
        int slast = StringUtilities.lastIndexOfAny(subset, PathSeparatorChars);
        if (StringUtilities.compare(subset, 0, path, 0, slast) != 0)
            return false;

        slast++;
        // then check if the last segment is identical
        int plast = StringUtilities.indexOfAny(path, PathSeparatorChars, slast);
        if (plast >= slast) {
            return StringUtilities.compare(subset, slast, path, slast, path.length() - plast) == 0;
        }
        if (subset.length() != path.length())
            return false;

        return StringUtilities.compare(subset, slast, path, slast, subset.length() - slast) == 0;
    }

    public static String combine(String... paths) {
        if (paths == null)
            throw new NullPointerException("paths");

        boolean need_sep;
        StringBuilder ret = new StringBuilder();
        int pathsLen = paths.length;
        int slen;
        need_sep = false;

        for (String s : paths) {
            if (s == null)
                throw new NullPointerException("One of the paths contains a null value: paths");
            if (s.length() == 0)
                continue;
            if (StringUtilities.indexOfAny(s, InvalidPathChars) != -1)
                throw new IllegalArgumentException("Illegal characters in path.");

            if (need_sep) {
                need_sep = false;
                ret.append(DirectorySeparatorStr);
            }

            pathsLen--;
            if (isPathRooted(s))
                ret.setLength(0);

            ret.append(s);
            slen = s.length();
            if (slen > 0 && pathsLen > 0) {
                char p1end = s.charAt(slen - 1);
                if (p1end != DirectorySeparatorChar && p1end != AltDirectorySeparatorChar && p1end != VolumeSeparatorChar)
                    need_sep = true;
            }
        }

        return ret.toString();
    }

    public static String combine(String path1, String path2, String path3) {
        if (path1 == null)
            throw new NullPointerException("path1");

        if (path2 == null)
            throw new NullPointerException("path2");

        if (path3 == null)
            throw new NullPointerException("path3");

        return combine(new String[] {
            path1, path2, path3
        });
    }

    public static String combine(String path1, String path2, String path3, String path4) {
        if (path1 == null)
            throw new NullPointerException("path1");

        if (path2 == null)
            throw new NullPointerException("path2");

        if (path3 == null)
            throw new NullPointerException("path3");

        if (path4 == null)
            throw new NullPointerException("path4");

        return combine(new String[] {
            path1, path2, path3, path4
        });
    }

    static void validate(String path) {
        validate(path, "path");
    }

    static void validate(String path, String parameterName) {
        if (path == null)
            throw new NullPointerException(parameterName);
        if (StringUtilities.isNullOrWhiteSpace(path))
            throw new IllegalArgumentException("Path is empty");
        if (StringUtilities.indexOfAny(path, Path.InvalidPathChars) != -1)
            throw new IllegalArgumentException("Path contains invalid chars");
    }

    static String getDirectorySeparatorCharAsString() {
        return DirectorySeparatorStr;
    }

    final int MAX_PATH = 260; // From WinDef.h

    // this was copied from corefx since it's not available in referencesource
    static final char[] trimEndCharsWindows = {
        (char) 0x9, (char) 0xA, (char) 0xB, (char) 0xC, (char) 0xD, (char) 0x20, (char) 0x85, (char) 0xA0
    };

    static final char[] trimEndCharsUnix = {};

//    static Function TrimEndChars = Path::trimEndCharsUnix;

    // ".." can only be used if it is specified as a part of a valid
    // File/Directory name. We disallow
    // the user being able to use it to move up directories. Here are some
    // examples eg
    // Valid: a..b abc..d
    // Invalid: ..ab ab.. .. abc..d\abc..
    //
    static void checkSearchPattern(String searchPattern) {
        int index;
        while ((index = searchPattern.indexOf("..")) != -1) {

            if (index + 2 == searchPattern.length()) // Terminal ".." . Files names cannot end in ".."
                throw new IllegalArgumentException(System.getenv("Arg_InvalidSearchPattern"));

            if ((searchPattern.charAt(index + 2) == DirectorySeparatorChar) ||
                (searchPattern.charAt(index + 2) == AltDirectorySeparatorChar))
                throw new IllegalArgumentException(System.getenv("Arg_InvalidSearchPattern"));

            searchPattern = searchPattern.substring(index + 2);
        }
    }

    static void checkInvalidPathChars(String path,
                                      boolean checkAdditional /* = false */) {
        if (path == null)
            throw new NullPointerException("path");

        if (PathInternal.hasIllegalCharacters(path, checkAdditional))
            throw new IllegalArgumentException(System.getenv("Argument_InvalidPathChars"));
    }

    static String internalCombine(String path1, String path2) {
        if (path1 == null || path2 == null)
            throw new NullPointerException((path1 == null) ? "path1" : "path2");
        checkInvalidPathChars(path1, false);
        checkInvalidPathChars(path2, false);

        if (path2.length() == 0)
            throw new IllegalArgumentException(System.getenv("Argument_PathEmpty") + ": path2");
        if (isPathRooted(path2))
            throw new IllegalArgumentException(System.getenv("Arg_Path2IsRooted") + ": path2");
        int i = path1.length();
        if (i == 0)
            return path2;
        char ch = path1.charAt(i - 1);
        if (ch != DirectorySeparatorChar && ch != AltDirectorySeparatorChar && ch != VolumeSeparatorChar)
            return path1 + DirectorySeparatorStr + path2;
        return path1 + path2;
    }

    public static CharSequence getFileName(CharSequence path) {
        int root = getPathRoot(path.toString()).length();

        // We don't want to cut off "C:\file.txt:stream" (i.e. should be
        // "file.txt:stream")
        // but we *do* want "C:Foo" => "Foo". This necessitates checking for the
        // root.

        for (int i = path.length(); --i >= 0;) {
            if (i < root || isDirectorySeparator(path.charAt(i)))
                return path.subSequence(i + 1, path.length());
        }

        return path;
    }

    public static String join(CharSequence path1, CharSequence path2) {
        if (path1.length() == 0)
            return path2.toString();
        if (path2.length() == 0)
            return path1.toString();

        return path1.toString() + path2;
    }

    public static String join(CharSequence path1, CharSequence path2, CharSequence path3) {
        if (path1.length() == 0)
            return join(path2, path3);

        if (path2.length() == 0)
            return join(path1, path3);

        if (path3.length() == 0)
            return join(path1, path2);

        return path1.toString() + path2 + path3;
    }
}

class PathInternal {
    public static boolean isPartiallyQualified(String path) {
        return false;
    }

    public static boolean hasIllegalCharacters(String path, boolean checkAdditional) {
        return StringUtilities.indexOfAny(path, Path.InvalidPathChars) != -1;
    }
}
