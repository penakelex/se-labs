package org.nstu.se.lab4.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

import org.nstu.se.lab4.Messages;
import org.nstu.se.lab4.audio.PlaybackController;
import org.nstu.se.lab4.audio.event.PlaybackListener;
import org.nstu.se.lab4.audio.event.PlaybackState;

import java.net.URL;
import java.util.ResourceBundle;

@SuppressWarnings("NullAway.Init")
public final class MainController implements Initializable, PlaybackListener {
    private static final double AMPLITUDE_MIN = 0.0;
    private static final double AMPLITUDE_MAX = 1.0;
    private static final double DELAY_MIN = 0.0;
    private static final double DELAY_MAX = 0.5;

    private static final String KEY_TIME_FORMAT = "time.format";
    private static final String KEY_SAMPLE_FORMAT = "sample.format";
    private static final String KEY_BUTTON_PLAY = "button.play";
    private static final String KEY_BUTTON_PAUSE = "button.pause";
    private static final String KEY_AMPLITUDE_VALUE = "amplitude.value";
    private static final String KEY_DELAY_VALUE = "delay.value";
    private static final String KEY_SOURCE_NAME = "source.name";

    @FXML private WaveformView waveformView;
    @FXML private Button playPauseButton;
    @FXML private Slider seekSlider;
    @FXML private Label timeLabel;
    @FXML private Label sampleLabel;
    @FXML private ComboBox<String> sourceSelector;
    @FXML private Slider amplitudeSlider;
    @FXML private Slider delaySlider;
    @FXML private Label amplitudeValueLabel;
    @FXML private Label delayValueLabel;

    private final PlaybackController controller = new PlaybackController(this);
    private int selectedSourceIndex;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        waveformView.setSource(controller);

        for (var index = 0; index < controller.getSourceCount(); index++) {
            sourceSelector.getItems().add(Messages.format(KEY_SOURCE_NAME, index + 1));
        }
        sourceSelector.getSelectionModel().selectFirst();
        sourceSelector.valueProperty().addListener((_, _, _) -> updateSourceControls());

        seekSlider.setMin(0.0);
        seekSlider.setMax(controller.getDurationSeconds());
        seekSlider
                .valueChangingProperty()
                .addListener(
                        (_, _, changing) -> {
                            if (!changing) {
                                controller.seek(seekSlider.getValue());
                            }
                        });

        amplitudeSlider.setMin(AMPLITUDE_MIN);
        amplitudeSlider.setMax(AMPLITUDE_MAX);
        amplitudeSlider
                .valueProperty()
                .addListener(
                        (_, _, newValue) -> {
                            controller.setAmplitude(selectedSourceIndex, newValue.doubleValue());
                            amplitudeValueLabel.setText(
                                    Messages.format(KEY_AMPLITUDE_VALUE, newValue.doubleValue()));
                            waveformView.refresh();
                        });

        delaySlider.setMin(DELAY_MIN);
        delaySlider.setMax(DELAY_MAX);
        delaySlider
                .valueProperty()
                .addListener(
                        (_, _, newValue) -> {
                            controller.setDelaySeconds(selectedSourceIndex, newValue.doubleValue());
                            delayValueLabel.setText(
                                    Messages.format(KEY_DELAY_VALUE, newValue.doubleValue()));
                            waveformView.refresh();
                        });

        timeLabel.setText(Messages.format(KEY_TIME_FORMAT, 0.0, controller.getDurationSeconds()));
        sampleLabel.setText(Messages.format(KEY_SAMPLE_FORMAT, 0.0));

        updateSourceControls();
    }

    private void updateSourceControls() {
        selectedSourceIndex = sourceSelector.getSelectionModel().getSelectedIndex();
        amplitudeSlider.setValue(controller.getAmplitude(selectedSourceIndex));
        delaySlider.setValue(controller.getDelaySeconds(selectedSourceIndex));
    }

    @FXML
    public void togglePlayPause() {
        if (controller.isPlaying()) {
            controller.pause();
        } else {
            controller.play();
        }
    }

    @FXML
    public void stop() {
        controller.stop();
    }

    @Override
    public void onPlaybackState(final PlaybackState state) {
        Platform.runLater(
                () -> {
                    seekSlider.setValue(state.positionSeconds());
                    timeLabel.setText(
                            Messages.format(
                                    KEY_TIME_FORMAT,
                                    state.positionSeconds(),
                                    state.durationSeconds()));
                    sampleLabel.setText(Messages.format(KEY_SAMPLE_FORMAT, state.currentSample()));
                    waveformView.setPosition(state.positionSeconds());
                    playPauseButton.setText(
                            state.playing()
                                    ? Messages.get(KEY_BUTTON_PAUSE)
                                    : Messages.get(KEY_BUTTON_PLAY));
                });
    }

    @Override
    public void onPlaybackFinished() {
        Platform.runLater(() -> playPauseButton.setText(Messages.get(KEY_BUTTON_PLAY)));
    }

    public void shutdown() {
        controller.shutdown();
    }
}
