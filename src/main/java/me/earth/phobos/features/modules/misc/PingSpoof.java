package me.earth.phobos.features.modules.misc;

import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.MathUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketKeepAlive;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PingSpoof extends Module {

    private Setting<Boolean> seconds = register(new Setting("Seconds", false));
    private Setting<Integer> delay = register(new Setting("DelayMS", 20, 0, 1000, v -> !seconds.getValue()));
    private Setting<Integer> secondDelay = register(new Setting("DelayS", 5, 0, 30, v -> seconds.getValue()));
    private Setting<Boolean> extraPacket = register(new Setting("Packet", true));
    private Setting<Boolean> offOnLogout = register(new Setting("Logout", false));

    private Queue<Packet<?>> packets = new ConcurrentLinkedQueue<>();
    private Timer timer = new Timer();
    private boolean receive = true;

    public PingSpoof() {
        super("PingSpoof", "Spoofs your ping!", Category.MISC, true, false, false);
    }

    @Override
    public void onLoad() {
        if(offOnLogout.getValue()) {
            this.disable();
        }
    }

    @Override
    public void onLogout() {
        if(offOnLogout.getValue()) {
            this.disable();
        }
    }

    @Override
    public void onUpdate() {
        clearQueue();
    }

    @Override
    public void onDisable() {
        clearQueue();
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if(receive && mc.player != null && !mc.isSingleplayer() && mc.player.isEntityAlive() && event.getStage() == 0 && event.getPacket() instanceof CPacketKeepAlive) {
            packets.add(event.getPacket());
            event.setCanceled(true);
        }
    }

    public void clearQueue() {
        if(mc.player != null && !mc.isSingleplayer() && mc.player.isEntityAlive() && ((!seconds.getValue() && timer.passedMs(delay.getValue())) || (seconds.getValue() && timer.passedS(secondDelay.getValue())))) {
            double limit = MathUtil.getIncremental(Math.random() * 10.0, 1.0);
            receive = false;
            for(int i = 0; i < limit; i++) {
                Packet<?> packet = packets.poll();
                if(packet != null) {
                    mc.player.connection.sendPacket(packet);
                }
            }

            if(extraPacket.getValue()) {
                mc.player.connection.sendPacket(new CPacketKeepAlive(10000));
            }
            timer.reset();
            receive = true;
        }
    }
}
