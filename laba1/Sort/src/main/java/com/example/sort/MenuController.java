package com.example.sort;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
        if (inputArray == null ||inputArray.length==0)
        {
            ErrorMessage.show("Введите массив");
        }
        else {
            Main.setArray(inputArray);
            Main.setRoot("main_view");
        }
    }

    public void generateArray(ActionEvent actionEvent) {
        try {
            int size = Integer.parseInt(countField.getText());
            int interval_1 = Integer.parseInt(small_num.getText());
            int interval_2 = Integer.parseInt(big_num.getText());

            if (size <= 0 || interval_1 > interval_2) {
                ErrorMessage.show("Некорректный интервал или размер");
                return;
            }

            int[] array = new int[size];
            Random random = new Random();

            for (int i = 0; i < array.length; i++) {
                array[i] = random.nextInt(interval_1, interval_2 + 1);
            }

            inputArray = Arrays.copyOf(array, array.length);
            myArray.setText(strArray(inputArray));

        } catch (NumberFormatException e) {
            ErrorMessage.show("Заполните все поля числами");
        }
    }

    public void clickOnInput(ActionEvent actionEvent)
    {
        String str = array.getText();
        try {
            String buff = str.replaceAll("\\s+", "");
            String[] arr = buff.split(",");

            int[] result = Arrays.stream(arr)
                    .mapToInt(Integer::parseInt)
                    .toArray();
            inputArray=java.util.Arrays.copyOf(result, result.length);

            myArray.setText(strArray(inputArray));

        } catch (Exception e)
        {
            ErrorMessage.show("Массив некорректный, введите другой");
        }
    }

    private String strArray(int[] arr)
    {
        String result = Arrays.toString(arr);
        return result;
    }

}
