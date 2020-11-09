package me.earth.phobos.features.gui;

import me.earth.phobos.Phobos;
import me.earth.phobos.features.gui.components.Component;
import me.earth.phobos.features.gui.components.items.Item;
import me.earth.phobos.features.gui.components.items.buttons.ModuleButton;
import me.earth.phobos.features.modules.Module;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;

public class PhobosGui extends GuiScreen {

    private static PhobosGui phobosGui;
    private final ArrayList<Component> components = new ArrayList<>();
    private static PhobosGui INSTANCE = new PhobosGui();

    public PhobosGui() {
        setInstance();
        load();
    }

    public static PhobosGui getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new PhobosGui();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static PhobosGui getClickGui() {
        return getInstance();
    }

    private void load() {
        int x = -84;
        for(Module.Category category : Phobos.moduleManager.getCategories()) {
            components.add(new Component(category.getName(), x += 90, 4, true) {
                @Override
                public void setupItems() {
                    Phobos.moduleManager.getModulesByCategory(category).forEach(module -> {
                        if (!module.hidden) {
                            addButton(new ModuleButton(module));
                        }
                    });
                }
            });
        }
        components.forEach(components -> components.getItems().sort((item1, item2) -> item1.getName().compareTo(item2.getName())));
    }

    public void updateModule(Module module) {
        for(Component component : this.components) {
            for(Item item : component.getItems()) {
                if(item instanceof ModuleButton) {
                    ModuleButton button = (ModuleButton)item;
                    Module mod = button.getModule();
                    if(module != null && module.equals(mod)) {
                        button.initSettings();
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        checkMouseWheel();
        this.drawDefaultBackground();
        components.forEach(components -> components.drawScreen(mouseX, mouseY, partialTicks));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int clickedButton) {
        components.forEach(components -> components.mouseClicked(mouseX, mouseY, clickedButton));
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int releaseButton) {
        components.forEach(components -> components.mouseReleased(mouseX, mouseY, releaseButton));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public final ArrayList<Component> getComponents() {
        return components;
    }

    public void checkMouseWheel() {
        int dWheel = Mouse.getDWheel();
        if (dWheel < 0) {
            components.forEach(component -> component.setY(component.getY() - 10));
        } else if (dWheel > 0) {
            components.forEach(component -> component.setY(component.getY() + 10));
        }
    }

    public int getTextOffset() {
        return -6;
    }

    public Component getComponentByName(String name) {
        for(Component component : this.components) {
            if(component.getName().equalsIgnoreCase(name)) {
                return component;
            }
        }
        return null;
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        components.forEach(component -> component.onKeyTyped(typedChar, keyCode));
    }
}