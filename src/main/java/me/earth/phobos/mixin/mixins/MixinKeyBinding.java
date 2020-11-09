package me.earth.phobos.mixin.mixins;

import me.earth.phobos.event.events.KeyEvent;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyBinding.class)
public class MixinKeyBinding {

    @Shadow
    private boolean pressed;

    //TODO: No Event?
    @Inject(method = "isKeyDown", at = @At("RETURN"), cancellable = true)
    private void isKeyDown(final CallbackInfoReturnable<Boolean> info) {
        KeyEvent event = new KeyEvent(0, info.getReturnValue(), this.pressed);
        MinecraftForge.EVENT_BUS.post(event);
        info.setReturnValue(event.info);
    }

}
