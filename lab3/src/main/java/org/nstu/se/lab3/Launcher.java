package org.nstu.se.lab3;

public final class Launcher {
    private Launcher() {}

    static void main(final String... args) {
        LogLevelApplication.launch(LogLevelApplication.class, args);
    }
}
