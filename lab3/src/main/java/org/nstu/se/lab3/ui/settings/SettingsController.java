package org.nstu.se.lab3.ui.settings;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import org.jspecify.annotations.Nullable;
import org.nstu.se.lab3.Messages;
import org.nstu.se.lab3.model.SettingsValues;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

@SuppressWarnings("NullAway.Init")
public final class SettingsController implements Initializable {
    private static final String KEY_ERROR_NUMBER = "error.numberFormat";
    private static final String KEY_ERROR_ZERO_LEVEL = "error.zeroLevel";
    private static final String KEY_ERROR_DECIBEL_RANGE = "error.decibelRange";
    private static final String KEY_ERROR_PERIOD = "error.period";
    private static final String KEY_ERROR_MAX_HISTORY = "error.maxHistory";
    private static final int DEBOUNCE_MILLIS = 400;

    @FXML private TextField zeroLevelField;
    @FXML private TextField minDecibelsField;
    @FXML private TextField maxDecibelsField;
    @FXML private TextField periodField;
    @FXML private TextField maxHistoryField;

    private SettingsValues currentSettings;
    private @Nullable Consumer<SettingsValues> onSettingsChanged;

    private final PauseTransition debounce = new PauseTransition(Duration.millis(DEBOUNCE_MILLIS));

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        currentSettings =
                new SettingsValues(
                        Messages.getInt("settings.default.zeroLevel"),
                        Messages.getDouble("settings.default.minDecibels"),
                        Messages.getDouble("settings.default.maxDecibels"),
                        Messages.getInt("settings.default.period"),
                        Messages.getInt("settings.default.maxHistory"));

        zeroLevelField.setText(String.valueOf(currentSettings.zeroLevelValue()));
        minDecibelsField.setText(String.valueOf(currentSettings.minDecibels()));
        maxDecibelsField.setText(String.valueOf(currentSettings.maxDecibels()));
        periodField.setText(String.valueOf(currentSettings.periodMillis()));
        maxHistoryField.setText(String.valueOf(currentSettings.maxHistorySize()));

        debounce.setOnFinished(_ -> validateAndNotify());

        zeroLevelField.textProperty().addListener((_, _, _) -> debounce.playFromStart());
        minDecibelsField.textProperty().addListener((_, _, _) -> debounce.playFromStart());
        maxDecibelsField.textProperty().addListener((_, _, _) -> debounce.playFromStart());
        periodField.textProperty().addListener((_, _, _) -> debounce.playFromStart());
        maxHistoryField.textProperty().addListener((_, _, _) -> debounce.playFromStart());

        if (onSettingsChanged != null) {
            onSettingsChanged.accept(currentSettings);
        }
    }

    public void setOnSettingsChanged(final Consumer<SettingsValues> listener) {
        this.onSettingsChanged = listener;
        listener.accept(currentSettings);
    }

    private void validateAndNotify() {
        final var parsed = readSettings();

        if (parsed == null || parsed.equals(currentSettings)) {
            return;
        }

        currentSettings = parsed;

        if (onSettingsChanged != null) {
            onSettingsChanged.accept(parsed);
        }
    }

    private @Nullable SettingsValues readSettings() {
        final var zeroLevel =
                InputValidator.parseInteger(zeroLevelField, KEY_ERROR_ZERO_LEVEL, true);
        final var minDb = InputValidator.parseDouble(minDecibelsField, KEY_ERROR_NUMBER);
        final var maxDb = InputValidator.parseDouble(maxDecibelsField, KEY_ERROR_NUMBER);
        final var period = InputValidator.parseInteger(periodField, KEY_ERROR_PERIOD, true);
        final var maxHistory =
                InputValidator.parseInteger(maxHistoryField, KEY_ERROR_MAX_HISTORY, true);

        if (!isValidDecibelRange(minDb, maxDb)) {
            return null;
        }

        if (zeroLevel == null || minDb == null || maxDb == null) {
            return null;
        }

        if (period == null || maxHistory == null) {
            return null;
        }

        return new SettingsValues(zeroLevel, minDb, maxDb, period, maxHistory);
    }

    private boolean isValidDecibelRange(
            final @Nullable Double minDb, final @Nullable Double maxDb) {
        if (minDb == null || maxDb == null) {
            return true;
        }

        if (minDb >= maxDb) {
            InputValidator.markInvalid(minDecibelsField, Messages.get(KEY_ERROR_DECIBEL_RANGE));
            InputValidator.markInvalid(maxDecibelsField, Messages.get(KEY_ERROR_DECIBEL_RANGE));
            return false;
        }

        return true;
    }
}
