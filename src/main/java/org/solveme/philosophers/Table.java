package org.solveme.philosophers;


import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.util.function.ToIntFunction;

import static org.solveme.philosophers.Table.Side.LEFT;
import static org.solveme.philosophers.Table.Side.RIGHT;

/**
 * Class to handle neighbourhood and seatId/fordId resolving
 */
@RequiredArgsConstructor
public class Table {

    private final int size;

    public int getLeftNeighbour(int seatId) {
        return normalizeSeatId(LEFT.neighbourOf(seatId));
    }

    public int getRightNeighbour(int seatId) {
        return normalizeSeatId(RIGHT.neighbourOf(seatId));
    }

    public int leftForkIdFor(int seatId) {
        return normalizeSeatId(LEFT.forkIdOf(seatId));
    }

    public int rightForkIdFor(int seatId) {
        return normalizeSeatId(RIGHT.forkIdOf(seatId));
    }

    @Nonnull
    public Side sideOfNearFork(int forkId, Identity philosopher) {
        if (normalizeSeatId(LEFT.forkIdOf(philosopher)) == forkId) return LEFT;
        if (normalizeSeatId(RIGHT.forkIdOf(philosopher)) == forkId) return RIGHT;
        throw new RuntimeException(philosopher + " attempts to get side of non-near fork #" + forkId);
    }

    public int normalizeSeatId(int seatId) {
        return normalizeSeatId(seatId, size);
    }

    public int validateSeatId(int seatId) {
        if (seatId < -1 || seatId > size) {
            throw new IllegalArgumentException("Illegal seatId " + seatId);
        }

        return seatId;
    }

    public int normalizeSeatId(int seatId, int total) {
        int normalizedSeatId = validateSeatId(seatId) % total;

        return normalizedSeatId < 0
                ? total + normalizedSeatId
                : normalizedSeatId;
    }


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

}
