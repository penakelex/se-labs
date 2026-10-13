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
import javafx.scene.layout.VBox;

import org.nstu.se.lab1.Messages;
import org.nstu.se.lab1.ResourcePaths;
import org.nstu.se.lab1.model.Session;
import org.nstu.se.lab1.model.SortArray;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Random;
import java.util.ResourceBundle;

public class MenuController implements Initializable {
    private static final int MAX_SIZE = 25;
    private static final int MAX_ABS_VALUE = 10_000;
    private static final String INVALID_STYLE = "invalid";

    @FXML private ToggleButton manualToggle;
    //    @FXML private ToggleButton generateToggle;

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

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        manualToggle.selectedProperty().addListener((_, _, selected) -> switchMode(selected));

        array.textProperty().addListener((_, _, _) -> parseManual());
        countField.textProperty().addListener((_, _, _) -> parseGeneration());
        smallNum.textProperty().addListener((_, _, _) -> parseGeneration());
        bigNum.textProperty().addListener((_, _, _) -> parseGeneration());

        switchMode(true);
    }

    public void start(final ActionEvent actionEvent) throws IOException {
        if (Session.getModel() == null || Session.getModel().size() == 0) {
            return;
        }

        final var loader =
                new FXMLLoader(ResourcePaths.url(ResourcePaths.SORT_VIEW), Messages.bundle());
        final Parent root = loader.load();

        final var source = (Node) actionEvent.getSource();
        source.getScene().setRoot(root);
    }

    private void switchMode(final boolean manual) {
        manualPane.setVisible(manual);
        manualPane.setManaged(manual);

        generatePane.setVisible(!manual);
        generatePane.setManaged(!manual);

        if (manual) {
            parseManual();
        } else {
            parseGeneration();
        }
    }

    private void parseManual() {
        final var raw = array.getText();
        final var tokens =
                Arrays.stream(raw.split("[,\\s]+"))
                        .map(String::trim)
                        .filter(token -> !token.isEmpty())
                        .toList();

        if (tokens.isEmpty()) {
            clearError(array, arrayError);
            updateDisplayedArray();
            return;
        }

        if (tokens.size() > MAX_SIZE) {
            markError(array, arrayError, Messages.format("menu.error.size.limit", MAX_SIZE));
            updateDisplayedArray();
            return;
        }

        try {
            final var parsed = tokens.stream().map(Integer::parseInt).toArray(Integer[]::new);

            clearError(array, arrayError);
            updateDisplayedArray(parsed);
        } catch (final NumberFormatException exception) {
            markError(array, arrayError, Messages.get("menu.error.manual.invalid"));
            updateDisplayedArray();
        }
    }

    private void parseGeneration() {
        final var count = validateCount();
        final var min = tryParse(smallNum, smallNumError, "menu.error.from.integer");
        final var max = tryParse(bigNum, bigNumError, "menu.error.to.integer");

        if (count == null || min == null || max == null) {
            updateDisplayedArray();
            return;
        }

        if (min > max) {
            markError(smallNum, smallNumError, Messages.get("menu.error.range.order"));
            updateDisplayedArray();
            return;
        }

        if (Math.abs(min) > MAX_ABS_VALUE || Math.abs(max) > MAX_ABS_VALUE) {
            markError(
                    bigNum,
                    bigNumError,
                    Messages.format("menu.error.range.magnitude", MAX_ABS_VALUE));
            updateDisplayedArray();
            return;
        }

        final var generated =
                new Random().ints(count, min, max + 1).boxed().toArray(Integer[]::new);

        updateDisplayedArray(generated);
    }

    private Integer validateCount() {
        final var value = tryParse(countField, countError, "menu.error.count.integer");

        if (value == null) {
            return null;
        }

        if (value <= 0) {
            markError(countField, countError, Messages.get("menu.error.count.positive"));
            return null;
        }

        if (value > MAX_SIZE) {
            markError(countField, countError, Messages.format("menu.error.size.limit", MAX_SIZE));
            return null;
        }

        clearError(countField, countError);
        return value;
    }

    private Integer tryParse(final TextField field, final Label error, final String messageKey) {
        final var text = field.getText();

        if (text.isBlank()) {
            clearError(field, error);
            return null;
        }

        try {
            final var parsed = Integer.parseInt(text);
            clearError(field, error);
            return parsed;
        } catch (final NumberFormatException exception) {
            markError(field, error, Messages.get(messageKey));
            return null;
        }
    }

    private void updateDisplayedArray(final Integer... values) {
        if (values == null || values.length == 0) {
            Session.setModel(null);
            myArray.setText("");
            startButton.setDisable(true);
            return;
        }

        Session.setModel(SortArray.from(values));
        myArray.setText(Arrays.toString(values));
        startButton.setDisable(false);
    }

    private static void markError(final TextField field, final Label error, final String message) {
        if (!field.getStyleClass().contains(INVALID_STYLE)) {
            field.getStyleClass().add(INVALID_STYLE);
        }

        error.setText(message);
        error.setVisible(true);
        error.setManaged(true);
    }

    private static void clearError(final TextField field, final Label error) {
        field.getStyleClass().remove(INVALID_STYLE);
        error.setVisible(false);
        error.setManaged(false);
    }
}
