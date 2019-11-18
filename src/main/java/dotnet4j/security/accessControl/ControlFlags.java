/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package dotnet4j.security.accessControl;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * ControlFlags.
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2019/10/17 nsano initial version <br>
 */
public enum ControlFlags {
//    None(0x0000),
    OwnerDefaulted,
    GroupDefaulted,
    DiscretionaryAclPresent,
    DiscretionaryAclDefaulted,
    SystemAclPresent,
    SystemAclDefaulted,
    DiscretionaryAclUntrusted,
    ServerSecurity,
    DiscretionaryAclAutoInheritRequired,
    SystemAclAutoInheritRequired,
    DiscretionaryAclAutoInherited,
    SystemAclAutoInherited,
    DiscretionaryAclProtected,
    SystemAclProtected,
    RMControlValid,
    SelfRelative;

    // TODO
    public Supplier<Integer> supplier() {
        return () -> 1 << ordinal();
    }

    // TODO
    public Function<Integer, Boolean> function() {
        return v -> (v & supplier().get()) != 0;
    };


    public static EnumSet<ControlFlags> valueOf(int value) {
        return Arrays.stream(values())
                .filter(v -> v.function().apply(value))
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(ControlFlags.class)));
    }

    public static long valueOf(EnumSet<ControlFlags> flags) {
        return flags.stream().collect(Collectors.summarizingInt(e -> e.supplier().get())).getSum();
    }
}

/* */
