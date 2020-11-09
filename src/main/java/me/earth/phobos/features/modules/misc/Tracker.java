package me.earth.phobos.features.modules.misc;

import me.earth.phobos.event.events.DeathEvent;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.modules.combat.AutoCrystal;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.EntityUtil;
import me.earth.phobos.util.TextUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Objects;

public class Tracker extends Module {

    public Tracker() {
        super("Tracker", "Tracks players in 1v1s.", Category.MISC, true, false, false);
        instance = this;
    }

    public Setting<TextUtil.Color> color = register(new Setting("Color", TextUtil.Color.RED));

    private EntityPlayer trackedPlayer;
    private static Tracker instance;
    private int usedExp = 0;
    private int usedStacks = 0;
    private int usedCrystals = 0;
    private int usedCStacks = 0;

    @Override
    public void onUpdate() {
        if(trackedPlayer == null) {
            trackedPlayer = EntityUtil.getClosestEnemy(1000.0);
        } else {
            if(usedStacks != usedExp / 64) {
                usedStacks = usedExp / 64;
                Command.sendMessage(TextUtil.coloredString(trackedPlayer.getName() + " used: " + usedStacks + " Stacks of EXP.", color.getValue()));
            }
            if(usedCStacks != usedCrystals / 64) {
                usedCStacks = usedCrystals / 64;
                Command.sendMessage(TextUtil.coloredString(trackedPlayer.getName() + " used: " + usedCStacks + " Stacks of Crystals.", color.getValue()));
            }
        }
    }

    public void onSpawnEntity(Entity entity) {
        if(entity instanceof EntityExpBottle) {
            if (Objects.equals(mc.world.getClosestPlayerToEntity(entity, 3), trackedPlayer)) {
                usedExp++;
            }
        }

        if(entity instanceof EntityEnderCrystal) {
            if(AutoCrystal.placedPos.contains(entity.getPosition())) {
                AutoCrystal.placedPos.remove(entity.getPosition());
            } else {
                usedCrystals++;
            }
        }
    }

    @Override
    public void onDisable() {
        trackedPlayer = null;
        usedExp = 0;
        usedStacks = 0;
        usedCrystals = 0;
        usedCStacks = 0;
    }

    @SubscribeEvent
    public void onDeath(DeathEvent event) {
        if(event.player.equals(trackedPlayer)) {
            usedExp = 0;
            usedStacks = 0;
            usedCrystals = 0;
            usedCStacks = 0;
        }
    }

    @Override
    public String getDisplayInfo() {
        if(trackedPlayer != null) {
            return trackedPlayer.getName();
        }
        return null;
    }

    public static Tracker getInstance() {
        if(instance == null) {
            instance = new Tracker();
        }
        return instance;
    }
}
