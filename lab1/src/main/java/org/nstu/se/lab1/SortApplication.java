package org.nstu.se.lab1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.nstu.se.lab1.ui.menu.MenuController;

import java.io.IOException;
import java.util.ResourceBundle;

public class SortApplication extends Application {
    @Override
    public void start(final Stage stage) throws IOException {
        final var context = new AppContext();

        final var loader =
                new FXMLLoader(
                        ResourcePaths.url(ResourcePaths.MENU_VIEW),
                        ResourceBundle.getBundle(Messages.BUNDLE_NAME));

        loader.setControllerFactory(_ -> new MenuController(context));

        final var scene = new Scene(loader.load(), 1280, 800);
        scene.getStylesheets().add(ResourcePaths.url(ResourcePaths.APP_CSS).toExternalForm());

        stage.setTitle(Messages.get("window.title"));
        stage.setScene(scene);
        stage.show();
    }
}
