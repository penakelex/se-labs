package org.nstu.se.lab4.ui;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.nstu.se.lab4.Messages;
import org.nstu.se.lab4.audio.playback.PlaybackController;
import org.nstu.se.lab4.audio.source.WaveSource;
import org.nstu.se.lab4.ui.dialog.AddToneDialog;
import org.nstu.se.lab4.ui.dialog.SourceFileChooser;
import org.nstu.se.lab4.ui.monitor.MonitorWindows;

import java.net.URL;
import java.util.ResourceBundle;

@SuppressWarnings("NullAway.Init")
public final class MainController implements Initializable {
    private static final String KEY_TIME_FORMAT = "time.format";
    private static final String KEY_BUTTON_PLAY = "button.play";
    private static final String KEY_BUTTON_PAUSE = "button.pause";
    private static final String KEY_TONE_NAME = "source.tone";
    private static final int TONE_SAMPLE_RATE = 44_100;

    @FXML private VBox mainPane;
    @FXML private WaveformView waveformView;
    @FXML private Button playPauseButton;
    @FXML private Slider seekSlider;
    @FXML private Label timeLabel;
    @FXML private VBox sourceControls;
    @FXML private HBox rangesContainer;

    private final PlaybackController controller = new PlaybackController();
    private final MonitorWindows monitors = new MonitorWindows();

    private SourceRowsPanel sourceRows;

    private boolean isSeeking;

    private final AnimationTimer timer =
            new AnimationTimer() {
                @Override
                public void handle(final long now) {
                    tick();
                }
            };

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        sourceRows =
                new SourceRowsPanel(
                        sourceControls,
                        controller,
                        monitors,
                        this::rebuildAudioAndWaveform,
                        this::onSourceStructureChanged);
        final var rangesPanel =
                new SliderRangesPanel(
                        rangesContainer,
                        ranges -> {
                            if (!ranges.equals(controller.session().sliderRanges())) {
                                controller.session().updateSliderRanges(ranges);
                                onSourceStructureChanged();
                            }
                        });
        rangesPanel.initDefaults(controller.session().sliderRanges());

        mainPane.addEventFilter(
                KeyEvent.KEY_PRESSED,
                event -> {
                    final var code = event.getCode();

                    if (code == KeyCode.ESCAPE || (code == KeyCode.ENTER && isTextFieldFocused())) {
                        mainPane.requestFocus();
                    }
                });
        mainPane.addEventFilter(
                MouseEvent.MOUSE_PRESSED,
                event -> {
                    if (!(event.getTarget() instanceof TextField)) {
                        mainPane.requestFocus();
                    }
                });

        final var state = controller.state();
        seekSlider.setMin(0.0);
        seekSlider.setMax(state.durationSeconds());
        seekSlider.setOnMousePressed(_ -> isSeeking = true);
        seekSlider.setOnMouseReleased(
                _ -> {
                    isSeeking = false;
                    controller.session().player().seek(seekSlider.getValue());
                });

        timeLabel.setText(Messages.format(KEY_TIME_FORMAT, 0.0, state.durationSeconds()));
        waveformView.setSource(state.signalSource());

        timer.start();
    }

    private boolean isTextFieldFocused() {
        return mainPane.getScene().getFocusOwner() instanceof TextField;
    }

    private void onSourceStructureChanged() {
        sourceRows.rebuild();
        rebuildAudioAndWaveform();
    }

    private void rebuildAudioAndWaveform() {
        final var state = controller.state();
        final var position = Math.min(state.positionSeconds(), state.durationSeconds());
        controller.session().rebuild();

        final var rebuilt = controller.state();
        seekSlider.setMax(rebuilt.durationSeconds());
        controller.session().player().seek(position);
        seekSlider.setValue(position);

        if (state.playing()) {
            controller.session().player().play();
        }

        waveformView.setSource(rebuilt.signalSource());
    }

    private void tick() {
        final var state = controller.state();
        final double position;

        if (state.playing() && !isSeeking) {
            position = state.positionSeconds();
            seekSlider.setValue(position);
        } else {
            position = seekSlider.getValue();
        }

        timeLabel.setText(Messages.format(KEY_TIME_FORMAT, position, state.durationSeconds()));
        waveformView.setPosition(position);
        updatePlayPauseButton();
        controller.broadcastState();
    }

    @FXML
    public void togglePlayPause() {
        if (controller.state().playing()) {
            controller.session().player().pause();
        } else {
            controller.session().player().play();
        }

        updatePlayPauseButton();
    }

    @FXML
    public void stop() {
        controller.session().player().stop();
        seekSlider.setValue(0.0);
        timeLabel.setText(
                Messages.format(KEY_TIME_FORMAT, 0.0, controller.state().durationSeconds()));
        waveformView.setPosition(0.0);
        updatePlayPauseButton();
    }

    @FXML
    public void addFromFile() {
        final var window = mainPane.getScene().getWindow();
        final var picked = SourceFileChooser.pickAndLoad(window);

        if (picked.isEmpty()) {
            return;
        }

        controller.session().addSource(picked.get().source(), picked.get().name());
        onSourceStructureChanged();
    }

    @FXML
    public void addTone() {
        final var window = mainPane.getScene().getWindow();
        final var spec = AddToneDialog.show(window);

        if (spec.isEmpty()) {
            return;
        }

        final var tone = spec.get();
        final var source =
                WaveSource.generateTone(tone.frequency(), tone.durationSeconds(), TONE_SAMPLE_RATE);
        controller.session().addSource(source, Messages.format(KEY_TONE_NAME, tone.frequency()));
        onSourceStructureChanged();
    }

    private void updatePlayPauseButton() {
        playPauseButton.setText(
                controller.state().playing()
                        ? Messages.get(KEY_BUTTON_PAUSE)
                        : Messages.get(KEY_BUTTON_PLAY));
    }

    public void shutdown() {
        timer.stop();
        monitors.closeAll();
        controller.shutdown();
    }
}
