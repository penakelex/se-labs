package org.nstu.se.lab1.ui.animation;

import javafx.animation.Animation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
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
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

public class SortController implements Initializable {
    @FXML private Pane animationPane;
    @FXML private Slider speedSlider;
    @FXML private Button playPauseButton;
    @FXML private Button restartButton;

    private SortArray<Integer> model;
    private SortElement[] elements;
    private Animation currentAnimation;
    private List<SortStep> sortingPlan;
    private Iterator<SortStep> steps;
    private boolean paused;
    private boolean running;

    private final AppContext context;

    public SortController(final AppContext context) {
        this.context = context;
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        SortButtonManager.collapseWhenDisabled(playPauseButton);
        SortButtonManager.collapseWhenDisabled(restartButton);

        restartButton.setDisable(true);
        model = context.retrieveModel();
        showInitialArray();

        SortButtonManager.resetPlayPauseButton(playPauseButton);
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

    private void showInitialArray() {
        if (model == null || model.size() == 0 || animationPane.getWidth() <= 0) {
            playPauseButton.setDisable(true);
            return;
        }

        elements = SortVisualizationBuilder.buildArray(animationPane, model);
        sortingPlan = QuickSorter.plan(model);
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
            SortButtonManager.setPauseButton(playPauseButton);
        } else {
            currentAnimation.pause();
            paused = true;
            SortButtonManager.setResumeButton(playPauseButton);
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
        SortButtonManager.resetPlayPauseButton(playPauseButton);

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
        elements = SortVisualizationBuilder.buildArray(animationPane, model);
        steps = sortingPlan.iterator();

        running = true;
        paused = false;

        restartButton.setDisable(false);
        SortButtonManager.setPauseButton(playPauseButton);

        playNextStep();
    }

    private void playNextStep() {
        if (!running || steps == null) {
            return;
        }

        if (!steps.hasNext()) {
            running = false;
            paused = false;
            SortButtonManager.resetPlayPauseButton(playPauseButton);
            return;
        }

        currentAnimation = SortAnimator.createStep(steps.next(), elements, this::playNextStep);
        currentAnimation.rateProperty().bind(speedSlider.valueProperty());
        currentAnimation.play();
    }
}
