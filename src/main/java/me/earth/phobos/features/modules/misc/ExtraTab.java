package me.earth.phobos.features.modules.misc;

import me.earth.phobos.Phobos;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.TextUtil;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;

public class ExtraTab extends Module {

    public Setting<Integer> size = register(new Setting("Size", 250, 1, 1000));

    private static ExtraTab INSTANCE = new ExtraTab();

    public ExtraTab() {
        super("ExtraTab", "Extends Tab.", Category.MISC, false, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static String getPlayerName(NetworkPlayerInfo networkPlayerInfoIn) {
        String name = networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());
        if (Phobos.friendManager.isFriend(name)) {
            return TextUtil.AQUA + name;
        }
        //if (Enemies.isEnemy(name)) return String.format("%sc%s", Command.SECTIONSIGN(), name);
        return name;
    }

    public static ExtraTab getINSTANCE() {
        if(INSTANCE == null) {
            INSTANCE = new ExtraTab();
        }
        return INSTANCE;
    }
}
