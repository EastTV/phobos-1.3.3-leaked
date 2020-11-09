package me.earth.phobos.features.modules.movement;

import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.EntityUtil;
import me.earth.phobos.util.InventoryUtil;
import me.earth.phobos.util.TextUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.network.play.server.SPacketWindowItems;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoFall extends Module {

    private Setting<Mode> mode = register(new Setting("Mode", Mode.PACKET));
    private Setting<Integer> distance = register(new Setting("Distance", 15, 0, 50, v -> mode.getValue() == Mode.BUCKET));
    private Setting<Boolean> glide = register(new Setting("Glide", false, v -> mode.getValue() == Mode.ELYTRA));
    private Setting<Boolean> silent = register(new Setting("Silent", true, v -> mode.getValue() == Mode.ELYTRA));
    private Setting<Boolean> bypass = register(new Setting("Bypass", false, v -> mode.getValue() == Mode.ELYTRA));

    private Timer timer = new Timer();
    private boolean equipped = false;
    private boolean gotElytra = false;
    private State currentState = State.FALL_CHECK;
    private static Timer bypassTimer = new Timer();
    private static int ogslot = -1;

    public NoFall() {
        super("NoFall", "Prevents fall damage.", Category.MOVEMENT, true, false, false);
    }

    @Override
    public void onEnable() {
        ogslot = -1;
        currentState = State.FALL_CHECK;
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if(fullNullCheck()) {
            return;
        }
        if(mode.getValue() == Mode.ELYTRA) {
            if(bypass.getValue()) {
                currentState = currentState.onSend(event);
            } else {
                if (!equipped && event.getPacket() instanceof CPacketPlayer && mc.player.fallDistance >= 3.0f) {
                    RayTraceResult result = null;
                    if (!glide.getValue()) {
                        result = mc.world.rayTraceBlocks(mc.player.getPositionVector(), mc.player.getPositionVector().add(0.0, -3.0, 0.0), true, true, false);
                    }
                    if ((glide.getValue() || (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK))) {
                        if (mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem().equals(Items.ELYTRA)) {
                            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                        } else if (silent.getValue()) {
                            int slot = InventoryUtil.getItemHotbar(Items.ELYTRA);
                            if (slot != -1) {
                                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 6, slot, ClickType.SWAP, mc.player);
                                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                            }
                            ogslot = slot;
                            equipped = true;
                        }
                    }
                }
            }
        }


        if(mode.getValue() == Mode.PACKET && event.getPacket() instanceof CPacketPlayer) {
            CPacketPlayer packet = event.getPacket();
            packet.onGround = true;
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if(fullNullCheck()) {
            return;
        }
        if((equipped || bypass.getValue()) && mode.getValue() == Mode.ELYTRA && (event.getPacket() instanceof SPacketWindowItems || event.getPacket() instanceof SPacketSetSlot)) {
            if(bypass.getValue()) {
                currentState = currentState.onReceive(event);
            } else {
                gotElytra = true;
            }
        }
    }

    @Override
    public void onUpdate() {
        if(fullNullCheck()) {
            return;
        }
        if(mode.getValue() == Mode.ELYTRA) {
            if(bypass.getValue()) {
                currentState = currentState.onUpdate();
            } else {
                if (silent.getValue() && equipped && gotElytra) {
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 6, ogslot, ClickType.SWAP, mc.player);
                    mc.playerController.updateController();
                    equipped = false;
                    gotElytra = false;
                } else if (silent.getValue() && InventoryUtil.getItemHotbar(Items.ELYTRA) == -1) {
                    int slot = InventoryUtil.findStackInventory(Items.ELYTRA);
                    if (slot != -1 && ogslot != -1){
                        System.out.println(String.format("Moving %d to hotbar %d", slot, ogslot));
                        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slot, ogslot, ClickType.SWAP, mc.player);
                        mc.playerController.updateController();
                    }
                }
            }
        }
    }

    @Override
    public void onTick() {
        if(fullNullCheck()) {
            return;
        }
        if(mode.getValue() == Mode.BUCKET && mc.player.fallDistance >= distance.getValue() && !EntityUtil.isAboveWater(mc.player) && timer.passedMs(100)) {
            Vec3d posVec = mc.player.getPositionVector();
            RayTraceResult result = mc.world.rayTraceBlocks(posVec, posVec.add(0, -5.33f, 0), true, true, false);
            if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {
                EnumHand hand = EnumHand.MAIN_HAND;
                if (mc.player.getHeldItemOffhand().getItem() == Items.WATER_BUCKET) hand = EnumHand.OFF_HAND;
                else if (mc.player.getHeldItemMainhand().getItem() != Items.WATER_BUCKET) {
                    for (int i = 0; i < 9; i++)
                        if (mc.player.inventory.getStackInSlot(i).getItem() == Items.WATER_BUCKET) {
                            mc.player.inventory.currentItem = i;
                            mc.player.rotationPitch = 90;
                            timer.reset();
                            return;
                        }
                    return;
                }

                mc.player.rotationPitch = 90;
                mc.playerController.processRightClick(mc.player, mc.world, hand);
                timer.reset();
            }
        }
    }

    @Override
    public String getDisplayInfo() {
        return mode.currentEnumName();
    }

    public enum Mode {
        PACKET,
        BUCKET,
        ELYTRA
    }

    public enum State {
        FALL_CHECK {
            @Override
            public State onSend(PacketEvent.Send event) {
                RayTraceResult result = mc.world.rayTraceBlocks(mc.player.getPositionVector(), mc.player.getPositionVector().add(0.0, -3.0, 0.0), true, true, false);
                if(event.getPacket() instanceof CPacketPlayer && mc.player.fallDistance >= 3.0f && result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {
                    int slot = InventoryUtil.getItemHotbar(Items.ELYTRA);
                    if (slot != -1) {
                        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 6, slot, ClickType.SWAP, mc.player);
                        ogslot = slot;
                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                        return WAIT_FOR_ELYTRA_DEQUIP;
                    }
                    return this;
                }
                return this;
            }
        },
        WAIT_FOR_ELYTRA_DEQUIP {
            @Override
            public State onReceive(PacketEvent.Receive event) {
                if ((event.getPacket() instanceof SPacketWindowItems || event.getPacket() instanceof SPacketSetSlot)) {
                    return REEQUIP_ELYTRA;
                }
                return this;
            }
        },
        REEQUIP_ELYTRA {
            public State onUpdate() {
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 6, ogslot, ClickType.SWAP, mc.player);
                mc.playerController.updateController();
                int slot = InventoryUtil.findStackInventory(Items.ELYTRA, true);
                if (slot == -1) {
                    Command.sendMessage(TextUtil.RED + "Elytra not found after regain?");
                    return WAIT_FOR_NEXT_REQUIP;
                } else {
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slot, ogslot, ClickType.SWAP, mc.player);
                    mc.playerController.updateController();
                    bypassTimer.reset();
                    return RESET_TIME;
                }
            }
        },
        WAIT_FOR_NEXT_REQUIP {
            public State onUpdate() {
                if (bypassTimer.passedMs(250)) {
                    return REEQUIP_ELYTRA;
                }
                return this;
            }
        },
        RESET_TIME {
            public State onUpdate() {
                if (mc.player.onGround || bypassTimer.passedMs(250)) {
                    mc.player.connection.sendPacket(new CPacketClickWindow(0, 0, 0, ClickType.PICKUP, new ItemStack(Blocks.BEDROCK), (short) 1337));
                    return FALL_CHECK;
                }
                return this;
            }
        };

        public State onSend(PacketEvent.Send e) { return this; };
        public State onReceive(PacketEvent.Receive e) { return this; }
        public State onUpdate() { return this; }
    }
}
