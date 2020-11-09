package me.earth.phobos.mixin.mixins;

import me.earth.phobos.features.modules.render.NoRender;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LayerBipedArmor.class)
public abstract class MixinLayerBipedArmor extends LayerArmorBase<ModelBiped> {

    public MixinLayerBipedArmor(RenderLivingBase<?> rendererIn) {
        super(rendererIn);
    }

    @Inject(method = "setModelSlotVisible", at = @At("HEAD"), cancellable = true)
    protected void setModelSlotVisible(ModelBiped model, EntityEquipmentSlot slotIn, CallbackInfo info) {
        NoRender noArmor = NoRender.getInstance();
        if(noArmor.isOn() && noArmor.noArmor.getValue() != NoRender.NoArmor.NONE) {
            info.cancel();
            switch(slotIn) {
                case HEAD:
                    model.bipedHead.showModel = false;
                    model.bipedHeadwear.showModel = false;
                    break;
                case CHEST:
                    model.bipedBody.showModel = noArmor.noArmor.getValue() != NoRender.NoArmor.ALL;
                    model.bipedRightArm.showModel = noArmor.noArmor.getValue() != NoRender.NoArmor.ALL;
                    model.bipedLeftArm.showModel = noArmor.noArmor.getValue() != NoRender.NoArmor.ALL;
                    break;
                case LEGS:
                    model.bipedBody.showModel = noArmor.noArmor.getValue() != NoRender.NoArmor.ALL;
                    model.bipedRightLeg.showModel = noArmor.noArmor.getValue() != NoRender.NoArmor.ALL;
                    model.bipedLeftLeg.showModel = noArmor.noArmor.getValue() != NoRender.NoArmor.ALL;
                    break;
                case FEET:
                    model.bipedRightLeg.showModel = noArmor.noArmor.getValue() != NoRender.NoArmor.ALL;
                    model.bipedLeftLeg.showModel = noArmor.noArmor.getValue() != NoRender.NoArmor.ALL;
            }
        }
    }

}
