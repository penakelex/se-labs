package org.nstu.se.lab4.audio.playback;

import org.nstu.se.lab4.audio.source.SignalSource;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;

public final class AudioEncoding {
    private static final int BITS_PER_SAMPLE = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;
    private static final int BYTES_PER_SAMPLE = 2;
    private static final int BYTE_MASK = 0xFF;
    private static final int BYTE_BITS = 8;

    private AudioEncoding() {}

    public static byte[] encode(final SignalSource source) {
        final var sampleCount = source.getSampleCount();
        final var data = new byte[sampleCount * BYTES_PER_SAMPLE];

        for (var index = 0; index < sampleCount; index++) {
            final var clamped = Math.clamp(source.getSample(index), -1.0f, 1.0f);
            final var value = (short) Math.round(clamped * Short.MAX_VALUE);
            data[index * BYTES_PER_SAMPLE] = (byte) (value & BYTE_MASK);
            data[index * BYTES_PER_SAMPLE + 1] = (byte) ((value >> BYTE_BITS) & BYTE_MASK);
        }

        return data;
    }

    public static Clip openClip(final SignalSource source) {
        final var sampleRate = source.getSampleRate();

        if (sampleRate <= 0) {
            throw new IllegalStateException("Source sample rate must be positive");
        }

        try {
            final var format =
                    new AudioFormat(sampleRate, BITS_PER_SAMPLE, CHANNELS, SIGNED, BIG_ENDIAN);
            final var data = encode(source);
            final var clip = AudioSystem.getClip();
            clip.open(format, data, 0, data.length);
            return clip;
        } catch (final LineUnavailableException exception) {
            throw new IllegalStateException("Cannot open audio line", exception);
        }
    }
}
