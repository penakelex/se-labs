package org.nstu.se.lab2.ui;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import org.jspecify.annotations.Nullable;
import org.nstu.se.lab2.Messages;
import org.nstu.se.lab2.core.RxService;
import org.nstu.se.lab2.services.ServiceCatalog;
import org.nstu.se.lab2.services.ServiceEntry;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@SuppressWarnings("NullAway.Init")
public class MainController implements Initializable {
    private static final String STYLE_ERROR = "error";
    private static final String STYLE_INVALID = "invalid";
    private static final String STATUS_ERROR_KEY = "rx.status.error";
    private static final String LOG_ERROR_KEY = "rx.log.error";

    @FXML private ComboBox<ServiceEntry> serviceSelector;
    @FXML private TableView<MethodInfo> methodsTable;
    @FXML private TableColumn<MethodInfo, String> methodNameColumn;
    @FXML private TableColumn<MethodInfo, String> methodInfoColumn;
    @FXML private TableColumn<MethodInfo, String> methodParamsColumn;
    @FXML private TextField requestArea;
    @FXML private TextArea logArea;
    @FXML private Button sendButton;
    @FXML private ProgressIndicator progress;
    @FXML private Label statusLabel;

    private final ServiceSwitcher serviceSwitcher = new ServiceSwitcher(this::logError);

    private @Nullable RxService service;
    private Future<Object> pending = CompletableFuture.completedFuture(new Object());

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        statusLabel.setText(Messages.get("rx.status.ready"));

        serviceSelector.setItems(FXCollections.observableArrayList(ServiceCatalog.entries()));
        serviceSelector.setCellFactory(_ -> new ServiceEntryCell());
        serviceSelector.setButtonCell(new ServiceEntryCell());

        methodNameColumn.setCellValueFactory(
                data -> new ReadOnlyStringWrapper(data.getValue().name()));
        methodInfoColumn.setCellValueFactory(
                data -> new ReadOnlyStringWrapper(data.getValue().info()));
        methodParamsColumn.setCellValueFactory(
                data -> new ReadOnlyStringWrapper(data.getValue().parameters()));

        methodsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        logArea.setWrapText(true);
        progress.setVisible(false);
        progress.managedProperty().bind(progress.visibleProperty());

        requestArea.textProperty().addListener((_, _, _) -> clearErrorState());
        methodsTable
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (_, _, info) -> {
                            if (info != null) {
                                requestArea.setText(info.exampleRequest());
                            }
                        });

        serviceSelector
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((_, _, entry) -> switchService(entry));
        serviceSelector.getSelectionModel().selectFirst();
    }

    private void switchService(@Nullable final ServiceEntry entry) {
        if (entry == null) {
            return;
        }

        final var result = serviceSwitcher.switchTo(entry);
        if (result == null) {
            statusLabel.setText(Messages.get(STATUS_ERROR_KEY));
            statusLabel.getStyleClass().add(STYLE_ERROR);
            return;
        }

        if (!pending.isDone()) {
            pending.cancel(true);
        }
        if (service != null) {
            service.shutdown();
        }
        service = result.service();

        methodsTable.setItems(FXCollections.observableArrayList(result.methods()));
        methodsTable.getSelectionModel().selectFirst();
        statusLabel.setText(Messages.format("rx.status.service", entry.getDisplayName()));
        clearErrorState();
    }

    private void clearErrorState() {
        statusLabel.getStyleClass().remove(STYLE_ERROR);
        requestArea.getStyleClass().remove(STYLE_INVALID);
    }

    @FXML
    public void sendRequest() {
        final var currentService = service;
        if (currentService == null) {
            appendLog(Messages.get("rx.log.noService"));
            return;
        }

        final var request = requestArea.getText().trim();
        if (request.isEmpty()) {
            appendLog(Messages.get("rx.log.enterRequest"));
            return;
        }

        clearErrorState();
        setBusy(true);
        statusLabel.setText(Messages.get("rx.status.running"));

        pending =
                CompletableFuture.supplyAsync(
                                () -> CommandExecutor.execute(currentService, request))
                        .whenComplete(
                                (result, error) ->
                                        Platform.runLater(() -> onCompleted(result, error)));
    }

    private void setBusy(final boolean busy) {
        sendButton.setDisable(busy);
        serviceSelector.setDisable(busy);
        progress.setVisible(busy);
    }

    private void onCompleted(final Object result, final Throwable error) {
        setBusy(false);

        if (error != null) {
            final var cause = CommandExecutor.unwrap(error);
            statusLabel.setText(Messages.get(STATUS_ERROR_KEY));
            statusLabel.getStyleClass().add(STYLE_ERROR);
            requestArea.getStyleClass().add(STYLE_INVALID);
            logError(cause);
        } else {
            statusLabel.setText(Messages.get("rx.status.done"));
            clearErrorState();
            final var display = result == null ? Messages.get("rx.result.void") : result.toString();
            appendLog(Messages.format("rx.log.result", display));
        }
    }

    private void logError(final Throwable cause) {
        appendLog(Messages.format(LOG_ERROR_KEY, CommandExecutor.errorMessage(cause)));
    }

    private void appendLog(final String message) {
        logArea.appendText(Messages.format("rx.log.line", LogClock.now(), message) + "\n");
    }
}
