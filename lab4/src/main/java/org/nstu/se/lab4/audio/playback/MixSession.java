package org.nstu.se.lab4.audio.playback;

import org.nstu.se.lab4.audio.mix.MixSettings;
import org.nstu.se.lab4.audio.mix.SliderRanges;
import org.nstu.se.lab4.audio.source.SignalSource;
import org.nstu.se.lab4.audio.source.SourceMix;

public final class MixSession {
    private MixSettings mixSettings = MixSettings.createDefault();
    private SliderRanges ranges = SliderRanges.defaultRanges();
    private SourceMix sourceMix;
    private SourcePlayer currentPlayer;

    public MixSession() {
        rebuild();
    }

    public MixSettings settings() {
        return mixSettings;
    }

    public SliderRanges sliderRanges() {
        return ranges;
    }

    public SourceMix mix() {
        return sourceMix;
    }

    public SourcePlayer player() {
        return currentPlayer;
    }

    public void updateSliderRanges(final SliderRanges newRanges) {
        ranges = newRanges;

        for (var index = 0; index < mixSettings.getSourceCount(); index++) {
            mixSettings =
                    mixSettings.withAmplitude(
                            index, newRanges.clampAmplitude(mixSettings.getAmplitude(index)));
            mixSettings =
                    mixSettings.withDelaySeconds(
                            index, newRanges.clampDelaySeconds(mixSettings.getDelaySeconds(index)));
        }

        rebuild();
    }

    public void addSource(final SignalSource source, final String name) {
        mixSettings = mixSettings.withAddedChannel(source, name);
    }

    public void removeSource(final int index) {
        mixSettings = mixSettings.withRemovedChannel(index);
    }

    public void updateAmplitude(final int index, final double amplitude) {
        mixSettings = mixSettings.withAmplitude(index, ranges.clampAmplitude(amplitude));
    }

    public void updateDelaySeconds(final int index, final double delaySeconds) {
        mixSettings = mixSettings.withDelaySeconds(index, ranges.clampDelaySeconds(delaySeconds));
    }

    public void rebuild() {
        sourceMix = MixBuilder.build(mixSettings);
        final var previous = currentPlayer;
        currentPlayer = new SourcePlayer(sourceMix);

        if (previous != null) {
            final var wasPlaying = previous.isPlaying();
            previous.dispose();

            if (wasPlaying) {
                currentPlayer.play();
            }
        }
    }

    public void shutdown() {
        currentPlayer.dispose();
    }
}
