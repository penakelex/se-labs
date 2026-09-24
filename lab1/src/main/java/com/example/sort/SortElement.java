package com.example.sort;

import java.util.Locale;
import javafx.scene.control.TextField;

public class SortElement extends TextField {

    private double currentFontSize = 16.0;

    public SortElement(int value) {
        super(String.valueOf(value));
        setEditable(false);
        setAlignment(javafx.geometry.Pos.CENTER);
        setPrefWidth(50);
        setPrefHeight(40);

        getStyleClass().add("sort-element");
        applyFontSize();
    }

    public void setCustomFontSize(double fontSize) {
        this.currentFontSize = fontSize;
        applyFontSize();
    }

    public void highlight() {
        if (!getStyleClass().contains("sort-element-highlighted")) {
            getStyleClass().add("sort-element-highlighted");
        }
    }

    public void resetStyle() {
        getStyleClass().remove("sort-element-highlighted");
    }

    private void applyFontSize() {
        setStyle(String.format(Locale.US, "-fx-font-size: %.1fpx;", currentFontSize));
    }
}