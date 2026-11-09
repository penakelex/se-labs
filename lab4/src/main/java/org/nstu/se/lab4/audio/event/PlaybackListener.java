package org.nstu.se.lab4.audio.event;

public interface PlaybackListener {
    void onPlaybackState(PlaybackState state);

    void onPlaybackFinished();
}
