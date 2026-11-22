package org.nstu.se.lab4.audio.source;

public interface SignalSource {
    float getSample(int index);

    int getSampleCount();

    int getSampleRate();
}
