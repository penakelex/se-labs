package org.nstu.se.lab4.ui;

import org.nstu.se.lab4.audio.source.SignalSource;

/** Min/max/mid amplitude envelope of a signal at a fixed bucket resolution. */
record WaveformEnvelope(float[] minValues, float[] maxValues, float[] midValues) {
    private static final int BUCKET_COUNT = 4096;

    static WaveformEnvelope empty() {
        return new WaveformEnvelope(new float[0], new float[0], new float[0]);
    }

    boolean isEmpty() {
        return minValues.length == 0;
    }

    static WaveformEnvelope compute(final SignalSource source) {
        final var sampleCount = source.getSampleCount();
        final var minValues = new float[BUCKET_COUNT];
        final var maxValues = new float[BUCKET_COUNT];
        final var midValues = new float[BUCKET_COUNT];
        var bucketStart = 0;

        for (var bucket = 0; bucket < BUCKET_COUNT; bucket++) {
            final var bucketEnd = bucketEndFor(bucket, bucketStart, sampleCount);
            final float[] samples =
                    bucketStart < sampleCount
                            ? bucketSamples(source, bucketStart, bucketEnd)
                            : new float[] {0.0f, 0.0f, 0.0f};

            minValues[bucket] = samples[0];
            maxValues[bucket] = samples[1];
            midValues[bucket] = samples[2];
            bucketStart = bucketEnd;
        }

        return new WaveformEnvelope(minValues, maxValues, midValues);
    }

    private static int bucketEndFor(
            final int bucket, final int bucketStart, final int sampleCount) {
        final var proportionalEnd = (int) ((double) (bucket + 1) / BUCKET_COUNT * sampleCount);
        return Math.clamp(proportionalEnd, bucketStart + 1, sampleCount);
    }

    private static float[] bucketSamples(
            final SignalSource source, final int fromIndex, final int toIndex) {
        var minimum = Float.MAX_VALUE;
        var maximum = -Float.MAX_VALUE;

        for (var index = fromIndex; index < toIndex; index++) {
            final var sample = source.getSample(index);

            if (sample < minimum) {
                minimum = sample;
            }

            if (sample > maximum) {
                maximum = sample;
            }
        }

        final var midIndex = (fromIndex + toIndex) / 2;
        final var midSample = source.getSample(midIndex);

        return new float[] {minimum, maximum, midSample};
    }
}
