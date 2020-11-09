package me.earth.phobos.features.modules.misc;

import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.util.PlayerUtil;
import me.earth.phobos.util.TextUtil;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AntiVanish extends Module {

    private final Queue<UUID> toLookUp = new ConcurrentLinkedQueue<>();

    public AntiVanish() {
        super("AntiVanish", "Notifies you when players vanish", Category.MISC, true, false, false);
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketPlayerListItem) {
            final SPacketPlayerListItem sPacketPlayerListItem = event.getPacket();
            if(sPacketPlayerListItem.getAction() == SPacketPlayerListItem.Action.UPDATE_LATENCY) {
                for(final SPacketPlayerListItem.AddPlayerData addPlayerData : sPacketPlayerListItem.getEntries()) {
                    try {
                        if(mc.getConnection().getPlayerInfo(addPlayerData.getProfile().getId()) == null) {
                            toLookUp.add(addPlayerData.getProfile().getId());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void onUpdate() {
        if(PlayerUtil.timer.passedS(5)) {
            UUID lookUp = toLookUp.poll();
            if(lookUp != null) {
                try {
                    String name = PlayerUtil.getNameFromUUID(lookUp);
                    if (name != null) {
                        Command.sendMessage(TextUtil.RED + name + " has gone into vanish.");
                    }
                } catch (Exception ignored) {}
                PlayerUtil.timer.reset();
            }
        }
    }

    @Override
    public void onLogout() {
        toLookUp.clear();
    }
}
