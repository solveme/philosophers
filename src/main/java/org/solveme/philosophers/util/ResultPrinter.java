package org.solveme.philosophers.util;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.solveme.philosophers.Philosopher;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

import static org.solveme.philosophers.util.Util.OUT;


@RequiredArgsConstructor
public class ResultPrinter {

    private final List<Philosopher.Result> results;
    private final int nameColWidth;
    private final int eatingColWidth;
    private final int thinkingColWidth;
    private final int wastedColWidth;
    private final int totalColWidth;

    public static ResultPrinter from(List<Philosopher.Result> results) {
        int nameLength = Philosopher.Name.MAX_LENGTH;
        int eatingLength = resolveMaxLength(results, Philosopher.Result::getEatingDuration);
        int thinkingLength = resolveMaxLength(results, Philosopher.Result::getThinkingDuration);
        int wastedLength = resolveMaxLength(results, Philosopher.Result::getWastedDuration);
        int totalLength = resolveMaxLength(results, Philosopher.Result::getTotalDuration);

        return new ResultPrinter(
                results,
                nameLength,
                eatingLength,
                thinkingLength,
                wastedLength,
                totalLength
        );
    }


    private static int resolveMaxLength(List<Philosopher.Result> results,
                                        Function<Philosopher.Result, Duration> durationExtractor
    ) {
        int maxValLength = results.stream()
                .map(durationExtractor)
                .map(ResultPrinter::durationMapper)
                .max(Long::compare)
                .map(n -> n.toString().length())
                .orElse(10);

        return Math.max(maxValLength, 10);
    }

    public ResultPrinter print() {
        printHeader();
        results.forEach(this::printPhilosopherResult);
        return this;
    }

    public void printHeader() {
        row()
                .appendPadded("Eating", nameColWidth + eatingColWidth)
                .appendPadded("Thinking", thinkingColWidth)
                .appendPadded("Wasted", wastedColWidth)
                .appendPadded("Total", totalColWidth)
                .print();
    }

    public void printPhilosopherResult(Philosopher.Result result) {
        row()
                .append(formatName(result.getName().toString()))
                .append(formatEating(result.getEatingDuration()))
                .append(formatThinking(result.getThinkingDuration()))
                .append(formatWasted(result.getWastedDuration()))
                .append(formatTotal(result.getTotalDuration()))
                .print();
    }

    public void printTotalResult() {
        row()
                .appendPadded("Eating", nameColWidth + eatingColWidth)
                .appendPadded("Thinking", thinkingColWidth)
                .appendPadded("Wasted", wastedColWidth)
                .appendPadded("Total", totalColWidth)
                .print();
    }

    private String formatName(String name) {
        return padColumn(name, nameColWidth);
    }

    private String formatEating(Duration eating) {
        return padColumn(formatDuration(eating), eatingColWidth);
    }

    private String formatThinking(Duration thinking) {
        return padColumn(formatDuration(thinking), thinkingColWidth);
    }

    private String formatWasted(Duration wasted) {
        return padColumn(formatDuration(wasted), wastedColWidth);
    }

    private String formatTotal(Duration total) {
        return padColumn(formatDuration(total), totalColWidth);
    }

    // Helpers

    public ResultPrinter printNewLine() {
        OUT.println();
        return this;
    }

    private static String padColumn(String value, int desiredWidth) {
        return StringUtils.leftPad(value, desiredWidth);
    }

    private static String formatDuration(Duration duration) {
        return String.valueOf(durationMapper(duration));
    }

    private static long durationMapper(Duration duration) {
        return duration.toMillis();
    }

    private static RowBuilder row() {
        return new RowBuilder();
    }

    public static class RowBuilder {

        private final StringBuilder row = new StringBuilder();

        public String build() {
            return row.toString();
        }

        public void print() {
            OUT.println(build());
        }

        public RowBuilder append(String value) {
            row.append(value);
            return this;
        }

        public RowBuilder appendPadded(String value, int desiredWidth) {
            row.append(padColumn(value, desiredWidth));
            return this;
        }

    }

}
