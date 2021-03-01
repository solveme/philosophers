package org.solveme.philosophers.results;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.solveme.philosophers.Identity;
import org.solveme.philosophers.Philosopher;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
public class PhilosopherResults extends ResultTable<Philosopher.Result> {

    private final NameColumn nameColumn = new NameColumn();
    private final List<DurationColumn<Philosopher.Result>> durationColumns;

    public PhilosopherResults(@Nonnull List<Philosopher.Result> values,
                              @Nonnull Duration dinnerDuration,
                              @Nonnull List<DurationColumn<Philosopher.Result>> durationColumns
    ) {
        super(values, dinnerDuration);
        this.durationColumns = durationColumns;
    }

    public static PhilosopherResults from(@Nonnull List<Philosopher.Result> results,
                                          @Nonnull Duration dinnerDuration
    ) {
        List<DurationColumn<Philosopher.Result>> columns = Arrays.asList(
                column().build("Eating", DurationColumn.Unit.MILLIS, results, Philosopher.Result::getEatingDuration),
                column().build("Thinking", DurationColumn.Unit.MILLIS, results, Philosopher.Result::getThinkingDuration),
                column().build("Burden", DurationColumn.Unit.MILLIS, results, Philosopher.Result::getIdleDuration),
                column().build("Total", DurationColumn.Unit.MILLIS, results, Philosopher.Result::getTotalDuration)
        );

        return new PhilosopherResults(results.stream().sorted().collect(Collectors.toList()), dinnerDuration, columns);
    }

    @Override
    protected void printHeader() {
        row()
                .append(nameColumn.getHeader())
                .with(rb -> durationColumns.forEach(dc -> rb.append(dc.getHeader())))
                .print();
    }

    @Override
    protected void printResultRow(Philosopher.Result result) {
        row()
                .append(nameColumn.formatResult(result))
                .with(rb -> durationColumns.forEach(dc -> rb.append(dc.formatResult(result))))
                .print();
    }

    // Helpers

    private static DurationColumn.DurationColumnBuilder<Philosopher.Result> column() {
        return DurationColumn.builder();
    }

    static class NameColumn extends ResultColumn<Philosopher.Result, Identity> {

        public NameColumn() {
            super(Philosopher.Result::getIdentity);
        }

        @Override
        public int getWidth() {
            return Identity.MAX_LENGTH;
        }

        @Override
        public String getHeader() {
            return StringUtils.rightPad(" ", getWidth());
        }

        @Override
        public String formatValue(Identity name) {
            return StringUtils.rightPad(name.toString(), getWidth());
        }

    }

}
