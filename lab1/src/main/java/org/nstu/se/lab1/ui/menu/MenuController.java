package org.nstu.se.lab1.ui.menu;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

import org.nstu.se.lab1.AppContext;
import org.nstu.se.lab1.Messages;
import org.nstu.se.lab1.ResourcePaths;
import org.nstu.se.lab1.model.MenuState;
import org.nstu.se.lab1.model.SortArray;
import org.nstu.se.lab1.ui.animation.SortController;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class MenuController implements Initializable {
    private final AppContext context;

    @FXML private ToggleGroup modeGroup;
    @FXML private ToggleButton manualToggle;
    @FXML private ToggleButton generateToggle;

    @FXML private VBox manualPane;
    @FXML private VBox generatePane;

    @FXML private TextField array;
    @FXML private Label arrayError;

    @FXML private TextField countField;
    @FXML private Label countError;

    @FXML private TextField smallNum;
    @FXML private Label smallNumError;

    @FXML private TextField bigNum;
    @FXML private Label bigNumError;

    @FXML private Label myArray;
    @FXML private Button startButton;

    public MenuController(final AppContext context) {
        this.context = context;
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        setupToggleGroup();
        setupListeners();
        restoreState();
    }

    private void setupToggleGroup() {
        modeGroup
                .selectedToggleProperty()
                .addListener(
                        (_, oldSelection, newSelection) -> {
                            if (newSelection == null) {
                                modeGroup.selectToggle(oldSelection);
                            } else {
                                switchMode(newSelection.equals(manualToggle));
                            }
                        });
    }

    private void setupListeners() {
        array.textProperty().addListener((_, _, _) -> handleManualInput());
        countField.textProperty().addListener((_, _, _) -> handleGenerationInput());
        smallNum.textProperty().addListener((_, _, _) -> handleGenerationInput());
        bigNum.textProperty().addListener((_, _, _) -> handleGenerationInput());
    }

    private void restoreState() {
        final var state = context.retrieveMenuState();
        array.setText(state.manualInputText());
        countField.setText(state.countText());
        smallNum.setText(state.minText());
        bigNum.setText(state.maxText());

        if (state.manualMode()) {
            modeGroup.selectToggle(manualToggle);
        } else {
            modeGroup.selectToggle(generateToggle);
        }

        switchMode(state.manualMode());
    }

    public void start(final ActionEvent actionEvent) throws IOException {
        if (!context.hasValidModel()) {
            return;
        }

        final var currentState =
                new MenuState(
                        manualToggle.isSelected(),
                        array.getText(),
                        countField.getText(),
                        smallNum.getText(),
                        bigNum.getText());
        context.updateMenuState(currentState);

        final var loader =
                new FXMLLoader(
                        ResourcePaths.url(ResourcePaths.SORT_VIEW),
                        ResourceBundle.getBundle(Messages.BUNDLE_NAME));

        loader.setControllerFactory(_ -> new SortController(context));
        final Parent root = loader.load();

        final var source = (Node) actionEvent.getSource();
        source.getScene().setRoot(root);
    }

    private void handleManualInput() {
        final var values = ArrayInputParser.parseManual(array, arrayError);
        updateDisplayedArray(values);
    }

    private void handleGenerationInput() {
        final var values =
                ArrayInputParser.parseGeneration(
                        countField, countError, smallNum, smallNumError, bigNum, bigNumError);
        updateDisplayedArray(values);
    }

    private void updateDisplayedArray(final Integer... values) {
        if (values == null || values.length == 0) {
            context.updateModel(null);
            myArray.setText(Messages.get("menu.array.empty"));
            startButton.setDisable(true);
            return;
        }

        context.updateModel(SortArray.from(values));
        myArray.setText(Arrays.toString(values));
        startButton.setDisable(false);
    }

    private void switchMode(final boolean isManualMode) {
        manualPane.setVisible(isManualMode);
        manualPane.setManaged(isManualMode);

        generatePane.setVisible(!isManualMode);
        generatePane.setManaged(!isManualMode);

        if (isManualMode) {
            handleManualInput();
        } else {
            handleGenerationInput();
        }
    }
}
