package org.solveme.philosophers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.solveme.philosophers.util.TimeRecorder;

import java.time.Duration;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;


@Slf4j
@RequiredArgsConstructor
public abstract class Philosopher<F extends Fork, P extends Philosopher<F, P>> {

    protected final Dinner<F, P> dinner;
    protected final Name name;
    protected final int seatId;
    protected final F leftFork;
    protected final F rightFork;

    @Getter
    private final TimeRecorder timeRecorder = new TimeRecorder();

    public Philosopher(Dinner<F, P> dinner, int seatId) {
        this(dinner, Name.values()[seatId], seatId, dinner.getLeftFork(seatId), dinner.getRightFork(seatId));
    }

    public Name getName() {
        return name;
    }

    public Result run() {
        timeRecorder.recordTotal(this::takeDinner);
        return Result.from(name, timeRecorder);
    }

    public void takeDinner() {
        while (!Thread.currentThread().isInterrupted()) {
            act();
        }

        log.info(name + " finished the dinner");
    }

    public void act() {
        if (acquireForks()) {
            eat();
            releaseForks();

        } else {
            think();
        }
    }

    public abstract boolean acquireForks();

    public void releaseForks() {
        leftFork.release();
        rightFork.release();
    }

    public void eat() {
        timeRecorder.recordEating(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep((long) (Math.random() * 1000));

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.debug(name + " was asked to stop eating");
            }
        });
    }

    public void think() {
        timeRecorder.recordThinking(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep((long) (Math.random() * 1000));

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.debug(name + " was asked to stop thinking");
            }
        });
    }

    public F getLeftFork() {
        return leftFork;
    }

    public F getRightFork() {
        return rightFork;
    }

    public P getLeftNeighbour() {
        return dinner.getLeftNeighbour(seatId);
    }

    public P getRightNeighbour() {
        return dinner.getRightNeighbour(seatId);
    }


    public enum Name {
        DESCARTES,
        ARISTOTLE,
        PLATO,
        KANT,
        SOCRATES

        //
        ;

        public static final int MAX_LENGTH = Stream.of(Name.values())
                .max(Comparator.comparingInt(n -> n.toString().length()))
                .map(n -> n.toString().length())
                .orElse(12);

        public static String padName(Name name) {
            return StringUtils.leftPad(name.toString(), MAX_LENGTH);
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

    @RequiredArgsConstructor
    @Getter
    public static class Result {

        private final Name name;
        private final Duration totalDuration;
        private final Duration eatingDuration;
        private final Duration thinkingDuration;
        private final Duration wastedDuration;

        public static Result from(Name name, TimeRecorder timeRecorder) {
            return new Result(
                    name,
                    Duration.ofMillis(timeRecorder.getTotalSpent()),
                    Duration.ofMillis(timeRecorder.getEatingSpent()),
                    Duration.ofMillis(timeRecorder.getThinkingSpent()),
                    Duration.ofMillis(timeRecorder.getIdleSpent())
            );
        }

    }

}
