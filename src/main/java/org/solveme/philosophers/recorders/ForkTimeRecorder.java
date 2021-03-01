package org.solveme.philosophers.recorders;

import lombok.Getter;
import org.solveme.philosophers.Side;


@Getter
public class ForkTimeRecorder {

    private final TimeRecorder leftUsage = new TimeRecorder();
    private final TimeRecorder rightUsage = new TimeRecorder();

    public void recordUsage(long usageNanos, Side side) {
        switch (side) {
            case LEFT:
                leftUsage.addSpentNanos(usageNanos);
                break;
            case RIGHT:
                rightUsage.addSpentNanos(usageNanos);
                break;
            default:
                throw new IllegalArgumentException("Unknown side: " + side);
        }
    }

}
