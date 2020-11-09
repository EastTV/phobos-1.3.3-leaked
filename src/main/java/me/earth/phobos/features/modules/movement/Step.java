package me.earth.phobos.features.modules.movement;

import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.material.Material;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class Step extends Module {

    public Setting<Integer> stepHeight = register(new Setting("Height", 2, 1, 4));
    public Setting<Boolean> spoof = register(new Setting("Spoof", true));
    public Setting<Integer> ticks = register(new Setting("Delay", 3, 0, 25, v -> spoof.getValue(), "Amount of ticks you want to spoof."));
    public Setting<Boolean> turnOff = register(new Setting("Disable", false));
    public Setting<Boolean> small = register(new Setting("Offset", false, v -> stepHeight.getValue() > 1));

    private final double[] oneblockPositions = { 0.42D, 0.75D };
    private final double[] twoblockPositions = { 0.4D, 0.75D, 0.5D, 0.41D, 0.83D, 1.16D, 1.41D, 1.57D, 1.58D, 1.42D };
    private final double[] futurePositions = { 0.42D, 0.78D, 0.63D, 0.51D, 0.9D, 1.21D, 1.45D, 1.43D };
    final double[] twoFiveOffset = { 0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 1.907 };
    private final double[] threeBlockPositions = { 0.42D, 0.78D, 0.63D, 0.51D, 0.9D, 1.21D, 1.45D, 1.43D, 1.78D, 1.63D, 1.51D, 1.9D, 2.21D, 2.45D, 2.43D };
    private final double[] fourBlockPositions = { 0.42D, 0.78D, 0.63D, 0.51D, 0.9D, 1.21D, 1.45D, 1.43D, 1.78D, 1.63D, 1.51D, 1.9D, 2.21D, 2.45D, 2.43D, 2.78D, 2.63D, 2.51D, 2.9D, 3.21D, 3.45D, 3.43D };
    private double[] selectedPositions = new double[0];
    private int packets;

    public Step() {
        super("Step", "Allows you to step up blocks", Module.Category.MOVEMENT, true, false, false);
    }

    @Override
    public void onUpdate() {
        switch (stepHeight.getValue()) {
            case 1:
                this.selectedPositions = this.oneblockPositions;
                break;
            case 2:
                this.selectedPositions = small.getValue() ? this.twoblockPositions : this.futurePositions;
                break;
            case 3:
                this.selectedPositions = this.twoFiveOffset;
                //this.selectedPositions = this.threeBlockPositions;
            case 4:
                this.selectedPositions = this.fourBlockPositions;
        }

        if (mc.player.collidedHorizontally && mc.player.onGround) {
            this.packets++;
        }

        final AxisAlignedBB bb = mc.player.getEntityBoundingBox();

        for (int x = MathHelper.floor(bb.minX); x < MathHelper.floor(bb.maxX + 1.0D); x++) {
            for (int z = MathHelper.floor(bb.minZ); z < MathHelper.floor(bb.maxZ + 1.0D); z++) {
                final Block block = mc.world.getBlockState(new BlockPos(x, bb.maxY + 1, z)).getBlock();
                if(!(block instanceof BlockAir)) {
                    return;
                }
            }
        }

        if (mc.player.onGround && !mc.player.isInsideOfMaterial(Material.WATER) && !mc.player.isInsideOfMaterial(Material.LAVA) && mc.player.collidedVertically && mc.player.fallDistance == 0 && !mc.gameSettings.keyBindJump.pressed && mc.player.collidedHorizontally && !mc.player.isOnLadder() && (this.packets > this.selectedPositions.length - 2  || (spoof.getValue() && this.packets > this.ticks.getValue()))) {
            for (double position : this.selectedPositions) {
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + position, mc.player.posZ, true));
            }
            mc.player.setPosition(mc.player.posX, mc.player.posY + this.selectedPositions[this.selectedPositions.length - 1], mc.player.posZ);
            this.packets = 0;
            if(turnOff.getValue()) {
                this.disable();
            }
        }
    }
}
