package me.earth.phobos.features.modules.combat;

import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.UpdateWalkingPlayerEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.modules.player.BlockTweaks;
import me.earth.phobos.features.modules.player.Freecam;
import me.earth.phobos.features.setting.Bind;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.BlockUtil;
import me.earth.phobos.util.EntityUtil;
import me.earth.phobos.util.InventoryUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockWeb;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO: THIS
public class Selftrap extends Module {

    public Setting<Mode> mode = register(new Setting("Mode", Mode.OBSIDIAN));
    public Setting<Bind> obbyBind = register(new Setting("Obsidian", new Bind(-1)));
    public Setting<Bind> webBind = register(new Setting("Webs", new Bind(-1)));
    private final Setting<Integer> delay = register(new Setting("Delay/Place", 50, 0, 250));
    private final Setting<Integer> blocksPerTick = register(new Setting("Block/Place", 8, 1, 20));
    private final Setting<Boolean> rotate = register(new Setting("Rotate", true));
    private final Setting<Boolean> disable = register(new Setting("Disable", true));
    private final Setting<Integer> disableTime = register(new Setting("Ms/Disable", 200, 1, 250));
    private final Setting<Boolean> offhand = register(new Setting("OffHand", true));
    private final Setting<InventoryUtil.Switch> switchMode = register(new Setting("Switch", InventoryUtil.Switch.NORMAL));
    private final Setting<Boolean> onlySafe = register(new Setting("OnlySafe", true, v -> offhand.getValue()));
    private final Setting<Boolean> highWeb = register(new Setting("HighWeb", false));
    private final Setting<Boolean> freecam = register(new Setting("Freecam", false));
    private final Setting<Boolean> packet = register(new Setting("Packet", false));

    public Mode currentMode = Mode.OBSIDIAN;
    private final Timer offTimer = new Timer();
    private final Timer timer = new Timer();
    private boolean accessedViaBind = false;
    private int blocksThisTick = 0;
    private Offhand.Mode offhandMode = Offhand.Mode.CRYSTALS;
    private Offhand.Mode2 offhandMode2 = Offhand.Mode2.CRYSTALS;
    private final Map<BlockPos, Integer> retries = new HashMap<>();
    private final Timer retryTimer = new Timer();
    private boolean isSneaking;
    private boolean hasOffhand = false;
    private boolean placeHighWeb = false;
    private int lastHotbarSlot = -1;
    private boolean switchedItem = false;

    public Selftrap() {
        super("Selftrap", "Lure your enemies in!", Category.COMBAT, true, false, true);
    }

    @Override
    public void onEnable() {
        if(fullNullCheck()) {
            this.disable();
        }

        lastHotbarSlot = mc.player.inventory.currentItem;

        if(!accessedViaBind) {
            currentMode = mode.getValue();
        }

        Offhand module = Phobos.moduleManager.getModuleByClass(Offhand.class);

        offhandMode = module.mode;
        offhandMode2 = module.currentMode;

        if(offhand.getValue() && (EntityUtil.isSafe(mc.player) || !onlySafe.getValue())) {
            if(module.type.getValue() == Offhand.Type.OLD) {
                if (currentMode == Mode.WEBS) {
                    module.setMode(Offhand.Mode2.WEBS);
                } else {
                    module.setMode(Offhand.Mode2.OBSIDIAN);
                }
            } else {
                if (currentMode == Mode.WEBS) {
                    module.setSwapToTotem(false);
                    module.setMode(Offhand.Mode.WEBS);
                } else {
                    module.setSwapToTotem(false);
                    module.setMode(Offhand.Mode.OBSIDIAN);
                }
            }
        }

        Phobos.holeManager.update();
        offTimer.reset();
    }

    @Override
    public void onTick() {
        if(this.isOn() && !(blocksPerTick.getValue() == 1 && rotate.getValue())) {
            doHoleFill();
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if(this.isOn() && event.getStage() == 0 && blocksPerTick.getValue() == 1 && rotate.getValue()) {
            doHoleFill();
        }
    }

    @Override
    public void onDisable() {
        if(offhand.getValue()) {
            Phobos.moduleManager.getModuleByClass(Offhand.class).setMode(offhandMode);
            Phobos.moduleManager.getModuleByClass(Offhand.class).setMode(offhandMode2);
        }
        switchItem(true);
        isSneaking = EntityUtil.stopSneaking(isSneaking);
        retries.clear();
        accessedViaBind = false;
        hasOffhand = false;
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if(Keyboard.getEventKeyState()) {
            if (obbyBind.getValue().getKey() == Keyboard.getEventKey()) {
                accessedViaBind = true;
                currentMode = Mode.OBSIDIAN;
                this.toggle();
            }

            if (webBind.getValue().getKey() == Keyboard.getEventKey()) {
                accessedViaBind = true;
                currentMode = Mode.WEBS;
                this.toggle();
            }
        }
    }

    private void doHoleFill() {
        if(check()) {
            return;
        }

        if(placeHighWeb) {
            BlockPos pos = new BlockPos(mc.player.posX, mc.player.posY + 1, mc.player.posZ);
            placeBlock(pos);
            placeHighWeb = false;
        }

        for(BlockPos position : getPositions()) {
            int placeability = BlockUtil.isPositionPlaceable(position, false);
            if(placeability == 1) {
                switch(currentMode) {
                    case WEBS:
                        placeBlock(position);
                        break;
                    case OBSIDIAN:
                        if((switchMode.getValue() == InventoryUtil.Switch.SILENT || (BlockTweaks.getINSTANCE().isOn() && BlockTweaks.getINSTANCE().noBlock.getValue()))) {
                            if(retries.get(position) == null || retries.get(position) < 4) {
                                placeBlock(position);
                                retries.put(position, (retries.get(position) == null ? 1 : (retries.get(position) + 1)));
                            }
                        }
                        break;
                }
            }

            if(placeability == 3) {
                placeBlock(position);
            }
        }
    }

    private List<BlockPos> getPositions() {
        List<BlockPos> positions = new ArrayList<>();
        switch(currentMode) {
            case WEBS:
                positions.add(new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ));
                if(highWeb.getValue()) {
                    positions.add(new BlockPos(mc.player.posX, mc.player.posY + 1, mc.player.posZ));
                }
                break;
            case OBSIDIAN:
                positions.add(new BlockPos(mc.player.posX, mc.player.posY + 2, mc.player.posZ));
                int placeability = BlockUtil.isPositionPlaceable(positions.get(0), false);
                switch(placeability) {
                    case 0:
                        return new ArrayList<>();
                    case 3:
                        return positions;
                    case 1:
                        if (BlockUtil.isPositionPlaceable(positions.get(0), false, false) == 3) {
                            return positions;
                        }
                    case 2:
                        positions.add(new BlockPos(mc.player.posX + 1, mc.player.posY + 1, mc.player.posZ));
                        positions.add(new BlockPos(mc.player.posX + 1, mc.player.posY + 2, mc.player.posZ));
                        break;
                    default:
                }
        }
        positions.sort(Comparator.comparingDouble(BlockPos::getY));
        return positions;
    }

    private void placeBlock(BlockPos pos) {
        if(blocksThisTick < blocksPerTick.getValue()) {
            if(switchItem(false)) {
                boolean smartRotate = blocksPerTick.getValue() == 1 && rotate.getValue();
                if (smartRotate) {
                    isSneaking = BlockUtil.placeBlockSmartRotate(pos, hasOffhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, true, packet.getValue(), isSneaking);
                } else {
                    isSneaking = BlockUtil.placeBlock(pos, hasOffhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, rotate.getValue(), packet.getValue(), isSneaking);
                }
                timer.reset();
                //placedThisTick = true;
                blocksThisTick++;
            }
        }
    }

    private boolean check() {
        if(fullNullCheck() || (disable.getValue() && offTimer.passedMs(disableTime.getValue()))) {
            this.disable();
            return true;
        }

        if(mc.player.inventory.currentItem != lastHotbarSlot && mc.player.inventory.currentItem != InventoryUtil.findHotbarBlock(currentMode == Mode.WEBS ? BlockWeb.class : BlockObsidian.class)) {
            lastHotbarSlot = mc.player.inventory.currentItem;
        }

        switchItem(true);

        if(!freecam.getValue() && Phobos.moduleManager.isModuleEnabled(Freecam.class)) {
            return true;
        }

        //placedThisTick = false;
        blocksThisTick = 0;
        isSneaking = EntityUtil.stopSneaking(isSneaking);

        if(retryTimer.passedMs(2000)) {
            retries.clear();
            retryTimer.reset();
        }

        int targetSlot = -1;
        switch (currentMode) {
            case WEBS:
                hasOffhand = InventoryUtil.isBlock(mc.player.getHeldItemOffhand().getItem(), BlockWeb.class);
                targetSlot = InventoryUtil.findHotbarBlock(BlockWeb.class);
                break;
            case OBSIDIAN:
                hasOffhand = InventoryUtil.isBlock(mc.player.getHeldItemOffhand().getItem(), BlockObsidian.class);
                targetSlot = InventoryUtil.findHotbarBlock(BlockObsidian.class);
                break;
            default:
        }

        if(onlySafe.getValue() && !EntityUtil.isSafe(mc.player)) {
            this.disable();
            return true;
        }

        if(!hasOffhand && targetSlot == -1 && !(offhand.getValue() && (EntityUtil.isSafe(mc.player) || !onlySafe.getValue()))) {
            return true;
        }

        if(offhand.getValue() && !hasOffhand) {
            return true;
        }

        return !timer.passedMs(delay.getValue());
    }

    private boolean switchItem(boolean back) {
        if(offhand.getValue()) {
            return true;
        }
        boolean[] value = InventoryUtil.switchItem(back, lastHotbarSlot, switchedItem, switchMode.getValue(), currentMode == Mode.WEBS ? BlockWeb.class : BlockObsidian.class);
        switchedItem = value[0];
        return value[1];
    }

    public enum Mode {
        WEBS,
        OBSIDIAN
    }
}
