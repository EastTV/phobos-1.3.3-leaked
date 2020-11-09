package me.earth.phobos.features.modules.combat;

import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.UpdateWalkingPlayerEvent;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.modules.player.BlockTweaks;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.BlockUtil;
import me.earth.phobos.util.EntityUtil;
import me.earth.phobos.util.InventoryUtil;
import me.earth.phobos.util.TextUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Surround extends Module {

    private final Setting<Integer> delay = register(new Setting("Delay/Place", 50, 0, 250));
    private final Setting<Integer> blocksPerTick = register(new Setting("Block/Place", 8, 1, 20));
    private final Setting<Boolean> rotate = register(new Setting("Rotate", true));
    private final Setting<Boolean> raytrace = register(new Setting("Raytrace", false));
    private final Setting<InventoryUtil.Switch> switchMode = register(new Setting("Switch", InventoryUtil.Switch.NORMAL));
    private final Setting<Boolean> center = register(new Setting("Center", false));
    private final Setting<Boolean> helpingBlocks = register(new Setting("HelpingBlocks", true));
    private final Setting<Boolean> intelligent = register(new Setting("Intelligent", false, v -> helpingBlocks.getValue()));
    private final Setting<Boolean> antiPedo = register(new Setting("NoPedo", false));
    private final Setting<Integer> extender = register(new Setting("Extend", 1, 1, 4));
    private final Setting<Boolean> extendMove = register(new Setting("MoveExtend", false, v -> extender.getValue() > 1));
    private final Setting<MovementMode> movementMode = register(new Setting("Movement", MovementMode.STATIC));
    private final Setting<Double> speed = register(new Setting("Speed", 10.0, 0.0, 30.0, v -> movementMode.getValue() == MovementMode.LIMIT || movementMode.getValue() == MovementMode.OFF, "Maximum Movement Speed"));
    private final Setting<Integer> eventMode = register(new Setting("Updates", 3, 1, 3));
    private final Setting<Boolean> floor = register(new Setting("Floor", false));
    private final Setting<Boolean> echests = register(new Setting("Echests", false));
    private final Setting<Boolean> noGhost = register(new Setting("Packet", false));
    private final Setting<Boolean> info = register(new Setting("Info", false));
    private final Setting<Integer> retryer = register(new Setting("Retries", 4, 1, 15));

    private final Timer timer = new Timer();
    private final Timer retryTimer = new Timer();
    private int isSafe;
    private BlockPos startPos;
    private boolean didPlace = false;
    private boolean switchedItem;
    private int lastHotbarSlot;
    private boolean isSneaking;
    private int placements = 0;
    private final Set<Vec3d> extendingBlocks = new HashSet<>();
    private int extenders = 1;
    public static boolean isPlacing = false;
    private int obbySlot = -1;
    private boolean offHand = false;
    private final Map<BlockPos, Integer> retries = new HashMap<>();

    public Surround() {
        super("Surround", "Surrounds you with Obsidian", Category.COMBAT, true, false, false);
    }

    @Override
    public void onEnable() {
        if(fullNullCheck()) {
            this.disable();
        }
        lastHotbarSlot = mc.player.inventory.currentItem;
        startPos = EntityUtil.getRoundedBlockPos(mc.player);
        if (center.getValue() && !Phobos.moduleManager.isModuleEnabled("Freecam")) {
            Phobos.positionManager.setPositionPacket(startPos.getX() + 0.5, startPos.getY(), startPos.getZ() + 0.5, true, true, true);
        }
        retries.clear();
        retryTimer.reset();
    }

    @Override
    public void onTick() {
        if(eventMode.getValue() == 3) {
            doFeetPlace();
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if(event.getStage() == 0 && eventMode.getValue() == 2) {
            doFeetPlace();
        }
    }

    @Override
    public void onUpdate() {
        if(eventMode.getValue() == 1) {
            doFeetPlace();
        }
    }

    @Override
    public void onDisable() {
        if(nullCheck()) {
            return;
        }
        isPlacing = false;
        isSneaking = EntityUtil.stopSneaking(isSneaking);
        switchItem(true);
    }

    @Override
    public String getDisplayInfo() {
        if(!info.getValue()) {
            return null;
        }

        switch(isSafe) {
            case 0:
                return TextUtil.RED + "Unsafe";
            case 1:
                return TextUtil.YELLOW + "Secure";
            default:
                return TextUtil.GREEN + "Secure";
        }
    }

    private void doFeetPlace() {
        if(check()) {
            return;
        }

        if(!EntityUtil.isSafe(mc.player, 0, floor.getValue())) {
            isSafe = 0;
            placeBlocks(mc.player.getPositionVector(), EntityUtil.getUnsafeBlockArray(mc.player, 0, floor.getValue()), helpingBlocks.getValue(), false, false);
        } else if(!EntityUtil.isSafe(mc.player, -1, false)) {
            isSafe = 1;
            if(antiPedo.getValue()) {
                placeBlocks(mc.player.getPositionVector(), EntityUtil.getUnsafeBlockArray(mc.player, -1, false), false, false, true);
            }
        } else {
            isSafe = 2;
        }

        processExtendingBlocks();

        if(didPlace) {
            timer.reset();
        }
    }

    private void processExtendingBlocks() {
        if(extendingBlocks.size() == 2 && extenders < extender.getValue()) {
            Vec3d[] array = new Vec3d[2];
            int i = 0;
            for(Vec3d vec3d : extendingBlocks) {
                array[i] = vec3d;
                i++;
            }
            int placementsBefore = placements;
            if(areClose(array) != null) {
                placeBlocks(areClose(array), EntityUtil.getUnsafeBlockArrayFromVec3d(areClose(array), 0, floor.getValue()), helpingBlocks.getValue(), false, true);
            }

            if(placementsBefore < placements) {
                extendingBlocks.clear();
            }
        } else if(extendingBlocks.size() > 2 || !(extenders < extender.getValue())) {
            extendingBlocks.clear();
        }
    }

    private Vec3d areClose(Vec3d[] vec3ds) {
        int matches = 0;
        for(Vec3d vec3d : vec3ds) {
            for(Vec3d pos : EntityUtil.getUnsafeBlockArray(mc.player, 0, floor.getValue())) {
                if(vec3d.equals(pos)) {
                    matches++;
                }
            }
        }
        if(matches == 2) {
            return mc.player.getPositionVector().add(vec3ds[0].add(vec3ds [1]));
        }
        return null;
    }

    private boolean placeBlocks(Vec3d pos, Vec3d[] vec3ds, boolean hasHelpingBlocks, boolean isHelping, boolean isExtending) {
        int helpings = 0;
        boolean gotHelp = true;
        for (Vec3d vec3d : vec3ds) {
            gotHelp = true;
            helpings++;
            if(isHelping && !intelligent.getValue() && helpings > 1) {
                return false;
            }
            BlockPos position = new BlockPos(pos).add(vec3d.x, vec3d.y, vec3d.z);
            switch (BlockUtil.isPositionPlaceable(position, raytrace.getValue())) {
                case -1:
                    continue;
                case 1:
                    if((switchMode.getValue() == InventoryUtil.Switch.SILENT || (BlockTweaks.getINSTANCE().isOn() && BlockTweaks.getINSTANCE().noBlock.getValue())) && (retries.get(position) == null || retries.get(position) < retryer.getValue())) {
                        placeBlock(position);
                        retries.put(position, (retries.get(position) == null ? 1 : (retries.get(position) + 1)));
                        retryTimer.reset();
                        continue;
                    }

                    if((extendMove.getValue() || Phobos.speedManager.getSpeedKpH() == 0.0) && !isExtending && extenders < extender.getValue()) {
                        placeBlocks(mc.player.getPositionVector().add(vec3d), EntityUtil.getUnsafeBlockArrayFromVec3d(mc.player.getPositionVector().add(vec3d), 0, floor.getValue()), hasHelpingBlocks, false, true);
                        extendingBlocks.add(vec3d);
                        extenders++;
                    }
                    continue;
                case 2:
                    if (hasHelpingBlocks) {
                        gotHelp = placeBlocks(pos, BlockUtil.getHelpingBlocks(vec3d), false, true, true);
                    } else {
                        continue;
                    }
                case 3:
                    if(gotHelp) {
                        placeBlock(position);
                    }
                    if(isHelping) {
                        return true;
                    }
            }
        }
        return false;
    }

    private boolean check() {
        if(nullCheck()) {
            return true;
        }

        offHand = InventoryUtil.isBlock(mc.player.getHeldItemOffhand().getItem(), BlockObsidian.class);
        isPlacing = false;
        didPlace = false;
        extenders = 1;
        placements = 0;
        obbySlot = InventoryUtil.findHotbarBlock(BlockObsidian.class);
        int echestSlot = InventoryUtil.findHotbarBlock(BlockEnderChest.class);

        if(this.isOff()) {
            return true;
        }

        if(retryTimer.passedMs(2500)) {
            retries.clear();
            retryTimer.reset();
        }

        switchItem(true);

        if(obbySlot == -1 && !offHand) {
            if(!echests.getValue() || echestSlot == -1) {
                if (info.getValue()) {
                    Command.sendMessage("<" + this.getDisplayName() + "> " + TextUtil.RED + "You are out of Obsidian.");
                }
                this.disable();
                return true;
            }
        }

        isSneaking = EntityUtil.stopSneaking(isSneaking);

        if(mc.player.inventory.currentItem != lastHotbarSlot && mc.player.inventory.currentItem != obbySlot && mc.player.inventory.currentItem != echestSlot) {
            lastHotbarSlot = mc.player.inventory.currentItem;
        }

        switch(movementMode.getValue()) {
            case NONE:
                break;
            case STATIC:
                if(!startPos.equals(EntityUtil.getRoundedBlockPos(mc.player))) {
                    this.disable();
                    return true;
                }
            case LIMIT:
                if(Phobos.speedManager.getSpeedKpH() > speed.getValue()) {
                    return true;
                }
                break;
            case OFF:
                if(Phobos.speedManager.getSpeedKpH() > speed.getValue()) {
                    this.disable();
                    return true;
                }
        }
        return Phobos.moduleManager.isModuleEnabled("Freecam") || !timer.passedMs(delay.getValue()) || switchMode.getValue() == InventoryUtil.Switch.NONE && mc.player.inventory.currentItem != InventoryUtil.findHotbarBlock(BlockObsidian.class);
    }

    private void placeBlock(BlockPos pos) {
        if(placements < blocksPerTick.getValue() && switchItem(false)) {
            isPlacing = true;
            isSneaking = BlockUtil.placeBlock(pos, offHand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, rotate.getValue(), noGhost.getValue(), isSneaking);
            didPlace = true;
            placements++;
        }
    }

    private boolean switchItem(boolean back) {
        if(offHand) {
            return true;
        }
        boolean[] value = InventoryUtil.switchItem(back, lastHotbarSlot, switchedItem, switchMode.getValue(), obbySlot == -1 ? BlockEnderChest.class : BlockObsidian.class);
        switchedItem = value[0];
        return value[1];
    }

    public enum MovementMode {
        NONE,
        STATIC,
        LIMIT,
        OFF
    }
}
