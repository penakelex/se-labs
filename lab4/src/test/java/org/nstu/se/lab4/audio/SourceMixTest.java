package org.nstu.se.lab4.audio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

import java.util.List;

final class SourceMixTest {
    private static final float SAMPLE_TOLERANCE = 0.0001f;
    private static final int SAMPLE_RATE = 10;

    @Test
    void emptyMixIsSilentAndHasZeroRate() {
        final var mix = new SourceMix(List.of());

        checkEmptyMix(mix);
    }

    @Test
    void mixesSingleChannel() {
        final var source = new FixedSignalSource(new float[] {0.2f, -0.4f}, SAMPLE_RATE);
        final var channel = new SourceMix.Channel(source, 0.5, 0);
        final var mix = new SourceMix(List.of(channel));

        checkSingleChannel(mix);
    }

    @Test
    void mixesChannelsWithDelay() {
        final var firstSource = new FixedSignalSource(new float[] {0.2f, 0.2f, 0.2f}, SAMPLE_RATE);
        final var secondSource = new FixedSignalSource(new float[] {0.1f, 0.1f}, SAMPLE_RATE);
        final var firstChannel = new SourceMix.Channel(firstSource, 1.0, 0);
        final var secondChannel = new SourceMix.Channel(secondSource, 1.0, 1);
        final var mix = new SourceMix(List.of(firstChannel, secondChannel));

        checkDelayedMix(mix);
    }

    @Test
    void clampsMixedSignalToValidRange() {
        final var source = new FixedSignalSource(new float[] {0.8f, -0.8f}, SAMPLE_RATE);
        final var firstChannel = new SourceMix.Channel(source, 1.0, 0);
        final var secondChannel = new SourceMix.Channel(source, 1.0, 0);
        final var mix = new SourceMix(List.of(firstChannel, secondChannel));

        checkClampedMix(mix);
    }

    @Test
    void usesSampleRateOfFirstChannel() {
        final var firstSource = new FixedSignalSource(new float[] {0.1f}, 10);
        final var secondSource = new FixedSignalSource(new float[] {0.1f}, 20);
        final var firstChannel = new SourceMix.Channel(firstSource, 1.0, 0);
        final var secondChannel = new SourceMix.Channel(secondSource, 1.0, 0);
        final var mix = new SourceMix(List.of(firstChannel, secondChannel));

        assertThat(mix.getSampleRate()).isEqualTo(10);
    }

    private static void checkEmptyMix(final SourceMix mix) {
        assertThat(mix.getSampleRate()).isZero();
        assertThat(mix.getSampleCount()).isZero();
        assertThat(mix.getSample(0)).isEqualTo(0.0f);
    }

    private static void checkSingleChannel(final SourceMix mix) {
        assertThat(mix.getSampleRate()).isEqualTo(SAMPLE_RATE);
        assertThat(mix.getSampleCount()).isEqualTo(2);
        assertThat(mix.getSample(0)).isCloseTo(0.1f, within(SAMPLE_TOLERANCE));
        assertThat(mix.getSample(1)).isCloseTo(-0.2f, within(SAMPLE_TOLERANCE));
    }

    private static void checkDelayedMix(final SourceMix mix) {
        assertThat(mix.getSampleRate()).isEqualTo(SAMPLE_RATE);
        assertThat(mix.getSampleCount()).isEqualTo(3);
        assertThat(mix.getSample(0)).isCloseTo(0.2f, within(SAMPLE_TOLERANCE));
        assertThat(mix.getSample(1)).isCloseTo(0.3f, within(SAMPLE_TOLERANCE));
        assertThat(mix.getSample(2)).isCloseTo(0.3f, within(SAMPLE_TOLERANCE));
    }

    private static void checkClampedMix(final SourceMix mix) {
        assertThat(mix.getSample(0)).isEqualTo(1.0f);
        assertThat(mix.getSample(1)).isEqualTo(-1.0f);
    }
}
