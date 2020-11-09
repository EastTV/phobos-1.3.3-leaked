package me.earth.phobos.features.modules.misc;

import me.earth.phobos.Phobos;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.util.text.TextComponentString;

public class AutoLog extends Module {

    private Setting<Float> health = register(new Setting("Health", 16.0f, 0.1f, 36.0f));
    private Setting<Boolean> logout = register(new Setting("LogoutOff", true));

    private static AutoLog INSTANCE = new AutoLog();

    public AutoLog() {
        super("AutoLog", "Logs when in danger.", Category.MISC, false, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static AutoLog getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new AutoLog();
        }
        return INSTANCE;
    }

    @Override
    public void onTick() {
        if(!nullCheck()) {
            if (mc.player.getHealth() <= health.getValue()) {
                Phobos.moduleManager.disableModule("AutoReconnect");
                mc.player.connection.sendPacket(new SPacketDisconnect(new TextComponentString("AutoLogged")));
                if(logout.getValue()) {
                    this.disable();
                }
            }
        }
    }
}
