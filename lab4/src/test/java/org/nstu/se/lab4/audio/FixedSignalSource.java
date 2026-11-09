package org.nstu.se.lab4.audio;

final class FixedSignalSource implements SignalSource {
    private final float[] samples;
    private final int sampleRate;

    FixedSignalSource(final float[] samples, final int sampleRate) {
        this.samples = samples.clone();
        this.sampleRate = sampleRate;
    }

    @Override
    public float getSample(final int index) {
        if (index < 0 || index >= samples.length) {
            return 0.0f;
        }

        return samples[index];
    }

    @Override
    public int getSampleCount() {
        return samples.length;
    }

    @Override
    public int getSampleRate() {
        return sampleRate;
    }
}
