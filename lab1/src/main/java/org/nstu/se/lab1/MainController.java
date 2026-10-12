package org.nstu.se.lab1;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    // Константы отрисовки и масштабирования
    private static final double DEFAULT_PANE_WIDTH = 660.0;
    private static final double DEFAULT_PANE_HEIGHT = 400.0;

    private static final double BASE_WIDTH = 50.0;
    private static final double BASE_HEIGHT = 40.0;
    private static final double BASE_GAP = 10.0;
    private static final double BASE_FONT_SIZE = 16.0;

    private static final double MARGIN = 20.0;
    private static final double MIN_SCALE = 0.25;
    private static final double MAX_SCALE = 1.2;
    private static final double MIN_FONT_SIZE = 8.0;

    @FXML private Pane animationPane;

    @FXML private Slider speedSlider;

    private SortElement[] elements;
    private int[] values;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        // Подписываемся на изменение ширины: рисуем массив, только если есть данные
        animationPane
                .widthProperty()
                .addListener(
                        (_, _, newValue) -> {
                            if (newValue.doubleValue() > 0
                                    && animationPane.getChildren().isEmpty()) {
                                showInitialArray();
                            }
                        });

        showInitialArray();
    }

    public void setArrayData(final int... inputArray) {
        this.values = inputArray;
        showInitialArray();
    }

    private void showInitialArray() {
        if (values != null && values.length > 0 && animationPane.getWidth() > 0) {
            createArray(values);
        }
    }

    @FXML
    public void startSorting() {
        // Логика запуска сортировки
    }

    @FXML
    public void pauseSorting() {
        // Пауза
    }

    @FXML
    public void resumeSorting() {
        // Возобновление
    }

    @FXML
    public void comeBack() throws IOException {
        final var loader = new FXMLLoader(getClass().getResource("menu_view.fxml"));
        animationPane.getScene().setRoot(loader.load());
    }

    private void createArray(final int... values) {
        animationPane.getChildren().clear();

        final var count = values.length;

        if (count == 0) {
            return;
        }

        elements = new SortElement[count];

        // Получаем текущие размеры панели или дефолтные значения
        final double paneWidth =
                animationPane.getWidth() > 0 ? animationPane.getWidth() : DEFAULT_PANE_WIDTH;
        final double paneHeight =
                animationPane.getHeight() > 0 ? animationPane.getHeight() : DEFAULT_PANE_HEIGHT;

        final double availableWidth = paneWidth - 2 * MARGIN;
        final double requiredWidth = count * BASE_WIDTH + (count - 1) * BASE_GAP;

        // Вычисляем и ограничиваем коэффициент масштабирования
        final double scale =
                Math.clamp(
                        requiredWidth > availableWidth ? availableWidth / requiredWidth : 1.0,
                        MIN_SCALE,
                        MAX_SCALE);

        final double scaledWidth = BASE_WIDTH * scale;
        final double scaledHeight = BASE_HEIGHT * scale;
        double scaledGap = BASE_GAP * scale;
        final double scaledFontSize = Math.max(MIN_FONT_SIZE, BASE_FONT_SIZE * scale);

        double totalWidth = count * scaledWidth + (count - 1) * scaledGap;

        // Уплотняем межэлементный интервал, если не помещаемся по ширине
        if (totalWidth > availableWidth && count > 1) {
            scaledGap = Math.max(1.0, (availableWidth - count * scaledWidth) / (count - 1));
            totalWidth = count * scaledWidth + (count - 1) * scaledGap;
        }

        // Центрирование
        final double startX = (paneWidth - totalWidth) / 2.0;
        final double startY = (paneHeight - scaledHeight) / 2.0;

        // Создаем и позиционируем элементы
        for (var i = 0; i < count; i++) {
            final var element = new SortElement(values[i]);
            elements[i] = element;

            element.setMinSize(scaledWidth, scaledHeight);
            element.setPrefSize(scaledWidth, scaledHeight);
            element.setMaxSize(scaledWidth, scaledHeight);

            element.setCustomFontSize(scaledFontSize);

            element.setLayoutX(startX + i * (scaledWidth + scaledGap));
            element.setLayoutY(startY);

            animationPane.getChildren().add(element);
        }
    }
}
