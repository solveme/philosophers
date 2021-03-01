package org.solveme.philosophers.recorders;

import lombok.Getter;

import java.time.Duration;
import java.time.Instant;


@Getter
public class DinnerTimeRecorder {

    private final TimeRecorder runningDuration = new TimeRecorder();

    private Instant start;
    private Instant end;
    private Duration duration;

    public void recordStart() {
        start = Instant.now();
    }

    public void recordEnd() {
        end = Instant.now();
        duration = Duration.between(start, end);
    }

    public Duration getDuration() {
        if (duration == null) {
            throw new IllegalStateException("Dinner not yet finished");
        }
        return duration;
    }
}
