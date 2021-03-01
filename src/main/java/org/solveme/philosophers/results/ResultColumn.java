package org.solveme.philosophers.results;

import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;


@RequiredArgsConstructor
public abstract class ResultColumn<R, T> {

    protected final Function<R, T> mapper;

    public abstract int getWidth();

    public abstract String getHeader();

    public abstract String formatValue(T value);

    public String formatResult(R result) {
        return formatValue(mapper.apply(result));
    }

    public static <R> Optional<Integer> calculateMaxWidthOf(Collection<R> values, Function<R, String> formatter) {
        return values.stream()
                .map(value -> formatter.apply(value).length())
                .max(Comparator.naturalOrder());
    }

}
