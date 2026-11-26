package org.nstu.se.lab4.ui;

import static org.assertj.core.api.Assertions.assertThat;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.nstu.se.lab4.Messages;
import org.nstu.se.lab4.ResourcePaths;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

final class SliderRangesPanelTest {
    private static final int FIELD_MIN_AMPLITUDE = 0;
    private static final int FIELD_MAX_AMPLITUDE = 1;
    private static final int FIELD_MIN_DELAY = 2;
    private static final int FIELD_MAX_DELAY = 3;
    private static final int APPLY_WAIT_MILLIS = 900;
    private static final String MAX_AMPLITUDE_TEXT = "100";
    private static final String MAX_DELAY_TEXT = "10";

    @BeforeAll
    static void startToolkit() throws InterruptedException {
        final var started = new CountDownLatch(1);

        try {
            Platform.startup(started::countDown);
        } catch (final IllegalStateException exception) {
            started.countDown();
        }

        assertThat(started.await(10, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void dotSeparatedInputUpdatesSliderBounds() throws InterruptedException {
        final var bounds = applyRanges("0.5", MAX_AMPLITUDE_TEXT, "1.5", MAX_DELAY_TEXT);
        assertThat(bounds).containsExactly(0.5, 100.0, 1.5, 10.0);
    }

    @Test
    void commaSeparatedInputUpdatesSliderBounds() throws InterruptedException {
        final var bounds = applyRanges("0,5", "0,9", "1,5", MAX_DELAY_TEXT);
        assertThat(bounds).containsExactly(0.5, 0.9, 1.5, 10.0);
    }

    private static List<Double> applyRanges(
            final String minAmplitude,
            final String maxAmplitude,
            final String minDelay,
            final String maxDelay)
            throws InterruptedException {
        final var applied = new CountDownLatch(1);
        final var bounds = new double[4];

        Platform.runLater(
                () -> {
                    final var root = loadMainView();
                    final var fields =
                            root.lookup("#rangesContainer").lookupAll(".range-field").toArray();
                    final var rows = (VBox) root.lookup("#sourceControls");

                    ((TextField) fields[FIELD_MIN_AMPLITUDE]).setText(minAmplitude);
                    ((TextField) fields[FIELD_MAX_AMPLITUDE]).setText(maxAmplitude);
                    ((TextField) fields[FIELD_MIN_DELAY]).setText(minDelay);
                    ((TextField) fields[FIELD_MAX_DELAY]).setText(maxDelay);

                    final var pause = new PauseTransition(Duration.millis(APPLY_WAIT_MILLIS));
                    pause.setOnFinished(
                            _ -> {
                                final var row = (HBox) rows.getChildren().getFirst();
                                final var amplitude = (Slider) row.lookup(".amplitude-slider");
                                final var delay = (Slider) row.lookup(".delay-slider");
                                bounds[0] = amplitude.getMin();
                                bounds[1] = amplitude.getMax();
                                bounds[2] = delay.getMin();
                                bounds[3] = delay.getMax();
                                applied.countDown();
                            });
                    pause.play();
                });

        assertThat(applied.await(10, TimeUnit.SECONDS)).isTrue();
        return List.of(bounds[0], bounds[1], bounds[2], bounds[3]);
    }

    private static VBox loadMainView() {
        final var loader =
                new FXMLLoader(
                        ResourcePaths.url(ResourcePaths.MAIN_VIEW),
                        ResourceBundle.getBundle(Messages.BUNDLE_NAME));

        try {
            final var root = (VBox) loader.load();
            new Scene(root).getRoot().applyCss();
            return root;
        } catch (final IOException exception) {
            throw new IllegalStateException("Cannot load main view", exception);
        }
    }
}
