package me.earth.phobos.features.modules;

import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.ClientEvent;
import me.earth.phobos.event.events.Render2DEvent;
import me.earth.phobos.event.events.Render3DEvent;
import me.earth.phobos.features.Feature;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.setting.Bind;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.TextUtil;
import net.minecraftforge.common.MinecraftForge;

public class Module extends Feature {

    private final String description;
    private final Category category;
    public Setting <Boolean> enabled = register(new Setting("Enabled", false));
    public Setting <Boolean> drawn = register(new Setting("Drawn", true));
    public Setting<Bind> bind = register(new Setting("Bind", new Bind(-1)));
    public Setting<String> displayName;
    public boolean hasListener;
    public boolean alwaysListening;
    public boolean hidden;

    public Module(String name, String description, Category category, boolean hasListener, boolean hidden, boolean alwaysListening) {
        super(name);
        this.displayName = register(new Setting("DisplayName", name));
        this.description = description;
        this.category = category;
        this.hasListener = hasListener;
        this.hidden = hidden;
        this.alwaysListening = alwaysListening;
    }

    public enum Category {
        COMBAT("Combat"),
        MISC("Misc"),
        RENDER("Render"),
        MOVEMENT("Movement"),
        PLAYER("Player"),
        CLIENT("Client");

        private final String name;

        Category(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public void onEnable() {
        //Is called on Enableing.
    }

    public void onDisable() {
        //Is called on disableing.
    }

    public void onToggle() {
        //Is called on both enableing and disableing
    }

    public void onLoad() {
        //Called for every module after loading the client.
    }

    public void onTick() {
        //Called on Client TickEvent. (That one has phases...)
    }

    public void onLogin() {
        //Called when the player logs in.
    }

    public void onLogout() {
        //Called when the player logs out.
    }

    public void onUpdate() {
        //Called onLivingUpdate
    }

    public void onRender2D(Render2DEvent event) {
        //Called instead of Render2DEvent //TODO: Maybe on the bus?
    }

    public void onRender3D(Render3DEvent event) {
        //Called instead of Render3DEvent //TODO: Maybe on the bus?
    }

    public void onUnload() {
        //Called when the client is unloaded or shut down.
    }

    public String getDisplayInfo() {
        return null;
    }

    public boolean isOn() { return this.enabled.getValue(); }

    public boolean isOff() { return !this.enabled.getValue(); }

    public void setEnabled(boolean enabled) {
        if(enabled) {
            this.enable();
        } else {
            this.disable();
        }
    }

    public void enable() {
        this.enabled.setValue(true);
        this.onToggle();
        this.onEnable();
        if(this.isOn() && this.hasListener && !alwaysListening) {
            MinecraftForge.EVENT_BUS.register(this);
        }
    }

    public void disable() {
        if(this.hasListener && !alwaysListening) {
            MinecraftForge.EVENT_BUS.unregister(this);
        }
        this.enabled.setValue(false);
        this.onToggle();
        this.onDisable();
    }

    public void toggle() {
        ClientEvent event = new ClientEvent(!this.isEnabled() ? 1 : 0, this);
        MinecraftForge.EVENT_BUS.post(event);
        if(!event.isCanceled()) {
            this.setEnabled(!this.isEnabled());
        }
    }

    public String getDisplayName() {
        return this.displayName.getValue();
    }

    public String getDescription() {
        return this.description;
    }

    public void setDisplayName(String name) {
        Module module = Phobos.moduleManager.getModuleByDisplayName(name);
        Module originalModule = Phobos.moduleManager.getModuleByName(name);
        if(module == null && originalModule == null) {
            Command.sendMessage(this.getDisplayName() + ", Original name: " + this.getName() + ", has been renamed to: " + name);
            this.displayName.setValue(name);
            return;
        }
        Command.sendMessage(TextUtil.RED + "A module of this name already exists.");
    }

    public boolean isDrawn() {
        return this.drawn.getValue();
    }

    public void setDrawn(boolean drawn) {
        this.drawn.setValue(drawn);
    }

    public Category getCategory() {
        return this.category;
    }

    public String getInfo() {
        return null;
    }

    public Bind getBind() {
        return this.bind.getValue();
    }

    public void setBind(int key) {
        this.bind.setValue(new Bind(key));
    }

    public boolean listening() {
        return (this.hasListener && this.isOn()) || this.alwaysListening;
    }

    public String getFullArrayString() {
        return this.getDisplayName() + TextUtil.DARK_GRAY + (this.getDisplayInfo() != null ? " [" + TextUtil.RESET + this.getDisplayInfo() + TextUtil.DARK_GRAY + "]" : "");
    }
}
