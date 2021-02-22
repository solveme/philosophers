package org.solveme.philosophers.strategies;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.solveme.philosophers.Dinner;

import java.util.function.Function;


@RequiredArgsConstructor
@Getter
public enum Strategy {
    CIVILIZED(Civilized::new)

    //
    ;

    private final Function<Dinner.Settings, Dinner> initiator;

}
