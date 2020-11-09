package me.earth.phobos.features.modules.render;

import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Fullbright extends Module {

    public Setting<Mode> mode = register(new Setting("Mode", Mode.GAMMA));
    public Setting<Boolean> effects = register(new Setting("Effects", false));

    private float previousSetting = 1f;

    public Fullbright() {
        super("Fullbright", "Makes your game brighter.", Category.RENDER, true, false, false);
    }

    @Override
    public void onEnable() {
        previousSetting = mc.gameSettings.gammaSetting;
    }

    @Override
    public void onUpdate() {
        if(mode.getValue() == Mode.GAMMA) {
            mc.gameSettings.gammaSetting = 1000f;
        }

        if(mode.getValue() == Mode.POTION) {
            mc.player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 5210));
        }
    }

    @Override
    public void onDisable() {
        if(mode.getValue() == Mode.POTION) {
            mc.player.removePotionEffect(MobEffects.NIGHT_VISION);
        }
        mc.gameSettings.gammaSetting = previousSetting;
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getStage() == 0 && event.getPacket() instanceof SPacketEntityEffect) {
            if(this.effects.getValue()) {
                SPacketEntityEffect packet = event.getPacket();
                if (mc.player != null && packet.getEntityId() == mc.player.getEntityId() && (packet.getEffectId() == 9 || packet.getEffectId() == 15)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    public enum Mode {
        GAMMA,
        POTION
    }
}
