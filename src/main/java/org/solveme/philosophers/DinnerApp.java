package org.solveme.philosophers;

import ch.qos.logback.classic.Level;
import com.diogonunes.jcolor.AnsiFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import static com.diogonunes.jcolor.Attribute.*;
import static org.solveme.philosophers.util.Util.OUT;


@CommandLine.Command(name = "dinner")
public class DinnerApp implements Runnable {

    public static final ch.qos.logback.classic.Logger ROOT_LOGGER = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    private static final Logger log = LoggerFactory.getLogger(DinnerApp.class);

    @CommandLine.Parameters(index = "0", description = "available strategies: ${COMPLETION-CANDIDATES}")
    Strategy strategy;

    @CommandLine.Option(names = "-c", paramLabel = "COUNT", description = "how many philosophers should be invited, default=${DEFAULT-VALUE}")
    int philosophersCount = Identity.values().length;

    @CommandLine.Option(names = "-D", paramLabel = "SECONDS", description = "dinner duration in seconds, default=${DEFAULT-VALUE}")
    int dinnerDurationSeconds = 10;

    @CommandLine.Option(names = "-A", paramLabel = "MILLISECONDS", description = "action (eating/thinking) duration factor in millis, default=${DEFAULT-VALUE}")
    int actionDurationMillis = 100;

    @CommandLine.Option(names = "-NP", description = "don't show progress bars during dinner, default=${DEFAULT-VALUE}")
    boolean dontShowProgress = false;

    @CommandLine.Option(names = "-v", description = "Verbosity. Specify multiple -v options to increase verbosity (e.g. -vv)")
    boolean[] verbosity;

    public static void main(String[] args) {
        AnsiFormat format = new AnsiFormat(RED_TEXT(), GREEN_BACK(), BOLD());
        String threadName = StringUtils.leftPad("Dinner", Identity.MAX_LENGTH);
        Thread.currentThread().setName(format.format(threadName));
        int exitCode = new CommandLine(new DinnerApp()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        tuneLogLevel();

        log.info("Initialize dinner");

        Settings settings = Settings.builder()
                .seatCount(philosophersCount)
                .durationSeconds(dinnerDurationSeconds)
                .actionDurationMillis(actionDurationMillis)
                .showProgress(!dontShowProgress)
                .build();

        Dinner<?, ?> dinner = strategy.getInitiator().apply(settings);
        Thread shutdownHook = shutdownHook(dinner::abort);

        try {
            Runtime.getRuntime().addShutdownHook(shutdownHook);

        } catch (Throwable t) {
            OUT.println("Failed to set up shutdown hook: " + t.getMessage());
            t.printStackTrace(OUT);
            System.exit(10);
        }

        dinner.init();
        dinner.start();

        Runtime.getRuntime().removeShutdownHook(shutdownHook);
    }

    private void tuneLogLevel() {
        if (verbosity == null || verbosity.length == 0) return;
        if (verbosity.length == 1) ROOT_LOGGER.setLevel(Level.DEBUG);
        if (verbosity.length == 2) ROOT_LOGGER.setLevel(Level.TRACE);
        if (verbosity.length == 3) ROOT_LOGGER.setLevel(Level.ALL);
    }

    private static Thread shutdownHook(Runnable runnable) {
        AnsiFormat format = new AnsiFormat(GREEN_TEXT(), RED_BACK(), BOLD());
        String threadName = StringUtils.leftPad("Shutdown", Identity.MAX_LENGTH);

        Thread hook = new Thread(runnable);
        hook.setName(format.format(threadName));
        return hook;
    }


    @Builder
    @Getter
    @RequiredArgsConstructor
    public static class Settings {

        private final int seatCount;
        private final int durationSeconds;
        private final int actionDurationMillis;
        private final boolean showProgress;

    }

}
