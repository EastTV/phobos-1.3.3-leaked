package me.earth.phobos.features.modules.render;

import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;

public class SmallShield extends Module {

    public Setting<Boolean> normalOffset = register(new Setting("OffNormal", false));
    public Setting<Float> offset = register(new Setting("Offset", 0.7f, 0.0f, 1.0f, v -> normalOffset.getValue()));
    public Setting<Float> offX = register(new Setting("OffX", 0.0f, -1.0f, 1.0f, v -> !normalOffset.getValue()));
    public Setting<Float> offY = register(new Setting("OffY", 0.0f, -1.0f, 1.0f, v -> !normalOffset.getValue()));
    public Setting<Float> mainX = register(new Setting("MainX", 0.0f, -1.0f, 1.0f));
    public Setting<Float> mainY = register(new Setting("MainY", 0.0f, -1.0f, 1.0f));

    private static SmallShield INSTANCE = new SmallShield();

    public SmallShield() {
        super("SmallShield", "Makes you offhand lower.", Category.RENDER, false, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        if(normalOffset.getValue()) {
            mc.entityRenderer.itemRenderer.equippedProgressOffHand = offset.getValue();
        }
    }

    public static SmallShield getINSTANCE() {
        if(INSTANCE == null) {
            INSTANCE = new SmallShield();
        }
        return INSTANCE;
    }
}
