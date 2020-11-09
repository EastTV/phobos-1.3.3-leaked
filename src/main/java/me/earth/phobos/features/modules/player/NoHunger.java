package me.earth.phobos.features.modules.player;

import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static net.minecraft.network.play.client.CPacketEntityAction.Action.START_SPRINTING;
import static net.minecraft.network.play.client.CPacketEntityAction.Action.STOP_SPRINTING;

public class NoHunger extends Module {

    public Setting<Boolean> cancelSprint = register(new Setting("CancelSprint", true));

    public NoHunger() {
        super("NoHunger", "Prevents you from getting Hungry", Category.PLAYER, true, false, false);
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketPlayer) {
            final CPacketPlayer packet = event.getPacket();
            packet.onGround = mc.player.fallDistance >= 0 || mc.playerController.isHittingBlock;
        }

        if(cancelSprint.getValue() && event.getPacket() instanceof CPacketEntityAction) {
            final CPacketEntityAction packet = event.getPacket();
            if (packet.getAction() == START_SPRINTING || packet.getAction() == STOP_SPRINTING) {
                event.setCanceled(true);
            }
        }
    }
}
