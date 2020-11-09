package me.earth.phobos.features.modules.client;

import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.ClientEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.TextUtil;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Managers extends Module {

    public Setting<Boolean> betterFrames = register(new Setting("BetterMaxFPS", false));

    private static Managers INSTANCE = new Managers();
    public Setting<String> commandBracket = register(new Setting("Bracket", "<"));
    public Setting<String> commandBracket2 = register(new Setting("Bracket2", ">"));
    public Setting<String> command = register(new Setting("Command", "Phobos.eu"));
    public Setting<TextUtil.Color> bracketColor = register(new Setting("BColor", TextUtil.Color.BLUE));
    public Setting<TextUtil.Color> commandColor = register(new Setting("CColor", TextUtil.Color.BLUE));
    public Setting<Integer> betterFPS = register(new Setting("MaxFPS", 300, 30, 1000, v -> betterFrames.getValue()));
    public Setting<Boolean> potions = register(new Setting("Potions", true));
    public Setting<Integer> textRadarUpdates = register(new Setting("TRUpdates", 500, 0, 1000));
    public Setting<Integer> respondTime = register(new Setting("SeverTime", 500, 0, 1000));
    public Setting<Integer> moduleListUpdates = register(new Setting("ALUpdates", 1000, 0, 1000));
    public Setting<Float> holeRange = register(new Setting("HoleRange", 6.0f, 1.0f, 32.0f));
    public Setting<Boolean> speed = register(new Setting("Speed", true));
    public Setting<Boolean> tRadarInv = register(new Setting("TRadarInv", true));
    public Setting<Boolean> unfocusedCpu = register(new Setting("UnfocusedCPU", false));
    public Setting<Integer> cpuFPS = register(new Setting("UnfocusedFPS", 60, 1, 60, v -> unfocusedCpu.getValue()));

    public Managers() {
        super("Management", "ClientManagement", Category.CLIENT, false, false, true);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static Managers getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Managers();
        }
        return INSTANCE;
    }

    @Override
    public void onLoad() {
        Phobos.commandManager.setClientMessage(getCommandMessage());
    }

    @SubscribeEvent
    public void onSettingChange(ClientEvent event) {
        if(event.getStage() == 2) {
            if(this.equals(event.getSetting().getFeature())) {
                Phobos.commandManager.setClientMessage(getCommandMessage());
            }
        }
    }

    public String getCommandMessage() {
        return TextUtil.coloredString(commandBracket.getPlannedValue(), bracketColor.getPlannedValue()) + TextUtil.coloredString(command.getPlannedValue(), commandColor.getPlannedValue()) + TextUtil.coloredString(commandBracket2.getPlannedValue(), bracketColor.getPlannedValue());
    }
}
