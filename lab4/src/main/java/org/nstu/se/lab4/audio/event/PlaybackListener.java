package org.nstu.se.lab4.audio.event;

public interface PlaybackListener {
    default void onPlaybackState(final PlaybackState state) {}

    default void onPlaybackFinished() {}
}
