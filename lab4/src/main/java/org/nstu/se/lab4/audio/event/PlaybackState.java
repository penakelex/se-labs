package org.nstu.se.lab4.audio.event;

import org.nstu.se.lab4.audio.source.SignalSource;

public record PlaybackState(
        double positionSeconds,
        double durationSeconds,
        float currentSample,
        boolean playing,
        SignalSource mix) {}
