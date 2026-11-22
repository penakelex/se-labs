package org.nstu.se.lab4.audio;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.nstu.se.lab4.audio.playback.AudioEncoding;
import org.nstu.se.lab4.audio.source.SignalSource;

final class AudioEncodingTest {
    private SignalSource constantSource(final float value, final int count) {
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
                return 44_100;
            }
        };
    }

    @Test
    void encodesSilenceToZeroBytes() {
        final var data = AudioEncoding.encode(constantSource(0.0f, 4));
        assertThat(data).containsExactly(0, 0, 0, 0, 0, 0, 0, 0);
    }

    @Test
    void encodesFullScalePositiveToMaxShortLittleEndian() {
        final var data = AudioEncoding.encode(constantSource(1.0f, 1));
        assertThat(data).containsExactly((byte) 0xFF, 0x7F);
    }

    @Test
    void encodesFullScaleNegativeToMinShortLittleEndian() {
        final var data = AudioEncoding.encode(constantSource(-1.0f, 1));
        assertThat(data).containsExactly(0x01, (byte) 0x80);
    }

    @Test
    void clampsOverflowingSamples() {
        final var data = AudioEncoding.encode(constantSource(2.0f, 1));
        assertThat(data).containsExactly((byte) 0xFF, 0x7F);
    }

    @Test
    void encodesTwiceAsManyBytesAsSamples() {
        final var data = AudioEncoding.encode(constantSource(0.25f, 100));
        assertThat(data).hasSize(200);
    }
}
