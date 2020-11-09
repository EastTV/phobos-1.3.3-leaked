package me.earth.phobos.features.modules.misc;

import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import net.minecraft.init.Items;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Logger extends Module {

    public Setting<Packets> packets = register(new Setting("Packets", Packets.OUTGOING));
    public Setting<Boolean> chat = register(new Setting("Chat", false));

    public Logger() {
        super("Logger", "Logs stuff", Category.MISC, true, false, false);
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if(packets.getValue() == Packets.OUTGOING || packets.getValue() == Packets.ALL) {
            if(chat.getValue()) {
                Command.sendMessage(event.getPacket().toString());
            } else {
                System.out.println(event.getPacket().toString());
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if(packets.getValue() == Packets.INCOMING || packets.getValue() == Packets.ALL) {
            boolean xD = true;
            if(mc.player != null) {
                xD = mc.player.inventoryContainer.getSlot(6).getStack().getItem() == Items.ELYTRA;
                System.out.println(xD);
            }
            if(chat.getValue()) {
                Command.sendMessage(xD + event.getPacket().toString());
            } else {
                System.out.println(xD + event.getPacket().toString());
            }
        }
    }

    public enum Packets {
        NONE,
        INCOMING,
        OUTGOING,
        ALL
    }

}
