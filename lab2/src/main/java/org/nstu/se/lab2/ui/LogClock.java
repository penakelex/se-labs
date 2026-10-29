package org.nstu.se.lab2.ui;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

final class LogClock {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final ZoneId ZONE = ZoneId.systemDefault();

    private LogClock() {}

    static String now() {
        return LocalTime.now(ZONE).format(FORMAT);
    }
}
