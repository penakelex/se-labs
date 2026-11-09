package org.nstu.se.lab3.ui.indicator;

import javafx.scene.control.Label;
import javafx.scene.control.Slider;

import org.nstu.se.lab3.model.LogScaleModel;

final class LevelWidgets {
    private static final double TICK_COUNT = 4.0;

    private LevelWidgets() {}

    static Label createValueLabel() {
        final var label = new Label();
        label.getStyleClass().add("level-value");
        return label;
    }

    static Label createDecibelLabel() {
        final var label = new Label();
        label.getStyleClass().add("level-decibels");
        return label;
    }

    static Slider createSlider(final LogScaleModel scale) {
        final var range = scale.getMaxDecibels() - scale.getMinDecibels();
        final var slider = new Slider(scale.getMinDecibels(), scale.getMaxDecibels(), 0.0);
        slider.setMajorTickUnit(Math.max(1.0, range / TICK_COUNT));
        slider.getStyleClass().add("level-slider");
        return slider;
    }
}
