package org.nstu.se.lab4.audio;

public final class SignalProcessor implements SignalSource {
    private static final double MIN_AMPLITUDE = 0.0;
    private static final double MIN_DELAY_SECONDS = 0.0;

    private final SignalSource source;
    private final double amplitude;
    private final int delaySamples;

    public SignalProcessor(
            final SignalSource source, final double amplitude, final double delaySeconds) {
        if (amplitude < MIN_AMPLITUDE) {
            throw new IllegalArgumentException("Amplitude must be non-negative");
        }
        if (delaySeconds < MIN_DELAY_SECONDS) {
            throw new IllegalArgumentException("Delay must be non-negative");
        }
        this.source = source;
        this.amplitude = amplitude;
        this.delaySamples = (int) Math.round(delaySeconds * source.getSampleRate());
    }

    @Override
    public float getSample(final int index) {
        final var sourceIndex = index - delaySamples;
        if (sourceIndex < 0 || sourceIndex >= source.getSampleCount()) {
            return 0.0f;
        }
        return (float) (amplitude * source.getSample(sourceIndex));
    }

    @Override
    public int getSampleCount() {
        return delaySamples + source.getSampleCount();
    }

    @Override
    public int getSampleRate() {
        return source.getSampleRate();
    }
}
