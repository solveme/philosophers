package org.solveme.philosophers;


public class TestingDinner extends Dinner<TestingDinner.TestingFork, TestingDinner.TestingPhilosopher> {

    public TestingDinner(DinnerApp.Settings settings) {
        super(settings);
    }

    @Override
    public TestingFork buildFork(Dinner<TestingFork, TestingPhilosopher> dinner, int forkId) {
        return new TestingFork(forkId);
    }

    @Override
    public TestingPhilosopher buildPhilosopher(Dinner<TestingFork, TestingPhilosopher> dinner, Identity identity) {
        return new TestingPhilosopher(dinner, identity);
    }

    static class TestingFork extends Fork {

        public TestingFork(int id) {
            super(id);
        }

        @Override
        public int getHolderId() {
            return Fork.FREE_FLAG;
        }

        @Override
        public boolean isBusy() {
            return false;
        }

        @Override
        public boolean take0(Identity identity) {
            return false;
        }

        @Override
        public void release0(Identity identity) {
            // no-op
        }
    }

    static class TestingPhilosopher extends Philosopher<TestingFork, TestingPhilosopher> {

        public TestingPhilosopher(Dinner<TestingFork, TestingPhilosopher> dinner, Identity identity) {
            super(dinner, identity);
        }

        @Override
        public boolean acquireForks0() {
            // no-op
            return false;
        }

        @Override
        public void releaseForks0() {
            // no-op
        }
    }

}
