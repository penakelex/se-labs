package com.example;

import com.example.ui.LogarithmicLevelPane;
import com.example.ui.SettingsPane;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class Main extends Application {

    private LogarithmicLevelPane levelControlPane;
    private HBox root;

    // Параметры по умолчанию
    private int currentV0 = 100;
    private double currentMinDb = -72.0;
    private double currentMaxDb = 15.0;
    private int currentPeriod = 1000;

    @Override
    public void start(Stage primaryStage) {
        //  Создаем панель настроек
        SettingsPane settingsPane = new SettingsPane(
            currentV0, currentMinDb, currentMaxDb, currentPeriod
        );
        settingsPane.setPrefWidth(180);
        settingsPane.setMaxWidth(180);

        //  Создаем основной компонент
        levelControlPane = new LogarithmicLevelPane(
            currentV0,
            currentMinDb,
            currentMaxDb,
            currentPeriod,
            value -> System.out.println("[Callback] Текущая позиция V: " + value)
        );

        // обработка кнопки применить
        settingsPane.getApplyButton().setOnAction(e -> {
            if (settingsPane.validateInputs()) {
                // Считываем обновленные данные из полей ввода
                currentV0 = settingsPane.getV0();
                currentMinDb = settingsPane.getMinDb();
                currentMaxDb = settingsPane.getMaxDb();
                currentPeriod = settingsPane.getPeriod();

                // Останавливаем и удаляем старую панель
                root.getChildren().remove(levelControlPane);
                levelControlPane.dispose();

                // Создаем новый компонент с новыми параметрами
                levelControlPane = new LogarithmicLevelPane(
                    currentV0,
                    currentMinDb,
                    currentMaxDb,
                    currentPeriod,
                    value -> System.out.println("[Callback] Текущая позиция V: " + value)
                );
                HBox.setHgrow(levelControlPane, Priority.ALWAYS);

                // Вставляем обновленную панель в интерфейс
                root.getChildren().add(levelControlPane);

                System.out.println("Параметры успешно обновлены: V0=" + currentV0 +
                    ", dB=[" + currentMinDb + ", " + currentMaxDb + "], период=" + currentPeriod + "мс");
            }
        });

        // обработка кнопки история данных
        settingsPane.getHistoryButton().setOnAction(e -> {
            List<Integer> historyData = levelControlPane.getAccumulatedData();

            Stage historyStage = new Stage();
            historyStage.initOwner(primaryStage);
            historyStage.initModality(Modality.APPLICATION_MODAL);
            historyStage.setTitle("История накопленных данных");

            ListView<String> listView = new ListView<>();
            for (int i = 0; i < historyData.size(); i++) {
                int val = historyData.get(i);
                listView.getItems().add(String.format("[%d] Значение V: %d", i + 1, val));
            }

            VBox historyRoot = new VBox(10, new Label("Всего записей: " + historyData.size()), listView);
            historyRoot.setPadding(new Insets(15));
            historyRoot.setAlignment(Pos.CENTER);
            historyRoot.setStyle("-fx-background-color: #d4d0c8; -fx-font-family: 'Segoe UI', sans-serif;");

            Scene historyScene = new Scene(historyRoot, 280, 320);
            historyStage.setScene(historyScene);
            historyStage.showAndWait();
        });

        // Компоновка сцены
        root = new HBox(10);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-padding: 15; -fx-background-color: #b8b4a8;");
        root.getChildren().addAll(settingsPane, levelControlPane);
        HBox.setHgrow(levelControlPane, Priority.ALWAYS);

        Scene scene = new Scene(root, 500, 350);
        primaryStage.setTitle("lab3");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            if (levelControlPane != null) {
                levelControlPane.dispose();
            }
        });
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
