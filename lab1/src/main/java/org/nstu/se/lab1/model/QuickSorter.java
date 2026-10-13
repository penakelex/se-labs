package org.nstu.se.lab1.model;

import java.util.ArrayList;
import java.util.List;

public final class QuickSorter {
    private QuickSorter() {}

    public static <T extends Comparable<? super T>> List<SortStep> plan(final SortArray<T> source) {
        final var data = source.toArray();
        final var steps = new ArrayList<SortStep>();

        sort(data, 0, data.length - 1, steps);

        return List.copyOf(steps);
    }

    private static <T extends Comparable<? super T>> void sort(
            final T[] data, final int low, final int high, final List<SortStep> steps) {
        if (low > high) {
            return;
        }

        if (low == high) {
            steps.add(new SortStep.Placed(low));
            return;
        }

        steps.add(new SortStep.Range(low, high));

        final var pivotIndex = partition(data, low, high, steps);
        steps.add(new SortStep.Placed(pivotIndex));

        sort(data, low, pivotIndex - 1, steps);
        sort(data, pivotIndex + 1, high, steps);
    }

    private static <T extends Comparable<? super T>> int partition(
            final T[] data, final int low, final int high, final List<SortStep> steps) {
        final var pivotValue = data[high];
        steps.add(new SortStep.Pivot(high));

        var boundary = low;

        for (var scan = low; scan < high; scan++) {

            if (data[scan].compareTo(pivotValue) < 0) {

                if (boundary != scan) {

                    swap(data, boundary, scan);
                    steps.add(new SortStep.Swap(boundary, scan));
                }

                boundary++;
            }
        }

        if (boundary != high) {

            swap(data, boundary, high);
            steps.add(new SortStep.Swap(boundary, high));
        }

        return boundary;
    }

    private static <T> void swap(final T[] data, final int first, final int second) {
        final var buffer = data[first];
        data[first] = data[second];
        data[second] = buffer;
    }
}
