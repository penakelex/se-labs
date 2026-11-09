package org.nstu.se.lab4.audio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

final class SignalProcessorTest {
    private static final float SAMPLE_TOLERANCE = 0.0001f;
    private static final double AMPLITUDE = 0.5;
    private static final double DELAY_SECONDS = 0.2;
    private static final int SAMPLE_RATE = 10;
    private static final String AMPLITUDE_MESSAGE = "Amplitude";
    private static final String DELAY_MESSAGE = "Delay";

    @Test
    void appliesAmplitudeAndDelay() {
        final var source = new FixedSignalSource(new float[] {0.1f, 0.2f, 0.3f}, SAMPLE_RATE);
        final var processor = new SignalProcessor(source, AMPLITUDE, DELAY_SECONDS);

        checkDelayedAmplifiedSignal(processor);
    }

    @Test
    void returnsZeroForNegativeIndex() {
        final var source = new FixedSignalSource(new float[] {0.4f}, SAMPLE_RATE);
        final var processor = new SignalProcessor(source, AMPLITUDE, DELAY_SECONDS);

        assertThat(processor.getSample(-1)).isEqualTo(0.0f);
    }

    @Test
    void returnsZeroAfterSourceEnds() {
        final var source = new FixedSignalSource(new float[] {0.4f}, SAMPLE_RATE);
        final var processor = new SignalProcessor(source, AMPLITUDE, DELAY_SECONDS);

        assertThat(processor.getSample(processor.getSampleCount())).isEqualTo(0.0f);
    }

    @Test
    void rejectsAmplitudeBelowMinimum() {
        final var source = new FixedSignalSource(new float[] {0.4f}, SAMPLE_RATE);

        assertThatThrownBy(() -> new SignalProcessor(source, -0.1, 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(AMPLITUDE_MESSAGE);
    }

    @Test
    void rejectsAmplitudeAboveMaximum() {
        final var source = new FixedSignalSource(new float[] {0.4f}, SAMPLE_RATE);

        assertThatThrownBy(() -> new SignalProcessor(source, 1.1, 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(AMPLITUDE_MESSAGE);
    }

    @Test
    void rejectsNegativeDelay() {
        final var source = new FixedSignalSource(new float[] {0.4f}, SAMPLE_RATE);

        assertThatThrownBy(() -> new SignalProcessor(source, AMPLITUDE, -0.1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(DELAY_MESSAGE);
    }

    private static void checkDelayedAmplifiedSignal(final SignalProcessor processor) {
        assertThat(processor.getSampleRate()).isEqualTo(SAMPLE_RATE);
        assertThat(processor.getSampleCount()).isEqualTo(5);
        assertThat(processor.getSample(0)).isEqualTo(0.0f);
        assertThat(processor.getSample(1)).isEqualTo(0.0f);
        assertThat(processor.getSample(2)).isCloseTo(0.05f, within(SAMPLE_TOLERANCE));
        assertThat(processor.getSample(3)).isCloseTo(0.1f, within(SAMPLE_TOLERANCE));
        assertThat(processor.getSample(4)).isCloseTo(0.15f, within(SAMPLE_TOLERANCE));
    }
}
