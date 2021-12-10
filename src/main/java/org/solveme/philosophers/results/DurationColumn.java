package org.solveme.philosophers.results;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.ToLongFunction;


public class DurationColumn<R> extends ResultColumn<R, Duration> {

    private final int width;
    private final String header;
    private final Unit unit;

    public DurationColumn(Function<R, Duration> mapper, int width, String header, Unit unit) {
        super(mapper);
        this.width = width;
        this.header = header;
        this.unit = unit;
    }

    public static <R> DurationColumnBuilder<R> builder() {
        return new DurationColumnBuilder<>();
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public String getHeader() {
        return StringUtils.leftPad(header + '[' + unit.getHint() + ']', getWidth());
    }

    @Override
    public String formatValue(Duration duration) {
        return StringUtils.leftPad(unit.format(duration), getWidth());
    }

    @RequiredArgsConstructor
    @Getter
    public enum Unit {
        SECONDS(TimeUnit.SECONDS, "s", Duration::getSeconds),
        MILLIS(TimeUnit.MILLISECONDS, "ms", Duration::toMillis),
        MICROS(TimeUnit.MICROSECONDS, "\u03bcs", d -> TimeUnit.NANOSECONDS.toMicros(d.toNanos())),
        NANOS(TimeUnit.NANOSECONDS, "ns", Duration::toNanos);

        private final TimeUnit timeUnit;
        private final String hint;
        private final ToLongFunction<Duration> mapper;

        private static final NumberFormat SMALL_VALUES_FORMATTER = new DecimalFormat("#.##");
        private static final NumberFormat MEDIUM_VALUES_FORMATTER = new DecimalFormat("#.#");

        public Unit getNextUnit() {
            return this.equals(NANOS)
                    ? NANOS
                    : Unit.values()[this.ordinal() + 1];
        }

        public String format(Duration duration) {
            long units = mapper.applyAsLong(duration);

            if (units < 10) {
                return SMALL_VALUES_FORMATTER.format(getNextUnit().getMapper().applyAsLong(duration) / 1000D);
            }

            if (units < 100) {
                return MEDIUM_VALUES_FORMATTER.format(getNextUnit().getMapper().applyAsLong(duration) / 1000D);
            }

            return String.valueOf(units);
        }

    }


    public static class DurationColumnBuilder<R> {

        private int minWidth = -1;
        private int padding = 2;

        public DurationColumnBuilder<R> minWidth(int width) {
            this.minWidth = width;
            return this;
        }

        public DurationColumnBuilder<R> padding(int padding) {
            this.padding = padding;
            return this;
        }

        public DurationColumn<R> build(@Nonnull String header,
                                       @Nonnull Unit unit,
                                       @Nonnull Collection<R> values,
                                       @Nonnull Function<R, Duration> mapper
        ) {
            int finalWidth = calculateMaxWidthOf(values, r -> unit.format(mapper.apply(r)))
                    .map(w -> Math.max(w, Math.max((header + unit.getHint() + 2).length(), minWidth)))
                    .map(w -> w + padding)
                    .orElse(10);

            return new DurationColumn<>(mapper, finalWidth, header, unit);
        }

    }


}
