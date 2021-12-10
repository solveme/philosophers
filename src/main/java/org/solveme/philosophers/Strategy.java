package org.solveme.philosophers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.solveme.philosophers.strategies.Atomic;
import org.solveme.philosophers.strategies.Managed;
import org.solveme.philosophers.strategies.Notify;
import org.solveme.philosophers.strategies.Synchronized;

import java.util.function.Function;


@RequiredArgsConstructor
@Getter
public enum Strategy {
    SYNCHRONIZED(Synchronized::new),
    ATOMIC(Atomic::new),
    NOTIFY(Notify::new),
    MANAGED(Managed::new),

    //
    ;

    private final Function<DinnerApp.Settings, Dinner> initiator;

}
