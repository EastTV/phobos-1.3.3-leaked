package me.earth.phobos.features.modules.combat;

import me.earth.phobos.Phobos;
import me.earth.phobos.features.gui.PhobosGui;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.modules.player.XCarry;
import me.earth.phobos.features.setting.Bind;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.DamageUtil;
import me.earth.phobos.util.EntityUtil;
import me.earth.phobos.util.InventoryUtil;
import me.earth.phobos.util.MathUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemExpBottle;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static me.earth.phobos.util.InventoryUtil.Task;

public class AutoArmor extends Module {

    private final Setting<Integer> delay = register(new Setting("Delay", 50, 0, 500));
    private final Setting<Boolean> mendingTakeOff = register(new Setting("AutoMend", false));
    private final Setting<Integer> closestEnemy = register(new Setting("Enemy", 8, 1, 20, v -> mendingTakeOff.getValue()));
    private final Setting<Integer> helmetThreshold = register(new Setting("Helmet%", 80, 1, 100, v -> mendingTakeOff.getValue()));
    private final Setting<Integer> chestThreshold = register(new Setting("Chest%", 80, 1, 100, v -> mendingTakeOff.getValue()));
    private final Setting<Integer> legThreshold = register(new Setting("Legs%", 80, 1, 100, v -> mendingTakeOff.getValue()));
    private final Setting<Integer> bootsThreshold = register(new Setting("Boots%", 80, 1, 100, v -> mendingTakeOff.getValue()));
    private final Setting<Boolean> curse = register(new Setting("CurseOfBinding", false));
    private final Setting<Integer> actions = register(new Setting("Actions", 3, 1, 12));
    private final Setting<Bind> elytraBind = register(new Setting("Elytra", new Bind(-1)));
    private final Setting<Boolean> tps = register(new Setting("TpsSync", true));
    private final Setting<Boolean> updateController = register(new Setting("Update", true));
    private final Setting<Boolean> shiftClick = register(new Setting("ShiftClick", false));

    private final Timer timer = new Timer();
    private final Timer elytraTimer = new Timer();
    private final Queue<Task> taskList = new ConcurrentLinkedQueue<>();
    private final List<Integer> doneSlots = new ArrayList<>();
    private boolean elytraOn = false;

    public AutoArmor() {
        super("AutoArmor", "Puts Armor on for you.", Category.COMBAT, true, false, false);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if(Keyboard.getEventKeyState() && !(mc.currentScreen instanceof PhobosGui) && elytraBind.getValue().getKey() == Keyboard.getEventKey()) {
            elytraOn = !elytraOn;
        }
    }

    @Override
    public void onLogin() {
        timer.reset();
        elytraTimer.reset();
    }

    @Override
    public void onDisable() {
        taskList.clear();
        doneSlots.clear();
        elytraOn = false;
    }

    @Override
    public void onLogout() {
        taskList.clear();
        doneSlots.clear();
    }

    @Override
    public void onTick() {
        if(fullNullCheck() || (mc.currentScreen instanceof GuiContainer && !(mc.currentScreen instanceof GuiInventory))) {
            return;
        }

        if(taskList.isEmpty()) {
            if (mendingTakeOff.getValue() && InventoryUtil.holdingItem(ItemExpBottle.class) && mc.gameSettings.keyBindUseItem.isKeyDown() && (isSafe() || EntityUtil.isSafe(mc.player, 1, false))) {
                final ItemStack helm = mc.player.inventoryContainer.getSlot(5).getStack();
                if (!helm.isEmpty) {
                    int helmDamage = DamageUtil.getRoundedDamage(helm);
                    if (helmDamage >= helmetThreshold.getValue()) {
                        takeOffSlot(5);
                    }
                }

                final ItemStack chest = mc.player.inventoryContainer.getSlot(6).getStack();
                if (!chest.isEmpty) {
                    int chestDamage = DamageUtil.getRoundedDamage(chest);
                    if (chestDamage >= chestThreshold.getValue()) {
                        takeOffSlot(6);
                    }
                }

                final ItemStack legging = mc.player.inventoryContainer.getSlot(7).getStack();
                if (!legging.isEmpty) {
                    int leggingDamage = DamageUtil.getRoundedDamage(legging);
                    if (leggingDamage >= legThreshold.getValue()) {
                        takeOffSlot(7);
                    }
                }

                final ItemStack feet = mc.player.inventoryContainer.getSlot(8).getStack();
                if (!feet.isEmpty) {
                    int bootDamage = DamageUtil.getRoundedDamage(feet);
                    if (bootDamage >= bootsThreshold.getValue()) {
                        takeOffSlot(8);
                    }
                }
                return;
            }

            final ItemStack helm = mc.player.inventoryContainer.getSlot(5).getStack();
            if (helm.getItem() == Items.AIR) {
                final int slot = InventoryUtil.findArmorSlot(EntityEquipmentSlot.HEAD, curse.getValue(), XCarry.getInstance().isOn());
                if (slot != -1) {
                    getSlotOn(5, slot);
                }
            }

            final ItemStack chest = mc.player.inventoryContainer.getSlot(6).getStack();
            if (chest.getItem() == Items.AIR) {
                if (taskList.isEmpty()) {
                    if (elytraOn && elytraTimer.passedMs(500)) {
                        int elytraSlot = InventoryUtil.findItemInventorySlot(Items.ELYTRA, false, XCarry.getInstance().isOn());
                        if (elytraSlot != -1) {
                            if((elytraSlot < 5 && elytraSlot > 1) || !shiftClick.getValue()) {
                                taskList.add(new Task(elytraSlot));
                                taskList.add(new Task(6));
                            } else {
                                taskList.add(new Task(elytraSlot, true));
                            }

                            if(updateController.getValue()) {
                                taskList.add(new Task());
                            }
                            elytraTimer.reset();
                        }
                    } else if (!elytraOn) {
                        final int slot = InventoryUtil.findArmorSlot(EntityEquipmentSlot.CHEST, curse.getValue(), XCarry.getInstance().isOn());
                        if (slot != -1) {
                            getSlotOn(6, slot);
                        }
                    }
                }
            } else {
                if (elytraOn && chest.getItem() != Items.ELYTRA && elytraTimer.passedMs(500)) {
                    if (taskList.isEmpty()) {
                        final int slot = InventoryUtil.findItemInventorySlot(Items.ELYTRA, false, XCarry.getInstance().isOn());
                        if(slot != -1) {
                            taskList.add(new Task(slot));
                            taskList.add(new Task(6));
                            taskList.add(new Task(slot));
                            if(updateController.getValue()) {
                                taskList.add(new Task());
                            }
                        }
                        elytraTimer.reset();
                    }
                } else if (!elytraOn && chest.getItem() == Items.ELYTRA && elytraTimer.passedMs(500) && taskList.isEmpty()) {
                    //TODO: WTF IS THIS
                    int slot = InventoryUtil.findItemInventorySlot(Items.DIAMOND_CHESTPLATE, false, XCarry.getInstance().isOn());
                    if (slot == -1) {
                        slot = InventoryUtil.findItemInventorySlot(Items.IRON_CHESTPLATE, false, XCarry.getInstance().isOn());
                        if (slot == -1) {
                            slot = InventoryUtil.findItemInventorySlot(Items.GOLDEN_CHESTPLATE, false, XCarry.getInstance().isOn());
                            if (slot == -1) {
                                slot = InventoryUtil.findItemInventorySlot(Items.CHAINMAIL_CHESTPLATE, false, XCarry.getInstance().isOn());
                                if (slot == -1) {
                                    slot = InventoryUtil.findItemInventorySlot(Items.LEATHER_CHESTPLATE, false, XCarry.getInstance().isOn());
                                }
                            }
                        }
                    }

                    if (slot != -1) {
                        taskList.add(new Task(slot));
                        taskList.add(new Task(6));
                        taskList.add(new Task(slot));
                        if(updateController.getValue()) {
                            taskList.add(new Task());
                        }
                    }
                    elytraTimer.reset();
                }
            }

            final ItemStack legging = mc.player.inventoryContainer.getSlot(7).getStack();
            if (legging.getItem() == Items.AIR) {
                final int slot = InventoryUtil.findArmorSlot(EntityEquipmentSlot.LEGS, curse.getValue(), XCarry.getInstance().isOn());
                if (slot != -1) {
                    getSlotOn(7, slot);
                }
            }

            final ItemStack feet = mc.player.inventoryContainer.getSlot(8).getStack();
            if (feet.getItem() == Items.AIR) {
                final int slot = InventoryUtil.findArmorSlot(EntityEquipmentSlot.FEET, curse.getValue(), XCarry.getInstance().isOn());
                if (slot != -1) {
                    getSlotOn(8, slot);
                }
            }
        }

        if(timer.passedMs((int)(delay.getValue() * (tps.getValue() ? Phobos.serverManager.getTpsFactor() : 1)))) {
            if (!taskList.isEmpty()) {
                for (int i = 0; i < actions.getValue(); i++) {
                    Task task = taskList.poll();
                    if (task != null) {
                        task.run();
                    }
                }
            }
            timer.reset();
        }
    }

    @Override
    public String getDisplayInfo() {
        if(elytraOn) {
            return "Elytra";
        } else {
            return null;
        }
    }

    private void takeOffSlot(int slot) {
        if(taskList.isEmpty()) {
            int target = -1;
            for(int i : InventoryUtil.findEmptySlots(XCarry.getInstance().isOn())) {
                if (!doneSlots.contains(target)) {
                    target = i;
                    doneSlots.add(i);
                }
            }

            if (target != -1) {
                if((target < 5 && target > 0) || !shiftClick.getValue()) {
                    taskList.add(new Task(slot));
                    taskList.add(new Task(target));
                } else {
                    taskList.add(new Task(slot, true));
                }
                if(updateController.getValue()) {
                    taskList.add(new Task());
                }
            }
        }
    }

    private void getSlotOn(int slot, int target) {
        if(taskList.isEmpty()) {
            doneSlots.remove((Object)target);
            if((target < 5 && target > 0) || !shiftClick.getValue()) {
                taskList.add(new Task(target));
                taskList.add(new Task(slot));
            } else {
                taskList.add(new Task(target, true));
            }
            if(updateController.getValue()) {
                taskList.add(new Task());
            }
        }
    }

    /*private static class Task {
        private final int slot;
        private boolean update = false;
        private boolean quickClick = false;

        public Task(int slot, boolean quickClick) {
            this.slot = slot;
            this.quickClick = quickClick;
        }

        public Task(int slot) {
            this.slot = slot;
            this.quickClick = false;
        }

        public Task() {
            this.update = true;
            this.slot = -1;
        }

        public void run() {
            if(this.update) {
                mc.playerController.updateController();
            }

            if(slot != -1) {
                mc.playerController.windowClick(0, this.slot, 0, this.quickClick ? ClickType.QUICK_MOVE : ClickType.PICKUP, mc.player);
            }
        }

        public boolean isSwitching() {
            return !this.update;
        }
    }*/

    private boolean isSafe() {
        EntityPlayer closest = EntityUtil.getClosestEnemy(closestEnemy.getValue());
        if(closest == null) {
            return true;
        }
        return mc.player.getDistanceSq(closest) >= MathUtil.square(closestEnemy.getValue());
    }

}
