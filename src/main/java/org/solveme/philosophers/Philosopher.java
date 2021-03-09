package org.solveme.philosophers;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solveme.philosophers.recorders.PhilosopherTimeRecorder;

import java.time.Duration;
import java.util.concurrent.TimeUnit;


@Slf4j
@RequiredArgsConstructor
public abstract class Philosopher<F extends Fork, P extends Philosopher<F, P>> {

    protected final Dinner<F, P> dinner;
    protected final Identity identity;
    protected final F leftFork;
    protected final F rightFork;

    @Getter
    private final PhilosopherTimeRecorder timeRecorder = new PhilosopherTimeRecorder();

    public Philosopher(Dinner<F, P> dinner, Identity identity) {
        this(dinner, identity, dinner.getLeftForkOf(identity), dinner.getRightForkOf(identity));
        leftFork.setRightUser(this.getIdentity());
        rightFork.setLeftUser(this.getIdentity());
    }

    public Identity getIdentity() {
        return identity;
    }

    public int getSeatId() {
        return getIdentity().getSeatId();
    }

    public Result run() {
        timeRecorder.getTotalDuration().addActionDuration(this::takeDinner);
        return Result.from(identity, timeRecorder);
    }

    public void takeDinner() {
        while (!Thread.currentThread().isInterrupted()) {
            act();
        }

        log.info(identity + " finished the dinner");
    }

    public void act() {
        if (acquireForks()) {
            eat();
            releaseForks();
        }

        // Philosopher should spend some time on thinking even after successful eating
        // to allow other philosophers to eat som food
        think();
    }

    public abstract boolean acquireForks();

    public abstract void releaseForks();

    public void eat() {
        timeRecorder.getEatingDuration().addActionDuration(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(calculateActionDuration());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info(identity + " was asked to stop eating");
            }
        });
    }

    public void think() {
        timeRecorder.getThinkingDuration().addActionDuration(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(calculateActionDuration());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info(identity + " was asked to stop thinking");
            }
        });
    }

    private long calculateActionDuration() {
        return dinner.settings.getActionDurationMillis() + (long) (Math.random() * dinner.settings.getActionDurationMillis());
    }

    public F getLeftFork() {
        return leftFork;
    }

    public F getRightFork() {
        return rightFork;
    }

    public P getLeftNeighbour() {
        return dinner.getLeftNeighbour(getSeatId());
    }

    public P getRightNeighbour() {
        return dinner.getRightNeighbour(getSeatId());
    }

    public Thread getLeftNeighbourThread() {
        return dinner.getThreadOfLeftNeighbour(getSeatId());
    }

    public Thread getRightNeighbourThread() {
        return dinner.getThreadOfRightNeighbour(getSeatId());
    }


    @Getter
    @RequiredArgsConstructor
    @EqualsAndHashCode
    public static class Result implements Comparable<Result> {

        private final Identity identity;
        private final Duration totalDuration;
        private final Duration eatingDuration;
        private final Duration thinkingDuration;
        private final Duration idleDuration;

        public static Result from(Identity identity, PhilosopherTimeRecorder timeRecorder) {
            return new Result(
                    identity,
                    timeRecorder.getTotalDuration().toDuration(),
                    timeRecorder.getEatingDuration().toDuration(),
                    timeRecorder.getThinkingDuration().toDuration(),
                    timeRecorder.getIdleDuration()
            );
        }

        @Override
        public int compareTo(Result other) {
            return identity.compareTo(other.getIdentity());
        }

    }

}
