package me.earth.phobos.features.modules.movement;

import me.earth.phobos.event.events.UpdateWalkingPlayerEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.MathUtil;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Objects;

public class TPSpeed extends Module {

    private Setting<Mode> mode = register(new Setting("Mode", Mode.NORMAL));
    private Setting<Double> speed = register(new Setting("Speed", 0.25, 0.1, 10.0));
    private Setting<Double> fallSpeed = register(new Setting("FallSpeed", 0.25, 0.1, 10.0, v -> mode.getValue() == Mode.STEP));
    private Setting<Boolean> turnOff = register(new Setting("Off", false));
    private Setting<Integer> tpLimit = register(new Setting("Limit", 2, 1, 10, v -> turnOff.getValue(), "Turn it off."));

    private int tps = 0;
    private double[] selectedPositions = {0.42D, 0.75D, 1.0D};

    public TPSpeed() {
        super("TpSpeed", "Teleports you.", Category.MOVEMENT, true, false, false);
    }

    @Override
    public void onEnable() {
        tps = 0;
    }

    @SubscribeEvent
    public void onUpdatePlayerWalking(UpdateWalkingPlayerEvent event) {
        if(event.getStage() != 0) {
            return;
        }

        if(mode.getValue() == Mode.NORMAL) {
            if (turnOff.getValue() && tps >= tpLimit.getValue()) {
                this.disable();
                return;
            }

            if (mc.player.moveForward != 0.0f || (mc.player.moveStrafing != 0.0f && mc.player.onGround)) {
                for (double x = 0.0625; x < speed.getValue(); x += 0.262) {
                    final double[] dir = MathUtil.directionSpeed(x);
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + dir[0], mc.player.posY, mc.player.posZ + dir[1], mc.player.onGround));
                }
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + mc.player.motionX, 0.0, mc.player.posZ + mc.player.motionZ, mc.player.onGround));
                tps++;
            }
        } else {
            if ((mc.player.moveForward != 0.0f || mc.player.moveStrafing != 0.0f) && mc.player.onGround) {
                double pawnY = 0;
                final double[] lastStep = MathUtil.directionSpeed(.262);

                for (double x = 0.0625; x < this.speed.getValue(); x += 0.262) {
                    final double[] dir = MathUtil.directionSpeed(x);
                    AxisAlignedBB bb = Objects.requireNonNull(mc.player.getEntityBoundingBox()).offset(dir[0],pawnY,dir[1]);;

                    while(collidesHorizontally(bb)) {
                        for (double position : this.selectedPositions) {
                            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + dir[0] - lastStep[0], mc.player.posY + pawnY + position, mc.player.posZ + dir[1] - lastStep[1], true));
                        }
                        pawnY = pawnY + 1;
                        bb = Objects.requireNonNull(mc.player.getEntityBoundingBox()).offset(dir[0],pawnY,dir[1]);
                    }

                    if (!mc.world.checkBlockCollision(bb.grow(0.0125, 0, 0.0125).offset(0,-1,0))) {
                        for (double i = 0.0d; i <= 1.0d; i += fallSpeed.getValue()) {
                            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + dir[0], mc.player.posY + pawnY - i, mc.player.posZ + dir[1], true));
                        }
                        pawnY -= 1.0;
                    }
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + dir[0], mc.player.posY + pawnY, mc.player.posZ + dir[1], mc.player.onGround));
                }
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + mc.player.motionX, 0.0, mc.player.posZ + mc.player.motionZ, mc.player.onGround));
            }
        }
    }

    private static boolean collidesHorizontally(AxisAlignedBB bb) {
        if (mc.world.collidesWithAnyBlock(bb)) {
            Vec3d center = bb.getCenter();
            BlockPos blockpos = new BlockPos(center.x, bb.minY, center.z);
            return mc.world.isBlockFullCube(blockpos.west()) || mc.world.isBlockFullCube(blockpos.east()) || mc.world.isBlockFullCube(blockpos.north()) || mc.world.isBlockFullCube(blockpos.south()) || mc.world.isBlockFullCube(blockpos);
        } else {
            return false;
        }
    }

    public enum Mode {
        NORMAL,
        STEP
    }
}
