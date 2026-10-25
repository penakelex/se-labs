package org.nstu.se.lab1.ui.menu;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import org.jspecify.annotations.Nullable;
import org.nstu.se.lab1.Messages;

import java.util.Arrays;
import java.util.Random;

public final class ArrayInputParser {
    private static final int MAX_SIZE = 25;
    private static final int MAX_ABS_VALUE = 10_000;
    private static final String INVALID_STYLE = "invalid";
    private static final String KEY_ERROR_SIZE_LIMIT = "menu.error.size.limit";
    private static final String KEY_ERROR_MAGNITUDE = "menu.error.range.magnitude";

    private ArrayInputParser() {}

    public static Integer[] parseManual(final TextField arrayField, final Label arrayError) {
        final var raw = arrayField.getText();
        final var tokens =
                Arrays.stream(raw.split("[,\\s]+"))
                        .map(String::trim)
                        .filter(token -> !token.isEmpty())
                        .toList();

        if (tokens.isEmpty()) {
            clearError(arrayField, arrayError);
            return new Integer[0];
        }

        if (tokens.size() > MAX_SIZE) {
            markError(arrayField, arrayError, Messages.format(KEY_ERROR_SIZE_LIMIT, MAX_SIZE));
            return new Integer[0];
        }

        try {
            final var parsed = tokens.stream().map(Integer::parseInt).toArray(Integer[]::new);

            for (final var value : parsed) {
                if (Math.abs(value) > MAX_ABS_VALUE) {
                    markError(
                            arrayField,
                            arrayError,
                            Messages.format(KEY_ERROR_MAGNITUDE, MAX_ABS_VALUE));
                    return new Integer[0];
                }
            }

            clearError(arrayField, arrayError);
            return parsed;
        } catch (final NumberFormatException exception) {
            markError(arrayField, arrayError, Messages.get("menu.error.manual.invalid"));
            return new Integer[0];
        }
    }

    public static Integer[] parseGeneration(
            final TextField countField,
            final Label countError,
            final TextField smallNumber,
            final Label smallNumberError,
            final TextField bigNumber,
            final Label bigNumberError) {
        final var count = validateCount(countField, countError);
        final var minimum = tryParse(smallNumber, smallNumberError, "menu.error.from.integer");
        final var maximum = tryParse(bigNumber, bigNumberError, "menu.error.to.integer");

        if (count == null || minimum == null || maximum == null) {
            return new Integer[0];
        }

        if (minimum > maximum) {
            markError(smallNumber, smallNumberError, Messages.get("menu.error.range.order"));
            return new Integer[0];
        }

        if (Math.abs(minimum) > MAX_ABS_VALUE || Math.abs(maximum) > MAX_ABS_VALUE) {
            markError(
                    bigNumber, bigNumberError, Messages.format(KEY_ERROR_MAGNITUDE, MAX_ABS_VALUE));
            return new Integer[0];
        }

        return new Random().ints(count, minimum, maximum + 1).boxed().toArray(Integer[]::new);
    }

    private static @Nullable Integer validateCount(
            final TextField countField, final Label countError) {
        final var value = tryParse(countField, countError, "menu.error.count.integer");

        if (value == null) {
            return null;
        }

        if (value <= 0) {
            markError(countField, countError, Messages.get("menu.error.count.positive"));
            return null;
        }

        if (value > MAX_SIZE) {
            markError(countField, countError, Messages.format(KEY_ERROR_SIZE_LIMIT, MAX_SIZE));
            return null;
        }

        clearError(countField, countError);
        return value;
    }

    private static @Nullable Integer tryParse(
            final TextField field, final Label error, final String messageKey) {
        final var text = field.getText();

        if (text.isBlank()) {
            clearError(field, error);
            return null;
        }

        try {
            final var parsed = Integer.parseInt(text);
            clearError(field, error);
            return parsed;
        } catch (final NumberFormatException exception) {
            markError(field, error, Messages.get(messageKey));
            return null;
        }
    }

    public static void markError(final TextField field, final Label error, final String message) {
        if (!field.getStyleClass().contains(INVALID_STYLE)) {
            field.getStyleClass().add(INVALID_STYLE);
        }

        error.setText(message);
        error.setVisible(true);
        error.setManaged(true);
    }

    public static void clearError(final TextField field, final Label error) {
        field.getStyleClass().remove(INVALID_STYLE);
        error.setVisible(false);
        error.setManaged(false);
    }
}
