package org.solveme.philosophers.util;

import lombok.Getter;


/**
 * Due to fact that recording would be performed only withing dedicated philosopher thread,
 * we don't use any synchronization for class fields
 */
@Getter
public class TimeRecorder {

    private long eatingSpent = 0;
    private long thinkingSpent = 0;
    private long totalSpent = 0;

    public long getIdleSpent() {
        return totalSpent - (eatingSpent + thinkingSpent);
    }

    public void recordEating(long spent) {
        eatingSpent += spent;
    }

    public void recordEating(Action action) {
        recordEating(measureDuration(action));
    }

    public void recordThinking(long spent) {
        thinkingSpent += spent;
    }

    public void recordThinking(Action action) {
        recordThinking(measureDuration(action));
    }

    public void recordTotal(long spent) {
        totalSpent += spent;
    }

    public void recordTotal(Action action) {
        recordTotal(measureDuration(action));
    }

    private static long measureDuration(Action action) {
        long start = System.currentTimeMillis();
        action.execute();
        long end = System.currentTimeMillis();
        return end - start;
    }

    public interface Action {

        void execute();

    }

}
