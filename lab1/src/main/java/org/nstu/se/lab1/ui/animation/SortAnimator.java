package org.nstu.se.lab1.ui.animation;

import javafx.animation.Animation;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

import org.nstu.se.lab1.model.SortStep;

public final class SortAnimator {
    private static final double RANGE_PAUSE_MILLIS = 300;
    private static final double PIVOT_PAUSE_MILLIS = 300;
    private static final double PLACED_PAUSE_MILLIS = 220;

    private static final double SWAP_LIFT_MILLIS = 160;
    private static final double SWAP_ACROSS_MILLIS = 260;
    private static final double SWAP_LOWER_MILLIS = 160;
    private static final double SWAP_LIFT_PIXELS = 40;

    private SortAnimator() {}

    public static Animation createStep(
            final SortStep step, final SortElement[] elements, final Runnable afterStep) {
        return switch (step) {
            case SortStep.Range(int low, int high) ->
                    pauseThen(RANGE_PAUSE_MILLIS, () -> applyRange(elements, low, high), afterStep);

            case SortStep.Pivot(int index) ->
                    pauseThen(PIVOT_PAUSE_MILLIS, () -> elements[index].setPivot(true), afterStep);

            case SortStep.Placed(int index) ->
                    pauseThen(PLACED_PAUSE_MILLIS, () -> markPlaced(elements, index), afterStep);

            case SortStep.Swap(int first, int second) ->
                    swapAnimation(elements, first, second, afterStep);
        };
    }

    private static Animation pauseThen(
            final double millis, final Runnable action, final Runnable afterStep) {
        final var pause = new PauseTransition(Duration.millis(millis));
        pause.setOnFinished(
                _ -> {
                    action.run();
                    afterStep.run();
                });
        return pause;
    }

    private static Animation swapAnimation(
            final SortElement[] elements,
            final int firstIndex,
            final int secondIndex,
            final Runnable afterStep) {

        final var firstNode = elements[firstIndex];
        final var secondNode = elements[secondIndex];

        final var firstX = firstNode.getLayoutX();
        final var secondX = secondNode.getLayoutX();

        final var moveFirst = liftAcrossLower(firstNode, secondX - firstX);
        final var moveSecond = liftAcrossLower(secondNode, firstX - secondX);

        final var parallel = new ParallelTransition(moveFirst, moveSecond);
        parallel.setOnFinished(
                _ -> {
                    firstNode.setTranslateX(0);
                    firstNode.setTranslateY(0);
                    secondNode.setTranslateX(0);
                    secondNode.setTranslateY(0);

                    firstNode.setLayoutX(secondX);
                    secondNode.setLayoutX(firstX);

                    final var buffer = elements[firstIndex];
                    elements[firstIndex] = elements[secondIndex];
                    elements[secondIndex] = buffer;

                    afterStep.run();
                });

        return parallel;
    }

    private static Animation liftAcrossLower(final SortElement node, final double offsetX) {
        final var lift = new TranslateTransition(Duration.millis(SWAP_LIFT_MILLIS), node);
        lift.setToY(-SWAP_LIFT_PIXELS);

        final var across = new TranslateTransition(Duration.millis(SWAP_ACROSS_MILLIS), node);
        across.setToX(offsetX);

        final var lower = new TranslateTransition(Duration.millis(SWAP_LOWER_MILLIS), node);
        lower.setToY(0);

        return new javafx.animation.SequentialTransition(lift, across, lower);
    }

    private static void applyRange(final SortElement[] elements, final int low, final int high) {
        for (final var element : elements) {
            element.setInRange(false);
            element.setPivot(false);
        }

        for (var index = low; index <= high; index++) {
            elements[index].setInRange(true);
        }
    }

    private static void markPlaced(final SortElement[] elements, final int index) {
        final var element = elements[index];
        element.setInRange(false);
        element.setPivot(false);
        element.setPlaced(true);
    }
}
