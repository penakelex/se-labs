package org.nstu.se.lab4.audio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;
import org.nstu.se.lab4.audio.source.ResampledSource;
import org.nstu.se.lab4.audio.source.SignalSource;

final class ResampledSourceTest {
    private static final float TOLERANCE = 0.0001f;

    private SignalSource constantSource(final float value, final int count, final int sampleRate) {
        return new SignalSource() {
            @Override
            public float getSample(final int index) {
                return index >= 0 && index < count ? value : 0.0f;
            }

            @Override
            public int getSampleCount() {
                return count;
            }

            @Override
            public int getSampleRate() {
                return sampleRate;
            }
        };
    }

    private SignalSource linearSource(final int count, final int sampleRate) {
        return new SignalSource() {
            @Override
            public float getSample(final int index) {
                return index >= 0 && index < count ? index / (float) count : 0.0f;
            }

            @Override
            public int getSampleCount() {
                return count;
            }

            @Override
            public int getSampleRate() {
                return sampleRate;
            }
        };
    }

    @Test
    void reportsTargetRateAndCountOnUpsampling() {
        final var source = new ResampledSource(constantSource(0.5f, 100, 100), 200);
        assertThat(java.util.List.of(source.getSampleRate(), source.getSampleCount()))
                .containsExactly(200, 200);
    }

    @Test
    void keepsConstantSignalOnUpsampling() {
        final var source = new ResampledSource(constantSource(0.5f, 100, 100), 200);
        assertThat(java.util.List.of(source.getSample(0), source.getSample(101)))
                .containsExactly(0.5f, 0.5f);
    }

    @Test
    void interpolatesLinearSignalAtHalfPosition() {
        final var source = new ResampledSource(linearSource(100, 100), 200);
        assertThat(source.getSample(100)).isCloseTo(0.5f, within(TOLERANCE));
    }

    @Test
    void interpolatesLinearSignalAtQuarterPosition() {
        final var source = new ResampledSource(linearSource(100, 100), 200);
        assertThat(source.getSample(50)).isCloseTo(0.25f, within(TOLERANCE));
    }

    @Test
    void returnsZeroOutsideRange() {
        final var source = new ResampledSource(constantSource(0.5f, 100, 100), 200);
        assertThat(java.util.List.of(source.getSample(-1), source.getSample(200)))
                .containsExactly(0.0f, 0.0f);
    }

    @Test
    void downsamplesToShorterCount() {
        final var source = new ResampledSource(constantSource(0.5f, 200, 200), 100);
        assertThat(source.getSampleCount()).isEqualTo(100);
    }

    @Test
    void keepsSignalOnDownsampling() {
        final var source = new ResampledSource(constantSource(0.5f, 200, 200), 100);
        assertThat(source.getSample(50)).isCloseTo(0.5f, within(TOLERANCE));
    }

    @Test
    void rejectsNonPositiveSampleRates() {
        final var source = constantSource(0.5f, 100, 0);
        assertThatThrownBy(() -> new ResampledSource(source, 100))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
