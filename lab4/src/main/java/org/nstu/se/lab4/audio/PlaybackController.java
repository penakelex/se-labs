package org.nstu.se.lab4.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;

public final class PlaybackController {
    private static final int BITS_PER_SAMPLE = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;

    private MixSettings settings = MixSettings.createDefault();
    private SourceMix currentMix;
    private Clip clip;

    public PlaybackController() {
        rebuildAudio();
    }

    public record State(
            double positionSeconds,
            double durationSeconds,
            boolean playing,
            SourceMix signalSource) {}

    public State getState() {
        return new State(getPositionSeconds(), getDurationSeconds(), isPlaying(), currentMix);
    }

    public MixSettings getSettings() {
        return settings;
    }

    public void updateAmplitude(final int index, final double amplitude) {
        settings = settings.withAmplitude(index, amplitude);
    }

    public void updateDelaySeconds(final int index, final double delaySeconds) {
        settings = settings.withDelaySeconds(index, delaySeconds);
    }

    public void rebuildAudio() {
        currentMix = buildMix();
        final var sampleRate = currentMix.getSampleRate();
        if (sampleRate <= 0) {
            throw new IllegalStateException("Source sample rate must be positive");
        }
        try {
            if (clip != null && clip.isOpen()) {
                clip.close();
            }
            final var format =
                    new AudioFormat(sampleRate, BITS_PER_SAMPLE, CHANNELS, SIGNED, BIG_ENDIAN);
            final var data = encode(currentMix);
            clip = AudioSystem.getClip();
            clip.open(format, data, 0, data.length);
        } catch (final LineUnavailableException exception) {
            throw new IllegalStateException("Cannot open audio line", exception);
        }
    }

    public void play() {
        if (clip == null) {
            return;
        }
        if (clip.getMicrosecondPosition() >= clip.getMicrosecondLength()) {
            clip.setMicrosecondPosition(0);
        }
        clip.start();
    }

    public void pause() {
        if (clip != null) {
            clip.stop();
        }
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
            clip.setMicrosecondPosition(0);
        }
    }

    public void seek(final double seconds) {
        if (clip == null) {
            return;
        }
        final var clampedSeconds = Math.clamp(seconds, 0.0, getDurationSeconds());
        clip.setMicrosecondPosition((long) (clampedSeconds * 1_000_000));
    }

    public void shutdown() {
        if (clip != null) {
            clip.close();
        }
    }

    private SourceMix buildMix() {
        final var channels = new java.util.ArrayList<SourceMix.Channel>(settings.getSourceCount());
        for (final var channel : settings.channels()) {
            final var delaySamples =
                    (int) Math.round(channel.delaySeconds() * channel.source().getSampleRate());
            channels.add(
                    new SourceMix.Channel(channel.source(), channel.amplitude(), delaySamples));
        }
        return new SourceMix(channels);
    }

    private double getPositionSeconds() {
        return clip == null ? 0.0 : clip.getMicrosecondPosition() / 1_000_000.0;
    }

    private double getDurationSeconds() {
        return clip == null ? 0.0 : clip.getMicrosecondLength() / 1_000_000.0;
    }

    private boolean isPlaying() {
        return clip != null && clip.isRunning();
    }

    private static byte[] encode(final SignalSource source) {
        final var sampleCount = source.getSampleCount();
        final var data = new byte[sampleCount * 2];
        for (var index = 0; index < sampleCount; index++) {
            final var value = (short) Math.round(source.getSample(index) * Short.MAX_VALUE);
            data[index * 2] = (byte) (value & 0xFF);
            data[index * 2 + 1] = (byte) ((value >> 8) & 0xFF);
        }
        return data;
    }
}
