package org.solveme.philosophers.results;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.solveme.philosophers.Fork;
import org.solveme.philosophers.Identity;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;


@Slf4j
public class ForkResults extends ResultTable<Fork.Result> {

    private final IdColumn idColumn = new IdColumn();
    private final List<DurationColumn<Fork.Result>> durationColumns;

    public static ForkResults from(@Nonnull List<Fork.Result> results,
                                   @Nonnull Duration dinnerDuration
    ) {

        List<DurationColumn<Fork.Result>> columns = Arrays.asList(
                column().build("Left usage", DurationColumn.Unit.MILLIS, results, Fork.Result::getLeftUsageDuration),
                column().build("Right usage", DurationColumn.Unit.MILLIS, results, Fork.Result::getRightUsageDuration),
                column().build("Total usage", DurationColumn.Unit.MILLIS, results, Fork.Result::getTotalUsageDuration),
                column().build("Idle", DurationColumn.Unit.MILLIS, results, r -> dinnerDuration.minus(r.getTotalUsageDuration()))
        );

        return new ForkResults(results, dinnerDuration, columns);
    }

    public ForkResults(@Nonnull List<Fork.Result> values,
                       @Nonnull Duration dinnerDuration,
                       @Nonnull List<DurationColumn<Fork.Result>> durationColumns
    ) {
        super(values, dinnerDuration);
        this.durationColumns = durationColumns;
    }

    @Override
    protected void printHeader() {
        row()
                .append(idColumn.getHeader())
                .with(rb -> durationColumns.forEach(dc -> rb.append(dc.getHeader())))
                .print();
    }

    @Override
    protected void printResultRow(Fork.Result result) {
        row()
                .append(idColumn.formatResult(result))
                .with(rb -> durationColumns.forEach(dc -> rb.append(dc.formatResult(result))))
                .print();
    }

    // Helpers

    private static DurationColumn.DurationColumnBuilder<Fork.Result> column() {
        return DurationColumn.builder();
    }


    static class IdColumn extends ResultColumn<Fork.Result, Fork.Result> {

        public IdColumn() {
            super(Function.identity());
        }

        @Override
        public int getWidth() {
            return Identity.MAX_LENGTH * 2 + 5;
        }

        @Override
        public String getHeader() {
            return StringUtils.rightPad(" ", getWidth());
        }

        @Override
        public String formatValue(Fork.Result result) {
            String leftUser = result.getLeftUser().toString();
            String rightUser = result.getRightUser().toString();
            return StringUtils.rightPad("F" + result.getId() + " " + leftUser + " / " + rightUser, getWidth());
        }

    }

}
