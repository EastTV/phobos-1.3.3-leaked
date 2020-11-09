package me.earth.phobos.features.modules.render;

import me.earth.phobos.event.events.ConnectionEvent;
import me.earth.phobos.event.events.Render3DEvent;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.ColorUtil;
import me.earth.phobos.util.MathUtil;
import me.earth.phobos.util.RenderUtil;
import me.earth.phobos.util.TextUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class LogoutSpots extends Module {

    public Setting<Float> range = register(new Setting("Range", 300.0f, 50.0f, 500.0f));
    private final Setting<Integer> red = register(new Setting("Red", 255, 0, 255));
    private final Setting<Integer> green = register(new Setting("Green", 0, 0, 255));
    private final Setting<Integer> blue = register(new Setting("Blue", 0, 0, 255));
    private final Setting<Integer> alpha = register(new Setting("Alpha", 255, 0, 255));
    private final Setting<Boolean> scaleing = register(new Setting("Scale", false));
    private final Setting<Float> scaling = register(new Setting("Size", 4.0f, 0.1f, 20.0f));
    private final Setting<Float> factor = register(new Setting("Factor", 0.3f, 0.1f, 1.0f, v -> scaleing.getValue()));
    private final Setting<Boolean> smartScale = register(new Setting("SmartScale", false, v -> scaleing.getValue()));
    private final Setting<Boolean> rect = register(new Setting("Rectangle", true));
    private final Setting<Boolean> coords = register(new Setting("Coords", true));
    private final Setting<Boolean> notification = register(new Setting("Notification", true));
    public Setting<Boolean> message = register(new Setting("Message", false));

    private final List<LogoutPos> spots = new CopyOnWriteArrayList<>();

    public LogoutSpots() {
        super("LogoutSpots", "Renders LogoutSpots", Category.RENDER, true, false, false);
    }

    @Override
    public void onLogout() {
        spots.clear();
    }

    @Override
    public void onDisable() {
        spots.clear();
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if(!spots.isEmpty()) {
            synchronized (spots) {
                spots.forEach(
                    spot -> {
                        if (spot.getEntity() != null) {
                            final AxisAlignedBB bb = RenderUtil.interpolateAxis(spot.getEntity().getEntityBoundingBox());
                            RenderUtil.drawBlockOutline(bb, new Color(red.getValue(), green.getValue(), blue.getValue(), alpha.getValue()), 1.0f);
                            double x = interpolate(spot.getEntity().lastTickPosX, spot.getEntity().posX, event.getPartialTicks()) - mc.getRenderManager().renderPosX;
                            double y = interpolate(spot.getEntity().lastTickPosY, spot.getEntity().posY, event.getPartialTicks()) - mc.getRenderManager().renderPosY;
                            double z = interpolate(spot.getEntity().lastTickPosZ, spot.getEntity().posZ, event.getPartialTicks()) - mc.getRenderManager().renderPosZ;
                            renderNameTag(spot.getName(), x, y, z, event.getPartialTicks(), spot.getX(), spot.getY(), spot.getZ());
                        }
                    }
                );
            }
        }
    }

    @Override
    public void onUpdate() {
        if(!fullNullCheck()) {
            spots.removeIf(spot -> mc.player.getDistanceSq(spot.getEntity()) >= MathUtil.square(range.getValue()));
        }
    }

    @SubscribeEvent
    public void onConnection(ConnectionEvent event) {
        if(event.getStage() == 0) {
            UUID uuid = event.getUuid();
            EntityPlayer entity = mc.world.getPlayerEntityByUUID(uuid);
            if(entity != null) {
                if(message.getValue()) {
                    Command.sendMessage(TextUtil.GREEN + entity.getName() + " just logged in"+ (coords.getValue() ? " at (" + (int) entity.posX + ", " + (int) entity.posY + ", " + (int) entity.posZ + ")!" : "!"),notification.getValue());
                }
            }
            spots.removeIf(pos -> pos.getName().equalsIgnoreCase(event.getName()));
        } else if(event.getStage() == 1) {
            EntityPlayer entity = event.getEntity();
            UUID uuid = event.getUuid();
            String name = event.getName();
            if(message.getValue()) {
                Command.sendMessage(TextUtil.RED + event.getName() + " just logged out"+ (coords.getValue() ? " at (" + (int) entity.posX + ", " + (int) entity.posY + ", " + (int) entity.posZ + ")!" : "!"),notification.getValue());
            }
            if(name != null && entity != null && uuid != null) {
                spots.add(new LogoutPos(name, uuid, entity));
            }
        }
    }

    private void renderNameTag(String name, double x, double yi, double z, float delta, double xPos, double yPos, double zPos) {
        double y = yi + 0.7D;
        Entity camera = mc.getRenderViewEntity();
        assert camera != null;
        double originalPositionX = camera.posX;
        double originalPositionY = camera.posY;
        double originalPositionZ = camera.posZ;
        camera.posX = interpolate(camera.prevPosX, camera.posX, delta);
        camera.posY = interpolate(camera.prevPosY, camera.posY, delta);
        camera.posZ = interpolate(camera.prevPosZ, camera.posZ, delta);

        String displayTag = name + " XYZ: " + (int)xPos + ", " + (int)yPos + ", " + (int)zPos;
        double distance = camera.getDistance(x + mc.getRenderManager().viewerPosX, y + mc.getRenderManager().viewerPosY, z + mc.getRenderManager().viewerPosZ);
        int width = renderer.getStringWidth(displayTag) / 2;
        double scale = (0.0018 + scaling.getValue() * (distance * factor.getValue())) / 1000.0;

        if (distance <= 8 && smartScale.getValue()) {
            scale = 0.0245D;
        }

        if(!scaleing.getValue()) {
            scale = scaling.getValue() / 100.0;
        }

        GlStateManager.pushMatrix();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(1, -1500000);
        GlStateManager.disableLighting();
        GlStateManager.translate((float) x, (float) y + 1.4F, (float) z);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, mc.gameSettings.thirdPersonView == 2 ? -1.0F : 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-scale, -scale, scale);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();

        GlStateManager.enableBlend();
        if(rect.getValue()) {
            RenderUtil.drawRect(-width - 2, -(renderer.getFontHeight() + 1), width + 2F, 1.5F, 0x55000000);
        }
        GlStateManager.disableBlend();

        renderer.drawStringWithShadow(displayTag, -width, -(renderer.getFontHeight() - 1), ColorUtil.toRGBA(new Color(red.getValue(), green.getValue(), blue.getValue(), alpha.getValue())));

        camera.posX = originalPositionX;
        camera.posY = originalPositionY;
        camera.posZ = originalPositionZ;
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.disablePolygonOffset();
        GlStateManager.doPolygonOffset(1, 1500000);
        GlStateManager.popMatrix();
    }

    private double interpolate(double previous, double current, float delta) {
        return (previous + (current - previous) * delta);
    }

    private static class LogoutPos {

        private final String name;
        private final UUID uuid;
        private final EntityPlayer entity;
        private final double x;
        private final double y;
        private final double z;

        public LogoutPos(String name, UUID uuid, EntityPlayer entity) {
            this.name = name;
            this.uuid = uuid;
            this.entity = entity;
            this.x = entity.posX;
            this.y = entity.posY;
            this.z = entity.posZ;
        }


        public String getName() {
            return name;
        }

        public UUID getUuid() {
            return uuid;
        }

        public EntityPlayer getEntity() {
            return entity;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getZ() {
            return z;
        }
    }
}
