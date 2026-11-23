package org.nstu.se.lab4.ui.monitor;

import javafx.animation.AnimationTimer;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

import org.jspecify.annotations.Nullable;
import org.nstu.se.lab4.Messages;
import org.nstu.se.lab4.audio.event.PlaybackState;

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

final class MonitorPlayback {
    private static final String KEY_BUTTON_PLAY = "button.play";
    private static final String KEY_BUTTON_PAUSE = "button.pause";
    private static final double END_TOLERANCE_SECONDS = 0.05;

    private final MonitorTransport transport;
    private final Button playPauseButton;
    private final Button stopButton;
    private final Slider seekSlider;
    private final DoubleConsumer onPositionUpdate;
    private final Consumer<MonitorChannel> onChannelRebuilt;

    private @Nullable MonitorChannel channel;
    private boolean controlsDisabled;
    private boolean wasPlaying;
    private double followPositionSeconds = -1.0;

    private final AnimationTimer timer =
            new AnimationTimer() {
                @Override
                public void handle(final long now) {
                    tick();
                }
            };

    MonitorPlayback(
            final Slider seekSlider,
            final Label timeLabel,
            final Button playPauseButton,
            final Button stopButton,
            final DoubleConsumer onPositionUpdate,
            final Consumer<MonitorChannel> onChannelRebuilt) {
        this.seekSlider = seekSlider;
        this.playPauseButton = playPauseButton;
        this.stopButton = stopButton;
        this.onPositionUpdate = onPositionUpdate;
        this.onChannelRebuilt = onChannelRebuilt;
        this.transport = new MonitorTransport(seekSlider, timeLabel);

        transport.setOnSeek(
                position -> {
                    if (!controlsDisabled && channel != null) {
                        channel.seek(position);
                    }
                });

        playPauseButton.setOnAction(_ -> togglePlayPause());
        stopButton.setOnAction(_ -> stop());
        timer.start();
    }

    void configure(final MonitorChannel newChannel) {
        this.channel = newChannel;
        seekSlider.setMin(0.0);
        transport.setDuration(newChannel.durationSeconds());
        transport.showTime(0.0, newChannel.durationSeconds());
        updatePlayPauseButton();
    }

    void handlePlaybackState(final PlaybackState state) {
        if (channel != null && channel.applySettingsChanges()) {
            transport.setDuration(channel.durationSeconds());
            onChannelRebuilt.accept(channel);
        }

        if (state.playing()) {
            controlsDisabled = true;
            followPositionSeconds = state.positionSeconds();

            if (channel != null) {
                channel.pauseIfPlaying();
            }
        } else {
            if (controlsDisabled && channel != null) {
                channel.seek(followPositionSeconds);
            }

            controlsDisabled = false;
            followPositionSeconds = -1.0;
        }

        applyControlsDisabled();
    }

    void syncButton() {
        updatePlayPauseButton();
    }

    void dispose() {
        timer.stop();

        if (channel != null) {
            channel.dispose();
        }
    }

    private void tick() {
        final var current = channel;

        if (current == null) {
            return;
        }

        final var player = current.player();
        final var duration = current.durationSeconds();
        final double position;

        if (controlsDisabled) {
            position = followPositionSeconds;
            seekSlider.setValue(position);
        } else if (player.isPlaying() && !transport.seeking()) {
            position = player.positionSeconds();
            seekSlider.setValue(position);
        } else {
            position = seekSlider.getValue();
        }

        final var finished =
                wasPlaying && !player.isPlaying() && position >= duration - END_TOLERANCE_SECONDS;

        if (finished) {
            seekSlider.setValue(duration);
        }

        final var shownPosition = finished ? duration : position;
        transport.showTime(shownPosition, duration);
        onPositionUpdate.accept(shownPosition);
        wasPlaying = player.isPlaying();
        updatePlayPauseButton();
    }

    private void togglePlayPause() {
        final var current = channel;

        if (current == null || controlsDisabled) {
            return;
        }

        final var player = current.player();

        if (player.isPlaying()) {
            player.pause();
        } else {
            player.play();
        }

        updatePlayPauseButton();
    }

    private void stop() {
        final var current = channel;

        if (current == null || controlsDisabled) {
            return;
        }

        current.player().stop();
        seekSlider.setValue(0.0);
        transport.showTime(0.0, current.durationSeconds());
        onPositionUpdate.accept(0.0);
        updatePlayPauseButton();
    }

    private void applyControlsDisabled() {
        playPauseButton.setDisable(controlsDisabled);
        stopButton.setDisable(controlsDisabled);
        seekSlider.setDisable(controlsDisabled);
    }

    private void updatePlayPauseButton() {
        final var playing = channel != null && channel.player().isPlaying();
        final var text = playing ? Messages.get(KEY_BUTTON_PAUSE) : Messages.get(KEY_BUTTON_PLAY);

        if (!text.equals(playPauseButton.getText())) {
            playPauseButton.setText(text);
        }
    }
}
