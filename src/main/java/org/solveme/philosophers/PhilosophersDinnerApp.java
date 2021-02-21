package org.solveme.philosophers;

import com.diogonunes.jcolor.AnsiFormat;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solveme.philosophers.dinners.CivilizedDinner;
import picocli.CommandLine;

import static com.diogonunes.jcolor.Attribute.*;


@CommandLine.Command
public class PhilosophersDinnerApp implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(PhilosophersDinnerApp.class);

    public static void main(String[] args) {
        AnsiFormat format = new AnsiFormat(RED_TEXT(), GREEN_BACK(), BOLD());
        String threadName = StringUtils.leftPad("Dinner", Philosopher.Name.MAX_LENGTH);
        Thread.currentThread().setName(format.format(threadName));
        int exitCode = new CommandLine(new PhilosophersDinnerApp()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        log.info("Initialize dinner");

        Dinner.Settings settings = Dinner.Settings.builder()
                .seatCount(Philosopher.Name.values().length)
                .durationSeconds(10)
                .build();

        CivilizedDinner dinner = new CivilizedDinner(settings);
        dinner.init();
        dinner.start();
    }

}
