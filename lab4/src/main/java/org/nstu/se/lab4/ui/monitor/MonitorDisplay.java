package org.nstu.se.lab4.ui.monitor;

import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

import org.jspecify.annotations.Nullable;
import org.nstu.se.lab4.audio.source.SignalSource;
import org.nstu.se.lab4.ui.WaveformView;

final class MonitorDisplay {
    private final ToggleButton graphToggle;
    private final WaveformView waveformView;
    private final LevelMeterView meterView;

    private @Nullable SignalSource channelSignal;

    MonitorDisplay(
            final ToggleButton graphToggle,
            final ToggleButton scaleToggle,
            final WaveformView waveformView,
            final LevelMeterView meterView) {
        this.graphToggle = graphToggle;
        this.waveformView = waveformView;
        this.meterView = meterView;
        initializeToggle(scaleToggle);
    }

    private void initializeToggle(final ToggleButton scaleToggle) {
        final var group = new ToggleGroup();
        graphToggle.setToggleGroup(group);
        scaleToggle.setToggleGroup(group);
        graphToggle.setSelected(true);

        graphToggle
                .selectedProperty()
                .addListener(
                        (_, _, isSelected) -> {
                            waveformView.setVisible(isSelected);
                            waveformView.setManaged(isSelected);
                            meterView.setVisible(!isSelected);
                            meterView.setManaged(!isSelected);
                        });
    }

    void setSignal(final SignalSource signal) {
        channelSignal = signal;
        waveformView.setSource(signal);
    }

    void show(final double positionSeconds) {
        waveformView.setPosition(positionSeconds);
        meterView.setLevel(sampleAt(positionSeconds));
    }

    private float sampleAt(final double positionSeconds) {
        final var signal = channelSignal;

        if (signal == null) {
            return 0.0f;
        }

        final var sampleIndex = (int) (positionSeconds * signal.getSampleRate());
        return signal.getSample(sampleIndex);
    }
}
