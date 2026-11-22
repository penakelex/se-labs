package org.nstu.se.lab4.ui.monitor;

import javafx.scene.control.Label;
import javafx.scene.control.Slider;

import org.jspecify.annotations.Nullable;
import org.nstu.se.lab4.Messages;

import java.util.function.DoubleConsumer;

@SuppressWarnings("NullAway.Init")
final class MonitorTransport {
    private static final String KEY_TIME_FORMAT = "time.format";

    private @Nullable Slider seekSlider;
    private @Nullable Label timeLabel;
    private @Nullable DoubleConsumer onSeek;

    private boolean seekInProgress;

    void bind(final Slider seekSlider, final Label timeLabel) {
        this.seekSlider = seekSlider;
        this.timeLabel = timeLabel;

        seekSlider.setOnMousePressed(_ -> seekInProgress = true);
        seekSlider.setOnMouseReleased(
                _ -> {
                    seekInProgress = false;

                    final var seek = onSeek;
                    final var slider = seekSlider;

                    if (seek != null && slider != null) {
                        seek.accept(slider.getValue());
                    }
                });
    }

    void setOnSeek(final DoubleConsumer onSeek) {
        this.onSeek = onSeek;
    }

    void setDuration(final double durationSeconds) {
        final var slider = seekSlider;

        if (slider != null) {
            slider.setMax(durationSeconds);
        }
    }

    void showTime(final double positionSeconds, final double durationSeconds) {
        final var label = timeLabel;

        if (label != null) {
            label.setText(Messages.format(KEY_TIME_FORMAT, positionSeconds, durationSeconds));
        }
    }

    boolean seeking() {
        return seekInProgress;
    }
}
