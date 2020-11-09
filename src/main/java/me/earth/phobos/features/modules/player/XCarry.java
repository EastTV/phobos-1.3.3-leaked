package me.earth.phobos.features.modules.player;

import me.earth.phobos.event.events.ClientEvent;
import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.gui.PhobosGui;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Bind;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.InventoryUtil;
import me.earth.phobos.util.ReflectionUtil;
import me.earth.phobos.util.TextUtil;
import me.earth.phobos.util.Util;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static me.earth.phobos.util.InventoryUtil.Task;

public class XCarry extends Module {

    private final Setting<Boolean> simpleMode = register(new Setting("Simple", false));
    private final Setting<Bind> autoStore = register(new Setting("AutoDuel", new Bind(-1)));
    private final Setting<Integer> obbySlot = register(new Setting("ObbySlot", 2, 1, 9, v -> autoStore.getValue().getKey() != -1));
    private final Setting<Integer> slot1 = register(new Setting("Slot1", 22, 9, 44, v -> autoStore.getValue().getKey() != -1));
    private final Setting<Integer> slot2 = register(new Setting("Slot2", 23, 9, 44, v -> autoStore.getValue().getKey() != -1));
    private final Setting<Integer> slot3 = register(new Setting("Slot3", 24, 9, 44, v -> autoStore.getValue().getKey() != -1));
    private final Setting<Integer> tasks = register(new Setting("Actions", 3, 1, 12, v -> autoStore.getValue().getKey() != -1));
    private final Setting<Boolean> store = register(new Setting("Store", false));
    private final Setting<Boolean> shiftClicker = register(new Setting("ShiftClick", false));
    private final Setting<Boolean> withShift = register(new Setting("WithShift", true, v -> shiftClicker.getValue()));
    private final Setting<Bind> keyBind = register(new Setting("ShiftBind", new Bind(-1), v -> shiftClicker.getValue()));

    private static XCarry INSTANCE = new XCarry();
    private GuiInventory openedGui = null;
    private final AtomicBoolean guiNeedsClose = new AtomicBoolean(false);
    private boolean guiCloseGuard = false;
    private boolean autoDuelOn = false;
    private final Queue<Task> taskList = new ConcurrentLinkedQueue<>();
    private boolean obbySlotDone = false;
    private boolean slot1done = false;
    private boolean slot2done = false;
    private boolean slot3done = false;
    private List<Integer> doneSlots = new ArrayList<>();

    public XCarry() {
        super("XCarry", "Uses the crafting inventory for storage", Category.PLAYER, true, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static XCarry getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new XCarry();
        }
        return INSTANCE;
    }

    @Override
    public void onUpdate() {
        //42 = Shift
        //0 = LeftClick //Since we are in a gui I think this is the way to do it.
        if(shiftClicker.getValue() && mc.currentScreen instanceof GuiInventory) {
            boolean ourBind = keyBind.getValue().getKey() != -1 && Keyboard.isKeyDown(keyBind.getValue().getKey()) && !Keyboard.isKeyDown(42);
            if(((Keyboard.isKeyDown(42) && withShift.getValue()) || ourBind) && Mouse.isButtonDown(0)) {
                Slot slot = ((GuiInventory) mc.currentScreen).getSlotUnderMouse();
                if(slot != null && InventoryUtil.getEmptyXCarry() != -1) {
                    int slotNumber = slot.slotNumber;
                    if (slotNumber > 4 && ourBind) {
                        taskList.add(new Task(slotNumber));
                        taskList.add(new Task(InventoryUtil.getEmptyXCarry()));
                    } else if (slotNumber > 4 && withShift.getValue()) {
                        boolean isHotBarFull = true;
                        boolean isInvFull = true;
                        for(int i : InventoryUtil.findEmptySlots(false)) {
                            if(i > 4 && i < 36) {
                                isInvFull = false;
                            } else if(i > 35 && i < 45) {
                                isHotBarFull = false;
                            }
                        }
                        if(slotNumber > 35 && slotNumber < 45) { //if the slot is a hotbarslot
                            if(isInvFull) {
                                taskList.add(new Task(slotNumber));
                                taskList.add(new Task(InventoryUtil.getEmptyXCarry()));
                            }
                        } else if(isHotBarFull) {
                            taskList.add(new Task(slotNumber));
                            taskList.add(new Task(InventoryUtil.getEmptyXCarry()));
                        }
                    }
                }
            }
        }

        if(autoDuelOn) {
            doneSlots = new ArrayList<>();
            if(InventoryUtil.getEmptyXCarry() == -1 || (obbySlotDone && slot1done && slot2done && slot3done)) {
                autoDuelOn = false;
            }

            if(autoDuelOn) {
                if (!obbySlotDone && !mc.player.inventory.getStackInSlot(obbySlot.getValue() - 1).isEmpty) {
                    addTasks(45 - 9 + obbySlot.getValue() - 1);
                }
                obbySlotDone = true;

                if (!slot1done && !mc.player.inventoryContainer.inventorySlots.get(slot1.getValue()).getStack().isEmpty) {
                    addTasks(slot1.getValue());
                }
                slot1done = true;

                if (!slot2done && !mc.player.inventoryContainer.inventorySlots.get(slot2.getValue()).getStack().isEmpty) {
                    addTasks(slot2.getValue());
                }
                slot2done = true;

                if (!slot3done && !mc.player.inventoryContainer.inventorySlots.get(slot3.getValue()).getStack().isEmpty) {
                    addTasks(slot3.getValue());
                }
                slot3done = true;
            }
        } else {
            obbySlotDone = false;
            slot1done = false;
            slot2done = false;
            slot3done = false;
        }

        if(!taskList.isEmpty()) {
            for (int i = 0; i < tasks.getValue(); i++) {
                Task task = taskList.poll();
                if (task != null) {
                    task.run();
                }
            }
        }
    }

    private void addTasks(int slot) {
        if(InventoryUtil.getEmptyXCarry() != -1) {
            int xcarrySlot = InventoryUtil.getEmptyXCarry();
            if(doneSlots.contains(xcarrySlot) || !InventoryUtil.isSlotEmpty(xcarrySlot)) {
                xcarrySlot += 1;
                if(doneSlots.contains(xcarrySlot) || !InventoryUtil.isSlotEmpty(xcarrySlot)) {
                    xcarrySlot += 1;
                    if(doneSlots.contains(xcarrySlot) || !InventoryUtil.isSlotEmpty(xcarrySlot)) {
                        xcarrySlot += 1;
                        if(doneSlots.contains(xcarrySlot) || !InventoryUtil.isSlotEmpty(xcarrySlot)) {
                            return;
                        }
                    }
                }
            }

            if(xcarrySlot <= 4) {
                doneSlots.add(xcarrySlot);
            } else {
                return;
            }

            taskList.add(new Task(slot));
            taskList.add(new Task(xcarrySlot));
            taskList.add(new Task());
        }
    }

    @Override
    public void onDisable() {
        if (!fullNullCheck()) {
            if (!simpleMode.getValue()) {
                closeGui();
                close();
            } else {
                mc.player.connection.sendPacket(new CPacketCloseWindow(mc.player.inventoryContainer.windowId));
            }
        }
    }

    @Override
    public void onLogout() {
        onDisable();
    }

    @SubscribeEvent
    public void onCloseGuiScreen(PacketEvent.Send event) {
        if((simpleMode.getValue() && event.getPacket() instanceof CPacketCloseWindow)) {
            final CPacketCloseWindow packet = event.getPacket();
            if(packet.windowId == mc.player.inventoryContainer.windowId) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGuiOpen(GuiOpenEvent event) {
        if(!simpleMode.getValue()) {
            if (guiCloseGuard) {
                event.setCanceled(true);
            } else if (event.getGui() instanceof GuiInventory) {
                event.setGui(openedGui = createGuiWrapper((GuiInventory) event.getGui()));
                guiNeedsClose.set(false);
            }
        }
    }

    @SubscribeEvent
    public void onSettingChange(ClientEvent event) {
        if(event.getStage() == 2) {
            if(event.getSetting() != null && event.getSetting().getFeature() != null && event.getSetting().getFeature().equals(this)) {
                Setting setting = event.getSetting();
                String settingname= event.getSetting().getName();
                if(setting.equals(this.simpleMode)) {
                    this.disable();
                } else if(settingname.equalsIgnoreCase("Store")) {
                    event.setCanceled(true);
                    autoDuelOn = !autoDuelOn;
                    Command.sendMessage("<XCarry> " + TextUtil.GREEN + "Autostoring...");
                }
            }
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if(Keyboard.getEventKeyState() && !(mc.currentScreen instanceof PhobosGui) && autoStore.getValue().getKey() == Keyboard.getEventKey()) {
            autoDuelOn = !autoDuelOn;
            Command.sendMessage("<XCarry> " + TextUtil.GREEN + "Autostoring...");
        }
    }

    private void close() {
        openedGui = null;
        guiNeedsClose.set(false);
        guiCloseGuard = false;
    }

    private void closeGui() {
        if(guiNeedsClose.compareAndSet(true, false)) {
            if (!fullNullCheck()) {
                guiCloseGuard = true;
                mc.player.closeScreen();
                if (openedGui != null) {
                    openedGui.onGuiClosed();
                    openedGui = null;
                }
                guiCloseGuard = false;
            }
        }
    }

    private GuiInventory createGuiWrapper(GuiInventory gui) {
        try {
            GuiInventoryWrapper wrapper = new GuiInventoryWrapper();
            ReflectionUtil.copyOf(gui, wrapper);
            return wrapper;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    private class GuiInventoryWrapper extends GuiInventory {

        GuiInventoryWrapper() {
            super(Util.mc.player);
        }

        @Override
        protected void keyTyped(char typedChar, int keyCode) throws IOException {
            if (isEnabled() && (keyCode == 1 || this.mc.gameSettings.keyBindInventory.isActiveAndMatches(keyCode))) {
                guiNeedsClose.set(true);
                mc.displayGuiScreen(null);
            } else {
                super.keyTyped(typedChar, keyCode);
            }
        }

        @Override
        public void onGuiClosed() {
            if (guiCloseGuard || !isEnabled()) {
                super.onGuiClosed();
            }
        }
    }
}
