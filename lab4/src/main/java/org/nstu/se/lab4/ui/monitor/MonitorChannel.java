package org.nstu.se.lab4.ui.monitor;

import org.nstu.se.lab4.audio.playback.PlaybackController;
import org.nstu.se.lab4.audio.playback.SourcePlayer;
import org.nstu.se.lab4.audio.source.SignalProcessor;
import org.nstu.se.lab4.audio.source.SignalSource;

@SuppressWarnings("NullAway.Init")
final class MonitorChannel {
    private final PlaybackController model;
    private final int channelIndex;

    private double appliedAmplitude = -1.0;
    private double appliedDelaySeconds = -1.0;

    private SignalSource currentSignal;
    private SourcePlayer currentPlayer;

    MonitorChannel(final int channelIndex, final PlaybackController model) {
        this.channelIndex = channelIndex;
        this.model = model;
        rebuild(true);
    }

    SignalSource signal() {
        return currentSignal;
    }

    SourcePlayer player() {
        return currentPlayer;
    }

    double durationSeconds() {
        return currentPlayer.durationSeconds();
    }

    void applySettingsChanges() {
        if (channelIndex >= model.session().settings().getSourceCount()) {
            return;
        }

        final var channel = model.session().settings().channels().get(channelIndex);
        final var unchanged =
                channel.amplitude() == appliedAmplitude
                        && channel.delaySeconds() == appliedDelaySeconds;

        if (!unchanged) {
            rebuild(false);
        }
    }

    void rebuild(final boolean createPlayer) {
        final var channel = model.session().settings().channels().get(channelIndex);
        appliedAmplitude = channel.amplitude();
        appliedDelaySeconds = channel.delaySeconds();
        final var rebuiltSignal =
                new SignalProcessor(channel.source(), appliedAmplitude, appliedDelaySeconds);
        currentSignal = rebuiltSignal;

        if (createPlayer) {
            currentPlayer = new SourcePlayer(rebuiltSignal);
        } else {
            rebuildPlayerPreservingState(rebuiltSignal);
        }
    }

    void pauseIfPlaying() {
        if (currentPlayer.isPlaying()) {
            currentPlayer.pause();
        }
    }

    void seek(final double positionSeconds) {
        currentPlayer.seek(positionSeconds);
    }

    private void rebuildPlayerPreservingState(final SignalSource rebuiltSignal) {
        final var wasPlaying = currentPlayer.isPlaying();
        final var position = currentPlayer.positionSeconds();
        currentPlayer.dispose();

        final var rebuilt = new SourcePlayer(rebuiltSignal);
        rebuilt.seek(Math.min(position, rebuilt.durationSeconds()));

        if (wasPlaying) {
            rebuilt.play();
        }

        currentPlayer = rebuilt;
    }

    void dispose() {
        currentPlayer.dispose();
    }
}
