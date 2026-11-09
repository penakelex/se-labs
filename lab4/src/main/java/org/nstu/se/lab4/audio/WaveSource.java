package org.nstu.se.lab4.audio;

import java.io.BufferedInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public final class WaveSource implements SignalSource {
    private static final double TWO_PI = 2.0 * Math.PI;
    private static final int MONO_CHANNEL_COUNT = 1;
    private static final int BYTE_SAMPLE_SIZE = 1;
    private static final int BITS_PER_BYTE = 8;
    private static final int BYTE_MASK = 0xFF;
    private static final float BYTE_DIVISOR = 128.0f;
    private static final int BUFFER_FRAMES = 1024;
    private static final double FADE_SECONDS = 0.01;

    private final float[] samples;
    private final int sampleRate;

    private WaveSource(final float[] samples, final int sampleRate) {
        this.samples = samples;
        this.sampleRate = sampleRate;
    }

    public static WaveSource fromResource(final String path) {
        try (var input = WaveSource.class.getResourceAsStream(path)) {
            if (input == null) {
                throw new IllegalArgumentException("Resource not found: " + path);
            }

            try (var buffered = new BufferedInputStream(input);
                    var audio = AudioSystem.getAudioInputStream(buffered)) {
                return fromAudioStream(audio);
            }
        } catch (final UnsupportedAudioFileException | IOException exception) {
            throw new IllegalArgumentException("Cannot read WAV: " + path, exception);
        }
    }

    public static WaveSource generateTone(
            final double frequency, final double durationSeconds, final int sampleRate) {
        final var count = (int) Math.round(durationSeconds * sampleRate);
        final var samples = new float[count];
        final var fadeCount = Math.max(1, (int) Math.round(FADE_SECONDS * sampleRate));

        for (var index = 0; index < count; index++) {
            final var fadeIn = Math.min(1.0, (double) index / fadeCount);
            final var fadeOut = Math.min(1.0, (double) (count - 1 - index) / fadeCount);
            final var envelope = Math.min(fadeIn, fadeOut);
            samples[index] = (float) (Math.sin(TWO_PI * frequency * index / sampleRate) * envelope);
        }

        return new WaveSource(samples, sampleRate);
    }

    private static WaveSource fromAudioStream(final AudioInputStream audio) throws IOException {
        final var format = audio.getFormat();
        final var frameLength = audio.getFrameLength();
        final var frameSize = format.getFrameSize();

        if (frameLength == AudioSystem.NOT_SPECIFIED || frameSize <= 0) {
            throw new IOException("Unknown WAV length");
        }

        final var samples = new float[(int) frameLength];
        final var buffer = new byte[frameSize * BUFFER_FRAMES];
        var sampleIndex = 0;

        while (sampleIndex < samples.length) {
            final var read = audio.read(buffer);

            if (read <= 0) {
                break;
            }

            final var frames = read / frameSize;

            for (var frame = 0; frame < frames && sampleIndex < samples.length; frame++) {
                final var offset = frame * frameSize;
                samples[sampleIndex] = readSample(buffer, offset, format);
                sampleIndex++;
            }
        }

        return new WaveSource(samples, (int) format.getSampleRate());
    }

    private static float readSample(
            final byte[] buffer, final int offset, final AudioFormat format) {
        final var sampleSize = format.getSampleSizeInBits() / BITS_PER_BYTE;
        final var bigEndian = format.isBigEndian();
        final var channels = format.getChannels();

        final var firstChannel = readChannel(buffer, offset, sampleSize, bigEndian);

        if (channels == MONO_CHANNEL_COUNT) {
            return firstChannel;
        }

        var sum = firstChannel;

        for (var channel = MONO_CHANNEL_COUNT; channel < channels; channel++) {
            sum += readChannel(buffer, offset + channel * sampleSize, sampleSize, bigEndian);
        }

        return sum / channels;
    }

    private static float readChannel(
            final byte[] buffer, final int offset, final int sampleSize, final boolean bigEndian) {
        if (sampleSize == BYTE_SAMPLE_SIZE) {
            return (buffer[offset] & BYTE_MASK) / BYTE_DIVISOR - 1.0f;
        }

        var value = 0L;

        if (bigEndian) {
            for (var index = 0; index < sampleSize; index++) {
                value = (value << BITS_PER_BYTE) | (buffer[offset + index] & BYTE_MASK);
            }
        } else {
            for (var index = sampleSize - 1; index >= 0; index--) {
                value = (value << BITS_PER_BYTE) | (buffer[offset + index] & BYTE_MASK);
            }
        }

        final var bits = sampleSize * BITS_PER_BYTE;
        final var shift = Long.SIZE - bits;
        value = value << shift >> shift;
        final var maximum = (float) Math.pow(2, bits - 1);
        return value / maximum;
    }

    @Override
    public float getSample(final int index) {
        if (index < 0 || index >= samples.length) {
            return 0.0f;
        }

        return samples[index];
    }

    @Override
    public int getSampleCount() {
        return samples.length;
    }

    @Override
    public int getSampleRate() {
        return sampleRate;
    }
}
