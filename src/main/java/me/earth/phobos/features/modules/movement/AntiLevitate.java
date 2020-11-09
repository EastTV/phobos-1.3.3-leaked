package me.earth.phobos.features.modules.movement;

import me.earth.phobos.features.modules.Module;
import net.minecraft.potion.Potion;

import java.util.Objects;

//TODO: AntiPotion...
public class AntiLevitate extends Module {

    public AntiLevitate() { super("AntiLevitate", "Removes shulker levitation", Category.MOVEMENT, false, false, false); }

    @Override
    public void onUpdate() {
        if(mc.player.isPotionActive(Objects.requireNonNull(Potion.getPotionFromResourceLocation("levitation")))) {
            mc.player.removeActivePotionEffect(Potion.getPotionFromResourceLocation("levitation"));
        }
    }

}
