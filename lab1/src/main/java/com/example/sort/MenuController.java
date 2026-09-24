package com.example.sort;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class MenuController {

    @FXML
    private TextField countField;

    @FXML
    private TextField small_num;

    @FXML
    private TextField big_num;

    @FXML
    private TextField array;

    @FXML
    private Label myArray;

    private int[] inputArray;

    public void start(ActionEvent actionEvent) throws IOException {
        if (inputArray == null || inputArray.length == 0) {
            ErrorMessage.show("Введите массив");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("main_view.fxml"));
        Parent root = loader.load();

        MainController controller = loader.getController();
        controller.setArrayData(inputArray);

        javafx.scene.Node source = (javafx.scene.Node) actionEvent.getSource();
        source.getScene().setRoot(root);
    }

    public void generateArray(ActionEvent actionEvent) {
        try {
            int size = Integer.parseInt(countField.getText());
            int min = Integer.parseInt(small_num.getText());
            int max = Integer.parseInt(big_num.getText());

            if (size <= 0 || min > max) {
                ErrorMessage.show("Некорректный интервал или размер");
                return;
            }

            inputArray = new Random().ints(size, min, max + 1).toArray();
            myArray.setText(Arrays.toString(inputArray));

        } catch (NumberFormatException e) {
            ErrorMessage.show("Заполните все поля числами");
        }
    }

    public void clickOnInput(ActionEvent actionEvent) {
        try {
            inputArray = Arrays.stream(array.getText().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .mapToInt(Integer::parseInt)
                    .toArray();

            myArray.setText(Arrays.toString(inputArray));
        } catch (Exception e) {
            ErrorMessage.show("Массив некорректный, введите другой");
        }
    }
}
