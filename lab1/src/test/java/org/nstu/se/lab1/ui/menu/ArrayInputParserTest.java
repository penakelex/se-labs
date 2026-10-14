package org.nstu.se.lab1.ui.menu;

import static org.assertj.core.api.Assertions.assertThat;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nstu.se.lab1.ui.JavaFxTestBase;

class ArrayInputParserTest extends JavaFxTestBase {
    private static final String STYLE_INVALID = "invalid";
    private static final String INVALID_INPUT = "10, abc, 30";

    @Test
    @DisplayName("Парсинг корректного ручного ввода")
    void testParseManualValid() {
        final var result =
                runOnFxThread(
                        () -> {
                            final var textField = new TextField("10, 20  30,40");
                            final var errorLabel = new Label();
                            return ArrayInputParser.parseManual(textField, errorLabel);
                        });

        assertThat(result).containsExactly(10, 20, 30, 40);
    }

    @Test
    @DisplayName("Проверка скрытия ошибки при корректном вводе")
    void testParseManualValidNoErrors() {
        final var errorLabel =
                runOnFxThread(
                        () -> {
                            final var textField = new TextField("10, 20");
                            final var label = new Label();
                            ArrayInputParser.parseManual(textField, label);
                            return label;
                        });

        assertThat(errorLabel.isVisible()).isFalse();
    }

    @Test
    @DisplayName("Парсинг некорректного ручного ввода (текст вместо чисел)")
    void testParseManualInvalidText() {
        final var result =
                runOnFxThread(
                        () -> {
                            final var textField = new TextField(INVALID_INPUT);
                            final var errorLabel = new Label();
                            return ArrayInputParser.parseManual(textField, errorLabel);
                        });

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Проверка установки стиля invalid при ошибке ввода")
    void testParseManualInvalidStyle() {
        final var textField =
                runOnFxThread(
                        () -> {
                            final var field = new TextField(INVALID_INPUT);
                            final var errorLabel = new Label();
                            ArrayInputParser.parseManual(field, errorLabel);
                            return field;
                        });

        assertThat(textField.getStyleClass()).contains(STYLE_INVALID);
    }

    @Test
    @DisplayName("Проверка ограничения на максимальный размер массива")
    void testParseManualSizeLimit() {
        final var result =
                runOnFxThread(
                        () -> {
                            final var largeInput = "1, ".repeat(25) + "1";
                            final var textField = new TextField(largeInput);
                            final var errorLabel = new Label();
                            return ArrayInputParser.parseManual(textField, errorLabel);
                        });

        assertThat(result).isEmpty();
    }
}
