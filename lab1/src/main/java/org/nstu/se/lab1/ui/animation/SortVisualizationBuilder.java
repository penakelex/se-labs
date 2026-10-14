package org.nstu.se.lab1.ui.animation;

import javafx.scene.layout.Pane;

import org.nstu.se.lab1.model.SortArray;

public final class SortVisualizationBuilder {
    private static final double DEFAULT_PANE_WIDTH = 900.0;
    private static final double DEFAULT_PANE_HEIGHT = 460.0;
    private static final double BASE_COLUMN_WIDTH = 60.0;
    private static final double BASE_GAP = 12.0;
    private static final double BASE_COLUMN_HEIGHT = 380.0;
    private static final double MARGIN = 30.0;
    private static final double MIN_SCALE = 0.25;
    private static final double MAX_SCALE = 1.5;
    private static final double MIN_BAR_HEIGHT = 6.0;

    private SortVisualizationBuilder() {}

    public static SortElement[] buildArray(
            final Pane animationPane, final SortArray<Integer> source) {
        animationPane.getChildren().clear();
        final var count = source.size();

        if (count == 0) {
            return new SortElement[0];
        }

        final var elements = new SortElement[count];
        var minValue = Integer.MAX_VALUE;
        var maxValue = Integer.MIN_VALUE;

        for (var index = 0; index < count; index++) {
            final var value = source.get(index);
            minValue = Math.min(minValue, value);
            maxValue = Math.max(maxValue, value);
        }

        final var layout = computeLayout(animationPane, count);
        final var allEqual = maxValue == minValue;
        final var valueRange = (double) maxValue - minValue;

        for (var index = 0; index < count; index++) {
            final var value = source.get(index);

            final double barHeight;

            if (allEqual) {
                barHeight = layout.columnHeight();
            } else {
                final var normalized = (value - minValue) / valueRange;
                barHeight = MIN_BAR_HEIGHT + normalized * (layout.columnHeight() - MIN_BAR_HEIGHT);
            }

            final var element =
                    new SortElement(
                            String.valueOf(value),
                            layout.columnWidth(),
                            layout.columnHeight(),
                            barHeight);

            elements[index] = element;
            element.setLayoutX(layout.startX() + index * (layout.columnWidth() + layout.gap()));
            element.setLayoutY(layout.startY());

            animationPane.getChildren().add(element);
        }

        return elements;
    }

    private static Layout computeLayout(final Pane animationPane, final int count) {
        final var paneWidth =
                animationPane.getWidth() > 0 ? animationPane.getWidth() : DEFAULT_PANE_WIDTH;
        final var paneHeight =
                animationPane.getHeight() > 0 ? animationPane.getHeight() : DEFAULT_PANE_HEIGHT;

        final var availableWidth = Math.max(100.0, paneWidth - 2 * MARGIN);
        final var requiredWidth = count * BASE_COLUMN_WIDTH + (count - 1) * BASE_GAP;

        final var scale =
                Math.clamp(
                        requiredWidth > availableWidth ? availableWidth / requiredWidth : 1.0,
                        MIN_SCALE,
                        MAX_SCALE);

        final var columnWidth = BASE_COLUMN_WIDTH * scale;
        final var columnHeight = BASE_COLUMN_HEIGHT * scale;
        var gap = BASE_GAP * scale;

        var totalWidth = count * columnWidth + (count - 1) * gap;

        if (totalWidth > availableWidth && count > 1) {
            gap = Math.max(2.0, (availableWidth - count * columnWidth) / (count - 1));
            totalWidth = count * columnWidth + (count - 1) * gap;
        }

        final var startX = Math.max(MARGIN, (paneWidth - totalWidth) / 2.0);
        final var startY = Math.max(MARGIN, (paneHeight - columnHeight) / 2.0);

        return new Layout(startX, startY, columnWidth, columnHeight, gap);
    }

    private record Layout(
            double startX, double startY, double columnWidth, double columnHeight, double gap) {}
}
