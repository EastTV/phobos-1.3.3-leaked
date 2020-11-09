package me.earth.phobos.features.gui.components;

import me.earth.phobos.Phobos;
import me.earth.phobos.features.Feature;
import me.earth.phobos.features.gui.PhobosGui;
import me.earth.phobos.features.gui.components.items.Item;
import me.earth.phobos.features.gui.components.items.buttons.Button;
import me.earth.phobos.features.modules.client.ClickGui;
import me.earth.phobos.util.ColorUtil;
import me.earth.phobos.util.RenderUtil;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;

import java.util.ArrayList;

public class Component extends Feature {

    private int x;
    private int y;
    private int x2;
    private int y2;
    private int width;
    private int height;
    private boolean open;
    public boolean drag;
    private final ArrayList<Item> items = new ArrayList<>();
    private boolean hidden = false;

    public Component(String name, int x, int y, boolean open) {
        super(name);
        this.x = x;
        this.y = y;
        this.width = 88;
        this.height = 18;
        this.open = open;
        setupItems();
    }

    public void setupItems() {
        //For the child class
    }

    private void drag(int mouseX, int mouseY) {
        if (!drag) {
            return;
        }
        x = x2 + mouseX;
        y = y2 + mouseY;
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drag(mouseX, mouseY);
        float totalItemHeight = open ? getTotalItemHeight() - 2F : 0F;
        int color = 0xFF888888;
        if(ClickGui.getInstance().devSettings.getValue()) {
            color = ColorUtil.toARGB(ClickGui.getInstance().topRed.getValue(), ClickGui.getInstance().topGreen.getValue(), ClickGui.getInstance().topBlue.getValue(), ClickGui.getInstance().topAlpha.getValue());
        }
        RenderUtil.drawRect(x, y - 1.5F, (x + width), y + height - 6, color);
        if (open) {
            RenderUtil.drawRect(x, y + 12.5F, (x + width), y + height + totalItemHeight, 0x77000000);
        }
        Phobos.textManager.drawStringWithShadow(this.getName(), x + 3F, y - 4F - PhobosGui.getClickGui().getTextOffset(), 0xFFFFFFFF);
        if (open) {
            float y = getY() + getHeight() - 3F;
            for (Item item : getItems()) {
                if(!item.isHidden()) {
                    item.setLocation(x + 2F, y);
                    item.setWidth(getWidth() - 4);
                    item.drawScreen(mouseX, mouseY, partialTicks);
                    y += item.getHeight() + 1.5F;
                }
            }
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && isHovering(mouseX, mouseY)) {
            x2 = x - mouseX;
            y2 = y - mouseY;
            PhobosGui.getClickGui().getComponents().forEach(component -> {
                if (component.drag) {
                    component.drag = false;
                }
            });
            drag = true;
            return;
        }
        if (mouseButton == 1 && isHovering(mouseX, mouseY)) {
            open = !open;
            mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return;
        }
        if (!open) {
            return;
        }
        getItems().forEach(item -> item.mouseClicked(mouseX, mouseY, mouseButton));
    }

    public void mouseReleased(final int mouseX, int mouseY, int releaseButton) {
        if (releaseButton == 0)
            drag = false;
        if (!open) {
            return;
        }
        getItems().forEach(item -> item.mouseReleased(mouseX, mouseY, releaseButton));
    }

    public void onKeyTyped(char typedChar, int keyCode) {
        if (!open) {
            return;
        }
        getItems().forEach(item -> item.onKeyTyped(typedChar, keyCode));
    }

    public void addButton(Button button) {
        items.add(button);
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public boolean isOpen() {
        return open;
    }

    public final ArrayList<Item> getItems() {
        return items;
    }

    private boolean isHovering(int mouseX, int mouseY) {
        return mouseX >= getX() && mouseX <= getX() + getWidth() && mouseY >= getY() && mouseY <= getY() + getHeight() - (open ? 2 : 0);
    }

    private float getTotalItemHeight() {
        float height = 0;
        for (Item item : getItems()) {
            height += item.getHeight() + 1.5F;
        }
        return height;
    }
}
