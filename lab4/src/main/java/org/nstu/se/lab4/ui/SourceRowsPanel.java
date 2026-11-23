package org.nstu.se.lab4.ui;

import javafx.animation.PauseTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import org.nstu.se.lab4.ResourcePaths;
import org.nstu.se.lab4.audio.playback.PlaybackController;

import java.io.IOException;

final class SourceRowsPanel {
    private static final int REBUILD_DEBOUNCE_MILLIS = 100;

    private final VBox container;
    private final PlaybackController controller;
    private final org.nstu.se.lab4.ui.monitor.MonitorWindows monitors;
    private final Runnable rebuildRequest;
    private final Runnable structureChangeRequest;

    private final PauseRebuild debounce = new PauseRebuild();

    SourceRowsPanel(
            final VBox container,
            final PlaybackController controller,
            final org.nstu.se.lab4.ui.monitor.MonitorWindows monitors,
            final Runnable rebuildRequest,
            final Runnable structureChangeRequest) {
        this.container = container;
        this.controller = controller;
        this.monitors = monitors;
        this.rebuildRequest = rebuildRequest;
        this.structureChangeRequest = structureChangeRequest;
        rebuild();
    }

    void rebuild() {
        monitors.closeAll();
        container.getChildren().clear();

        for (var index = 0; index < controller.session().settings().getSourceCount(); index++) {
            container.getChildren().add(loadRow(index));
        }
    }

    private HBox loadRow(final int index) {
        final var loader = new FXMLLoader(ResourcePaths.url(ResourcePaths.SOURCE_ROW_VIEW));

        try {
            final var row = (HBox) loader.load();
            final var rowController = (SourceRowController) loader.getController();
            rowController.configure(
                    index,
                    controller,
                    controller.session().sliderRanges(),
                    debounce::request,
                    structureChangeRequest,
                    () -> monitors.open(index, controller));
            return row;
        } catch (final IOException exception) {
            throw new IllegalStateException("Cannot load source row", exception);
        }
    }

    private final class PauseRebuild {
        private final PauseTransition transition =
                new PauseTransition(Duration.millis(REBUILD_DEBOUNCE_MILLIS));

        private PauseRebuild() {
            transition.setOnFinished(_ -> rebuildRequest.run());
        }

        private void request() {
            transition.playFromStart();
        }
    }
}
