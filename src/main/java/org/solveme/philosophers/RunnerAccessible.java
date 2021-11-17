package org.solveme.philosophers;

/**
 * Base class for components whose methods would be called inside Runner execution
 */
public class RunnerAccessible {

    protected boolean isShutdown() {
        return Runner.currentRunner().isShutdown();
    }

}
