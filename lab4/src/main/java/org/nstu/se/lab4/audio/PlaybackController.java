package org.nstu.se.lab4.audio;

import org.nstu.se.lab4.audio.event.PlaybackListener;
import org.nstu.se.lab4.audio.event.PlaybackState;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public final class PlaybackController implements SignalSource {
    private static final int UPDATE_INTERVAL_MILLIS = 50;
    private static final int DEFAULT_SAMPLE_RATE = 44_100;
    private static final double DEFAULT_DURATION_SECONDS = 5.0;
    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    private final SourceMix sourceMix = new SourceMix();
    private final PlaybackListener listener;
    private final ScheduledExecutorService scheduler;
    private final ScheduledFuture<?> tickingTask;
    private final AtomicBoolean playing = new AtomicBoolean(false);
    private final AtomicLong positionNanos = new AtomicLong(0);
    private final AtomicLong lastNanos = new AtomicLong(System.nanoTime());
    private final long durationNanos;

    public PlaybackController(final PlaybackListener listener) {
        this.listener = listener;
        this.durationNanos = (long) (DEFAULT_DURATION_SECONDS * NANOS_PER_SECOND);
        this.scheduler =
                Executors.newSingleThreadScheduledExecutor(
                        runnable -> {
                            final var thread = new Thread(runnable, "playback-thread");
                            thread.setDaemon(true);
                            return thread;
                        });
        this.tickingTask =
                scheduler.scheduleAtFixedRate(
                        this::tick,
                        UPDATE_INTERVAL_MILLIS,
                        UPDATE_INTERVAL_MILLIS,
                        TimeUnit.MILLISECONDS);
    }

    public int getSourceCount() {
        return sourceMix.getSourceCount();
    }

    public double getAmplitude(final int index) {
        return sourceMix.getAmplitude(index);
    }

    public void setAmplitude(final int index, final double amplitude) {
        sourceMix.setAmplitude(index, amplitude);
    }

    public double getDelaySeconds(final int index) {
        return sourceMix.getDelaySeconds(index);
    }

    public void setDelaySeconds(final int index, final double delaySeconds) {
        sourceMix.setDelaySeconds(index, delaySeconds);
    }

    public double getDurationSeconds() {
        return (double) durationNanos / NANOS_PER_SECOND;
    }

    public boolean isPlaying() {
        return playing.get();
    }

    public void play() {
        playing.set(true);
    }

    public void pause() {
        playing.set(false);
    }

    public void stop() {
        playing.set(false);
        positionNanos.set(0);
        notifyState();
    }

    public void seek(final double seconds) {
        final var clamped = Math.clamp(seconds, 0.0, getDurationSeconds());
        positionNanos.set((long) (clamped * NANOS_PER_SECOND));
        notifyState();
    }

    public void shutdown() {
        tickingTask.cancel(true);
        scheduler.shutdownNow();
    }

    @Override
    public float getSample(final int index) {
        return sourceMix.getSample(index);
    }

    @Override
    public int getSampleCount() {
        return sourceMix.getSampleCount();
    }

    @Override
    public int getSampleRate() {
        return sourceMix.getSampleRate();
    }

    private void tick() {
        final var nowNanos = System.nanoTime();
        final var elapsedNanos = nowNanos - lastNanos.getAndSet(nowNanos);
        advance(elapsedNanos);
        notifyState();
    }

    private void advance(final long elapsedNanos) {
        if (!playing.get()) {
            return;
        }
        final var current = positionNanos.get();
        final var updated = current + elapsedNanos;
        if (updated >= durationNanos) {
            if (positionNanos.compareAndSet(current, durationNanos)) {
                playing.set(false);
                listener.onPlaybackFinished();
            }
        } else {
            positionNanos.compareAndSet(current, updated);
        }
    }

    private void notifyState() {
        final var currentNanos = positionNanos.get();
        final var seconds = (double) currentNanos / NANOS_PER_SECOND;
        final var sampleIndex = (int) Math.round(seconds * DEFAULT_SAMPLE_RATE);
        final var sample = sourceMix.getSample(sampleIndex);
        listener.onPlaybackState(
                new PlaybackState(seconds, getDurationSeconds(), sample, playing.get()));
    }
}
