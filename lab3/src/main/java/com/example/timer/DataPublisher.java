package com.example.timer;

import com.example.listener.NextDataListener;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.function.IntSupplier;

public class DataPublisher {

    private final Timeline timeline;

    public DataPublisher(int periodMs, IntSupplier currentValueSupplier, NextDataListener listener) {
        if (periodMs > 0 && listener != null) {
            this.timeline = new Timeline(new KeyFrame(Duration.millis(periodMs), event -> {
                int value = currentValueSupplier.getAsInt();
                listener.onNextData(value);
            }));
            this.timeline.setCycleCount(Timeline.INDEFINITE);
        } else {
            this.timeline = null;
        }
    }

    public void start() {
        if (timeline != null) {
            timeline.play();
        }
    }

    public void stop() {
        if (timeline != null) {
            timeline.stop();
        }
    }
}
