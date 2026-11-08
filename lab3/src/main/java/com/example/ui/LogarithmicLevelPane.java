package com.example.ui;

import com.example.listener.NextDataListener;
import com.example.model.DataAccumulator;
import com.example.model.LogScaleModel;
import com.example.timer.DataPublisher;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class LogarithmicLevelPane extends VBox {

    private static final String UNIFIED_BG_STYLE = "-fx-background-color: #d4d0c8; -fx-border-color: #808080; -fx-border-width: 1.5; -fx-border-radius: 6; -fx-background-radius: 6;";
    private static final String UNIFIED_FONT = "-fx-font-family: 'Segoe UI', 'Arial', sans-serif;";

    private final LogScaleModel scaleModel;
    private final DataAccumulator accumulator;
    private final DataPublisher publisher;

    private final Slider slider;
    private final Rectangle meterBar;
    private final Label lblValue;
    private final Label lblDb;

    private final double meterMaxHeight = 160.0;

    public LogarithmicLevelPane(int v0, double minDb, double maxDb, int periodMs, NextDataListener dataListener) {
        this.scaleModel = new LogScaleModel(v0, minDb, maxDb);
        this.accumulator = new DataAccumulator();

        setAlignment(Pos.CENTER);
        setSpacing(15);
        setPadding(new javafx.geometry.Insets(15));
        setStyle(UNIFIED_BG_STYLE + " " + UNIFIED_FONT + " -fx-font-size: 13px;");

        lblValue = new Label();
        lblValue.setStyle(UNIFIED_FONT + " -fx-text-fill: #006600; -fx-font-size: 15px; -fx-font-weight: bold;");

        lblDb = new Label();
        lblDb.setStyle(UNIFIED_FONT + " -fx-text-fill: #333333; -fx-font-size: 15px; -fx-font-weight: bold;");

        HBox mixerBox = new HBox(20);
        mixerBox.setAlignment(Pos.CENTER);

        VBox sliderBox = new VBox(5);
        sliderBox.setAlignment(Pos.CENTER);

        Label lblTopDb = new Label("dB");
        lblTopDb.setStyle(UNIFIED_FONT + " -fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #222;");

        slider = new Slider(scaleModel.getMinDb(), scaleModel.getMaxDb(), 0.0);
        slider.setOrientation(Orientation.VERTICAL);
        slider.setPrefHeight(meterMaxHeight);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setMajorTickUnit((maxDb - minDb) / 4.0);
        // Применяем шрифт к слайдеру
        slider.setStyle(UNIFIED_FONT);

        sliderBox.getChildren().addAll(lblTopDb, slider);

        VBox meterContainer = new VBox(5);
        meterContainer.setAlignment(Pos.CENTER);

        Label lblMeterTop = new Label("0");
        lblMeterTop.setStyle(UNIFIED_FONT + " -fx-font-size: 12px; -fx-text-fill: #222;");

        // Черная подложка индикатора с таким же скруглением (6px)
        Rectangle meterBackground = new Rectangle(22, meterMaxHeight, Color.BLACK);
        meterBackground.setArcWidth(6);
        meterBackground.setArcHeight(6);

        // Активный заполняющийся столбик с таким же скруглением (6px)
        meterBar = new Rectangle(18, 0, Color.LIME);
        meterBar.setArcWidth(6);
        meterBar.setArcHeight(6);

        // Накладываем заполняющий столбик поверх подложки
        StackPane meterPane = new StackPane();
        meterPane.setAlignment(Pos.BOTTOM_CENTER);
        meterPane.getChildren().addAll(meterBackground, meterBar);

        Label lblMeterBottom = new Label("Min");
        lblMeterBottom.setStyle(UNIFIED_FONT + " -fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #222;");

        meterContainer.getChildren().addAll(lblMeterTop, meterPane, lblMeterBottom);

        mixerBox.getChildren().addAll(sliderBox, meterContainer);
        getChildren().addAll(lblValue, lblDb, mixerBox);

        // Логика изменения значения ползунка
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double currentDb = newVal.doubleValue();
            int calculatedValue = scaleModel.calculateValueFromDb(currentDb);

            updateDisplayAndMeter(calculatedValue, currentDb);
            accumulator.addValue(calculatedValue);

            if (dataListener != null) {
                dataListener.onNextData(calculatedValue);
            }
        });

        slider.setValue(0.0);
        updateDisplayAndMeter(scaleModel.getV0(), 0.0);

        this.publisher = new DataPublisher(periodMs, this::getCurrentValue, dataListener);
        this.publisher.start();
    }

    private void updateDisplayAndMeter(int v, double db) {
        lblValue.setText(String.format("V: %d", v));
        lblDb.setText(String.format("%.1f dB", db));

        double range = scaleModel.getMaxDb() - scaleModel.getMinDb();
        double pct = (db - scaleModel.getMinDb()) / range;
        pct = Math.max(0.0, Math.min(1.0, pct));

        double barHeight = pct * (meterMaxHeight - 4);
        meterBar.setHeight(barHeight);

        if (db > 0) {
            meterBar.setFill(Color.RED);
        } else if (db > -12) {
            meterBar.setFill(Color.YELLOW);
        } else {
            meterBar.setFill(Color.LIME);
        }
    }

    public int getCurrentValue() {
        return scaleModel.calculateValueFromDb(slider.getValue());
    }

    public List<Integer> getAccumulatedData() {
        return accumulator.getAccumulatedData();
    }

    public void dispose() {
        if (publisher != null) {
            publisher.stop();
        }
    }
}
