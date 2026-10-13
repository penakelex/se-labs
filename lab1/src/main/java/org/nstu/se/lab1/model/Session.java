package org.nstu.se.lab1.model;

public final class Session {
    private static SortArray<Integer> model;

    private Session() {}

    public static SortArray<Integer> getModel() {
        return model;
    }

    public static void setModel(final SortArray<Integer> newModel) {
        model = newModel;
    }
}
