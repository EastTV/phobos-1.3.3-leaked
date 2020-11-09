package me.earth.phobos.features.modules.player;

import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;

public class TpsSync extends Module {

    public Setting<Boolean> mining = register(new Setting("Mining", true));
    public Setting<Boolean> attack = register(new Setting("Attack", false));
    //public Setting<Boolean> eating = register(new Setting("Eating", false));

    private static TpsSync INSTANCE = new TpsSync();
    //private final Timer timer = new Timer();

    public TpsSync() {
        super("TpsSync", "Syncs your client with the TPS.", Category.PLAYER, true, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static TpsSync getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new TpsSync();
        }
        return INSTANCE;
    }

    /*@SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if(eating.getValue()) {
            Command.sendMessage(event.getPacket().getClass().getName());
        }
    }*/
}
