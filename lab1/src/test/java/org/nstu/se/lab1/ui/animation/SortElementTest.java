package org.nstu.se.lab1.ui.animation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nstu.se.lab1.ui.JavaFxTestBase;

class SortElementTest extends JavaFxTestBase {
    private static final String ELEMENT_VALUE = "42";

    @Test
    @DisplayName("Проверка базового стиля элемента визуализации")
    void testSortElementBaseStyle() {
        final var element = runOnFxThread(() -> new SortElement(ELEMENT_VALUE, 60.0, 380.0, 150.0));

        assertThat(element.getStyleClass()).contains("sort-element");
    }

    @Test
    @DisplayName("Проверка переключения режима диапазонов")
    void testSortElementRangeState() {
        final var element =
                runOnFxThread(
                        () -> {
                            final var elementInstance =
                                    new SortElement(ELEMENT_VALUE, 60.0, 380.0, 150.0);
                            elementInstance.setInRange(true);
                            return elementInstance;
                        });

        assertThat(element.getStyleClass()).contains("sort-element-in-range");
    }

    @Test
    @DisplayName("Проверка статуса фиксации элемента")
    void testSortElementPlacedState() {
        final var element =
                runOnFxThread(
                        () -> {
                            final var elementInstance =
                                    new SortElement(ELEMENT_VALUE, 60.0, 380.0, 150.0);
                            elementInstance.setPlaced(true);
                            return elementInstance;
                        });

        assertThat(element.getStyleClass()).contains("sort-element-placed");
    }
}
