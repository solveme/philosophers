package org.solveme.philosophers.strategies;

import lombok.extern.slf4j.Slf4j;
import org.solveme.philosophers.*;

import java.util.concurrent.TimeUnit;


/**
 * Solution based on usage of a condition queue inside fork acquire/release methods. Due to fact that for acquiring
 * logic uses timeouts as a parameter in <code>wait()</code> calls we are safe from deadlock that may happen
 * in "synchronized only" logic. Also <code>notify()</code> usage also helps to reduce forks idle thus
 * increasing overall performance.
 */
public class Notify extends Dinner<Notify.NotifyFork, Notify.NotifyPhilosopher> {

    public Notify(DinnerApp.Settings settings) {
        super(settings);
    }

    @Override
    public NotifyFork buildFork(Dinner<NotifyFork, NotifyPhilosopher> dinner, int forkId) {
        return new NotifyFork(forkId);
    }

    @Override
    public NotifyPhilosopher buildPhilosopher(Dinner<NotifyFork, NotifyPhilosopher> dinner, Identity identity) {
        return new NotifyPhilosopher(dinner, identity);
    }

    @Slf4j
    static class NotifyFork extends Fork {

        private static final long WAIT_TIME = TimeUnit.SECONDS.toMillis(1);

        private int holder = FREE_FLAG;

        public NotifyFork(int id) {
            super(id);
        }

        @Override
        public int getHolderId() {
            return holder;
        }

        @Override
        protected synchronized boolean take0(Identity identity) {

            // Due to possible spurious wakeup during this.wait()
            // we do some additional calculations for precise waiting time
            long waitingElapsed = WAIT_TIME;
            long waitingStart = System.nanoTime();

            while (isBusy()) {
                try {
                    this.wait(waitingElapsed);
                    waitingElapsed -= TimeUnit.NANOSECONDS.toMillis(waitingStart - System.nanoTime());
                    if (waitingElapsed < 0 && isBusy()) {
                        log.debug("Timeout during acquiring #{} by {}", id, identity);
                        return false;
                    }

                } catch (InterruptedException e) {
                    log.debug("Interrupt during acquiring #{} by {}", id, identity);
                    Thread.currentThread().interrupt();
                    return false;
                }
            }

            holder = identity.getSeatId();

            return true;
        }

        @Override
        protected synchronized void release0(Identity identity) {
            holder = FREE_FLAG;

            // Only one thread may wait on this fork
            this.notify();
        }

    }

    @Slf4j
    static class NotifyPhilosopher extends Philosopher<NotifyFork, NotifyPhilosopher> {


        public NotifyPhilosopher(Dinner<NotifyFork, NotifyPhilosopher> dinner,
                                 Identity identity
        ) {
            super(dinner, identity);
        }

        @Override
        public boolean acquireForks() {
            if (leftFork.take(identity)) {
                if (!rightFork.take(identity)) {
                    leftFork.release(identity);
                    return false;
                }
            }

            return true;
        }

        @Override
        public void releaseForks() {
            leftFork.release(identity);
            rightFork.release(identity);
        }
    }

}
