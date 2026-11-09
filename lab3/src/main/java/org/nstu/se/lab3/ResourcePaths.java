package org.nstu.se.lab3;

import java.net.URL;
import java.util.Objects;

public final class ResourcePaths {
    public static final String MAIN_VIEW = "/org/nstu/se/lab3/main_view.fxml";
    public static final String APP_CSS = "/org/nstu/se/lab3/css/app.css";

    private ResourcePaths() {}

    public static URL url(final String path) {
        return Objects.requireNonNull(
                ResourcePaths.class.getResource(path), () -> "Resource not found: " + path);
    }
}
