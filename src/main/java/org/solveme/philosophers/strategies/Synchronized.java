package org.solveme.philosophers.strategies;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solveme.philosophers.*;


/**
 * Straight forward solution that performs acquiring/releasing via intrinsic locks on forks objects. For preventing
 * deadlock when all philosophers have acquired left fork and are waiting on releasing right fork (that would never
 * happen in this case) ${@link AcquiringOrder} helper was introduced. This object would encapsulate acquiring order
 * and would use inverted order for first philosopher ${@link SynchronizedPhilosopher#SynchronizedPhilosopher(Dinner, Identity)}
 */
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
        public int getHolderId() {
            return holder;
        }

        @Override
        protected boolean take0(Identity identity) {
            if (!isBusy()) {
                log.debug("#{} acquired by {}", id, identity);
                holder = identity.getSeatId();
                return true;
            }

            return false;
        }

        @Override
        protected void release0(Identity identity) {
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
        public boolean acquireForks0() {
            return acquiringOrder.acquire();
        }

        @Override
        public void releaseForks0() {
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
