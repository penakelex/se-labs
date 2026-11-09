package org.nstu.se.lab3.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DataAccumulatorTest {
    @Test
    @DisplayName("Сохраняет добавленные значения")
    void storesValues() {
        final var accumulator = new DataAccumulator(100);
        accumulator.addValue(1);
        accumulator.addValue(2);
        assertThat(accumulator.getAccumulatedData())
                .extracting(HistoryEntry::value)
                .containsExactly(1, 2);
    }

    @Test
    @DisplayName("Возвращает неизменяемую копию")
    void returnsImmutableCopy() {
        final var accumulator = new DataAccumulator(100);
        accumulator.addValue(1);
        final var values = accumulator.getAccumulatedData();
        assertThatThrownBy(() -> values.add(new HistoryEntry(2, 2)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Очищает накопленные значения")
    void clearsValues() {
        final var accumulator = new DataAccumulator(100);
        accumulator.addValue(1);
        accumulator.clear();
        assertThat(accumulator.getAccumulatedData()).isEmpty();
    }

    @Test
    @DisplayName("Обрезает историю при превышении лимита")
    void limitsHistorySize() {
        final var accumulator = new DataAccumulator(5);

        for (var index = 0; index < 10; index++) {
            accumulator.addValue(index);
        }

        assertThat(accumulator.getAccumulatedData()).hasSize(5);
    }

    @Test
    @DisplayName("Сохраняет сквозной ID при обрезке истории")
    void retainsContinuousIds() {
        final var accumulator = new DataAccumulator(3);

        for (var index = 0; index < 5; index++) {
            accumulator.addValue(index);
        }

        assertThat(accumulator.getAccumulatedData())
                .hasSize(3)
                .extracting(HistoryEntry::identifier)
                .containsExactly(3, 4, 5);
    }

    @Test
    @DisplayName("Динамически изменяет лимит")
    void changesLimitDynamically() {
        final var accumulator = new DataAccumulator(10);

        for (var index = 0; index < 10; index++) {
            accumulator.addValue(index);
        }

        accumulator.setMaxSize(5);
        assertThat(accumulator.getAccumulatedData()).hasSize(5);
    }
}
