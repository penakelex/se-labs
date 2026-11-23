package org.nstu.se.lab4.ui.monitor;

import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import org.nstu.se.lab4.Messages;

public final class LevelMeterView extends HBox {
    private static final double MIN_DECIBELS = -60.0;
    private static final double MAX_DECIBELS = 0.0;
    private static final double MIDDLE_PERCENTAGE = 0.5;
    private static final double MULTIPLIER = 2.0;
    private static final double EPSILON = 1.0e-6;
    private static final double LOG10_FACTOR = 20.0;

    private static final double MIN_METER_HEIGHT = 180.0;
    private static final double METER_WIDTH = 90.0;

    private static final double CLIP_ARC = 16.0;
    private static final double HOLDER_MIN_HEIGHT = 60.0;
    private static final double AMPLITUDE_LABEL_MIN_WIDTH = 70.0;
    private static final double AMPLITUDE_PRECISION = 1000.0;
    private static final double DECIBEL_PRECISION = 10.0;
    private static final double METER_CORNER_RADIUS = 16.0;
    private static final double LEVEL_FONT_SIZE = 12.0;
    private static final double TEXT_MIN_TOP = 16.0;
    private static final double TEXT_BOTTOM_OFFSET = 8.0;

    private static final Color LOW_COLOR = Color.web("#10b981");
    private static final Color MID_COLOR = Color.web("#f59e0b");
    private static final Color HIGH_COLOR = Color.web("#ef4444");
    private static final Color BACKGROUND_COLOR = Color.web("#f1f5f9");
    private static final Color BORDER_COLOR = Color.web("#e2e8f0");

    private static final String KEY_LEVEL_FORMAT = "monitor.level.format";
    private static final String KEY_AMPLITUDE_FORMAT = "monitor.amplitude.format";
    private static final String KEY_MAX_LABEL = "monitor.max";
    private static final String KEY_MIN_LABEL = "monitor.min";
    private static final String STYLE_CAPTION = "meter-caption";
    private static final String STYLE_AMPLITUDE = "meter-amplitude";
    private static final String FONT_FAMILY = "Segoe UI";

    private final Canvas canvas = new Canvas();
    private final Label amplitudeLabel = new Label();

    private double levelDecibels = MIN_DECIBELS;

    public LevelMeterView() {
        super();

        final var clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        clip.setArcWidth(CLIP_ARC);
        clip.setArcHeight(CLIP_ARC);
        setClip(clip);

        final var maxLabel = new Label(Messages.get(KEY_MAX_LABEL));
        final var minLabel = new Label(Messages.get(KEY_MIN_LABEL));

        getStyleClass().add("level-meter");
        maxLabel.getStyleClass().add(STYLE_CAPTION);
        minLabel.getStyleClass().add(STYLE_CAPTION);
        amplitudeLabel.getStyleClass().add(STYLE_AMPLITUDE);
        maxLabel.setMaxWidth(Double.MAX_VALUE);
        minLabel.setMaxWidth(Double.MAX_VALUE);

        final var holder = new Pane(canvas);
        holder.setMinHeight(HOLDER_MIN_HEIGHT);
        VBox.setVgrow(holder, Priority.ALWAYS);
        canvas.widthProperty().bind(holder.widthProperty());
        canvas.heightProperty().bind(holder.heightProperty());

        final var meterBox = new VBox(maxLabel, holder, minLabel);
        meterBox.setMaxWidth(METER_WIDTH);
        meterBox.setMinWidth(METER_WIDTH);
        meterBox.setMinHeight(MIN_METER_HEIGHT);
        HBox.setHgrow(meterBox, Priority.NEVER);

        amplitudeLabel.setMinWidth(AMPLITUDE_LABEL_MIN_WIDTH);
        amplitudeLabel.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(amplitudeLabel, Priority.ALWAYS);

        getChildren().addAll(meterBox, amplitudeLabel);
        setAlignment(Pos.CENTER_LEFT);
        setMinHeight(MIN_METER_HEIGHT);
    }

    public void setLevel(final float sample) {
        final var magnitude = Math.max(Math.abs(sample), (float) EPSILON);
        levelDecibels =
                Math.clamp(LOG10_FACTOR * Math.log10(magnitude), MIN_DECIBELS, MAX_DECIBELS);
        amplitudeLabel.setText(
                Messages.format(
                        KEY_AMPLITUDE_FORMAT,
                        Math.round(Math.abs(sample) * AMPLITUDE_PRECISION) / AMPLITUDE_PRECISION));
        draw();
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        draw();
    }

    private void draw() {
        final var graphics = canvas.getGraphicsContext2D();
        final var width = canvas.getWidth();
        final var height = canvas.getHeight();

        graphics.clearRect(0, 0, width, height);

        if (width <= 0 || height <= 0) {
            return;
        }

        final var percentage = percentageOf(levelDecibels);
        final var fillHeight = percentage * height;

        graphics.setFill(BACKGROUND_COLOR);
        graphics.fillRoundRect(0, 0, width, height, METER_CORNER_RADIUS, METER_CORNER_RADIUS);
        graphics.setStroke(BORDER_COLOR);
        graphics.setLineWidth(1.0);
        graphics.strokeRoundRect(
                0.5, 0.5, width - 1, height - 1, METER_CORNER_RADIUS, METER_CORNER_RADIUS);

        graphics.setFill(colorFor(percentage));
        graphics.fillRect(0, height - fillHeight, width, fillHeight);

        graphics.setFill(Color.web("#0f172a"));
        graphics.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        graphics.setFont(
                javafx.scene.text.Font.font(
                        FONT_FAMILY, javafx.scene.text.FontWeight.BOLD, LEVEL_FONT_SIZE));
        graphics.fillText(
                Messages.format(
                        KEY_LEVEL_FORMAT,
                        Math.round(levelDecibels * DECIBEL_PRECISION) / DECIBEL_PRECISION),
                width / 2,
                Math.max(TEXT_MIN_TOP, height - fillHeight - TEXT_BOTTOM_OFFSET));
    }

    private double percentageOf(final double decibels) {
        final var raw = (decibels - MIN_DECIBELS) / (MAX_DECIBELS - MIN_DECIBELS);
        return Math.clamp(raw, 0.0, 1.0);
    }

    private Color colorFor(final double percentage) {
        if (percentage < MIDDLE_PERCENTAGE) {
            return LOW_COLOR.interpolate(MID_COLOR, percentage * MULTIPLIER);
        }

        return MID_COLOR.interpolate(HIGH_COLOR, (percentage - MIDDLE_PERCENTAGE) * MULTIPLIER);
    }
}
