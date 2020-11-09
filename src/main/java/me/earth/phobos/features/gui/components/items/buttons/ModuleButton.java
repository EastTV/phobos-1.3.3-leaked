package me.earth.phobos.features.gui.components.items.buttons;

import me.earth.phobos.Phobos;
import me.earth.phobos.features.gui.PhobosGui;
import me.earth.phobos.features.gui.components.items.Item;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.modules.client.ClickGui;
import me.earth.phobos.features.setting.Bind;
import me.earth.phobos.features.setting.Setting;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;

import java.util.ArrayList;
import java.util.List;

public class ModuleButton extends Button {

    private final Module module;
    private List<Item> items = new ArrayList<>();
    private boolean subOpen;

    public ModuleButton(Module module) {
        super(module.getName());
        this.module = module;
        initSettings();
    }

    public void initSettings() {
        List<Item> newItems = new ArrayList<>();
        if (!this.module.getSettings().isEmpty()) {
            for (Setting setting : this.module.getSettings()) {
                if (setting.getValue() instanceof Boolean && !setting.getName().equals("Enabled")) {
                    newItems.add(new BooleanButton(setting));
                }

                if(setting.getValue() instanceof Bind && !module.getName().equalsIgnoreCase("Hud")) {
                    newItems.add(new BindButton(setting));
                }

                if(setting.getValue() instanceof String || setting.getValue() instanceof Character) {
                    newItems.add(new StringButton(setting));
                }

                if(setting.isNumberSetting()) {
                    if(setting.hasRestriction()) {
                        newItems.add(new Slider(setting));
                        continue;
                    }
                    newItems.add(new UnlimitedSlider(setting));
                }

                if(setting.isEnumSetting()) {
                    newItems.add(new EnumButton(setting));
                }
            }
        }
        this.items = newItems;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (!this.items.isEmpty()) {
            Phobos.textManager.drawStringWithShadow((Phobos.moduleManager.getModuleByClass(ClickGui.class).getSettingByName("Buttons").getValueAsString()), x - 1.5F + width - 7.4F, y - 2F - PhobosGui.getClickGui().getTextOffset(), 0xFFFFFFFF);
            if (subOpen) {
                float height = 1;
                for (Item item : this.items) {
                    if(!item.isHidden()) {
                        height += 15F;
                        item.setLocation(x + 1, y + height);
                        item.setHeight(15);
                        item.setWidth(width - 9);
                        item.drawScreen(mouseX, mouseY, partialTicks);
                    }
                    item.update();
                }
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (!this.items.isEmpty()) {
            if (mouseButton == 1 && isHovering(mouseX, mouseY)) {
                subOpen = !subOpen;
                mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }

            if (subOpen) {
                for (Item item : this.items) {
                    if(!item.isHidden()) {
                        item.mouseClicked(mouseX, mouseY, mouseButton);
                    }
                }
            }
        }
    }

    @Override
    public void onKeyTyped(char typedChar, int keyCode) {
        super.onKeyTyped(typedChar, keyCode);
        if (!this.items.isEmpty() && subOpen) {
            for (Item item : this.items) {
                if(!item.isHidden()) {
                    item.onKeyTyped(typedChar, keyCode);
                }
            }
        }
    }

    @Override
    public int getHeight() {
        if (subOpen) {
            int height = 14;
            for (Item item : this.items) {
                if(!item.isHidden()) {
                    height += item.getHeight() + 1;
                }
            }
            return height + 2;
        } else {
            return 14;
        }
    }

    public Module getModule() {
        return this.module;
    }

    public void toggle() {
        module.toggle();
    }

    public boolean getState() {
        return module.isEnabled();
    }
}

