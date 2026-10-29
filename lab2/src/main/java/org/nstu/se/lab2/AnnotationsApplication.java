package org.nstu.se.lab2;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.nstu.se.lab2.ui.MainController;

import java.io.IOException;
import java.util.Objects;
import java.util.ResourceBundle;

public class AnnotationsApplication extends Application {
    private static final String VIEW = "/org/nstu/se/lab2/main_view.fxml";
    private static final String CSS = "/org/nstu/se/lab2/css/app.css";

    @Override
    public void start(final Stage stage) throws IOException {
        final var loader =
                new FXMLLoader(
                        AnnotationsApplication.class.getResource(VIEW),
                        ResourceBundle.getBundle(Messages.BUNDLE_NAME));

        loader.setControllerFactory(_ -> new MainController());

        final var scene = new Scene(loader.load(), 1180, 760);
        scene.getStylesheets()
                .add(
                        Objects.requireNonNull(AnnotationsApplication.class.getResource(CSS))
                                .toExternalForm());

        stage.setTitle(Messages.get("window.title"));
        stage.setScene(scene);
        stage.show();
    }
}
