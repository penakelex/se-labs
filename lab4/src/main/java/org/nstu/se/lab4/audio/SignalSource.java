package org.nstu.se.lab4.audio;

public interface SignalSource {
    float getSample(int index);

    int getSampleCount();

    int getSampleRate();
}
