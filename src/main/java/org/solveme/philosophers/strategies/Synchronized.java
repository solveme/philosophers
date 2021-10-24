package org.solveme.philosophers.strategies;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Slf4j
    static class SynchronizedFork extends Fork {

        private int holder = FREE_FLAG;

        public SynchronizedFork(int id) {
            super(id);
        }

        @Override
        public boolean isBusy() {
            return holder != FREE_FLAG;
        }

        @Override
        public boolean doTake(Identity identity) {
            if (!isBusy()) {
                log.debug("#{} acquired by {}", id, identity);
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

    @Slf4j
    static class SynchronizedPhilosopher extends Philosopher<SynchronizedFork, SynchronizedPhilosopher> {

        private final AcquiringOrder acquiringOrder;

        public SynchronizedPhilosopher(Dinner<SynchronizedFork, SynchronizedPhilosopher> dinner,
                                       Identity identity
        ) {
            super(dinner, identity);
            // One of philosophers should acquire/release forks in reverse order to avoid deadlock
            acquiringOrder = getSeatId() != 0
                    ? AcquiringOrder.straight(identity, getLeftFork(), getRightFork())
                    : AcquiringOrder.reverse(identity, getLeftFork(), getRightFork());
        }

        @Override
        public boolean acquireForks() {
            return acquiringOrder.acquire();
        }

        @Override
        public void releaseForks() {
            acquiringOrder.releaseForks();
        }
    }

    @Slf4j
    @RequiredArgsConstructor
    private static class AcquiringOrder {

        private final Identity identity;
        private final SynchronizedFork first;
        private final SynchronizedFork second;

        public static AcquiringOrder straight(Identity identity,
                                              SynchronizedFork leftFork,
                                              SynchronizedFork rightFork
        ) {
            return new AcquiringOrder(identity, leftFork, rightFork);
        }

        public static AcquiringOrder reverse(Identity identity,
                                             SynchronizedFork leftFork,
                                             SynchronizedFork rightFork
        ) {
            return new AcquiringOrder(identity, rightFork, leftFork);
        }

        public boolean acquire() {
            log.debug("{} wait for #{}", identity, first.getId());
            synchronized (first) {
                if (first.take(identity)) {

                    log.debug("{} wait for #{}", identity, first.getId());
                    synchronized (second) {
                        if (second.take(identity)) {
                            return true;
                        }

                        first.release(identity);
                        return false;
                    }

                }

                return false;
            }
        }

        public void releaseForks() {
            synchronized (first) {
                synchronized (second) {
                    second.release(identity);
                }

                first.release(identity);
            }
        }

    }

}
