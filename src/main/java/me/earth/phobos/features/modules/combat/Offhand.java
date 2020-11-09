package me.earth.phobos.features.modules.combat;

import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.event.events.ProcessRightClickBlockEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Bind;
import me.earth.phobos.features.setting.EnumConverter;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.EntityUtil;
import me.earth.phobos.util.InventoryUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockWeb;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Offhand extends Module {

    public Setting<Type> type = register(new Setting("Mode", Type.NEW));

    public Setting<Boolean> cycle = register(new Setting("Cycle", false, v -> type.getValue() == Type.OLD));
    public Setting<Bind> cycleKey = register(new Setting("Key", new Bind(-1), v -> cycle.getValue() && type.getValue() == Type.OLD));

    public Setting<Bind> offHandGapple = register(new Setting("Gapple", new Bind(-1)));
    public Setting<Float> gappleHealth = register(new Setting("G-Health", 13.0f, 0.1f, 36.0f));
    public Setting<Float> gappleHoleHealth = register(new Setting("G-H-Health", 3.5f, 0.1f, 36.0f));
    public Setting<Bind> offHandCrystal = register(new Setting("Crystal", new Bind(-1)));
    public Setting<Float> crystalHealth = register(new Setting("C-Health", 13.0f, 0.1f, 36.0f));
    public Setting<Float> crystalHoleHealth = register(new Setting("C-H-Health", 3.5f, 0.1f, 36.0f));
    public Setting<Float> cTargetDistance = register(new Setting("C-Distance", 10.0f, 1.0f, 20.0f));
    public Setting<Bind> obsidian = register(new Setting("Obsidian", new Bind(-1)));
    public Setting<Float> obsidianHealth = register(new Setting("O-Health", 13.0f, 0.1f, 36.0f));
    public Setting<Float> obsidianHoleHealth = register(new Setting("O-H-Health", 8.0f, 0.1f, 36.0f));
    public Setting<Bind> webBind = register(new Setting("Webs", new Bind(-1)));
    public Setting<Float> webHealth = register(new Setting("W-Health", 13.0f, 0.1f, 36.0f));
    public Setting<Float> webHoleHealth = register(new Setting("W-H-Health", 8.0f, 0.1f, 36.0f));
    public Setting<Boolean> holeCheck = register(new Setting("Hole-Check", true));
    public Setting<Boolean> crystalCheck = register(new Setting("Crystal-Check", false));
    public Setting<Boolean> gapSwap = register(new Setting("Gap-Swap", true));
    public Setting<Integer> updates = register(new Setting("Updates", 1, 1, 2));

    public Setting<Boolean> cycleObby = register(new Setting("CycleObby", false, v -> type.getValue() == Type.OLD));
    public Setting<Boolean> cycleWebs = register(new Setting("CycleWebs", false, v -> type.getValue() == Type.OLD));
    public Setting<Boolean> crystalToTotem = register(new Setting("Crystal-Totem", true, v -> type.getValue() == Type.OLD));
    public Setting<Boolean> absorption = register(new Setting("Absorption", false, v -> type.getValue() == Type.OLD));
    public Setting<Boolean> autoGapple = register(new Setting("AutoGapple", false, v -> type.getValue() == Type.OLD));
    public Setting<Boolean> onlyWTotem = register(new Setting("OnlyWTotem", true, v -> autoGapple.getValue() && type.getValue() == Type.OLD));
    public Setting<Boolean> unDrawTotem = register(new Setting("DrawTotems", true, v -> type.getValue() == Type.OLD));
    public Setting<Boolean> noOffhandGC = register(new Setting("NoOGC", false));
    public Setting<Boolean> returnToCrystal = register(new Setting("RecoverySwitch", false));
    public Setting<Integer> timeout = register(new Setting("Timeout", 50, 0, 500));
    public Setting<Integer> timeout2 = register(new Setting("Timeout2", 50, 0, 500));
    public Setting<Integer> actions = register(new Setting("Actions", 4, 1,4, v -> type.getValue() == Type.OLD));
    public Setting<NameMode> displayNameChange = register(new Setting("Name", NameMode.TOTEM, v -> type.getValue() == Type.OLD));

    public Mode mode = Mode.CRYSTALS;
    public Mode oldMode = Mode.CRYSTALS;
    private int oldSlot = -1;
    private boolean swapToTotem = false, eatingApple = false, oldSwapToTotem = false;

    public Mode2 currentMode = Mode2.TOTEMS;
    public int totems = 0;
    public int crystals = 0;
    public int gapples = 0;
    public int obby = 0;
    public int webs = 0;
    public int lastTotemSlot = -1;
    public int lastGappleSlot = -1;
    public int lastCrystalSlot = -1;
    public int lastObbySlot = -1;
    public int lastWebSlot = -1;
    public boolean holdingCrystal = false;
    public boolean holdingTotem = false;
    public boolean holdingGapple = false;
    public boolean holdingObby = false;
    public boolean holdingWeb = false;
    public boolean didSwitchThisTick = false;
    private final Queue<InventoryUtil.Task> taskList = new ConcurrentLinkedQueue<>();
    private boolean autoGappleSwitch = false;
    private static Offhand instance;
    private Timer timer = new Timer();
    private Timer secondTimer = new Timer();
    private boolean second = false;
    private boolean switchedForHealthReason = false;

    public Offhand() {
        super("Offhand", "Allows you to switch up your Offhand.", Category.COMBAT, true, false, false);
        instance = this;
    }

    public static Offhand getInstance() {
        if(instance == null) {
            instance = new Offhand();
        }
        return instance;
    }

    public void onItemFinish(ItemStack stack, EntityLivingBase base) {
        if(noOffhandGC.getValue() && base.equals(mc.player) && stack.getItem() == mc.player.getHeldItemOffhand().getItem()) {
            secondTimer.reset();
            second = true;
        }
    }

    @Override
    public void onTick() {
        if (nullCheck() || updates.getValue() == 1) {
            return;
        }
        doOffhand();
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(ProcessRightClickBlockEvent event) {
        if (noOffhandGC.getValue() && event.hand == EnumHand.MAIN_HAND && event.stack.getItem() == Items.END_CRYSTAL && mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE && mc.objectMouseOver != null && event.pos == mc.objectMouseOver.getBlockPos()) {
            event.setCanceled(true);
            mc.player.setActiveHand(EnumHand.OFF_HAND);
            mc.playerController.processRightClick(mc.player, mc.world, EnumHand.OFF_HAND);
        }
    }

    @Override
    public void onUpdate() {
        if(noOffhandGC.getValue()) {
            if (timer.passedMs(timeout.getValue())) {
                if (mc.player != null && mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE && mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL && Mouse.isButtonDown(1)) {
                    mc.player.setActiveHand(EnumHand.OFF_HAND);
                    mc.gameSettings.keyBindUseItem.pressed = Mouse.isButtonDown(1);
                }
            } else if(mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE && mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL) {
                mc.gameSettings.keyBindUseItem.pressed = false;
                //mc.player.stopActiveHand();
            }
        }
        if (nullCheck() || updates.getValue() == 2) {
            return;
        }
        doOffhand();
        if(secondTimer.passedMs(timeout2.getValue()) && second) {
            second = false;
            timer.reset();
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Keyboard.getEventKeyState()) {
            if(type.getValue() == Type.NEW) {
                if (offHandCrystal.getValue().getKey() == Keyboard.getEventKey()) {
                    if (mode == Mode.CRYSTALS) {
                        setSwapToTotem(!isSwapToTotem());
                    } else setSwapToTotem(false);
                    setMode(Mode.CRYSTALS);
                }
                if (offHandGapple.getValue().getKey() == Keyboard.getEventKey()) {
                    if (mode == Mode.GAPPLES) {
                        setSwapToTotem(!isSwapToTotem());
                    } else setSwapToTotem(false);
                    setMode(Mode.GAPPLES);
                }
                if (obsidian.getValue().getKey() == Keyboard.getEventKey()) {
                    if (mode == Mode.OBSIDIAN) {
                        setSwapToTotem(!isSwapToTotem());
                    } else setSwapToTotem(false);
                    setMode(Mode.OBSIDIAN);
                }
                if (webBind.getValue().getKey() == Keyboard.getEventKey()) {
                    if (mode == Mode.WEBS) {
                        setSwapToTotem(!isSwapToTotem());
                    } else setSwapToTotem(false);
                    setMode(Mode.WEBS);
                }
            } else {
                if(cycle.getValue()) {
                    if(cycleKey.getValue().getKey() == Keyboard.getEventKey()) {
                        Mode2 newMode = (Mode2) EnumConverter.increaseEnum(currentMode);
                        if((newMode == Mode2.OBSIDIAN && !cycleObby.getValue()) || (newMode == Mode2.WEBS && !cycleWebs.getValue())) {
                            newMode = Mode2.TOTEMS;
                        }
                        setMode(newMode);
                    }
                } else {
                    if (offHandCrystal.getValue().getKey() == Keyboard.getEventKey()) {
                        setMode(Mode2.CRYSTALS);
                    }

                    if (offHandGapple.getValue().getKey() == Keyboard.getEventKey()) {
                        setMode(Mode2.GAPPLES);
                    }

                    if(obsidian.getValue().getKey() == Keyboard.getEventKey()) {
                        setMode(Mode2.OBSIDIAN);
                    }

                    if(webBind.getValue().getKey() == Keyboard.getEventKey()) {
                        setMode(Mode2.WEBS);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if(noOffhandGC.getValue() && !fullNullCheck() && mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE && mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL && mc.gameSettings.keyBindUseItem.isKeyDown()) {
            if(event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
                CPacketPlayerTryUseItemOnBlock packet = event.getPacket();
                if (packet.getHand() == EnumHand.MAIN_HAND) {
                    if (!AutoCrystal.placedPos.contains(packet.getPos())) {
                        if(timer.passedMs(timeout.getValue())) {
                            mc.player.setActiveHand(EnumHand.OFF_HAND);
                            mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.OFF_HAND));
                        }
                        event.setCanceled(true);
                    }
                }
            } else if(event.getPacket() instanceof CPacketPlayerTryUseItem) {
                CPacketPlayerTryUseItem packet = event.getPacket();
                if(packet.getHand() == EnumHand.OFF_HAND && !timer.passedMs(timeout.getValue())) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @Override
    public String getDisplayInfo() {
        if(type.getValue() == Type.NEW) {
            return String.valueOf(getStackSize());
        } else {
            switch(displayNameChange.getValue()) {
                case MODE:
                    return EnumConverter.getProperName(currentMode);
                case TOTEM:
                    if(currentMode == Mode2.TOTEMS) {
                        return totems + "";
                    }
                    return EnumConverter.getProperName(currentMode);
                default:
                    switch(currentMode) {
                        case TOTEMS:
                            return totems + "";
                        case GAPPLES:
                            return gapples + "";
                        default:
                            return crystals + "";
                    }
            }
        }
    }

    @Override
    public String getDisplayName() {
        if(type.getValue() == Type.NEW) {
            if (!shouldTotem()) {
                switch (mode) {
                    case GAPPLES:
                        return "OffhandGapple";
                    case WEBS:
                        return "OffhandWebs";
                    case OBSIDIAN:
                        return "OffhandObby";
                    default:
                        return "OffhandCrystal";
                }
            }
            return "AutoTotem" + (!isSwapToTotem() ? "-" + getModeStr() : "");
        } else {
            switch(displayNameChange.getValue()) {
                case MODE:
                    return this.displayName.getValue();
                case TOTEM:
                    if(currentMode == Mode2.TOTEMS) {
                        return "AutoTotem";
                    }
                    return this.displayName.getValue();
                default:
                    switch(currentMode) {
                        case TOTEMS:
                            return "AutoTotem";
                        case GAPPLES:
                            return "OffhandGapple";
                        case WEBS:
                            return "OffhandWebs";
                        case OBSIDIAN:
                            return "OffhandObby";
                        default:
                            return "OffhandCrystal";
                    }
            }
        }
    }

    public void doOffhand() {
        if(type.getValue() == Type.NEW) {
            if (mc.currentScreen instanceof GuiContainer && !(mc.currentScreen instanceof GuiInventory))
                return;
            if (gapSwap.getValue()) {
                if (!(getSlot(Mode.GAPPLES) == -1 && mc.player.getHeldItemOffhand().getItem() != Items.GOLDEN_APPLE) && mc.player.getHeldItemMainhand().getItem() != Items.GOLDEN_APPLE && mc.gameSettings.keyBindUseItem.isKeyDown()) {
                    setMode(Mode.GAPPLES);
                    eatingApple = true;
                    swapToTotem = false;
                } else {
                    if (eatingApple) {
                        setMode(oldMode);
                        swapToTotem = oldSwapToTotem;
                        eatingApple = false;
                    } else {
                        oldMode = mode;
                        oldSwapToTotem = swapToTotem;
                    }
                }
            }

            if (!shouldTotem()) {
                if (!(mc.player.getHeldItemOffhand() != ItemStack.EMPTY && isItemInOffhand())) {
                    final int slot = getSlot(mode) < 9 ? getSlot(mode) + 36 : getSlot(mode);
                    if (getSlot(mode) != -1) {
                        if (oldSlot != -1) {
                            mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
                            mc.playerController.windowClick(0, oldSlot, 0, ClickType.PICKUP, mc.player);
                        }
                        oldSlot = slot;
                        mc.playerController.windowClick(0, slot, 0, ClickType.PICKUP, mc.player);
                        mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
                        mc.playerController.windowClick(0, slot, 0, ClickType.PICKUP, mc.player);
                    }
                }
            } else if (!eatingApple && !(mc.player.getHeldItemOffhand() != ItemStack.EMPTY && mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING)) {
                final int slot = getTotemSlot() < 9 ? getTotemSlot() + 36 : getTotemSlot();
                if (getTotemSlot() != -1) {
                    mc.playerController.windowClick(0, slot, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(0, oldSlot, 0, ClickType.PICKUP, mc.player);
                    oldSlot = -1;
                }
            }
        } else {
            if(!unDrawTotem.getValue()) {
                manageDrawn();
            }

            didSwitchThisTick = false;
            holdingCrystal  = mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL;
            holdingTotem = mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING;
            holdingGapple = mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE;
            holdingObby = InventoryUtil.isBlock(mc.player.getHeldItemOffhand().getItem(), BlockObsidian.class);
            holdingWeb = InventoryUtil.isBlock(mc.player.getHeldItemOffhand().getItem(), BlockWeb.class);

            totems = mc.player.inventory.mainInventory.stream().filter(itemStack -> itemStack.getItem() == Items.TOTEM_OF_UNDYING).mapToInt(ItemStack::getCount).sum();
            if (holdingTotem) {
                totems += mc.player.inventory.offHandInventory.stream().filter(itemStack -> itemStack.getItem() == Items.TOTEM_OF_UNDYING).mapToInt(ItemStack::getCount).sum();
            }

            crystals = mc.player.inventory.mainInventory.stream().filter(itemStack -> itemStack.getItem() == Items.END_CRYSTAL).mapToInt(ItemStack::getCount).sum();
            if (holdingCrystal) {
                crystals += mc.player.inventory.offHandInventory.stream().filter(itemStack -> itemStack.getItem() == Items.END_CRYSTAL).mapToInt(ItemStack::getCount).sum();
            }

            gapples = mc.player.inventory.mainInventory.stream().filter(itemStack -> itemStack.getItem() == Items.GOLDEN_APPLE).mapToInt(ItemStack::getCount).sum();
            if (holdingGapple) {
                gapples += mc.player.inventory.offHandInventory.stream().filter(itemStack -> itemStack.getItem() == Items.GOLDEN_APPLE).mapToInt(ItemStack::getCount).sum();
            }

            if(currentMode == Mode2.WEBS || currentMode == Mode2.OBSIDIAN) {
                obby = mc.player.inventory.mainInventory.stream().filter(itemStack -> InventoryUtil.isBlock(itemStack.getItem(), BlockObsidian.class)).mapToInt(ItemStack::getCount).sum();
                if (holdingObby) {
                    obby += mc.player.inventory.offHandInventory.stream().filter(itemStack -> InventoryUtil.isBlock(itemStack.getItem(), BlockObsidian.class)).mapToInt(ItemStack::getCount).sum();
                }

                webs = mc.player.inventory.mainInventory.stream().filter(itemStack -> InventoryUtil.isBlock(itemStack.getItem(), BlockWeb.class)).mapToInt(ItemStack::getCount).sum();
                if (holdingWeb) {
                    webs += mc.player.inventory.offHandInventory.stream().filter(itemStack -> InventoryUtil.isBlock(itemStack.getItem(), BlockWeb.class)).mapToInt(ItemStack::getCount).sum();
                }
            }

            doSwitch();
        }
    }
    private void manageDrawn() {
        if(currentMode == Mode2.TOTEMS && this.drawn.getValue()) {
            this.drawn.setValue(false);
        }

        if(currentMode != Mode2.TOTEMS && !this.drawn.getValue()) {
            this.drawn.setValue(true);
        }
    }

    public void doSwitch() {

        if(autoGapple.getValue()) {
            if(mc.gameSettings.keyBindUseItem.isKeyDown()) {
                if(mc.player.getHeldItemMainhand().getItem() instanceof ItemSword && (!onlyWTotem.getValue() || mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING)) {
                    setMode(Mode.GAPPLES);
                    autoGappleSwitch = true;
                }
            } else if(autoGappleSwitch) {
                setMode(Mode2.TOTEMS);
                autoGappleSwitch = false;
            }
        }

        if((currentMode == Mode2.GAPPLES && ((!EntityUtil.isSafe(mc.player) && EntityUtil.getHealth(mc.player, absorption.getValue()) <= gappleHealth.getValue()) || (EntityUtil.getHealth(mc.player, absorption.getValue()) <= gappleHoleHealth.getValue())))
                || (currentMode == Mode2.CRYSTALS && ((!EntityUtil.isSafe(mc.player) && EntityUtil.getHealth(mc.player, absorption.getValue()) <= crystalHealth.getValue()) || (EntityUtil.getHealth(mc.player, absorption.getValue()) <= crystalHoleHealth.getValue())))
                || (currentMode == Mode2.OBSIDIAN && ((!EntityUtil.isSafe(mc.player) && EntityUtil.getHealth(mc.player, absorption.getValue()) <= obsidianHealth.getValue()) || (EntityUtil.getHealth(mc.player, absorption.getValue()) <= obsidianHoleHealth.getValue())))
                || (currentMode == Mode2.WEBS && ((!EntityUtil.isSafe(mc.player) && EntityUtil.getHealth(mc.player, absorption.getValue()) <= webHealth.getValue()) || (EntityUtil.getHealth(mc.player, absorption.getValue()) <= webHoleHealth.getValue())))) {
            if(returnToCrystal.getValue() && currentMode == Mode2.CRYSTALS) {
                switchedForHealthReason = true;
            }
            setMode(Mode2.TOTEMS);
        }

        if(switchedForHealthReason && ((EntityUtil.isSafe(mc.player) && EntityUtil.getHealth(mc.player, absorption.getValue()) > crystalHoleHealth.getValue()) || (EntityUtil.getHealth(mc.player, absorption.getValue()) > crystalHealth.getValue()))) {
            setMode(Mode2.CRYSTALS);
            switchedForHealthReason = false;
        }

        if(mc.currentScreen instanceof GuiContainer && !(mc.currentScreen instanceof GuiInventory)) {
            return;
        }

        Item currentOffhandItem = mc.player.getHeldItemOffhand().getItem();
        int lastSlot;

        switch(currentMode) {
            case TOTEMS:
                if(totems > 0 && !holdingTotem) {
                    lastTotemSlot = InventoryUtil.findItemInventorySlot(Items.TOTEM_OF_UNDYING, false);
                    lastSlot = getLastSlot(currentOffhandItem, lastTotemSlot);
                    putItemInOffhand(lastTotemSlot, lastSlot);
                }
                break;
            case GAPPLES:
                if(gapples > 0 && !holdingGapple) {
                    lastGappleSlot = InventoryUtil.findItemInventorySlot(Items.GOLDEN_APPLE, false);
                    lastSlot = getLastSlot(currentOffhandItem, lastGappleSlot);
                    putItemInOffhand(lastGappleSlot, lastSlot);
                }
                break;
            case WEBS:
                if(webs > 0 && !holdingWeb) {
                    lastWebSlot = InventoryUtil.findInventoryBlock(BlockWeb.class, false);
                    lastSlot = getLastSlot(currentOffhandItem, lastWebSlot);
                    putItemInOffhand(lastWebSlot, lastSlot);
                }
                break;
            case OBSIDIAN:
                if(obby > 0 && !holdingObby) {
                    lastObbySlot = InventoryUtil.findInventoryBlock(BlockObsidian.class, false);
                    lastSlot = getLastSlot(currentOffhandItem, lastObbySlot);
                    putItemInOffhand(lastObbySlot, lastSlot);
                }
                break;
            default:
                if(crystals > 0 && !holdingCrystal) {
                    lastCrystalSlot = InventoryUtil.findItemInventorySlot(Items.END_CRYSTAL, false);
                    lastSlot = getLastSlot(currentOffhandItem, lastCrystalSlot);
                    putItemInOffhand(lastCrystalSlot, lastSlot);
                }
        }

        for(int i = 0; i < actions.getValue(); i++) {
            InventoryUtil.Task task = taskList.poll();
            if(task != null) {
                task.run();
                if(task.isSwitching()) {
                    didSwitchThisTick = true;
                }
            }
        }
    }

    private int getLastSlot(Item item, int slotIn) {
        if(item == Items.END_CRYSTAL) {
            return lastCrystalSlot;
        } else if(item == Items.GOLDEN_APPLE) {
            return lastGappleSlot;
        } else if(item == Items.TOTEM_OF_UNDYING) {
            return lastTotemSlot;
        } else if(InventoryUtil.isBlock(item, BlockObsidian.class)) {
            return lastObbySlot;
        } else if(InventoryUtil.isBlock(item, BlockWeb.class)) {
            return lastWebSlot;
        } else if (item == Items.AIR) {
            return -1;
        } else {
            return slotIn;
        }
    }

    private void putItemInOffhand(int slotIn, int slotOut) {
        if(slotIn != -1 && taskList.isEmpty()) {
            taskList.add(new InventoryUtil.Task(slotIn));
            taskList.add(new InventoryUtil.Task(45));
            taskList.add(new InventoryUtil.Task(slotOut));
            taskList.add(new InventoryUtil.Task());
        }
    }

    private boolean noNearbyPlayers() {
        return mode == Mode.CRYSTALS && mc.world.playerEntities.stream().noneMatch(e -> e != mc.player && !Phobos.friendManager.isFriend(e) && mc.player.getDistance(e) <= cTargetDistance.getValue());
    }

    private boolean isItemInOffhand() {
        switch (mode) {
            case GAPPLES:
                return mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE;
            case CRYSTALS:
                return mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL;
            case OBSIDIAN:
                return mc.player.getHeldItemOffhand().getItem() instanceof ItemBlock && ((ItemBlock) mc.player.getHeldItemOffhand().getItem()).block == Blocks.OBSIDIAN;
            case WEBS:
                return mc.player.getHeldItemOffhand().getItem() instanceof ItemBlock && ((ItemBlock) mc.player.getHeldItemOffhand().getItem()).block == Blocks.WEB;
        }
        return false;
    }

    private boolean isHeldInMainHand() {
        switch (mode) {
            case GAPPLES:
                return mc.player.getHeldItemMainhand().getItem() == Items.GOLDEN_APPLE;
            case CRYSTALS:
                return mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL;
            case OBSIDIAN:
                return mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock && ((ItemBlock) mc.player.getHeldItemMainhand().getItem()).block == Blocks.OBSIDIAN;
            case WEBS:
                return mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock && ((ItemBlock) mc.player.getHeldItemMainhand().getItem()).block == Blocks.WEB;
        }
        return false;
    }

    private boolean shouldTotem() {
        if (isHeldInMainHand() || isSwapToTotem()) return true;
        if (holeCheck.getValue() && EntityUtil.isInHole(mc.player)) {
            return (mc.player.getHealth() + mc.player.getAbsorptionAmount()) <= getHoleHealth() || mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == Items.ELYTRA || mc.player.fallDistance >= 3 || noNearbyPlayers() || (crystalCheck.getValue() && isCrystalsAABBEmpty());
        }
        return (mc.player.getHealth() + mc.player.getAbsorptionAmount()) <= getHealth() || mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == Items.ELYTRA || mc.player.fallDistance >= 3 || noNearbyPlayers() || (crystalCheck.getValue() && isCrystalsAABBEmpty());
    }

    private boolean isNotEmpty(BlockPos pos) {
        return mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos)).stream().anyMatch(e -> e instanceof EntityEnderCrystal);
    }

    private float getHealth() {
        switch (mode) {
            case CRYSTALS:
                return crystalHealth.getValue();
            case GAPPLES:
                return gappleHealth.getValue();
            case OBSIDIAN:
                return obsidianHealth.getValue();
        }
        return webHealth.getValue();
    }

    private float getHoleHealth() {
        switch (mode) {
            case CRYSTALS:
                return crystalHoleHealth.getValue();
            case GAPPLES:
                return gappleHoleHealth.getValue();
            case OBSIDIAN:
                return obsidianHoleHealth.getValue();
        }
        return webHoleHealth.getValue();
    }

    private boolean isCrystalsAABBEmpty() {
        return isNotEmpty(mc.player.getPosition().add(1, 0, 0)) || isNotEmpty(mc.player.getPosition().add(-1, 0, 0)) || isNotEmpty(mc.player.getPosition().add(0, 0, 1)) || isNotEmpty(mc.player.getPosition().add(0, 0, -1)) || isNotEmpty(mc.player.getPosition());
    }

    int getStackSize() {
        int size = 0;
        if (shouldTotem()) {
            for (int i = 45; i > 0; i--) {
                if (mc.player.inventory.getStackInSlot(i).getItem() == Items.TOTEM_OF_UNDYING) {
                    size += mc.player.inventory.getStackInSlot(i).getCount();
                }
            }
        } else if (mode == Mode.OBSIDIAN) {
            for (int i = 45; i > 0; i--) {
                if (mc.player.inventory.getStackInSlot(i).getItem() instanceof ItemBlock && ((ItemBlock) mc.player.inventory.getStackInSlot(i).getItem()).block == Blocks.OBSIDIAN) {
                    size += mc.player.inventory.getStackInSlot(i).getCount();
                }
            }
        } else if (mode == Mode.WEBS) {
            for (int i = 45; i > 0; i--) {
                if (mc.player.inventory.getStackInSlot(i).getItem() instanceof ItemBlock && ((ItemBlock) mc.player.inventory.getStackInSlot(i).getItem()).block == Blocks.WEB) {
                    size += mc.player.inventory.getStackInSlot(i).getCount();
                }
            }
        } else {
            for (int i = 45; i > 0; i--) {
                if (mc.player.inventory.getStackInSlot(i).getItem() == (mode == Mode.CRYSTALS ? Items.END_CRYSTAL : Items.GOLDEN_APPLE)) {
                    size += mc.player.inventory.getStackInSlot(i).getCount();
                }
            }
        }
        return size;
    }

    int getSlot(Mode m) {
        int slot = -1;
        if (m == Mode.OBSIDIAN) {
            for (int i = 45; i > 0; i--) {
                if (mc.player.inventory.getStackInSlot(i).getItem() instanceof ItemBlock && ((ItemBlock) mc.player.inventory.getStackInSlot(i).getItem()).block == Blocks.OBSIDIAN) {
                    slot = i;
                    break;
                }
            }
        } else if (m == Mode.WEBS) {
            for (int i = 45; i > 0; i--) {
                if (mc.player.inventory.getStackInSlot(i).getItem() instanceof ItemBlock && ((ItemBlock) mc.player.inventory.getStackInSlot(i).getItem()).block == Blocks.WEB) {
                    slot = i;
                    break;
                }
            }
        } else {
            for (int i = 45; i > 0; i--) {
                if (mc.player.inventory.getStackInSlot(i).getItem() == (m == Mode.CRYSTALS ? Items.END_CRYSTAL : Items.GOLDEN_APPLE)) {
                    slot = i;
                    break;
                }
            }
        }
        return slot;
    }

    int getTotemSlot() {
        int totemSlot = -1;
        for (int i = 45; i > 0; i--) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == Items.TOTEM_OF_UNDYING) {
                totemSlot = i;
                break;
            }
        }
        return totemSlot;
    }

    private String getModeStr() {
        switch (mode) {
            case GAPPLES:
                return "G";
            case WEBS:
                return "W";
            case OBSIDIAN:
                return "O";
            default:
                return "C";
        }
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public void setMode(Mode2 mode) {
        if (currentMode == mode) {
            currentMode = Mode2.TOTEMS;
        } else if (!cycle.getValue() && crystalToTotem.getValue() && (currentMode == Mode2.CRYSTALS || currentMode == Mode2.OBSIDIAN || currentMode == Mode2.WEBS) && mode == Mode2.GAPPLES) {
            currentMode = Mode2.TOTEMS;
        } else {
            currentMode = mode;
        }
    }

    public boolean isSwapToTotem() {
        return swapToTotem;
    }

    public void setSwapToTotem(boolean swapToTotem) {
        this.swapToTotem = swapToTotem;
    }

    public enum Mode {
        CRYSTALS,
        GAPPLES,
        OBSIDIAN,
        WEBS
    }

    public enum Type {
        OLD,
        NEW
    }

    public enum Mode2 {
        TOTEMS,
        GAPPLES,
        CRYSTALS,
        OBSIDIAN,
        WEBS
    }

    public enum NameMode {
        MODE,
        TOTEM,
        AMOUNT
    }
}
