package me.earth.phobos.features.modules.render;

import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.Render3DEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.EntityUtil;
import me.earth.phobos.util.MathUtil;
import me.earth.phobos.util.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glVertex3d;

public class Ranges extends Module {

    public Ranges() {
        super("Ranges", "Draws a circle around the player.", Category.RENDER, false, false, false);
    }

    private final Setting<Boolean> hitSpheres = register(new Setting("HitSpheres", false));
    private final Setting<Boolean> circle = register(new Setting("Circle", true));
    private final Setting<Boolean> ownSphere = register(new Setting("OwnSphere", false, v -> hitSpheres.getValue()));
    private final Setting<Float> lineWidth = register(new Setting("LineWidth", 1.5f, 0.1f, 5.0f));
    private final Setting<Double> radius = register(new Setting("Radius", 4.5d, 0.1d, 8.0d));

    @Override
    public void onRender3D(Render3DEvent event) {
        if(circle.getValue()) {
            GlStateManager.pushMatrix();
            RenderUtil.GLPre(lineWidth.getValue());
            GlStateManager.enableBlend();
            GlStateManager.glLineWidth(3.0f);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

            final RenderManager renderManager = mc.getRenderManager();
            final Color color = Color.RED;

            java.util.List<Vec3d> hVectors = new ArrayList<>();
            List<Vec3d> vVectors = new ArrayList<>();

            double x = mc.player.lastTickPosX + (mc.player.posX - mc.player.lastTickPosX) * event.getPartialTicks()
                    - renderManager.renderPosX;
            double y = mc.player.lastTickPosY + (mc.player.posY - mc.player.lastTickPosY) * event.getPartialTicks()
                    - renderManager.renderPosY;
            double z = mc.player.lastTickPosZ + (mc.player.posZ - mc.player.lastTickPosZ) * event.getPartialTicks()
                    - renderManager.renderPosZ;
            glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
            glLineWidth(lineWidth.getValue());
            glBegin(1);
            for (int i = 0; i <= 360; i++) {
                Vec3d vec = new Vec3d(x + Math.sin(i * Math.PI / 180.0) * radius.getValue(), y + 0.1, z + Math.cos(i * Math.PI / 180.0) * radius.getValue());
                RayTraceResult result = mc.world.rayTraceBlocks(new Vec3d(x, y + 0.1, z), vec, false, true, false);
                if (result != null) {
                    hVectors.add(result.hitVec);
                } else {
                    hVectors.add(vec);
                }
            }
            for (int j = 0; j < hVectors.size() - 1; j++) {
                glVertex3d(hVectors.get(j).x, hVectors.get(j).y, hVectors.get(j).z);
                glVertex3d(hVectors.get(j + 1).x, hVectors.get(j + 1).y, hVectors.get(j + 1).z);
            }
            glEnd();

            GlStateManager.resetColor();
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            RenderUtil.GlPost();
            GlStateManager.popMatrix();
        }

        if(hitSpheres.getValue()) {
            //Rusherhack, TODO: more options who cares
            for (EntityPlayer player : mc.world.playerEntities) {
                if (player != null && (!player.equals(mc.player) || ownSphere.getValue())) {
                    Vec3d interpolated = EntityUtil.interpolateEntity(player, event.getPartialTicks());
                    if (Phobos.friendManager.isFriend(player.getName())) {
                        GL11.glColor4f(0.15F, 0.15F, 1.0F, 1.0F);
                    } else {
                        if (mc.player.getDistance(player) >= 64) {
                            GL11.glColor4f(0.0F, 1.0F, 0.0F, 1.0F);
                        } else {
                            GL11.glColor4f(1.0F, mc.player.getDistance(player) / 150, 0.0F, 1.0F);
                        }
                    }
                    RenderUtil.drawSphere(interpolated.x, interpolated.y, interpolated.z, radius.getValue().floatValue(), 20, 15);
                }
            }
        }
    }

}
