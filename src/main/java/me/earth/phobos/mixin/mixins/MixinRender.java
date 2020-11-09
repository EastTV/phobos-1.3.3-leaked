package me.earth.phobos.mixin.mixins;

import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Render.class)
public class MixinRender<T extends Entity> {

    /*@Shadow
    public abstract boolean shouldRender(T livingEntity, ICamera camera, double camX, double camY, double camZ);

    private boolean injection = true;*/

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    public void shouldRender(T livingEntity, ICamera camera, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> info) {
        if(livingEntity == null || camera == null || livingEntity.getRenderBoundingBox() == null) {
            info.setReturnValue(false);
        }
    }

    /*@Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    public void shouldRender(T livingEntity, ICamera camera, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> info) {
        if(injection) {
            injection = false;
            boolean result = false;
            try {
                result = this.shouldRender(livingEntity, camera, camX, camY, camZ);
            } catch(Exception e) {
                result = false;
            }
            info.setReturnValue(result);
            injection = true;
        }
    }*/

}
