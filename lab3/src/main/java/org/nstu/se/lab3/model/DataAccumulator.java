package org.nstu.se.lab3.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class DataAccumulator {
    private final Deque<HistoryEntry> history = new ArrayDeque<>();
    private final Lock lock = new ReentrantLock();
    private int totalCount;
    private int statsCount;
    private int maxSize;
    private long sum;
    private int minValue = Integer.MAX_VALUE;
    private int maxValue = Integer.MIN_VALUE;

    public DataAccumulator(final int initialMaxSize) {
        this.maxSize = initialMaxSize;
        this.totalCount = 0;
    }

    public record Stats(int count, int min, int max, double average) {}

    public void setMaxSize(final int newMaxSize) {
        lock.lock();

        try {
            this.maxSize = newMaxSize;
            trimToSize();
        } finally {
            lock.unlock();
        }
    }

    public void addValue(final int value, final double decibels) {
        lock.lock();

        try {
            totalCount++;
            statsCount++;
            history.addLast(new HistoryEntry(totalCount, value, decibels));
            sum += value;
            minValue = Math.min(minValue, value);
            maxValue = Math.max(maxValue, value);
            trimToSize();
        } finally {
            lock.unlock();
        }
    }

    public Stats getStats() {
        lock.lock();

        try {
            if (statsCount == 0) {
                return new Stats(0, 0, 0, 0.0);
            }

            return new Stats(statsCount, minValue, maxValue, (double) sum / statsCount);
        } finally {
            lock.unlock();
        }
    }

    public List<HistoryEntry> getAccumulatedData() {
        lock.lock();

        try {
            return List.copyOf(history);
        } finally {
            lock.unlock();
        }
    }

    public void clear() {
        lock.lock();

        try {
            history.clear();
            statsCount = 0;
            sum = 0;
            minValue = Integer.MAX_VALUE;
            maxValue = Integer.MIN_VALUE;
        } finally {
            lock.unlock();
        }
    }

    private void trimToSize() {
        while (history.size() > maxSize) {
            history.removeFirst();
        }
    }
}
