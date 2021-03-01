package org.solveme.philosophers.strategies;

import org.solveme.philosophers.*;


public class Synchronized extends Dinner<Synchronized.SynchronizedFork, Synchronized.SynchronizedPhilosopher> {

    public Synchronized(DinnerApp.Settings settings) {
        super(settings);
    }

    @Override
    public SynchronizedFork buildFork(Dinner<SynchronizedFork, SynchronizedPhilosopher> dinner, int forkId) {
        return new SynchronizedFork(forkId);
    }

    @Override
    public SynchronizedPhilosopher buildPhilosopher(Dinner<SynchronizedFork, SynchronizedPhilosopher> dinner, Identity identity) {
        return new SynchronizedPhilosopher(dinner, identity);
    }

    static class SynchronizedFork extends Fork {

        private int holder = FREE_FLAG;

        public SynchronizedFork(int id) {
            super(id);
        }

        @Override
        public boolean isBusy() {
            return holder == FREE_FLAG;
        }

        @Override
        public boolean doTake(Identity identity) {
            if (!isBusy()) {
                holder = identity.getSeatId();
                return true;
            }

            return false;
        }

        @Override
        public void doRelease(Identity identity) {
            holder = FREE_FLAG;
        }
    }

    static class SynchronizedPhilosopher extends Philosopher<SynchronizedFork, SynchronizedPhilosopher> {

        public SynchronizedPhilosopher(Dinner<SynchronizedFork, SynchronizedPhilosopher> dinner, Identity identity) {
            super(dinner, identity);
        }

        @Override
        public boolean acquireForks() {

            synchronized (leftFork) {
                if (leftFork.take(identity)) {

                    synchronized (rightFork) {
                        if (rightFork.take(identity)) {
                            return true;
                        }

                        leftFork.release(identity, Side.LEFT);
                        return false;
                    }

                }

                return false;
            }
        }

        @Override
        public void releaseForks() {
            synchronized (leftFork) {
                synchronized (rightFork) {
                    rightFork.release(identity, Side.RIGHT);
                }

                leftFork.release(identity, Side.LEFT);
            }
        }
    }

}
