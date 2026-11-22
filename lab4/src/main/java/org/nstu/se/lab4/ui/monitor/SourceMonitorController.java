package org.nstu.se.lab4.ui.monitor;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;

import org.jspecify.annotations.Nullable;
import org.nstu.se.lab4.Messages;
import org.nstu.se.lab4.audio.event.PlaybackListener;
import org.nstu.se.lab4.audio.event.PlaybackState;
import org.nstu.se.lab4.audio.playback.PlaybackController;
import org.nstu.se.lab4.ui.WaveformView;

import java.net.URL;
import java.util.ResourceBundle;

@SuppressWarnings("NullAway.Init")
public final class SourceMonitorController implements Initializable, PlaybackListener {
    private static final String KEY_BUTTON_PLAY = "button.play";
    private static final String KEY_BUTTON_PAUSE = "button.pause";
    private static final double END_TOLERANCE_SECONDS = 0.05;

    @FXML private ToggleButton graphToggle;
    @FXML private ToggleButton scaleToggle;
    @FXML private WaveformView waveformView;
    @FXML private LevelMeterView meterView;
    @FXML private Button playPauseButton;
    @FXML private Button stopButton;
    @FXML private Slider seekSlider;
    @FXML private Label timeLabel;

    private final MonitorTransport transport = new MonitorTransport();

    private MonitorDisplay display;
    private @Nullable MonitorChannel channel;
    private @Nullable PlaybackController model;

    private boolean disposed;
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

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        display = new MonitorDisplay(graphToggle, scaleToggle, waveformView, meterView);
        transport.bind(seekSlider, timeLabel);
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

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "PlaybackController is intentionally shared with the monitor window")
    public void configure(final int index, final PlaybackController playback) {
        this.model = playback;
        this.channel = new MonitorChannel(index, playback);

        seekSlider.setMin(0.0);
        display.setSignal(channel.signal());
        transport.setDuration(channel.durationSeconds());
        transport.showTime(0.0, channel.durationSeconds());
        display.show(0.0);
        updatePlayPauseButton();

        playback.addPlaybackListener(this);
    }

    public void dispose() {
        timer.stop();

        if (disposed) {
            return;
        }

        disposed = true;

        if (model != null) {
            model.removePlaybackListener(this);
        }

        if (channel != null) {
            channel.dispose();
        }
    }

    @Override
    public void onPlaybackState(final PlaybackState state) {
        if (channel != null) {
            channel.applySettingsChanges();
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

    @Override
    public void onPlaybackFinished() {
        updatePlayPauseButton();
    }

    private void tick() {
        if (channel == null) {
            return;
        }

        final var player = channel.player();
        final var duration = channel.durationSeconds();
        final double position;

        if (controlsDisabled) {
            position = followPositionSeconds;
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
        display.show(shownPosition);
        wasPlaying = player.isPlaying();
    }

    private void togglePlayPause() {
        if (channel == null || controlsDisabled) {
            return;
        }

        final var player = channel.player();

        if (player.isPlaying()) {
            player.pause();
        } else {
            player.play();
        }

        updatePlayPauseButton();
    }

    private void stop() {
        if (channel == null || controlsDisabled) {
            return;
        }

        channel.player().stop();
        seekSlider.setValue(0.0);
        transport.showTime(0.0, channel.durationSeconds());
        display.show(0.0);
        updatePlayPauseButton();
    }

    private void applyControlsDisabled() {
        playPauseButton.setDisable(controlsDisabled);
        stopButton.setDisable(controlsDisabled);
        seekSlider.setDisable(controlsDisabled);
    }

    private void updatePlayPauseButton() {
        final var playing = channel != null && channel.player().isPlaying();
        playPauseButton.setText(
                playing ? Messages.get(KEY_BUTTON_PAUSE) : Messages.get(KEY_BUTTON_PLAY));
    }
}
