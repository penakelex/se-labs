package org.nstu.se.lab3.ui.indicator;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import org.nstu.se.lab3.Messages;
import org.nstu.se.lab3.model.LogScaleModel;

import java.util.function.DoubleSupplier;

final class MeterRenderer {
    static final double MIN_HEIGHT = 100.0;

    private static final double BACKGROUND_WIDTH = 22.0;
    private static final double BAR_WIDTH = 18.0;
    private static final double BAR_INSET = 4.0;
    private static final double MIDDLE_PERCENTAGE = 0.5;
    private static final double MULTIPLIER = 2.0;
    private static final double MINIMAL_RANGE = 0.0;

    private static final Color LOW_COLOR = Color.web("#10b981");
    private static final Color MID_COLOR = Color.web("#f59e0b");
    private static final Color HIGH_COLOR = Color.web("#ef4444");

    private static final String STYLE_BACKGROUND = "meter-background";
    private static final String STYLE_BAR = "meter-bar";
    private static final String STYLE_GROUP = "meter-group";
    private static final String KEY_VALUE_FORMAT = "level.value.format";
    private static final String KEY_DECIBEL_FORMAT = "level.decibel.format";

    private final LogScaleModel scaleModel;
    private final Label valueLabel;
    private final Label decibelLabel;
    private final Rectangle background;
    private final Rectangle bar;
    private final StackPane container;

    MeterRenderer(
            final LogScaleModel scaleModel, final Label valueLabel, final Label decibelLabel) {
        this.scaleModel = scaleModel;
        this.valueLabel = valueLabel;
        this.decibelLabel = decibelLabel;
        this.background = createRectangle(BACKGROUND_WIDTH, MIN_HEIGHT, STYLE_BACKGROUND);
        this.bar = createRectangle(BAR_WIDTH, 0.0, STYLE_BAR);
        this.container = buildGroup();
    }

    StackPane getContainer() {
        return container;
    }

    void render(final double decibels) {
        final var value = scaleModel.calculateValueFromDecibels(decibels);
        valueLabel.setText(Messages.format(KEY_VALUE_FORMAT, value));
        decibelLabel.setText(Messages.format(KEY_DECIBEL_FORMAT, decibels));

        final var percentage = percentageOf(decibels);
        final var currentHeight = background.getHeight();

        if (currentHeight > 0) {
            bar.setHeight(percentage * (currentHeight - 2 * BAR_INSET));
        }

        bar.setFill(calculateColor(percentage));
    }

    void bindPreferredHeight(final ObservableValue<Number> source) {
        container.prefHeightProperty().bind(source);
    }

    void bindHeightTo(final Region track, final DoubleSupplier decibelsSupplier) {
        background.heightProperty().unbind();
        background.heightProperty().bind(track.heightProperty());

        final var trackOffset =
                Bindings.createDoubleBinding(
                        () -> track.getBoundsInParent().getMaxY() - container.getHeight(),
                        track.boundsInParentProperty(),
                        container.heightProperty());

        background.translateYProperty().bind(trackOffset);
        bar.translateYProperty().bind(trackOffset);

        track.heightProperty().addListener((_, _, _) -> render(decibelsSupplier.getAsDouble()));
        render(decibelsSupplier.getAsDouble());
    }

    private StackPane buildGroup() {
        final var result = new StackPane();
        result.getStyleClass().add(STYLE_GROUP);
        StackPane.setMargin(bar, new Insets(0, 0, BAR_INSET, 0));
        result.getChildren().addAll(background, bar);
        return result;
    }

    private double percentageOf(final double decibels) {
        final var range = scaleModel.getMaxDecibels() - scaleModel.getMinDecibels();

        if (range <= MINIMAL_RANGE) {
            return 0.0;
        }

        final var raw = (decibels - scaleModel.getMinDecibels()) / range;
        return Math.clamp(raw, 0.0, 1.0);
    }

    private Color calculateColor(final double percentage) {
        if (percentage < MIDDLE_PERCENTAGE) {
            return LOW_COLOR.interpolate(MID_COLOR, percentage * MULTIPLIER);
        }

        return MID_COLOR.interpolate(HIGH_COLOR, (percentage - MIDDLE_PERCENTAGE) * MULTIPLIER);
    }

    private static Rectangle createRectangle(
            final double width, final double height, final String styleClass) {
        final var rectangle = new Rectangle(width, height);
        rectangle.getStyleClass().add(styleClass);
        return rectangle;
    }
}
