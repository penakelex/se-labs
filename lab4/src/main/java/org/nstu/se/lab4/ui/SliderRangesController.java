package org.nstu.se.lab4.ui;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import org.jspecify.annotations.Nullable;
import org.nstu.se.lab4.Messages;
import org.nstu.se.lab4.audio.mix.SliderRanges;

import java.util.function.Consumer;

@SuppressWarnings("NullAway.Init")
public final class SliderRangesController {
    private static final int RANGES_DEBOUNCE_MILLIS = 400;
    private static final String KEY_AMPLITUDE = "label.range.amplitude";
    private static final String KEY_DELAY = "label.range.delay";
    private static final String KEY_FROM = "label.range.from";
    private static final String KEY_TO = "label.range.to";

    @FXML private Label amplitudeLabel;
    @FXML private Label amplitudeFromLabel;
    @FXML private TextField minAmplitudeField;
    @FXML private Label amplitudeToLabel;
    @FXML private TextField maxAmplitudeField;
    @FXML private Label delayLabel;
    @FXML private Label delayFromLabel;
    @FXML private TextField minDelayField;
    @FXML private Label delayToLabel;
    @FXML private TextField maxDelayField;

    private final PauseTransition debounce =
            new PauseTransition(Duration.millis(RANGES_DEBOUNCE_MILLIS));

    private @Nullable Consumer<SliderRanges> onRanges;

    @FXML
    public void initialize() {
        amplitudeLabel.setText(Messages.get(KEY_AMPLITUDE));
        amplitudeFromLabel.setText(Messages.get(KEY_FROM));
        amplitudeToLabel.setText(Messages.get(KEY_TO));
        delayLabel.setText(Messages.get(KEY_DELAY));
        delayFromLabel.setText(Messages.get(KEY_FROM));
        delayToLabel.setText(Messages.get(KEY_TO));

        debounce.setOnFinished(_ -> apply());
        minAmplitudeField.textProperty().addListener((_, _, _) -> debounce.playFromStart());
        maxAmplitudeField.textProperty().addListener((_, _, _) -> debounce.playFromStart());
        minDelayField.textProperty().addListener((_, _, _) -> debounce.playFromStart());
        maxDelayField.textProperty().addListener((_, _, _) -> debounce.playFromStart());
    }

    void configure(final Consumer<SliderRanges> onRanges) {
        this.onRanges = onRanges;
    }

    void initDefaults(final SliderRanges ranges) {
        minAmplitudeField.setText(String.valueOf(ranges.minAmplitude()));
        maxAmplitudeField.setText(String.valueOf(ranges.maxAmplitude()));
        minDelayField.setText(String.valueOf(ranges.minDelaySeconds()));
        maxDelayField.setText(String.valueOf(ranges.maxDelaySeconds()));
    }

    private void apply() {
        final var ranges =
                SliderRanges.tryCreate(
                        parse(minAmplitudeField.getText()),
                        parse(maxAmplitudeField.getText()),
                        parse(minDelayField.getText()),
                        parse(maxDelayField.getText()));

        if (ranges != null && onRanges != null) {
            onRanges.accept(ranges);
        }
    }

    private static @Nullable Double parse(final String text) {
        try {
            return Double.valueOf(text.trim().replace(',', '.'));
        } catch (final NumberFormatException exception) {
            return null;
        }
    }
}
