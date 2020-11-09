package me.earth.phobos.features.modules.render;

import me.earth.phobos.event.events.Render3DEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.ColorUtil;
import me.earth.phobos.util.MathUtil;
import me.earth.phobos.util.RenderUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.math.BlockPos;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class StorageESP extends Module {

    private final Setting<Float> range = register(new Setting("Range", 50.0f, 1.0f, 300.0f));
    private final Setting<Boolean> chest = register(new Setting("Chest", true));
    private final Setting<Boolean> dispenser = register(new Setting("Dispenser", false));
    private final Setting<Boolean> shulker = register(new Setting("Shulker", true));
    private final Setting<Boolean> echest = register(new Setting("Ender Chest", true));
    private final Setting<Boolean> furnace = register(new Setting("Furnace", false));
    private final Setting<Boolean> hopper = register(new Setting("Hopper", false));
    private final Setting<Boolean> cart = register(new Setting("Minecart", false));
    private final Setting<Boolean> frame = register(new Setting("Item Frame", false));
    private final Setting<Boolean> box = register(new Setting("Box", false));
    private final Setting<Integer> boxAlpha = register(new Setting("BoxAlpha", 125, 0, 255, v -> box.getValue()));
    private final Setting<Boolean> outline = register(new Setting("Outline", true));
    private final Setting<Float> lineWidth = register(new Setting("LineWidth", 1.0f, 0.1f, 5.0f, v -> outline.getValue()));

    public StorageESP() {
        super("StorageESP", "Highlights Containers.", Category.RENDER, false, false, false);
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        Map<BlockPos, Integer> positions = new HashMap<>();
        for (TileEntity tileEntity : mc.world.loadedTileEntityList) {
            if ((tileEntity instanceof TileEntityChest && chest.getValue()) || (tileEntity instanceof TileEntityDispenser && dispenser.getValue()) || (tileEntity instanceof TileEntityShulkerBox && shulker.getValue()) || (tileEntity instanceof TileEntityEnderChest && echest.getValue()) || (tileEntity instanceof TileEntityFurnace && furnace.getValue()) || (tileEntity instanceof TileEntityHopper && hopper.getValue())) {
                BlockPos pos = tileEntity.getPos();
                if(mc.player.getDistanceSq(pos) <= MathUtil.square(range.getValue())) {
                    int color = getTileEntityColor(tileEntity);
                    if (color != -1) {
                        positions.put(pos, color);
                    }
                }
            }
        }

        for (Entity entity : mc.world.loadedEntityList) {
            if ((entity instanceof EntityItemFrame && frame.getValue()) || (entity instanceof EntityMinecartChest && cart.getValue())) {
                BlockPos pos = entity.getPosition();
                if(mc.player.getDistanceSq(pos) <= MathUtil.square(range.getValue())) {
                    int color = getEntityColor(entity);
                    if (color != -1) {
                        positions.put(pos, color);
                    }
                }
            }
        }

        for(Map.Entry<BlockPos, Integer> entry : positions.entrySet()) {
            BlockPos blockPos = entry.getKey();
            int color = entry.getValue();
            RenderUtil.drawBoxESP(blockPos, new Color(color), false, new Color(color), lineWidth.getValue(), outline.getValue(), box.getValue(), boxAlpha.getValue(), false);
        }
    }

    /*private int changeAlpha(int origColor) {
        origColor = origColor & 0x00ffffff; //drop the previous alpha value
        return (100 << 24) | origColor; //add the one the user inputted
    }*/

    private int getTileEntityColor(TileEntity tileEntity) {
        if (tileEntity instanceof TileEntityChest) {
            //return ColorUtil.Colors.WHITE;
            return ColorUtil.Colors.BLUE;
        } else if (tileEntity instanceof TileEntityShulkerBox) {
            return ColorUtil.Colors.RED;
        } else if (tileEntity instanceof TileEntityEnderChest) {
            return ColorUtil.Colors.PURPLE;
        } else if (tileEntity instanceof TileEntityFurnace) {
            return ColorUtil.Colors.GRAY;
        } else if (tileEntity instanceof TileEntityHopper) {
            return ColorUtil.Colors.DARK_RED;
        } else if (tileEntity instanceof TileEntityDispenser) {
            return ColorUtil.Colors.ORANGE;
        } else {
            return -1;
        }
    }

    private int getEntityColor(Entity entity) {
        if (entity instanceof EntityMinecartChest) {
            return ColorUtil.Colors.ORANGE;
        } else if (entity instanceof EntityItemFrame && ((EntityItemFrame) entity).getDisplayedItem().getItem() instanceof ItemShulkerBox) {
            return ColorUtil.Colors.YELLOW;
        } else if (entity instanceof EntityItemFrame && (!(((EntityItemFrame) entity).getDisplayedItem().getItem() instanceof ItemShulkerBox))) {
            return ColorUtil.Colors.ORANGE;
        } else {
            return -1;
        }
    }

}
