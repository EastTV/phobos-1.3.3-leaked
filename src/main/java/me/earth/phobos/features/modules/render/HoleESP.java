package me.earth.phobos.features.modules.render;

import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.Render3DEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.BlockUtil;
import me.earth.phobos.util.RenderUtil;
import net.minecraft.util.math.BlockPos;

import java.awt.*;

public class HoleESP extends Module {

    private Setting<Integer> holes = register(new Setting("Holes", 3, 1, 50));
    public Setting<Boolean> box = register(new Setting("Box", true));
    public Setting<Boolean> outline = register(new Setting("Outline", true));
    private Setting<Integer> red = register(new Setting("Red", 0, 0, 255));
    private Setting<Integer> green = register(new Setting("Green", 255, 0, 255));
    private Setting<Integer> blue = register(new Setting("Blue", 0, 0, 255));
    private Setting<Integer> alpha = register(new Setting("Alpha", 255, 0, 255));
    private Setting<Integer> boxAlpha = register(new Setting("BoxAlpha", 125, 0, 255, v -> box.getValue()));
    private Setting<Float> lineWidth = register(new Setting("LineWidth", 1.0f, 0.1f, 5.0f, v -> outline.getValue()));
    public Setting<Boolean> safeColor = register(new Setting("SafeColor", false));
    private Setting<Integer> safeRed = register(new Setting("SafeRed", 0, 0, 255, v -> safeColor.getValue()));
    private Setting<Integer> safeGreen = register(new Setting("SafeGreen", 255, 0, 255, v -> safeColor.getValue()));
    private Setting<Integer> safeBlue = register(new Setting("SafeBlue", 0, 0, 255, v -> safeColor.getValue()));
    private Setting<Integer> safeAlpha = register(new Setting("SafeAlpha", 255, 0, 255, v -> safeColor.getValue()));
    public Setting<Boolean> customOutline = register(new Setting("CustomLine", false, v -> outline.getValue()));
    private Setting<Integer> cRed = register(new Setting("OL-Red", 0, 0, 255, v -> customOutline.getValue() && outline.getValue()));
    private Setting<Integer> cGreen = register(new Setting("OL-Green", 0, 0, 255, v -> customOutline.getValue() && outline.getValue()));
    private Setting<Integer> cBlue = register(new Setting("OL-Blue", 255, 0, 255, v -> customOutline.getValue() && outline.getValue()));
    private Setting<Integer> cAlpha = register(new Setting("OL-Alpha", 255, 0, 255, v -> customOutline.getValue() && outline.getValue()));
    private Setting<Integer> safecRed = register(new Setting("OL-SafeRed", 0, 0, 255, v -> customOutline.getValue() && outline.getValue() && safeColor.getValue()));
    private Setting<Integer> safecGreen = register(new Setting("OL-SafeGreen", 255, 0, 255, v -> customOutline.getValue() && outline.getValue() && safeColor.getValue()));
    private Setting<Integer> safecBlue = register(new Setting("OL-SafeBlue", 0, 0, 255, v -> customOutline.getValue() && outline.getValue() && safeColor.getValue()));
    private Setting<Integer> safecAlpha = register(new Setting("OL-SafeAlpha", 255, 0, 255, v -> customOutline.getValue() && outline.getValue() && safeColor.getValue()));

    private static HoleESP INSTANCE = new HoleESP();

    public HoleESP() {
        super("HoleESP", "Shows safe spots.", Category.RENDER, false, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static HoleESP getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new HoleESP();
        }
        return INSTANCE;
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        int drawnHoles = 0;
        for(BlockPos pos : Phobos.holeManager.getSortedHoles()) {
            if(drawnHoles >= holes.getValue()) {
                break;
            }

            if(pos.equals(new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ))) {
                continue;
            }

            if(BlockUtil.isPosInFov(pos)) {
                if(safeColor.getValue() && Phobos.holeManager.isSafe(pos)) {
                    RenderUtil.drawBoxESP(pos, new Color(safeRed.getValue(), safeGreen.getValue(), safeBlue.getValue(), safeAlpha.getValue()), customOutline.getValue(), new Color(safecRed.getValue(), safecGreen.getValue(), safecBlue.getValue(), safecAlpha.getValue()), lineWidth.getValue(), outline.getValue(), box.getValue(), boxAlpha.getValue(), true);
                } else {
                    RenderUtil.drawBoxESP(pos, new Color(red.getValue(), green.getValue(), blue.getValue(), alpha.getValue()), customOutline.getValue(), new Color(cRed.getValue(), cGreen.getValue(), cBlue.getValue(), cAlpha.getValue()), lineWidth.getValue(), outline.getValue(), box.getValue(), boxAlpha.getValue(), true);
                }
                drawnHoles++;
            }
        }
    }
}
