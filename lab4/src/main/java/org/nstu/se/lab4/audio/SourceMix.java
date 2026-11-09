package org.nstu.se.lab4.audio;

public final class SourceMix implements SignalSource {
    private static final int DEFAULT_SAMPLE_RATE = 44_100;
    private static final double DEFAULT_DURATION_SECONDS = 5.0;
    private static final double FIRST_FREQUENCY = 440.0;
    private static final double SECOND_FREQUENCY = 660.0;
    private static final String FIRST_RESOURCE = "/org/nstu/se/lab4/sample1.wav";
    private static final String SECOND_RESOURCE = "/org/nstu/se/lab4/sample2.wav";

    private final SignalProcessor[] processors;
    private final double[] amplitudes;
    private final double[] delays;

    public SourceMix() {
        final var first = new SignalProcessor(loadOrGenerate(FIRST_RESOURCE, FIRST_FREQUENCY));
        final var second = new SignalProcessor(loadOrGenerate(SECOND_RESOURCE, SECOND_FREQUENCY));
        this.processors = new SignalProcessor[] {first, second};
        this.amplitudes = new double[] {1.0, 1.0};
        this.delays = new double[] {0.0, 0.0};
    }

    public int getSourceCount() {
        return processors.length;
    }

    public void setAmplitude(final int index, final double amplitude) {
        amplitudes[index] = amplitude;
        final var processor = processors[index];
        processor.setAmplitude(amplitude);
    }

    public double getAmplitude(final int index) {
        return amplitudes[index];
    }

    public void setDelaySeconds(final int index, final double delaySeconds) {
        delays[index] = delaySeconds;
        final var processor = processors[index];
        processor.setDelaySeconds(delaySeconds);
    }

    public double getDelaySeconds(final int index) {
        return delays[index];
    }

    @Override
    public float getSample(final int index) {
        var sum = 0.0f;
        for (final var processor : processors) {
            sum += processor.getSample(index);
        }
        return Math.clamp(sum, -1.0f, 1.0f);
    }

    @Override
    public int getSampleCount() {
        var max = 0;
        for (final var processor : processors) {
            max = Math.max(max, processor.getSampleCount());
        }
        return max;
    }

    @Override
    public int getSampleRate() {
        if (processors.length == 0) {
            return 0;
        }
        return processors[0].getSampleRate();
    }

    private static WaveSource loadOrGenerate(final String resourcePath, final double frequency) {
        if (WaveSource.class.getResource(resourcePath) != null) {
            return WaveSource.fromResource(resourcePath);
        }
        return WaveSource.generateTone(frequency, DEFAULT_DURATION_SECONDS, DEFAULT_SAMPLE_RATE);
    }
}
