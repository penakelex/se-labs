package org.nstu.se.lab4.audio;

import java.util.List;

public final class Mixer implements SignalSource {
    private final List<SignalSource> sources;

    public Mixer(final List<SignalSource> sources) {
        this.sources = List.copyOf(sources);
    }

    @Override
    public float getSample(final int index) {
        var sum = 0.0f;
        for (final var source : sources) {
            sum += source.getSample(index);
        }
        return Math.clamp(sum, -1.0f, 1.0f);
    }

    @Override
    public int getSampleCount() {
        return sources.stream().mapToInt(SignalSource::getSampleCount).max().orElse(0);
    }

    @Override
    public int getSampleRate() {
        return sources.isEmpty() ? 0 : sources.getFirst().getSampleRate();
    }
}
