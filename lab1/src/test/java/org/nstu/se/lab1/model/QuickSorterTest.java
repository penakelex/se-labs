package org.nstu.se.lab1.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QuickSorterTest {
    @Test
    @DisplayName("Проверка размера массива")
    void testSortArraySize() {
        final var array = SortArray.from(5, 2, 9, 1, 5);
        assertThat(array.size()).isEqualTo(5);
    }

    @Test
    @DisplayName("Проверка получения первого элемента массива")
    void testSortArrayGetFirst() {
        final var array = SortArray.from(5, 2, 9, 1, 5);
        assertThat(array.get(0)).isEqualTo(5);
    }

    @Test
    @DisplayName("Проверка получения элемента массива по индексу 2")
    void testSortArrayGetMiddle() {
        final var array = SortArray.from(5, 2, 9, 1, 5);
        assertThat(array.get(2)).isEqualTo(9);
    }

    @Test
    @DisplayName("Проверка строкового представления массива")
    void testSortArrayToString() {
        final var array = SortArray.from(5, 2, 9, 1, 5);
        assertThat(array.toString()).isEqualTo("[5, 2, 9, 1, 5]");
    }

    @Test
    @DisplayName("Сравнение массивов через equals")
    void testSortArrayEquality() {
        final var first = SortArray.from(1, 2, 3);
        final var second = SortArray.from(1, 2, 3);
        assertThat(first).isEqualTo(second);
    }

    @Test
    @DisplayName("Сравнение хеш-кодов массивов")
    void testSortArrayHashCode() {
        final var first = SortArray.from(1, 2, 3);
        final var second = SortArray.from(1, 2, 3);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    @DisplayName("Неравенство разных массивов")
    void testSortArrayInequality() {
        final var first = SortArray.from(1, 2, 3);
        final var third = SortArray.from(3, 2, 1);
        assertThat(first).isNotEqualTo(third);
    }
}
