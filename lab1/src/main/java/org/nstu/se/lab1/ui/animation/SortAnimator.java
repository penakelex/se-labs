package org.nstu.se.lab1.ui.animation;

import javafx.animation.Animation;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

import org.nstu.se.lab1.model.SortStep;

import java.util.List;

public final class SortAnimator {
    private static final double RANGE_PAUSE_MILLIS = 300;
    private static final double PIVOT_PAUSE_MILLIS = 300;
    private static final double PLACED_PAUSE_MILLIS = 220;
    private static final double SWAP_LIFT_MILLIS = 160;
    private static final double SWAP_ACROSS_MILLIS = 260;
    private static final double SWAP_LOWER_MILLIS = 160;
    private static final double SWAP_LIFT_PIXELS = 40;

    private final SortElement[] elements;

    private SortAnimator(final SortElement... elements) {
        this.elements = elements;
    }

    public static SequentialTransition build(
            final List<SortStep> steps, final SortElement... elements) {
        final var animator = new SortAnimator(elements);
        final var sequence = new SequentialTransition();

        for (final var step : steps) {
            sequence.getChildren().add(animator.toAnimation(step));
        }

        return sequence;
    }

    private Animation toAnimation(final SortStep step) {
        return switch (step) {
            case SortStep.Range(int low, int high) ->
                    pauseThen(RANGE_PAUSE_MILLIS, () -> applyRange(low, high));
            case SortStep.Pivot(int index) ->
                    pauseThen(PIVOT_PAUSE_MILLIS, () -> elements[index].setPivot(true));
            case SortStep.Placed(int index) ->
                    pauseThen(PLACED_PAUSE_MILLIS, () -> markPlaced(index));
            case SortStep.Swap(int first, int second) -> swapAnimation(first, second);
        };
    }

    private Animation pauseThen(final double millis, final Runnable action) {
        final var pause = new PauseTransition(Duration.millis(millis));
        pause.setOnFinished(_ -> action.run());
        return pause;
    }

    private Animation swapAnimation(final int firstIndex, final int secondIndex) {
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
                });

        return parallel;
    }

    private Animation liftAcrossLower(final SortElement node, final double offsetX) {
        final var lift = new TranslateTransition(Duration.millis(SWAP_LIFT_MILLIS), node);
        lift.setToY(-SWAP_LIFT_PIXELS);

        final var across = new TranslateTransition(Duration.millis(SWAP_ACROSS_MILLIS), node);
        across.setToX(offsetX);

        final var lower = new TranslateTransition(Duration.millis(SWAP_LOWER_MILLIS), node);
        lower.setToY(0);

        return new SequentialTransition(lift, across, lower);
    }

    private void applyRange(final int low, final int high) {
        for (final var element : elements) {
            element.setInRange(false);
            element.setPivot(false);
        }

        for (var index = low; index <= high; index++) {
            elements[index].setInRange(true);
        }
    }

    private void markPlaced(final int index) {
        final var element = elements[index];
        element.setInRange(false);
        element.setPivot(false);
        element.setPlaced(true);
    }
}
