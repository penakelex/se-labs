package org.nstu.se.lab4.audio;

import java.util.List;

public final class SourceMix implements SignalSource {
    private final List<Channel> channels;

    public SourceMix(final List<Channel> channels) {
        this.channels = List.copyOf(channels);
    }

    @Override
    public float getSample(final int index) {
        var sum = 0.0f;
        for (final var channel : channels) {
            sum += channel.sample(index);
        }
        return Math.clamp(sum, -1.0f, 1.0f);
    }

    @Override
    public int getSampleCount() {
        var maximum = 0;
        for (final var channel : channels) {
            maximum = Math.max(maximum, channel.count());
        }
        return maximum;
    }

    @Override
    public int getSampleRate() {
        if (channels.isEmpty()) {
            return 0;
        }
        return channels.getFirst().source().getSampleRate();
    }

    public record Channel(SignalSource source, double amplitude, int delaySamples) {
        float sample(final int index) {
            final var sourceIndex = index - delaySamples;
            if (sourceIndex < 0 || sourceIndex >= source.getSampleCount()) {
                return 0.0f;
            }
            return (float) (amplitude * source.getSample(sourceIndex));
        }

        int count() {
            return delaySamples + source.getSampleCount();
        }
    }
}
