package org.nstu.se.lab3;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public final class Messages {
    public static final String BUNDLE_NAME = "org.nstu.se.lab3.app";

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private Messages() {}

    public static String get(final String key) {
        return BUNDLE.getString(key);
    }

    public static int getInt(final String key) {
        return Integer.parseInt(BUNDLE.getString(key));
    }

    public static double getDouble(final String key) {
        return Double.parseDouble(BUNDLE.getString(key));
    }

    public static String format(final String key, final Object... arguments) {
        return MessageFormat.format(BUNDLE.getString(key), arguments);
    }
}
