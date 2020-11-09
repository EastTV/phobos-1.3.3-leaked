package me.earth.phobos.features.modules.movement;

import me.earth.phobos.Phobos;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class Static extends Module {

    private final Setting<Mode> mode = register(new Setting("Mode", Mode.ROOF));
    private final Setting<Boolean> disabler = register(new Setting("Disable", true, v -> mode.getValue() == Mode.ROOF));
    private final Setting<Boolean> ySpeed = register(new Setting("YSpeed", false, v -> mode.getValue() == Mode.STATIC));
    private final Setting<Float> speed = register(new Setting("Speed", 0.1f, 0.0f, 10.0f, v -> ySpeed.getValue() && mode.getValue() == Mode.STATIC));
    private final Setting<Float> height = register(new Setting("Height", 3.0f, 0.0f, 256.0f, v -> mode.getValue() == Mode.NOVOID));

    public Static() {
        super("Static", "Stops any movement. Glitches you up.", Category.MOVEMENT, false, false, false);
    }

    @Override
    public void onUpdate() {
        if(fullNullCheck()) {
            return;
        }

        switch(mode.getValue()) {
            case STATIC:
                mc.player.capabilities.isFlying = false;
                mc.player.motionX = 0;
                mc.player.motionY = 0;
                mc.player.motionZ = 0;

                if(ySpeed.getValue()) {
                    mc.player.jumpMovementFactor = speed.getValue();
                    if (mc.gameSettings.keyBindJump.isKeyDown()) {
                        mc.player.motionY += speed.getValue();
                    }
                    if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                        mc.player.motionY -= speed.getValue();
                    }
                }
                break;
            case ROOF:
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX,10000, mc.player.posZ, mc.player.onGround));
                if(disabler.getValue()) {
                    this.disable();
                }
                break;
            case NOVOID:
                if(!mc.player.noClip && mc.player.posY <= height.getValue()) {
                    final RayTraceResult trace = mc.world.rayTraceBlocks(mc.player.getPositionVector(), new Vec3d(mc.player.posX, 0, mc.player.posZ), false, false, false);
                    if (trace != null && trace.typeOfHit == RayTraceResult.Type.BLOCK) {
                        return;
                    }

                    if (Phobos.moduleManager.isModuleEnabled(Phase.class) || Phobos.moduleManager.isModuleEnabled(Flight.class)) {
                        return;
                    }

                    mc.player.setVelocity(0, 0, 0);
                    if (mc.player.getRidingEntity() != null) {
                        mc.player.getRidingEntity().setVelocity(0, 0, 0);
                    }
                }
                break;
        }
    }

    @Override
    public String getDisplayInfo() {
        if(mode.getValue() == Mode.ROOF) {
            return "Roof";
        } else if(mode.getValue() == Mode.NOVOID) {
            return "NoVoid";
        } else {
            return null;
        }
    }

    public enum Mode {
        STATIC,
        ROOF,
        NOVOID
    }
}
