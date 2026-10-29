package org.nstu.se.lab2.ui;

import javafx.scene.control.ListCell;

import org.nstu.se.lab2.services.ServiceEntry;

public final class ServiceEntryCell extends ListCell<ServiceEntry> {
    @Override
    protected void updateItem(final ServiceEntry item, final boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : item.getDisplayName());
    }
}
