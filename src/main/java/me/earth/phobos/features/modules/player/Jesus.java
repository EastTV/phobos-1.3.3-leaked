package me.earth.phobos.features.modules.player;

import me.earth.phobos.event.events.JesusEvent;
import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.event.events.UpdateWalkingPlayerEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.EntityUtil;
import net.minecraft.block.Block;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketMoveVehicle;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Jesus extends Module {

    public Setting<Mode> mode = register(new Setting("Mode", Mode.NORMAL));
    public Setting<Boolean> cancelVehicle = register(new Setting("NoVehicle", false));
    public Setting<EventMode> eventMode = register(new Setting("Jump", EventMode.PRE, v -> mode.getValue() == Mode.TRAMPOLINE));
    public Setting<Boolean> fall = register(new Setting("NoFall", false, v -> mode.getValue() == Mode.TRAMPOLINE));

    public static AxisAlignedBB offset = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.9999, 1.0);
    private static Jesus INSTANCE = new Jesus();
    private boolean grounded = false;

    public Jesus() {
        super("Jesus", "Allows you to walk on water", Category.PLAYER, true, false, false);
        INSTANCE = this;
    }

    public static Jesus getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Jesus();
        }
        return INSTANCE;
    }

    @SubscribeEvent
    public void updateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if(fullNullCheck() || Freecam.getInstance().isOn()) {
            return;
        }

        if (event.getStage() == 0 && (mode.getValue() == Mode.BOUNCE || mode.getValue() == Mode.VANILLA || mode.getValue() == Mode.NORMAL)) {
            if (!mc.player.isSneaking() && !mc.player.noClip && !mc.gameSettings.keyBindJump.isKeyDown() && EntityUtil.isInLiquid()) {
                mc.player.motionY = 0.1f;
            }
        }

        if(event.getStage() == 0 && mode.getValue() == Mode.TRAMPOLINE && (eventMode.getValue() == EventMode.ALL || eventMode.getValue() == EventMode.PRE)) {
            doTrampoline();
        } else if(event.getStage() == 1 && mode.getValue() == Mode.TRAMPOLINE && (eventMode.getValue() == EventMode.ALL || eventMode.getValue() == EventMode.POST)) {
            doTrampoline();
        }
    }

    @SubscribeEvent
    public void sendPacket(PacketEvent.Send event) {
        if(event.getPacket() instanceof CPacketPlayer && Freecam.getInstance().isOff()) {
            if ((mode.getValue() == Mode.BOUNCE || mode.getValue() == Mode.NORMAL) && mc.player.getRidingEntity() == null && !mc.gameSettings.keyBindJump.isKeyDown()) {
                final CPacketPlayer packet = event.getPacket();
                if (!EntityUtil.isInLiquid() && EntityUtil.isOnLiquid(0.05f) && EntityUtil.checkCollide() && mc.player.ticksExisted % 3 == 0) {
                    packet.y -= 0.05f;
                }
            }
        }
    }

    @SubscribeEvent
    public void onLiquidCollision(JesusEvent event) {
        if(fullNullCheck() || Freecam.getInstance().isOn()) {
            return;
        }

        if(event.getStage() == 0 && (mode.getValue() == Mode.BOUNCE || mode.getValue() == Mode.VANILLA || mode.getValue() == Mode.NORMAL)) {
            if(mc.world != null && mc.player != null) {
                if (EntityUtil.checkCollide() && !(mc.player.motionY >= 0.1f) && event.getPos().getY() < mc.player.posY - 0.05f) {
                    if (mc.player.getRidingEntity() != null) {
                        event.setBoundingBox(new AxisAlignedBB(0, 0, 0, 1, 1 - 0.05f, 1));
                    } else {
                        event.setBoundingBox(Block.FULL_BLOCK_AABB);
                    }
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceived(PacketEvent.Receive event) {
        if(cancelVehicle .getValue() && event.getPacket() instanceof SPacketMoveVehicle) {
            event.setCanceled(true);
        }
    }

    @Override
    public String getDisplayInfo() {
        if(mode.getValue() == Mode.NORMAL) {
            return null;
        } else {
            return mode.currentEnumName();
        }
    }

    private void doTrampoline() {
        if (mc.player.isSneaking()) {
            return;
        }

        if (EntityUtil.isAboveLiquid(mc.player) && !mc.player.isSneaking() && !mc.gameSettings.keyBindJump.pressed) {
            mc.player.motionY = 0.1;
            return;
        }

        if(mc.player.onGround || mc.player.isOnLadder()) {
            grounded = false;
        }

        if (mc.player.motionY > 0.0) {
            if (mc.player.motionY < 0.03 && grounded) {
                mc.player.motionY += 0.06713;
            }
            else if (mc.player.motionY <= 0.05 && grounded) {
                mc.player.motionY *= 1.20000000999;
                mc.player.motionY += 0.06;
            }
            else if (mc.player.motionY <= 0.08 && grounded) {
                mc.player.motionY *= 1.20000003;
                mc.player.motionY += 0.055;
            }
            else if (mc.player.motionY <= 0.112 && grounded) {
                mc.player.motionY += 0.0535;
            }
            else if (grounded) {
                mc.player.motionY *= 1.000000000002;
                mc.player.motionY += 0.0517;
            }
        }

        if (grounded && mc.player.motionY < 0.0 && mc.player.motionY > -0.3) {
            mc.player.motionY += 0.045835;
        }

        if(!fall.getValue()) {
            mc.player.fallDistance = 0.0f;
        }

        if (!EntityUtil.checkForLiquid(mc.player, true)) {
            return;
        }

        if (EntityUtil.checkForLiquid(mc.player, true)) {
            mc.player.motionY = 0.5;
        }

        grounded = true;
    }

    public enum EventMode {
        PRE,
        POST,
        ALL
    }

    public enum Mode {
        TRAMPOLINE,
        BOUNCE,
        VANILLA,
        NORMAL
    }
}
