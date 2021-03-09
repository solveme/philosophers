package org.solveme.philosophers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solveme.philosophers.progress.ProgressContext;
import org.solveme.philosophers.recorders.DinnerTimeRecorder;
import org.solveme.philosophers.results.ForkResults;
import org.solveme.philosophers.results.PhilosopherResults;
import org.solveme.philosophers.util.Util;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.solveme.philosophers.Side.LEFT;
import static org.solveme.philosophers.Side.RIGHT;
import static org.solveme.philosophers.util.Util.OUT;
import static org.solveme.philosophers.util.Util.normalizeSeatId;


@Slf4j
public abstract class Dinner<F extends Fork, P extends Philosopher<F, P>> {

    protected final DinnerApp.Settings settings;
    protected final List<P> philosophers;
    protected final List<F> forks;
    protected final List<Thread> threads;
    protected final Coordinator<F, P> coordinator;
    protected final DinnerTimeRecorder timeRecorder = new DinnerTimeRecorder();

    public Dinner(@Nonnull DinnerApp.Settings settings,
                  @Nonnull List<P> philosophers,
                  @Nonnull List<F> forks,
                  @Nonnull List<Thread> threads,
                  @Nonnull Coordinator<F, P> coordinator
    ) {
        this.settings = settings;
        this.philosophers = philosophers;
        this.forks = forks;
        this.threads = threads;
        this.coordinator = coordinator;
    }

    public Dinner(@Nonnull DinnerApp.Settings settings) {
        this(
                settings,
                new ArrayList<>(settings.getSeatCount()),
                new ArrayList<>(settings.getSeatCount()),
                new ArrayList<>(settings.getSeatCount()),
                new Coordinator<>(settings.getSeatCount())
        );
    }

    public void init() {

        // Init forks
        for (int forkId = 0; forkId < settings.getSeatCount(); forkId++) {
            forks.add(buildFork(this, forkId));
        }

        // Init philosophers
        for (int seatId = 0; seatId < settings.getSeatCount(); seatId++) {
            P philosopher = buildPhilosopher(this, Identity.at(seatId));
            philosophers.add(seatId, philosopher);
            threads.add(seatId, new Thread(() -> {

                coordinator.readyToStart(philosopher);
                Philosopher.Result result = philosopher.run();
                coordinator.finishesWith(result);

            }, philosopher.getIdentity().padded()));
        }

    }

    protected abstract F buildFork(Dinner<F, P> dinner, int forkId);

    protected abstract P buildPhilosopher(Dinner<F, P> dinner, Identity identity);

    public void start() {

        threads.forEach(Thread::start);

        if (!coordinator.waitForOtherToStart()) {
            return;
        } else {
            timeRecorder.recordStart();
        }

        progressLoop();

        log.warn("Dinner ends, wait for everybody to stop");
        threads.forEach(Thread::interrupt);

        timeRecorder.recordEnd();
        coordinator.waitForOtherToFinish();

        displayResults();
    }

    public void progressLoop() {
        if (settings.isShowProgress()) {
            OUT.println();
            try (ProgressContext progress = ProgressContext.from(philosophers)) {
                progressLoop(() -> progress.tick(TimeUnit.SECONDS.toMillis(settings.getDurationSeconds())));
            }
            OUT.println();

        } else {
            progressLoop(() -> {
                // no-op
            });
        }
    }

    public void progressLoop(Util.Action action) {
        while (!Thread.currentThread().isInterrupted()) {
            if (timeRecorder.getRunningDuration().getSeconds() < settings.getDurationSeconds()) {
                timeRecorder.getRunningDuration().addActionDuration(() -> {
                    action.execute();
                    Util.pause(100);
                });
            } else {
                break;
            }
        }
    }

    public void displayResults() {
        OUT.println();
        PhilosopherResults.from(
                coordinator.getResults(),
                timeRecorder.getRunningDuration().toDuration()
        ).print();
        OUT.println();
        ForkResults.from(
                forks.stream().map(Fork::calculateResult).collect(Collectors.toList()),
                timeRecorder.getRunningDuration().toDuration()
        ).print();
        OUT.println();
    }

    public int getSeatCount() {
        return forks.size();
    }

    // Philosopher access

    public P getLeftNeighbour(int seatId) {
        return getPhilosopherBySeatId(LEFT.neighbourOf(seatId));
    }

    public P getRightNeighbour(int seatId) {
        return getPhilosopherBySeatId(RIGHT.neighbourOf(seatId));
    }

    // Thread access

    public Thread getThreadOfLeftNeighbour(int callerSeatId) {
        return getPhilosopherThreadBySeatId(LEFT.neighbourOf(callerSeatId));
    }

    public Thread getThreadOfRightNeighbour(int callerSeatId) {
        return getPhilosopherThreadBySeatId(RIGHT.neighbourOf(callerSeatId));
    }

    // Forks access

    public F getLeftForkOf(int seatId) {
        return getForkById(LEFT.forkIdOf(seatId));
    }

    public F getLeftForkOf(Identity identity) {
        return getLeftForkOf(identity.getSeatId());
    }

    public F getRightForkOf(int seatId) {
        return getForkById(RIGHT.forkIdOf(seatId));
    }

    public F getRightForkOf(Identity identity) {
        return getRightForkOf(identity.getSeatId());
    }

    // Helpers

    public P getPhilosopherBySeatId(int seatId) {
        return philosophers.get(normalizeId(seatId));
    }

    public F getForkById(int forkId) {
        return forks.get(normalizeId(forkId));
    }

    public Thread getPhilosopherThreadBySeatId(int seatId) {
        return threads.get(normalizeId(seatId));
    }

    private int normalizeId(int id) {
        return normalizeSeatId(id, getSeatCount());
    }


    @RequiredArgsConstructor
    public static class Coordinator<F extends Fork, P extends Philosopher<F, P>> {

        private final CyclicBarrier startTrigger;
        private final CountDownLatch finishLatch;
        private final List<Philosopher.Result> results;

        public Coordinator(int seatCount) {
            this(
                    new CyclicBarrier(seatCount + 1),
                    new CountDownLatch(seatCount),
                    Collections.synchronizedList(new ArrayList<>(seatCount))
            );
        }

        public List<Philosopher.Result> getResults() {
            return results;
        }

        public boolean waitForOtherToStart() {
            try {
                startTrigger.await();
                log.warn("Everybody is ready");
                return true;

            } catch (InterruptedException | BrokenBarrierException e) {
                Thread.currentThread().interrupt();
                log.warn("Dinner is over due to interrupt");
                return false;
            }
        }

        public void readyToStart(P philosopher) {
            try {
                log.info(philosopher.getIdentity() + " waits for other to start");
                startTrigger.await();

            } catch (InterruptedException | BrokenBarrierException e) {
                Thread.currentThread().interrupt();
                log.info(philosopher.getIdentity() + " left the dinner before starting");
            }
        }

        public void waitForOtherToFinish() {
            try {
                finishLatch.await();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public void finishesWith(Philosopher.Result result) {
            results.add(result);
            finishLatch.countDown();
        }

    }

}
