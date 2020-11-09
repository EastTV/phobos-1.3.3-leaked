package me.earth.phobos.features.modules.combat;

import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.event.events.Render3DEvent;
import me.earth.phobos.event.events.UpdateWalkingPlayerEvent;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.gui.PhobosGui;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.modules.misc.Tracker;
import me.earth.phobos.features.setting.Bind;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.BlockUtil;
import me.earth.phobos.util.DamageUtil;
import me.earth.phobos.util.EntityUtil;
import me.earth.phobos.util.InventoryUtil;
import me.earth.phobos.util.MathUtil;
import me.earth.phobos.util.RenderUtil;
import me.earth.phobos.util.TextUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEndCrystal;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AutoCrystal extends Module {

    private final Setting<Settings> setting = register(new Setting<>("Settings", Settings.PLACE));

    public Setting<Raytrace> raytrace = register(new Setting("Raytrace", Raytrace.NONE, v -> setting.getValue() == Settings.MISC));

    //PLACE
    public Setting<Boolean> place = register(new Setting("Place", true, v -> setting.getValue() == Settings.PLACE));
    public Setting<Integer> placeDelay = register(new Setting("PlaceDelay", 0, 0, 1000, v -> setting.getValue() == Settings.PLACE && place.getValue()));
    public Setting<Float> placeRange = register(new Setting("PlaceRange", 6.0f, 0.0f, 10.0f, v -> setting.getValue() == Settings.PLACE && place.getValue()));
    public Setting<Float> minDamage = register(new Setting("MinDamage", 4.0f, 0.1f, 20.0f, v -> setting.getValue() == Settings.PLACE && place.getValue()));
    public Setting<Integer> wasteAmount = register(new Setting("WasteAmount", 1, 1, 5, v -> setting.getValue() == Settings.PLACE && place.getValue()));
    public Setting<Boolean> wasteMinDmgCount = register(new Setting("CountMinDmg", true, v -> setting.getValue() == Settings.PLACE && place.getValue()));
    public Setting<Float> facePlace = register(new Setting("FacePlace", 8.0f, 0.1f, 20.0f, v -> setting.getValue() == Settings.PLACE && place.getValue()));
    public Setting<Float> placetrace = register(new Setting("Placetrace", 6.0f, 0.0f, 10.0f, v -> setting.getValue() == Settings.PLACE && place.getValue() && raytrace.getValue() != Raytrace.NONE && raytrace.getValue() != Raytrace.BREAK));
    public Setting<Boolean> antiSurround = register(new Setting("AntiSurround", false, v -> setting.getValue() == Settings.PLACE && place.getValue()));

    //BREAK
    public Setting<Boolean> explode = register(new Setting("Break", true, v -> setting.getValue() == Settings.BREAK));
    public Setting<Switch> switchMode = register(new Setting("Attack", Switch.BREAKSLOT, v -> setting.getValue() == Settings.BREAK && explode.getValue()));
    public Setting<Integer> breakDelay = register(new Setting("BreakDelay", 0, 0, 1000, v -> setting.getValue() == Settings.BREAK && explode.getValue()));
    public Setting<Float> breakRange = register(new Setting("BreakRange", 6.0f, 0.0f, 10.0f, v -> setting.getValue() == Settings.BREAK && explode.getValue()));
    public Setting<Integer> packets = register(new Setting("Packets", 1, 1, 6, v -> setting.getValue() == Settings.BREAK && explode.getValue()));
    public Setting<Float> breaktrace = register(new Setting("Breaktrace", 6.0f, 0.0f, 10.0f, v -> setting.getValue() == Settings.BREAK && explode.getValue() && raytrace.getValue() != Raytrace.NONE && raytrace.getValue() != Raytrace.PLACE));
    public Setting<Boolean> manual = register(new Setting("Manual", false, v -> setting.getValue() == Settings.BREAK));
    public Setting<Boolean> manualMinDmg = register(new Setting("ManMinDmg", false, v -> setting.getValue() == Settings.BREAK && manual.getValue()));
    public Setting<Integer> manualBreak = register(new Setting("ManualDelay", 500, 0, 1000, v -> setting.getValue() == Settings.BREAK && manual.getValue()));
    public Setting<Boolean> sync = register(new Setting("Sync", true, v -> setting.getValue() == Settings.BREAK && (explode.getValue() || manual.getValue())));

    //RENDER
    public Setting<Boolean> render = register(new Setting("Render", true, v -> setting.getValue() == Settings.RENDER));
    public Setting<Boolean> box = register(new Setting("Box", true, v -> setting.getValue() == Settings.RENDER && render.getValue()));
    public Setting<Boolean> outline = register(new Setting("Outline", true, v -> setting.getValue() == Settings.RENDER && render.getValue()));
    public Setting<Boolean> text = register(new Setting("Text", false, v -> setting.getValue() == Settings.RENDER && render.getValue()));
    private final Setting<Integer> red = register(new Setting("Red", 255, 0, 255, v -> setting.getValue() == Settings.RENDER && render.getValue()));
    private final Setting<Integer> green = register(new Setting("Green", 255, 0, 255, v -> setting.getValue() == Settings.RENDER && render.getValue()));
    private final Setting<Integer> blue = register(new Setting("Blue", 255, 0, 255, v -> setting.getValue() == Settings.RENDER && render.getValue()));
    private final Setting<Integer> alpha = register(new Setting("Alpha", 255, 0, 255, v -> setting.getValue() == Settings.RENDER && render.getValue()));
    private final Setting<Integer> boxAlpha = register(new Setting("BoxAlpha", 125, 0, 255, v -> setting.getValue() == Settings.RENDER && render.getValue() && box.getValue()));
    private final Setting<Float> lineWidth = register(new Setting("LineWidth", 1.5f, 0.1f, 5.0f, v -> setting.getValue() == Settings.RENDER && render.getValue() && outline.getValue()));
    public Setting<Boolean> customOutline = register(new Setting("CustomLine", false, v -> setting.getValue() == Settings.RENDER && render.getValue() && outline.getValue()));
    private final Setting<Integer> cRed = register(new Setting("OL-Red", 255, 0, 255, v -> setting.getValue() == Settings.RENDER && render.getValue() && customOutline.getValue() && outline.getValue()));
    private final Setting<Integer> cGreen = register(new Setting("OL-Green", 255, 0, 255, v -> setting.getValue() == Settings.RENDER && render.getValue() && customOutline.getValue() && outline.getValue()));
    private final Setting<Integer> cBlue = register(new Setting("OL-Blue", 255, 0, 255, v -> setting.getValue() == Settings.RENDER && render.getValue() && customOutline.getValue() && outline.getValue()));
    private final Setting<Integer> cAlpha = register(new Setting("OL-Alpha", 255, 0, 255, v -> setting.getValue() == Settings.RENDER && render.getValue() && customOutline.getValue() && outline.getValue()));

    //MISC
    public Setting<Float> range = register(new Setting("Range", 12.0f, 0.1f, 20.0f, v -> setting.getValue() == Settings.MISC));
    public Setting<Target> targetMode = register(new Setting("Target", Target.CLOSEST, v -> setting.getValue() == Settings.MISC));
    public Setting<Integer> minArmor = register(new Setting("MinArmor", 0, 0, 125, v -> setting.getValue() == Settings.MISC));
    private final Setting<Integer> switchCooldown = register(new Setting("Cooldown", 500, 0, 1000, v -> setting.getValue() == Settings.MISC));
    public Setting<AutoSwitch> autoSwitch = register(new Setting("Switch", AutoSwitch.TOGGLE, v -> setting.getValue() == Settings.MISC));
    public Setting<Bind> switchBind = register(new Setting("SwitchBind", new Bind(-1), v -> setting.getValue() == Settings.MISC && autoSwitch.getValue() == AutoSwitch.TOGGLE));
    public Setting<Boolean> offhandSwitch = register(new Setting("Offhand", false, v -> setting.getValue() == Settings.MISC && autoSwitch.getValue() != AutoSwitch.NONE));
    public Setting<Boolean> switchBack = register(new Setting("Switchback", false, v -> setting.getValue() == Settings.MISC && autoSwitch.getValue() != AutoSwitch.NONE && offhandSwitch.getValue()));
    public Setting<Boolean> lethalSwitch = register(new Setting("LethalSwitch", false, v -> setting.getValue() == Settings.MISC && autoSwitch.getValue() != AutoSwitch.NONE));
    public Setting<Boolean> mineSwitch = register(new Setting("MineSwitch", false, v -> setting.getValue() == Settings.MISC && autoSwitch.getValue() != AutoSwitch.NONE));
    public Setting<Rotate> rotate = register(new Setting("Rotate", Rotate.OFF, v -> setting.getValue() == Settings.MISC));
    private final Setting<Integer> eventMode = register(new Setting("Updates", 3, 1, 3, v -> setting.getValue() == Settings.MISC));
    public Setting<Logic> logic = register(new Setting("Logic", Logic.BREAKPLACE, v -> setting.getValue() == Settings.MISC));
    public Setting<Boolean> doubleMap = register(new Setting("DoubleMap", false, v -> setting.getValue() == Settings.MISC && logic.getValue() == Logic.PLACEBREAK));
    public Setting<Boolean> suicide = register(new Setting("Suicide", false, v -> setting.getValue() == Settings.MISC));
    public Setting<Boolean> webAttack = register(new Setting("WebAttack", false, v -> setting.getValue() == Settings.MISC && targetMode.getValue() != Target.DAMAGE));
    public Setting<Boolean> fullCalc = register(new Setting("ExtraCalc", false, v -> setting.getValue() == Settings.MISC));
    public Setting<Boolean> extraSelfCalc = register(new Setting("MinSelfDmg", true, v -> setting.getValue() == Settings.MISC));
    public Setting<DamageSync> damageSync = register(new Setting("DamageSync", DamageSync.NONE, v -> setting.getValue() == Settings.MISC));
    public Setting<Integer> damageSyncTime = register(new Setting("SyncDelay", 500, 0, 1000, v -> setting.getValue() == Settings.MISC && damageSync.getValue() != DamageSync.NONE));
    public Setting<Float> dropOff = register(new Setting("DropOff", 5.0f, 0.0f, 10.0f, v -> setting.getValue() == Settings.MISC && damageSync.getValue() == DamageSync.BREAK));
    public Setting<Integer> confirm = register(new Setting("Confirm", 250, 0, 1000, v -> setting.getValue() == Settings.MISC && damageSync.getValue() != DamageSync.NONE));

    private Queue<Entity> attackList = new ConcurrentLinkedQueue<>();
    private Map<Entity, Float> crystalMap = new HashMap<>();
    private final Timer switchTimer = new Timer();
    private final Timer manualTimer = new Timer();
    private final Timer breakTimer = new Timer();
    private final Timer placeTimer = new Timer();
    private final Timer syncTimer = new Timer();
    public static EntityPlayer target = null;
    private Entity efficientTarget = null;
    private double currentDamage = 0.0;
    private double renderDamage = 0.0;
    private double lastDamage = 0.0;
    private boolean didRotation = false;
    private boolean switching = false;
    private BlockPos placePos = null;
    private BlockPos renderPos = null;
    private boolean mainHand = false;
    private boolean rotating = false;
    private boolean offHand = false;
    private int crystalCount = 0;
    private int minDmgCount = 0;
    private int lastSlot = -1;
    private float yaw = 0.0f;
    private float pitch = 0.0f;
    private BlockPos webPos = null;
    private final Timer renderTimer = new Timer();
    private BlockPos lastPos = null;
    public static ArrayList<BlockPos> placedPos = new ArrayList<>();
    public static ArrayList<BlockPos> brokenPos = new ArrayList<>();
    private boolean posConfirmed = false;

    public AutoCrystal() {
        super("AutoCrystal", "Best CA on the market", Category.COMBAT, true, false, false);
    }

    @Override
    public void onTick() {
        if (eventMode.getValue() == 3) {
            doAutoCrystal();
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (event.getStage() == 0 && eventMode.getValue() == 2) {
            doAutoCrystal();
        }
    }

    @Override
    public void onUpdate() {
        if (eventMode.getValue() == 1) {
            doAutoCrystal();
        }
    }

    @Override
    public void onDisable() {
        rotating = false;
    }

    @Override
    public String getDisplayInfo() {
        if (switching) {
            return TextUtil.GREEN + "Switch";
        }

        if (target != null) {
            return target.getName();
        }

        return null;
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getStage() == 0 && (rotate.getValue() != Rotate.OFF && rotating && eventMode.getValue() != 2)) {
            if (event.getPacket() instanceof CPacketPlayer) {
                CPacketPlayer packet = event.getPacket();
                packet.yaw = this.yaw;
                packet.pitch = this.pitch;
                rotating = false;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onPacketReceive(PacketEvent.Receive event) {
        if(event.getPacket() instanceof SPacketExplosion) {
            SPacketExplosion packet = event.getPacket();
            BlockPos pos = new BlockPos(packet.getX(), packet.getY(), packet.getZ()).down();
            if(damageSync.getValue() == DamageSync.PLACE) {
                if (placedPos.contains(pos)) {
                    if (!Tracker.getInstance().isOn()) {
                        placedPos.remove(pos);
                    }
                    posConfirmed = true;
                }
            } else if(damageSync.getValue() == DamageSync.BREAK) {
                if(brokenPos.contains(pos)) {
                    brokenPos.remove(pos);
                    posConfirmed = true;
                }
            }
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if ((offHand || mainHand || switchMode.getValue() == Switch.CALC) && renderPos != null && render.getValue() && (box.getValue() || text.getValue() || outline.getValue())) {
            RenderUtil.drawBoxESP(renderPos, new Color(red.getValue(), green.getValue(), blue.getValue(), alpha.getValue()), customOutline.getValue(), new Color(cRed.getValue(), cGreen.getValue(), cBlue.getValue(), cAlpha.getValue()), lineWidth.getValue(), outline.getValue(), box.getValue(), boxAlpha.getValue(), false);
            if (text.getValue()) {
                RenderUtil.drawText(renderPos, (Math.floor(renderDamage) == renderDamage ? (int) renderDamage : String.format("%.1f", renderDamage)) + "");
            }
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if(Keyboard.getEventKeyState() && !(mc.currentScreen instanceof PhobosGui) && switchBind.getValue().getKey() == Keyboard.getEventKey()) {
            if(switchBack.getValue() && offhandSwitch.getValue() && offHand) {
                Offhand module = Phobos.moduleManager.getModuleByClass(Offhand.class);
                if(module.isOff()) {
                    Command.sendMessage("<" + this.getDisplayName() + "> " + TextUtil.RED + "Switch failed. Enable the Offhand module.");
                } else {
                    if(module.type.getValue() == Offhand.Type.NEW) {
                        module.setSwapToTotem(true);
                        module.doOffhand();
                    } else {
                        module.setMode(Offhand.Mode2.TOTEMS);
                        module.doSwitch();
                    }
                }
                return;
            }
            switching = !switching;
        }
    }

    private void doAutoCrystal() {
        if (check()) {
            switch (logic.getValue()) {
                case PLACEBREAK:
                    placeCrystal();
                    if (doubleMap.getValue()) {
                        mapCrystals();
                    }
                    breakCrystal();
                    break;
                case BREAKPLACE:
                    breakCrystal();
                    placeCrystal();
                    break;
                default:
            }
            manualBreaker();
        }
    }

    private boolean check() {
        if (fullNullCheck()) {
            return false;
        }

        if (renderTimer.passedMs(500)) {
            renderPos = null;
            renderTimer.reset();
            //TODO: retard
        }

        mainHand = mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL;
        offHand = mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL;
        currentDamage = 0.0;
        placePos = null;

        if (lastSlot != mc.player.inventory.currentItem || AutoTrap.isPlacing || Surround.isPlacing) {
            lastSlot = mc.player.inventory.currentItem;
            switchTimer.reset();
        }

        if (offHand || mainHand) {
            switching = false;
        }

        if ((!offHand && !mainHand && switchMode.getValue() == Switch.BREAKSLOT && !switching) || !DamageUtil.canBreakWeakness(mc.player) || !switchTimer.passedMs(switchCooldown.getValue())) {
            renderPos = null;
            target = null;
            rotating = false;
            return false;
        }

        if (mineSwitch.getValue() && mc.gameSettings.keyBindAttack.isKeyDown() && (switching || autoSwitch.getValue() == AutoSwitch.ALWAYS) && mc.gameSettings.keyBindUseItem.isKeyDown() && mc.player.getHeldItemMainhand().getItem() instanceof ItemPickaxe) {
            switchItem();
        }

        mapCrystals();

        if(!posConfirmed && damageSync.getValue() != DamageSync.NONE && syncTimer.passedMs(confirm.getValue())) {
            syncTimer.setMs(damageSyncTime.getValue() + 1);
        }
        return true;
    }

    private void mapCrystals() {
        efficientTarget = null;
        if (packets.getValue() != 1) {
            attackList = new ConcurrentLinkedQueue<>();
            crystalMap = new HashMap<>();
        }
        crystalCount = 0;
        minDmgCount = 0;
        Entity maxCrystal = null;
        float maxDamage = 0.5f;
        for (Entity crystal : mc.world.loadedEntityList) {
            if (crystal instanceof EntityEnderCrystal) {
                if (this.isValid(crystal)) {
                    boolean count = false;
                    boolean countMin = false;
                    float selfDamage = DamageUtil.calculateDamage(crystal, mc.player);
                    if (selfDamage + 0.5 < EntityUtil.getHealth(mc.player) || !DamageUtil.canTakeDamage(suicide.getValue())) {
                        for (EntityPlayer player : mc.world.playerEntities) {
                            if (player.getDistanceSq(crystal) < MathUtil.square((range.getValue())) && EntityUtil.isValid(player, (range.getValue() + breakRange.getValue()))) {
                                float damage = DamageUtil.calculateDamage(crystal, player);
                                if (damage > selfDamage || (damage > minDamage.getValue() && !DamageUtil.canTakeDamage(suicide.getValue())) || damage > EntityUtil.getHealth(player)) {
                                    if (damage > maxDamage) {
                                        maxDamage = damage;
                                        maxCrystal = crystal;
                                    }

                                    if (packets.getValue() == 1) {
                                        if (damage >= minDamage.getValue() || !wasteMinDmgCount.getValue()) {
                                            count = true;
                                        }
                                        countMin = true;
                                    } else {
                                        if (crystalMap.get(crystal) == null || crystalMap.get(crystal) < damage) {
                                            crystalMap.put(crystal, damage);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (countMin) {
                        minDmgCount++;
                        if (count) {
                            crystalCount++;
                        }
                    }
                }
            }
        }

        if (damageSync.getValue() == DamageSync.BREAK && (maxDamage > lastDamage || syncTimer.passedMs(damageSyncTime.getValue()) || damageSync.getValue() == DamageSync.NONE)) {
            lastDamage = maxDamage;
        }

        if (webAttack.getValue() && webPos != null) {
            if (mc.player.getDistanceSq(webPos.up()) > MathUtil.square(breakRange.getValue())) {
                webPos = null;
            } else {
                for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(webPos.up()))) {
                    if (entity instanceof EntityEnderCrystal) {
                        attackList.add(entity);
                        efficientTarget = entity;
                        webPos = null;
                        lastDamage = 0.5f;
                        return;
                    }
                }
            }
        }

        if(manual.getValue() && manualMinDmg.getValue() && mc.gameSettings.keyBindUseItem.isKeyDown() && ((offHand && mc.player.getActiveHand() == EnumHand.OFF_HAND) || (mainHand && mc.player.getActiveHand() == EnumHand.MAIN_HAND)) && maxDamage < minDamage.getValue()) {
            efficientTarget = null;
            return;
        }

        if (packets.getValue() == 1) {
            efficientTarget = maxCrystal;

        } else {
            crystalMap = MathUtil.sortByValue(crystalMap, true);
            for (Map.Entry<Entity, Float> entry : crystalMap.entrySet()) {
                Entity crystal = entry.getKey();
                float damage = entry.getValue();
                if(damage >= minDamage.getValue() || !wasteMinDmgCount.getValue()) {
                    crystalCount++;
                }
                attackList.add(crystal);
                minDmgCount++;
            }
        }
    }

    private void placeCrystal() {
        int crystalLimit = wasteAmount.getValue();
        if (placeTimer.passedMs(placeDelay.getValue()) && place.getValue() && (offHand || mainHand || switchMode.getValue() == Switch.CALC || (switchMode.getValue() == Switch.BREAKSLOT && switching))) {
            if ((offHand || mainHand || (switchMode.getValue() != Switch.ALWAYS && !switching)) && (crystalCount >= crystalLimit && !(antiSurround.getValue() && lastPos != null && lastPos.equals(placePos)))) {
                return;
            }
            calculateDamage(getTarget(targetMode.getValue() == Target.UNSAFE));
            if (target != null && placePos != null) {
                if ((!offHand && !mainHand) && (autoSwitch.getValue() != AutoSwitch.NONE) && (currentDamage > minDamage.getValue() || (lethalSwitch.getValue() && EntityUtil.getHealth(target) < facePlace.getValue())) && !switchItem()) {
                    return;
                }

                if (currentDamage < minDamage.getValue()) {
                    crystalLimit = 1;
                }

                if ((offHand || mainHand || autoSwitch.getValue() != AutoSwitch.NONE) && (crystalCount < crystalLimit || (antiSurround.getValue() && lastPos != null && lastPos.equals(placePos))) && (currentDamage > minDamage.getValue() || minDmgCount < crystalLimit) && currentDamage >= 1.0 && (DamageUtil.isArmorLow(target, minArmor.getValue()) || EntityUtil.getHealth(target) < facePlace.getValue() || currentDamage > minDamage.getValue())) {
                    float damageOffset = (damageSync.getValue() == DamageSync.BREAK) ? dropOff.getValue() - 5.0f : 0.0f;
                    if (currentDamage - damageOffset > lastDamage || syncTimer.passedMs(damageSyncTime.getValue()) || damageSync.getValue() == DamageSync.NONE) {
                        if (damageSync.getValue() != DamageSync.BREAK) {
                            lastDamage = currentDamage;
                        }
                        renderPos = placePos;
                        renderDamage = currentDamage;
                        if (switchItem()) {
                            rotateToPos(placePos);
                            BlockUtil.placeCrystalOnBlock(placePos, offHand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
                            placedPos.add(placePos);
                            lastPos = placePos;
                            placeTimer.reset();
                            posConfirmed = false;
                            if (syncTimer.passedMs(damageSyncTime.getValue())) {
                                syncTimer.reset();
                            }
                        }
                    }
                }
            } else {
                renderPos = null;
            }
        }
    }

    private boolean switchItem() {
        if (offHand || mainHand) {
            return true;
        }

        switch (autoSwitch.getValue()) {
            case NONE:
                return false;
            case TOGGLE:
                if (!switching) {
                    return false;
                }
            case ALWAYS:
                if (doSwitch()) {
                    return true;
                }
        }

        return false;
    }

    private boolean doSwitch() {
        if(offhandSwitch.getValue()) {
            Offhand module = Phobos.moduleManager.getModuleByClass(Offhand.class);
            if(module.isOff()) {
                Command.sendMessage("<" + this.getDisplayName() + "> " + TextUtil.RED + "Switch failed. Enable the Offhand module.");
                switching = false;
                return false;
            } else {
                if(module.type.getValue() == Offhand.Type.NEW) {
                    module.setSwapToTotem(false);
                    module.setMode(Offhand.Mode.CRYSTALS);
                    module.doOffhand();
                } else {
                    module.setMode(Offhand.Mode2.CRYSTALS);
                    module.doSwitch();
                }
                switching = false;
                return true;
            }
        }

        if (mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL) {
            mainHand = false;
        } else {
            InventoryUtil.switchToHotbarSlot(ItemEndCrystal.class, false);
            mainHand = true;
        }
        switching = false;
        return true;
    }

    private void calculateDamage(EntityPlayer targettedPlayer) {
        if (targettedPlayer == null && targetMode.getValue() != Target.DAMAGE && !fullCalc.getValue()) {
            return;
        }

        float maxDamage = 0.5f;
        EntityPlayer currentTarget = null;
        BlockPos currentPos = null;
        float maxSelfDamage = 0.0f;

        BlockPos setToAir = null;
        IBlockState state = null;
        if (webAttack.getValue() && targettedPlayer != null) {
            BlockPos playerPos = new BlockPos(targettedPlayer.getPositionVector());
            Block web = mc.world.getBlockState(playerPos).getBlock();
            if (web == Blocks.WEB) {
                setToAir = playerPos;
                state = mc.world.getBlockState(playerPos);
                mc.world.setBlockToAir(playerPos);
            }
        }

        for (BlockPos pos : BlockUtil.possiblePlacePositions(placeRange.getValue(), antiSurround.getValue())) {
            if (BlockUtil.rayTracePlaceCheck(pos, ((raytrace.getValue() == Raytrace.PLACE || raytrace.getValue() == Raytrace.FULL) && mc.player.getDistanceSq(pos) > MathUtil.square(placetrace.getValue())), 1.0f)) {
                float selfDamage = -1;
                if (DamageUtil.canTakeDamage(suicide.getValue())) {
                    selfDamage = DamageUtil.calculateDamage(pos, mc.player);
                }

                if (selfDamage + 0.5 < EntityUtil.getHealth(mc.player)) {
                    if (targettedPlayer != null) {
                        float playerDamage = DamageUtil.calculateDamage(pos, targettedPlayer);
                        if ((playerDamage > maxDamage || (extraSelfCalc.getValue() && playerDamage >= maxDamage && selfDamage < maxSelfDamage)) && (playerDamage > selfDamage || (playerDamage > minDamage.getValue() && !DamageUtil.canTakeDamage(suicide.getValue())) || playerDamage > EntityUtil.getHealth(targettedPlayer))) {
                            maxDamage = playerDamage;
                            currentTarget = targettedPlayer;
                            currentPos = pos;
                            maxSelfDamage = selfDamage;
                        }
                    } else {
                        for (EntityPlayer player : mc.world.playerEntities) {
                            if (EntityUtil.isValid(player, (placeRange.getValue() + range.getValue()))) {
                                float playerDamage = DamageUtil.calculateDamage(pos, player);
                                if ((playerDamage > maxDamage || (extraSelfCalc.getValue() && playerDamage >= maxDamage && selfDamage < maxSelfDamage)) && (playerDamage > selfDamage || (playerDamage > minDamage.getValue() && !DamageUtil.canTakeDamage(suicide.getValue())) || playerDamage > EntityUtil.getHealth(player))) {
                                    maxDamage = playerDamage;
                                    currentTarget = player;
                                    currentPos = pos;
                                    maxSelfDamage = selfDamage;
                                }
                            }
                        }
                    }
                }
            }
        }

        if(setToAir != null) {
            mc.world.setBlockState(setToAir, state);
            webPos = currentPos;
        }

        target = currentTarget;
        currentDamage = maxDamage;
        placePos = currentPos;
    }

    private EntityPlayer getTarget(boolean unsafe) {
        if (targetMode.getValue() == Target.DAMAGE) {
            return null;
        }

        EntityPlayer currentTarget = null;
        for (EntityPlayer player : mc.world.playerEntities) {
            if (EntityUtil.isntValid(player, (placeRange.getValue() + range.getValue()))) {
                continue;
            }

            if (unsafe && EntityUtil.isSafe(player)) {
                continue;
            }

            if (minArmor.getValue() > 0 && DamageUtil.isArmorLow(player, minArmor.getValue())) {
                currentTarget = player;
                break;
            }

            if (currentTarget == null) {
                currentTarget = player;
                continue;
            }

            if (mc.player.getDistanceSq(player) < mc.player.getDistanceSq(currentTarget)) {
                currentTarget = player;
            }
        }

        if (unsafe && currentTarget == null) {
            return getTarget(false);
        }

        return currentTarget;
    }

    private void breakCrystal() {
        if (explode.getValue() && breakTimer.passedMs(breakDelay.getValue()) && (switchMode.getValue() == Switch.ALWAYS || mainHand || offHand)) {
            if (packets.getValue() == 1 && efficientTarget != null) {
                rotateTo(efficientTarget);
                EntityUtil.attackEntity(efficientTarget, sync.getValue(), true);
                brokenPos.add(efficientTarget.getPosition().down());
            } else if (!attackList.isEmpty()) {
                for (int i = 0; i < packets.getValue(); i++) {
                    Entity entity = attackList.poll();
                    if (entity != null) {
                        rotateTo(entity);
                        EntityUtil.attackEntity(entity, sync.getValue(), true);
                        brokenPos.add(entity.getPosition().down());
                    }
                }
            }
            breakTimer.reset();
        }
    }

    private void manualBreaker() {
        if (rotate.getValue() != Rotate.OFF && eventMode.getValue() != 2 && rotating) {
            if (didRotation) {
                mc.player.rotationPitch += 0.0004;
                didRotation = false;
            } else {
                mc.player.rotationPitch -= 0.0004;
                didRotation = true;
            }
        }

        if ((offHand || mainHand) && manual.getValue() && manualTimer.passedMs(manualBreak.getValue()) && (mc.gameSettings.keyBindUseItem.isKeyDown() && (mc.player.getHeldItemOffhand().getItem() != Items.GOLDEN_APPLE && mc.player.inventory.getCurrentItem().getItem() != Items.GOLDEN_APPLE && mc.player.inventory.getCurrentItem().getItem() != Items.BOW && mc.player.inventory.getCurrentItem().getItem() != Items.EXPERIENCE_BOTTLE))) {
            RayTraceResult result = mc.objectMouseOver;
            if (result != null) {
                switch (result.typeOfHit) {
                    case ENTITY:
                        Entity entity = result.entityHit;
                        if (entity instanceof EntityEnderCrystal) {
                            EntityUtil.attackEntity(entity, sync.getValue(), true);
                            manualTimer.reset();
                        }
                        break;
                    case BLOCK:
                        BlockPos mousePos = new BlockPos(mc.objectMouseOver.getBlockPos().getX(), mc.objectMouseOver.getBlockPos().getY() + 1.0, mc.objectMouseOver.getBlockPos().getZ());
                        for (Entity target : mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(mousePos))) {
                            if (target instanceof EntityEnderCrystal) {
                                EntityUtil.attackEntity(target, sync.getValue(), true);
                                manualTimer.reset();
                            }
                        }
                        break;
                }
            }
        }
    }

    private void rotateTo(Entity entity) {
        switch (rotate.getValue()) {
            case OFF:
                rotating = false;
            case PLACE:
                break;
            case BREAK:
            case ALL:
                final float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), entity.getPositionVector());
                if (eventMode.getValue() == 2) {
                    Phobos.rotationManager.setPlayerRotations(angle[0], angle[1]);
                } else {
                    yaw = angle[0];
                    pitch = angle[1];
                    rotating = true;
                }
        }
    }

    private void rotateToPos(BlockPos pos) {
        switch (rotate.getValue()) {
            case OFF:
                rotating = false;
            case BREAK:
                break;
            case PLACE:
            case ALL:
                final float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(pos.getX() + 0.5f, pos.getY() - 0.5f, pos.getZ() + 0.5f));
                if (eventMode.getValue() == 2) {
                    Phobos.rotationManager.setPlayerRotations(angle[0], angle[1]);
                } else {
                    yaw = angle[0];
                    pitch = angle[1];
                    rotating = true;
                }
        }
    }

    private boolean isValid(Entity entity) {
        return entity != null && mc.player.getDistanceSq(entity) <= MathUtil.square(breakRange.getValue())
                && ((raytrace.getValue() == Raytrace.NONE || raytrace.getValue() == Raytrace.PLACE)
                || mc.player.canEntityBeSeen(entity)
                || (!mc.player.canEntityBeSeen(entity) && mc.player.getDistanceSq(entity) <= MathUtil.square(breaktrace.getValue())));
    }

    public enum Settings {
        PLACE,
        BREAK,
        RENDER,
        MISC
    }

    public enum DamageSync {
        NONE,
        PLACE,
        BREAK
    }

    public enum Rotate {
        OFF,
        PLACE,
        BREAK,
        ALL
    }

    public enum Target {
        CLOSEST,
        UNSAFE,
        DAMAGE
    }

    public enum Logic {
        BREAKPLACE,
        PLACEBREAK
    }

    public enum Switch {
        ALWAYS,
        BREAKSLOT,
        CALC
    }

    public enum Raytrace {
        NONE,
        PLACE,
        BREAK,
        FULL
    }

    public enum AutoSwitch {
        NONE,
        TOGGLE,
        ALWAYS
    }
}
