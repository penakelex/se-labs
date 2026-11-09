package org.nstu.se.lab4.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

import org.nstu.se.lab4.Messages;
import org.nstu.se.lab4.audio.PlaybackController;

@SuppressWarnings("NullAway.Init")
public final class SourceRowController {
    private static final String KEY_SOURCE_NAME = "source.name";
    private static final String KEY_AMPLITUDE_VALUE = "amplitude.value";
    private static final String KEY_DELAY_VALUE = "delay.value";

    @FXML private Label nameLabel;
    @FXML private Slider amplitudeSlider;
    @FXML private Label amplitudeValueLabel;
    @FXML private Slider delaySlider;
    @FXML private Label delayValueLabel;

    public void configure(
            final int index, final PlaybackController playback, final Runnable rebuildRequest) {
        nameLabel.setText(Messages.format(KEY_SOURCE_NAME, index + 1));

        final var settings = playback.getSettings();
        final var amplitude = settings.getAmplitude(index);
        final var delaySeconds = settings.getDelaySeconds(index);
        amplitudeSlider.setValue(amplitude);
        delaySlider.setValue(delaySeconds);
        amplitudeValueLabel.setText(Messages.format(KEY_AMPLITUDE_VALUE, amplitude));
        delayValueLabel.setText(Messages.format(KEY_DELAY_VALUE, delaySeconds));

        amplitudeSlider
                .valueProperty()
                .addListener(
                        (_, _, newValue) -> {
                            playback.updateAmplitude(index, newValue.doubleValue());
                            amplitudeValueLabel.setText(
                                    Messages.format(KEY_AMPLITUDE_VALUE, newValue.doubleValue()));
                            rebuildRequest.run();
                        });

        delaySlider
                .valueProperty()
                .addListener(
                        (_, _, newValue) -> {
                            playback.updateDelaySeconds(index, newValue.doubleValue());
                            delayValueLabel.setText(
                                    Messages.format(KEY_DELAY_VALUE, newValue.doubleValue()));
                            rebuildRequest.run();
                        });
    }
}
