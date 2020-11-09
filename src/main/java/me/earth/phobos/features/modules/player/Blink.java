package me.earth.phobos.features.modules.player;

import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.MathUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketKeepAlive;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketTabComplete;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Blink extends Module {

    public Setting<Boolean> cPacketPlayer = register(new Setting("CPacketPlayer", true));
    public Setting<Mode> autoOff = register(new Setting("AutoOff", Mode.MANUAL));
    public Setting<Integer> timeLimit = register(new Setting("Time", 20,1, 500, v -> autoOff.getValue() == Mode.TIME));
    public Setting<Integer> packetLimit = register(new Setting("Packets", 20,1, 500, v -> autoOff.getValue() == Mode.PACKETS));
    public Setting<Float> distance = register(new Setting("Distance", 10.0f, 1.0f, 100.0f, v -> autoOff.getValue() == Mode.DISTANCE));

    private Timer timer = new Timer();
    private Queue<Packet<?>> packets = new ConcurrentLinkedQueue<>();
    private EntityOtherPlayerMP entity;
    private int packetsCanceled = 0;
    private BlockPos startPos = null;

    private static Blink INSTANCE = new Blink();

    public Blink() {
        super("Blink", "Fakelag.", Category.PLAYER, true, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static Blink getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Blink();
        }
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        if (!fullNullCheck()) {
            entity = new EntityOtherPlayerMP(mc.world, mc.session.getProfile());
            entity.copyLocationAndAnglesFrom(mc.player);
            entity.rotationYaw = mc.player.rotationYaw;
            entity.rotationYawHead = mc.player.rotationYawHead;
            entity.inventory.copyInventory(mc.player.inventory);
            mc.world.addEntityToWorld(6942069, entity);
            startPos = mc.player.getPosition();
        } else {
            this.disable();
        }
        packetsCanceled = 0;
        timer.reset();
    }

    @Override
    public void onUpdate() {
        if(nullCheck() || (autoOff.getValue() == Mode.TIME && timer.passedS(timeLimit.getValue())) || (autoOff.getValue() == Mode.DISTANCE && startPos != null && mc.player.getDistanceSq(startPos) >= MathUtil.square(distance.getValue())) || (autoOff.getValue() == Mode.PACKETS && packetsCanceled >= packetLimit.getValue())) {
            this.disable();
        }
    }

    @Override
    public void onLogout() {
        if(this.isOn()) {
            this.disable();
        }
    }

    @SubscribeEvent
    public void onSendPacket(PacketEvent.Send event) {
        if (event.getStage() == 0 && mc.world != null && !mc.isSingleplayer()) {
            Packet<?> packet = event.getPacket();
            if(cPacketPlayer.getValue() && packet instanceof CPacketPlayer) {
                event.setCanceled(true);
                packets.add(packet);
                packetsCanceled++;
            }

            if(!cPacketPlayer.getValue()) {
                if (packet instanceof CPacketChatMessage || packet instanceof CPacketConfirmTeleport || packet instanceof CPacketKeepAlive || packet instanceof CPacketTabComplete || packet instanceof CPacketClientStatus) {
                    return;
                }
                packets.add(packet);
                event.setCanceled(true);
                packetsCanceled++;
            }
        }
    }

    @Override
    public void onDisable() {
        if(!fullNullCheck()) {
            mc.world.removeEntity(entity);
            while (!packets.isEmpty()) {
                mc.player.connection.sendPacket(packets.poll());
            }
        }
        startPos = null;
    }

    public enum Mode {
        MANUAL,
        TIME,
        DISTANCE,
        PACKETS
    }

}
