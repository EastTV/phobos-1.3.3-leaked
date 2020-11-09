package me.earth.phobos.features.modules.combat;

import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.BlockUtil;
import me.earth.phobos.util.InventoryUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Comparator;
import java.util.List;

public class Crasher extends Module {

    private final Setting<Mode> mode = register(new Setting("Mode", Mode.ONCE));
    private final Setting<Float> placeRange = register(new Setting("PlaceRange", 6.0f, 0.0f, 10.0f));
    private final Setting<Integer> crystals = register(new Setting("Positions", 100, 0, 1000));
    private final Setting<Integer> coolDown = register(new Setting("CoolDown", 400, 0, 1000));
    private final Setting<InventoryUtil.Switch> switchMode = register(new Setting("Switch", InventoryUtil.Switch.NORMAL));
    public Setting<Integer> sort = register(new Setting("Sort", 0, 0, 2));

    private boolean offhand = false;
    private boolean mainhand = false;
    private Timer timer = new Timer();
    private int lastHotbarSlot = -1;
    private boolean switchedItem = false;
    private boolean chinese = false;

    public Crasher() {
        super("CrystalCrash", "Attempts to crash chinese AutoCrystals", Category.COMBAT, false, false, true);
    }

    @Override
    public void onEnable() {
        chinese = false;
        if(fullNullCheck() || !timer.passedMs(coolDown.getValue())) {
            this.disable();
            return;
        }

        lastHotbarSlot = mc.player.inventory.currentItem;
        if(mode.getValue() == Mode.ONCE) {
            placeCrystals();
            this.disable();
        }
    }

    @Override
    public void onDisable() {
        timer.reset();
        if(mode.getValue() == Mode.SPAM) {
            switchItem(true);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if(fullNullCheck() || event.phase == TickEvent.ClientTickEvent.Phase.START || (this.isOff() && (timer.passedMs(10) || mode.getValue() == Mode.SPAM))) {
            return;
        }

        if(mode.getValue() == Mode.SPAM) {
            placeCrystals();
        } else {
            switchItem(true);
        }
    }

    private void placeCrystals() {
        offhand = mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL;
        mainhand = mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL;
        int crystalcount = 0;
        List<BlockPos> blocks = BlockUtil.possiblePlacePositions(placeRange.getValue(), false);
        if (sort.getValue() == 1) {
            blocks.sort(Comparator.comparingDouble(hole -> mc.player.getDistanceSq(hole)));
        } else if (sort.getValue() == 2) {
            blocks.sort(Comparator.comparingDouble(hole -> -mc.player.getDistanceSq(hole)));
        }

        for (BlockPos pos : blocks) {
            if (this.isOff() || crystalcount >= crystals.getValue()) {
                break;
            }
            placeCrystal(pos);
            crystalcount++;
        }
    }

    private void placeCrystal(BlockPos pos) {
        if(!chinese && !mainhand && !offhand) {
            if(!switchItem(false)) {
                this.disable();
                return;
            }
        }
        RayTraceResult result = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(pos.getX() + .5, pos.getY() - .5d, pos.getZ() + .5));
        EnumFacing facing = (result == null || result.sideHit == null) ? EnumFacing.UP : result.sideHit;
        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, facing, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0, 0, 0));
        mc.player.swingArm(EnumHand.MAIN_HAND);
    }

    private boolean switchItem(boolean back) {
        chinese = true;
        if(offhand) {
            return true;
        }
        boolean[] value = InventoryUtil.switchItemToItem(back, lastHotbarSlot, switchedItem, switchMode.getValue(), Items.END_CRYSTAL);
        switchedItem = value[0];
        return value[1];
    }

    public enum Mode {
        ONCE,
        SPAM
    }
}
