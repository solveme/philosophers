package org.solveme.philosophers.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.PrintStream;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Util {

    public static final PrintStream OUT = System.out;

    public static void pause(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void await(CyclicBarrier barrier, AwaitAction onAlreadyInterruptedAwait) {
        try {
            barrier.await();

        } catch (InterruptedException | BrokenBarrierException e) {
            Thread.currentThread().interrupt();
            onAlreadyInterruptedAwait.exec();
        }
    }

    public interface AwaitAction {

        void exec();

    }

}
