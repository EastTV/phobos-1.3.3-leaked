package me.earth.phobos.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MathUtil implements Util {

    private static final Random random = new Random();

    public static int getRandom(final int min, final int max) {
        return min + random.nextInt(max - min + 1);
    }

    public static double getRandom(final double min, final double max) {
        return MathHelper.clamp(min + random.nextDouble() * max, min, max);
    }

    public static float getRandom(final float min, final float max) {
        return MathHelper.clamp(min + random.nextFloat() * max, min, max);
    }

    public static int clamp(int num, int min, int max) {
        return (num < min) ? min : (Math.min(num, max));
    }

    public static float clamp(float num, float min, float max) {
        return (num < min) ? min : (Math.min(num, max));
    }

    public static double clamp(double num, double min, double max) {
        return (num < min) ? min : (Math.min(num, max));
    }

    public static float sin(final float value) {
        return MathHelper.sin(value);
    }

    public static float cos(final float value) {
        return MathHelper.cos(value);
    }

    public static float wrapDegrees(final float value) {
        return MathHelper.wrapDegrees(value);
    }

    public static double wrapDegrees(final double value) {
        return MathHelper.wrapDegrees(value);
    }

    public static Vec3d roundVec(Vec3d vec3d, int places) {
        return new Vec3d(round(vec3d.x, places), round(vec3d.y, places), round(vec3d.z, places));
    }

    public static double square(double input) { return (input * input); }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.FLOOR);
        return bd.doubleValue();
    }

    public static float wrap(float valI) {
        float val = valI % 360.0f;
        if (val >= 180.0f)
            val -= 360.0f;
        if (val < -180.0f)
            val += 360.0f;
        return val;
    }

    public static Vec3d direction(float yaw) {
        return new Vec3d(Math.cos(degToRad(yaw + 90f)), 0, Math.sin(degToRad(yaw + 90f)));
    }

    public static float round(float value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.FLOOR);
        return bd.floatValue();
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, boolean descending) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        if(descending) {
            list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        } else {
            list.sort(Map.Entry.comparingByValue());
        }

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static String getTimeOfDay() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if (timeOfDay < 12){
            return "Good Morning ";
        } else if(timeOfDay < 16){
            return "Good Afternoon ";
        } else if(timeOfDay < 21){
            return "Good Evening ";
        } else {
            return "Good Night ";
        }
    }

    public static double radToDeg(double rad) {
        return rad * (float) (180.0f / Math.PI);
    }

    public static double degToRad(double deg) {
        return deg * (float) (Math.PI / 180.0f);
    }

    public static double getIncremental(final double val, final double inc) {
        final double one = 1.0 / inc;
        return Math.round(val * one) / one;
    }

    public static double[] directionSpeed(double speed) {
        float forward = mc.player.movementInput.moveForward;
        float side = mc.player.movementInput.moveStrafe;
        float yaw = mc.player.prevRotationYaw + (mc.player.rotationYaw - mc.player.prevRotationYaw) * mc.getRenderPartialTicks();
        if (forward != 0) {
            if (side > 0) {
                yaw += (forward > 0 ? -45 : 45);
            } else if (side < 0) {
                yaw += (forward > 0 ? 45 : -45);
            }
            side = 0;
            if (forward > 0) {
                forward = 1;
            } else if (forward < 0) {
                forward = -1;
            }
        }
        final double sin = Math.sin(Math.toRadians(yaw + 90));
        final double cos = Math.cos(Math.toRadians(yaw + 90));
        final double posX = (forward * speed * cos + side * speed * sin);
        final double posZ = (forward * speed * sin - side * speed * cos);
        return new double[] { posX, posZ };
    }

    public static List<Vec3d> getBlockBlocks(Entity entity) {
        List<Vec3d> vec3ds = new ArrayList<>();
        AxisAlignedBB bb = entity.getEntityBoundingBox();
        double y = entity.posY;
        double minX = round(bb.minX, 0);
        double minZ = round(bb.minZ, 0);
        double maxX = round(bb.maxX, 0);
        double maxZ = round(bb.maxZ, 0);
        if(minX != maxX) {
            vec3ds.add(new Vec3d(minX, y, minZ));
            vec3ds.add(new Vec3d(maxX, y, minZ));
            if(minZ != maxZ) {
                vec3ds.add(new Vec3d(minX, y, maxZ));
                vec3ds.add(new Vec3d(maxX, y, maxZ));
                return vec3ds;
            }
        } else if(minZ != maxZ) {
            vec3ds.add(new Vec3d(minX, y, minZ));
            vec3ds.add(new Vec3d(minX, y, maxZ));
            return vec3ds;
        }
        vec3ds.add(entity.getPositionVector());
        return vec3ds;
    }

    public static boolean areVec3dsAligned(Vec3d vec3d1, Vec3d vec3d2) {
        return areVec3dsAlignedRetarded(vec3d1, vec3d2);
        //Vec3d vec3d3 = MathUtil.roundVec(vec3d1, 0);
        //Vec3d vec3d4 = MathUtil.roundVec(vec3d2, 0);
        //return (vec3d3.x == vec3d4.x) && (vec3d3.z == vec3d4.z);
    }

    public static boolean areVec3dsAlignedRetarded(Vec3d vec3d1, Vec3d vec3d2) {
        BlockPos pos1 = new BlockPos(vec3d1);
        BlockPos pos2 = new BlockPos(vec3d2.x, vec3d1.y, vec3d2.z);
        return pos1.equals(pos2);
    }

    public static float[] calcAngle(Vec3d from, Vec3d to) {
        final double difX = to.x - from.x;
        final double difY = (to.y - from.y) * -1.0F;
        final double difZ = to.z - from.z;
        final double dist = MathHelper.sqrt(difX * difX + difZ * difZ);
        return new float[]{(float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0f), (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difY, dist)))};
    }
}

