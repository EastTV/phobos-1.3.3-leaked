package me.earth.phobos.features.modules.player;

import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;

public class Reach extends Module {

    public Setting<Boolean> override = register(new Setting("Override", false));
    public Setting<Float> add = register(new Setting("Add", 3.0f, v -> !override.getValue()));
    public Setting<Float> reach = register(new Setting("Reach", 6.0f, v -> override.getValue()));

    private static Reach INSTANCE = new Reach();

    public Reach() {
        super("Reach", "Extends your block reach", Module.Category.PLAYER, true, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static Reach getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Reach();
        }
        return INSTANCE;
    }

    @Override
    public String getDisplayInfo() {
        return override.getValue() ? reach.getValue().toString() : add.getValue().toString();
    }
}
