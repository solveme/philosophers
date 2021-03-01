package org.solveme.philosophers.recorders;

import lombok.Getter;
import org.solveme.philosophers.util.Util;

import java.time.Duration;
import java.util.concurrent.TimeUnit;


@Getter
public class TimeRecorder {

    private long nanos = 0;

    public void addSpentNanos(long spentNanos) {
        nanos += spentNanos;
    }

    public void addActionDuration(Util.Action action) {
        addSpentNanos(measureDuration(action));
    }

    public long getMillis() {
        return TimeUnit.NANOSECONDS.toMillis(nanos);
    }

    public long getSeconds() {
        return TimeUnit.NANOSECONDS.toSeconds(nanos);
    }

    public Duration toDuration() {
        return Duration.ofNanos(getNanos());
    }

    public static long measureDuration(Util.Action action) {
        long start = System.nanoTime();
        action.execute();
        return System.nanoTime() - start;
    }

}
