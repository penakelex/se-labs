package org.nstu.se.lab4.audio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.nstu.se.lab4.audio.mix.SliderRanges;

final class SliderRangesTest {
    @Test
    void createsDefaultRanges() {
        final var ranges = SliderRanges.defaultRanges();
        assertThat(
                        java.util.List.of(
                                ranges.minAmplitude(),
                                ranges.maxAmplitude(),
                                ranges.minDelaySeconds(),
                                ranges.maxDelaySeconds()))
                .containsExactly(0.0, 1.0, 0.0, 2.0);
    }

    @Test
    void clampsValuesIntoRange() {
        final var ranges = new SliderRanges(0.2, 0.8, 0.5, 2.0);
        assertThat(
                        java.util.List.of(
                                ranges.clampAmplitude(0.0),
                                ranges.clampAmplitude(1.0),
                                ranges.clampDelaySeconds(0.0),
                                ranges.clampDelaySeconds(10.0)))
                .containsExactly(0.2, 0.8, 0.5, 2.0);
    }

    @Test
    void tryCreateReturnsNullForInvalidAmplitudeRange() {
        assertThat(SliderRanges.tryCreate(-0.1, 1.0, 0.0, 2.0)).isNull();
    }

    @Test
    void tryCreateReturnsNullForAmplitudeAboveOne() {
        assertThat(SliderRanges.tryCreate(0.0, 1.1, 0.0, 2.0)).isNull();
    }

    @Test
    void tryCreateReturnsNullForReversedAmplitudeRange() {
        assertThat(SliderRanges.tryCreate(0.8, 0.2, 0.0, 2.0)).isNull();
    }

    @Test
    void tryCreateReturnsNullForNegativeDelay() {
        assertThat(SliderRanges.tryCreate(0.0, 1.0, -1.0, 2.0)).isNull();
    }

    @Test
    void tryCreateReturnsNullForDelayAboveLimit() {
        assertThat(SliderRanges.tryCreate(0.0, 1.0, 0.0, 31.0)).isNull();
    }

    @Test
    void tryCreateReturnsRangesForValidInput() {
        assertThat(SliderRanges.tryCreate(0.0, 1.0, 0.0, 2.0)).isNotNull();
    }

    @Test
    void rejectsReversedAmplitudeRange() {
        assertThatThrownBy(() -> new SliderRanges(0.9, 0.1, 0.0, 2.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amplitude");
    }

    @Test
    void rejectsReversedDelayRange() {
        assertThatThrownBy(() -> new SliderRanges(0.0, 1.0, 2.0, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Delay");
    }
}
