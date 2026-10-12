package org.nstu.se.lab1;

import javafx.scene.control.Alert;

public class ErrorMessage {
    public static void show(final String message) {
        final var alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Ошибка");
        alert.setContentText(message);

        alert.showAndWait();
    }
}
