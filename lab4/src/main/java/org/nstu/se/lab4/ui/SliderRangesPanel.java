package org.nstu.se.lab4.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;

import org.nstu.se.lab4.ResourcePaths;
import org.nstu.se.lab4.audio.mix.SliderRanges;

import java.io.IOException;
import java.util.function.Consumer;

final class SliderRangesPanel {
    private final SliderRangesController controller;

    SliderRangesPanel(final HBox container, final Consumer<SliderRanges> onRanges) {
        final var loader = new FXMLLoader(ResourcePaths.url(ResourcePaths.SLIDER_RANGES_VIEW));

        try {
            container.getChildren().add(loader.load());
        } catch (final IOException exception) {
            throw new IllegalStateException("Cannot load slider ranges panel", exception);
        }

        controller = loader.getController();
        controller.configure(onRanges);
    }

    void initDefaults(final SliderRanges ranges) {
        controller.initDefaults(ranges);
    }
}
