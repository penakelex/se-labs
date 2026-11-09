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
    private static final String KEY_LABEL_AMPLITUDE = "label.amplitude";
    private static final String KEY_LABEL_DELAY = "label.delay";

    private static final double MIN_AMPLITUDE = 0.0;
    private static final double MAX_AMPLITUDE = 1.0;
    private static final double AMPLITUDE_BLOCK_INCREMENT = 0.01;
    private static final double MIN_DELAY_SECONDS = 0.0;
    private static final double MAX_DELAY_SECONDS = 2.0;
    private static final double DELAY_BLOCK_INCREMENT = 0.01;

    @FXML private Label nameLabel;
    @FXML private Label amplitudeLabel;
    @FXML private Slider amplitudeSlider;
    @FXML private Label amplitudeValueLabel;
    @FXML private Label delayLabel;
    @FXML private Slider delaySlider;
    @FXML private Label delayValueLabel;

    public void configure(
            final int index, final PlaybackController playback, final Runnable rebuildRequest) {
        configureLabels(index);
        configureAmplitudeSlider(index, playback, rebuildRequest);
        configureDelaySlider(index, playback, rebuildRequest);
    }

    private void configureLabels(final int index) {
        nameLabel.setText(Messages.format(KEY_SOURCE_NAME, index + 1));
        amplitudeLabel.setText(Messages.get(KEY_LABEL_AMPLITUDE));
        delayLabel.setText(Messages.get(KEY_LABEL_DELAY));
    }

    private void configureAmplitudeSlider(
            final int index, final PlaybackController playback, final Runnable rebuildRequest) {
        amplitudeSlider.setMin(MIN_AMPLITUDE);
        amplitudeSlider.setMax(MAX_AMPLITUDE);
        amplitudeSlider.setBlockIncrement(AMPLITUDE_BLOCK_INCREMENT);

        final var settings = playback.getSettings();
        final var amplitude = settings.getAmplitude(index);
        amplitudeSlider.setValue(amplitude);
        amplitudeValueLabel.setText(Messages.format(KEY_AMPLITUDE_VALUE, amplitude));

        amplitudeSlider
                .valueProperty()
                .addListener(
                        (_, _, newValue) -> {
                            playback.updateAmplitude(index, newValue.doubleValue());
                            amplitudeValueLabel.setText(
                                    Messages.format(KEY_AMPLITUDE_VALUE, newValue.doubleValue()));
                            rebuildRequest.run();
                        });
    }

    private void configureDelaySlider(
            final int index, final PlaybackController playback, final Runnable rebuildRequest) {
        delaySlider.setMin(MIN_DELAY_SECONDS);
        delaySlider.setMax(MAX_DELAY_SECONDS);
        delaySlider.setBlockIncrement(DELAY_BLOCK_INCREMENT);

        final var settings = playback.getSettings();
        final var delaySeconds = settings.getDelaySeconds(index);
        delaySlider.setValue(delaySeconds);
        delayValueLabel.setText(Messages.format(KEY_DELAY_VALUE, delaySeconds));

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
