package org.solveme.philosophers;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.solveme.philosophers.recorders.PhilosopherTimeRecorder;

import java.time.Duration;
import java.util.concurrent.TimeUnit;


@Slf4j
@RequiredArgsConstructor
public abstract class Philosopher<F extends Fork, P extends Philosopher<F, P>> extends RunnerAccessible {

    protected final Dinner<F, P> dinner;
    protected final Identity identity;
    protected final F leftFork;
    protected final F rightFork;

    @Setter
    @Getter
    protected Runner runner;

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
        while (!isShutdown()) {
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
        // to allow other philosophers to eat some food
        think();
    }

    public void interrupt() {
        runner.interrupt();
    }

    private void logWithThreadStatus(String message) {
        assert runner == Runner.currentRunner();
        log.trace(message + ", interrupted: {}", runner.isInterrupted());
    }

    protected boolean acquireForks() {
        if (isShutdown()) {
            log.info("Skip fork acquiring due to shutdown");
            return false;
        }
        logWithThreadStatus("Acquire forks");
        long acquiringStart = System.nanoTime();
        boolean acquiringResult = acquireForks0();
        timeRecorder.getForkAccessDuration().addSpentNanosFrom(acquiringStart);

        return acquiringResult;
    }

    protected abstract boolean acquireForks0();

    protected void releaseForks() {
        if (isShutdown()) {
            log.info("Skip fork releasing due to shutdown");
            return;
        }
        logWithThreadStatus("Release forks");
        timeRecorder.getForkAccessDuration().addActionDuration(this::releaseForks0);
    }

    protected abstract void releaseForks0();

    protected void eat() {
        if (isShutdown()) {
            log.info("Skip eating due to shutdown");
            return;
        }
        logWithThreadStatus("Started to eat");
        timeRecorder.getEatingDuration().addActionDuration(this::eat0);
    }

    protected void eat0() {
        try {
            TimeUnit.MILLISECONDS.sleep(calculateActionDuration());

        } catch (InterruptedException e) {
            logActionInterruption("eating");
            onEatingInterruption(isShutdown());
        }
    }

    protected void onEatingInterruption(boolean isShutdown) {
        // no-op
    }

    protected void think() {
        if (isShutdown()) {
            log.info("Skip thinking due to shutdown");
            return;
        }
        logWithThreadStatus("Started to think");
        timeRecorder.getThinkingDuration().addActionDuration(this::think0);
    }

    protected void think0() {
        try {
            TimeUnit.MILLISECONDS.sleep(calculateActionDuration());

        } catch (InterruptedException e) {
            logActionInterruption("thinking");
            onThinkingInterruption(isShutdown());
        }
    }

    protected void onThinkingInterruption(boolean isShutdown) {
        // no-op
    }

    protected void logActionInterruption(String performedAction) {
        if (isShutdown()) {
            log.info("{} was asked to stop {} due shutdown", identity, performedAction);

        } else {
            log.trace("{} was asked to stop {}", identity, performedAction);
        }
    }

    protected long calculateActionDuration() {
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


    @Getter
    @RequiredArgsConstructor
    @EqualsAndHashCode
    public static class Result implements Comparable<Result> {

        private final Identity identity;
        private final Duration totalDuration;
        private final Duration eatingDuration;
        private final Duration thinkingDuration;
        private final Duration forkAccessDuration;
        private final Duration idleDuration;

        public static Result from(Identity identity, PhilosopherTimeRecorder timeRecorder) {
            return new Result(
                    identity,
                    timeRecorder.getTotalDuration().toDuration(),
                    timeRecorder.getEatingDuration().toDuration(),
                    timeRecorder.getThinkingDuration().toDuration(),
                    timeRecorder.getForkAccessDuration().toDuration(),
                    timeRecorder.getIdleDuration()
            );
        }

        @Override
        public int compareTo(Result other) {
            return identity.compareTo(other.getIdentity());
        }

    }

}
