package org.solveme.philosophers.strategies;

import org.solveme.philosophers.Dinner;
import org.solveme.philosophers.Fork;
import org.solveme.philosophers.Philosopher;

import java.util.concurrent.atomic.AtomicBoolean;


public class Civilized extends Dinner<Civilized.CivilFork, Civilized.CivilPhilosopher> {

    public Civilized(Settings settings) {
        super(settings);
    }

    @Override
    public CivilFork buildFork(Dinner<CivilFork, CivilPhilosopher> dinner) {
        return new CivilFork();
    }

    @Override
    public CivilPhilosopher buildPhilosopher(Dinner<CivilFork, CivilPhilosopher> dinner, int seatId) {
        return new CivilPhilosopher(dinner, seatId);
    }

    static class CivilFork implements Fork {

        private final AtomicBoolean busy = new AtomicBoolean(false);

        @Override
        public boolean isBusy() {
            return busy.get();
        }

        @Override
        public boolean take() {
            return busy.compareAndSet(false, true);
        }

        @Override
        public void release() {
            busy.set(false);
        }
    }

    static class CivilPhilosopher extends Philosopher<CivilFork, CivilPhilosopher> {

        public CivilPhilosopher(Dinner<CivilFork, CivilPhilosopher> dinner, int seatId) {
            super(dinner, seatId);
        }

        @Override
        public boolean acquireForks() {
            return leftFork.take() && rightFork.take();
        }

    }

}
