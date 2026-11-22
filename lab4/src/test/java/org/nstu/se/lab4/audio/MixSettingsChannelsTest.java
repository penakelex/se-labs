package org.nstu.se.lab4.audio;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.nstu.se.lab4.Messages;
import org.nstu.se.lab4.audio.mix.MixSettings;
import org.nstu.se.lab4.audio.source.SignalSource;
import org.nstu.se.lab4.audio.source.WaveSource;

final class MixSettingsChannelsTest {
    private static final String KEY_TONE_NAME = "source.tone";
    private static final String SECOND_NAME = "second.wav";

    private SignalSource tone() {
        return WaveSource.generateTone(440.0, 0.1, 44_100);
    }

    @Test
    void addsChannelIncreasingSourceCount() {
        final var settings = MixSettings.createDefault().withAddedChannel(tone(), SECOND_NAME);
        assertThat(settings.getSourceCount()).isEqualTo(3);
    }

    @Test
    void addedChannelHasNameAndDefaults() {
        final var settings = MixSettings.createDefault().withAddedChannel(tone(), SECOND_NAME);
        assertThat(
                        java.util.List.of(
                                settings.getChannelName(2),
                                settings.getAmplitude(2),
                                settings.getDelaySeconds(2)))
                .containsExactly(SECOND_NAME, 0.5, 0.0);
    }

    @Test
    void removesChannelKeepingRemainingOrder() {
        final var settings = MixSettings.createDefault().withAddedChannel(tone(), SECOND_NAME);
        final var removed = settings.withRemovedChannel(0);

        assertThat(java.util.List.of(removed.getChannelName(0), removed.getChannelName(1)))
                .containsExactly(settings.getChannelName(1), SECOND_NAME);
    }

    @Test
    void removesLastChannelLeavingEmptyMix() {
        final var single =
                new MixSettings(
                        java.util.List.of(new MixSettings.Channel(tone(), "first.wav", 0.5, 0.0)));

        assertThat(single.withRemovedChannel(0).getSourceCount()).isZero();
    }

    @Test
    void allowsEmptyChannelsList() {
        assertThat(new MixSettings(java.util.List.of()).getSourceCount()).isZero();
    }

    @Test
    void defaultChannelsHaveToneNames() {
        final var settings = MixSettings.createDefault();
        assertThat(java.util.List.of(settings.getChannelName(0), settings.getChannelName(1)))
                .containsExactly(
                        Messages.format(KEY_TONE_NAME, 440.0),
                        Messages.format(KEY_TONE_NAME, 660.0));
    }
}
