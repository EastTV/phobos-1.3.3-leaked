package me.earth.phobos.features.modules.client;

import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.ClientEvent;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.gui.PhobosGui;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.TextUtil;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClickGui extends Module {

    public Setting<String> prefix = register(new Setting("Prefix", "."));
    public Setting<Integer> red = register(new Setting("Red", 255, 0, 255));
    public Setting<Integer> green = register(new Setting("Green", 0, 0, 255));
    public Setting<Integer> blue = register(new Setting("Blue", 0, 0, 255));
    public Setting<Integer> hoverAlpha = register(new Setting("Alpha", 180, 0, 255));
    public Setting<Integer> alpha = register(new Setting("HoverAlpha", 240, 0, 255));
    public Setting<Boolean> customFov = register(new Setting("CustomFov", false));
    public Setting<Float> fov = register(new Setting("Fov", 150.0f, -180.0f, 180.0f, v -> customFov.getValue()));
    public Setting<String> moduleButton = register(new Setting("Buttons", ""));
    public Setting<Boolean> devSettings = register(new Setting("DevSettings", false));
    public Setting<Integer> topRed = register(new Setting("TopRed", 255, 0, 255, v -> devSettings.getValue()));
    public Setting<Integer> topGreen = register(new Setting("TopGreen", 0, 0, 255, v -> devSettings.getValue()));
    public Setting<Integer> topBlue = register(new Setting("TopBlue", 0, 0, 255, v -> devSettings.getValue()));
    public Setting<Integer> topAlpha = register(new Setting("TopAlpha", 255, 0, 255, v -> devSettings.getValue()));

    private static ClickGui INSTANCE = new ClickGui();

    public ClickGui() {
        super("ClickGui", "Opens the ClickGui", Category.CLIENT, true, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static ClickGui getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new ClickGui();
        }
        return INSTANCE;
    }

    @Override
    public void onUpdate() {
        if(customFov.getValue()) {
            mc.gameSettings.setOptionFloatValue(GameSettings.Options.FOV, fov.getValue());
        }
    }

    @SubscribeEvent
    public void onSettingChange(ClientEvent event) {
        if(event.getStage() == 2) {
            if(event.getSetting().getFeature().equals(this)) {
                if(event.getSetting().equals(this.prefix)) {
                    Phobos.commandManager.setPrefix(this.prefix.getPlannedValue());
                    Command.sendMessage("Prefix set to " + TextUtil.GREEN + Phobos.commandManager.getPrefix());
                }
                Phobos.colorManager.setColor(red.getPlannedValue(), green.getPlannedValue(), blue.getPlannedValue(), hoverAlpha.getPlannedValue());
            }
        }
    }

    @Override
    public void onEnable() {
        mc.displayGuiScreen(new PhobosGui());
    }

    @Override
    public void onLoad() {
        Phobos.colorManager.setColor(red.getValue(), green.getValue(), blue.getValue(), hoverAlpha.getValue());
        Phobos.commandManager.setPrefix(this.prefix.getValue());
    }

    @Override
    public void onTick() {
        if(!(mc.currentScreen instanceof PhobosGui)) {
            this.disable();
        }
    }

    @Override
    public void onDisable() {
        if(mc.currentScreen instanceof PhobosGui) {
            mc.displayGuiScreen(null);
        }
    }
}
