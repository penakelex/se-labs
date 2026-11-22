package org.nstu.se.lab4.ui;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import org.jspecify.annotations.Nullable;
import org.nstu.se.lab4.Messages;
import org.nstu.se.lab4.audio.mix.SliderRanges;

import java.util.function.Consumer;

final class SliderRangesPanel {
    private static final int RANGES_DEBOUNCE_MILLIS = 400;
    private static final int FIELD_COLUMNS = 5;
    private static final String KEY_TO = "label.range.to";
    private static final String STYLE_FIELD = "range-field";

    private final HBox container;
    private final Consumer<SliderRanges> onRanges;

    private final TextField minAmplitudeField = new TextField();
    private final TextField maxAmplitudeField = new TextField();
    private final TextField minDelayField = new TextField();
    private final TextField maxDelayField = new TextField();
    private final PauseTransition debounce =
            new PauseTransition(Duration.millis(RANGES_DEBOUNCE_MILLIS));

    SliderRangesPanel(final HBox container, final Consumer<SliderRanges> onRanges) {
        this.container = container;
        this.onRanges = onRanges;
        debounce.setOnFinished(_ -> apply());
        buildLayout();
    }

    void initDefaults(final SliderRanges ranges) {
        minAmplitudeField.setText(String.valueOf(ranges.minAmplitude()));
        maxAmplitudeField.setText(String.valueOf(ranges.maxAmplitude()));
        minDelayField.setText(String.valueOf(ranges.minDelaySeconds()));
        maxDelayField.setText(String.valueOf(ranges.maxDelaySeconds()));
    }

    private void buildLayout() {
        minAmplitudeField.setPrefColumnCount(FIELD_COLUMNS);
        maxAmplitudeField.setPrefColumnCount(FIELD_COLUMNS);
        minDelayField.setPrefColumnCount(FIELD_COLUMNS);
        maxDelayField.setPrefColumnCount(FIELD_COLUMNS);
        minAmplitudeField.getStyleClass().add(STYLE_FIELD);
        maxAmplitudeField.getStyleClass().add(STYLE_FIELD);
        minDelayField.getStyleClass().add(STYLE_FIELD);
        maxDelayField.getStyleClass().add(STYLE_FIELD);

        container.setSpacing(8);
        container.setPadding(new Insets(4, 0, 0, 0));
        container
                .getChildren()
                .addAll(
                        new Label(Messages.get("label.range.amplitude")),
                        minAmplitudeField,
                        new Label(Messages.get(KEY_TO)),
                        maxAmplitudeField,
                        new Label(Messages.get("label.range.delay")),
                        minDelayField,
                        new Label(Messages.get(KEY_TO)),
                        maxDelayField);

        minAmplitudeField.textProperty().addListener((_, _, _) -> debounce.playFromStart());
        maxAmplitudeField.textProperty().addListener((_, _, _) -> debounce.playFromStart());
        minDelayField.textProperty().addListener((_, _, _) -> debounce.playFromStart());
        maxDelayField.textProperty().addListener((_, _, _) -> debounce.playFromStart());
    }

    private void apply() {
        final var ranges =
                SliderRanges.tryCreate(
                        parse(minAmplitudeField.getText()),
                        parse(maxAmplitudeField.getText()),
                        parse(minDelayField.getText()),
                        parse(maxDelayField.getText()));

        if (ranges != null) {
            onRanges.accept(ranges);
        }
    }

    private static @Nullable Double parse(final String text) {
        try {
            return Double.valueOf(text.trim());
        } catch (final NumberFormatException exception) {
            return null;
        }
    }
}
