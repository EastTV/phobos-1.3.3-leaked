package me.earth.phobos.features.modules.render;

import me.earth.phobos.features.modules.Module;

public class Chams extends Module {

    private static Chams INSTANCE = new Chams();

    public Chams() {
        super("Chams", "Renders players through walls.", Category.RENDER, false, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static Chams getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Chams();
        }
        return INSTANCE;
    }
}
