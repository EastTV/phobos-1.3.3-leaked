package me.earth.phobos.features.modules.render;

import me.earth.phobos.event.events.ClientEvent;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import net.minecraft.block.Block;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class XRay extends Module {

    //public Setting<Integer> opacity = register(new Setting("Opacity", 150, 0, 255));
    public Setting<String> newBlock = register(new Setting("NewBlock", "Add Block..."));
    //public Setting<String> removeBlock = register(new Setting("RemoveBlock", "Remove Block..."));
    public Setting<Boolean> showBlocks = register(new Setting("ShowBlocks", false));

    private static XRay INSTANCE = new XRay();

    public XRay() {
        super("XRay", "Lets you look through walls.", Category.RENDER, false, false, true);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static XRay getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new XRay();
        }
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        mc.renderGlobal.loadRenderers();
    }

    @Override
    public void onDisable() {
        mc.renderGlobal.loadRenderers();
    }

    @SubscribeEvent
    public void onSettingChange(ClientEvent event) {
        if(event.getStage() == 2) {
            if(event.getSetting() != null && event.getSetting().getFeature() != null && event.getSetting().getFeature().equals(this)) {
                if(event.getSetting().equals(this.newBlock) && !shouldRender(newBlock.getPlannedValue())) {
                    this.register(new Setting(this.newBlock.getPlannedValue(), true, v -> showBlocks.getValue()));
                    Command.sendMessage("<Xray> Added new Block: " + this.newBlock.getPlannedValue());
                    if(this.isOn()) {
                        mc.renderGlobal.loadRenderers();
                    }
                    event.setCanceled(true);
                } else {
                    Setting setting = event.getSetting();
                    if(setting.equals(this.enabled) || setting.equals(this.drawn) || setting.equals(this.bind) || setting.equals(this.newBlock) || setting.equals(this.showBlocks)) {
                        return;
                    }

                    if(setting.getValue() instanceof Boolean) {
                        if(!(boolean)setting.getPlannedValue()) {
                            this.unregister(setting);
                            if(this.isOn()) {
                                mc.renderGlobal.loadRenderers();
                            }
                            event.setCanceled(true);
                        }
                    }
                }
            }
        }
    }

    public boolean shouldRender(Block block) {
        return shouldRender(block.getLocalizedName());
    }

    public boolean shouldRender(String name) {
        for(Setting setting : this.getSettings()) {
            if(name.equalsIgnoreCase(setting.getName())) {
                return true;
            }
        }
        return false;
    }
}
