package me.earth.phobos.features.modules.player;

import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.UpdateWalkingPlayerEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.InventoryUtil;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEndCrystal;
import net.minecraft.item.ItemExpBottle;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FastPlace extends Module {

    private Setting<Boolean> all = register(new Setting("All", false));
    private Setting<Boolean> obby = register(new Setting("Obsidian", false, v -> !all.getValue()));
    private Setting<Boolean> enderChests = register(new Setting("EnderChests", false, v -> !all.getValue()));
    private Setting<Boolean> crystals = register(new Setting("Crystals", false, v -> !all.getValue()));
    private Setting<Boolean> exp = register(new Setting("Experience", false, v -> !all.getValue()));
    private Setting<Boolean> feetExp = register(new Setting("ExpFeet", false));
    private Setting<Boolean> fastCrystal = register(new Setting("PacketCrystal", false));

    private BlockPos mousePos = null;

    public FastPlace() {
        super("FastPlace", "Fast everything.", Category.PLAYER, true, false, false);
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if(event.getStage() == 0 && feetExp.getValue()) {
            boolean mainHand = mc.player.getHeldItemMainhand().getItem() == Items.EXPERIENCE_BOTTLE;
            boolean offHand = mc.player.getHeldItemOffhand().getItem() == Items.EXPERIENCE_BOTTLE;
            if(mc.gameSettings.keyBindUseItem.isKeyDown() && ((mc.player.getActiveHand() == EnumHand.MAIN_HAND && mainHand) || (mc.player.getActiveHand() == EnumHand.OFF_HAND && offHand))) {
                Phobos.rotationManager.lookAtVec3d(mc.player.getPositionVector());
            }
        }
    }

    @Override
    public void onUpdate() {
        if(fullNullCheck()) {
            return;
        }

        if (InventoryUtil.holdingItem(ItemExpBottle.class) && exp.getValue()) {
            mc.rightClickDelayTimer = 0;
        }

        if (InventoryUtil.holdingItem(BlockObsidian.class) && obby.getValue()) {
            mc.rightClickDelayTimer = 0;
        }

        if (InventoryUtil.holdingItem(BlockEnderChest.class) && enderChests.getValue()) {
            mc.rightClickDelayTimer = 0;
        }

        if (all.getValue()) {
            mc.rightClickDelayTimer = 0;
        }

        if (InventoryUtil.holdingItem(ItemEndCrystal.class) && (crystals.getValue() || all.getValue())) {
            mc.rightClickDelayTimer = 0;
        }

        if(fastCrystal.getValue() && mc.gameSettings.keyBindUseItem.isKeyDown()) {
            boolean offhand = mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL;
            if(offhand || mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL) {
                final RayTraceResult result = mc.objectMouseOver;
                if (result == null) {
                    return;
                }

                switch (result.typeOfHit) {
                    case MISS:
                        mousePos = null;
                        break;
                    case BLOCK:
                        mousePos = mc.objectMouseOver.getBlockPos();
                        break;
                    case ENTITY:
                        if(mousePos != null) {
                            Entity entity = result.entityHit;
                            if(entity != null) {
                                if(mousePos.equals(new BlockPos(entity.posX, entity.posY - 1, entity.posZ))) {
                                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(mousePos, EnumFacing.DOWN, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0, 0, 0));
                                }
                            }
                        }
                        break;
                }
            }
        }
    }
}
