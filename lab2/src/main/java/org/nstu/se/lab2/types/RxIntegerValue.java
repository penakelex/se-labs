package org.nstu.se.lab2.types;

public final class RxIntegerValue {
    private final int value;

    public RxIntegerValue(final int value) {
        this.value = value;
    }

    public int get() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
