package org.solveme.philosophers.strategies;

import lombok.extern.slf4j.Slf4j;
import org.solveme.philosophers.*;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * Solution that utilizes CAS features of AtomicInteger for handling forks acquiring and releasing
 */
public class Atomic extends Dinner<Atomic.AtomicFork, Atomic.AtomicPhilosopher> {

    public Atomic(DinnerApp.Settings settings) {
        super(settings);
    }

    @Override
    public AtomicFork buildFork(Dinner<AtomicFork, AtomicPhilosopher> dinner, int forkId) {
        return new AtomicFork(forkId);
    }

    @Override
    public AtomicPhilosopher buildPhilosopher(Dinner<AtomicFork, AtomicPhilosopher> dinner, Identity identity) {
        return new AtomicPhilosopher(dinner, identity);
    }

    @Slf4j
    static class AtomicFork extends Fork {

        private final AtomicInteger holder = new AtomicInteger(FREE_FLAG);

        public AtomicFork(int id) {
            super(id);
        }

        @Override
        public int getHolderId() {
            return holder.get();
        }

        @Override
        protected boolean take0(Identity identity) {
            return holder.compareAndSet(FREE_FLAG, identity.getSeatId());
        }

        @Override
        protected void release0(Identity identity) {
            // Invariant guard: only holder is allowed to release fork
            assert holder.get() != identity.getSeatId() : identity + " is not a holder of #" + id;
            holder.compareAndSet(identity.getSeatId(), FREE_FLAG);
        }

    }

    static class AtomicPhilosopher extends Philosopher<AtomicFork, AtomicPhilosopher> {

        public AtomicPhilosopher(Dinner<AtomicFork, AtomicPhilosopher> dinner, Identity identity) {
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
