package me.earth.phobos.util;

import me.earth.phobos.Phobos;
import me.earth.phobos.features.modules.client.Managers;
import me.earth.phobos.features.modules.combat.Killaura;
import me.earth.phobos.features.modules.player.Blink;
import me.earth.phobos.features.modules.player.FakePlayer;
import me.earth.phobos.features.modules.player.Freecam;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockDeadBush;
import net.minecraft.block.BlockFire;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumHand;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EntityUtil implements Util {

    public static final Vec3d[] antiDropOffsetList = {
            new Vec3d(0, -2, 0),
    };

    public static final Vec3d[] platformOffsetList = {
            new Vec3d(0, -1, 0),
            new Vec3d(0, -1, -1),
            new Vec3d(0, -1, 1),
            new Vec3d(-1, -1, 0),
            new Vec3d(1, -1, 0)
    };

    public static final Vec3d[] legOffsetList = {
            new Vec3d(-1, 0, 0),
            new Vec3d(1, 0, 0),
            new Vec3d(0, 0, -1),
            new Vec3d(0, 0, 1)
    };

    public static final Vec3d[] OffsetList = {
            new Vec3d(1, 1, 0),
            new Vec3d(-1, 1, 0),
            new Vec3d(0, 1, 1),
            new Vec3d(0, 1, -1),
            new Vec3d(0, 2, 0),
            //new Vec3d(0, 2, -1)
    };

    public static final Vec3d[] antiStepOffsetList = {
            new Vec3d(-1, 2, 0),
            new Vec3d(1, 2, 0),
            new Vec3d(0, 2, 1),
            new Vec3d(0, 2, -1),
    };

    public static final Vec3d[] antiScaffoldOffsetList = {
            new Vec3d(0, 3, 0)
    };

    public static void attackEntity(Entity entity, boolean packet, boolean swingArm) {
        if(packet) {
            mc.player.connection.sendPacket(new CPacketUseEntity(entity));
        } else {
            mc.playerController.attackEntity(mc.player, entity);
        }

        if(swingArm) {
            mc.player.swingArm(EnumHand.MAIN_HAND);
        }
    }

    public static Vec3d interpolateEntity(Entity entity, float time) {
        return new Vec3d(entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * time,
                entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * time,
                entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * time);
    }

    public static Vec3d getInterpolatedPos(Entity entity, float partialTicks) {
        return new Vec3d(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ).add(getInterpolatedAmount(entity, partialTicks));
    }

    public static Vec3d getInterpolatedRenderPos(Entity entity, float partialTicks) {
        return getInterpolatedPos(entity, partialTicks).subtract(mc.getRenderManager().renderPosX, mc.getRenderManager().renderPosY, mc.getRenderManager().renderPosZ);
    }

    public static Vec3d getInterpolatedRenderPos(Vec3d vec) {
        return (new Vec3d(vec.x, vec.y, vec.z)).subtract(mc.getRenderManager().renderPosX, mc.getRenderManager().renderPosY, mc.getRenderManager().renderPosZ);
    }

    public static Vec3d getInterpolatedAmount(Entity entity, double x, double y, double z) {
        return new Vec3d(
                (entity.posX - entity.lastTickPosX) * x,
                (entity.posY - entity.lastTickPosY) * y,
                (entity.posZ - entity.lastTickPosZ) * z
        );
    }

    public static Vec3d getInterpolatedAmount(Entity entity, Vec3d vec) {
        return getInterpolatedAmount(entity, vec.x, vec.y, vec.z);
    }

    public static Vec3d getInterpolatedAmount(Entity entity, float partialTicks) {
        return getInterpolatedAmount(entity, partialTicks, partialTicks, partialTicks);
    }

    public static boolean isPassive(Entity entity){
        if (entity instanceof EntityWolf && ((EntityWolf) entity).isAngry()) return false;
        if(entity instanceof EntityAgeable || entity instanceof EntityAmbientCreature || entity instanceof EntitySquid) return true;
        return entity instanceof EntityIronGolem && ((EntityIronGolem) entity).getRevengeTarget() == null;
    }

    public static boolean isSafe(Entity entity, int height, boolean floor) {
        return getUnsafeBlocks(entity, height, floor).size() == 0;
    }

    public static boolean stopSneaking(boolean isSneaking) {
        if(isSneaking && mc.player != null) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        }
        return false;
    }

    public static boolean isSafe(Entity entity) { return isSafe(entity, 0, false); }

    public static BlockPos getPlayerPos(EntityPlayer player) {
        return new BlockPos(Math.floor(player.posX), Math.floor(player.posY), Math.floor(player.posZ));
    }

    public static List<Vec3d> getUnsafeBlocks(Entity entity, int height, boolean floor) {
        return getUnsafeBlocksFromVec3d(entity.getPositionVector(), height, floor);
    }

    public static boolean isMobAggressive(Entity entity) {
        if(entity instanceof EntityPigZombie) {
            if(((EntityPigZombie) entity).isArmsRaised() || ((EntityPigZombie) entity).isAngry()) {
                return true;
            }
        } else if(entity instanceof EntityWolf) {
            return ((EntityWolf) entity).isAngry() &&
                    !mc.player.equals(((EntityWolf) entity).getOwner());
        } else if(entity instanceof EntityEnderman) {
            return ((EntityEnderman) entity).isScreaming();
        }
        return isHostileMob(entity);
    }

    public static boolean isNeutralMob(Entity entity) {
        return entity instanceof EntityPigZombie ||
                entity instanceof EntityWolf ||
                entity instanceof EntityEnderman;
    }

    public static boolean isProjectile(Entity entity) {
        return entity instanceof EntityShulkerBullet || entity instanceof EntityFireball;
    }

    public static boolean isVehicle(Entity entity) {
        return entity instanceof EntityBoat || entity instanceof EntityMinecart;
    }

    public static boolean isFriendlyMob(Entity entity) {
        return (entity.isCreatureType(EnumCreatureType.CREATURE, false) && !EntityUtil.isNeutralMob(entity)) ||
                (entity.isCreatureType(EnumCreatureType.AMBIENT, false)) ||
                entity instanceof EntityVillager ||
                entity instanceof EntityIronGolem ||
                (isNeutralMob(entity) && !EntityUtil.isMobAggressive(entity));
    }

    public static boolean isHostileMob(Entity entity) {
        return (entity.isCreatureType(EnumCreatureType.MONSTER, false) && !EntityUtil.isNeutralMob(entity));
    }

    public static List<Vec3d> getUnsafeBlocksFromVec3d(Vec3d pos, int height, boolean floor) {
        List<Vec3d> vec3ds = new ArrayList<>();
        for(Vec3d vector : getOffsets(height, floor)) {
            BlockPos targetPos = new BlockPos(pos).add(vector.x, vector.y, vector.z);
            Block block = mc.world.getBlockState(targetPos).getBlock();
            if (block instanceof BlockAir || block instanceof BlockLiquid || block instanceof BlockTallGrass || block instanceof BlockFire || block instanceof BlockDeadBush || block instanceof BlockSnow) {
                vec3ds.add(vector);
            }
        }
        return vec3ds;
    }
    public static boolean isInHole(Entity entity) {
        return isBlockValid(new BlockPos(entity.posX, entity.posY, entity.posZ));
    }

    public static boolean isBlockValid(BlockPos blockPos) {
        return isBedrockHole(blockPos) || isObbyHole(blockPos) || isBothHole(blockPos);
    }

    public static boolean isObbyHole(BlockPos blockPos) {
        BlockPos[] touchingBlocks = new BlockPos[]{blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west(), blockPos.down()};
        for (BlockPos pos : touchingBlocks) {
            IBlockState touchingState = mc.world.getBlockState(pos);
            if (touchingState.getBlock() == Blocks.AIR || touchingState.getBlock() != Blocks.OBSIDIAN) {
                return false;
            }
        }

        return true;
    }

    public static boolean isBedrockHole(BlockPos blockPos) {
        BlockPos[] touchingBlocks = new BlockPos[]{blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west(), blockPos.down()};
        for (BlockPos pos : touchingBlocks) {
            IBlockState touchingState = mc.world.getBlockState(pos);
            if (touchingState.getBlock() == Blocks.AIR || touchingState.getBlock() != Blocks.BEDROCK) {
                return false;
            }
        }

        return true;
    }

    public static boolean isBothHole(BlockPos blockPos) {
        BlockPos[] touchingBlocks = new BlockPos[]{blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west(), blockPos.down()};
        for (BlockPos pos : touchingBlocks) {
            IBlockState touchingState = mc.world.getBlockState(pos);
            if (touchingState.getBlock() == Blocks.AIR || touchingState.getBlock() != Blocks.BEDROCK && touchingState.getBlock() != Blocks.OBSIDIAN) {
                return false;
            }
        }
        return true;
    }
    public static Vec3d[] getUnsafeBlockArray(Entity entity, int height, boolean floor) {
        List<Vec3d> list = getUnsafeBlocks(entity, height, floor);
        Vec3d[] array = new Vec3d[list.size()];
        return list.toArray(array);
    }

    public static Vec3d[] getUnsafeBlockArrayFromVec3d(Vec3d pos, int height, boolean floor) {
        List<Vec3d> list = getUnsafeBlocksFromVec3d(pos, height, floor);
        Vec3d[] array = new Vec3d[list.size()];
        return list.toArray(array);
    }

    public static double getDst(Vec3d vec) {
        return mc.player.getPositionVector().distanceTo(vec);
    }

    public static boolean isTrapped(EntityPlayer player, boolean antiScaffold, boolean antiStep, boolean legs, boolean platform, boolean antiDrop) {
        return getUntrappedBlocks(player, antiScaffold, antiStep, legs, platform, antiDrop).size() == 0;
    }

    public static boolean isTrappedExtended(int extension, EntityPlayer player, boolean antiScaffold, boolean antiStep, boolean legs, boolean platform, boolean antiDrop, boolean raytrace) {
        return getUntrappedBlocksExtended(extension, player, antiScaffold, antiStep, legs, platform, antiDrop, raytrace).size() == 0;
    }

    public static List<Vec3d> getUntrappedBlocks(EntityPlayer player, boolean antiScaffold, boolean antiStep, boolean legs, boolean platform, boolean antiDrop) {
        List<Vec3d> vec3ds = new ArrayList<>();
        if(!antiStep && getUnsafeBlocks(player, 2, false).size() == 4) {
            vec3ds.addAll(getUnsafeBlocks(player, 2, false));
        }
        for (int i = 0; i < getTrapOffsets(antiScaffold, antiStep, legs, platform, antiDrop).length; i++) {
            Vec3d vector = getTrapOffsets(antiScaffold, antiStep, legs, platform, antiDrop)[i];
            BlockPos targetPos = new BlockPos(player.getPositionVector()).add(vector.x, vector.y, vector.z);
            Block block = mc.world.getBlockState(targetPos).getBlock();
            if (block instanceof BlockAir || block instanceof BlockLiquid || block instanceof BlockTallGrass || block instanceof BlockFire || block instanceof BlockDeadBush || block instanceof BlockSnow) {
                vec3ds.add(vector);
            }
        }
        return vec3ds;
    }

    public static boolean isInWater(Entity entity) {
        if(entity == null) return false;

        double y = entity.posY + 0.01;

        for(int x = MathHelper.floor(entity.posX); x < MathHelper.ceil(entity.posX); x++)
            for (int z = MathHelper.floor(entity.posZ); z < MathHelper.ceil(entity.posZ); z++) {
                BlockPos pos = new BlockPos(x, (int) y, z);

                if(mc.world.getBlockState(pos).getBlock() instanceof BlockLiquid) return true;
            }

        return false;
    }

    public static boolean isDrivenByPlayer(Entity entityIn) {
        return mc.player != null && entityIn != null && entityIn.equals(mc.player.getRidingEntity());
    }

    public static boolean isPlayer(Entity entity) {
        return entity instanceof EntityPlayer;
    }

    public static boolean isAboveWater(Entity entity) { return isAboveWater(entity, false); }

    public static boolean isAboveWater(Entity entity, boolean packet){
        if (entity == null) return false;

        double y = entity.posY - (packet ? 0.03 : (isPlayer(entity) ? 0.2 : 0.5)); // increasing this seems to flag more in NCP but needs to be increased so the player lands on solid water

        for(int x = MathHelper.floor(entity.posX); x < MathHelper.ceil(entity.posX); x++)
            for (int z = MathHelper.floor(entity.posZ); z < MathHelper.ceil(entity.posZ); z++) {
                BlockPos pos = new BlockPos(x, MathHelper.floor(y), z);

                if (mc.world.getBlockState(pos).getBlock() instanceof BlockLiquid) return true;
            }

        return false;
    }

    public static List<Vec3d> getUntrappedBlocksExtended(int extension, EntityPlayer player, boolean antiScaffold, boolean antiStep, boolean legs, boolean platform, boolean antiDrop, boolean raytrace) {
        List<Vec3d> placeTargets = new ArrayList<>();
        if(extension == 1) {
            placeTargets.addAll(targets(player.getPositionVector(), antiScaffold, antiStep, legs, platform, antiDrop, raytrace));
        } else {
            int extend = 1;
            for (Vec3d vec3d : MathUtil.getBlockBlocks(player)) {
                if(extend > extension) {
                    break;
                }
                placeTargets.addAll(targets(vec3d, antiScaffold, antiStep, legs, platform, antiDrop, raytrace));
                extend++;
            }
        }

        List<Vec3d> removeList = new ArrayList<>();
        for(Vec3d vec3d : placeTargets) {
            BlockPos pos = new BlockPos(vec3d);
            if(BlockUtil.isPositionPlaceable(pos, raytrace) == -1) {
                removeList.add(vec3d);
            }
        }

        for(Vec3d vec3d : removeList) {
            placeTargets.remove(vec3d);
        }

        return placeTargets;
    }

    public static List<Vec3d> targets(Vec3d vec3d, boolean antiScaffold, boolean antiStep, boolean legs, boolean platform, boolean antiDrop, boolean raytrace) {
        List<Vec3d> placeTargets = new ArrayList<>();
        if(antiDrop) {
            Collections.addAll(placeTargets, BlockUtil.convertVec3ds(vec3d, antiDropOffsetList));
        }

        if(platform){
            Collections.addAll(placeTargets, BlockUtil.convertVec3ds(vec3d, platformOffsetList));
        }

        if(legs){
            Collections.addAll(placeTargets, BlockUtil.convertVec3ds(vec3d, legOffsetList));
        }

        Collections.addAll(placeTargets, BlockUtil.convertVec3ds(vec3d, OffsetList));

        if(antiStep){
            Collections.addAll(placeTargets, BlockUtil.convertVec3ds(vec3d, antiStepOffsetList));
        } else {
            List<Vec3d> vec3ds = EntityUtil.getUnsafeBlocksFromVec3d(vec3d, 2, false);
            if(vec3ds.size() == 4) {
                for (Vec3d vector : vec3ds) {
                    BlockPos position = new BlockPos(vec3d).add(vector.x, vector.y, vector.z);
                    switch (BlockUtil.isPositionPlaceable(position, raytrace)) {
                        case 0:
                            break;
                        case -1:
                        case 1:
                        case 2:
                            continue;
                        case 3:
                            placeTargets.add(vec3d.add(vector));
                            break;
                    }
                    break;
                }
            }
        }

        if(antiScaffold){
            Collections.addAll(placeTargets, BlockUtil.convertVec3ds(vec3d, antiScaffoldOffsetList));
        }
        return placeTargets;
    }

    public static List<Vec3d> getOffsetList(int y, boolean floor) {
        List<Vec3d> offsets = new ArrayList<>();
        offsets.add(new Vec3d(-1, y, 0));
        offsets.add(new Vec3d(1, y, 0));
        offsets.add(new Vec3d(0, y, -1));
        offsets.add(new Vec3d(0, y, 1));

        if(floor) {
            offsets.add(new Vec3d(0, y - 1, 0));
        }

        return offsets;
    }

    public static Vec3d[] getOffsets(int y, boolean floor) {
        List<Vec3d> offsets = getOffsetList(y, floor);
        Vec3d[] array = new Vec3d[offsets.size()];
        return offsets.toArray(array);
    }

    public static Vec3d[] getTrapOffsets(boolean antiScaffold, boolean antiStep, boolean legs, boolean platform, boolean antiDrop) {
        List<Vec3d> offsets = getTrapOffsetsList(antiScaffold, antiStep, legs, platform, antiDrop);
        Vec3d[] array = new Vec3d[offsets.size()];
        return offsets.toArray(array);
    }

    public static List<Vec3d> getTrapOffsetsList(boolean antiScaffold, boolean antiStep, boolean legs, boolean platform, boolean antiDrop) {
        List<Vec3d> offsets = new ArrayList<>(getOffsetList(1, false));
        offsets.add(new Vec3d(0, 2, 0));
        if(antiScaffold) {
            offsets.add(new Vec3d(0, 3, 0));
        }
        if(antiStep) {
            offsets.addAll(getOffsetList(2, false));
        }
        if(legs) {
            offsets.addAll(getOffsetList(0, false));
        }
        if(platform) {
            offsets.addAll(getOffsetList(-1, false));
            offsets.add(new Vec3d(0, -1, 0));
        }
        if(antiDrop) {
            offsets.add(new Vec3d(0, -2, 0));
        }
        return offsets;
    }

    public static Vec3d[] getHeightOffsets(int min, int max) {
        List<Vec3d> offsets = new ArrayList<>();
        for(int i = min; i <= max; i++) {
            offsets.add(new Vec3d(0, i, 0));
        }
        Vec3d[] array = new Vec3d[offsets.size()];
        return offsets.toArray(array);
    }

    public static BlockPos getRoundedBlockPos(Entity entity) {
        return new BlockPos(MathUtil.roundVec(entity.getPositionVector(), 0));
    }

    public static boolean isLiving(Entity entity) {
        return entity instanceof EntityLivingBase;
    }

    public static boolean isAlive(Entity entity) {
        return isLiving(entity) && !entity.isDead && ((EntityLivingBase)(entity)).getHealth() > 0;
    }

    public static boolean isDead(Entity entity) {
        return !isAlive(entity);
    }

    public static float getHealth(Entity entity) {
        if(isLiving(entity)) {
            EntityLivingBase livingBase = (EntityLivingBase)entity;
            return livingBase.getHealth() + livingBase.getAbsorptionAmount();
        }
        return 0.0f;
    }

    public static float getHealth(Entity entity, boolean absorption) {
        if(isLiving(entity)) {
            EntityLivingBase livingBase = (EntityLivingBase)entity;
            return livingBase.getHealth() + (absorption ? livingBase.getAbsorptionAmount() : 0.0f);
        }
        return 0.0f;
    }

    public static boolean canEntityFeetBeSeen(Entity entityIn) {
        return mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posX + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(entityIn.posX, entityIn.posY, entityIn.posZ), false, true, false) == null;
    }

    public static boolean isntValid(Entity entity, double range) {
        return entity == null || isDead(entity) || entity.equals(mc.player) || (entity instanceof EntityPlayer && Phobos.friendManager.isFriend(entity.getName())) || mc.player.getDistanceSq(entity) > MathUtil.square(range);
    }

    public static boolean isValid(Entity entity, double range) {
        return !isntValid(entity, range);
    }

    public static boolean holdingWeapon(EntityPlayer player) {
        return player.getHeldItemMainhand().getItem() instanceof ItemSword || player.getHeldItemMainhand().getItem() instanceof ItemAxe;
    }

    public static double getMaxSpeed() {
        double maxModifier = 0.2873;
        if (mc.player.isPotionActive(Objects.requireNonNull(Potion.getPotionById(1)))) {
            maxModifier *= 1.0 + 0.2 * (Objects.requireNonNull(mc.player.getActivePotionEffect(Objects.requireNonNull(Potion.getPotionById(1)))).getAmplifier() + 1);
        }
        return maxModifier;
    }

    public static void mutliplyEntitySpeed(final Entity entity, final double multiplier) {
        if (entity != null) {
            entity.motionX *= multiplier;
            entity.motionZ *= multiplier;
        }
    }

    public static boolean isEntityMoving(final Entity entity) {
        if (entity == null) {
            return false;
        }
        if (entity instanceof EntityPlayer) {
            return mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown() || mc.gameSettings.keyBindLeft.isKeyDown() || mc.gameSettings.keyBindRight.isKeyDown();
        }
        return entity.motionX != 0.0 || entity.motionY != 0.0 || entity.motionZ != 0.0;
    }

    public static double getEntitySpeed(final Entity entity) {
        if (entity != null) {
            double distTraveledLastTickX = entity.posX - entity.prevPosX;
            double distTraveledLastTickZ = entity.posZ - entity.prevPosZ;
            double speed = MathHelper.sqrt(distTraveledLastTickX * distTraveledLastTickX + distTraveledLastTickZ * distTraveledLastTickZ);
            return speed * 20.0;
        }
        return 0.0;
    }

    public static boolean holding32k(EntityPlayer player) {
        return is32k(player.getHeldItemMainhand());
    }

    public static boolean is32k(ItemStack stack) {
        if(stack == null) {
            return false;
        }

        if (stack.getTagCompound() == null) {
            return false;
        }
        NBTTagList enchants = (NBTTagList) stack.getTagCompound().getTag("ench");
        if (enchants == null) {
            return false;
        }

        for (int i = 0; i < enchants.tagCount(); i++) {
            NBTTagCompound enchant = enchants.getCompoundTagAt(i);
            if(enchant.getInteger("id") == 16) {
                int lvl = enchant.getInteger("lvl");
                if (lvl >= 42) {
                    return true;
                }
                break;
            }
        }
        return false;
    }

    public static boolean simpleIs32k(ItemStack stack) {
        return EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS, stack) >= 1000;
    }

    public static void moveEntityStrafe(double speed, Entity entity) {
        if (entity != null) {
            MovementInput movementInput = mc.player.movementInput;
            double forward = movementInput.moveForward;
            double strafe = movementInput.moveStrafe;
            float yaw = mc.player.rotationYaw;
            if (forward == 0.0 && strafe == 0.0) {
                entity.motionX = 0.0;
                entity.motionZ = 0.0;
            }
            else {
                if (forward != 0.0) {
                    if (strafe > 0.0) {
                        yaw += ((forward > 0.0) ? -45 : 45);
                    }
                    else if (strafe < 0.0) {
                        yaw += ((forward > 0.0) ? 45 : -45);
                    }
                    strafe = 0.0;
                    if (forward > 0.0) {
                        forward = 1.0;
                    }
                    else if (forward < 0.0) {
                        forward = -1.0;
                    }
                }
                entity.motionX = forward * speed * Math.cos(Math.toRadians(yaw + 90.0f)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0f));
                entity.motionZ = forward * speed * Math.sin(Math.toRadians(yaw + 90.0f)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0f));
            }
        }
    }

    public static boolean rayTraceHitCheck(Entity entity, boolean shouldCheck) {
        return !shouldCheck || mc.player.canEntityBeSeen(entity);
    }

    public static Color getColor(Entity entity, int red, int green, int blue, int alpha, boolean colorFriends) {
        Color color = new Color(red / 255.0f, green / 255.0f, blue / 255.0f, alpha / 255.0f);
        if(entity instanceof EntityPlayer) {
            if (colorFriends && Phobos.friendManager.isFriend((EntityPlayer)entity)) {
                color = new Color(85 / 255.0f, 255 / 255.0f, 255 / 255.0f, alpha / 255.0f);
            }

            Killaura killaura = (Killaura)Phobos.moduleManager.getModuleByClass(Killaura.class);
            if (killaura.info.getValue() && killaura.target != null && killaura.target.equals(entity)) {
                color = new Color(255 / 255.0f, 0f, 0f, alpha / 255.0f);
            }
        }
        return color;
    }

    public static boolean isFakePlayer(EntityPlayer player) {
        Freecam freecam = Freecam.getInstance();
        FakePlayer fakePlayer = FakePlayer.getInstance();
        Blink blink = Blink.getInstance();
        int playerID = player.getEntityId();
        if(freecam.isOn() && playerID == 69420) {
            return true;
        }

        if(fakePlayer.isOn()) {
            for(int id : fakePlayer.fakePlayerIdList) {
                if(id == playerID) {
                    return true;
                }
            }
        }

        if(blink.isOn()) {
            return playerID == 6942069;
        }

        return false;
    }

    public static boolean isMoving() {
        return (mc.player.moveForward != 0D || mc.player.moveStrafing != 0D);
    }

    public static EntityPlayer getClosestEnemy(double distance) {
        EntityPlayer closest = null;
        for(EntityPlayer player : mc.world.playerEntities) {
            if(EntityUtil.isntValid(player, distance)) {
                continue;
            }

            if(closest == null) {
                closest = player;
                continue;
            }

            if(mc.player.getDistanceSq(player) < mc.player.getDistanceSq(closest)) {
                closest = player;
            }
        }
        return closest;
    }

    public static boolean checkCollide() {
        if(mc.player.isSneaking()) {
            return false;
        }

        if (mc.player.getRidingEntity() != null && mc.player.getRidingEntity().fallDistance >= 3.0f) {
            return false;
        }

        return !(mc.player.fallDistance >= 3.0f);
    }

    public static boolean isInLiquid() {
        if (mc.player.fallDistance >= 3.0f) {
            return false;
        }

        boolean inLiquid = false;
        final AxisAlignedBB bb = mc.player.getRidingEntity() != null ? mc.player.getRidingEntity().getEntityBoundingBox() : mc.player.getEntityBoundingBox();
        int y = (int) bb.minY;
        for (int x = MathHelper.floor(bb.minX); x < MathHelper.floor(bb.maxX) + 1; x++) {
            for (int z = MathHelper.floor(bb.minZ); z < MathHelper.floor(bb.maxZ) + 1; z++) {
                final Block block = mc.world.getBlockState(new BlockPos(x, y, z)).getBlock();
                if(!(block instanceof BlockAir)) {
                    if (!(block instanceof BlockLiquid)) {
                        return false;
                    }
                    inLiquid = true;
                }
            }
        }
        return inLiquid;
    }

    public static boolean isOnLiquid(double offset) {
        if (mc.player.fallDistance >= 3.0f) {
            return false;
        }

        final AxisAlignedBB bb = mc.player.getRidingEntity() != null ? mc.player.getRidingEntity().getEntityBoundingBox().contract(0.0d, 0.0d, 0.0d).offset(0.0d, -offset, 0.0d) : mc.player.getEntityBoundingBox().contract(0.0d, 0.0d, 0.0d).offset(0.0d, -offset, 0.0d);
        boolean onLiquid = false;
        int y = (int) bb.minY;
        for (int x = MathHelper.floor(bb.minX); x < MathHelper.floor(bb.maxX + 1.0D); x++) {
            for (int z = MathHelper.floor(bb.minZ); z < MathHelper.floor(bb.maxZ + 1.0D); z++) {
                final Block block = mc.world.getBlockState(new BlockPos(x, y, z)).getBlock();
                if (block != Blocks.AIR) {
                    if (!(block instanceof BlockLiquid)) {
                        return false;
                    }
                    onLiquid = true;
                }
            }
        }
        return onLiquid;
    }

    public static boolean isAboveLiquid(Entity entity) {
        if (entity == null) {
            return false;
        }

        final double n = entity.posY + 0.01;
        int i = MathHelper.floor(entity.posX);
        while (i < MathHelper.ceil(entity.posX)) {
            int j = MathHelper.floor(entity.posZ);
            while (j < MathHelper.ceil(entity.posZ)) {
                if (mc.world.getBlockState(new BlockPos(i, (int)n, j)).getBlock() instanceof BlockLiquid) {
                    return true;
                }
                ++j;
            }
            ++i;
        }
        return false;
    }

    public static BlockPos getPlayerPosWithEntity() {
        return new BlockPos(
                mc.player.getRidingEntity() != null ? mc.player.getRidingEntity().posX : mc.player.posX,
                mc.player.getRidingEntity() != null ? mc.player.getRidingEntity().posY : mc.player.posY,
                mc.player.getRidingEntity() != null ? mc.player.getRidingEntity().posZ : mc.player.posZ);
    }

    public static boolean checkForLiquid(Entity entity, boolean b) {
        if (entity == null) {
            return false;
        }

        final double posY = entity.posY;
        double n;
        if (b) {
            n = 0.03;
        } else if (entity instanceof EntityPlayer) {
            n = 0.2;
        } else {
            n = 0.5;
        }

        final double n2 = posY - n;
        int i = MathHelper.floor(entity.posX);
        while (i < MathHelper.ceil(entity.posX)) {
            int j = MathHelper.floor(entity.posZ);
            while (j < MathHelper.ceil(entity.posZ)) {
                if (mc.world.getBlockState(new BlockPos(i, MathHelper.floor(n2), j)).getBlock() instanceof BlockLiquid) {
                    return true;
                }
                ++j;
            }
            ++i;
        }
        return false;
    }

    public static boolean isOnLiquid() {
        double y = mc.player.posY - 0.03D;
        for(int x = MathHelper.floor(mc.player.posX); x < MathHelper.ceil(mc.player.posX); ++x) {
            for(int z = MathHelper.floor(mc.player.posZ); z < MathHelper.ceil(mc.player.posZ); ++z) {
                BlockPos pos = new BlockPos(x, MathHelper.floor(y), z);
                if (mc.world.getBlockState(pos).getBlock() instanceof BlockLiquid) {
                    return true;
                }
            }
        }

        return false;
    }

    public static double[] forward(final double speed) {
        float forward = mc.player.movementInput.moveForward;
        float side = mc.player.movementInput.moveStrafe;
        float yaw = mc.player.prevRotationYaw + (mc.player.rotationYaw - mc.player.prevRotationYaw) * mc.getRenderPartialTicks();
        if (forward != 0.0f) {
            if (side > 0.0f) {
                yaw += ((forward > 0.0f) ? -45 : 45);
            } else if (side < 0.0f) {
                yaw += ((forward > 0.0f) ? 45 : -45);
            }
            side = 0.0f;
            if (forward > 0.0f) {
                forward = 1.0f;
            } else if (forward < 0.0f) {
                forward = -1.0f;
            }
        }
        final double sin = Math.sin(Math.toRadians(yaw + 90.0f));
        final double cos = Math.cos(Math.toRadians(yaw + 90.0f));
        final double posX = forward * speed * cos + side * speed * sin;
        final double posZ = forward * speed * sin - side * speed * cos;
        return new double[]{posX, posZ};
    }

    public static Map<String, Integer> getTextRadarPlayers() {
        Map<String, Integer> output = new HashMap<>();
        DecimalFormat dfHealth = new DecimalFormat("#.#");
        dfHealth.setRoundingMode(RoundingMode.CEILING);
        DecimalFormat dfDistance = new DecimalFormat("#.#");
        dfDistance.setRoundingMode(RoundingMode.CEILING);
        StringBuilder healthSB = new StringBuilder();
        StringBuilder distanceSB = new StringBuilder();
        for(EntityPlayer player : mc.world.playerEntities) {
            if(player.isInvisible() && !Managers.getInstance().tRadarInv.getValue()) {
                continue;
            }

            if (player.getName().equals(mc.player.getName())) {
                continue;
            }
            int hpRaw = (int)EntityUtil.getHealth(player);
            String hp = dfHealth.format(hpRaw);
            healthSB.append(TextUtil.SECTIONSIGN);
            if (hpRaw >= 20) {
                healthSB.append("a");
            } else if (hpRaw >= 10) {
                healthSB.append("e");
            } else if (hpRaw >= 5) {
                healthSB.append("6");
            } else {
                healthSB.append("c");
            }
            healthSB.append(hp);
            int distanceInt = (int) mc.player.getDistance(player);
            String distance = dfDistance.format(distanceInt);
            distanceSB.append(TextUtil.SECTIONSIGN);
            if (distanceInt >= 25) {
                distanceSB.append("a");
            } else if (distanceInt > 10) {
                distanceSB.append("6");
            } else if (distanceInt >= 50) {
                distanceSB.append("7"); //TODO: Always false!!!
            } else {
                distanceSB.append("c");
            }
            distanceSB.append(distance);
            output.put(healthSB.toString() + " " + (Phobos.friendManager.isFriend(player) ? TextUtil.AQUA : TextUtil.RESET) + player.getName() + " " + distanceSB.toString() + " " + TextUtil.WHITE + Phobos.totemPopManager.getTotemPopString(player) + Phobos.potionManager.getTextRadarPotion(player), (int) mc.player.getDistance(player));

            healthSB.setLength(0);
            distanceSB.setLength(0);
        }

        if (!output.isEmpty()) {
            output = MathUtil.sortByValue(output, false);
        }
        return output;
    }

    public static boolean isAboveBlock(Entity entity, BlockPos blockPos) {
        return entity.posY >= blockPos.getY();
    }
}
