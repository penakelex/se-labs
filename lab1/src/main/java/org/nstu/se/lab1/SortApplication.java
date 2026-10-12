package org.nstu.se.lab1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SortApplication extends Application {
    @Override
    public void start(final Stage stage) throws IOException {
        final var fxmlLoader = new FXMLLoader(SortApplication.class.getResource("menu_view.fxml"));
        final var scene = new Scene(fxmlLoader.load(), 1000, 700);
        stage.setTitle("Sort");
        stage.setScene(scene);
        stage.show();
    }
}
