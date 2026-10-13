package org.nstu.se.lab1.model;

public sealed interface SortStep {
    record Range(int low, int high) implements SortStep {}

    record Pivot(int index) implements SortStep {}

    record Placed(int index) implements SortStep {}

    record Swap(int first, int second) implements SortStep {}
}
