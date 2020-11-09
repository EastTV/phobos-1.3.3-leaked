package me.earth.phobos.mixin.mixins;

import me.earth.phobos.features.modules.movement.Flight;
import me.earth.phobos.features.modules.movement.Phase;
import me.earth.phobos.features.modules.player.Freecam;
import me.earth.phobos.features.modules.player.Jesus;
import me.earth.phobos.features.modules.render.XRay;
import me.earth.phobos.util.EntityUtil;
import me.earth.phobos.util.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(Block.class)
public class MixinBlock {

    //TODO: WTF PLS USE AN EVENT
    @Inject(method = "addCollisionBoxToList(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;Z)V", at = @At("HEAD"), cancellable = true)
    public void addCollisionBoxToListHook(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState, CallbackInfo info) {
        if((entityIn != null && Util.mc.player != null && (entityIn.equals(Util.mc.player) || (Util.mc.player.getRidingEntity() != null && entityIn.equals(Util.mc.player.getRidingEntity())))) && ((Flight.getInstance().isOn() && ((Flight.getInstance().mode.getValue() == Flight.Mode.PACKET && Flight.getInstance().better.getValue() && Flight.getInstance().phase.getValue()) || (Flight.getInstance().mode.getValue() == Flight.Mode.DAMAGE && Flight.getInstance().noClip.getValue())) || (Phase.getInstance().isOn() && Phase.getInstance().mode.getValue() == Phase.Mode.PACKETFLY && Phase.getInstance().type.getValue() == Phase.PacketFlyMode.SETBACK && Phase.getInstance().boundingBox.getValue())))) {
            info.cancel();
        }

        try {
            if(Freecam.getInstance().isOff() && Jesus.getInstance().isOn() && Jesus.getInstance().mode.getValue() == Jesus.Mode.TRAMPOLINE && Util.mc.player != null && state != null && state.getBlock() instanceof BlockLiquid && !(entityIn instanceof EntityBoat) && !Util.mc.player.isSneaking() && (Util.mc.player.fallDistance < 3.0f && !EntityUtil.isAboveLiquid(Util.mc.player) && EntityUtil.checkForLiquid(Util.mc.player, false)) || (EntityUtil.checkForLiquid(Util.mc.player, false) && Util.mc.player.getRidingEntity() != null && Util.mc.player.getRidingEntity().fallDistance < 3.0f) && EntityUtil.isAboveBlock(Util.mc.player, pos)) {
                final AxisAlignedBB offset = Jesus.offset.offset(pos);
                if (entityBox.intersects(offset)) {
                    collidingBoxes.add(offset);
                }
                info.cancel();
            }
        } catch (Exception ignored) {}
    }

    @Inject(method = "isFullCube", at = { @At("HEAD") }, cancellable = true)
    public void isFullCubeHook(IBlockState blockState, CallbackInfoReturnable<Boolean> info) {
        try {
            if (XRay.getInstance().isOn()) {
                info.setReturnValue(XRay.getInstance().shouldRender(Block.class.cast(this)));
                info.cancel();
            }
        } catch (Exception ignored) {}
    }

}
