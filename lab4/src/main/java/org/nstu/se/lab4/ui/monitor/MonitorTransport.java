package org.nstu.se.lab4.ui.monitor;

import javafx.scene.control.Label;
import javafx.scene.control.Slider;

import org.jspecify.annotations.Nullable;
import org.nstu.se.lab4.Messages;

import java.util.function.DoubleConsumer;

final class MonitorTransport {
    private static final String KEY_TIME_FORMAT = "time.format";

    private final Slider seekSlider;
    private final Label timeLabel;
    private @Nullable DoubleConsumer onSeek;

    private boolean seekInProgress;

    MonitorTransport(final Slider seekSlider, final Label timeLabel) {
        this.seekSlider = seekSlider;
        this.timeLabel = timeLabel;

        seekSlider.setOnMousePressed(_ -> seekInProgress = true);
        seekSlider.setOnMouseReleased(
                _ -> {
                    seekInProgress = false;

                    if (onSeek != null) {
                        onSeek.accept(seekSlider.getValue());
                    }
                });
    }

    void setOnSeek(final @Nullable DoubleConsumer onSeek) {
        this.onSeek = onSeek;
    }

    void setDuration(final double durationSeconds) {
        seekSlider.setMax(durationSeconds);
    }

    void showTime(final double positionSeconds, final double durationSeconds) {
        timeLabel.setText(Messages.format(KEY_TIME_FORMAT, positionSeconds, durationSeconds));
    }

    boolean seeking() {
        return seekInProgress;
    }
}
