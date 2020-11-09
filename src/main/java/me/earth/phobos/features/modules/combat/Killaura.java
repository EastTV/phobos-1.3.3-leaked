package me.earth.phobos.features.modules.combat;

import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.UpdateWalkingPlayerEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.DamageUtil;
import me.earth.phobos.util.EntityUtil;
import me.earth.phobos.util.MathUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Killaura extends Module {

    public Setting<Float> range = register(new Setting("Range", 6.0f, 0.1f, 7.0f));
    private Setting<TargetMode> targetMode = register(new Setting("Target", TargetMode.CLOSEST));
    public Setting<Float> health = register(new Setting("Health", 6.0f, 0.1f, 36.0f, v -> targetMode.getValue() == TargetMode.SMART));
    public Setting<Boolean> delay = register(new Setting("Delay", true));
    public Setting<Boolean> rotate = register(new Setting("Rotate", true));
    public Setting<Boolean> armorBreak = register(new Setting("ArmorBreak", false));
    public Setting<Boolean> eating = register(new Setting("Eating", true));
    public Setting<Boolean> onlySharp = register(new Setting("Axe/Sword", true));
    public Setting<Boolean> teleport = register(new Setting("Teleport", false));
    public Setting<Float> raytrace = register(new Setting("Raytrace", 6.0f, 0.1f, 7.0f, v -> !teleport.getValue(), "Wall Range."));
    public Setting<Float> teleportRange = register(new Setting("TpRange", 15.0f, 0.1f, 50.0f, v -> teleport.getValue(), "Teleport Range."));
    public Setting<Boolean> lagBack = register(new Setting("LagBack", true, v -> teleport.getValue()));
    public Setting<Boolean> teekaydelay = register(new Setting("32kDelay", false));
    public Setting<Integer> time32k = register(new Setting("32kTime", 5, 1, 50));
    public Setting<Integer> multi = register(new Setting("32kPackets", 2, v -> !teekaydelay.getValue()));
    public Setting<Boolean> multi32k = register(new Setting("Multi32k", false));
    public Setting<Boolean> players = register(new Setting("Players", true));
    public Setting<Boolean> mobs = register(new Setting("Mobs", false));
    public Setting<Boolean> animals = register(new Setting("Animals", false));
    public Setting<Boolean> vehicles = register(new Setting("Entities", false));
    public Setting<Boolean> projectiles = register(new Setting("Projectiles", false));
    public Setting<Boolean> tps = register(new Setting("TpsSync", true));
    public Setting<Boolean> packet = register(new Setting("Packet", false));
    public Setting<Boolean> info = register(new Setting("Info", true));

    private final Timer timer = new Timer();
    public static Entity target;

    public Killaura() {
        super("Killaura", "Kills aura.", Category.COMBAT, true, false, false);
    }

    @Override
    public void onTick() {
        if(!rotate.getValue()) {
            doKillaura();
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent event) {
        if(event.getStage() == 0 && rotate.getValue()) {
            doKillaura();
        }
    }

    private void doKillaura() {
        if(onlySharp.getValue() && !EntityUtil.holdingWeapon(mc.player)) {
            target = null;
            return;
        }

        int wait = (!delay.getValue() || (EntityUtil.holding32k(mc.player) && !teekaydelay.getValue())) ? 0 : (int)(DamageUtil.getCooldownByWeapon(mc.player) * (tps.getValue() ? Phobos.serverManager.getTpsFactor() : 1));
        if(!timer.passedMs(wait) || (!eating.getValue() && mc.player.isHandActive() && !(mc.player.getHeldItemOffhand().getItem().equals(Items.SHIELD) && mc.player.getActiveHand() == EnumHand.OFF_HAND))) {
            return;
        }

        if(!(targetMode.getValue() == TargetMode.FOCUS && target != null && ((mc.player.getDistanceSq(target) < MathUtil.square(range.getValue()) || (teleport.getValue() && mc.player.getDistanceSq(target) < MathUtil.square(teleportRange.getValue()))) && ((mc.player.canEntityBeSeen(target) || EntityUtil.canEntityFeetBeSeen(target) || mc.player.getDistanceSq(target) < MathUtil.square(raytrace.getValue())) || teleport.getValue())))) {
            target = getTarget();
        }

        if(target == null) {
            return;
        }

        if(rotate.getValue()) {
            Phobos.rotationManager.lookAtEntity(target);
        }

        if(teleport.getValue()) {
            Phobos.positionManager.setPositionPacket(target.posX, EntityUtil.canEntityFeetBeSeen(target) ? target.posY : target.posY + target.getEyeHeight(), target.posZ, true, true, !lagBack.getValue());
        }

        if(EntityUtil.holding32k(mc.player) && !teekaydelay.getValue()) {
            if(multi32k.getValue()) {
                for(EntityPlayer player : mc.world.playerEntities) {
                    if(EntityUtil.isValid(player, range.getValue())) {
                        teekayAttack(player);
                    }
                }
            } else {
                teekayAttack(target);
            }
            timer.reset();
            return;
        }

        if(armorBreak.getValue()) {
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 9, mc.player.inventory.currentItem, ClickType.SWAP, mc.player);
            EntityUtil.attackEntity(target, packet.getValue(), true);
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 9, mc.player.inventory.currentItem, ClickType.SWAP, mc.player);
            EntityUtil.attackEntity(target, packet.getValue(), true);
        } else {
            EntityUtil.attackEntity(target, packet.getValue(), true);
        }
        timer.reset();
    }

    private void teekayAttack(Entity entity) {
        for(int i = 0; i < multi.getValue(); i++) {
            //EntityUtil.attackEntity(entity, packet.getValue(), true);
            startEntityAttackThread(entity, i * time32k.getValue());
        }
    }

    private void startEntityAttackThread(Entity entity, int time) {
        new Thread(() -> {
            Timer timer = new Timer();
            timer.reset();
            try {
                Thread.sleep(time);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            EntityUtil.attackEntity(entity, true, true);
        }).start();
    }

    private Entity getTarget() {
        Entity target = null;
        double distance = teleport.getValue() ? teleportRange.getValue() : range.getValue();
        double maxHealth = 36.0;
        for(Entity entity : mc.world.loadedEntityList) {
            if((players.getValue() && entity instanceof EntityPlayer) || (animals.getValue() && EntityUtil.isPassive(entity)) || (mobs.getValue() && EntityUtil.isMobAggressive(entity)) || (vehicles.getValue() && EntityUtil.isVehicle(entity)) || (projectiles.getValue() && EntityUtil.isProjectile(entity))) {
                if(entity instanceof EntityLivingBase && EntityUtil.isntValid(entity, distance)) {
                    continue;
                }

                if (!teleport.getValue() && !mc.player.canEntityBeSeen(entity) && !EntityUtil.canEntityFeetBeSeen(entity) && mc.player.getDistanceSq(entity) > MathUtil.square(raytrace.getValue())) {
                    continue;
                }

                if(target == null) {
                    target = entity;
                    distance = mc.player.getDistanceSq(entity);
                    maxHealth = EntityUtil.getHealth(entity);
                    continue;
                }

                if(entity instanceof EntityPlayer && DamageUtil.isArmorLow((EntityPlayer)entity, 18)) {
                    target = entity;
                    break;
                }

                if(targetMode.getValue() == TargetMode.SMART && EntityUtil.getHealth(entity) < health.getValue()) {
                    target = entity;
                    break;
                }

                if(targetMode.getValue() != TargetMode.HEALTH && mc.player.getDistanceSq(entity) < distance) {
                    target = entity;
                    distance = mc.player.getDistanceSq(entity);
                    maxHealth = EntityUtil.getHealth(entity);
                }

                if (targetMode.getValue() == TargetMode.HEALTH && EntityUtil.getHealth(entity) < maxHealth) {
                    target = entity;
                    distance = mc.player.getDistanceSq(entity);
                    maxHealth = EntityUtil.getHealth(entity);
                }
            }
        }
        return target;
    }

    @Override
    public String getDisplayInfo() {
        if(info.getValue() && target != null && target instanceof EntityPlayer)  {
            return target.getName();
        }
        return null;
    }

    public enum TargetMode {
        FOCUS,
        CLOSEST,
        HEALTH,
        SMART
    }
}
