package org.solveme.philosophers.progress;

import lombok.extern.slf4j.Slf4j;
import me.tongfei.progressbar.InteractiveConsoleProgressBarConsumer;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.commons.lang3.StringUtils;
import org.solveme.philosophers.Fork;
import org.solveme.philosophers.Identity;
import org.solveme.philosophers.Philosopher;
import org.solveme.philosophers.recorders.PhilosopherTimeRecorder;

import static org.solveme.philosophers.util.Util.OUT;


@Slf4j
public class PhilosopherProgress implements AutoCloseable {

    private static final String PROGRESS_TYPE_EATING = " >> eating  ";
    private static final String PROGRESS_TYPE_THINKING = "  > thinking";

    private final ProgressBar eatingProgress;
    private final ProgressBar thinkingProgress;
    private final PhilosopherTimeRecorder philosopherTimeRecorder;

    public PhilosopherProgress(ProgressBar eatingProgress, ProgressBar thinkingProgress, PhilosopherTimeRecorder philosopherTimeRecorder) {
        this.eatingProgress = eatingProgress;
        this.thinkingProgress = thinkingProgress;
        this.philosopherTimeRecorder = philosopherTimeRecorder;
    }

    public static <F extends Fork, P extends Philosopher<F, P>> PhilosopherProgress from(P philosopher) {
        return new PhilosopherProgress(
                getProgressBarForPhilosopher()
                        .setTaskName(eatingBarName(philosopher.getIdentity()))
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

    private static String eatingBarName(Identity name) {
        return name.padded() + PROGRESS_TYPE_EATING;
    }

    private static String thinkingBarName() {
        return StringUtils.repeat(' ', Identity.MAX_LENGTH) + PROGRESS_TYPE_THINKING;
    }

    public void tick(long totalRunning) {
        eatingProgress.stepTo(philosopherTimeRecorder.getEatingDuration().getMillis());
        eatingProgress.maxHint(totalRunning);
        thinkingProgress.stepTo(philosopherTimeRecorder.getThinkingDuration().getMillis());
        thinkingProgress.maxHint(totalRunning);
    }

    @Override
    public void close() {
        eatingProgress.close();
    }

}
