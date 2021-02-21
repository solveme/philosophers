package org.solveme.philosophers.progress;

import lombok.RequiredArgsConstructor;
import org.solveme.philosophers.Fork;
import org.solveme.philosophers.Philosopher;

import java.util.List;

import static java.util.stream.Collectors.toList;


@RequiredArgsConstructor
public class ProgressContext implements AutoCloseable {

    private final List<PhilosopherProgress> members;

    public static <F extends Fork, P extends Philosopher<F, P>> ProgressContext from(List<? extends P> philosophers) {
        return new ProgressContext(
                philosophers.stream()
                        .map(PhilosopherProgress::from)
                        .collect(toList())
        );
    }

    public void tick(long totalRunning) {
        members.forEach(p -> p.tick(totalRunning));
    }

    @Override
    public void close() {
        members.forEach(PhilosopherProgress::close);
    }

}
