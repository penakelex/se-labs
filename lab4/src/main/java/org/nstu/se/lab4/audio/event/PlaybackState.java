package org.nstu.se.lab4.audio.event;

public record PlaybackState(
        double positionSeconds, double durationSeconds, float currentSample, boolean playing) {}
