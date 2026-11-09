package org.nstu.se.lab4.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import org.jspecify.annotations.Nullable;
import org.nstu.se.lab4.audio.SignalSource;

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
        drawWaveform();
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

        final var centerY = height / 2.0;
        final var amplitude = height * WAVEFORM_HEIGHT_RATIO / 2.0;
        final var sampleCount = source.getSampleCount();
        final var samplesPerPixel = Math.max(1, sampleCount / (int) width);

        graphics.setStroke(WAVEFORM_COLOR);
        graphics.setLineWidth(1.0);
        graphics.beginPath();

        for (var pixel = 0; pixel < (int) width; pixel++) {
            final var startSample = pixel * samplesPerPixel;
            if (startSample >= sampleCount) {
                break;
            }
            final var endSample = Math.min(startSample + samplesPerPixel, sampleCount);

            var min = 1.0f;
            var max = -1.0f;

            for (var sampleIndex = startSample; sampleIndex < endSample; sampleIndex++) {
                final var sample = source.getSample(sampleIndex);
                min = Math.min(min, sample);
                max = Math.max(max, sample);
            }

            final var yMin = centerY - max * amplitude;
            final var yMax = centerY - min * amplitude;

            graphics.moveTo(pixel, yMin);
            graphics.lineTo(pixel, yMax);
        }

        graphics.stroke();
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
