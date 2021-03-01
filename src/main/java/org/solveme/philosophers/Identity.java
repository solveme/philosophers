package org.solveme.philosophers;

import org.apache.commons.lang3.StringUtils;
import org.solveme.philosophers.util.Util;

import java.util.Comparator;
import java.util.stream.Stream;


public enum Identity {
    ARISTOTLE,
    PLATO,
    SOCRATES,
    DIOGEN,
    DESCARTES,
    KANT,
    HEGEL

    //
    ;

    public static final int MAX_LENGTH = Stream.of(Identity.values())
            .max(Comparator.comparingInt(n -> n.toString().length()))
            .map(n -> n.toString().length())
            .orElse(12);

    public static String padName(Identity name) {
        return StringUtils.leftPad(name.toString(), MAX_LENGTH);
    }

    public static Identity at(int seatId) {
        return Identity.values()[Util.normalizeSeatId(seatId, Identity.values().length)];
    }

    public int getSeatId() {
        return ordinal();
    }

    @Override
    public String toString() {
        String name = name().toLowerCase();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public String padded() {
        return padName(this);
    }

}
