package me.earth.phobos.features.modules.render;

import me.earth.phobos.event.events.Render3DEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.RenderUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import java.awt.*;

public class BlockHighlight extends Module {

    public Setting<Boolean> box = register(new Setting("Box", false));
    public Setting<Boolean> outline = register(new Setting("Outline", true));
    private final Setting<Integer> red = register(new Setting("Red", 0, 0, 255));
    private final Setting<Integer> green = register(new Setting("Green", 255, 0, 255));
    private final Setting<Integer> blue = register(new Setting("Blue", 0, 0, 255));
    private final Setting<Integer> alpha = register(new Setting("Alpha", 255, 0, 255));
    private final Setting<Integer> boxAlpha = register(new Setting("BoxAlpha", 125, 0, 255, v -> box.getValue()));
    private final Setting<Float> lineWidth = register(new Setting("LineWidth", 1.0f, 0.1f, 5.0f, v -> outline.getValue()));
    public Setting<Boolean> customOutline = register(new Setting("CustomLine", false, v -> outline.getValue()));
    private final Setting<Integer> cRed = register(new Setting("OL-Red", 255, 0, 255, v -> customOutline.getValue() && outline.getValue()));
    private final Setting<Integer> cGreen = register(new Setting("OL-Green", 255, 0, 255, v -> customOutline.getValue() && outline.getValue()));
    private final Setting<Integer> cBlue = register(new Setting("OL-Blue", 255, 0, 255, v -> customOutline.getValue() && outline.getValue()));
    private final Setting<Integer> cAlpha = register(new Setting("OL-Alpha", 255, 0, 255, v -> customOutline.getValue() && outline.getValue()));

    public BlockHighlight() {
        super("BlockHighlight", "Highlights the block u look at.", Category.RENDER, false, false, false);
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        RayTraceResult ray = mc.objectMouseOver;
        if(ray != null && ray.typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos blockpos = ray.getBlockPos();
            RenderUtil.drawBoxESP(blockpos, new Color(red.getValue(), green.getValue(), blue.getValue(), alpha.getValue()), customOutline.getValue(), new Color(cRed.getValue(), cGreen.getValue(), cBlue.getValue(), cAlpha.getValue()), lineWidth.getValue(), outline.getValue(), box.getValue(), boxAlpha.getValue(), false);
        }
    }
}
