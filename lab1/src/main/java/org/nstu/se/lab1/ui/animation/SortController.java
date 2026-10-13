package org.nstu.se.lab1.ui.animation;

import javafx.animation.Animation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;

import org.nstu.se.lab1.Messages;
import org.nstu.se.lab1.ResourcePaths;
import org.nstu.se.lab1.model.QuickSorter;
import org.nstu.se.lab1.model.Session;
import org.nstu.se.lab1.model.SortArray;
import org.nstu.se.lab1.model.SortStep;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SortController implements Initializable {
    private static final double DEFAULT_PANE_WIDTH = 900.0;
    private static final double DEFAULT_PANE_HEIGHT = 460.0;

    private static final double BASE_COLUMN_WIDTH = 60.0;
    private static final double BASE_GAP = 12.0;
    private static final double BASE_COLUMN_HEIGHT = 380.0;

    private static final double MARGIN = 30.0;
    private static final double MIN_SCALE = 0.25;
    private static final double MAX_SCALE = 1.5;
    private static final double MIN_BAR_HEIGHT = 6.0;

    @FXML private Pane animationPane;
    @FXML private Slider speedSlider;
    @FXML private Button playPauseButton;
    @FXML private Button restartButton;

    private SortArray<Integer> model;
    private SortElement[] elements;
    private Animation currentAnimation;
    private boolean paused;
    private boolean running;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        collapseWhenDisabled(playPauseButton);
        collapseWhenDisabled(restartButton);

        animationPane.widthProperty().addListener((_, _, _) -> maybeBuildInitialArray());
        animationPane.heightProperty().addListener((_, _, _) -> maybeBuildInitialArray());

        restartButton.setDisable(true);

        model = Session.getModel();
        showInitialArray();

        resetPlayPauseButton();
        playPauseButton.setDisable(model == null || model.size() < 2);

        animationPane
                .layoutBoundsProperty()
                .addListener(
                        (_, _, bounds) -> {
                            if (bounds.getWidth() > 0
                                    && bounds.getHeight() > 0
                                    && animationPane.getChildren().isEmpty()) {
                                showInitialArray();
                            }
                        });
    }

    private void maybeBuildInitialArray() {
        if (model == null
                || model.size() == 0
                || animationPane.getWidth() <= 0
                || animationPane.getHeight() <= 0) {
            playPauseButton.setDisable(true);
        }
    }

    private static void collapseWhenDisabled(final Button button) {
        button.visibleProperty().bind(button.disableProperty().not());
        button.managedProperty().bind(button.visibleProperty());
    }

    private void showInitialArray() {
        if (model == null || model.size() == 0 || animationPane.getWidth() <= 0) {
            playPauseButton.setDisable(true);
            return;
        }

        createArray(model);
        playPauseButton.setDisable(model.size() < 2);
    }

    @FXML
    public void togglePlayPause() {
        if (model == null || model.size() < 2) {
            return;
        }

        if (!running) {
            startNewAnimation();
            return;
        }

        if (paused) {
            currentAnimation.play();
            paused = false;
            setPauseButton();
        } else {
            currentAnimation.pause();
            paused = true;
            setResumeButton();
        }
    }

    @FXML
    public void restart() {
        if (currentAnimation != null) {
            currentAnimation.stop();
        }

        running = false;
        paused = false;
        createArray(model);
        startNewAnimation();
    }

    @FXML
    public void comeBack() throws IOException {
        if (currentAnimation != null) {
            currentAnimation.stop();
        }

        running = false;

        final var loader =
                new FXMLLoader(ResourcePaths.url(ResourcePaths.MENU_VIEW), Messages.bundle());
        animationPane.getScene().setRoot(loader.load());
    }

    private void startNewAnimation() {
        final List<SortStep> steps = QuickSorter.plan(model);
        currentAnimation = SortAnimator.build(steps, elements);
        currentAnimation.rateProperty().bind(speedSlider.valueProperty());
        currentAnimation.setOnFinished(
                _ -> {
                    running = false;
                    paused = false;
                    resetPlayPauseButton();
                });

        running = true;
        setPauseButton();

        restartButton.setDisable(false);

        currentAnimation.play();
    }

    private void resetPlayPauseButton() {
        playPauseButton.setText(Messages.get("sort.button.play"));
        playPauseButton.setTooltip(new Tooltip(Messages.get("sort.tooltip.start")));
    }

    private void setPauseButton() {
        playPauseButton.setText(Messages.get("sort.button.pause"));
        playPauseButton.setTooltip(new Tooltip(Messages.get("sort.tooltip.pause")));
    }

    private void setResumeButton() {
        playPauseButton.setText(Messages.get("sort.button.play"));
        playPauseButton.setTooltip(new Tooltip(Messages.get("sort.tooltip.resume")));
    }

    private void createArray(final SortArray<Integer> source) {
        animationPane.getChildren().clear();

        final var count = source.size();

        if (count == 0) {
            return;
        }

        elements = new SortElement[count];

        var minValue = Integer.MAX_VALUE;
        var maxValue = Integer.MIN_VALUE;

        for (var index = 0; index < count; index++) {
            final var value = source.get(index);
            minValue = Math.min(minValue, value);
            maxValue = Math.max(maxValue, value);
        }

        final var layout = computeLayout(count);
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
    }

    private Layout computeLayout(final int count) {
        final var paneWidth =
                animationPane.getWidth() > 0 ? animationPane.getWidth() : DEFAULT_PANE_WIDTH;
        final var paneHeight =
                animationPane.getHeight() > 0 ? animationPane.getHeight() : DEFAULT_PANE_HEIGHT;

        final var availableWidth = paneWidth - 2 * MARGIN;
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
            gap = Math.max(1.0, (availableWidth - count * columnWidth) / (count - 1));
            totalWidth = count * columnWidth + (count - 1) * gap;
        }

        final var startX = (paneWidth - totalWidth) / 2.0;
        final var startY = (paneHeight - columnHeight) / 2.0;

        return new Layout(startX, startY, columnWidth, columnHeight, gap);
    }

    private record Layout(
            double startX, double startY, double columnWidth, double columnHeight, double gap) {}
}
