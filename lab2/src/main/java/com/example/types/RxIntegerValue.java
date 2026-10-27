package com.example.types;

public class RxIntegerValue {
    private final int value;

    public RxIntegerValue(int value) {
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
