package org.nstu.se.lab4.ui.dialog;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

import org.jspecify.annotations.Nullable;
import org.nstu.se.lab4.Messages;

import java.util.Optional;

public final class AddToneDialog {
    private static final String KEY_TITLE = "dialog.tone.title";
    private static final String KEY_FREQUENCY = "dialog.tone.frequency";
    private static final String KEY_DURATION = "dialog.tone.duration";
    private static final String KEY_ADD = "dialog.tone.add";
    private static final String KEY_CANCEL = "dialog.tone.cancel";
    private static final String STYLE_BUTTON = "dialog-button";
    private static final String STYLE_BUTTON_PRIMARY = "dialog-button-primary";
    private static final String STYLE_BUTTON_SECONDARY = "dialog-button-secondary";
    private static final String STYLE_FIELD = "range-field";
    private static final double DEFAULT_FREQUENCY = 440.0;
    private static final double DEFAULT_DURATION_SECONDS = 3.0;
    private static final double MIN_FREQUENCY = 20.0;
    private static final double MAX_FREQUENCY = 20_000.0;
    private static final double MIN_DURATION_SECONDS = 0.1;
    private static final double MAX_DURATION_SECONDS = 60.0;
    private static final int GRID_GAP = 8;
    private static final int GRID_PADDING = 12;

    public record ToneSpec(double frequency, double durationSeconds) {}

    private AddToneDialog() {}

    public static Optional<ToneSpec> show(final Window owner) {
        final var dialog = new Dialog<ToneSpec>();
        dialog.initOwner(owner);
        dialog.setTitle(Messages.get(KEY_TITLE));

        final var addButtonType =
                new ButtonType(Messages.get(KEY_ADD), ButtonBar.ButtonData.OK_DONE);
        final var cancelButtonType =
                new ButtonType(Messages.get(KEY_CANCEL), ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, cancelButtonType);

        final var addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.getStyleClass().addAll(STYLE_BUTTON, STYLE_BUTTON_PRIMARY);
        final var cancelButton = dialog.getDialogPane().lookupButton(cancelButtonType);
        cancelButton.getStyleClass().addAll(STYLE_BUTTON, STYLE_BUTTON_SECONDARY);

        final var frequencyField = new TextField(String.valueOf(DEFAULT_FREQUENCY));
        final var durationField = new TextField(String.valueOf(DEFAULT_DURATION_SECONDS));
        frequencyField.getStyleClass().add(STYLE_FIELD);
        durationField.getStyleClass().add(STYLE_FIELD);

        final var grid = new GridPane();
        grid.setHgap(GRID_GAP);
        grid.setVgap(GRID_GAP);
        grid.setPadding(new Insets(GRID_PADDING));
        grid.addRow(0, new Label(Messages.get(KEY_FREQUENCY)), frequencyField);
        grid.addRow(1, new Label(Messages.get(KEY_DURATION)), durationField);
        dialog.getDialogPane().setContent(grid);

        addButton.disableProperty().bind(frequencyField.textProperty().isEmpty());

        dialog.setResultConverter(
                button -> {
                    if (button != addButtonType) {
                        return null;
                    }

                    return createSpec(frequencyField.getText(), durationField.getText());
                });

        Platform.runLater(frequencyField::requestFocus);
        return dialog.showAndWait();
    }

    private static @Nullable ToneSpec createSpec(
            final String frequencyText, final String durationText) {
        final var frequency = parse(frequencyText);
        final var duration = parse(durationText);

        if (frequency == null || frequency < MIN_FREQUENCY || frequency > MAX_FREQUENCY) {
            return null;
        }

        if (duration == null
                || duration < MIN_DURATION_SECONDS
                || duration > MAX_DURATION_SECONDS) {
            return null;
        }

        return new ToneSpec(frequency, duration);
    }

    private static @Nullable Double parse(final String text) {
        try {
            return Double.valueOf(text.trim());
        } catch (final NumberFormatException exception) {
            return null;
        }
    }
}
