package dotnet4j.security.accessControl;

import java.util.EnumSet;


public enum AccessControlSections {
    /** DACL: Discretionary Access Control List */
    Access(0x2),
    /** SACL: System Access Control List */
    Audit(0x1),
    /** primary group */
    Group(0x8),
    /** no section specified */
    None(0x0),
    /** owner */
    Owner(0x4);

    final int value;

    AccessControlSections(int value) {
        this.value = value;
    }

    /** entire security descriptor */
    public static final EnumSet<AccessControlSections> All = EnumSet.of(Audit, Access, Owner, Group);
}
