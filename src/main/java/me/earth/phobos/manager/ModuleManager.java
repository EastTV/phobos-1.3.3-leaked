package me.earth.phobos.manager;

import me.earth.phobos.event.events.Render2DEvent;
import me.earth.phobos.event.events.Render3DEvent;
import me.earth.phobos.features.Feature;
import me.earth.phobos.features.gui.PhobosGui;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.modules.client.ClickGui;
import me.earth.phobos.features.modules.client.Components;
import me.earth.phobos.features.modules.client.FontMod;
import me.earth.phobos.features.modules.client.HUD;
import me.earth.phobos.features.modules.client.Managers;
import me.earth.phobos.features.modules.client.Notifications;
import me.earth.phobos.features.modules.client.StreamerMode;
import me.earth.phobos.features.modules.combat.AntiTrap;
import me.earth.phobos.features.modules.combat.ArmorMessage;
import me.earth.phobos.features.modules.combat.Auto32k;
import me.earth.phobos.features.modules.combat.AutoArmor;
import me.earth.phobos.features.modules.combat.AutoCrystal;
import me.earth.phobos.features.modules.combat.AutoTrap;
import me.earth.phobos.features.modules.combat.BedBomb;
import me.earth.phobos.features.modules.combat.BowSpam;
import me.earth.phobos.features.modules.combat.Crasher;
import me.earth.phobos.features.modules.combat.Criticals;
import me.earth.phobos.features.modules.combat.HoleFiller;
import me.earth.phobos.features.modules.combat.Killaura;
import me.earth.phobos.features.modules.combat.Offhand;
import me.earth.phobos.features.modules.combat.Selftrap;
import me.earth.phobos.features.modules.combat.Surround;
import me.earth.phobos.features.modules.combat.Webaura;
import me.earth.phobos.features.modules.misc.AntiPackets;
import me.earth.phobos.features.modules.misc.AntiVanish;
import me.earth.phobos.features.modules.misc.AutoLog;
import me.earth.phobos.features.modules.misc.AutoReconnect;
import me.earth.phobos.features.modules.misc.AutoRespawn;
import me.earth.phobos.features.modules.misc.BetterPortals;
import me.earth.phobos.features.modules.misc.BuildHeight;
import me.earth.phobos.features.modules.misc.Bypass;
import me.earth.phobos.features.modules.misc.ChatModifier;
import me.earth.phobos.features.modules.misc.Exploits;
import me.earth.phobos.features.modules.misc.ExtraTab;
import me.earth.phobos.features.modules.misc.KitDelete;
import me.earth.phobos.features.modules.misc.MCF;
import me.earth.phobos.features.modules.misc.MobOwner;
import me.earth.phobos.features.modules.misc.NoAFK;
import me.earth.phobos.features.modules.misc.NoHandShake;
import me.earth.phobos.features.modules.misc.NoRotate;
import me.earth.phobos.features.modules.misc.NoSoundLag;
import me.earth.phobos.features.modules.misc.Nuker;
import me.earth.phobos.features.modules.misc.PingSpoof;
import me.earth.phobos.features.modules.misc.Spammer;
import me.earth.phobos.features.modules.misc.ToolTips;
import me.earth.phobos.features.modules.misc.Tracker;
import me.earth.phobos.features.modules.movement.AntiLevitate;
import me.earth.phobos.features.modules.movement.ElytraFlight;
import me.earth.phobos.features.modules.movement.Flight;
import me.earth.phobos.features.modules.movement.HoleTP;
import me.earth.phobos.features.modules.movement.IceSpeed;
import me.earth.phobos.features.modules.movement.NoFall;
import me.earth.phobos.features.modules.movement.NoSlowDown;
import me.earth.phobos.features.modules.movement.Phase;
import me.earth.phobos.features.modules.movement.SafeWalk;
import me.earth.phobos.features.modules.movement.Speed;
import me.earth.phobos.features.modules.movement.Sprint;
import me.earth.phobos.features.modules.movement.Static;
import me.earth.phobos.features.modules.movement.Step;
import me.earth.phobos.features.modules.movement.Strafe;
import me.earth.phobos.features.modules.movement.TPSpeed;
import me.earth.phobos.features.modules.movement.Velocity;
import me.earth.phobos.features.modules.player.Blink;
import me.earth.phobos.features.modules.player.BlockTweaks;
import me.earth.phobos.features.modules.player.EchestBP;
import me.earth.phobos.features.modules.player.FakePlayer;
import me.earth.phobos.features.modules.player.FastPlace;
import me.earth.phobos.features.modules.player.Freecam;
import me.earth.phobos.features.modules.player.Jesus;
import me.earth.phobos.features.modules.player.LiquidInteract;
import me.earth.phobos.features.modules.player.MCP;
import me.earth.phobos.features.modules.player.MultiTask;
import me.earth.phobos.features.modules.player.NoHunger;
import me.earth.phobos.features.modules.player.Reach;
import me.earth.phobos.features.modules.player.Replenish;
import me.earth.phobos.features.modules.player.Scaffold;
import me.earth.phobos.features.modules.player.Speedmine;
import me.earth.phobos.features.modules.player.TimerSpeed;
import me.earth.phobos.features.modules.player.TpsSync;
import me.earth.phobos.features.modules.player.XCarry;
import me.earth.phobos.features.modules.render.BlockHighlight;
import me.earth.phobos.features.modules.render.CameraClip;
import me.earth.phobos.features.modules.render.Chams;
import me.earth.phobos.features.modules.render.ESP;
import me.earth.phobos.features.modules.render.Fullbright;
import me.earth.phobos.features.modules.render.HoleESP;
import me.earth.phobos.features.modules.render.LogoutSpots;
import me.earth.phobos.features.modules.render.Nametags;
import me.earth.phobos.features.modules.render.NoRender;
import me.earth.phobos.features.modules.render.PortalESP;
import me.earth.phobos.features.modules.render.Ranges;
import me.earth.phobos.features.modules.render.Skeleton;
import me.earth.phobos.features.modules.render.SmallShield;
import me.earth.phobos.features.modules.render.StorageESP;
import me.earth.phobos.features.modules.render.Tracer;
import me.earth.phobos.features.modules.render.Trajectories;
import me.earth.phobos.features.modules.render.XRay;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager extends Feature {

    public ArrayList<Module> modules = new ArrayList<>();
    public List<Module> sortedModules = new ArrayList<>();

    public void init() {
        //COMBAT
        modules.add(new Offhand());
        modules.add(new Surround());
        modules.add(new AutoTrap());
        modules.add(new AutoCrystal());
        modules.add(new Criticals());
        modules.add(new BowSpam());
        modules.add(new Killaura());
        modules.add(new HoleFiller());
        modules.add(new Selftrap());
        modules.add(new Webaura());
        modules.add(new AutoArmor());
        modules.add(new AntiTrap());
        modules.add(new BedBomb());
        modules.add(new ArmorMessage());
        modules.add(new Crasher());
        modules.add(new Auto32k());

        //MISC
        modules.add(new ChatModifier());
        modules.add(new BetterPortals());
        modules.add(new BuildHeight());
        modules.add(new NoHandShake());
        modules.add(new AutoRespawn());
        modules.add(new NoRotate());
        modules.add(new MCF());
        modules.add(new PingSpoof());
        modules.add(new NoSoundLag());
        modules.add(new AutoLog());
        modules.add(new KitDelete());
        modules.add(new Exploits());
        modules.add(new Spammer());
        modules.add(new AntiVanish());
        modules.add(new ExtraTab());
        modules.add(new MobOwner());
        modules.add(new Nuker());
        modules.add(new AutoReconnect());
        modules.add(new NoAFK());
        modules.add(new Tracker());
        modules.add(new AntiPackets());

        //MOVEMENT
        modules.add(new Bypass());
        modules.add(new Strafe());
        modules.add(new Velocity());
        modules.add(new Speed());
        modules.add(new Step());
        modules.add(new Sprint());
        modules.add(new AntiLevitate());
        modules.add(new Phase());
        modules.add(new Static());
        modules.add(new TPSpeed());
        modules.add(new Flight());
        modules.add(new ElytraFlight());
        modules.add(new NoSlowDown());
        modules.add(new HoleTP());
        modules.add(new NoFall());
        modules.add(new IceSpeed());

        //PLAYER
        modules.add(new Reach());
        modules.add(new LiquidInteract());
        modules.add(new FakePlayer());
        modules.add(new TimerSpeed());
        modules.add(new FastPlace());
        modules.add(new Freecam());
        modules.add(new Speedmine());
        modules.add(new SafeWalk());
        modules.add(new Blink());
        modules.add(new MultiTask());
        modules.add(new BlockTweaks());
        modules.add(new XCarry());
        modules.add(new Replenish());
        modules.add(new NoHunger());
        modules.add(new Jesus());
        modules.add(new Scaffold());
        modules.add(new EchestBP());
        modules.add(new TpsSync());
        modules.add(new MCP());

        //RENDER
        modules.add(new StorageESP());
        modules.add(new NoRender());
        modules.add(new SmallShield());
        modules.add(new Fullbright());
        modules.add(new Nametags());
        modules.add(new CameraClip());
        modules.add(new Chams());
        modules.add(new Skeleton());
        modules.add(new ESP());
        modules.add(new HoleESP());
        modules.add(new BlockHighlight());
        modules.add(new Trajectories());
        modules.add(new Tracer());
        modules.add(new LogoutSpots());
        modules.add(new XRay());
        modules.add(new PortalESP());
        modules.add(new Ranges());

        //CLIENT
        modules.add(new Notifications());
        modules.add(new HUD());
        modules.add(new ToolTips()); //MISC, but needs to be rendered after HUD
        modules.add(new FontMod());
        modules.add(new ClickGui());
        modules.add(new Managers());
        modules.add(new Components());
        modules.add(new StreamerMode());
    }

    public Module getModuleByName(String name) {
        for(Module module : this.modules) {
            if(module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

    public <T extends Module> T getModuleByClass(Class<T> clazz) {
        for(Module module : this.modules) {
            if(clazz.isInstance(module)) {
                return (T)module;
            }
        }
        return null;
    }

    public void enableModule(Class clazz) {
        Module module = getModuleByClass(clazz);
        if(module != null) {
            module.enable();
        }
    }

    public void disableModule(Class clazz) {
        Module module = getModuleByClass(clazz);
        if(module != null) {
            module.disable();
        }
    }

    public void enableModule(String name) {
        Module module = getModuleByName(name);
        if(module != null) {
            module.enable();
        }
    }

    public void disableModule(String name) {
        Module module = getModuleByName(name);
        if(module != null) {
            module.disable();
        }
    }

    public boolean isModuleEnabled(String name) {
        Module module = getModuleByName(name);
        return module != null && module.isOn();
    }

    public boolean isModuleEnabled(Class clazz) {
        Module module = getModuleByClass(clazz);
        return module != null && module.isOn();
    }

    public Module getModuleByDisplayName(String displayName) {
        for(Module module : this.modules) {
            if(module.getDisplayName().equalsIgnoreCase(displayName)) {
                return module;
            }
        }
        return null;
    }

    public ArrayList<Module> getEnabledModules() {
        ArrayList<Module> enabledModules = new ArrayList<>();
        for (Module module : modules) {
            if (module.isEnabled()) {
                enabledModules.add(module);
            }
        }
        return enabledModules;
    }

    public ArrayList<Module> getModulesByCategory(Module.Category category) {
        ArrayList<Module> modulesCategory = new ArrayList<>();
        this.modules.forEach(module -> {
            if(module.getCategory() == category) {
                modulesCategory.add(module);
            }
        });
        return modulesCategory;
    }

    public List<Module.Category> getCategories() {
        return Arrays.asList(Module.Category.values());
    }

    public void onLoad() {
        modules.stream().filter(Module::listening).forEach(MinecraftForge.EVENT_BUS::register);
        modules.forEach(Module::onLoad);
    }

    public void onUpdate() {
        modules.stream().filter(Feature::isEnabled).forEach(Module::onUpdate);
    }

    public void onTick() {
        modules.stream().filter(Feature::isEnabled).forEach(Module::onTick);
    }

    public void onRender2D(Render2DEvent event) {
        modules.stream().filter(Feature::isEnabled).forEach(module -> module.onRender2D(event));
    }

    public void onRender3D(Render3DEvent event) {
        modules.stream().filter(Feature::isEnabled).forEach(module -> module.onRender3D(event));
    }

    public void sortModules(boolean reverse) {
        this.sortedModules = getEnabledModules().stream().filter(Module::isDrawn)
                .sorted(Comparator.comparing(module -> renderer.getStringWidth(module.getFullArrayString()) * (reverse ? -1 : 1)))
                .collect(Collectors.toList());
    }

    public void onLogout() {
        modules.forEach(Module::onLogout);
    }

    public void onLogin() {
        modules.forEach(Module::onLogin);
    }

    public void onUnload() {
        modules.forEach(MinecraftForge.EVENT_BUS::unregister);
        modules.forEach(Module::onUnload);
    }

    public void onUnloadPost() {
        for(Module module : modules) {
            module.enabled.setValue(false);
        }
    }

    public void onKeyPressed(int eventKey) {
        if (eventKey == 0 || !Keyboard.getEventKeyState() || mc.currentScreen instanceof PhobosGui) {
            return;
        }
        modules.forEach(module -> {
            if (module.getBind().getKey() == eventKey) {
                module.toggle();
            }
        });
    }
}
