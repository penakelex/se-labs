package org.nstu.se.lab4.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import org.jspecify.annotations.Nullable;
import org.nstu.se.lab4.audio.source.SignalSource;

public final class WaveformView extends Pane {
    private static final double WAVEFORM_HEIGHT_RATIO = 0.8;
    private static final Color WAVEFORM_COLOR = Color.web("#6366f1");
    private static final Color PLAYHEAD_COLOR = Color.web("#ef4444");
    private static final double PLAYHEAD_WIDTH = 2.0;

    private final Canvas waveformCanvas = new Canvas();
    private final Canvas overlayCanvas = new Canvas();
    private @Nullable SignalSource source;
    private double positionSeconds;
    private double durationSeconds = 1.0;

    public WaveformView() {
        super();

        final var clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        clip.setArcWidth(24.0);
        clip.setArcHeight(24.0);
        setClip(clip);

        getChildren().addAll(waveformCanvas, overlayCanvas);
    }

    public void setSource(final SignalSource newSource) {
        this.source = newSource;
        this.durationSeconds = (double) newSource.getSampleCount() / newSource.getSampleRate();
        drawWaveform();
        drawPlayhead();
    }

    public void setPosition(final double seconds) {
        this.positionSeconds = seconds;
        drawPlayhead();
    }

    public void refresh() {
        if (source != null) {
            durationSeconds = (double) source.getSampleCount() / source.getSampleRate();
        }
        drawWaveform();
        drawPlayhead();
    }

    @Override
    protected void layoutChildren() {
        final var width = getWidth();
        final var height = getHeight();
        waveformCanvas.setWidth(width);
        waveformCanvas.setHeight(height);
        overlayCanvas.setWidth(width);
        overlayCanvas.setHeight(height);
        waveformCanvas.relocate(0, 0);
        overlayCanvas.relocate(0, 0);
        drawWaveform();
        drawPlayhead();
    }

    private void drawWaveform() {
        final var graphics = waveformCanvas.getGraphicsContext2D();
        final var width = waveformCanvas.getWidth();
        final var height = waveformCanvas.getHeight();

        graphics.clearRect(0, 0, width, height);

        if (source == null || width <= 0 || height <= 0) {
            return;
        }

        final var envelope = computeEnvelope(source, (int) width, height);
        drawEnvelope(graphics, envelope);
    }

    private static double[][] computeEnvelope(
            final SignalSource source, final int pixels, final double height) {
        final var centerY = height / 2.0;
        final var amplitude = height * WAVEFORM_HEIGHT_RATIO / 2.0;
        final var sampleCount = source.getSampleCount();
        final var envelope = new double[3][pixels];
        var bucketStart = 0;

        for (var pixel = 0; pixel < pixels; pixel++) {
            final var bucketEnd =
                    Math.max(bucketStart + 1, (int) ((double) (pixel + 1) / pixels * sampleCount));

            var minimum = Float.MAX_VALUE;
            var maximum = -Float.MAX_VALUE;
            final var firstSample = source.getSample(bucketStart);

            for (var index = bucketStart; index < bucketEnd && index < sampleCount; index++) {
                final var sample = source.getSample(index);

                if (sample < minimum) {
                    minimum = sample;
                }

                if (sample > maximum) {
                    maximum = sample;
                }
            }

            bucketStart = bucketEnd;
            envelope[0][pixel] = centerY - minimum * amplitude;
            envelope[1][pixel] = centerY - maximum * amplitude;
            envelope[2][pixel] = centerY - firstSample * amplitude;
        }

        return envelope;
    }

    private static void drawEnvelope(
            final javafx.scene.canvas.GraphicsContext graphics, final double[]... envelope) {
        final var minValues = envelope[0];
        final var maxValues = envelope[1];
        final var pixels = minValues.length;

        graphics.setFill(WAVEFORM_COLOR.deriveColor(0, 1, 1, 0.25));
        graphics.beginPath();
        tracePath(graphics, maxValues);

        for (var pixel = pixels - 1; pixel >= 0; pixel--) {
            graphics.lineTo(pixel, minValues[pixel]);
        }

        graphics.closePath();
        graphics.fill();

        graphics.setStroke(WAVEFORM_COLOR);
        graphics.setLineWidth(1.0);
        graphics.beginPath();
        tracePath(graphics, maxValues);
        graphics.stroke();

        graphics.beginPath();
        tracePath(graphics, minValues);
        graphics.stroke();

        graphics.setLineWidth(1.4);
        graphics.beginPath();
        tracePath(graphics, envelope[2]);
        graphics.stroke();
    }

    private static void tracePath(
            final javafx.scene.canvas.GraphicsContext graphics, final double... values) {
        graphics.moveTo(0, values[0]);

        for (var pixel = 1; pixel < values.length; pixel++) {
            graphics.lineTo(pixel, values[pixel]);
        }
    }

    private void drawPlayhead() {
        final var graphics = overlayCanvas.getGraphicsContext2D();
        final var width = overlayCanvas.getWidth();
        final var height = overlayCanvas.getHeight();

        graphics.clearRect(0, 0, width, height);

        if (durationSeconds > 0 && width > 0 && height > 0) {
            final var playheadX = positionSeconds / durationSeconds * width;
            graphics.setStroke(PLAYHEAD_COLOR);
            graphics.setLineWidth(PLAYHEAD_WIDTH);
            graphics.strokeLine(playheadX, 0, playheadX, height);
        }
    }
}
