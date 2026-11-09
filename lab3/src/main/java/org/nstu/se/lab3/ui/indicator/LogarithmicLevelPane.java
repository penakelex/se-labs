package org.nstu.se.lab3.ui.indicator;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import org.jspecify.annotations.Nullable;
import org.nstu.se.lab3.Messages;
import org.nstu.se.lab3.listener.NextDataListener;
import org.nstu.se.lab3.model.DataAccumulator;
import org.nstu.se.lab3.model.LogScaleModel;
import org.nstu.se.lab3.model.SettingsValues;
import org.nstu.se.lab3.timer.DataPublisher;

public final class LogarithmicLevelPane extends VBox {
    private static final double METER_PADDING = 150.0;
    private static final String STYLE_METER_CAPTION = "meter-caption";
    private static final String KEY_DECIBELS_LABEL = "level.label.decibels";
    private static final String KEY_MAX_LABEL = "level.label.max";
    private static final String KEY_MIN_LABEL = "level.label.min";

    private final LogScaleModel scaleModel;
    private final DataPublisher publisher;
    private final Slider slider;
    private final MeterRenderer meterRenderer;

    public LogarithmicLevelPane(
            final SettingsValues settings,
            final DataAccumulator accumulator,
            final @Nullable NextDataListener dataListener) {
        super();
        this.scaleModel =
                new LogScaleModel(
                        settings.zeroLevelValue(), settings.minDecibels(), settings.maxDecibels());

        getStyleClass().add("level-pane");

        final var valueLabel = LevelWidgets.createValueLabel();
        final var decibelLabel = LevelWidgets.createDecibelLabel();
        this.slider = LevelWidgets.createSlider(scaleModel);
        this.meterRenderer = new MeterRenderer(scaleModel, valueLabel, decibelLabel);

        final var mixerBox = buildMixerBox();
        getChildren().addAll(valueLabel, decibelLabel, mixerBox);

        bindHeights();
        setupSliderSkinListener();

        slider.valueProperty().addListener((_, _, _) -> meterRenderer.render(slider.getValue()));
        slider.setValue(Math.clamp(0.0, scaleModel.getMinDecibels(), scaleModel.getMaxDecibels()));
        meterRenderer.render(slider.getValue());

        publisher = createPublisher(settings, accumulator, dataListener);
        publisher.start();
    }

    private HBox buildMixerBox() {
        final var sliderBox = new VBox();
        sliderBox.getStyleClass().add("slider-box");

        final var decibelsCaption = new Label(Messages.get(KEY_DECIBELS_LABEL));
        decibelsCaption.getStyleClass().add(STYLE_METER_CAPTION);

        final var bottomSpacer = new Region();
        sliderBox.getChildren().addAll(decibelsCaption, slider, bottomSpacer);

        final var meterBox = new VBox();
        meterBox.getStyleClass().add("meter-box");

        final var topLabel = new Label(Messages.get(KEY_MAX_LABEL));
        topLabel.getStyleClass().add(STYLE_METER_CAPTION);
        final var bottomLabel = new Label(Messages.get(KEY_MIN_LABEL));
        bottomLabel.getStyleClass().add(STYLE_METER_CAPTION);

        meterBox.getChildren().addAll(topLabel, meterRenderer.getContainer(), bottomLabel);

        final var mixerBox = new HBox();
        mixerBox.getStyleClass().add("mixer-box");
        VBox.setVgrow(mixerBox, Priority.ALWAYS);

        decibelsCaption.minHeightProperty().bind(topLabel.heightProperty());
        bottomSpacer.minHeightProperty().bind(bottomLabel.heightProperty());

        mixerBox.getChildren().addAll(sliderBox, meterBox);
        return mixerBox;
    }

    private void bindHeights() {
        final var dynamicMeterHeight =
                Bindings.max(MeterRenderer.MIN_HEIGHT, heightProperty().subtract(METER_PADDING));
        slider.prefHeightProperty().bind(dynamicMeterHeight);
        meterRenderer.bindPreferredHeight(dynamicMeterHeight);
    }

    private void setupSliderSkinListener() {
        slider.skinProperty()
                .addListener(
                        (_, _, newSkin) -> {
                            if (newSkin == null) {
                                return;
                            }
                            Platform.runLater(this::bindMeterToTrack);
                        });
    }

    private void bindMeterToTrack() {
        final var track = slider.lookup(".track");

        if (track instanceof Region trackRegion) {
            meterRenderer.bindHeightTo(trackRegion, slider::getValue);
        }
    }

    private DataPublisher createPublisher(
            final SettingsValues settings,
            final DataAccumulator accumulator,
            final @Nullable NextDataListener dataListener) {
        return new DataPublisher(
                settings.periodMillis(),
                this::getCurrentValue,
                value -> {
                    accumulator.addValue(value);
                    if (dataListener != null) {
                        dataListener.onNextData(value);
                    }
                });
    }

    public int getCurrentValue() {
        return scaleModel.calculateValueFromDecibels(slider.getValue());
    }

    public void dispose() {
        publisher.stop();
    }
}
