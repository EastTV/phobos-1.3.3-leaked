package me.earth.phobos.features.gui.components.items.buttons;

import me.earth.phobos.Phobos;
import me.earth.phobos.features.gui.PhobosGui;
import me.earth.phobos.features.modules.client.ClickGui;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.RenderUtil;
import me.earth.phobos.util.TextUtil;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;

public class UnlimitedSlider extends Button {

    public Setting setting;

    public UnlimitedSlider(Setting setting) {
        super(setting.getName());
        this.setting = setting;
        width = 15;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        RenderUtil.drawRect(x, y, x + width + 7.4F, y + height - 0.5f, !isHovering(mouseX, mouseY) ? Phobos.colorManager.getColorWithAlpha((Phobos.moduleManager.getModuleByClass(ClickGui.class)).hoverAlpha.getValue()) : Phobos.colorManager.getColorWithAlpha((Phobos.moduleManager.getModuleByClass(ClickGui.class)).alpha.getValue()));
        Phobos.textManager.drawStringWithShadow(" - " + setting.getName() + " " + TextUtil.GRAY + setting.getValue() + TextUtil.RESET + " +", x + 2.3F, y - 1.7F - PhobosGui.getClickGui().getTextOffset(), getState() ? 0xFFFFFFFF : 0xFFAAAAAA);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (isHovering(mouseX, mouseY)) {
            mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            if(isRight(mouseX)) {
                if(setting.getValue() instanceof Double) {
                    setting.setValue(((Double) setting.getValue() + 1));
                } else if (setting.getValue() instanceof Float) {
                    setting.setValue(((Float) setting.getValue() + 1));
                } else if (setting.getValue() instanceof Integer) {
                    setting.setValue(((Integer) setting.getValue() + 1));
                }
            } else {
                if(setting.getValue() instanceof Double) {
                    setting.setValue(((Double) setting.getValue() - 1));
                } else if (setting.getValue() instanceof Float) {
                    setting.setValue(((Float) setting.getValue() - 1));
                } else if (setting.getValue() instanceof Integer) {
                    setting.setValue(((Integer) setting.getValue() - 1));
                }
            }
        }
    }

    @Override
    public void update() {
        this.setHidden(!setting.isVisible());
    }

    @Override
    public int getHeight() {
        return 14;
    }

    public void toggle() {}

    public boolean getState() {
        return true;
    }

    public boolean isRight(int x) {
        return x > this.x + ((width + 7.4F) / 2);
    }
}
