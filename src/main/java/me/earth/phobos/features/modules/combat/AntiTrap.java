package me.earth.phobos.features.modules.combat;

import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.UpdateWalkingPlayerEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.BlockUtil;
import me.earth.phobos.util.EntityUtil;
import me.earth.phobos.util.InventoryUtil;
import me.earth.phobos.util.MathUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEndCrystal;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AntiTrap extends Module {

    public Setting<Rotate> rotate = register(new Setting("Rotate", Rotate.NORMAL));
    private final Setting<Integer> coolDown = register(new Setting("CoolDown", 400, 0, 1000));
    private final Setting<InventoryUtil.Switch> switchMode = register(new Setting("Switch", InventoryUtil.Switch.NORMAL));
    public Setting<Boolean> sortY = register(new Setting("SortY", true));

    private final Vec3d[] surroundTargets = {
            new Vec3d(1, 0, 0),
            new Vec3d(0, 0, 1),
            new Vec3d(-1, 0, 0),
            new Vec3d(0, 0, -1),
            new Vec3d(1, 0, -1),
            new Vec3d(1, 0, 1),
            new Vec3d(-1, 0, -1),
            new Vec3d(-1, 0, 1),
            new Vec3d(1, 1, 0),
            new Vec3d(0, 1, 1),
            new Vec3d(-1, 1, 0),
            new Vec3d(0, 1, -1),
            new Vec3d(1, 1, -1),
            new Vec3d(1, 1, 1),
            new Vec3d(-1, 1, -1),
            new Vec3d(-1, 1, 1)
    };

    private int lastHotbarSlot = -1;
    private boolean switchedItem;
    private boolean offhand = false;
    private final Timer timer = new Timer();

    public AntiTrap() {
        super("AntiTrap", "Places a crystal to prevent you getting trapped.", Category.COMBAT, true, false, false);
    }

    @Override
    public void onEnable() {
        if(fullNullCheck() || !timer.passedMs(coolDown.getValue())) {
            this.disable();
            return;
        }
        lastHotbarSlot = mc.player.inventory.currentItem;
    }

    @Override
    public void onDisable() {
        if(fullNullCheck()) {
            return;
        }
        switchItem(true);
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if(!fullNullCheck() && event.getStage() == 0) {
            doAntiTrap();
        }
    }

    public void doAntiTrap() {
        offhand = mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL;
        if(!offhand && InventoryUtil.findHotbarBlock(ItemEndCrystal.class) == -1) {
            this.disable();
            return;
        }
        lastHotbarSlot = mc.player.inventory.currentItem;
        List<Vec3d> targets = new ArrayList<>();
        Collections.addAll(targets, BlockUtil.convertVec3ds(mc.player.getPositionVector(), surroundTargets));
        EntityPlayer closestPlayer = EntityUtil.getClosestEnemy(6.0);
        if(closestPlayer != null) {
            targets.sort((vec3d, vec3d2) -> Double.compare(closestPlayer.getDistanceSq(vec3d2.x, vec3d2.y, vec3d2.z), closestPlayer.getDistanceSq(vec3d.x, vec3d.y, vec3d.z)));
            if(sortY.getValue()) {
                targets.sort(Comparator.comparingDouble(vec3d -> vec3d.y));
            }
        }

        for(Vec3d vec3d : targets) {
            BlockPos pos = new BlockPos(vec3d);
            if(BlockUtil.canPlaceCrystal(pos)) {
                placeCrystal(pos);
                this.disable();
                break;
            }
        }
    }

    private void placeCrystal(BlockPos pos) {
        boolean mainhand = mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL;
        if(!mainhand && !offhand) {
            if(!switchItem(false)) {
                this.disable();
                return;
            }
        }
        RayTraceResult result = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(pos.getX() + .5, pos.getY() - .5d, pos.getZ() + .5));
        EnumFacing facing = (result == null || result.sideHit == null) ? EnumFacing.UP : result.sideHit;
        final float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(pos.getX() + 0.5f, pos.getY() - 0.5f, pos.getZ() + 0.5f));
        switch(rotate.getValue()) {
            case NONE:
                break;
            case NORMAL:
                Phobos.rotationManager.setPlayerRotations(angle[0], angle[1]);
                break;
            case PACKET:
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(angle[0], MathHelper.normalizeAngle((int) angle[1], 360), mc.player.onGround));
                break;
            default:
        }
        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, facing, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0, 0, 0));
        mc.player.swingArm(EnumHand.MAIN_HAND);
        timer.reset();
    }

    private boolean switchItem(boolean back) {
        if(offhand) {
            return true;
        }
        boolean[] value = InventoryUtil.switchItemToItem(back, lastHotbarSlot, switchedItem, switchMode.getValue(), Items.END_CRYSTAL);
        switchedItem = value[0];
        return value[1];
    }

    public enum Rotate {
        NONE,
        NORMAL,
        PACKET
    }

}
