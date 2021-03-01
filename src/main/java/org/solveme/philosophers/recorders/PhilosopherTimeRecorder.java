package org.solveme.philosophers.recorders;

import lombok.Getter;

import java.time.Duration;


/**
 * Due to fact that recording would be performed only withing dedicated philosopher thread,
 * we don't use any synchronization for class fields
 */
@Getter
public class PhilosopherTimeRecorder {

    private final TimeRecorder eatingDuration = new TimeRecorder();
    private final TimeRecorder thinkingDuration = new TimeRecorder();
    private final TimeRecorder totalDuration = new TimeRecorder();

    public Duration getIdleDuration() {
        return Duration.ofNanos(totalDuration.getNanos() - (eatingDuration.getNanos() + thinkingDuration.getNanos()));
    }

}
