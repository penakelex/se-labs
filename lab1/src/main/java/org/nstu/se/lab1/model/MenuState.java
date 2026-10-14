package org.nstu.se.lab1.model;

public record MenuState(
        boolean manualMode,
        String manualInputText,
        String countText,
        String minText,
        String maxText) {}
