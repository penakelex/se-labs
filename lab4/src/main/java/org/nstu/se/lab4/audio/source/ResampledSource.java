package org.nstu.se.lab4.audio.source;

public final class ResampledSource implements SignalSource {
    private final SignalSource source;
    private final int targetSampleRate;
    private final int sampleCount;

    public ResampledSource(final SignalSource source, final int targetSampleRate) {
        if (source.getSampleRate() <= 0 || targetSampleRate <= 0) {
            throw new IllegalArgumentException("Sample rates must be positive");
        }

        this.source = source;
        this.targetSampleRate = targetSampleRate;
        this.sampleCount =
                (int)
                        Math.ceil(
                                (double) source.getSampleCount()
                                        * targetSampleRate
                                        / source.getSampleRate());
    }

    @Override
    public float getSample(final int index) {
        if (index < 0 || index >= sampleCount) {
            return 0.0f;
        }

        final var position = (double) index * source.getSampleRate() / targetSampleRate;
        final var firstIndex = (int) position;
        final var fraction = position - firstIndex;
        final var first = source.getSample(firstIndex);
        final var second = source.getSample(firstIndex + 1);

        return (float) (first + (second - first) * fraction);
    }

    @Override
    public int getSampleCount() {
        return sampleCount;
    }

    @Override
    public int getSampleRate() {
        return targetSampleRate;
    }
}
