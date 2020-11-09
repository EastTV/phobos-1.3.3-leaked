package me.earth.phobos.features.modules.player;

import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.InventoryUtil;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.util.EnumHand;
import org.lwjgl.input.Mouse;

public class MCP extends Module {

    private Setting<Mode> mode = register(new Setting("Mode", Mode.MIDDLECLICK));
    private Setting<Boolean> stopRotation = register(new Setting("Rotation", true));
    private Setting<Integer> rotation = register(new Setting("Delay", 10, 0, 100, v -> stopRotation.getValue()));

    //private Timer timer = new Timer();
    private boolean clicked = false;

    public MCP() {
        super("MCP", "Throws a pearl", Category.PLAYER, false, false, false);
    }

    @Override
    public void onEnable() {
        if(!fullNullCheck() && mode.getValue() == Mode.TOGGLE) {
            throwPearl();
            this.disable();
        }
    }

    @Override
    public void onTick() {//UpdateWalkingPlayerEvent event) {
        if(/*event.getStage() == 0 && */mode.getValue() == Mode.MIDDLECLICK) {
            if(Mouse.isButtonDown(2)) {
                if(!clicked) {
                    throwPearl();
                }
                clicked = true;
            } else {
                clicked = false;
            }
        }
    }

    /*@SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPacketSend(PacketEvent.Send event) {
        //This prevents other modules from changing the rotationYaw after us
        if(stopRotation.getValue() && !timer.passedMs(rotation.getValue()) && event.getPacket() instanceof CPacketPlayer) {
            CPacketPlayer packet = event.getPacket();
            packet.yaw = mc.player.rotationYaw;
            packet.pitch = mc.player.rotationPitch;
        }
    }*/

    private void throwPearl() {
        int pearlSlot = InventoryUtil.findHotbarBlock(ItemEnderPearl.class);
        boolean offhand = mc.player.getHeldItemOffhand().getItem() == Items.ENDER_PEARL;
        if(pearlSlot != -1 || offhand) {
            int oldslot = mc.player.inventory.currentItem;
            if(!offhand) {
                InventoryUtil.switchToHotbarSlot(pearlSlot, false);
            }
            mc.playerController.processRightClick(mc.player, mc.world, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
            if(!offhand) {
                InventoryUtil.switchToHotbarSlot(oldslot, false);
            }
            //timer.reset();
        }
    }

    public enum Mode {
        TOGGLE,
        MIDDLECLICK
    }
}
