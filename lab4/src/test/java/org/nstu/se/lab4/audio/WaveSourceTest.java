package org.nstu.se.lab4.audio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

final class WaveSourceTest {
    private static final int SAMPLE_RATE = 100;
    private static final double FREQUENCY = 25.0;
    private static final double DURATION_SECONDS = 1.0;
    private static final float SAMPLE_TOLERANCE = 0.0001f;
    private static final String MISSING_RESOURCE = "/org/nstu/se/lab4/missing.wav";

    @Test
    void generatesToneWithExpectedShape() {
        checkTone(WaveSource.generateTone(FREQUENCY, DURATION_SECONDS, SAMPLE_RATE));
    }

    @Test
    void returnsZeroOutsideRange() {
        checkOutOfRangeSamples(WaveSource.generateTone(FREQUENCY, DURATION_SECONDS, SAMPLE_RATE));
    }

    @Test
    void rejectsMissingResource() {
        assertThatThrownBy(() -> WaveSource.fromResource(MISSING_RESOURCE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Resource not found");
    }

    private static void checkTone(final WaveSource source) {
        assertThat(source.getSampleRate()).isEqualTo(SAMPLE_RATE);
        assertThat(source.getSampleCount()).isEqualTo(SAMPLE_RATE);
        assertThat(source.getSample(0)).isEqualTo(0.0f);
        assertThat(source.getSample(1)).isCloseTo(1.0f, within(SAMPLE_TOLERANCE));
        assertThat(source.getSample(3)).isCloseTo(-1.0f, within(SAMPLE_TOLERANCE));

        for (var index = 0; index < source.getSampleCount(); index++) {
            assertThat(source.getSample(index)).isBetween(-1.0f, 1.0f);
        }
    }

    private static void checkOutOfRangeSamples(final WaveSource source) {
        assertThat(source.getSample(-1)).isEqualTo(0.0f);
        assertThat(source.getSample(SAMPLE_RATE)).isEqualTo(0.0f);
    }
}
