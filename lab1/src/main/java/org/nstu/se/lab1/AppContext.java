package org.nstu.se.lab1;

import org.jspecify.annotations.Nullable;
import org.nstu.se.lab1.model.MenuState;
import org.nstu.se.lab1.model.SortArray;

public final class AppContext {
    private @Nullable SortArray<Integer> model;
    private MenuState menuState = new MenuState(true, "", "", "", "");

    public boolean hasValidModel() {
        return model != null && model.size() > 0;
    }

    public @Nullable SortArray<Integer> retrieveModel() {
        return model;
    }

    public void updateModel(@Nullable final SortArray<Integer> newModel) {
        this.model = newModel;
    }

    public MenuState retrieveMenuState() {
        return menuState;
    }

    public void updateMenuState(final MenuState newMenuState) {
        this.menuState = newMenuState;
    }
}
