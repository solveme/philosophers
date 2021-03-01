package org.solveme.philosophers;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solveme.philosophers.recorders.ForkTimeRecorder;

import java.time.Duration;


@Slf4j
@RequiredArgsConstructor
public abstract class Fork {

    protected static final int FREE_FLAG = -1;

    protected final int id;
    protected final ForkTimeRecorder timeRecorder = new ForkTimeRecorder();

    protected volatile long takenTimestamp;

    public abstract boolean isBusy();

    public boolean take(Identity identity) {
        if (doTake(identity)) {
            takenTimestamp = System.nanoTime();
            return true;

        }

        return false;
    }

    protected abstract boolean doTake(Identity identity);

    public void release(Identity identity, Side forkSide) {
        // After releasing other thread could update takenTimestamp,
        // so we make local copy for further usage duration calculation
        long taken = takenTimestamp;

        doRelease(identity);

        timeRecorder.recordUsage(System.nanoTime() - taken, forkSide);
    }

    protected abstract void doRelease(Identity identity);

    public Result calculateResult() {
        return Result.from(this);
    }

    @Getter
    @RequiredArgsConstructor
    public static class Result {

        private final int id;
        private final Duration leftUsageDuration;
        private final Duration rightUsageDuration;
        private final Duration totalUsageDuration;

        public static <F extends Fork> Fork.Result from(F fork) {
            Duration leftUsage = fork.timeRecorder.getLeftUsage().toDuration();
            Duration rightUsage = fork.timeRecorder.getRightUsage().toDuration();

            return new Result(
                    fork.id,
                    leftUsage,
                    rightUsage,
                    leftUsage.plus(rightUsage)
            );
        }

    }

}
