package org.solveme.philosophers;

import com.diogonunes.jcolor.AnsiFormat;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solveme.philosophers.strategies.Strategy;
import picocli.CommandLine;

import static com.diogonunes.jcolor.Attribute.*;


@CommandLine.Command(name = "dinner")
public class DinnerApp implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(DinnerApp.class);

    @CommandLine.Parameters(index = "0", description = "available strategies: ${COMPLETION-CANDIDATES}")
    Strategy strategy;

    @CommandLine.Option(names = "-c", paramLabel = "COUNT", description = "how many philosophers should be invited")
    int philosophersCount = 5;

    @CommandLine.Option(names = "-d", paramLabel = "SECONDS", description = "dinner duration in seconds")
    int duration = 10;

    public static void main(String[] args) {
        AnsiFormat format = new AnsiFormat(RED_TEXT(), GREEN_BACK(), BOLD());
        String threadName = StringUtils.leftPad("Dinner", Philosopher.Name.MAX_LENGTH);
        Thread.currentThread().setName(format.format(threadName));
        int exitCode = new CommandLine(new DinnerApp()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        log.info("Initialize dinner");

        Dinner.Settings settings = Dinner.Settings.builder()
                .seatCount(philosophersCount)
                .durationSeconds(duration)
                .build();

        Dinner dinner = strategy.getInitiator().apply(settings);
        dinner.init();
        dinner.start();
    }

}
