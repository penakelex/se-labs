package org.nstu.se.lab3.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nstu.se.lab3.model.DataAccumulator.Stats;

class DataAccumulatorTest {
    @Test
    @DisplayName("Сохраняет добавленные значения")
    void storesValues() {
        final var accumulator = new DataAccumulator(100);
        accumulator.addValue(1, 0.0);
        accumulator.addValue(2, 0.0);
        assertThat(accumulator.getAccumulatedData())
                .extracting(HistoryEntry::value)
                .containsExactly(1, 2);
    }

    @Test
    @DisplayName("Возвращает неизменяемую копию")
    void returnsImmutableCopy() {
        final var accumulator = new DataAccumulator(100);
        accumulator.addValue(1, 0.0);
        final var values = accumulator.getAccumulatedData();
        assertThatThrownBy(() -> values.add(new HistoryEntry(2, 2, 0.0)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Очищает накопленные значения")
    void clearsValues() {
        final var accumulator = new DataAccumulator(100);
        accumulator.addValue(1, 0.0);
        accumulator.clear();
        assertThat(accumulator.getAccumulatedData()).isEmpty();
    }

    @Test
    @DisplayName("Обрезает историю при превышении лимита")
    void limitsHistorySize() {
        final var accumulator = new DataAccumulator(5);

        for (var index = 0; index < 10; index++) {
            accumulator.addValue(index, 0.0);
        }

        assertThat(accumulator.getAccumulatedData()).hasSize(5);
    }

    @Test
    @DisplayName("Сохраняет сквозной ID при обрезке истории")
    void retainsContinuousIds() {
        final var accumulator = new DataAccumulator(3);

        for (var index = 0; index < 5; index++) {
            accumulator.addValue(index, 0.0);
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
            accumulator.addValue(index, 0.0);
        }

        accumulator.setMaxSize(5);
        assertThat(accumulator.getAccumulatedData()).hasSize(5);
    }

    @Test
    @DisplayName("Возвращает нулевую статистику для пустого накопителя")
    void returnsEmptyStats() {
        final var accumulator = new DataAccumulator(100);
        assertThat(accumulator.getStats())
                .extracting(Stats::count, Stats::min, Stats::max, Stats::average)
                .containsExactly(0, 0, 0, 0.0);
    }

    @Test
    @DisplayName("Считает статистику по добавленным значениям")
    void accumulatesStats() {
        final var accumulator = new DataAccumulator(100);
        accumulator.addValue(10, 0.0);
        accumulator.addValue(20, 6.0);
        accumulator.addValue(30, -6.0);

        assertThat(accumulator.getStats())
                .extracting(Stats::count, Stats::min, Stats::max, Stats::average)
                .containsExactly(3, 10, 30, 20.0);
    }

    @Test
    @DisplayName("Сбрасывает статистику при очистке")
    void clearsStats() {
        final var accumulator = new DataAccumulator(100);
        accumulator.addValue(10, 0.0);
        accumulator.clear();

        assertThat(accumulator.getStats().count()).isZero();
    }

    @Test
    @DisplayName("Сохраняет сквозную нумерацию после очистки")
    void retainsIdsAfterClear() {
        final var accumulator = new DataAccumulator(100);
        accumulator.addValue(10, 0.0);
        accumulator.clear();
        accumulator.addValue(40, 3.0);

        assertThat(accumulator.getAccumulatedData())
                .extracting(HistoryEntry::identifier)
                .containsExactly(2);
    }
}
