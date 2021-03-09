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
    protected Identity leftUser;
    protected Identity rightUser;
    protected final ForkTimeRecorder timeRecorder = new ForkTimeRecorder();

    protected volatile long takenTimestamp;

    public void setLeftUser(Identity leftUser) {
        this.leftUser = leftUser;
    }

    public void setRightUser(Identity rightUser) {
        this.rightUser = rightUser;
    }

    public int getId() {
        return id;
    }

    public abstract boolean isBusy();

    public boolean take(Identity identity) {
        if (doTake(identity)) {
            takenTimestamp = System.nanoTime();
            return true;
        }

        return false;
    }

    protected abstract boolean doTake(Identity identity);

    public void release(Identity identity) {
        // After releasing other thread could update takenTimestamp,
        // so we make local copy for further usage duration calculation
        long taken = takenTimestamp;

        doRelease(identity);

        if (identity == leftUser) {
            timeRecorder.recordLeftUsage(System.nanoTime() - taken);

        } else if (identity == rightUser) {
            timeRecorder.recordRightUsage(System.nanoTime() - taken);

        } else {
            throw new IllegalArgumentException(identity + " is not able to use fork #" + id);
        }
    }

    /**
     * Invariants should be guarded by implementations
     */
    protected abstract void doRelease(Identity identity);

    public Result calculateResult() {
        return Result.from(this);
    }

    @Getter
    @RequiredArgsConstructor
    public static class Result {

        private final int id;
        private final Identity leftUser;
        private final Identity rightUser;
        private final Duration leftUsageDuration;
        private final Duration rightUsageDuration;
        private final Duration totalUsageDuration;

        public static <F extends Fork> Fork.Result from(F fork) {
            Duration leftUsage = fork.timeRecorder.getLeftUsage().toDuration();
            Duration rightUsage = fork.timeRecorder.getRightUsage().toDuration();

            return new Result(
                    fork.id,
                    fork.leftUser,
                    fork.rightUser,
                    leftUsage,
                    rightUsage,
                    leftUsage.plus(rightUsage)
            );
        }

    }

}
