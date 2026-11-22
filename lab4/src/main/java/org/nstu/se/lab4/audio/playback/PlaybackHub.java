package org.nstu.se.lab4.audio.playback;

import org.nstu.se.lab4.audio.event.PlaybackListener;
import org.nstu.se.lab4.audio.event.PlaybackState;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class PlaybackHub {
    private static final double FINISH_TOLERANCE_SECONDS = 0.05;

    private final List<PlaybackListener> listeners = new CopyOnWriteArrayList<>();
    private boolean wasPlaying;

    public void add(final PlaybackListener listener) {
        listeners.add(listener);
    }

    public void remove(final PlaybackListener listener) {
        listeners.remove(listener);
    }

    public void broadcast(final PlaybackState state) {
        if (!listeners.isEmpty()) {
            for (final var listener : listeners) {
                listener.onPlaybackState(state);
            }

            if (hasJustFinished(state)) {
                for (final var listener : listeners) {
                    listener.onPlaybackFinished();
                }
            }
        }

        wasPlaying = state.playing();
    }

    private boolean hasJustFinished(final PlaybackState state) {
        return wasPlaying
                && !state.playing()
                && state.positionSeconds() >= state.durationSeconds() - FINISH_TOLERANCE_SECONDS;
    }
}
