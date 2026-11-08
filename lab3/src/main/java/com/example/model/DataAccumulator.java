package com.example.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataAccumulator {

    private static final int MAX_HISTORY_SIZE = 1000;
    private final List<Integer> history = Collections.synchronizedList(new ArrayList<>());

    public void addValue(int value) {
        synchronized (history) {
            if (history.size() >= MAX_HISTORY_SIZE) {
                history.remove(0); // Удаляем самое старое значение
            }
            history.add(value);
        }
    }

    public List<Integer> getAccumulatedData() {
        synchronized (history) {
            return new ArrayList<>(history);
        }
    }

    public void clear() {
        history.clear();
    }
}
