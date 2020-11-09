package me.earth.phobos.features.modules.movement;

import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.event.events.PushEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Velocity extends Module {

    public Setting<Boolean> noPush = register(new Setting("NoPush", true));
    public Setting<Float> horizontal = register(new Setting("Horizontal", 0.0f, 0.0f, 100.0f));
    public Setting<Float> vertical = register(new Setting("Vertical", 0.0f, 0.0f, 100.0f));
    public Setting<Boolean> explosions = register(new Setting("Explosions", true));
    public Setting<Boolean> bobbers = register(new Setting("Bobbers", true));
    public Setting<Boolean> water = register(new Setting("Water", false));
    public Setting<Boolean> blocks = register(new Setting("Blocks", false));
    public Setting<Boolean> ice = register(new Setting("Ice", false));

    private static Velocity INSTANCE = new Velocity();

    public Velocity() {
        super("Velocity", "Allows you to control your velocity", Category.MOVEMENT, true, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static Velocity getINSTANCE() {
        if(INSTANCE == null) {
            INSTANCE = new Velocity();
        }
        return INSTANCE;
    }

    @Override
    public void onUpdate() {
        if(IceSpeed.getINSTANCE().isOff() && ice.getValue()) {
            Blocks.ICE.slipperiness = 0.6f;
            Blocks.PACKED_ICE.slipperiness = 0.6f;
            Blocks.FROSTED_ICE.slipperiness = 0.6f;
        }
    }

    @Override
    public void onDisable() {
        if(IceSpeed.getINSTANCE().isOff()) {
            Blocks.ICE.slipperiness = 0.98f;
            Blocks.PACKED_ICE.slipperiness = 0.98f;
            Blocks.FROSTED_ICE.slipperiness = 0.98f;
        }
    }

    @SubscribeEvent
    public void onPacketReceived(PacketEvent.Receive event) {
        if(event.getStage() == 0 && mc.player != null) {
            if (event.getPacket() instanceof SPacketEntityVelocity) {
                SPacketEntityVelocity velocity = event.getPacket();
                if (velocity.getEntityID() == mc.player.entityId) {
                    if (horizontal.getValue() == 0 && vertical.getValue() == 0) {
                        event.setCanceled(true);
                        return;
                    }

                    velocity.motionX *= horizontal.getValue();
                    velocity.motionY *= vertical.getValue();
                    velocity.motionZ *= horizontal.getValue();
                }
            }

            if (event.getPacket() instanceof SPacketEntityStatus && bobbers.getValue()) {
                SPacketEntityStatus packet = event.getPacket();
                if (packet.getOpCode() == 31) {
                    Entity entity = packet.getEntity(mc.world);
                    if (entity instanceof EntityFishHook) {
                        EntityFishHook fishHook = (EntityFishHook)entity;
                        if (fishHook.caughtEntity == mc.player) {
                            event.setCanceled(true);
                        }
                    }
                }
            }

            if (explosions.getValue() && event.getPacket() instanceof SPacketExplosion) {
                if (horizontal.getValue() == 0 && vertical.getValue() == 0) {
                    event.setCanceled(true);
                    return;
                }

                SPacketExplosion velocity = event.getPacket();
                velocity.motionX *= horizontal.getValue();
                velocity.motionY *= vertical.getValue();
                velocity.motionZ *= horizontal.getValue();
            }
        }
    }

    @SubscribeEvent
    public void onPush(PushEvent event) {
        if(event.getStage() == 0 && noPush.getValue() && event.entity.equals(mc.player)) {
            if (horizontal.getValue() == 0 && vertical.getValue() == 0) {
                event.setCanceled(true);
                return;
            }

            event.x = -event.x * horizontal.getValue();
            event.y = -event.y * vertical.getValue();
            event.z = -event.z * horizontal.getValue();
        } else if(event.getStage() == 1 && blocks.getValue()) {
            event.setCanceled(true);
        } else if(event.getStage() == 2 && water.getValue() && mc.player != null && mc.player.equals(event.entity)) {
            event.setCanceled(true);
        }
    }
}
