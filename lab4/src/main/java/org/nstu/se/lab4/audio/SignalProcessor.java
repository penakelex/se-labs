package org.nstu.se.lab4.audio;

public final class SignalProcessor implements SignalSource {
    private final SignalSource source;
    private double amplitude = 1.0;
    private int delaySamples;

    public SignalProcessor(final SignalSource source) {
        this.source = source;
    }

    public void setAmplitude(final double amplitude) {
        this.amplitude = amplitude;
    }

    public void setDelaySeconds(final double delaySeconds) {
        this.delaySamples = (int) Math.round(delaySeconds * source.getSampleRate());
    }

    public double getAmplitude() {
        return amplitude;
    }

    public double getDelaySeconds() {
        return (double) delaySamples / source.getSampleRate();
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
        return source.getSampleCount();
    }

    @Override
    public int getSampleRate() {
        return source.getSampleRate();
    }
}
