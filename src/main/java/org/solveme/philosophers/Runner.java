package org.solveme.philosophers;

public class Runner extends Thread {

    private volatile boolean shutdown = false;
    private volatile boolean graceful = false;

    public Runner(Runnable target) {
        super(target);
    }

    public static Runner currentRunner() {
        Thread thread = Thread.currentThread();
        assert thread instanceof Runner : "Current thread is not a runner";
        return (Runner) thread;
    }

    public void shutdown(boolean graceful) {
        this.graceful = graceful;
        this.shutdown = true;
        this.interrupt();
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public boolean isGraceful() {
        return graceful;
    }

}
