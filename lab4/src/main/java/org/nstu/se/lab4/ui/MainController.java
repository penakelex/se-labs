package org.nstu.se.lab4.ui;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import org.nstu.se.lab4.Messages;
import org.nstu.se.lab4.ResourcePaths;
import org.nstu.se.lab4.audio.PlaybackController;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@SuppressWarnings("NullAway.Init")
public final class MainController implements Initializable {
    private static final String KEY_TIME_FORMAT = "time.format";
    private static final String KEY_BUTTON_PLAY = "button.play";
    private static final String KEY_BUTTON_PAUSE = "button.pause";
    private static final double END_TOLERANCE_SECONDS = 0.1;

    @FXML private VBox mainPane;
    @FXML private WaveformView waveformView;
    @FXML private Button playPauseButton;
    @FXML private Slider seekSlider;
    @FXML private Label timeLabel;
    @FXML private VBox sourceControls;

    private final PlaybackController controller = new PlaybackController();
    private final PauseTransition rebuildDebounce = new PauseTransition(Duration.millis(100));

    private boolean isSeeking;
    private boolean wasPlaying;

    private final AnimationTimer timer =
            new AnimationTimer() {
                @Override
                public void handle(final long now) {
                    tick();
                }
            };

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        buildSourceControls();

        mainPane.addEventFilter(
                KeyEvent.KEY_PRESSED,
                event -> {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        mainPane.requestFocus();
                    }
                });

        final var state = controller.getState();
        seekSlider.setMin(0.0);
        seekSlider.setMax(state.durationSeconds());
        seekSlider.setOnMousePressed(_ -> isSeeking = true);
        seekSlider.setOnMouseReleased(
                _ -> {
                    isSeeking = false;
                    controller.seek(seekSlider.getValue());
                });

        rebuildDebounce.setOnFinished(_ -> rebuildAudioAndWaveform());

        timeLabel.setText(Messages.format(KEY_TIME_FORMAT, 0.0, state.durationSeconds()));
        waveformView.setSource(state.signalSource());

        timer.start();
    }

    private void buildSourceControls() {
        for (var index = 0; index < controller.getSettings().getSourceCount(); index++) {
            sourceControls.getChildren().add(loadSourceRow(index));
        }
    }

    private HBox loadSourceRow(final int index) {
        final var loader = new FXMLLoader(ResourcePaths.url(ResourcePaths.SOURCE_ROW_VIEW));
        try {
            final var row = (HBox) loader.load();
            final var rowController = (SourceRowController) loader.getController();
            rowController.configure(index, controller, rebuildDebounce::playFromStart);
            return row;
        } catch (final IOException exception) {
            throw new IllegalStateException("Cannot load source row", exception);
        }
    }

    private void rebuildAudioAndWaveform() {
        final var state = controller.getState();
        final var position = Math.min(state.positionSeconds(), state.durationSeconds());
        controller.rebuildAudio();
        final var rebuilt = controller.getState();
        seekSlider.setMax(rebuilt.durationSeconds());
        controller.seek(position);
        seekSlider.setValue(position);
        if (state.playing()) {
            controller.play();
        }
        waveformView.setSource(rebuilt.signalSource());
    }

    private void tick() {
        final var state = controller.getState();
        final double position;
        if (state.playing() && !isSeeking) {
            position = state.positionSeconds();
            seekSlider.setValue(position);
        } else {
            position = seekSlider.getValue();
        }
        timeLabel.setText(Messages.format(KEY_TIME_FORMAT, position, state.durationSeconds()));
        waveformView.setPosition(position);
        if (wasPlaying
                && !state.playing()
                && position >= state.durationSeconds() - END_TOLERANCE_SECONDS) {
            seekSlider.setValue(state.durationSeconds());
            timeLabel.setText(
                    Messages.format(
                            KEY_TIME_FORMAT, state.durationSeconds(), state.durationSeconds()));
            waveformView.setPosition(state.durationSeconds());
        }
        wasPlaying = state.playing();
        updatePlayPauseButton();
    }

    @FXML
    public void togglePlayPause() {
        if (controller.getState().playing()) {
            controller.pause();
        } else {
            controller.play();
        }
        updatePlayPauseButton();
    }

    @FXML
    public void stop() {
        controller.stop();
        seekSlider.setValue(0.0);
        timeLabel.setText(
                Messages.format(KEY_TIME_FORMAT, 0.0, controller.getState().durationSeconds()));
        waveformView.setPosition(0.0);
        updatePlayPauseButton();
    }

    private void updatePlayPauseButton() {
        playPauseButton.setText(
                controller.getState().playing()
                        ? Messages.get(KEY_BUTTON_PAUSE)
                        : Messages.get(KEY_BUTTON_PLAY));
    }

    public void shutdown() {
        timer.stop();
        rebuildDebounce.stop();
        controller.shutdown();
    }
}
