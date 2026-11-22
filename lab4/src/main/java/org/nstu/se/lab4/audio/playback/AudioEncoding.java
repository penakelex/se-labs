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

    private AudioEncoding() {}

    public static byte[] encode(final SignalSource source) {
        final var sampleCount = source.getSampleCount();
        final var data = new byte[sampleCount * 2];

        for (var index = 0; index < sampleCount; index++) {
            final var clamped = Math.clamp(source.getSample(index), -1.0f, 1.0f);
            final var value = (short) Math.round(clamped * Short.MAX_VALUE);
            data[index * 2] = (byte) (value & 0xFF);
            data[index * 2 + 1] = (byte) ((value >> 8) & 0xFF);
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
