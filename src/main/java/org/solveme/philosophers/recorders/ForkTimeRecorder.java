package org.solveme.philosophers.recorders;

import lombok.Getter;


@Getter
public class ForkTimeRecorder {

    private final TimeRecorder leftUsage = new TimeRecorder();
    private final TimeRecorder rightUsage = new TimeRecorder();

    /**
     * @param usageNanos of user to the left of fork
     */
    public void recordLeftUsage(long usageNanos) {
        leftUsage.addSpentNanos(usageNanos);
    }

    /**
     * @param usageNanos of user to the left of fork
     */
    public void recordRightUsage(long usageNanos) {
        rightUsage.addSpentNanos(usageNanos);
    }

}
