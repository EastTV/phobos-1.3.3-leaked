package me.earth.phobos.manager;

import me.earth.phobos.features.Feature;
import me.earth.phobos.features.modules.client.Managers;
import me.earth.phobos.features.modules.combat.HoleFiller;
import me.earth.phobos.features.modules.movement.HoleTP;
import me.earth.phobos.features.modules.render.HoleESP;
import me.earth.phobos.util.BlockUtil;
import me.earth.phobos.util.EntityUtil;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HoleManager extends Feature {

    private static final BlockPos[] surroundOffset = BlockUtil.toBlockPos(EntityUtil.getOffsets(0, true));
    private List<BlockPos> holes = new ArrayList<>();
    private List<BlockPos> midSafety = new ArrayList<>();

    public void update() {
        if(!fullNullCheck() && (HoleESP.getInstance().isOn() || HoleFiller.getInstance().isOn() || HoleTP.getInstance().isOn())) {
            holes = calcHoles();
        }
    }

    public List<BlockPos> getHoles() {
        return this.holes;
    }

    public List<BlockPos> getMidSafety() {
        return this.midSafety;
    }

    public List<BlockPos> getSortedHoles() {
        this.holes.sort(Comparator.comparingDouble(hole -> mc.player.getDistanceSq(hole)));
        return getHoles();
    }

    public List<BlockPos> calcHoles() {
        List<BlockPos> safeSpots = new ArrayList<>();
        midSafety.clear();
        List<BlockPos> positions = BlockUtil.getSphere(EntityUtil.getPlayerPos(mc.player), Managers.getInstance().holeRange.getValue(), Managers.getInstance().holeRange.getValue().intValue(), false, true, 0);
        for (BlockPos pos : positions) {
            if(!mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR)) {
                continue;
            }

            if(!mc.world.getBlockState(pos.add(0, 1, 0)).getBlock().equals(Blocks.AIR)) {
                continue;
            }

            if(!mc.world.getBlockState(pos.add(0, 2, 0)).getBlock().equals(Blocks.AIR)) {
                continue;
            }

            boolean isSafe = true;
            boolean midSafe = true;
            for(BlockPos offset : surroundOffset) {
                Block block = mc.world.getBlockState(pos.add(offset)).getBlock();
                if(BlockUtil.isBlockUnSolid(block)) {
                    midSafe = false;
                }

                if(block != Blocks.BEDROCK && block != Blocks.OBSIDIAN && block != Blocks.ENDER_CHEST && block != Blocks.ANVIL) {
                    isSafe = false;
                }
            }

            if (isSafe) {
                safeSpots.add(pos);
            }

            if(midSafe) {
                midSafety.add(pos);
            }
        }
        return safeSpots;
    }

    public boolean isSafe(BlockPos pos) {
        boolean isSafe = true;
        for(BlockPos offset : surroundOffset) {
            Block block = mc.world.getBlockState(pos.add(offset)).getBlock();
            if (block != Blocks.BEDROCK) {
                isSafe = false;
                break;
            }
        }
        return isSafe;
    }
}
