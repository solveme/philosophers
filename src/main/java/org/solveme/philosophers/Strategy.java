package org.solveme.philosophers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.solveme.philosophers.strategies.Atomic;
import org.solveme.philosophers.strategies.Synchronized;

import java.util.function.Function;


@RequiredArgsConstructor
@Getter
public enum Strategy {
    SYNCHRONIZED(Synchronized::new),
    ATOMIC(Atomic::new),

    //
    ;

    private final Function<DinnerApp.Settings, Dinner> initiator;

}
