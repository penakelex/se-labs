package org.nstu.se.lab4.audio.playback;

import org.nstu.se.lab4.audio.mix.MixSettings;
import org.nstu.se.lab4.audio.source.ResampledSource;
import org.nstu.se.lab4.audio.source.SourceMix;
import org.nstu.se.lab4.audio.source.WaveSource;

import java.util.ArrayList;
import java.util.List;

final class MixBuilder {
    private static final double SILENCE_DURATION_SECONDS = 1.0;
    private static final int SILENCE_SAMPLE_RATE = 44_100;

    private MixBuilder() {}

    static SourceMix build(final MixSettings settings) {
        if (settings.getSourceCount() == 0) {
            return new SourceMix(
                    List.of(
                            new SourceMix.Channel(
                                    WaveSource.generateSilence(
                                            SILENCE_DURATION_SECONDS, SILENCE_SAMPLE_RATE),
                                    1.0,
                                    0)));
        }

        return new SourceMix(channelsFor(settings));
    }

    private static List<SourceMix.Channel> channelsFor(final MixSettings settings) {
        final var targetSampleRate = settings.channels().getFirst().source().getSampleRate();
        final var channels = new ArrayList<SourceMix.Channel>(settings.getSourceCount());

        for (final var channel : settings.channels()) {
            final var source =
                    channel.source().getSampleRate() == targetSampleRate
                            ? channel.source()
                            : new ResampledSource(channel.source(), targetSampleRate);
            final var delaySamples =
                    (int) Math.round(channel.delaySeconds() * source.getSampleRate());
            channels.add(new SourceMix.Channel(source, channel.amplitude(), delaySamples));
        }

        return channels;
    }

    static float currentSample(final SourceMix mix, final SourcePlayer player) {
        if (mix == null) {
            return 0.0f;
        }

        final var sampleIndex = (int) (player.positionSeconds() * mix.getSampleRate());
        return mix.getSample(sampleIndex);
    }
}
