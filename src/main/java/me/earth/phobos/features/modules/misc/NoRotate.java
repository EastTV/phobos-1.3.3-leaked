package me.earth.phobos.features.modules.misc;

import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.TextUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

//Wait on login and logoff
public class NoRotate extends Module {

    private Setting<Integer> waitDelay = register(new Setting("Delay", 2500, 0, 10000));

    //Im not sure when the server sends you the packets. So to be sure it works I do the 2 booleans on logout and login...
    private Timer timer = new Timer();
    private boolean cancelPackets = true;
    private boolean timerReset = false;

    public NoRotate() {
        super("NoRotate", "Dangerous to use might desync you.", Category.MISC,true, false, false);
    }

    @Override
    public void onLogout() {
        cancelPackets = false;
    }

    @Override
    public void onLogin() {
        timer.reset();
        timerReset = true;
    }

    @Override
    public void onUpdate() {
        if(timerReset && !cancelPackets && timer.passedMs(waitDelay.getValue())) {
            Command.sendMessage("<NoRotate> " + TextUtil.RED + "This module might desync you!");
            cancelPackets = true;
            timerReset = false;
        }
    }

    @Override
    public void onEnable() {
        Command.sendMessage("<NoRotate> " + TextUtil.RED + "This module might desync you!");
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if(event.getStage() == 0 && cancelPackets) {
            if (event.getPacket() instanceof SPacketPlayerPosLook) {
                SPacketPlayerPosLook packet = event.getPacket();
                packet.yaw = mc.player.rotationYaw;
                packet.pitch = mc.player.rotationPitch;
            }
        }
    }
}
