package org.nstu.se.lab4.audio.playback;

import org.nstu.se.lab4.audio.event.PlaybackListener;
import org.nstu.se.lab4.audio.event.PlaybackState;
import org.nstu.se.lab4.audio.source.SourceMix;

public final class PlaybackController {
    private final PlaybackHub hub = new PlaybackHub();
    private final MixSession mixSession = new MixSession();

    public record State(
            double positionSeconds,
            double durationSeconds,
            boolean playing,
            SourceMix signalSource) {}

    public State state() {
        final var player = mixSession.player();
        return new State(
                player.positionSeconds(),
                player.durationSeconds(),
                player.isPlaying(),
                mixSession.mix());
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(
            value = "EI_EXPOSE_REP",
            justification = "MixSession is intentionally shared with UI controllers")
    public MixSession session() {
        return mixSession;
    }

    public void addPlaybackListener(final PlaybackListener listener) {
        hub.add(listener);
    }

    public void removePlaybackListener(final PlaybackListener listener) {
        hub.remove(listener);
    }

    public void broadcastState() {
        final var player = mixSession.player();
        hub.broadcast(
                new PlaybackState(
                        player.positionSeconds(),
                        player.durationSeconds(),
                        MixBuilder.currentSample(mixSession.mix(), player),
                        player.isPlaying(),
                        mixSession.mix()));
    }

    public void shutdown() {
        mixSession.shutdown();
    }
}
