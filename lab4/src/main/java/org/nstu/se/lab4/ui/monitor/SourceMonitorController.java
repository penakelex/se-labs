package org.nstu.se.lab4.ui.monitor;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;

import org.jspecify.annotations.Nullable;
import org.nstu.se.lab4.audio.event.PlaybackListener;
import org.nstu.se.lab4.audio.event.PlaybackState;
import org.nstu.se.lab4.audio.playback.PlaybackController;
import org.nstu.se.lab4.ui.WaveformView;

import java.net.URL;
import java.util.ResourceBundle;

@SuppressWarnings("NullAway.Init")
public final class SourceMonitorController implements Initializable, PlaybackListener {
    @FXML private ToggleButton graphToggle;
    @FXML private ToggleButton scaleToggle;
    @FXML private WaveformView waveformView;
    @FXML private LevelMeterView meterView;
    @FXML private Button playPauseButton;
    @FXML private Button stopButton;
    @FXML private Slider seekSlider;
    @FXML private Label timeLabel;

    private MonitorDisplay display;
    private MonitorPlayback playback;
    private @Nullable PlaybackController model;

    private boolean disposed;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        display = new MonitorDisplay(graphToggle, scaleToggle, waveformView, meterView);
        playback =
                new MonitorPlayback(
                        seekSlider,
                        timeLabel,
                        playPauseButton,
                        stopButton,
                        display::show,
                        this::onChannelRebuilt);
    }

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "PlaybackController is intentionally shared with the monitor window")
    public void configure(final int index, final PlaybackController playbackController) {
        this.model = playbackController;

        final var channel = new MonitorChannel(index, playbackController);
        playback.configure(channel);
        display.setSignal(channel.signal());
        display.show(0.0);

        playbackController.addPlaybackListener(this);
    }

    public void dispose() {
        if (disposed) {
            return;
        }

        disposed = true;
        playback.dispose();

        if (model != null) {
            model.removePlaybackListener(this);
        }
    }

    @Override
    public void onPlaybackState(final PlaybackState state) {
        playback.handlePlaybackState(state);
    }

    @Override
    public void onPlaybackFinished() {
        playback.syncButton();
    }

    private void onChannelRebuilt(final MonitorChannel channel) {
        display.setSignal(channel.signal());
    }
}
