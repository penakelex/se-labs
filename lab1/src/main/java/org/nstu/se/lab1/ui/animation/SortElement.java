package org.nstu.se.lab1.ui.animation;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class SortElement extends VBox {
    private static final String STYLE_BAR = "sort-bar";
    private static final String STYLE_RANGE = "sort-element-in-range";
    private static final String STYLE_PIVOT = "sort-element-pivot";
    private static final String STYLE_PLACED = "sort-element-placed";

    public SortElement(
            final String displayValue,
            final double barWidth,
            final double columnHeight,
            final double barHeight) {
        super();

        final var barContainer = new StackPane();
        barContainer.setAlignment(Pos.BOTTOM_CENTER);
        barContainer.getStyleClass().add("sort-bar-container");

        final var bar = new Region();
        bar.getStyleClass().add(STYLE_BAR);

        bar.setPrefWidth(barWidth);
        bar.setMinWidth(barWidth);
        bar.setMaxWidth(barWidth);
        bar.setPrefHeight(barHeight);
        bar.setMinHeight(barHeight);
        bar.setMaxHeight(barHeight);

        barContainer.getChildren().add(bar);
        barContainer.setPrefSize(barWidth, columnHeight);
        barContainer.setMinSize(barWidth, columnHeight);
        barContainer.setMaxSize(barWidth, columnHeight);

        final var valueLabel = new Label(displayValue);
        valueLabel.getStyleClass().add("sort-value");
        valueLabel.setAlignment(Pos.CENTER);
        valueLabel.setPrefWidth(barWidth);
        valueLabel.setMinWidth(barWidth);
        valueLabel.setMaxWidth(barWidth);

        getChildren().addAll(barContainer, valueLabel);
        getStyleClass().add("sort-element");
    }

    public void setInRange(final boolean enabled) {
        toggle(STYLE_RANGE, enabled);
    }

    public void setPivot(final boolean enabled) {
        toggle(STYLE_PIVOT, enabled);
    }

    public void setPlaced(final boolean enabled) {
        toggle(STYLE_PLACED, enabled);
    }

    private void toggle(final String style, final boolean enabled) {
        if (!enabled) {
            getStyleClass().remove(style);
            return;
        }

        if (!getStyleClass().contains(style)) {
            getStyleClass().add(style);
        }
    }
}
