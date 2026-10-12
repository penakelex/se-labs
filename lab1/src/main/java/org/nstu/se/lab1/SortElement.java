package org.nstu.se.lab1;

import javafx.scene.control.TextField;

import java.util.Locale;

public class SortElement extends TextField {
    private static final String HIGHLIGHTED_STYLE = "sort-element-highlighted";

    private double currentFontSize = 16.0;

    public SortElement(final int value) {
        super(String.valueOf(value));
        setEditable(false);
        setAlignment(javafx.geometry.Pos.CENTER);
        setPrefWidth(50);
        setPrefHeight(40);

        getStyleClass().add("sort-element");
        applyFontSize();
    }

    public void setCustomFontSize(final double fontSize) {
        this.currentFontSize = fontSize;
        applyFontSize();
    }

    public void highlight() {
        if (!getStyleClass().contains(HIGHLIGHTED_STYLE)) {
            getStyleClass().add(HIGHLIGHTED_STYLE);
        }
    }

    public void resetStyle() {
        getStyleClass().remove(HIGHLIGHTED_STYLE);
    }

    private void applyFontSize() {
        setStyle(String.format(Locale.US, "-fx-font-size: %.1fpx;", currentFontSize));
    }
}
