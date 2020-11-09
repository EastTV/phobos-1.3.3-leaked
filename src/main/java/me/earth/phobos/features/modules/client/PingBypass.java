package me.earth.phobos.features.modules.client;

import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.modules.Module;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Objects;

public class PingBypass extends Module {

    public PingBypass() {
        super("PingBypass", "Big Hack", Category.CLIENT, true, false, false);
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if(event.getPacket() instanceof CPacketUseEntity) {
            CPacketUseEntity packet = event.getPacket();
            Command.sendMessage(Objects.requireNonNull(packet.getEntityFromWorld(mc.world)).getEntityId() + "");
        }
    }
}
