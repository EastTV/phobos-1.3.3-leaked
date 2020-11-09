package me.earth.phobos.features.modules.movement;

import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.UpdateWalkingPlayerEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.EntityUtil;
import me.earth.phobos.util.MathUtil;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

public class HoleTP extends Module {

    private final Setting<Mode> mode = register(new Setting("Mode", Mode.TELEPORT));
    private final Setting<Float> range = register(new Setting("Range", 1.0f, 0.0f, 5.0f, v -> mode.getValue() == Mode.TELEPORT));
    private final Setting<Boolean> setY = register(new Setting("SetY", false, v -> mode.getValue() == Mode.TELEPORT));
    private final Setting<Float> xOffset = register(new Setting("XOffset", 0.5f, 0.0f, 1.0f, v -> mode.getValue() == Mode.TELEPORT));
    private final Setting<Float> yOffset = register(new Setting("YOffset", 0.0f, 0.0f, 1.0f, v -> mode.getValue() == Mode.TELEPORT));
    private final Setting<Float> zOffset = register(new Setting("ZOffset", 0.5f, 0.0f, 1.0f, v -> mode.getValue() == Mode.TELEPORT));

    private static HoleTP INSTANCE = new HoleTP();

    private final double[] oneblockPositions = new double[]{0.42D, 0.75D};
    private int packets;
    private boolean jumped = false;

    public HoleTP() {
        super("HoleTP", "Teleports you in a hole.", Category.MOVEMENT, true, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static HoleTP getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HoleTP();
        }
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        if (mode.getValue() == Mode.TELEPORT) {
            Phobos.holeManager.update();
            List<BlockPos> holes = Phobos.holeManager.getSortedHoles();
            if (!holes.isEmpty()) {
                BlockPos pos = holes.get(0);
                if (mc.player.getDistanceSq(pos) <= MathUtil.square(range.getValue())) {
                    Phobos.positionManager.setPositionPacket(pos.getX() + xOffset.getValue(), (setY.getValue() ? pos.getY() : mc.player.posY) + yOffset.getValue(), pos.getZ() + zOffset.getValue(), setY.getValue() && yOffset.getValue() == 0.0f, true, true);
                }
            }
            this.disable();
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (event.getStage() == 1 && mode.getValue() == Mode.STEP && (!Phobos.moduleManager.isModuleEnabled(Speed.class) || Phobos.moduleManager.getModuleByClass(Speed.class).mode.getValue() == Speed.Mode.INSTANT) && !Phobos.moduleManager.isModuleEnabled(Strafe.class)) {
            if (!mc.player.onGround) {
                if (mc.gameSettings.keyBindJump.isKeyDown())
                    jumped = true;
            } else jumped = false;
            if (!jumped && mc.player.fallDistance < 0.5 && isInHole() && mc.player.posY - getNearestBlockBelow() <= 1.125 && mc.player.posY - getNearestBlockBelow() <= 0.95 && !EntityUtil.isOnLiquid() && !EntityUtil.isInLiquid()) {
                if (!mc.player.onGround) {
                    this.packets++;
                }
                if (!mc.player.onGround && !mc.player.isInsideOfMaterial(Material.WATER) && !mc.player.isInsideOfMaterial(Material.LAVA) && !mc.gameSettings.keyBindJump.isKeyDown() && !mc.player.isOnLadder() && this.packets > 0) {
                    final BlockPos blockPos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
                    for (double position : this.oneblockPositions) {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(blockPos.getX() + 0.5f, mc.player.posY - position, blockPos.getZ() + 0.5f, true));
                    }
                    mc.player.setPosition(blockPos.getX() + 0.5f, getNearestBlockBelow() + 0.1, blockPos.getZ() + 0.5f);
                    this.packets = 0;
                }
            }
        }
    }

    private boolean isInHole() {
        BlockPos blockPos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
        IBlockState blockState = mc.world.getBlockState(blockPos);
        return this.isBlockValid(blockState, blockPos);
    }

    private double getNearestBlockBelow() {
        for (double y = mc.player.posY; y > 0.0D; y -= 0.001D) {
            if (!(mc.world.getBlockState(new BlockPos(mc.player.posX, y, mc.player.posZ)).getBlock() instanceof BlockSlab) && mc.world.getBlockState(new BlockPos(mc.player.posX, y, mc.player.posZ)).getBlock().getDefaultState().getCollisionBoundingBox(mc.world, new BlockPos(0, 0, 0)) != null) {
                return y;
            }
        }
        return -1.0D;
    }

    private boolean isBlockValid(IBlockState blockState, BlockPos blockPos) {
        if (blockState.getBlock() != Blocks.AIR) {
            return false;
        } else if (mc.player.getDistanceSq(blockPos) < 1.0D) {
            return false;
        } else if (mc.world.getBlockState(blockPos.up()).getBlock() != Blocks.AIR) {
            return false;
        } else if (mc.world.getBlockState(blockPos.up(2)).getBlock() != Blocks.AIR) {
            return false;
        } else {
            return this.isBedrockHole(blockPos) || this.isObbyHole(blockPos) || this.isBothHole(blockPos) || this.isElseHole(blockPos);
        }
    }

    private boolean isObbyHole(BlockPos blockPos) {
        BlockPos[] touchingBlocks = new BlockPos[]{blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west(), blockPos.down()};
        for (BlockPos pos : touchingBlocks) {
            IBlockState touchingState = mc.world.getBlockState(pos);
            if (touchingState.getBlock() == Blocks.AIR || touchingState.getBlock() != Blocks.OBSIDIAN) {
                return false;
            }
        }

        return true;
    }

    private boolean isBedrockHole(BlockPos blockPos) {
        BlockPos[] touchingBlocks = new BlockPos[]{blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west(), blockPos.down()};
        for (BlockPos pos : touchingBlocks) {
            IBlockState touchingState = mc.world.getBlockState(pos);
            if (touchingState.getBlock() == Blocks.AIR || touchingState.getBlock() != Blocks.BEDROCK) {
                return false;
            }
        }

        return true;
    }

    private boolean isBothHole(BlockPos blockPos) {
        BlockPos[] touchingBlocks = new BlockPos[]{blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west(), blockPos.down()};
        for (BlockPos pos : touchingBlocks) {
            IBlockState touchingState = mc.world.getBlockState(pos);
            if (touchingState.getBlock() == Blocks.AIR || touchingState.getBlock() != Blocks.BEDROCK && touchingState.getBlock() != Blocks.OBSIDIAN) {
                return false;
            }
        }
        return true;
    }

    private boolean isElseHole(BlockPos blockPos) {
        BlockPos[] touchingBlocks = new BlockPos[]{blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west(), blockPos.down()};
        for (BlockPos pos : touchingBlocks) {
            IBlockState touchingState = mc.world.getBlockState(pos);
            if (touchingState.getBlock() == Blocks.AIR || !touchingState.isFullBlock()) {
                return false;
            }
        }
        return true;
    }

    public enum Mode {
        TELEPORT,
        STEP
    }
}
