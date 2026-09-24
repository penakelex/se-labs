package com.example.sort;

import javafx.scene.control.TextField;

public class SortElement extends TextField {

    private double currentFontSize = 16.0; // По умолчанию 16px

    public SortElement(int value) {
        super(String.valueOf(value));

        setEditable(false);
        setPrefWidth(50);
        setPrefHeight(40);
        updateStyle("");
    }

    // Метод для динамической установки размера шрифта
    public void setCustomFontSize(double fontSize) {
        this.currentFontSize = fontSize;
        updateStyle("");
    }

    // Подсветка с сохранением текущего размера шрифта
    public void highlight() {
        updateStyle("-fx-background-color: lightblue;");
    }

    // Сброс стиля с сохранением текущего размера шрифта
    public void resetStyle() {
        updateStyle("");
    }

    private void updateStyle(String extraStyle) {
        setStyle(
                "-fx-alignment: center;" +
                        "-fx-font-size: " + String.format(java.util.Locale.US, "%.1f", currentFontSize) + "px;" +
                        extraStyle
        );
    }
}