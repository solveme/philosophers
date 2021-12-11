package org.solveme.philosophers.strategies;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solveme.philosophers.*;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * Implementation, that uses additional actor 'Waiter' for managing access to forks.
 * With waiter philosophers don't have to wait or compete for forks, waiter would accept fork requests
 * and notify philosophers about forks availability via thread interruption.
 */
public class Managed extends Dinner<Managed.ManagedFork, Managed.ManagedPhilosopher> {

    private final Waiter waiter;

    public Managed(DinnerApp.Settings settings) {
        super(settings);
        waiter = new Waiter(this);
    }

    @Override
    public ManagedFork buildFork(Dinner<ManagedFork, ManagedPhilosopher> dinner, int forkId) {
        return new ManagedFork(forkId);
    }

    @Override
    public ManagedPhilosopher buildPhilosopher(Dinner<ManagedFork, ManagedPhilosopher> dinner, Identity identity) {
        return new ManagedPhilosopher(dinner, identity, waiter);
    }

    @Slf4j
    static class ManagedFork extends Fork {

        private int holder = FREE_FLAG;

        public ManagedFork(int id) {
            super(id);
        }

        @Override
        public int getHolderId() {
            return holder;
        }

        @Override
        protected boolean take0(Identity identity) {
            log.trace("Fork #{} was taken by {}", getId(), identity);
            holder = identity.getSeatId();
            return true;
        }

        @Override
        protected void release0(Identity identity) {
            log.trace("Fork #{} was released by {}", getId(), identity);
            holder = FREE_FLAG;
        }

    }

    static class ManagedPhilosopher extends Philosopher<ManagedFork, ManagedPhilosopher> {

        private final Waiter waiter;

        public ManagedPhilosopher(Dinner<ManagedFork, ManagedPhilosopher> dinner, Identity identity, Waiter waiter) {
            super(dinner, identity);
            this.waiter = waiter;
        }

        @Override
        public boolean acquireForks0() {
            return waiter.acquireForks(this);
        }

        @Override
        public void releaseForks0() {
            waiter.releaseForks(this);
        }

    }

    @Slf4j
    static class Waiter extends RunnerAccessible {

        private final Managed dinner;

        private final Object mutex = new Object();
        private final List<Reservation> reservations;

        public Waiter(Managed dinner) {
            this.dinner = dinner;
            this.reservations = IntStream.range(0, dinner.getForkCount())
                    .mapToObj(i -> new Reservation(dinner, i))
                    .collect(Collectors.toList());
        }

        public Reservation getReservation(int forkId) {
            return reservations.get(forkId);
        }

        public boolean acquireForks(ManagedPhilosopher philosopher) {
            Identity identity = philosopher.getIdentity();
            Fork leftFork = philosopher.getLeftFork();
            Fork rightFork = philosopher.getRightFork();

            synchronized (mutex) {
                if (!reserveFork(leftFork, identity) || !reserveFork(rightFork, identity)) {
                    log.debug("{} failed to reserve both forks and should try next time", identity);
                    return false;
                }

                if (leftFork.isBusy() && rightFork.isBusy()) {
                    log.debug("Both forks are busy, {} has to wait", identity);
                    return false;
                }

                takeFork(philosopher, leftFork);
                takeFork(philosopher, rightFork);
            }

            return true;
        }

        private void takeFork(ManagedPhilosopher philosopher, Fork fork) {
            Identity identity = philosopher.getIdentity();

            if (fork.isBusy()) {
                ManagedPhilosopher forkHolder = dinner.getPhilosopherBySeatId(fork.getHolderId());

                // Clear notify flag since already waits for releasing this fork
                getReservation(fork.getId()).dontNotify();

                log.trace("Forcibly ask {} to release fork #{}", forkHolder.getIdentity(), fork.getId());
                forkHolder.interrupt();

                while (fork.isBusy()) {
                    try {
                        mutex.wait();

                    } catch (InterruptedException e) {
                        if (isShutdown()) {
                            log.info("Waiter interrupted during acquiring #{} for {} due to shutdown", fork.getId(), identity);
                        } else {
                            log.trace("Waiter interrupted during acquiring #{} for {}", fork.getId(), identity);
                        }
                        return;
                    }
                }
            }

            fork.take(identity);
            finishReservation(fork, identity);
        }

        private boolean reserveFork(Fork fork, Identity identity) {
            int forkId = fork.getId();
            int seatId = identity.getSeatId();
            Reservation reservation = getReservation(forkId);

            if (reservation.getSeatId() == seatId) {
                log.trace("Fork #{} was already reserved by {}", forkId, identity);
                return true;
            }

            if (reservation.isFree()) {
                reservation.update(seatId, true);
                log.trace("{} successfully reserved fork #{}", identity, forkId);
                return true;
            }

            log.trace("{} failed to reserve fork #{}", identity, forkId);
            return false;
        }

        private void finishReservation(Fork fork, Identity identity) {
            int forkId = fork.getId();
            int seatId = identity.getSeatId();
            Reservation reservation = getReservation(forkId);

            if (reservation.getSeatId() == seatId) {
                log.trace("{} release reservation of fork #{}", identity, forkId);
                reservation.release();
            }
        }

        public void releaseForks(ManagedPhilosopher philosopher) {
            log.debug("{} is going to release forks", philosopher.getIdentity());
            Fork leftFork = philosopher.getLeftFork();
            Fork rightFork = philosopher.getRightFork();

            synchronized (mutex) {
                Reservation leftReservation = releaseFork(leftFork, philosopher);
                Reservation rightReservation = releaseFork(rightFork, philosopher);
                if (leftReservation.isNotificationRequired()) {
                    leftReservation.doNotification();
                }
                if (rightReservation.isNotificationRequired() && rightReservation.getSeatId() != leftReservation.getSeatId()) {
                    rightReservation.doNotification();
                }

                mutex.notifyAll();
            }
        }

        @Nonnull
        private Reservation releaseFork(Fork fork, ManagedPhilosopher oldOwner) {
            fork.release(oldOwner.getIdentity());
            return getReservation(fork.getId());
        }

    }

    /**
     * Any method should be called with acquired {@link Waiter#mutex}
     */
    @Slf4j
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class Reservation {

        private final Managed dinner;
        private final int forkId;

        @Getter
        private int seatId = Fork.FREE_FLAG;
        private boolean notify = false;

        @Nonnull
        public ManagedPhilosopher getRequester() {
            assert seatId != Fork.FREE_FLAG : "Fork is free!";
            return dinner.getPhilosopherBySeatId(seatId);
        }

        public boolean isFree() {
            return seatId == Fork.FREE_FLAG;
        }

        public void update(int seatId, boolean notify) {
            this.seatId = seatId;
            this.notify = notify;
        }

        public boolean isNotificationRequired() {
            return !isFree() && notify;
        }

        public void doNotification() {
            log.trace("Notify {} about availability fork #{}", getRequester().getIdentity(), forkId);
            getRequester().interrupt();
        }

        public void dontNotify() {
            this.notify = false;
        }

        public void release() {
            this.seatId = Fork.FREE_FLAG;
            this.notify = false;
        }

    }

}
