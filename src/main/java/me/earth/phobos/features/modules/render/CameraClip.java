package me.earth.phobos.features.modules.render;

import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;

public class CameraClip extends Module {

    public Setting<Boolean> extend = register(new Setting("Extend", false));
    public Setting<Double> distance = register(new Setting("Distance", 10.0, 0.0, 50.0, v -> extend.getValue(), "By how much you want to extend the distance."));
    private static CameraClip INSTANCE = new CameraClip();

    public CameraClip() {
        super("CameraClip", "Makes your Camera clip.", Category.RENDER, false, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static CameraClip getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new CameraClip();
        }
        return INSTANCE;
    }
}
