package org.solveme.philosophers;

import com.diogonunes.jcolor.AnsiFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import static com.diogonunes.jcolor.Attribute.*;


@CommandLine.Command(name = "dinner")
public class DinnerApp implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(DinnerApp.class);

    @CommandLine.Parameters(index = "0", description = "available strategies: ${COMPLETION-CANDIDATES}")
    Strategy strategy;

    @CommandLine.Option(names = "-c", paramLabel = "COUNT", description = "how many philosophers should be invited, default=${DEFAULT-VALUE}")
    int philosophersCount = Identity.values().length;

    @CommandLine.Option(names = "-D", paramLabel = "SECONDS", description = "dinner duration in seconds, default=${DEFAULT-VALUE}")
    int dinnerDuration = 10;

    @CommandLine.Option(names = "-A", paramLabel = "SECONDS", description = "action (eating/thinking) duration factor in millis, default=${DEFAULT-VALUE}")
    int actionDuration = 20;

    @CommandLine.Option(names = "-NP", description = "show progress bars during dinner, default=${DEFAULT-VALUE}")
    boolean showProgress = true;

    public static void main(String[] args) {
        AnsiFormat format = new AnsiFormat(RED_TEXT(), GREEN_BACK(), BOLD());
        String threadName = StringUtils.leftPad("Dinner", Identity.MAX_LENGTH);
        Thread.currentThread().setName(format.format(threadName));
        int exitCode = new CommandLine(new DinnerApp()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        log.info("Initialize dinner");

        Settings settings = Settings.builder()
                .seatCount(philosophersCount)
                .durationSeconds(dinnerDuration)
                .actionDurationMillis(actionDuration)
                .showProgress(showProgress)
                .build();

        Dinner dinner = strategy.getInitiator().apply(settings);
        dinner.init();
        dinner.start();
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
