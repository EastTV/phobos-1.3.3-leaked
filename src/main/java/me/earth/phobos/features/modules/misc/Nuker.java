package me.earth.phobos.features.modules.misc;

import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.BlockEvent;
import me.earth.phobos.event.events.UpdateWalkingPlayerEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.BlockUtil;
import me.earth.phobos.util.MathUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

//TODO: SignNuker + BannerNuker, Shulker and HopperNuker multiple per tick.
public class Nuker extends Module {

    public Setting<Boolean> rotate = register(new Setting("Rotate", false));
    public Setting<Float> distance = register(new Setting("Range", 6.0f, 0.1f, 10.0f));
    public Setting<Integer> blockPerTick = register(new Setting("Blocks/Attack", 50, 1, 100));
    public Setting<Integer> delay = register(new Setting("Delay/Attack", 50, 1, 1000));
    public Setting<Boolean> nuke = register(new Setting("Nuke", false));
    public Setting<Mode> mode = register(new Setting("Mode", Mode.NUKE, v -> nuke.getValue()));
    public Setting<Boolean> antiRegear = register(new Setting("AntiRegear", false));
    public Setting<Boolean> hopperNuker = register(new Setting("HopperAura", false));
    //public Setting<Boolean> signNuker = register(new Setting("SignAura", false));
    //public Setting<Boolean> bannerNuker = register(new Setting("BannerAura", false));
    private Setting<Boolean> autoSwitch = register(new Setting("AutoSwitch", false));

    private int oldSlot = -1;
    private boolean isMining = false;
    private final Timer timer = new Timer();
    private Block selected;

    public Nuker() {
        super("Nuker", "Mines many blocks", Category.MISC, true, false, false);
    }

    @Override
    public void onToggle() {
        this.selected = null;
    }

    @SubscribeEvent
    public void onClickBlock(BlockEvent event) {
        if(event.getStage() == 3 && (this.mode.getValue() == Mode.SELECTION || this.mode.getValue() == Mode.NUKE)) {
            final Block block = mc.world.getBlockState(event.pos).getBlock();
            if (block != null && block != this.selected) {
                this.selected = block;
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayerPre(UpdateWalkingPlayerEvent event) {
        if(event.getStage() == 0) {
            if(nuke.getValue()) {
                BlockPos pos = null;
                switch (this.mode.getValue()) {
                    case SELECTION:
                    case NUKE:
                        pos = this.getClosestBlockSelection();
                        break;
                    case ALL:
                        pos = this.getClosestBlockAll();
                        break;
                    default:
                }

                if (pos != null) {
                    if (this.mode.getValue() == Mode.SELECTION || this.mode.getValue() == Mode.ALL) {
                        if (rotate.getValue()) {
                            final float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f));
                            Phobos.rotationManager.setPlayerRotations(angle[0], angle[1]);
                        }
                        if (canBreak(pos)) {
                            mc.playerController.onPlayerDamageBlock(pos, mc.player.getHorizontalFacing());
                            mc.player.swingArm(EnumHand.MAIN_HAND);
                        }
                    } else {
                        for (int i = 0; i < blockPerTick.getValue(); i++) {

                            pos = this.getClosestBlockSelection();

                            if (pos != null) {
                                if (rotate.getValue()) {
                                    final float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f));
                                    Phobos.rotationManager.setPlayerRotations(angle[0], angle[1]);
                                }

                                if (timer.passedMs(delay.getValue())) {
                                    mc.playerController.onPlayerDamageBlock(pos, mc.player.getHorizontalFacing());
                                    mc.player.swingArm(EnumHand.MAIN_HAND);
                                    timer.reset();
                                }
                            }
                        }
                    }
                }
            }

            if(antiRegear.getValue()) {
                breakBlocks(BlockUtil.shulkerList);
            }

            if(hopperNuker.getValue()) {
                List<Block> blocklist = new ArrayList<>();
                blocklist.add(Blocks.HOPPER);
                breakBlocks(blocklist);
            }
        }
    }

    public void breakBlocks(List<Block> blocks) {
        BlockPos pos = getNearestBlock(blocks);
        if (pos != null) {
            if (!isMining) {
                oldSlot = mc.player.inventory.currentItem;
                isMining = true;
            }

            if (rotate.getValue()) {
                float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d((pos.getX() + 0.5F), (pos.getY() + 0.5F), (pos.getZ() + 0.5F)));
                Phobos.rotationManager.setPlayerRotations(angle[0], angle[1]);
            }

            if (canBreak(pos)) {
                if (autoSwitch.getValue()) {
                    int newSlot = -1;
                    for (int i = 0; i < 9; i++) {
                        ItemStack stack = mc.player.inventory.getStackInSlot(i);

                        if (stack == ItemStack.EMPTY) {
                            continue;
                        }

                        if ((stack.getItem() instanceof ItemPickaxe)) {
                            newSlot = i;
                            break;
                        }

                    }

                    if (newSlot != -1) {
                        mc.player.inventory.currentItem = newSlot;
                    }
                }
                mc.playerController.onPlayerDamageBlock(pos, mc.player.getHorizontalFacing());
                mc.player.swingArm(EnumHand.MAIN_HAND);
            }
        } else {
            if (autoSwitch.getValue() && oldSlot != -1) {
                mc.player.inventory.currentItem = oldSlot;
                oldSlot = -1;
                isMining = false;
            }
        }
    }

    private boolean canBreak(BlockPos pos) {
        final IBlockState blockState = mc.world.getBlockState(pos);
        final Block block = blockState.getBlock();
        return block.getBlockHardness(blockState, mc.world, pos) != -1;
    }

    private BlockPos getNearestBlock(List<Block> blocks) {
        double maxDist = MathUtil.square(this.distance.getValue());
        BlockPos ret = null;
        double x;
        for (x = maxDist; x >= -maxDist; x--) {
            double y;
            for (y = maxDist; y >= -maxDist; y--) {
                double z;
                for (z = maxDist; z >= -maxDist; z--) {
                    BlockPos pos = new BlockPos(mc.player.posX + x, mc.player.posY + y, mc.player.posZ + z);
                    double dist = mc.player.getDistanceSq(pos.getX(), pos.getY(), pos.getZ());
                    if (dist <= maxDist && blocks.contains(mc.world.getBlockState(pos).getBlock()) && canBreak(pos)) {
                        maxDist = dist;
                        ret = pos;
                    }
                }
            }
        }
        return ret;
    }

    private BlockPos getClosestBlockAll() {
        float maxDist = this.distance.getValue();
        BlockPos ret = null;
        for (float x = maxDist; x >= -maxDist; x--) {
            for (float y = maxDist; y >= -maxDist; y--) {
                for (float z = maxDist; z >= -maxDist; z--) {
                    final BlockPos pos = new BlockPos(mc.player.posX + x, mc.player.posY + y, mc.player.posZ + z);
                    final double dist = mc.player.getDistance(pos.getX(), pos.getY(), pos.getZ());
                    if (dist <= maxDist && (mc.world.getBlockState(pos).getBlock() != Blocks.AIR && !(mc.world.getBlockState(pos).getBlock() instanceof BlockLiquid)) && canBreak(pos)) {
                        if (pos.getY() >= mc.player.posY) {
                            maxDist = (float) dist;
                            ret = pos;
                        }
                    }
                }
            }
        }

        return ret;
    }

    private BlockPos getClosestBlockSelection() {
        float maxDist = this.distance.getValue();
        BlockPos ret = null;
        for (float x = maxDist; x >= -maxDist; x--) {
            for (float y = maxDist; y >= -maxDist; y--) {
                for (float z = maxDist; z >= -maxDist; z--) {
                    final BlockPos pos = new BlockPos(mc.player.posX + x, mc.player.posY + y, mc.player.posZ + z);
                    final double dist = mc.player.getDistance(pos.getX(), pos.getY(), pos.getZ());
                    if (dist <= maxDist && (mc.world.getBlockState(pos).getBlock() != Blocks.AIR && !(mc.world.getBlockState(pos).getBlock() instanceof BlockLiquid)) && mc.world.getBlockState(pos).getBlock() == this.selected && canBreak(pos)) {
                        if (pos.getY() >= mc.player.posY) {
                            maxDist = (float) dist;
                            ret = pos;
                        }
                    }
                }
            }
        }

        return ret;
    }

    public enum Mode {
        SELECTION,
        ALL,
        NUKE
    }
}
