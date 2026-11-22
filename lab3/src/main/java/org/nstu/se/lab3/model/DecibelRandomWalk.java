package org.nstu.se.lab3.model;

import java.util.random.RandomGenerator;

public final class DecibelRandomWalk {
    private final RandomGenerator random;
    private final double minDecibels;
    private final double maxDecibels;
    private double stepDb;

    public DecibelRandomWalk(
            final RandomGenerator random,
            final double minDecibels,
            final double maxDecibels,
            final double stepDb) {
        this.random = random;
        this.minDecibels = Math.min(minDecibels, maxDecibels);
        this.maxDecibels = Math.max(minDecibels, maxDecibels);
        this.stepDb = stepDb;
    }

    public void setStepDb(final double stepDb) {
        this.stepDb = stepDb;
    }

    public double next(final double currentDecibels) {
        final var offset = (random.nextDouble() * 2.0 - 1.0) * stepDb;
        final var raw = currentDecibels + offset;

        if (raw < minDecibels) {
            return Math.min(maxDecibels, 2.0 * minDecibels - raw);
        }

        if (raw > maxDecibels) {
            return Math.max(minDecibels, 2.0 * maxDecibels - raw);
        }

        return raw;
    }
}
