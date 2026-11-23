package org.nstu.se.lab4.ui;

import javafx.animation.PauseTransition;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import org.jspecify.annotations.Nullable;
import org.nstu.se.lab4.audio.source.SignalSource;

public final class WaveformView extends Pane {
    private static final double WAVEFORM_HEIGHT_RATIO = 0.8;
    private static final Color WAVEFORM_COLOR = Color.web("#6366f1");
    private static final Color ENVELOPE_BAND_COLOR = WAVEFORM_COLOR.deriveColor(0, 1, 1, 0.15);
    private static final double MIDLINE_WIDTH = 1.2;
    private static final double CORNER_RADIUS = 24.0;
    private static final long RESIZE_SETTLE_MILLIS = 80;
    private static final Color PLAYHEAD_COLOR = Color.web("#ef4444");
    private static final double PLAYHEAD_WIDTH = 2.0;

    private final Canvas waveformCanvas = new Canvas();
    private final Canvas overlayCanvas = new Canvas();
    private final Canvas renderCache = new Canvas();
    private final PauseTransition resizeSettle =
            new PauseTransition(Duration.millis(RESIZE_SETTLE_MILLIS));

    private @Nullable SignalSource source;
    private WaveformEnvelope envelope = WaveformEnvelope.empty();
    private @Nullable WritableImage cacheImage;
    private boolean cacheValid;
    private double positionSeconds;
    private double durationSeconds = 1.0;

    public WaveformView() {
        super();

        final var clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        clip.setArcWidth(CORNER_RADIUS);
        clip.setArcHeight(CORNER_RADIUS);
        setClip(clip);

        resizeSettle.setOnFinished(_ -> renderCrisp());

        getChildren().addAll(waveformCanvas, overlayCanvas);
    }

    public void setSource(final SignalSource newSource) {
        this.source = newSource;
        invalidateEnvelope();
        this.durationSeconds = (double) newSource.getSampleCount() / newSource.getSampleRate();
        drawWaveform();
        drawPlayhead();
    }

    public void setPosition(final double seconds) {
        this.positionSeconds = seconds;
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

    private void invalidateEnvelope() {
        envelope = WaveformEnvelope.empty();
        cacheValid = false;
    }

    private void drawWaveform() {
        final var graphics = waveformCanvas.getGraphicsContext2D();
        final var width = waveformCanvas.getWidth();
        final var height = waveformCanvas.getHeight();

        graphics.clearRect(0, 0, width, height);

        if (source == null || width <= 0 || height <= 0) {
            cacheValid = false;
            return;
        }

        if (envelope.isEmpty()) {
            envelope = WaveformEnvelope.compute(source);
        }

        if (!cacheValid) {
            renderIntoCache(width, height);
        }

        final var image = cacheImage;

        if (image != null) {
            graphics.drawImage(image, 0, 0, width, height);
        }

        resizeSettle.playFromStart();
    }

    private void renderCrisp() {
        final var width = waveformCanvas.getWidth();
        final var height = waveformCanvas.getHeight();

        if (source == null || width <= 0 || height <= 0) {
            return;
        }

        if (renderCache.getWidth() == width && renderCache.getHeight() == height) {
            return;
        }

        renderIntoCache(width, height);

        final var image = cacheImage;

        if (image != null) {
            waveformCanvas.getGraphicsContext2D().drawImage(image, 0, 0, width, height);
        }
    }

    private void renderIntoCache(final double width, final double height) {
        renderCache.setWidth(width);
        renderCache.setHeight(height);

        final var graphics = renderCache.getGraphicsContext2D();
        graphics.clearRect(0, 0, width, height);
        drawEnvelope(graphics, envelope, width, height);
        cacheImage = renderCache.snapshot(null, null);
        cacheValid = true;
    }

    private static void drawEnvelope(
            final GraphicsContext graphics,
            final WaveformEnvelope envelope,
            final double width,
            final double height) {
        final var minValues = envelope.minValues();
        final var maxValues = envelope.maxValues();
        final var midValues = envelope.midValues();
        final var bucketCount = minValues.length;
        final var centerY = height / 2.0;
        final var amplitude = height * WAVEFORM_HEIGHT_RATIO / 2.0;
        final var scaleX = width / bucketCount;

        graphics.setFill(ENVELOPE_BAND_COLOR);
        graphics.beginPath();
        graphics.moveTo(0.5 * scaleX, centerY - maxValues[0] * amplitude);

        for (var bucket = 1; bucket < bucketCount; bucket++) {
            graphics.lineTo((bucket + 0.5) * scaleX, centerY - maxValues[bucket] * amplitude);
        }

        for (var bucket = bucketCount - 1; bucket >= 0; bucket--) {
            graphics.lineTo((bucket + 0.5) * scaleX, centerY - minValues[bucket] * amplitude);
        }

        graphics.closePath();
        graphics.fill();

        graphics.setStroke(WAVEFORM_COLOR);
        graphics.setLineWidth(MIDLINE_WIDTH);
        graphics.beginPath();
        graphics.moveTo(0.5 * scaleX, centerY - midValues[0] * amplitude);

        for (var bucket = 1; bucket < bucketCount; bucket++) {
            graphics.lineTo((bucket + 0.5) * scaleX, centerY - midValues[bucket] * amplitude);
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
