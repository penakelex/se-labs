package com.example.ui;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

public class SettingsPane extends VBox {

    private static final String UNIFIED_BG_STYLE = "-fx-background-color: #d4d0c8; -fx-border-color: #808080; -fx-border-width: 1.5; -fx-border-radius: 6; -fx-background-radius: 6;";
    private static final String UNIFIED_FONT = "-fx-font-family: 'Segoe UI', 'Arial', sans-serif;";

    private final TextField v0Field;
    private final TextField minDbField;
    private final TextField maxDbField;
    private final TextField periodField;
    private final Button applyButton;
    private final Button historyButton;

    public SettingsPane(int defaultV0, double defaultMinDb, double defaultMaxDb, int defaultPeriod) {
        setSpacing(12);
        setPadding(new Insets(15));
        setStyle(UNIFIED_BG_STYLE + " " + UNIFIED_FONT + " -fx-font-size: 13px;");

        Label titleLabel = new Label("Параметры");
        titleLabel.setStyle(UNIFIED_FONT + " -fx-font-weight: bold; -fx-font-size: 15px;");
        HBox titleBox = new HBox(titleLabel);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(0, 0, 5, 0));

        // GridPane с полями ввода
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER_LEFT);

        v0Field = createTextField(String.valueOf(defaultV0));
        minDbField = createTextField(String.valueOf(defaultMinDb));
        maxDbField = createTextField(String.valueOf(defaultMaxDb));
        periodField = createTextField(String.valueOf(defaultPeriod));

        Label lblV0 = new Label("V0:");
        lblV0.setMinWidth(85);
        lblV0.setMaxWidth(85);

        Label lblMinDb = new Label("Мин. dB:");
        lblMinDb.setMinWidth(85);
        lblMinDb.setMaxWidth(85);

        Label lblMaxDb = new Label("Макс. dB:");
        lblMaxDb.setMinWidth(85);
        lblMaxDb.setMaxWidth(85);

        Label lblPeriod = new Label("Период (мс):");
        lblPeriod.setMinWidth(85);
        lblPeriod.setMaxWidth(85);

        // Добавляем в grid
        grid.add(lblV0, 0, 0);
        grid.add(v0Field, 1, 0);
        grid.add(lblMinDb, 0, 1);
        grid.add(minDbField, 1, 1);
        grid.add(lblMaxDb, 0, 2);
        grid.add(maxDbField, 1, 2);
        grid.add(lblPeriod, 0, 3);
        grid.add(periodField, 1, 3);

        // Кнопки по центру
        applyButton = new Button("Применить");
        applyButton.setStyle(UNIFIED_FONT + " -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 6 15 6 15;");

        historyButton = new Button("История данных");
        historyButton.setStyle(UNIFIED_FONT + " -fx-background-color: #606060; -fx-text-fill: white; -fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 6 12 6 12;");

        VBox buttonBox = new VBox(10, applyButton, historyButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(5, 0, 0, 0));

        getChildren().addAll(titleBox, grid, buttonBox);
    }

    private TextField createTextField(String defaultValue) {
        TextField field = new TextField(defaultValue);
        field.setPrefWidth(80);
        field.setStyle(UNIFIED_FONT + " -fx-background-radius: 4; -fx-border-radius: 4; -fx-border-color: #a0a0a0; -fx-border-width: 1;");
        return field;
    }

    public Button getApplyButton() {
        return applyButton;
    }

    public int getV0() {
        return Integer.parseInt(v0Field.getText().trim());
    }

    public double getMinDb() {
        return Double.parseDouble(minDbField.getText().trim());
    }

    public double getMaxDb() {
        return Double.parseDouble(maxDbField.getText().trim());
    }

    public int getPeriod() {
        return Integer.parseInt(periodField.getText().trim());
    }

    public Button getHistoryButton() {
        return historyButton;
    }

    public void showError(String message) {
        Window window = getScene() != null ? getScene().getWindow() : null;

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка ввода");
        alert.setHeaderText("Некорректные параметры");
        alert.setContentText(message);
        alert.initOwner(window);

        alert.getDialogPane().setStyle("-fx-font-family: 'Segoe UI', 'Arial', sans-serif; " + "-fx-font-size: 12px;");

        alert.showAndWait();
    }

    public boolean validateInputs() {
        try {
            int v0 = getV0();
            double minDb = getMinDb();
            double maxDb = getMaxDb();
            int period = getPeriod();

            if (v0 <= 0) {
                showError("V0 должно быть > 0");
                return false;
            }
            if (minDb >= maxDb) {
                showError("Мин. dB должен быть < Макс. dB");
                return false;
            }
            if (period <= 0) {
                showError("Период должен быть > 0");
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            showError("Ошибка формата чисел");
            return false;
        }
    }
}
