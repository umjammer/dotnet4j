
package dotnet4j.security.accessControl;

import java.util.ArrayList;
import java.util.List;

import dotnet4j.io.compat.Utilities;


/**
 * System.Security.AccessControl.SddlAccessRight.cs
 *
 * @author Kenneth Bell
 */
class SddlAccessRight {
    public String _name;

    public int _value;

    public int _objectType;

    public static SddlAccessRight lookupByName(String s) {
        for (SddlAccessRight right : rights) {
            if (Utilities.equals(right._name, s))
                return right;
        }

        return null;
    }

    public static SddlAccessRight[] decompose(int mask) {
        for (SddlAccessRight right : rights) {
            if (mask == right._value)
                return new SddlAccessRight[] {
                    right
                };
        }

        int foundType = 0;
        List<SddlAccessRight> found = new ArrayList<>();
        int accountedBits = 0;
        for (SddlAccessRight right : rights) {
            if ((mask & right._value) == right._value && (accountedBits | right._value) != accountedBits) {

                if (foundType == 0)
                    foundType = right._objectType;

                if (right._objectType != 0 && foundType != right._objectType)
                    return null;

                found.add(right);
                accountedBits |= right._value;
            }

            if (accountedBits == mask) {
                return found.toArray(new SddlAccessRight[0]);
            }
        }

        return null;
    }

    private static final SddlAccessRight[] rights = new SddlAccessRight[] {
        // @formatter:off
        new SddlAccessRight() {{ _name = "CC"; _value = 0x00000001; _objectType = 1;}},
        new SddlAccessRight() {{ _name = "DC"; _value = 0x00000002; _objectType = 1;}},
        new SddlAccessRight() {{ _name = "LC"; _value = 0x00000004; _objectType = 1;}},
        new SddlAccessRight() {{ _name = "SW"; _value = 0x00000008; _objectType = 1;}},
        new SddlAccessRight() {{ _name = "RP"; _value = 0x00000010; _objectType = 1;}},
        new SddlAccessRight() {{ _name = "WP"; _value = 0x00000020; _objectType = 1;}},
        new SddlAccessRight() {{ _name = "DT"; _value = 0x00000040; _objectType = 1;}},
        new SddlAccessRight() {{ _name = "LO"; _value = 0x00000080; _objectType = 1;}},
        new SddlAccessRight() {{ _name = "CR"; _value = 0x00000100; _objectType = 1;}},

        new SddlAccessRight() {{ _name = "SD"; _value = 0x00010000;}},
        new SddlAccessRight() {{ _name = "RC"; _value = 0x00020000;}},
        new SddlAccessRight() {{ _name = "WD"; _value = 0x00040000;}},
        new SddlAccessRight() {{ _name = "WO"; _value = 0x00080000;}},

        new SddlAccessRight() {{ _name = "GA"; _value = 0x10000000;}},
        new SddlAccessRight() {{ _name = "GX"; _value = 0x20000000;}},
        new SddlAccessRight() {{ _name = "GW"; _value = 0x40000000;}},
        new SddlAccessRight() {{ _name = "GR"; _value = 0x80000000;}},

        new SddlAccessRight() {{ _name = "FA"; _value = 0x001F01FF; _objectType = 2;}},
        new SddlAccessRight() {{ _name = "FR"; _value = 0x00120089; _objectType = 2;}},
        new SddlAccessRight() {{ _name = "FW"; _value = 0x00120116; _objectType = 2;}},
        new SddlAccessRight() {{ _name = "FX"; _value = 0x001200A0; _objectType = 2;}},

        new SddlAccessRight() {{ _name = "KA"; _value = 0x000F003F; _objectType = 3;}},
        new SddlAccessRight() {{ _name = "KR"; _value = 0x00020019; _objectType = 3;}},
        new SddlAccessRight() {{ _name = "KW"; _value = 0x00020006; _objectType = 3;}},
        new SddlAccessRight() {{ _name = "KX"; _value = 0x00020019; _objectType = 3;}},

        new SddlAccessRight() {{ _name = "NW"; _value = 0x00000001;}},
        new SddlAccessRight() {{ _name = "NR"; _value = 0x00000002;}},
        new SddlAccessRight() {{ _name = "NX"; _value = 0x00000004;}},
        // @formatter:on
    };
}
