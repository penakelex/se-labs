package com.example.model;

public class LogScaleModel {

    private final int v0;
    private final double minDb;
    private final double maxDb;

    public LogScaleModel(int v0, double minDb, double maxDb) {
        if (v0 <= 0) {
            throw new IllegalArgumentException("V0 должно быть строго больше 0");
        }
        this.v0 = v0;
        this.minDb = minDb;
        this.maxDb = maxDb;
    }

    public double calculateDb(int v) {
        if (v <= 0) {
            return minDb;
        }
        double db = 20.0 * Math.log10((double) v / v0);
        return Math.max(minDb, Math.min(maxDb, db));
    }

    public int calculateValueFromDb(double db) {
        double clampedDb = Math.max(minDb, Math.min(maxDb, db));
        double vDouble = v0 * Math.pow(10.0, clampedDb / 20.0);
        return (int) Math.round(vDouble);
    }

    public int getV0() {
        return v0;
    }

    public double getMinDb() {
        return minDb;
    }

    public double getMaxDb() {
        return maxDb;
    }
}
