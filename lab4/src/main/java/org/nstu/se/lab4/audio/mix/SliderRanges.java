package org.nstu.se.lab4.audio.mix;

import org.jspecify.annotations.Nullable;

public record SliderRanges(
        double minAmplitude, double maxAmplitude, double minDelaySeconds, double maxDelaySeconds) {
    private static final double ABSOLUTE_MIN_AMPLITUDE = 0.0;
    private static final double ABSOLUTE_MAX_AMPLITUDE = 1.0;
    private static final double ABSOLUTE_MIN_DELAY_SECONDS = 0.0;
    private static final double ABSOLUTE_MAX_DELAY_SECONDS = 30.0;

    public SliderRanges {
        if (minAmplitude < ABSOLUTE_MIN_AMPLITUDE
                || maxAmplitude > ABSOLUTE_MAX_AMPLITUDE
                || minAmplitude > maxAmplitude) {
            throw new IllegalArgumentException("Amplitude range must be within 0.0..1.0");
        }

        if (minDelaySeconds < ABSOLUTE_MIN_DELAY_SECONDS
                || maxDelaySeconds > ABSOLUTE_MAX_DELAY_SECONDS
                || minDelaySeconds > maxDelaySeconds) {
            throw new IllegalArgumentException("Delay range must be within 0..30 seconds");
        }
    }

    public static SliderRanges defaultRanges() {
        return new SliderRanges(0.0, 1.0, 0.0, 2.0);
    }

    public static @Nullable SliderRanges tryCreate(
            final @Nullable Double minAmplitude,
            final @Nullable Double maxAmplitude,
            final @Nullable Double minDelaySeconds,
            final @Nullable Double maxDelaySeconds) {
        if (minAmplitude == null
                || maxAmplitude == null
                || minDelaySeconds == null
                || maxDelaySeconds == null) {
            return null;
        }

        try {
            return new SliderRanges(minAmplitude, maxAmplitude, minDelaySeconds, maxDelaySeconds);
        } catch (final IllegalArgumentException exception) {
            return null;
        }
    }

    public double clampAmplitude(final double amplitude) {
        return Math.clamp(amplitude, minAmplitude, maxAmplitude);
    }

    public double clampDelaySeconds(final double delaySeconds) {
        return Math.clamp(delaySeconds, minDelaySeconds, maxDelaySeconds);
    }
}
