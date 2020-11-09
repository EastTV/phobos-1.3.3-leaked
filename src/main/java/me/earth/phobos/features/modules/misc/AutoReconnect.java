package me.earth.phobos.features.modules.misc;

import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.MathUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoReconnect extends Module {

    private static ServerData serverData;
    private static AutoReconnect INSTANCE = new AutoReconnect();

    private final Setting<Integer> delay = register(new Setting("Delay", 5));

    public AutoReconnect() {
        super("AutoReconnect", "Reconnects you if you disconnect.", Category.MISC, true, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static AutoReconnect getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new AutoReconnect();
        }
        return INSTANCE;
    }

    @SubscribeEvent
    public void sendPacket(GuiOpenEvent event) {
        if(event.getGui() instanceof GuiDisconnected) {
            updateLastConnectedServer();
            if(AutoLog.getInstance().isOff()) {
                GuiDisconnected disconnected = (GuiDisconnected)event.getGui();
                event.setGui(new GuiDisconnectedHook(disconnected));
            }
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        updateLastConnectedServer();
    }

    public void updateLastConnectedServer() {
        ServerData data = mc.getCurrentServerData();
        if (data != null) {
            serverData = data;
        }
    }

    private class GuiDisconnectedHook extends GuiDisconnected {

        private final Timer timer = new Timer();

        public GuiDisconnectedHook(GuiDisconnected disconnected) {
            super(disconnected.parentScreen, disconnected.reason, disconnected.message);
            timer.reset();
        }

        @Override
        public void updateScreen() {
            if (timer.passedS(delay.getValue())) {
                mc.displayGuiScreen(new GuiConnecting(parentScreen, mc, serverData == null ? mc.currentServerData : serverData));
            }
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            super.drawScreen(mouseX, mouseY, partialTicks);
            String s = "Reconnecting in " + (MathUtil.round((delay.getValue() * 1000 - timer.getPassedTimeMs()) / 1000.0, 1));
            renderer.drawString(s, width / 2 - renderer.getStringWidth(s) / 2, height - 16, 0xffffff, true);
        }
    }
}
