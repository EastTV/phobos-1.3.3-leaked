package me.earth.phobos.features.modules.player;

import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.EntityUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

//TODO: Placed but with valid packet
public class BlockTweaks extends Module {

    public Setting<Boolean> autoTool = register(new Setting("AutoTool", false));
    public Setting<Boolean> autoWeapon = register(new Setting("AutoWeapon", false));
    public Setting<Boolean> noFriendAttack = register(new Setting("NoFriendAttack", false));
    public Setting<Boolean> noBlock = register(new Setting("NoHitboxBlock", true));
    public Setting<Boolean> noGhost = register(new Setting("NoGlitchBlocks", false));
    public Setting<Boolean> destroy = register(new Setting("Destroy", false, v -> noGhost.getValue()));

    private static BlockTweaks INSTANCE = new BlockTweaks();
    private int lastHotbarSlot = -1;
    private int currentTargetSlot = -1;
    private boolean switched = false;

    public BlockTweaks() {
        super("BlockTweaks", "Some tweaks for blocks.", Category.PLAYER, true, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static BlockTweaks getINSTANCE() {
        if(INSTANCE == null) {
            INSTANCE = new BlockTweaks();
        }
        return INSTANCE;
    }

    @Override
    public void onDisable() {
        if(switched) {
            equip(lastHotbarSlot, false);
        }
        lastHotbarSlot = -1;
        currentTargetSlot = -1;
    }

    @SubscribeEvent
    public void onBreak(BlockEvent.BreakEvent event) {
        if (fullNullCheck() || !noGhost.getValue() || !destroy.getValue()) {
            return;
        }

        if (!(mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock)) {
            BlockPos pos = mc.player.getPosition();
            removeGlitchBlocks(pos);
        }
    }

    @SubscribeEvent
    public void onBlockInteract(PlayerInteractEvent.LeftClickBlock event) {
        if(autoTool.getValue() && (Speedmine.getInstance().mode.getValue() != Speedmine.Mode.PACKET || Speedmine.getInstance().isOff() || !Speedmine.getInstance().tweaks.getValue())) {
            if(!fullNullCheck() && event.getPos() != null) {
                equipBestTool(mc.world.getBlockState(event.getPos()));
            }
        }
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        if(autoWeapon.getValue()) {
            if(!fullNullCheck() && event.getTarget() != null) {
                equipBestWeapon(event.getTarget());
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if(fullNullCheck()) {
            return;
        }

        if(noFriendAttack.getValue() && event.getPacket() instanceof CPacketUseEntity) {
            CPacketUseEntity packet = event.getPacket();
            Entity entity = packet.getEntityFromWorld(mc.world);
            if(entity != null && Phobos.friendManager.isFriend(entity.getName())) {
                event.setCanceled(true);
            }
        }
    }

    @Override
    public void onUpdate() {
        if(!fullNullCheck()) {
            if(mc.player.inventory.currentItem != lastHotbarSlot && mc.player.inventory.currentItem != currentTargetSlot) {
                lastHotbarSlot = mc.player.inventory.currentItem;
            }

            if(!mc.gameSettings.keyBindAttack.isKeyDown() && switched) {
                equip(lastHotbarSlot, false);
            }
        }
    }

    private void removeGlitchBlocks(BlockPos pos) {
        for(int dx = -4; dx <= 4; ++dx) {
            for(int dy = -4; dy <= 4; ++dy) {
                for(int dz = -4; dz <= 4; ++dz) {
                    BlockPos blockPos = new BlockPos(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                    if (mc.world.getBlockState(blockPos).getBlock().equals(Blocks.AIR)) {
                        mc.playerController.processRightClickBlock(mc.player, mc.world, blockPos, EnumFacing.DOWN, new Vec3d(0.5D, 0.5D, 0.5D), EnumHand.MAIN_HAND);
                    }
                }
            }
        }
    }

    private void equipBestTool(IBlockState blockState) {
        int bestSlot = -1;
        double max = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.isEmpty) continue;
            float speed = stack.getDestroySpeed(blockState);
            int eff;
            if (speed > 1) {
                speed += ((eff = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack)) > 0 ? (Math.pow(eff, 2) + 1) : 0);
                if (speed > max) {
                    max = speed;
                    bestSlot = i;
                }
            }
        }
        equip(bestSlot, true);
    }

    public void equipBestWeapon(Entity entity) {
        int bestSlot = -1;
        double maxDamage = 0;
        EnumCreatureAttribute creatureAttribute = EnumCreatureAttribute.UNDEFINED;
        if(EntityUtil.isLiving(entity)) {
            EntityLivingBase base = (EntityLivingBase)entity;
            creatureAttribute = base.getCreatureAttribute();
        }

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if(stack.isEmpty) {
                continue;
            }

            if (stack.getItem() instanceof ItemTool) {
                double damage = (((ItemTool)stack.getItem()).attackDamage + (double)EnchantmentHelper.getModifierForCreature(stack, creatureAttribute));
                if (damage > maxDamage) {
                    maxDamage = damage;
                    bestSlot = i;
                }
            } else if (stack.getItem() instanceof ItemSword) {
                double damage = (((ItemSword) stack.getItem()).getAttackDamage() + (double)EnchantmentHelper.getModifierForCreature(stack, creatureAttribute));
                if (damage > maxDamage) {
                    maxDamage = damage;
                    bestSlot = i;
                }
            }
        }
        equip(bestSlot, true);
    }

    private void equip(int slot, boolean equipTool) {
        if(slot != -1) {
            if(slot != mc.player.inventory.currentItem) {
                lastHotbarSlot = mc.player.inventory.currentItem;
            }
            currentTargetSlot = slot;
            mc.player.inventory.currentItem = slot;
            mc.playerController.syncCurrentPlayItem();
            switched = equipTool;
        }
    }
}
