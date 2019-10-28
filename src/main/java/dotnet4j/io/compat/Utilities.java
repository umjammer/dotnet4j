//
// Copyright (c) 2008-2011, Kenneth Bell
//
// Permission is hereby granted, free of charge, to any person obtaining a
// copy of this software and associated documentation files (the "Software"),
// to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense,
// and/or sell copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
// DEALINGS IN THE SOFTWARE.
//

package dotnet4j.io.compat;


public class Utilities {

    private Utilities() {
    }

    /**
     * @param obj1 nullable
     * @param obj2 nullable
     */
    public static boolean equals(Object obj1, Object obj2) {
        if (obj1 == null) {
            return obj2 == null;
        } else {
            return obj1.equals(obj2);
        }
    }

    public static int getCombinedHashCode(Object obj0, Object obj1, Object... objs) {
        int result = 0;
        int hash = combineHashCode(toHashCode(obj0), toHashCode(obj1));
        for (int i = 0; i < objs.length; i++) {
            result = combineHashCode(hash, toHashCode(objs[i]));
            hash = result;
        }
        return result;
    }

    private static int toHashCode(Object o) {
        if (Integer.TYPE.isInstance(o)) {
            return Integer.TYPE.cast(o);
        } else if (Integer.class.isInstance(o)) {
            return Integer.class.cast(o);
        } else if (Byte.TYPE.isInstance(o)) {
            return Byte.hashCode(Byte.TYPE.cast(o));
        } else if (Byte.class.isInstance(o)) {
            return Byte.hashCode(Byte.class.cast(o));
        } else if (Character.TYPE.isInstance(o)) {
            return Character.hashCode(Character.TYPE.cast(o));
        } else if (Character.class.isInstance(o)) {
            return Character.hashCode(Character.class.cast(o));
        } else if (Short.TYPE.isInstance(o)) {
            return Short.hashCode(Short.TYPE.cast(o));
        } else if (Short.class.isInstance(o)) {
            return Short.hashCode(Short.class.cast(o));
        } else if (Long.TYPE.isInstance(o)) {
            return Long.hashCode(Long.TYPE.cast(o));
        } else if (Long.class.isInstance(o)) {
            return Long.hashCode(Long.class.cast(o));
        } else {
            return o.hashCode();
        }
    }

    private static int combineHashCode(int a, int b) {
        return 997 * a ^ 991 * b;
    }
}
