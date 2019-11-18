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
 * AceFlags.
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2019/10/17 nsano initial version <br>
 */
public enum AceFlags {
    ObjectInherit,
    ContainerInherit,
    NoPropagateInherit,
    InheritOnly,
    Inherited,
    _dummy_20,
    SuccessfulAccess,
    FailedAccess;

    public static final EnumSet<AceFlags> InheritanceFlags = EnumSet
            .of(ObjectInherit, ContainerInherit, NoPropagateInherit, InheritOnly);

    public static final EnumSet<AceFlags> AuditFlags = EnumSet.of(SuccessfulAccess, FailedAccess);

    // TODO
    public Supplier<Integer> supplier() {
        return () -> 1 << ordinal();
    }

    // TODO
    public Function<Integer, Boolean> function() {
        return v -> (v & supplier().get()) != 0;
    };

    public static EnumSet<AceFlags> valueOf(int value) {
        return Arrays.stream(values())
                .filter(v -> v.function().apply(value))
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(AceFlags.class)));
    }

    public static long valueOf(EnumSet<AceFlags> flags) {
        return flags.stream().collect(Collectors.summarizingInt(e -> e.supplier().get())).getSum();
    }
}

/* */
