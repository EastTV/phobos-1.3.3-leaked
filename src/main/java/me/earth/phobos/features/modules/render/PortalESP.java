package me.earth.phobos.features.modules.render;

import me.earth.phobos.event.events.Render3DEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.ArrayList;

/**
 * Made for Phobos
 *
 * @author oHare
 * @since 6/29/2020
 **/
public class PortalESP extends Module {
    private int cooldownTicks;
    private final ArrayList<BlockPos> blockPosArrayList = new ArrayList<>();
    private final Setting<Integer> distance = register(new Setting("Distance", 60, 10, 100));
    private final Setting<Boolean> box = register(new Setting("Box", false));
    private final Setting<Integer> boxAlpha = register(new Setting("BoxAlpha", 125, 0, 255, v -> box.getValue()));
    private final Setting<Boolean> outline = register(new Setting("Outline", true));
    private final Setting<Float> lineWidth = register(new Setting("LineWidth", 1.0f, 0.1f, 5.0f, v -> outline.getValue()));
    public PortalESP() {
        super("PortalESP", "Draws portals", Category.RENDER, true, false, false);
    }

    @SubscribeEvent
    public void onTickEvent(TickEvent.ClientTickEvent event) {
        if (mc.world == null) {
            return;
        }
        if (cooldownTicks < 1) {
            blockPosArrayList.clear();
            compileDL();
            cooldownTicks = 80;
        }
        --cooldownTicks;
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (this.mc.world == null) {
            return;
        }
        for (final BlockPos pos : blockPosArrayList) {
            RenderUtil.drawBoxESP(pos, new Color(204, 0, 153, 255), false, new Color(204, 0, 153, 255), lineWidth.getValue(), outline.getValue(), box.getValue(), boxAlpha.getValue(), false);
        }
    }

    private void compileDL() {
        if (mc.world == null || mc.player == null) {
            return;
        }
        for (int x = (int) mc.player.posX - distance.getValue(); x <= (int) mc.player.posX + distance.getValue(); ++x) {
            for (int y = (int) mc.player.posZ - distance.getValue(); y <= (int) mc.player.posZ + distance.getValue(); ++y) {
                for (int z = (int) Math.max(mc.player.posY - distance.getValue(), 0.0f); z <= Math.min(mc.player.posY + distance.getValue(), 255); ++z) {
                    final BlockPos pos = new BlockPos(x, y, z);
                    final Block block = mc.world.getBlockState(pos).getBlock();
                    if (block == Blocks.PORTAL) {
                        blockPosArrayList.add(pos);
                    }
                }
            }
        }
    }
}
