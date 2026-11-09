package org.nstu.se.lab3.ui;

import javafx.animation.KeyFrame;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import org.jspecify.annotations.Nullable;
import org.nstu.se.lab3.model.DataAccumulator;
import org.nstu.se.lab3.model.HistoryEntry;
import org.nstu.se.lab3.model.SettingsValues;
import org.nstu.se.lab3.ui.history.HistoryManager;
import org.nstu.se.lab3.ui.indicator.LogarithmicLevelPane;
import org.nstu.se.lab3.ui.settings.SettingsController;

import java.net.URL;
import java.util.ResourceBundle;

@SuppressWarnings("NullAway.Init")
public final class MainController implements Initializable {
    private static final int HISTORY_UPDATE_RATE_MILLIS = 200;

    @FXML private SplitPane mainSplitPane;
    @FXML private VBox settingsPane;
    @FXML private SettingsController settingsFormController;
    @FXML private ToggleButton historyToggle;
    @FXML private Label historyArrow;
    @FXML private HBox levelContainer;
    @FXML private VBox historyPane;
    @FXML private Label historyTotalLabel;
    @FXML private ListView<HistoryEntry> historyList;

    private @Nullable LogarithmicLevelPane levelPane;
    private SettingsValues currentSettings;
    private HistoryManager historyManager;
    private DataAccumulator dataAccumulator;

    private final Timeline historyUpdater = new Timeline();

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        SplitPane.setResizableWithParent(settingsPane, Boolean.FALSE);

        historyManager = new HistoryManager(historyList, historyTotalLabel);

        settingsFormController.setOnSettingsChanged(this::applySettings);

        setupFocusBehavior();
        setupHistoryUpdater();
        setupHistoryToggle();
    }

    private void applySettings(final SettingsValues newSettings) {
        currentSettings = newSettings;

        if (dataAccumulator == null) {
            dataAccumulator = new DataAccumulator(newSettings.maxHistorySize());
        } else {
            dataAccumulator.setMaxSize(newSettings.maxHistorySize());
        }

        recreateLevelPane();
    }

    private void setupFocusBehavior() {
        Platform.runLater(() -> mainSplitPane.requestFocus());

        mainSplitPane.addEventFilter(
                KeyEvent.KEY_PRESSED,
                event -> {
                    if ((event.getCode() == KeyCode.ESCAPE || event.getCode() == KeyCode.ENTER)
                            && event.getTarget() instanceof TextField) {
                        mainSplitPane.requestFocus();
                        event.consume();
                    }
                });

        mainSplitPane.addEventFilter(
                MouseEvent.MOUSE_PRESSED,
                event -> {
                    if (!(event.getTarget() instanceof TextField)) {
                        mainSplitPane.requestFocus();
                    }
                });
    }

    private void setupHistoryUpdater() {
        final var frame =
                new KeyFrame(Duration.millis(HISTORY_UPDATE_RATE_MILLIS), _ -> updateHistory());
        historyUpdater.getKeyFrames().add(frame);
        historyUpdater.setCycleCount(Timeline.INDEFINITE);
    }

    private void setupHistoryToggle() {
        final var rotate = new RotateTransition(Duration.millis(200), historyArrow);
        rotate.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);

        historyToggle
                .selectedProperty()
                .addListener(
                        (_, _, isSelected) -> {
                            rotate.stop();
                            rotate.setFromAngle(historyArrow.getRotate());
                            rotate.setToAngle(isSelected ? 180 : 0);
                            rotate.play();

                            historyPane.setVisible(isSelected);
                            historyPane.setManaged(isSelected);

                            if (isSelected) {
                                updateHistory();
                                historyUpdater.play();
                            } else {
                                historyUpdater.stop();
                            }
                        });
    }

    private void recreateLevelPane() {
        if (levelPane != null) {
            levelPane.dispose();
            levelContainer.getChildren().remove(levelPane);
        }

        final var newLevelPane = new LogarithmicLevelPane(currentSettings, dataAccumulator, null);
        HBox.setHgrow(newLevelPane, Priority.ALWAYS);
        levelContainer.getChildren().add(newLevelPane);
        levelPane = newLevelPane;

        if (historyToggle.isSelected()) {
            updateHistory();
        }
    }

    private void updateHistory() {
        if (historyToggle.isSelected()) {
            historyManager.update(dataAccumulator.getAccumulatedData());
        }
    }
}
