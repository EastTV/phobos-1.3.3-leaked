package me.earth.phobos.features.gui.components.items.buttons;

import me.earth.phobos.Phobos;
import me.earth.phobos.features.gui.PhobosGui;
import me.earth.phobos.features.modules.client.ClickGui;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.RenderUtil;
import me.earth.phobos.util.TextUtil;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ChatAllowedCharacters;

public class StringButton extends Button {

    private Setting setting;
    public boolean isListening;
    private CurrentString currentString = new CurrentString("");

    public StringButton(Setting setting) {
        super(setting.getName());
        this.setting = setting;
        width = 15;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        RenderUtil.drawRect(x, y, x + width + 7.4F, y + height - 0.5f, getState() ? (!isHovering(mouseX, mouseY) ? Phobos.colorManager.getColorWithAlpha(((Phobos.moduleManager.getModuleByClass(ClickGui.class)).hoverAlpha.getValue())) : Phobos.colorManager.getColorWithAlpha(((Phobos.moduleManager.getModuleByClass(ClickGui.class)).alpha.getValue()))) : !isHovering(mouseX, mouseY) ? 0x11555555 : 0x88555555);
        if(isListening) {
            Phobos.textManager.drawStringWithShadow(currentString.getString() + Phobos.textManager.getIdleSign(), x + 2.3F, y - 1.7F - PhobosGui.getClickGui().getTextOffset(), getState() ? 0xFFFFFFFF : 0xFFAAAAAA);
        } else {
            Phobos.textManager.drawStringWithShadow((setting.getName().equals("Buttons") ? "Buttons " : (setting.getName().equals("Prefix") ? "Prefix  " + TextUtil.GRAY : "")) + setting.getValue(), x + 2.3F, y - 1.7F - PhobosGui.getClickGui().getTextOffset(), getState() ? 0xFFFFFFFF : 0xFFAAAAAA);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (isHovering(mouseX, mouseY)) {
            mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    //TODO: CHINESE
    @Override
    public void onKeyTyped(char typedChar, int keyCode) {
        if(isListening) {
            switch (keyCode) {
                case 1:
                    break;
                case 28:
                    enterString();
                    break;
                case 14:
                    setString(removeLastChar(currentString.getString()));
                    break;
                default:
                    if(ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                        setString(currentString.getString() + typedChar);
                    }
            }
        }
    }

    @Override
    public void update() {
        this.setHidden(!setting.isVisible());
    }

    private void enterString() {
        if(currentString.getString().isEmpty()) {
            setting.setValue(setting.getDefaultValue());
        } else {
            setting.setValue(currentString.getString());
        }
        setString("");
        super.onMouseClick();
    }

    @Override
    public int getHeight() {
        return 14;
    }

    public void toggle() {
        isListening = !isListening;
    }

    public boolean getState() {
        return !isListening;
    }

    public void setString(String newString) {
        this.currentString = new CurrentString(newString);
    }

    public static String removeLastChar(String str) {
        String output = "";
        if (str != null && str.length() > 0) {
            output = str.substring(0, str.length() - 1);
        }
        return output;
    }

    //TODO: WTF IS THIS
    public static class CurrentString {
        private String string;

        public CurrentString(String string) {
            this.string = string;
        }

        public String getString() {
            return this.string;
        }
    }
}
