package me.earth.phobos.features.modules.player;

import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.BlockEvent;
import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.event.events.Render3DEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.InventoryUtil;
import me.earth.phobos.util.RenderUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

import static me.earth.phobos.util.BlockUtil.canBreak;
import static net.minecraft.network.play.client.CPacketPlayerDigging.Action.START_DESTROY_BLOCK;
import static net.minecraft.network.play.client.CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK;

public class Speedmine extends Module {

    public Setting<Boolean> tweaks = register(new Setting("Tweaks", true));
    public Setting<Mode> mode = register(new Setting("Mode", Mode.PACKET, v -> tweaks.getValue()));
    public Setting<Boolean> reset = register(new Setting("Reset", true));
    public Setting<Float> damage = register(new Setting("Damage", 0.7f, 0.0f, 1.0f, v -> mode.getValue() == Mode.DAMAGE && tweaks.getValue()));
    public Setting<Boolean> noBreakAnim = register(new Setting("NoBreakAnim", false));
    public Setting<Boolean> noDelay = register(new Setting("NoDelay", false));
    public Setting<Boolean> noSwing = register(new Setting("NoSwing", false));
    public Setting<Boolean> noTrace = register(new Setting("NoTrace", false));
    public Setting<Boolean> allow = register(new Setting("AllowMultiTask", false));
    public Setting<Boolean> pickaxe = register(new Setting("Pickaxe", true, v -> noTrace.getValue()));
    public Setting<Boolean> doubleBreak = register(new Setting("DoubleBreak", false));
    public Setting<Boolean> webSwitch = register(new Setting("WebSwitch", false));
    public Setting<Boolean> render = register(new Setting("Render", false));
    public Setting<Boolean> box = register(new Setting("Box", false, v -> render.getValue()));
    public Setting<Boolean> outline = register(new Setting("Outline", true, v -> render.getValue()));
    private final Setting<Integer> boxAlpha = register(new Setting("BoxAlpha", 85, 0, 255, v -> box.getValue() && render.getValue()));
    private final Setting<Float> lineWidth = register(new Setting("LineWidth", 1.0f, 0.1f, 5.0f, v -> outline.getValue() && render.getValue()));

    private static Speedmine INSTANCE = new Speedmine();

    public BlockPos currentPos;
    public IBlockState currentBlockState;
    private final Timer timer = new Timer();

    private boolean isMining = false;
    private BlockPos lastPos = null;
    private EnumFacing lastFacing = null;

    public Speedmine() {
        super("Speedmine", "Speeds up mining.", Category.PLAYER, true, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static Speedmine getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Speedmine();
        }
        return INSTANCE;
    }

    @Override
    public void onTick() {
        if(currentPos != null) {
            if(!mc.world.getBlockState(currentPos).equals(currentBlockState) || mc.world.getBlockState(currentPos).getBlock() == Blocks.AIR) {
                currentPos = null;
                currentBlockState = null;
            } else {
                if(webSwitch.getValue() && currentBlockState.getBlock() == Blocks.WEB && mc.player.getHeldItemMainhand().getItem() instanceof ItemPickaxe) {
                    InventoryUtil.switchToHotbarSlot(ItemSword.class, false);
                }
            }
        }
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }

        if(noDelay.getValue()) {
            mc.playerController.blockHitDelay = 0;
        }

        if (isMining && lastPos != null && lastFacing != null && noBreakAnim.getValue()) {
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, lastPos, lastFacing));
        }

        if(reset.getValue() && mc.gameSettings.keyBindUseItem.isKeyDown() && !allow.getValue()) {
            mc.playerController.isHittingBlock = false;
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if(render.getValue() && currentPos != null) {
            Color color = new Color((timer.passedMs((int)(2000 * Phobos.serverManager.getTpsFactor())) ? 0 : 255), (timer.passedMs((int)(2000 * Phobos.serverManager.getTpsFactor())) ? 255 : 0), 0, 255);
            RenderUtil.drawBoxESP(currentPos, color, false, color, lineWidth.getValue(), outline.getValue(), box.getValue(), boxAlpha.getValue(), false);
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (fullNullCheck()) {
            return;
        }

        if (event.getStage() == 0) {
            if (noSwing.getValue() && event.getPacket() instanceof CPacketAnimation) {
                event.setCanceled(true);
            }

            if (noBreakAnim.getValue() && event.getPacket() instanceof CPacketPlayerDigging) {
                CPacketPlayerDigging packet = event.getPacket();
                if (packet != null && packet.getPosition() != null) {
                    try {
                        for (Entity entity : mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(packet.getPosition()))) {
                            if (entity instanceof EntityEnderCrystal) {
                                showAnimation();
                                return;
                            }
                        }
                    } catch(Exception ignored) { }

                    if (packet.getAction().equals(START_DESTROY_BLOCK)) {
                        showAnimation(true, packet.getPosition(), packet.getFacing());
                    }

                    if (packet.getAction().equals(STOP_DESTROY_BLOCK)) {
                        showAnimation();
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onBlockEvent(BlockEvent event) {
        if (fullNullCheck()) {
            return;
        }

        if (event.getStage() == 3 && reset.getValue()) {
            if (mc.playerController.curBlockDamageMP > 0.1f) {
                mc.playerController.isHittingBlock = true;
            }
        }

        if (event.getStage() == 4 && tweaks.getValue()) {
            if (canBreak(event.pos)) {
                if (reset.getValue()) {
                    mc.playerController.isHittingBlock = false;
                }

                switch (this.mode.getValue()) {
                    case PACKET:
                        if(currentPos == null) {
                            currentPos = event.pos;
                            currentBlockState = mc.world.getBlockState(currentPos);
                            timer.reset();
                        }
                        mc.player.swingArm(EnumHand.MAIN_HAND);
                        mc.player.connection.sendPacket(new CPacketPlayerDigging(START_DESTROY_BLOCK, event.pos, event.facing));
                        mc.player.connection.sendPacket(new CPacketPlayerDigging(STOP_DESTROY_BLOCK, event.pos, event.facing));
                        event.setCanceled(true);
                        break;
                    case DAMAGE:
                        if (mc.playerController.curBlockDamageMP >= damage.getValue()) {
                            mc.playerController.curBlockDamageMP = 1.0f;
                        }
                        break;
                    case INSTANT:
                        mc.player.swingArm(EnumHand.MAIN_HAND);
                        mc.player.connection.sendPacket(new CPacketPlayerDigging(START_DESTROY_BLOCK, event.pos, event.facing));
                        mc.player.connection.sendPacket(new CPacketPlayerDigging(STOP_DESTROY_BLOCK, event.pos, event.facing));
                        mc.playerController.onPlayerDestroyBlock(event.pos);
                        mc.world.setBlockToAir(event.pos);
                        break;
                }
            }

            if (doubleBreak.getValue()) {
                BlockPos above = event.pos.add(0, 1, 0);
                if (canBreak(above) && mc.player.getDistance(above.getX(), above.getY(), above.getZ()) <= 5f) {
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(START_DESTROY_BLOCK, above, event.facing));
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(STOP_DESTROY_BLOCK, above, event.facing));
                    mc.playerController.onPlayerDestroyBlock(above);
                    mc.world.setBlockToAir(above);
                }
            }
        }
    }

    private void showAnimation(boolean isMining, BlockPos lastPos, EnumFacing lastFacing) {
        this.isMining = isMining;
        this.lastPos = lastPos;
        this.lastFacing = lastFacing;
    }

    public void showAnimation() {
        showAnimation(false, null, null);
    }

    @Override
    public String getDisplayInfo() {
        return mode.currentEnumName();
    }

    public enum Mode {
        PACKET,
        DAMAGE,
        INSTANT
    }
}
