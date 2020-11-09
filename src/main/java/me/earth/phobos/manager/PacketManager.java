package me.earth.phobos.manager;

import me.earth.phobos.features.Feature;
import net.minecraft.network.Packet;

import java.util.ArrayList;
import java.util.List;

public class PacketManager extends Feature {

    private final List<Packet<?>> noEventPackets = new ArrayList<>();

    /*public void sendAPacket(Packet<?> packet) {
        if(!nullCheck() && packet != null) {
            NetworkManager manager = mc.player.connection.getNetworkManager();
            if(manager.isChannelOpen()) {
                manager.flushOutboundQueue();
                manager.dispatchPacket(packet, null);
            } else {
                manager.readWriteLock.writeLock().lock();

                try {
                    manager.outboundPacketsQueue.add(new NetworkManager.InboundHandlerTuplePacketListener(packet, new GenericFutureListener[0]));
                } finally {
                    manager.readWriteLock.writeLock().unlock();
                }
            }
        }
    }*/

    public void sendPacketNoEvent(Packet<?> packet) {
        if(packet != null && !nullCheck()) {
            noEventPackets.add(packet);
            mc.player.connection.sendPacket(packet);
        }
    }

    public boolean shouldSendPacket(Packet<?> packet) {
        if(noEventPackets.contains(packet)) {
            noEventPackets.remove(packet);
            return false;
        }
        return true;
    }
}
