package org.nstu.se.lab4.audio.mix;

import org.nstu.se.lab4.Messages;
import org.nstu.se.lab4.audio.source.SignalSource;
import org.nstu.se.lab4.audio.source.WaveSource;

import java.util.ArrayList;
import java.util.List;

public record MixSettings(List<Channel> channels) {
    private static final double MIN_AMPLITUDE = 0.0;
    private static final double MAX_AMPLITUDE = 1.0;
    private static final double MIN_DELAY_SECONDS = 0.0;
    private static final double DEFAULT_AMPLITUDE = 0.5;
    private static final double DEFAULT_DELAY_SECONDS = 0.0;
    private static final int DEFAULT_SAMPLE_RATE = 44_100;
    private static final double DEFAULT_DURATION_SECONDS = 3.0;
    private static final double FIRST_FREQUENCY = 440.0;
    private static final double SECOND_FREQUENCY = 660.0;
    private static final String FIRST_RESOURCE = "/org/nstu/se/lab4/sample1.wav";
    private static final String SECOND_RESOURCE = "/org/nstu/se/lab4/sample2.wav";
    private static final String KEY_TONE_NAME = "source.tone";

    public MixSettings {
        channels = List.copyOf(channels);
    }

    public static MixSettings createDefault() {
        return new MixSettings(
                List.of(
                        new Channel(
                                loadOrGenerate(FIRST_RESOURCE, FIRST_FREQUENCY),
                                toneName(FIRST_FREQUENCY),
                                DEFAULT_AMPLITUDE,
                                DEFAULT_DELAY_SECONDS),
                        new Channel(
                                loadOrGenerate(SECOND_RESOURCE, SECOND_FREQUENCY),
                                toneName(SECOND_FREQUENCY),
                                DEFAULT_AMPLITUDE,
                                DEFAULT_DELAY_SECONDS)));
    }

    public int getSourceCount() {
        return channels.size();
    }

    public String getChannelName(final int index) {
        return channels.get(index).name();
    }

    public double getAmplitude(final int index) {
        return channels.get(index).amplitude();
    }

    public MixSettings withAmplitude(final int index, final double amplitude) {
        if (amplitude < MIN_AMPLITUDE || amplitude > MAX_AMPLITUDE) {
            throw new IllegalArgumentException("Amplitude must be between 0.0 and 1.0");
        }

        return replace(index, channels.get(index).withAmplitude(amplitude));
    }

    public double getDelaySeconds(final int index) {
        return channels.get(index).delaySeconds();
    }

    public MixSettings withDelaySeconds(final int index, final double delaySeconds) {
        if (delaySeconds < MIN_DELAY_SECONDS) {
            throw new IllegalArgumentException("Delay must be non-negative");
        }

        return replace(index, channels.get(index).withDelaySeconds(delaySeconds));
    }

    public MixSettings withAddedChannel(final SignalSource source, final String name) {
        final var updated = new ArrayList<>(channels);
        updated.add(new Channel(source, name, DEFAULT_AMPLITUDE, DEFAULT_DELAY_SECONDS));
        return new MixSettings(updated);
    }

    public MixSettings withRemovedChannel(final int index) {
        final var updated = new ArrayList<>(channels);
        updated.remove(index);
        return new MixSettings(updated);
    }

    private MixSettings replace(final int index, final Channel channel) {
        final var updated = new ArrayList<>(channels);
        updated.set(index, channel);
        return new MixSettings(updated);
    }

    private static SignalSource loadOrGenerate(final String resourcePath, final double frequency) {
        if (WaveSource.class.getResource(resourcePath) != null) {
            return WaveSource.fromResource(resourcePath);
        }

        return WaveSource.generateTone(frequency, DEFAULT_DURATION_SECONDS, DEFAULT_SAMPLE_RATE);
    }

    private static String toneName(final double frequency) {
        return Messages.format(KEY_TONE_NAME, frequency);
    }

    public record Channel(SignalSource source, String name, double amplitude, double delaySeconds) {
        Channel withAmplitude(final double newAmplitude) {
            return new Channel(source, name, newAmplitude, delaySeconds);
        }

        Channel withDelaySeconds(final double newDelaySeconds) {
            return new Channel(source, name, amplitude, newDelaySeconds);
        }
    }
}
