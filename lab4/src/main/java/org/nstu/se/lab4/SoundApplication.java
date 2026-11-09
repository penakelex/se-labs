package org.nstu.se.lab4;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.nstu.se.lab4.ui.MainController;

import java.io.IOException;
import java.util.ResourceBundle;

public final class SoundApplication extends Application {
    private static final int INITIAL_WIDTH = 900;
    private static final int INITIAL_HEIGHT = 600;

    @Override
    public void start(final Stage stage) throws IOException {
        final var loader =
                new FXMLLoader(
                        ResourcePaths.url(ResourcePaths.MAIN_VIEW),
                        ResourceBundle.getBundle(Messages.BUNDLE_NAME));

        final var scene = new Scene(loader.load(), INITIAL_WIDTH, INITIAL_HEIGHT);
        scene.getStylesheets().add(ResourcePaths.url(ResourcePaths.APP_CSS).toExternalForm());

        final var controller = (MainController) loader.getController();
        stage.setOnCloseRequest(_ -> controller.shutdown());

        stage.setTitle(Messages.get("window.title"));
        stage.setScene(scene);
        stage.show();
    }
}
