package me.earth.phobos.features.modules.render;

import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.Render3DEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.modules.combat.AutoCrystal;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.EntityUtil;
import me.earth.phobos.util.MathUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.*;

//TODO: rewrite
public class Tracer extends Module {

    public Setting<Boolean> players = register(new Setting("Players", true));
    public Setting<Boolean> mobs = register(new Setting("Mobs", false));
    public Setting<Boolean> animals = register(new Setting("Animals", false));
    public Setting<Boolean> invisibles = register(new Setting("Invisibles", false));
    public Setting<Boolean> drawFromSky = register(new Setting("DrawFromSky", false));
    public Setting<Float> width = register(new Setting("Width", 1.0f, 0.1f, 5.0f));
    public Setting<Integer> distance = register(new Setting("Radius", 300, 0, 300));
    public Setting<Boolean> crystalCheck = register(new Setting("CrystalCheck", false));

    public Tracer() {
        super("Tracers", "Draws lines to other players.", Category.RENDER, false, false, false);
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (fullNullCheck()) {
            return;
        }

        GlStateManager.pushMatrix();
        mc.world.loadedEntityList.stream()
                .filter(EntityUtil::isLiving)
                .filter(entity -> (entity instanceof EntityPlayer ? players.getValue() && mc.player != entity : (EntityUtil.isPassive(entity) ? animals.getValue() : mobs.getValue())))
                .filter(entity -> mc.player.getDistanceSq(entity) < MathUtil.square(distance.getValue()))
                .filter(entity -> invisibles.getValue() || !entity.isInvisible())
                .forEach(entity -> {
                    float[] colour = getColorByDistance(entity);
                    drawLineToEntity(entity, colour[0], colour[1], colour[2], colour[3]);
                });
        GlStateManager.popMatrix();
    }

    public double interpolate(double now, double then) {
        return then + (now - then) * mc.getRenderPartialTicks();
    }

    public double[] interpolate(Entity entity) {
        double posX = interpolate(entity.posX, entity.lastTickPosX) - mc.getRenderManager().renderPosX;
        double posY = interpolate(entity.posY, entity.lastTickPosY) - mc.getRenderManager().renderPosY;
        double posZ = interpolate(entity.posZ, entity.lastTickPosZ) - mc.getRenderManager().renderPosZ;
        return new double[]{posX, posY, posZ};
    }

    public void drawLineToEntity(Entity e, float red, float green, float blue, float opacity) {
        double[] xyz = interpolate(e);
        drawLine(xyz[0], xyz[1], xyz[2], e.height, red, green, blue, opacity);
    }

    public void drawLine(double posx, double posy, double posz, double up, float red, float green, float blue, float opacity) {
        Vec3d eyes = new Vec3d(0, 0, 1)
                .rotatePitch(-(float) Math
                        .toRadians(mc.player.rotationPitch))
                .rotateYaw(-(float) Math
                        .toRadians(mc.player.rotationYaw));

        if (!drawFromSky.getValue()) {
            drawLineFromPosToPos(eyes.x, eyes.y + mc.player.getEyeHeight(), eyes.z, posx, posy, posz, up, red, green, blue, opacity);
        } else {
            drawLineFromPosToPos(posx, 256, posz, posx, posy, posz, up, red, green, blue, opacity);
        }
    }

    public void drawLineFromPosToPos(double posx, double posy, double posz, double posx2, double posy2, double posz2, double up, float red, float green, float blue, float opacity) {
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glLineWidth(width.getValue());
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glColor4f(red, green, blue, opacity);
        GlStateManager.disableLighting();
        GL11.glLoadIdentity();
        mc.entityRenderer.orientCamera(mc.getRenderPartialTicks());
        GL11.glBegin(GL11.GL_LINES);
        {
            GL11.glVertex3d(posx, posy, posz);
            GL11.glVertex3d(posx2, posy2, posz2);
            GL11.glVertex3d(posx2, posy2, posz2);
        }
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor3d(1d, 1d, 1d);
        GlStateManager.enableLighting();
    }

    public float[] getColorByDistance(Entity entity) {
        if (entity instanceof EntityPlayer && Phobos.friendManager.isFriend(entity.getName())) {
            return new float[]{0.0f, 0.5f, 1.0f, 1.0f};
        }
        final AutoCrystal autoCrystal = Phobos.moduleManager.getModuleByClass(AutoCrystal.class);
        final Color col = new Color(Color.HSBtoRGB((float) (Math.max(0.0F, Math.min(mc.player.getDistanceSq(entity), crystalCheck.getValue() ? autoCrystal.placeRange.getValue() * autoCrystal.placeRange.getValue() : 2500) / (crystalCheck.getValue() ? autoCrystal.placeRange.getValue() * autoCrystal.placeRange.getValue() : 2500)) / 3.0F), 1.0F, 0.8f) | 0xFF000000);
        return new float[]{col.getRed() / 255.f, col.getGreen() / 255.f, col.getBlue() / 255.f, 1.0f};
    }
}
