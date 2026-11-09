package org.nstu.se.lab3.ui.settings;

import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

import org.jspecify.annotations.Nullable;
import org.nstu.se.lab3.Messages;

public final class InputValidator {
    private static final String STYLE_INVALID = "invalid-input";
    private static final int TOOLTIP_DELAY_MILLIS = 100;

    private InputValidator() {}

    public static @Nullable Integer parseInteger(
            final TextField field, final String errorKey, final boolean mustBePositive) {
        try {
            final var value = Integer.parseInt(field.getText().trim());

            if (mustBePositive && value <= 0) {
                markInvalid(field, Messages.get(errorKey));
                return null;
            }

            markValid(field);
            return value;
        } catch (final NumberFormatException exception) {
            markInvalid(field, Messages.get("error.numberFormat"));
            return null;
        }
    }

    public static @Nullable Double parseDouble(final TextField field, final String errorKey) {
        try {
            final var value = Double.parseDouble(field.getText().trim());
            markValid(field);
            return value;
        } catch (final NumberFormatException exception) {
            markInvalid(field, Messages.get(errorKey));
            return null;
        }
    }

    public static void markInvalid(final TextField field, final String message) {
        if (!field.getStyleClass().contains(STYLE_INVALID)) {
            field.getStyleClass().add(STYLE_INVALID);
        }

        final var tooltip = new Tooltip(message);
        tooltip.getStyleClass().add("error-tooltip");
        tooltip.setShowDelay(Duration.millis(TOOLTIP_DELAY_MILLIS));

        Tooltip.install(field, tooltip);
    }

    public static void markValid(final TextField field) {
        field.getStyleClass().remove(STYLE_INVALID);
        final var tooltip = field.getTooltip();

        if (tooltip != null) {
            Tooltip.uninstall(field, tooltip);
            field.setTooltip(null);
        }
    }
}
