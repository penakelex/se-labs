package com.example.sort;

import javafx.scene.control.Alert;

public class ErrorMessage {

    public static void show(String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}