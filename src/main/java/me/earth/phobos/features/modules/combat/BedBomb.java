package me.earth.phobos.features.modules.combat;

import com.google.common.util.concurrent.AtomicDouble;
import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.BlockUtil;
import me.earth.phobos.util.DamageUtil;
import me.earth.phobos.util.EntityUtil;
import me.earth.phobos.util.MathUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.block.BlockBed;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketCombatEvent;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.concurrent.atomic.AtomicBoolean;

public class BedBomb extends Module {

    public BedBomb() {
        super("BedAura", "AutoPlace and Break for beds", Category.COMBAT, true, false, false);
    }

    //private final Setting<Boolean> place = register(new Setting("Place", false));
    //private final Setting<Integer> placeDelay = register(new Setting("PlaceDelay", 0, 0, 500, v -> place.getValue()));
    private final Setting<Integer> breakDelay = register(new Setting("Breakdelay", 50, 0, 500));
    private final Setting<Float> breakRange = register(new Setting("BreakRange", 6.0f, 1.0f, 10.0f));
    private final Setting<Boolean> calc = register(new Setting("Calc", false));
    private final Setting<Float> minDamage = register(new Setting("MinDamage", 5.0f, 1.0f, 36.0f, v -> calc.getValue()));
    private final Setting<Float> range = register(new Setting("Range", 10.0f, 1.0f, 12.0f, v -> calc.getValue()));
    private final Setting<Boolean> rotate = register(new Setting("Rotate", false, v -> calc.getValue()));
    private final Setting<Boolean> suicide = register(new Setting("Suicide", false, v -> calc.getValue()));
    private final Setting<Boolean> offhand = register(new Setting("OffHand", false));

    private final Timer breakTimer = new Timer();
    //private final Timer placeTimer = new Timer();

    private AtomicDouble yaw = new AtomicDouble(-1D);
    private AtomicDouble pitch = new AtomicDouble(-1D);
    private AtomicBoolean shouldRotate = new AtomicBoolean(false);

    @SubscribeEvent
    public void onDeath(PacketEvent.Receive event) {
        if(event.getPacket() instanceof SPacketCombatEvent) {
            SPacketCombatEvent packet = event.getPacket();
            if (packet.eventType == SPacketCombatEvent.Event.ENTITY_DIED) {
                Entity entity = mc.world.getEntityByID(packet.entityId);
                if(entity instanceof EntityPlayer) {
                    Command.sendMessage(entity.getName() + " died.");
                }
            }
        }
    }

    @SubscribeEvent
    public void onDeath(PacketEvent.Send event) {
        if(rotate.getValue() && shouldRotate.get()) {
            if (event.getPacket() instanceof CPacketPlayer) {
                CPacketPlayer packet = event.getPacket();
                packet.yaw = (float)this.yaw.get();
                packet.pitch = (float)this.pitch.get();
                shouldRotate.set(false);
            }
        }
    }

    @Override
    public void onTick() {
        if (mc.player.dimension != -1 && mc.player.dimension != 1) {
            return;
        }

        if(breakTimer.passedMs(breakDelay.getValue())) {
            BlockPos maxPos = null;
            float maxDamage = 0.5f;
            for(BlockPos pos : BlockUtil.getBlockSphere(breakRange.getValue(), BlockBed.class)) {
                if(calc.getValue()) {
                    float selfDamage = DamageUtil.calculateDamage(pos, mc.player);
                    if (selfDamage + 0.5 < EntityUtil.getHealth(mc.player) || !DamageUtil.canTakeDamage(suicide.getValue())) {
                        for (EntityPlayer player : mc.world.playerEntities) {
                            if (player.getDistanceSq(pos) < MathUtil.square((range.getValue())) && EntityUtil.isValid(player, (range.getValue() + breakRange.getValue()))) {
                                float damage = DamageUtil.calculateDamage(pos, player);
                                if (damage > selfDamage || (damage > minDamage.getValue() && !DamageUtil.canTakeDamage(suicide.getValue())) || damage > EntityUtil.getHealth(player)) {
                                    if (damage > maxDamage) {
                                        maxDamage = damage;
                                        maxPos = pos;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    maxPos = pos;
                    break;
                }
            }

            if(maxPos != null) {
                BlockUtil.rightClickBlockLegit(maxPos, range.getValue(), rotate.getValue(), offhand.getValue() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, yaw, pitch, shouldRotate);
                breakTimer.reset();
            }
        }
    }

    @Override
    public void onToggle() {
        yaw = new AtomicDouble(-1D);
        pitch = new AtomicDouble(-1D);
        shouldRotate = new AtomicBoolean(false);
    }

    /*public enum Rotation {
        NONE,
        BREAK,
        PLACE
    }*/
}
