package org.nstu.se.lab3.timer;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import org.jspecify.annotations.Nullable;
import org.nstu.se.lab3.listener.NextDataListener;

import java.util.function.IntSupplier;

public final class DataPublisher {
    private final Timeline timeline;
    private final boolean active;

    public DataPublisher(
            final int periodMillis,
            final IntSupplier currentValueSupplier,
            final @Nullable NextDataListener listener) {
        if (periodMillis > 0 && listener != null) {
            this.active = true;
            this.timeline =
                    new Timeline(
                            new KeyFrame(
                                    Duration.millis(periodMillis),
                                    _ -> listener.onNextData(currentValueSupplier.getAsInt())));
            this.timeline.setCycleCount(Timeline.INDEFINITE);
        } else {
            this.active = false;
            this.timeline = new Timeline();
        }
    }

    public void start() {
        if (active) {
            timeline.play();
        }
    }

    public void stop() {
        timeline.stop();
    }
}
