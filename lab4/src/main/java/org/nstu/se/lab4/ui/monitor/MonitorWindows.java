package org.nstu.se.lab4.ui.monitor;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.nstu.se.lab4.Messages;
import org.nstu.se.lab4.ResourcePaths;
import org.nstu.se.lab4.audio.playback.PlaybackController;

import java.io.IOException;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public final class MonitorWindows {
    private static final String KEY_TITLE = "monitor.title";
    private static final int INITIAL_WIDTH = 520;
    private static final int INITIAL_HEIGHT = 420;

    private final Map<Integer, Stage> windows = new ConcurrentHashMap<>();

    public void open(final int channelIndex, final PlaybackController model) {
        final var existing = windows.get(channelIndex);

        if (existing != null) {
            existing.toFront();
            return;
        }

        final var loader =
                new FXMLLoader(
                        ResourcePaths.url(ResourcePaths.SOURCE_MONITOR_VIEW),
                        ResourceBundle.getBundle(Messages.BUNDLE_NAME));

        try {
            final var root = (Parent) loader.load();
            final var monitor = (SourceMonitorController) loader.getController();
            monitor.configure(channelIndex, model);

            final var stage = new Stage();
            stage.setTitle(
                    Messages.format(
                            KEY_TITLE, model.session().settings().getChannelName(channelIndex)));
            final var scene = new Scene(root, INITIAL_WIDTH, INITIAL_HEIGHT);
            scene.getStylesheets().add(ResourcePaths.url(ResourcePaths.APP_CSS).toExternalForm());
            stage.setScene(scene);
            stage.setOnCloseRequest(
                    _ -> {
                        monitor.dispose();
                        windows.remove(channelIndex, stage);
                    });

            windows.put(channelIndex, stage);
            stage.show();
        } catch (final IOException exception) {
            throw new IllegalStateException("Cannot load source monitor", exception);
        }
    }

    public void closeAll() {
        for (final var stage : windows.values()) {
            stage.close();
        }

        windows.clear();
    }
}
