package org.nstu.se.lab3.model;

public final class LogScaleModel {
    private final int zeroLevelValue;
    private final double minDecibels;
    private final double maxDecibels;

    public LogScaleModel(
            final int zeroLevelValue, final double minDecibels, final double maxDecibels) {
        this.zeroLevelValue = Math.max(1, zeroLevelValue);
        this.minDecibels = Math.min(minDecibels, maxDecibels);
        this.maxDecibels = Math.max(minDecibels, maxDecibels);
    }

    public double calculateDecibels(final int value) {
        if (value <= 0) {
            return minDecibels;
        }

        final var rawDecibels = 20.0 * Math.log10((double) value / zeroLevelValue);
        return Math.clamp(rawDecibels, minDecibels, maxDecibels);
    }

    public int calculateValueFromDecibels(final double decibels) {
        final var clampedDecibels = Math.clamp(decibels, minDecibels, maxDecibels);
        final var value = zeroLevelValue * Math.pow(10.0, clampedDecibels / 20.0);
        return (int) Math.round(value);
    }

    public int getZeroLevelValue() {
        return zeroLevelValue;
    }

    public double getMinDecibels() {
        return minDecibels;
    }

    public double getMaxDecibels() {
        return maxDecibels;
    }
}
