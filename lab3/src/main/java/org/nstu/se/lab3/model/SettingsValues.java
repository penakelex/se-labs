package org.nstu.se.lab3.model;

public record SettingsValues(
        int zeroLevelValue,
        double minDecibels,
        double maxDecibels,
        int periodMillis,
        int maxHistorySize) {}
