package me.earth.phobos.features.modules.misc;

import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.util.EnumHand;

import java.util.Random;

public class NoAFK extends Module {

    public NoAFK() {
        super("NoAFK", "Prevents you from getting kicked for afk.", Category.MISC, false, false, false);
    }

    private final Setting<Boolean> swing = register(new Setting("Swing", true));
    private final Setting<Boolean> turn = register(new Setting("Turn", true));

    private final Random random = new Random();

    @Override
    public void onUpdate() {
        if (mc.playerController.getIsHittingBlock()) {
            return;
        }

        if (mc.player.ticksExisted % 40 == 0 && swing.getValue()) {
            mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
        }

        if (mc.player.ticksExisted % 15 == 0 && turn.getValue()) {
            mc.player.rotationYaw = random.nextInt(360) - 180;
        }

        if (!(swing.getValue() || turn.getValue()) && mc.player.ticksExisted % 80 == 0) {
            mc.player.jump();
        }
    }

}
