package me.earth.phobos.features.modules.combat;

import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.event.events.UpdateWalkingPlayerEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.EntityUtil;
import me.earth.phobos.util.InventoryUtil;
import me.earth.phobos.util.MathUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemEndCrystal;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

public class BowSpam extends Module {

    public Setting<Mode> mode = register(new Setting("Mode", Mode.FAST));
    public Setting<Boolean> bowbomb = register(new Setting("BowBomb", false, v -> mode.getValue() != Mode.BOWBOMB));
    public Setting<Boolean> allowOffhand = register(new Setting("Offhand", true, v -> mode.getValue() != Mode.AUTORELEASE));
    public Setting<Integer> ticks = register(new Setting("Ticks", 3, 0, 20, v -> mode.getValue() == Mode.BOWBOMB || mode.getValue() == Mode.FAST, "Speed"));
    public Setting<Integer> delay = register(new Setting("Delay", 50, 0, 500, v -> mode.getValue() == Mode.AUTORELEASE, "Speed"));
    public Setting<Boolean> tpsSync = register(new Setting("TpsSync", true));
    public Setting<Boolean> autoSwitch = register(new Setting("AutoSwitch", false));
    public Setting<Boolean> onlyWhenSave = register(new Setting("OnlyWhenSave", true, v -> autoSwitch.getValue()));
    public Setting<Target> targetMode = register(new Setting("Target", Target.LOWEST, v -> autoSwitch.getValue()));
    public Setting<Float> range = register(new Setting("Range", 3.0f, 0.0f, 6.0f, v -> autoSwitch.getValue(), "Range of the target"));
    public Setting<Float> health = register(new Setting("Lethal", 6.0f, 0.1f, 36.0f, v -> autoSwitch.getValue(), "When should it switch?"));
    public Setting<Float> ownHealth = register(new Setting("OwnHealth", 20.0f, 0.1f, 36.0f, v -> autoSwitch.getValue(), "Own Health."));

    private final Timer timer = new Timer();
    private boolean offhand = false;
    private boolean switched = false;
    private int lastHotbarSlot = -1;

    public BowSpam() {
        super("BowSpam", "Spams your bow", Category.COMBAT, true, false, false);
    }

    @Override
    public void onEnable() {
        lastHotbarSlot = mc.player.inventory.currentItem;
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if(event.getStage() != 0) {
            return;
        }

        if(autoSwitch.getValue() && InventoryUtil.findHotbarBlock(ItemBow.class) != -1 && ownHealth.getValue() <= EntityUtil.getHealth(mc.player) && (!onlyWhenSave.getValue() || EntityUtil.isSafe(mc.player))) {
            EntityPlayer target = getTarget();
            if(target != null) {
                AutoCrystal crystal = Phobos.moduleManager.getModuleByClass(AutoCrystal.class);
                if(!(crystal.isOn() && InventoryUtil.holdingItem(ItemEndCrystal.class))) {
                    Vec3d pos = target.getPositionVector();
                    double xPos = pos.x;
                    double yPos = pos.y;
                    double zPos = pos.z;
                    if(mc.player.canEntityBeSeen(target)) { //TODO: More Accurate!
                        //Vec3d sameY = new Vec3d(xPos, mc.player.posY, zPos);
                        yPos += target.eyeHeight;
                    } else if(EntityUtil.canEntityFeetBeSeen(target)) {
                        yPos += 0.1;
                    } else {
                        return;
                    }

                    if(!(mc.player.getHeldItemMainhand().getItem() instanceof ItemBow)) {
                        lastHotbarSlot = mc.player.inventory.currentItem;
                        InventoryUtil.switchToHotbarSlot(ItemBow.class, false);
                        mc.gameSettings.keyBindUseItem.pressed = true;
                        switched = true;
                    }

                    Phobos.rotationManager.lookAtVec3d(xPos, yPos, zPos);
                    if(mc.player.getHeldItemMainhand().getItem() instanceof ItemBow) {
                        switched = true;
                    }
                }
            }
        } else if(event.getStage() == 0 && switched && lastHotbarSlot != -1) {
            InventoryUtil.switchToHotbarSlot(lastHotbarSlot, false);
            mc.gameSettings.keyBindUseItem.pressed = Mouse.isButtonDown(1);
            switched = false;
        } else {
            mc.gameSettings.keyBindUseItem.pressed = Mouse.isButtonDown(1);
        }

        if (mode.getValue() == Mode.FAST) {
            if (offhand || mc.player.inventory.getCurrentItem().getItem() instanceof ItemBow) {
                if (mc.player.isHandActive() && mc.player.getItemInUseMaxCount() >= ticks.getValue() * (tpsSync.getValue() ? Phobos.serverManager.getTpsFactor() : 1)) {
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND));
                    mc.player.stopActiveHand();
                }
            }
        }
    }

    @Override
    public void onUpdate() {
        offhand = mc.player.getHeldItemOffhand().getItem() == Items.BOW && allowOffhand.getValue();
        switch(mode.getValue()) {
            case AUTORELEASE:
                if(offhand || mc.player.inventory.getCurrentItem().getItem() instanceof ItemBow) {
                    if (timer.passedMs((int)(delay.getValue() * (tpsSync.getValue() ? Phobos.serverManager.getTpsFactor() : 1)))) {
                        mc.playerController.onStoppedUsingItem(mc.player);
                        timer.reset();
                    }
                }
                break;
            case BOWBOMB:
                if (offhand || mc.player.inventory.getCurrentItem().getItem() instanceof ItemBow) {
                    if (mc.player.isHandActive() && mc.player.getItemInUseMaxCount() >= ticks.getValue() * (tpsSync.getValue() ? Phobos.serverManager.getTpsFactor() : 1)) {
                        mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
                        mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY - 0.0624, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
                        mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY - 999.0D, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, true));
                        mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND));
                        mc.player.stopActiveHand();
                    }
                }
                break;
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getStage() == 0 && bowbomb.getValue() && mode.getValue() != Mode.BOWBOMB && event.getPacket() instanceof CPacketPlayerDigging) {
            CPacketPlayerDigging packet = event.getPacket();
            if (packet.getAction() == CPacketPlayerDigging.Action.RELEASE_USE_ITEM) {
                if ((offhand || mc.player.inventory.getCurrentItem().getItem() instanceof ItemBow) && mc.player.getItemInUseMaxCount() >= 20 && !mc.player.onGround) {
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 0.1f, mc.player.posZ, false));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 10000f, mc.player.posZ, true));
                }
            }
        }
    }

    private EntityPlayer getTarget() {
        double maxHealth = 36.0;
        EntityPlayer target = null;
        for(EntityPlayer player : mc.world.playerEntities) {
            if(player == null) {
                continue;
            }

            if(EntityUtil.isDead(player)) {
                continue;
            }

            if(EntityUtil.getHealth(player) > health.getValue()) {
                continue;
            }

            if(player.equals(mc.player)) {
                continue;
            }

            if(Phobos.friendManager.isFriend(player)) {
                continue;
            }

            if(mc.player.getDistanceSq(player) > MathUtil.square(range.getValue())) {
                continue;
            }

            if(!mc.player.canEntityBeSeen(player) && !EntityUtil.canEntityFeetBeSeen(player)) {
                continue;
            }

            if(target == null) {
                target = player;
                maxHealth = EntityUtil.getHealth(player);
            }

            if(targetMode.getValue() == Target.CLOSEST && mc.player.getDistanceSq(player) < mc.player.getDistanceSq(target)) {
                target = player;
                maxHealth = EntityUtil.getHealth(player);
            }

            if(targetMode.getValue() == Target.LOWEST && EntityUtil.getHealth(player) < maxHealth) {
                target = player;
                maxHealth = EntityUtil.getHealth(player);
            }
        }
        return target;
    }

    public enum Target {
        CLOSEST,
        LOWEST
    }

    public enum Mode {
        FAST,
        AUTORELEASE,
        BOWBOMB
    }
}
