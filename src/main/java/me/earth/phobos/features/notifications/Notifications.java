package me.earth.phobos.features.notifications;

import me.earth.phobos.Phobos;
import me.earth.phobos.features.modules.client.HUD;
import me.earth.phobos.util.RenderUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public class Notifications {
    private final String text;
    private final long disableTime;
    private final float width;
    private final Timer timer = new Timer();
    public Notifications(String text, long disableTime) {
        this.text = text;
        this.disableTime = disableTime;
        this.width = Phobos.moduleManager.getModuleByClass(HUD.class).renderer.getStringWidth(text);
        timer.reset();
    }

    public void onDraw(int y) {
        final ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        if (timer.passedMs(disableTime)) Phobos.notificationManager.getNotifications().remove(this);
        RenderUtil.drawRect(scaledResolution.getScaledWidth() - 4 - width,y,scaledResolution.getScaledWidth() - 2,y + Phobos.moduleManager.getModuleByClass(HUD.class).renderer.getFontHeight() + 3,0x75000000);
        Phobos.moduleManager.getModuleByClass(HUD.class).renderer.drawString(text,scaledResolution.getScaledWidth() - width - 3,y + 2,-1,true);
    }
}
