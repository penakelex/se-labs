package org.nstu.se.lab3.ui.history;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;

import org.nstu.se.lab3.Messages;
import org.nstu.se.lab3.model.HistoryEntry;

import java.util.List;

public final class HistoryManager {
    private static final String KEY_HISTORY_TOTAL = "history.total";
    private static final String KEY_HISTORY_ITEM = "history.item";

    private final ListView<HistoryEntry> historyList;
    private final Label historyTotalLabel;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "JavaFX widgets are intentionally shared with the controller")
    public HistoryManager(final ListView<HistoryEntry> historyList, final Label historyTotalLabel) {
        this.historyList = historyList;
        this.historyTotalLabel = historyTotalLabel;
        setupCellFactory();
    }

    private void setupCellFactory() {
        historyList.setCellFactory(
                _ ->
                        new ListCell<>() {
                            @Override
                            protected void updateItem(
                                    final HistoryEntry item, final boolean empty) {
                                super.updateItem(item, empty);

                                if (empty || item == null) {
                                    setText(null);
                                } else {
                                    setText(
                                            Messages.format(
                                                    KEY_HISTORY_ITEM,
                                                    item.identifier(),
                                                    item.value()));
                                }
                            }
                        });
    }

    public void update(final List<HistoryEntry> historyData) {
        historyTotalLabel.setText(Messages.format(KEY_HISTORY_TOTAL, historyData.size()));

        final var isAtBottom = isAtBottom();

        if (isChanged(historyData)) {
            historyList.getItems().setAll(historyData);

            if (isAtBottom && !historyData.isEmpty()) {
                historyList.scrollTo(historyData.size() - 1);
            }
        }
    }

    private boolean isChanged(final List<HistoryEntry> newData) {
        final var currentItems = historyList.getItems();

        if (currentItems.size() != newData.size()) {
            return true;
        }

        if (newData.isEmpty()) {
            return false;
        }

        final var currentLast = currentItems.getLast();
        final var newLast = newData.getLast();
        return currentLast.identifier() != newLast.identifier();
    }

    private boolean isAtBottom() {
        for (final var node : historyList.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar scrollBar
                    && scrollBar.getOrientation() == Orientation.VERTICAL) {
                return scrollBar.getValue() >= scrollBar.getMax() - 0.01 || scrollBar.getMax() == 0;
            }
        }

        return true;
    }
}
