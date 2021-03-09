package org.solveme.philosophers;

import lombok.RequiredArgsConstructor;

import java.util.function.ToIntFunction;


@RequiredArgsConstructor
public enum Side {
    LEFT(
            seatId -> seatId + 1,
            seatId -> seatId
    ),
    RIGHT(
            seatId -> seatId - 1,
            seatId -> seatId - 1
    );

    private final ToIntFunction<Integer> neighbourIdResolver;
    private final ToIntFunction<Integer> forkIdResolver;

    public Side opposite() {
        return this == LEFT ? RIGHT : LEFT;
    }

    public int neighbourOf(Identity philosopher) {
        return neighbourOf(philosopher.getSeatId());
    }

    public int neighbourOf(int seatId) {
        return neighbourIdResolver.applyAsInt(seatId);
    }

    public int forkIdOf(int seatId) {
        return forkIdResolver.applyAsInt(seatId);
    }

    public int forkIdOf(Identity philosopher) {
        return forkIdOf(philosopher.getSeatId());
    }

}
