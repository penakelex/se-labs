package org.nstu.se.lab1.model;

import java.util.Arrays;
import java.util.Objects;

public final class SortArray<T extends Comparable<? super T>> {
    private final T[] values;

    @SafeVarargs
    private SortArray(final T... source) {
        this.values = source.clone();
    }

    @SafeVarargs
    public static <T extends Comparable<? super T>> SortArray<T> from(final T... input) {
        Objects.requireNonNull(input, "input");
        return new SortArray<>(input);
    }

    public int size() {
        return values.length;
    }

    public T get(final int index) {
        Objects.checkIndex(index, values.length);
        return values[index];
    }

    T[] toArray() {
        return values.clone();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof SortArray<?> other && Arrays.equals(values, other.values);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }

    @Override
    public String toString() {
        return Arrays.toString(values);
    }
}
