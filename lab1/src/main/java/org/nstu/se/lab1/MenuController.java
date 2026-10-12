package org.nstu.se.lab1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class MenuController {
    @FXML private TextField countField;

    @FXML private TextField smallNum;

    @FXML private TextField bigNum;

    @FXML private TextField array;

    @FXML private Label myArray;

    private int[] inputArray;

    public void start(final ActionEvent actionEvent) throws IOException {
        if (inputArray == null || inputArray.length == 0) {
            ErrorMessage.show("Введите массив");
            return;
        }

        final var loader = new FXMLLoader(getClass().getResource("main_view.fxml"));
        final Parent root = loader.load();
        final MainController controller = loader.getController();
        controller.setArrayData(inputArray);

        final var source = (Node) actionEvent.getSource();
        source.getScene().setRoot(root);
    }

    public void generateArray() {
        try {
            final var size = Integer.parseInt(countField.getText());
            final var min = Integer.parseInt(smallNum.getText());
            final var max = Integer.parseInt(bigNum.getText());

            if (size <= 0 || min > max) {
                ErrorMessage.show("Некорректный интервал или размер");
                return;
            }

            inputArray = new Random().ints(size, min, max + 1).toArray();
            myArray.setText(Arrays.toString(inputArray));

        } catch (final NumberFormatException e) {
            ErrorMessage.show("Заполните все поля числами");
        }
    }

    public void clickOnInput() {
        try {
            inputArray =
                    Arrays.stream(array.getText().split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .mapToInt(Integer::parseInt)
                            .toArray();

            myArray.setText(Arrays.toString(inputArray));
        } catch (final NumberFormatException e) {
            ErrorMessage.show("Массив некорректный, введите другой");
        }
    }
}
