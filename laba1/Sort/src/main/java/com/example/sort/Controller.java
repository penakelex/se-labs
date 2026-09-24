package com.example.sort;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;

public class Controller implements Initializable {

    @FXML
    private Pane animationPane;

    @FXML
    private Slider speedSlider;

    private SortElement[] elements;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Ждём, пока панель получит свои реальные размеры на сцене
        animationPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0 && animationPane.getChildren().isEmpty()) {
                showInitialArray();
            }
        });

        // На случай, если размеры уже определены
        showInitialArray();
    }

    private void showInitialArray() {
        int[] values = Main.getArray();
        if (values != null && values.length > 0 && animationPane.getWidth() > 0) {
            createArray(values);
        }
    }

    @FXML
    public void startSorting(ActionEvent actionEvent) {

    }

    public void comeBack(ActionEvent actionEvent) throws IOException {

        Main.setRoot("menu_view");
    }

    @FXML
    public void pauseSorting(ActionEvent actionEvent) {

    }

    @FXML
    public void resumeSorting(ActionEvent actionEvent) {

    }

    private void createArray(int[] values) {
        animationPane.getChildren().clear();

        int count = values.length;
        if (count == 0) return;

        elements = new SortElement[count];

        for (int i = 0; i < count; i++) {
            SortElement element = new SortElement(values[i]);
            elements[i] = element;
            animationPane.getChildren().add(element);
        }

        animationPane.applyCss();
        animationPane.layout();

        double paneWidth = animationPane.getWidth();
        double paneHeight = animationPane.getHeight();

        if (paneWidth <= 0) paneWidth = 660;
        if (paneHeight <= 0) paneHeight = 400;

        //базовый масштаб элемента
        double baseWidth = 50;
        double baseHeight = 40;
        double baseGap = 10;
        double baseFontSize = 16;

        double margin = 20;// Отступы от краев панели
        double availableWidth = paneWidth - 2 * margin;

        double requiredWidth = count * baseWidth + (count - 1) * baseGap;

        double scale = 1.0;
        if (requiredWidth > availableWidth) {
            scale = availableWidth / requiredWidth;
        }

        scale = Math.max(scale, 0.25);
        scale = Math.min(scale, 1.2);

        double scaledWidth = baseWidth * scale;
        double scaledHeight = baseHeight * scale;
        double scaledGap = baseGap * scale;

        double scaledFontSize = Math.max(8.0, baseFontSize * scale);

        double totalWidth = count * scaledWidth + (count - 1) * scaledGap;

        if (totalWidth > availableWidth && count > 1) {
            scaledGap = (availableWidth - count * scaledWidth) / (count - 1);
            scaledGap = Math.max(1, scaledGap);
            totalWidth = count * scaledWidth + (count - 1) * scaledGap;
        }

        //центрирование
        double startX = (paneWidth - totalWidth) / 2.0;
        double startY = (paneHeight - scaledHeight) / 2.0;

        //Позиционирование каждого элемента
        for (int i = 0; i < count; i++) {
            SortElement element = elements[i];

            element.setPrefWidth(scaledWidth);
            element.setMinWidth(scaledWidth);
            element.setMaxWidth(scaledWidth);

            element.setPrefHeight(scaledHeight);
            element.setMinHeight(scaledHeight);
            element.setMaxHeight(scaledHeight);

            element.setStyle(
                    "-fx-padding: 0;" +
                            "-fx-alignment: center;" +
                            "-fx-font-size: " + String.format(java.util.Locale.US, "%.1f", scaledFontSize) + "px;"
            );

            element.setCustomFontSize(scaledFontSize);

            double x = startX + i * (scaledWidth + scaledGap);
            element.setLayoutX(x);
            element.setLayoutY(startY);
        }
    }
}