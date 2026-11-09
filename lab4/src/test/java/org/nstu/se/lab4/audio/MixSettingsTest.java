package org.nstu.se.lab4.audio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

final class MixSettingsTest {
    private static final double UPDATED_AMPLITUDE = 0.75;
    private static final double UPDATED_DELAY_SECONDS = 0.25;
    private static final double DEFAULT_AMPLITUDE = 0.5;
    private static final double DEFAULT_DELAY_SECONDS = 0.0;
    private static final int DEFAULT_SOURCE_COUNT = 2;
    private static final String AMPLITUDE_MESSAGE = "Amplitude";
    private static final String DELAY_MESSAGE = "Delay";

    @Test
    void createsDefaultSettingsWithTwoSources() {
        checkDefaultSettings(MixSettings.createDefault());
    }

    @Test
    void updatesAmplitudeWithoutMutatingOriginalSettings() {
        final var settings = MixSettings.createDefault();
        final var updated = settings.withAmplitude(0, UPDATED_AMPLITUDE);

        checkAmplitudeUpdate(settings, updated);
    }

    @Test
    void updatesDelayWithoutMutatingOriginalSettings() {
        final var settings = MixSettings.createDefault();
        final var updated = settings.withDelaySeconds(0, UPDATED_DELAY_SECONDS);

        checkDelayUpdate(settings, updated);
    }

    @Test
    void rejectsAmplitudeBelowMinimum() {
        final var settings = MixSettings.createDefault();

        assertThatThrownBy(() -> settings.withAmplitude(0, -0.1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(AMPLITUDE_MESSAGE);
    }

    @Test
    void rejectsAmplitudeAboveMaximum() {
        final var settings = MixSettings.createDefault();

        assertThatThrownBy(() -> settings.withAmplitude(0, 1.1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(AMPLITUDE_MESSAGE);
    }

    @Test
    void rejectsNegativeDelay() {
        final var settings = MixSettings.createDefault();

        assertThatThrownBy(() -> settings.withDelaySeconds(0, -0.1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(DELAY_MESSAGE);
    }

    private static void checkDefaultSettings(final MixSettings settings) {
        assertThat(settings.getSourceCount()).isEqualTo(DEFAULT_SOURCE_COUNT);
        assertThat(settings.getAmplitude(0)).isEqualTo(DEFAULT_AMPLITUDE);
        assertThat(settings.getAmplitude(1)).isEqualTo(DEFAULT_AMPLITUDE);
        assertThat(settings.getDelaySeconds(0)).isEqualTo(DEFAULT_DELAY_SECONDS);
        assertThat(settings.getDelaySeconds(1)).isEqualTo(DEFAULT_DELAY_SECONDS);
        assertThat(settings.channels().get(0).source().getSampleCount()).isPositive();
        assertThat(settings.channels().get(1).source().getSampleCount()).isPositive();
    }

    private static void checkAmplitudeUpdate(
            final MixSettings original, final MixSettings updated) {
        assertThat(original.getAmplitude(0)).isEqualTo(DEFAULT_AMPLITUDE);
        assertThat(original.getDelaySeconds(0)).isEqualTo(DEFAULT_DELAY_SECONDS);
        assertThat(updated.getAmplitude(0)).isEqualTo(UPDATED_AMPLITUDE);
        assertThat(updated.getAmplitude(1)).isEqualTo(DEFAULT_AMPLITUDE);
    }

    private static void checkDelayUpdate(final MixSettings original, final MixSettings updated) {
        assertThat(original.getDelaySeconds(0)).isEqualTo(DEFAULT_DELAY_SECONDS);
        assertThat(original.getAmplitude(0)).isEqualTo(DEFAULT_AMPLITUDE);
        assertThat(updated.getDelaySeconds(0)).isEqualTo(UPDATED_DELAY_SECONDS);
        assertThat(updated.getDelaySeconds(1)).isEqualTo(DEFAULT_DELAY_SECONDS);
    }
}
