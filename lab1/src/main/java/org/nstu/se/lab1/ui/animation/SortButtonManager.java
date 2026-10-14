package org.nstu.se.lab1.ui.animation;

import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

import org.nstu.se.lab1.Messages;

public final class SortButtonManager {
    private static final String KEY_PLAY = "sort.button.play";
    private static final String KEY_TOOLTIP_START = "sort.tooltip.start";
    private static final String KEY_TOOLTIP_PAUSE = "sort.tooltip.pause";
    private static final String KEY_TOOLTIP_RESUME = "sort.tooltip.resume";

    private SortButtonManager() {}

    public static void collapseWhenDisabled(final Button button) {
        button.visibleProperty().bind(button.disableProperty().not());
        button.managedProperty().bind(button.visibleProperty());
    }

    public static void resetPlayPauseButton(final Button button) {
        button.setText(Messages.get(KEY_PLAY));
        button.setTooltip(new Tooltip(Messages.get(KEY_TOOLTIP_START)));
    }

    public static void setPauseButton(final Button button) {
        button.setText(Messages.get("sort.button.pause"));
        button.setTooltip(new Tooltip(Messages.get(KEY_TOOLTIP_PAUSE)));
    }

    public static void setResumeButton(final Button button) {
        button.setText(Messages.get(KEY_PLAY));
        button.setTooltip(new Tooltip(Messages.get(KEY_TOOLTIP_RESUME)));
    }
}
