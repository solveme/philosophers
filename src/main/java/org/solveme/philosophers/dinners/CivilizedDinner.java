package org.solveme.philosophers.dinners;

import org.solveme.philosophers.Dinner;
import org.solveme.philosophers.Fork;
import org.solveme.philosophers.Philosopher;


public class CivilizedDinner extends Dinner<CivilizedDinner.CivilFork, CivilizedDinner.CivilPhilosopher> {

    public CivilizedDinner(Settings settings) {
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

    public static class CivilFork implements Fork {

        @Override
        public boolean isBusy() {
            return false;
        }

        @Override
        public boolean take() {
            return false;
        }

        @Override
        public void release() {

        }
    }

    public static class CivilPhilosopher extends Philosopher<CivilFork, CivilPhilosopher> {

        public CivilPhilosopher(Dinner<CivilFork, CivilPhilosopher> dinner, int seatId) {
            super(dinner, seatId);
        }

        @Override
        public boolean acquireForks() {
            return false;
        }

    }

}
