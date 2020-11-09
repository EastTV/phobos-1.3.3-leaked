package me.earth.phobos.features.modules.combat;

import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.UpdateWalkingPlayerEvent;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.BlockUtil;
import me.earth.phobos.util.EntityUtil;
import me.earth.phobos.util.InventoryUtil;
import me.earth.phobos.util.MathUtil;
import me.earth.phobos.util.TextUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.block.BlockWeb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

//TODO: THIS
public class Webaura extends Module {

    private final Setting<Integer> delay = register(new Setting("Delay/Place", 50, 0, 250));
    private final Setting<Integer> blocksPerPlace = register(new Setting("Block/Place", 8, 1, 30));
    private final Setting<Double> targetRange = register(new Setting("TargetRange", 10.0, 0.0, 20.0));
    private final Setting<Double> range = register(new Setting("PlaceRange", 6.0, 0.0, 10.0));
    private final Setting<TargetMode> targetMode = register(new Setting("Target", TargetMode.CLOSEST));
    private final Setting<InventoryUtil.Switch> switchMode = register(new Setting("Switch", InventoryUtil.Switch.NORMAL));
    private final Setting<Boolean> rotate = register(new Setting("Rotate", true));
    private final Setting<Boolean> raytrace = register(new Setting("Raytrace", false));
    private final Setting<Double> speed = register(new Setting("Speed", 30.0, 0.0, 30.0));
    private final Setting<Boolean> upperBody = register(new Setting("Upper", false));
    private final Setting<Boolean> lowerbody = register(new Setting("Lower", true));
    private final Setting<Boolean> ylower = register(new Setting("Y-1", false));
    private final Setting<Boolean> antiSelf = register(new Setting("AntiSelf", false));
    private final Setting<Integer> eventMode = register(new Setting("Updates", 3, 1, 3));
    private final Setting<Boolean> freecam = register(new Setting("Freecam", false));
    private final Setting<Boolean> info = register(new Setting("Info", false));
    private final Setting<Boolean> disable = register(new Setting("TSelfMove", false));
    private final Setting<Boolean> packet = register(new Setting("Packet", false));

    private final Timer timer = new Timer();
    private boolean didPlace = false;
    private boolean switchedItem;
    public EntityPlayer target;
    private boolean isSneaking;
    private int lastHotbarSlot;
    private int placements = 0;
    public static boolean isPlacing = false;
    private boolean smartRotate = false;
    private BlockPos startPos = null;

    public Webaura() {
        super("Webaura", "Traps other players in webs", Category.COMBAT, true, false, false);
    }

    @Override
    public void onEnable() {
        if(fullNullCheck()) return;
        startPos = EntityUtil.getRoundedBlockPos(mc.player);
        lastHotbarSlot = mc.player.inventory.currentItem;
    }

    @Override
    public void onTick() {
        if(eventMode.getValue() == 3) {
            smartRotate = false;
            doTrap();
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if(event.getStage() == 0 && eventMode.getValue() == 2) {
            smartRotate = rotate.getValue() && blocksPerPlace.getValue() == 1;
            doTrap();
        }
    }

    @Override
    public void onUpdate() {
        if(eventMode.getValue() == 1) {
            smartRotate = false;
            doTrap();
        }
    }

    @Override
    public String getDisplayInfo() {
        if(info.getValue() && target != null) {
            return target.getName();
        }
        return null;
    }

    @Override
    public void onDisable() {
        isPlacing = false;
        isSneaking = EntityUtil.stopSneaking(isSneaking);
        switchItem(true);
    }

    private void doTrap() {
        if(check()) {
            return;
        }

        doWebTrap();

        if(didPlace) {
            timer.reset();
        }
    }

    private void doWebTrap() {
        List<Vec3d> placeTargets = getPlacements();
        placeList(placeTargets);
    }

    private List<Vec3d> getPlacements() {
        List<Vec3d> list = new ArrayList<>();
        Vec3d baseVec = target.getPositionVector();
        if(ylower.getValue()) {
            list.add(baseVec.add(0, -1, 0));
        }

        if(lowerbody.getValue()) {
            list.add(baseVec);
        }

        if(upperBody.getValue()) {
            list.add(baseVec.add(0, 1, 0));
        }

        return list;
    }

    private void placeList(List<Vec3d> list) {
        list.sort((vec3d, vec3d2) -> Double.compare(mc.player.getDistanceSq(vec3d2.x, vec3d2.y, vec3d2.z), mc.player.getDistanceSq(vec3d.x, vec3d.y, vec3d.z)));
        list.sort(Comparator.comparingDouble(vec3d -> vec3d.y));

        for(Vec3d vec3d : list) {
            BlockPos position = new BlockPos(vec3d);
            int placeability = BlockUtil.isPositionPlaceable(position, raytrace.getValue());
            if ((placeability == 3 || placeability == 1) && (!antiSelf.getValue() || !MathUtil.areVec3dsAligned(mc.player.getPositionVector(), vec3d))) {
                placeBlock(position);
            }
        }
    }

    private boolean check() {
        isPlacing = false;
        didPlace = false;
        placements = 0;
        int obbySlot = InventoryUtil.findHotbarBlock(BlockWeb.class);

        if(this.isOff()) {
            return true;
        }

        if(disable.getValue() && !startPos.equals(EntityUtil.getRoundedBlockPos(mc.player))) {
            this.disable();
            return true;
        }

        if(obbySlot == -1) {
            if(switchMode.getValue() != InventoryUtil.Switch.NONE) {
                if(info.getValue()) {
                    Command.sendMessage("<" + this.getDisplayName() + "> " + TextUtil.RED + "You are out of Obsidian.");
                }
                this.disable();
            }
            return true;
        }

        if(mc.player.inventory.currentItem != lastHotbarSlot && mc.player.inventory.currentItem != obbySlot) {
            lastHotbarSlot = mc.player.inventory.currentItem;
        }

        switchItem(true);
        isSneaking = EntityUtil.stopSneaking(isSneaking);
        target = getTarget(targetRange.getValue(), targetMode.getValue() == TargetMode.UNTRAPPED);

        return target == null || (Phobos.moduleManager.isModuleEnabled("Freecam") && !freecam.getValue()) || !timer.passedMs(delay.getValue()) || switchMode.getValue() == InventoryUtil.Switch.NONE && mc.player.inventory.currentItem != InventoryUtil.findHotbarBlock(BlockWeb.class);
    }

    private EntityPlayer getTarget(double range, boolean trapped) {
        EntityPlayer target = null;
        double distance = Math.pow(range, 2) + 1;
        for(EntityPlayer player : mc.world.playerEntities) {
            if(EntityUtil.isntValid(player, range)) {
                continue;
            }

            if(trapped && player.isInWeb) {
                continue;
            }

            if(EntityUtil.getRoundedBlockPos(mc.player).equals(EntityUtil.getRoundedBlockPos(player)) && antiSelf.getValue()) {
                continue;
            }

            if(Phobos.speedManager.getPlayerSpeed(player) > speed.getValue()) {
                continue;
            }

            if(target == null) {
                target = player;
                distance = mc.player.getDistanceSq(player);
                continue;
            }

            if(mc.player.getDistanceSq(player) < distance) {
                target = player;
                distance = mc.player.getDistanceSq(player);
            }
        }
        return target;
    }

    private void placeBlock(BlockPos pos) {
        if(placements < blocksPerPlace.getValue() && mc.player.getDistanceSq(pos) <= MathUtil.square(range.getValue()) && switchItem(false)) {
            isPlacing = true;
            if(smartRotate) {
                isSneaking = BlockUtil.placeBlockSmartRotate(pos, EnumHand.MAIN_HAND, true, packet.getValue(), isSneaking);
            } else {
                isSneaking = BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, rotate.getValue(), packet.getValue(), isSneaking);
            }
            didPlace = true;
            placements++;
        }
    }

    private boolean switchItem(boolean back) {
        boolean[] value = InventoryUtil.switchItem(back, lastHotbarSlot, switchedItem, switchMode.getValue(), BlockWeb.class);
        switchedItem = value[0];
        return value[1];
    }

    public enum TargetMode {
        CLOSEST,
        UNTRAPPED
    }
}
