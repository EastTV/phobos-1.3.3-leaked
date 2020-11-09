package me.earth.phobos.manager;

import me.earth.phobos.util.Util;
import net.minecraft.network.play.client.CPacketHeldItemChange;

public class InventoryManager implements Util {

    private int recoverySlot = -1;

    public void update() {
        if(recoverySlot != -1) {
            mc.player.connection.sendPacket(new CPacketHeldItemChange(recoverySlot == 8 ? 7 : recoverySlot + 1));
            mc.player.connection.sendPacket(new CPacketHeldItemChange(recoverySlot));
            mc.player.inventory.currentItem = recoverySlot;
            mc.playerController.syncCurrentPlayItem();
            recoverySlot = -1;
        }
    }

    public void recoverSilent(int slot) {
        recoverySlot = slot;
    }
}
