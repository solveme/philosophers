package org.solveme.philosophers.results;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

import static org.solveme.philosophers.util.Util.OUT;


@RequiredArgsConstructor
public abstract class ResultTable<R> {

    protected final List<R> values;
    protected final Duration dinnerDuration;

    public void print() {
        printHeader();
        values.forEach(this::printResultRow);
    }

    protected abstract void printHeader();

    protected abstract void printResultRow(R result);

    protected ResultTable.RowBuilder row() {
        return new ResultTable.RowBuilder();
    }

    public static class RowBuilder {

        private final StringBuilder row = new StringBuilder();

        public String build() {
            return row.toString();
        }

        public void print() {
            OUT.println(build());
        }

        public ResultTable.RowBuilder append(String value) {
            row.append(value);
            return this;
        }

        public ResultTable.RowBuilder appendPadded(String value, int desiredWidth) {
            row.append(StringUtils.leftPad(value, desiredWidth));
            return this;
        }

        public ResultTable.RowBuilder with(Consumer<ResultTable.RowBuilder> modification) {
            modification.accept(this);
            return this;
        }

    }

}
