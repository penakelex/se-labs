package org.nstu.se.lab1;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public final class Messages {
    private static final ResourceBundle BUNDLE =
            ResourceBundle.getBundle("org.nstu.se.lab1.app", Locale.getDefault());

    private Messages() {}

    public static ResourceBundle bundle() {
        return BUNDLE;
    }

    public static String get(final String key) {
        return BUNDLE.getString(key);
    }

    public static String format(final String key, final Object... arguments) {
        return MessageFormat.format(BUNDLE.getString(key), arguments);
    }
}
