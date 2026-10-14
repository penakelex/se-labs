package org.nstu.se.lab1.ui.animation;

import javafx.animation.Animation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;

import org.nstu.se.lab1.AppContext;
import org.nstu.se.lab1.Messages;
import org.nstu.se.lab1.ResourcePaths;
import org.nstu.se.lab1.model.QuickSorter;
import org.nstu.se.lab1.model.SortArray;
import org.nstu.se.lab1.model.SortStep;
import org.nstu.se.lab1.ui.menu.MenuController;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SortController implements Initializable {
    private static final String KEY_PLAY = "sort.button.play";
    private static final String KEY_TOOLTIP_START = "sort.tooltip.start";
    private static final String KEY_TOOLTIP_PAUSE = "sort.tooltip.pause";
    private static final String KEY_TOOLTIP_RESUME = "sort.tooltip.resume";

    @FXML private Pane animationPane;
    @FXML private Slider speedSlider;
    @FXML private Button playPauseButton;
    @FXML private Button restartButton;

    private SortArray<Integer> model;
    private SortElement[] elements;
    private Animation currentAnimation;
    private boolean paused;
    private boolean running;

    private final AppContext context;

    public SortController(final AppContext context) {
        this.context = context;
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        collapseWhenDisabled(playPauseButton);
        collapseWhenDisabled(restartButton);

        restartButton.setDisable(true);
        model = context.retrieveModel();
        showInitialArray();

        resetPlayPauseButton();
        playPauseButton.setDisable(model == null || model.size() < 2);

        animationPane
                .layoutBoundsProperty()
                .addListener(
                        (_, _, bounds) -> {
                            if (bounds.getWidth() > 0
                                    && bounds.getHeight() > 0
                                    && animationPane.getChildren().isEmpty()) {
                                showInitialArray();
                            }
                        });
    }

    private static void collapseWhenDisabled(final Button button) {
        button.visibleProperty().bind(button.disableProperty().not());
        button.managedProperty().bind(button.visibleProperty());
    }

    private void showInitialArray() {
        if (model == null || model.size() == 0 || animationPane.getWidth() <= 0) {
            playPauseButton.setDisable(true);
            return;
        }

        elements = SortVisualizationBuilder.buildArray(animationPane, model);
        playPauseButton.setDisable(model.size() < 2);
    }

    @FXML
    public void togglePlayPause() {
        if (model == null || model.size() < 2) {
            return;
        }

        if (!running) {
            startNewAnimation();
            return;
        }

        if (paused) {
            currentAnimation.play();
            paused = false;
            setPauseButton();
        } else {
            currentAnimation.pause();
            paused = true;
            setResumeButton();
        }
    }

    @FXML
    public void restart() {
        final boolean wasPlaying = running && !paused;

        if (currentAnimation != null) {
            currentAnimation.stop();
        }

        running = false;
        paused = false;

        elements = SortVisualizationBuilder.buildArray(animationPane, model);
        resetPlayPauseButton();

        if (wasPlaying) {
            startNewAnimation();
        }
    }

    public void comeBack() throws IOException {
        if (currentAnimation != null) {
            currentAnimation.stop();
        }

        final var loader =
                new FXMLLoader(
                        ResourcePaths.url(ResourcePaths.MENU_VIEW),
                        ResourceBundle.getBundle(Messages.BUNDLE_NAME));

        loader.setControllerFactory(_ -> new MenuController(context));

        animationPane.getScene().setRoot(loader.load());
    }

    private void startNewAnimation() {
        final List<SortStep> steps = QuickSorter.plan(model);
        currentAnimation = SortAnimator.build(steps, elements);
        currentAnimation.rateProperty().bind(speedSlider.valueProperty());
        currentAnimation.setOnFinished(
                _ -> {
                    running = false;
                    paused = false;
                    resetPlayPauseButton();
                });

        running = true;
        setPauseButton();
        restartButton.setDisable(false);
        currentAnimation.play();
    }

    private void resetPlayPauseButton() {
        playPauseButton.setText(Messages.get(KEY_PLAY));
        playPauseButton.setTooltip(new Tooltip(Messages.get(KEY_TOOLTIP_START)));
    }

    private void setPauseButton() {
        playPauseButton.setText(Messages.get("sort.button.pause"));
        playPauseButton.setTooltip(new Tooltip(Messages.get(KEY_TOOLTIP_PAUSE)));
    }

    private void setResumeButton() {
        playPauseButton.setText(Messages.get(KEY_PLAY));
        playPauseButton.setTooltip(new Tooltip(Messages.get(KEY_TOOLTIP_RESUME)));
    }
}
