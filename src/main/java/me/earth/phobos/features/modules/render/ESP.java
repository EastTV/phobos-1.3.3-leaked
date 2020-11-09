package me.earth.phobos.features.modules.render;

import me.earth.phobos.event.events.Render3DEvent;
import me.earth.phobos.event.events.RenderEntityModelEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.EntityUtil;
import me.earth.phobos.util.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ESP extends Module {

    private final Setting<Mode> mode = register(new Setting("Mode", Mode.OUTLINE));
    private final Setting<Boolean> players = register(new Setting("Players", true));
    private final Setting<Boolean> animals = register(new Setting("Animals", false));
    private final Setting<Boolean> mobs = register(new Setting("Mobs", false));
    private final Setting<Boolean> items = register(new Setting("Items", false));
    private final Setting<Boolean> xporbs = register(new Setting("XpOrbs", false));
    private final Setting<Boolean> xpbottles = register(new Setting("XpBottles", false));
    private final Setting<Boolean> pearl = register(new Setting("Pearls", false));
    private final Setting<Integer> red = register(new Setting("Red", 255, 0, 255));
    private final Setting<Integer> green = register(new Setting("Green", 255, 0, 255));
    private final Setting<Integer> blue = register(new Setting("Blue", 255, 0, 255));
    private final Setting<Integer> boxAlpha = register(new Setting("BoxAlpha", 120, 0, 255));
    private final Setting<Integer> alpha = register(new Setting("Alpha", 255, 0, 255));
    private final Setting<Float> lineWidth = register(new Setting("LineWidth", 2.0f, 0.1f, 5.0f));
    private final Setting<Boolean> colorFriends = register(new Setting("Friends", true));
    private final Setting<Boolean> self = register(new Setting("Self", true));
    private final Setting<Boolean> onTop = register(new Setting("onTop", true));
    private final Setting<Boolean> invisibles = register(new Setting("Invisibles", false));

    private static ESP INSTANCE = new ESP();

    public ESP() {
        super("ESP", "Renders a nice ESP.", Category.RENDER, false, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static ESP getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new ESP();
        }
        return INSTANCE;
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if(items.getValue()) {
            int i = 0;
            for (Entity entity : mc.world.loadedEntityList) {
                if (((entity instanceof EntityItem)) && mc.player.getDistanceSq(entity) < 2500) {
                    Vec3d interp = EntityUtil.getInterpolatedRenderPos(entity, mc.getRenderPartialTicks());
                    AxisAlignedBB bb = new AxisAlignedBB(entity.getEntityBoundingBox().minX - 0.05D - entity.posX + interp.x, entity.getEntityBoundingBox().minY - 0.0D - entity.posY + interp.y, entity.getEntityBoundingBox().minZ - 0.05D - entity.posZ + interp.z, entity.getEntityBoundingBox().maxX + 0.05D - entity.posX + interp.x, entity.getEntityBoundingBox().maxY + 0.1D - entity.posY + interp.y, entity.getEntityBoundingBox().maxZ + 0.05D - entity.posZ + interp.z);
                    GlStateManager.pushMatrix();
                    GlStateManager.enableBlend();
                    GlStateManager.disableDepth();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
                    GlStateManager.disableTexture2D();
                    GlStateManager.depthMask(false);
                    GL11.glEnable(2848);
                    GL11.glHint(3154, 4354);
                    GL11.glLineWidth(1.0f);
                    RenderGlobal.renderFilledBox(bb, red.getValue() / 255.0f, green.getValue() / 255.0f, blue.getValue() / 255.0f, boxAlpha.getValue() / 255.0f);
                    GL11.glDisable(2848);
                    GlStateManager.depthMask(true);
                    GlStateManager.enableDepth();
                    GlStateManager.enableTexture2D();
                    GlStateManager.disableBlend();
                    GlStateManager.popMatrix();
                    RenderUtil.drawBlockOutline(bb, new Color(red.getValue(), green.getValue(), blue.getValue(), alpha.getValue()), 1.0f);
                    i++;
                    if (i >= 50) {
                        break;
                    }
                }
            }
        }

        if(xporbs.getValue()) {
            int i = 0;
            for (Entity entity : mc.world.loadedEntityList) {
                if (((entity instanceof EntityXPOrb)) && mc.player.getDistanceSq(entity) < 2500) {
                    Vec3d interp = EntityUtil.getInterpolatedRenderPos(entity, mc.getRenderPartialTicks());
                    AxisAlignedBB bb = new AxisAlignedBB(entity.getEntityBoundingBox().minX - 0.05D - entity.posX + interp.x, entity.getEntityBoundingBox().minY - 0.0D - entity.posY + interp.y, entity.getEntityBoundingBox().minZ - 0.05D - entity.posZ + interp.z, entity.getEntityBoundingBox().maxX + 0.05D - entity.posX + interp.x, entity.getEntityBoundingBox().maxY + 0.1D - entity.posY + interp.y, entity.getEntityBoundingBox().maxZ + 0.05D - entity.posZ + interp.z);
                    GlStateManager.pushMatrix();
                    GlStateManager.enableBlend();
                    GlStateManager.disableDepth();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
                    GlStateManager.disableTexture2D();
                    GlStateManager.depthMask(false);
                    GL11.glEnable(2848);
                    GL11.glHint(3154, 4354);
                    GL11.glLineWidth(1.0f);
                    RenderGlobal.renderFilledBox(bb, red.getValue() / 255.0f, green.getValue() / 255.0f, blue.getValue() / 255.0f, boxAlpha.getValue() / 255.0f);
                    GL11.glDisable(2848);
                    GlStateManager.depthMask(true);
                    GlStateManager.enableDepth();
                    GlStateManager.enableTexture2D();
                    GlStateManager.disableBlend();
                    GlStateManager.popMatrix();
                    RenderUtil.drawBlockOutline(bb, new Color(red.getValue(), green.getValue(), blue.getValue(), alpha.getValue()), 1.0f);
                    i++;
                    if (i >= 50) {
                        break;
                    }
                }
            }
        }
        if(pearl.getValue()) {
            int i = 0;
            for (Entity entity : mc.world.loadedEntityList) {
                if (((entity instanceof EntityEnderPearl))  && mc.player.getDistanceSq(entity) < 2500) {
                    Vec3d interp = EntityUtil.getInterpolatedRenderPos(entity, mc.getRenderPartialTicks());
                    AxisAlignedBB bb = new AxisAlignedBB(entity.getEntityBoundingBox().minX - 0.05D - entity.posX + interp.x, entity.getEntityBoundingBox().minY - 0.0D - entity.posY + interp.y, entity.getEntityBoundingBox().minZ - 0.05D - entity.posZ + interp.z, entity.getEntityBoundingBox().maxX + 0.05D - entity.posX + interp.x, entity.getEntityBoundingBox().maxY + 0.1D - entity.posY + interp.y, entity.getEntityBoundingBox().maxZ + 0.05D - entity.posZ + interp.z);
                    GlStateManager.pushMatrix();
                    GlStateManager.enableBlend();
                    GlStateManager.disableDepth();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
                    GlStateManager.disableTexture2D();
                    GlStateManager.depthMask(false);
                    GL11.glEnable(2848);
                    GL11.glHint(3154, 4354);
                    GL11.glLineWidth(1.0f);
                    RenderGlobal.renderFilledBox(bb, red.getValue() / 255.0f, green.getValue() / 255.0f, blue.getValue() / 255.0f, boxAlpha.getValue() / 255.0f);
                    GL11.glDisable(2848);
                    GlStateManager.depthMask(true);
                    GlStateManager.enableDepth();
                    GlStateManager.enableTexture2D();
                    GlStateManager.disableBlend();
                    GlStateManager.popMatrix();
                    RenderUtil.drawBlockOutline(bb, new Color(red.getValue(), green.getValue(), blue.getValue(), alpha.getValue()), 1.0f);
                    i++;
                    if (i >= 50) {
                        break;
                    }
                }
            }
        }

        if(xpbottles.getValue()) {
            int i = 0;
            for (Entity entity : mc.world.loadedEntityList) {
                if (((entity instanceof EntityExpBottle))  && mc.player.getDistanceSq(entity) < 2500) {
                    Vec3d interp = EntityUtil.getInterpolatedRenderPos(entity, mc.getRenderPartialTicks());
                    AxisAlignedBB bb = new AxisAlignedBB(entity.getEntityBoundingBox().minX - 0.05D - entity.posX + interp.x, entity.getEntityBoundingBox().minY - 0.0D - entity.posY + interp.y, entity.getEntityBoundingBox().minZ - 0.05D - entity.posZ + interp.z, entity.getEntityBoundingBox().maxX + 0.05D - entity.posX + interp.x, entity.getEntityBoundingBox().maxY + 0.1D - entity.posY + interp.y, entity.getEntityBoundingBox().maxZ + 0.05D - entity.posZ + interp.z);
                    GlStateManager.pushMatrix();
                    GlStateManager.enableBlend();
                    GlStateManager.disableDepth();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
                    GlStateManager.disableTexture2D();
                    GlStateManager.depthMask(false);
                    GL11.glEnable(2848);
                    GL11.glHint(3154, 4354);
                    GL11.glLineWidth(1.0f);
                    RenderGlobal.renderFilledBox(bb, red.getValue() / 255.0f, green.getValue() / 255.0f, blue.getValue() / 255.0f, boxAlpha.getValue() / 255.0f);
                    GL11.glDisable(2848);
                    GlStateManager.depthMask(true);
                    GlStateManager.enableDepth();
                    GlStateManager.enableTexture2D();
                    GlStateManager.disableBlend();
                    GlStateManager.popMatrix();
                    RenderUtil.drawBlockOutline(bb, new Color(red.getValue(), green.getValue(), blue.getValue(), alpha.getValue()), 1.0f);
                    i++;
                    if (i >= 50) {
                        break;
                    }
                }
            }
        }
    }

    public void onRenderModel(RenderEntityModelEvent event) {
        if(event.getStage() != 0 || event.entity == null || (event.entity.isInvisible() && !invisibles.getValue()) || (!self.getValue() && event.entity.equals(mc.player)) || (!players.getValue() && event.entity instanceof EntityPlayer) || (!animals.getValue() && EntityUtil.isPassive(event.entity)) || (!mobs.getValue() && !EntityUtil.isPassive(event.entity) && !(event.entity instanceof EntityPlayer))) {
            return;
        }

        Color color = EntityUtil.getColor(event.entity, red.getValue(), green.getValue(), blue.getValue(), alpha.getValue(), colorFriends.getValue());
        boolean fancyGraphics = mc.gameSettings.fancyGraphics;
        mc.gameSettings.fancyGraphics = false;
        float gamma = mc.gameSettings.gammaSetting;
        mc.gameSettings.gammaSetting = 10000f;

        if (onTop.getValue()) {
            event.modelBase.render(
                    event.entity, event.limbSwing, event.limbSwingAmount, event.age, event.headYaw, event.headPitch, event.scale
            );
        }

        if (mode.getValue() == Mode.OUTLINE) {
            RenderUtil.renderOne(lineWidth.getValue());
            event.modelBase.render(
                    event.entity, event.limbSwing, event.limbSwingAmount, event.age, event.headYaw, event.headPitch, event.scale
            );
            GlStateManager.glLineWidth(lineWidth.getValue());
            RenderUtil.renderTwo();
            event.modelBase.render(
                    event.entity, event.limbSwing, event.limbSwingAmount, event.age, event.headYaw, event.headPitch, event.scale
            );
            GlStateManager.glLineWidth(lineWidth.getValue());
            RenderUtil.renderThree();
            RenderUtil.renderFour(color);
            event.modelBase.render(
                    event.entity, event.limbSwing, event.limbSwingAmount, event.age, event.headYaw, event.headPitch, event.scale
            );
            GlStateManager.glLineWidth(lineWidth.getValue());
            RenderUtil.renderFive();
        } else {
            GL11.glPushMatrix();
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            if (mode.getValue() == Mode.WIREFRAME) {
                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
            } else {
                GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_LINE);
            }

            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);

            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glEnable(GL11.GL_BLEND);

            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
            GlStateManager.glLineWidth(lineWidth.getValue());

            event.modelBase.render(
                    event.entity, event.limbSwing, event.limbSwingAmount, event.age, event.headYaw, event.headPitch, event.scale
            );

            GL11.glPopAttrib();
            GL11.glPopMatrix();
        }

        if (!onTop.getValue()) {
            event.modelBase.render(event.entity, event.limbSwing, event.limbSwingAmount, event.age, event.headYaw, event.headPitch, event.scale);
        }

        try {
            mc.gameSettings.fancyGraphics = fancyGraphics;
            mc.gameSettings.gammaSetting = gamma;
        } catch (Exception ignored) {}

        event.setCanceled(true);
    }

    public enum Mode {
        WIREFRAME,
        OUTLINE
    }
}
