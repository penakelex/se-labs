package org.nstu.se.lab4.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

import org.nstu.se.lab4.Messages;
import org.nstu.se.lab4.audio.mix.SliderRanges;
import org.nstu.se.lab4.audio.playback.PlaybackController;

@SuppressWarnings("NullAway.Init")
public final class SourceRowController {
    private static final String KEY_AMPLITUDE_VALUE = "amplitude.value";
    private static final String KEY_DELAY_VALUE = "delay.value";
    private static final String KEY_LABEL_AMPLITUDE = "label.amplitude";
    private static final String KEY_LABEL_DELAY = "label.delay";
    private static final String KEY_BUTTON_REMOVE = "button.remove";
    private static final String KEY_BUTTON_SUBSCRIBE = "button.subscribe";

    private static final double AMPLITUDE_BLOCK_INCREMENT = 0.01;
    private static final double DELAY_BLOCK_INCREMENT = 0.01;

    @FXML private Label nameLabel;
    @FXML private Label amplitudeLabel;
    @FXML private Slider amplitudeSlider;
    @FXML private Label amplitudeValueLabel;
    @FXML private Label delayLabel;
    @FXML private Slider delaySlider;
    @FXML private Label delayValueLabel;
    @FXML private Button removeButton;
    @FXML private Button subscribeButton;

    public void configure(
            final int index,
            final PlaybackController playback,
            final SliderRanges ranges,
            final Runnable rebuildRequest,
            final Runnable structureChangeRequest,
            final Runnable subscribeRequest) {
        configureLabels(index, playback);
        configureAmplitudeSlider(index, playback, ranges, rebuildRequest);
        configureDelaySlider(index, playback, ranges, rebuildRequest);
        removeButton.setText(Messages.get(KEY_BUTTON_REMOVE));
        removeButton.setOnAction(
                _ -> {
                    playback.session().removeSource(index);
                    structureChangeRequest.run();
                });
        subscribeButton.setText(Messages.get(KEY_BUTTON_SUBSCRIBE));
        subscribeButton.setOnAction(_ -> subscribeRequest.run());
    }

    private void configureLabels(final int index, final PlaybackController playback) {
        nameLabel.setText(playback.session().settings().getChannelName(index));
        amplitudeLabel.setText(Messages.get(KEY_LABEL_AMPLITUDE));
        delayLabel.setText(Messages.get(KEY_LABEL_DELAY));
    }

    private void configureAmplitudeSlider(
            final int index,
            final PlaybackController playback,
            final SliderRanges ranges,
            final Runnable rebuildRequest) {
        amplitudeSlider.setMin(ranges.minAmplitude());
        amplitudeSlider.setMax(ranges.maxAmplitude());
        amplitudeSlider.setBlockIncrement(AMPLITUDE_BLOCK_INCREMENT);

        final var settings = playback.session().settings();
        final var amplitude = settings.getAmplitude(index);
        amplitudeSlider.setValue(amplitude);
        amplitudeValueLabel.setText(Messages.format(KEY_AMPLITUDE_VALUE, amplitude));

        amplitudeSlider
                .valueProperty()
                .addListener(
                        (_, _, newValue) -> {
                            playback.session().updateAmplitude(index, newValue.doubleValue());
                            amplitudeValueLabel.setText(
                                    Messages.format(KEY_AMPLITUDE_VALUE, newValue.doubleValue()));
                            rebuildRequest.run();
                        });
    }

    private void configureDelaySlider(
            final int index,
            final PlaybackController playback,
            final SliderRanges ranges,
            final Runnable rebuildRequest) {
        delaySlider.setMin(ranges.minDelaySeconds());
        delaySlider.setMax(ranges.maxDelaySeconds());
        delaySlider.setBlockIncrement(DELAY_BLOCK_INCREMENT);

        final var settings = playback.session().settings();
        final var delaySeconds = settings.getDelaySeconds(index);
        delaySlider.setValue(delaySeconds);
        delayValueLabel.setText(Messages.format(KEY_DELAY_VALUE, delaySeconds));

        delaySlider
                .valueProperty()
                .addListener(
                        (_, _, newValue) -> {
                            playback.session().updateDelaySeconds(index, newValue.doubleValue());
                            delayValueLabel.setText(
                                    Messages.format(KEY_DELAY_VALUE, newValue.doubleValue()));
                            rebuildRequest.run();
                        });
    }
}
