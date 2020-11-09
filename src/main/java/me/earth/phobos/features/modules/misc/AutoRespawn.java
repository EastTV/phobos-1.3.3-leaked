package me.earth.phobos.features.modules.misc;

import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoRespawn extends Module {

    public Setting<Boolean> antiDeathScreen = register(new Setting("AntiDeathScreen", true));
    public Setting<Boolean> deathCoords = register(new Setting("DeathCoords", false));
    public Setting<Boolean> respawn = register(new Setting("Respawn", true));

    public AutoRespawn() {
        super("AutoRespawn", "Respawns you when you die.", Category.MISC, true, false, false);
    }

    @SubscribeEvent
    public void onDisplayDeathScreen(GuiOpenEvent event) {
        if (event.getGui() instanceof GuiGameOver) {
            if(deathCoords.getValue()) {
                if(event.getGui() instanceof GuiGameOver) {
                    Command.sendMessage(String.format("You died at x %d y %d z %d", (int)mc.player.posX, (int)mc.player.posY, (int)mc.player.posZ));
                }
            }

            if((respawn.getValue() && mc.player.getHealth() <= 0) || (antiDeathScreen.getValue() && mc.player.getHealth() > 0)) {
                event.setCanceled(true);
                mc.player.respawnPlayer();
            }
        }
    }
}
