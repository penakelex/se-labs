package org.nstu.se.lab3.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LogScaleModelTest {
    private static final int ZERO_LEVEL = 100;
    private static final double MIN_DECIBELS = -72.0;
    private static final double MAX_DECIBELS = 15.0;

    @Test
    @DisplayName("Нулевой уровень даёт 0 dB")
    void calculatesZeroDecibels() {
        final var model = new LogScaleModel(ZERO_LEVEL, MIN_DECIBELS, MAX_DECIBELS);
        assertThat(model.calculateDecibels(ZERO_LEVEL)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Значение выше диапазона ограничивается максимумом")
    void clampsDecibelsToMaximum() {
        final var model = new LogScaleModel(ZERO_LEVEL, MIN_DECIBELS, MAX_DECIBELS);
        assertThat(model.calculateDecibels(100_000)).isEqualTo(MAX_DECIBELS);
    }

    @Test
    @DisplayName("Нулевое значение ограничивается минимумом")
    void clampsZeroValueToMinimum() {
        final var model = new LogScaleModel(ZERO_LEVEL, MIN_DECIBELS, MAX_DECIBELS);
        assertThat(model.calculateDecibels(0)).isEqualTo(MIN_DECIBELS);
    }

    @Test
    @DisplayName("Перевод из dB в значение работает")
    void convertsDecibelsToValue() {
        final var model = new LogScaleModel(ZERO_LEVEL, MIN_DECIBELS, MAX_DECIBELS);
        assertThat(model.calculateValueFromDecibels(0.0)).isEqualTo(ZERO_LEVEL);
    }

    @Test
    @DisplayName("Некорректный диапазон нормализует минимум")
    void normalizesMinDecibels() {
        final var model = new LogScaleModel(ZERO_LEVEL, MAX_DECIBELS, MIN_DECIBELS);
        assertThat(model.getMinDecibels()).isEqualTo(MIN_DECIBELS);
    }

    @Test
    @DisplayName("Некорректный диапазон нормализует максимум")
    void normalizesMaxDecibels() {
        final var model = new LogScaleModel(ZERO_LEVEL, MAX_DECIBELS, MIN_DECIBELS);
        assertThat(model.getMaxDecibels()).isEqualTo(MAX_DECIBELS);
    }

    @Test
    @DisplayName("Нулевой V0 заменяется на единицу")
    void guardsZeroLevel() {
        final var model = new LogScaleModel(0, MIN_DECIBELS, MAX_DECIBELS);
        assertThat(model.getZeroLevelValue()).isEqualTo(1);
    }
}
