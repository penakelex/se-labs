package org.nstu.se.lab4.audio.playback;

import org.nstu.se.lab4.audio.source.SignalSource;

import javax.sound.sampled.Clip;

public final class SourcePlayer {
    private static final long MICROSECONDS_PER_SECOND = 1_000_000;
    private static final double MICROSECONDS_PER_SECOND_DOUBLE = 1_000_000.0;

    private final Clip clip;

    public SourcePlayer(final SignalSource source) {
        clip = AudioEncoding.openClip(source);
    }

    public void play() {
        if (clip.getMicrosecondPosition() >= clip.getMicrosecondLength()) {
            clip.setMicrosecondPosition(0);
        }

        clip.start();
    }

    public void pause() {
        clip.stop();
    }

    public void stop() {
        clip.stop();
        clip.setMicrosecondPosition(0);
    }

    public void seek(final double seconds) {
        final var clampedSeconds = Math.clamp(seconds, 0.0, durationSeconds());
        clip.setMicrosecondPosition((long) (clampedSeconds * MICROSECONDS_PER_SECOND));
    }

    public double positionSeconds() {
        return clip.getMicrosecondPosition() / MICROSECONDS_PER_SECOND_DOUBLE;
    }

    public double durationSeconds() {
        return clip.getMicrosecondLength() / MICROSECONDS_PER_SECOND_DOUBLE;
    }

    public boolean isPlaying() {
        return clip.isRunning();
    }

    public void dispose() {
        clip.close();
    }
}
