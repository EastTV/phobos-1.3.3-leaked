package me.earth.phobos.features.modules.player;

import me.earth.phobos.features.modules.Module;

public class MultiTask extends Module {

    private static MultiTask INSTANCE = new MultiTask();

    public MultiTask() {
        super("MultiTask", "Allows you to eat while mining.", Category.PLAYER, false, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static MultiTask getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new MultiTask();
        }
        return INSTANCE;
    }
}
