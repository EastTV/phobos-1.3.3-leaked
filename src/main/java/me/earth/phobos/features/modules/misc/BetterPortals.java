package me.earth.phobos.features.modules.misc;

import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BetterPortals extends Module {

    public Setting<Boolean> portalChat = register(new Setting("Chat", true, "Allows you to chat in portals."));
    public Setting<Boolean> godmode = register(new Setting("Godmode", false, "Portal Godmode."));
    public Setting<Boolean> fastPortal = register(new Setting("FastPortal", false));
    public Setting<Integer> cooldown = register(new Setting("Cooldown", 5, 1, 10, v -> fastPortal.getValue(), "Portal cooldown."));
    public Setting<Integer> time = register(new Setting("Time", 5, 0, 80, v -> fastPortal.getValue(), "Time in Portal"));

    private static BetterPortals INSTANCE = new BetterPortals();

    public BetterPortals() {
        super("BetterPortals", "Tweaks for Portals", Category.MISC, true, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static BetterPortals getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new BetterPortals();
        }
        return INSTANCE;
    }

    @Override
    public String getDisplayInfo() {
        if(godmode.getValue()) {
            return "Godmode";
        }
        return null;
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if(event.getStage() == 0 && godmode.getValue() && event.getPacket() instanceof CPacketConfirmTeleport) {
            event.setCanceled(true);
        }
    }
}
