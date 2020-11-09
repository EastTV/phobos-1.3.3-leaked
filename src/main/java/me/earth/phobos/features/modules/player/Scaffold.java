package me.earth.phobos.features.modules.player;

import me.earth.phobos.event.events.UpdateWalkingPlayerEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.BlockUtil;
import me.earth.phobos.util.EntityUtil;
import me.earth.phobos.util.InventoryUtil;
import me.earth.phobos.util.MathUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Scaffold extends Module {

    public Setting<Boolean> rotation = register(new Setting("Rotate", false));

    private final Timer timer = new Timer();

    public Scaffold() {
        super("Scaffold", "Places Blocks underneath you.", Category.PLAYER, true, false, false);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayerPost(UpdateWalkingPlayerEvent event) {
        if(this.isOff() || fullNullCheck() || event.getStage() == 0) {
            return;
        }

        if(!mc.gameSettings.keyBindJump.isKeyDown()) {
            timer.reset();
        }

        BlockPos playerBlock = EntityUtil.getPlayerPosWithEntity();
        if (BlockUtil.isScaffoldPos(playerBlock.add(0, -1, 0))) {
            if(BlockUtil.isValidBlock(playerBlock.add(0, -2, 0))) {
                this.place(playerBlock.add(0, -1, 0), EnumFacing.UP);
            } else if (BlockUtil.isValidBlock(playerBlock.add(-1, -1, 0))) {
                this.place(playerBlock.add(0, -1, 0), EnumFacing.EAST);
            } else if (BlockUtil.isValidBlock(playerBlock.add(1, -1, 0))) {
                this.place(playerBlock.add(0, -1, 0), EnumFacing.WEST);
            } else if (BlockUtil.isValidBlock(playerBlock.add(0, -1, -1))) {
                this.place(playerBlock.add(0, -1, 0), EnumFacing.SOUTH);
            } else if (BlockUtil.isValidBlock(playerBlock.add(0, -1, 1))) {
                this.place(playerBlock.add(0, -1, 0), EnumFacing.NORTH);
            } else if (BlockUtil.isValidBlock(playerBlock.add(1, -1, 1))) {
                if (BlockUtil.isValidBlock(playerBlock.add(0, -1, 1))) {
                    this.place(playerBlock.add(0, -1, 1), EnumFacing.NORTH);
                }
                this.place(playerBlock.add(1, -1, 1), EnumFacing.EAST);
            } else if (BlockUtil.isValidBlock(playerBlock.add(-1, -1, 1))) {
                if (BlockUtil.isValidBlock(playerBlock.add(-1, -1, 0))) {
                    this.place(playerBlock.add(0, -1, 1), EnumFacing.WEST);
                }
                this.place(playerBlock.add(-1, -1, 1), EnumFacing.SOUTH);
            } else if (BlockUtil.isValidBlock(playerBlock.add(1, -1, 1))) {
                if (BlockUtil.isValidBlock(playerBlock.add(0, -1, 1))) {
                    this.place(playerBlock.add(0, -1, 1), EnumFacing.SOUTH);
                }
                this.place(playerBlock.add(1, -1, 1), EnumFacing.WEST);
            } else if (BlockUtil.isValidBlock(playerBlock.add(1, -1, 1))) {
                if (BlockUtil.isValidBlock(playerBlock.add(0, -1, 1))) {
                    this.place(playerBlock.add(0, -1, 1), EnumFacing.EAST);
                }
                this.place(playerBlock.add(1, -1, 1), EnumFacing.NORTH);
            }
        }
    }

    public void place(BlockPos posI, EnumFacing face) {
        BlockPos pos = posI;
        if (face == EnumFacing.UP) {
            pos = pos.add(0, -1, 0);
        } else if (face == EnumFacing.NORTH) {
            pos = pos.add(0, 0, 1);
        } else if (face == EnumFacing.SOUTH) {
            pos = pos.add(0, 0, -1);
        } else if (face == EnumFacing.EAST) {
            pos = pos.add(-1, 0, 0);
        } else if (face == EnumFacing.WEST) {
            pos = pos.add(1, 0, 0);
        }
        final int oldSlot = mc.player.inventory.currentItem;
        int newSlot = -1;
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (!InventoryUtil.isNull(stack) && stack.getItem() instanceof ItemBlock && Block.getBlockFromItem(stack.getItem()).getDefaultState().isFullBlock()) {
                newSlot = i;
                break;
            }
        }

        if (newSlot == -1) {
            return;
        }

        boolean crouched = false;
        if(!mc.player.isSneaking()) {
            Block block = mc.world.getBlockState(pos).getBlock();
            if(BlockUtil.blackList.contains(block)) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                crouched = true;
            }
        }

        if(!(mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock)) {
            mc.player.connection.sendPacket(new CPacketHeldItemChange(newSlot));
            mc.player.inventory.currentItem = newSlot;
            mc.playerController.updateController();
        }

        if(mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.player.motionX *= 0.3;
            mc.player.motionZ *= 0.3;
            mc.player.jump();
            if (timer.passedMs(1500)) {
                mc.player.motionY = -0.28;
                timer.reset();
            }
        }

        if(rotation.getValue()) {
            final float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(pos.getX() + 0.5f, pos.getY() - 0.5f, pos.getZ() + 0.5f));
            mc.player.connection.sendPacket(new CPacketPlayer.Rotation(angle[0], MathHelper.normalizeAngle((int) angle[1], 360), mc.player.onGround));
        }
        mc.playerController.processRightClickBlock(mc.player, mc.world, pos, face, new Vec3d(0.5, 0.5, 0.5), EnumHand.MAIN_HAND);
        mc.player.swingArm(EnumHand.MAIN_HAND);
        mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));
        mc.player.inventory.currentItem = oldSlot;
        mc.playerController.updateController();

        if(crouched) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        }
    }
}
