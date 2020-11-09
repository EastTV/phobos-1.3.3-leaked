package me.earth.phobos.features.modules.misc;

import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.Timer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Bypass extends Module {

    public Setting<Boolean> illegals = register(new Setting("Illegals", false));
    public Setting<Boolean> secretClose = register(new Setting("SecretClose", false, v -> illegals.getValue()));
    public Setting<Boolean> elytra = register(new Setting("Elytra", false));
    public Setting<Boolean> reopen = register(new Setting("Reopen", false, v -> elytra.getValue()));
    public Setting<Integer> reopen_interval = register(new Setting("ReopenDelay", 1000, 0, 5000, v -> elytra.getValue()));
    public Setting<Integer> delay = register(new Setting("Delay", 0, 0, 1000, v -> elytra.getValue()));
    public Setting<Boolean> allow_ghost = register(new Setting("Ghost", true, v -> elytra.getValue()));
    public Setting<Boolean> cancel_close = register(new Setting("Cancel", true, v -> elytra.getValue()));
    public Setting<Boolean> discreet = register(new Setting("Secret", true, v -> elytra.getValue()));

    int cooldown = 0;
    private final Timer timer = new Timer();

    public Bypass() {
        super("Bypass", "Bypass for stuff", Category.MISC, true, false, false);
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if(illegals.getValue() && secretClose.getValue() && event.getPacket() instanceof CPacketCloseWindow) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onIncomingPacket(PacketEvent.Receive event) {
        if(!fullNullCheck() && elytra.getValue()) {
            if(event.getPacket() instanceof SPacketSetSlot) {
                SPacketSetSlot packet = event.getPacket();
                if (packet.getSlot() == 6) {
                    event.setCanceled(true);
                }

                if(!allow_ghost.getValue() && packet.getStack().getItem().equals(Items.ELYTRA)) {
                    event.setCanceled(true);
                }
            }

            if(cancel_close.getValue() && mc.player.isElytraFlying() && event.getPacket() instanceof SPacketEntityMetadata) {
                SPacketEntityMetadata MetadataPacket = event.getPacket();
                if (MetadataPacket.getEntityId() == mc.player.getEntityId()) {
                    event.setCanceled(true);
                }
            }

        }
    }

    @Override
    public void onTick() {
        if(elytra.getValue()) {
            if (this.cooldown > 0) {
                --this.cooldown;
            } else if (mc.player != null && !(mc.currentScreen instanceof GuiInventory) && (!mc.player.onGround || !discreet.getValue())) {
                for (int i = 0; i < 36; ++i) {
                    ItemStack item = mc.player.inventory.getStackInSlot(i);
                    if (item.getItem().equals(Items.ELYTRA)) {
                        mc.playerController.windowClick(0, i < 9 ? i + 36 : i, 0, ClickType.QUICK_MOVE, mc.player);
                        this.cooldown = delay.getValue();
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void onUpdate() {
        if (elytra.getValue() && this.timer.passedMs(reopen_interval.getValue()) && reopen.getValue() && !mc.player.isElytraFlying() && mc.player.fallDistance > 0.0F) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
        }
    }
}
