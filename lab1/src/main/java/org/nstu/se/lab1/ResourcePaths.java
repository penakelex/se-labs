package org.nstu.se.lab1;

import java.net.URL;
import java.util.Objects;

public final class ResourcePaths {
    public static final String MENU_VIEW = "/org/nstu/se/lab1/menu_view.fxml";
    public static final String SORT_VIEW = "/org/nstu/se/lab1/sort_view.fxml";
    public static final String APP_CSS = "/org/nstu/se/lab1/css/app.css";

    private ResourcePaths() {}

    public static URL url(final String path) {
        return Objects.requireNonNull(
                ResourcePaths.class.getResource(path), () -> "Resource not found: " + path);
    }
}
