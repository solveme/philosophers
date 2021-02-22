package org.solveme.philosophers.progress;

import lombok.extern.slf4j.Slf4j;
import me.tongfei.progressbar.InteractiveConsoleProgressBarConsumer;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.commons.lang3.StringUtils;
import org.solveme.philosophers.Fork;
import org.solveme.philosophers.Philosopher;
import org.solveme.philosophers.util.TimeRecorder;

import static org.solveme.philosophers.util.Util.OUT;


@Slf4j
public class PhilosopherProgress implements AutoCloseable {

    private static final String PROGRESS_TYPE_EATING = " >> eating  ";
    private static final String PROGRESS_TYPE_THINKING = "  > thinking";

    private final ProgressBar eatingProgress;
    private final ProgressBar thinkingProgress;
    private final TimeRecorder timeRecorder;

    public PhilosopherProgress(ProgressBar eatingProgress, ProgressBar thinkingProgress, TimeRecorder timeRecorder) {
        this.eatingProgress = eatingProgress;
        this.thinkingProgress = thinkingProgress;
        this.timeRecorder = timeRecorder;
    }

    public static <F extends Fork, P extends Philosopher<F, P>> PhilosopherProgress from(P philosopher) {
        return new PhilosopherProgress(
                getProgressBarForPhilosopher()
                        .setTaskName(eatingBarName(philosopher.getName()))
                        .build(),
                getProgressBarForPhilosopher()
                        .setTaskName(thinkingBarName())
                        .build(),
                philosopher.getTimeRecorder()
        );
    }

    private static <F extends Fork, P extends Philosopher<F, P>> ProgressBarBuilder getProgressBarForPhilosopher() {
        return new ProgressBarBuilder()
                .setConsumer(new InteractiveConsoleProgressBarConsumer(OUT))
                .setStyle(ProgressBarStyle.ASCII)
                .setUnit("ms", 100);
    }

    private static String eatingBarName(Philosopher.Name name) {
        return name.padded() + PROGRESS_TYPE_EATING;
    }

    private static String thinkingBarName() {
        return StringUtils.repeat(' ', Philosopher.Name.MAX_LENGTH) + PROGRESS_TYPE_THINKING;
    }

    public void tick(long totalRunning) {
        eatingProgress.stepTo(timeRecorder.getEatingSpent());
        eatingProgress.maxHint(totalRunning);
        thinkingProgress.stepTo(timeRecorder.getThinkingSpent());
        thinkingProgress.maxHint(totalRunning);
    }

    @Override
    public void close() {
        eatingProgress.close();
    }

}
