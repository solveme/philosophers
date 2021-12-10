package org.solveme.philosophers.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.PrintStream;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Util {

    public static final PrintStream OUT = System.out;

    public static void pause(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);

        } catch (InterruptedException e) {
            log.trace("Pause interruption");
            Thread.currentThread().interrupt();
        }
    }

    public static void await(CyclicBarrier barrier, Action onAlreadyInterruptedAwait) {
        try {
            barrier.await();

        } catch (InterruptedException | BrokenBarrierException e) {
            Thread.currentThread().interrupt();
            onAlreadyInterruptedAwait.execute();
        }
    }

    public interface Action {

        void execute();

    }

}
