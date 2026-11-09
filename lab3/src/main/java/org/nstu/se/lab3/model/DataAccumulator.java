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
    private int maxSize;

    public DataAccumulator(final int initialMaxSize) {
        this.maxSize = initialMaxSize;
        this.totalCount = 0;
    }

    public void setMaxSize(final int newMaxSize) {
        lock.lock();

        try {
            this.maxSize = newMaxSize;
            trimToSize();
        } finally {
            lock.unlock();
        }
    }

    public void addValue(final int value) {
        lock.lock();

        try {
            totalCount++;
            history.addLast(new HistoryEntry(totalCount, value));
            trimToSize();
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
