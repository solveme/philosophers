package org.solveme.philosophers;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solveme.philosophers.progress.ProgressContext;
import org.solveme.philosophers.util.ResultPrinter;
import org.solveme.philosophers.util.TimeRecorder;
import org.solveme.philosophers.util.Util;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import static org.solveme.philosophers.util.Util.OUT;


@Slf4j
public abstract class Dinner<F extends Fork, P extends Philosopher<F, P>> {

    protected final Settings settings;
    protected final List<P> philosophers;
    protected final List<F> forks;
    protected final List<Thread> threads;
    protected final Coordinator<F, P> coordinator;
    protected final TimeRecorder timeRecorder = new TimeRecorder();

    public Dinner(@Nonnull Settings settings,
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

    public Dinner(@Nonnull Settings settings) {
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
        for (int i = 0; i < settings.getSeatCount(); i++) {
            forks.add(buildFork(this));
        }

        // Init philosophers
        for (int i = 0; i < settings.getSeatCount(); i++) {
            P philosopher = buildPhilosopher(this, i);
            philosophers.add(i, philosopher);
            threads.add(i, new Thread(() -> {

                coordinator.readyToStart(philosopher);
                Philosopher.Result result = philosopher.run();
                coordinator.finishesWith(result);

            }, philosopher.getName().padded()));
        }

    }

    protected abstract F buildFork(Dinner<F, P> dinner);

    protected abstract P buildPhilosopher(Dinner<F, P> dinner, int seatId);

    public void start() {

        threads.forEach(Thread::start);

        if (!coordinator.waitForOtherToStart()) {
            return;
        }

        displayProgress();

        log.warn("Dinner ends, wait for everybody to stop");
        threads.forEach(Thread::interrupt);
        coordinator.waitForOtherToFinish();

        displayResults();
    }

    public void displayProgress() {
        OUT.println();
        try (ProgressContext progress = ProgressContext.from(philosophers)) {
            while (!Thread.currentThread().isInterrupted()) {
                if (TimeUnit.MILLISECONDS.toSeconds(timeRecorder.getTotalSpent()) < settings.getDurationSeconds()) {
                    timeRecorder.recordTotal(() -> {
                        progress.tick(TimeUnit.SECONDS.toMillis(settings.getDurationSeconds()));
                        Util.pause(100);
                    });
                } else {
                    break;
                }
            }
        }
        OUT.println();
    }

    public void displayResults() {
        ResultPrinter printer = ResultPrinter.from(coordinator.getResults());
        OUT.println();
        printer.print();
        OUT.println();
    }

    public int getSeatCount() {
        return forks.size();
    }

    public P getPhilosopherBySeat(int seatId) {
        checkSeatIndex(seatId);
        return philosophers.get(seatId);
    }

    public P getLeftNeighbour(int seatId) {
        checkSeatIndex(seatId);
        return getPhilosopherBySeat(seatId == getSeatCount() - 1 ? 0 : seatId + 1);
    }

    public P getRightNeighbour(int seatId) {
        checkSeatIndex(seatId);
        return getPhilosopherBySeat(seatId == 0 ? getSeatCount() - 1 : seatId - 1);
    }

    public F getLeftFork(int seatId) {
        checkSeatIndex(seatId);
        return forks.get(seatId);
    }

    public F getRightFork(int seatId) {
        checkSeatIndex(seatId);
        return forks.get(seatId == 0 ? getSeatCount() - 1 : seatId - 1);
    }

    // Helpers

    private void checkSeatIndex(int seatId) {
        if (seatId >= getSeatCount()) {
            throw new IllegalArgumentException("There is only " + getSeatCount() + " seats/forks on the table");
        }
    }


    @Builder
    @Getter
    @RequiredArgsConstructor
    public static class Settings {

        private final int seatCount;
        private final int durationSeconds;

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
                    new ArrayList<>(seatCount)
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
                log.info(philosopher.getName() + " waits for other to start");
                startTrigger.await();

            } catch (InterruptedException | BrokenBarrierException e) {
                Thread.currentThread().interrupt();
                log.info(philosopher.getName() + " left the dinner before starting");
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
