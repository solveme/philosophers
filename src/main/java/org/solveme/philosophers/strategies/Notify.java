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
        return new NotifyFork(forkId, table);
    }

    @Override
    public NotifyPhilosopher buildPhilosopher(Dinner<NotifyFork, NotifyPhilosopher> dinner, Identity identity) {
        return new NotifyPhilosopher(dinner, identity);
    }

    @Slf4j
    static class NotifyFork extends Fork {

        private static final long WAIT_TIMEOUT = TimeUnit.SECONDS.toMillis(1);
        private static final long ELAPSED_EPSILON_MILLIS = 5;

        private int holder = FREE_FLAG;
        private final Table table;

        public NotifyFork(int id, Table table) {
            super(id);
            this.table = table;
        }

        @Override
        public int getHolderId() {
            return holder;
        }

        @Override
        protected synchronized boolean take0(Identity identity) {

            Identity currentHolder;

            // Due to possible spurious wakeup during this.wait()
            // we do some additional calculations for precise waiting time
            long waitingElapsedMillis = WAIT_TIMEOUT;
            long waitingStartNanos = 0;
            long wakeupTimeNanos;

            while (isBusy()) {

                currentHolder = Identity.at(holder);

                if (waitingStartNanos == 0) {
                    log.trace("Wait until {} release the fork #{}", currentHolder, id);
                }

                try {
                    waitingStartNanos = System.nanoTime();
                    this.wait(waitingElapsedMillis);

                    // At this moment we could be awakened by the thread notification or by spurious wakeup,
                    // and we could not wait for the entire WAIT_TIMEOUT, so if fork is still busy,
                    // we reduce waitingElapsedMillis and starting to wait again
                    wakeupTimeNanos = System.nanoTime();
                    waitingElapsedMillis -= TimeUnit.NANOSECONDS.toMillis(wakeupTimeNanos - waitingStartNanos);

                    if (isBusy()) {
                        if (waitingElapsedMillis <= ELAPSED_EPSILON_MILLIS) {
                            log.debug("Timeout during acquiring #{} by {}", id, identity);
                            return false;
                        } else {
                            log.trace("Continue to wait until {} release the fork #{}", currentHolder, id);
                        }
                    } else {
                        log.debug("Finish waiting due to fork #{} release by {}", id, currentHolder);
                        break;
                    }

                } catch (InterruptedException e) {
                    log.debug("Interrupt during acquiring #{} by {}", id, identity);
                    Thread.currentThread().interrupt();
                    return false;
                }
            }

            log.debug("Take {} fork #{}", table.sideOfNearFork(id, identity).name().toLowerCase(), id);
            holder = identity.getSeatId();

            return true;
        }

        @Override
        protected synchronized void release0(Identity identity) {
            log.debug("Release {} fork #{}", table.sideOfNearFork(id, identity).name().toLowerCase(), id);
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
        public boolean acquireForks0() {
            if (leftFork.take(identity)) {
                if (rightFork.take(identity)) {
                    return true;
                } else {
                    leftFork.release(identity);
                    return false;
                }
            }

            return false;
        }

        @Override
        public void releaseForks0() {
            rightFork.release(identity);
            leftFork.release(identity);
        }
    }

}
