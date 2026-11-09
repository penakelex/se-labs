package org.nstu.se.lab3;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ResourceBundle;

public final class LogLevelApplication extends Application {
    private static final int INITIAL_WIDTH = 680;
    private static final int INITIAL_HEIGHT = 460;
    private static final int MIN_WIDTH = 600;
    private static final int MIN_HEIGHT = 450;

    @Override
    public void start(final Stage stage) throws IOException {
        final var loader =
                new FXMLLoader(
                        ResourcePaths.url(ResourcePaths.MAIN_VIEW),
                        ResourceBundle.getBundle(Messages.BUNDLE_NAME));

        final var scene = new Scene(loader.load(), INITIAL_WIDTH, INITIAL_HEIGHT);
        scene.getStylesheets().add(ResourcePaths.url(ResourcePaths.APP_CSS).toExternalForm());

        stage.setTitle(Messages.get("window.title"));
        stage.setScene(scene);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.show();
    }
}
