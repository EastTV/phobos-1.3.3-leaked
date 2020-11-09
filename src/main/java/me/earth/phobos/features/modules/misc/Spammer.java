package me.earth.phobos.features.modules.misc;

import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.FileUtil;
import me.earth.phobos.util.TextUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.util.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Spammer extends Module {

    public Setting<Mode> mode = register(new Setting("Mode", Mode.PWORD));
    public Setting<PwordMode> type = register(new Setting("Pword", PwordMode.CHAT, v -> mode.getValue() == Mode.PWORD));
    public Setting<DelayType> delayType = register(new Setting("DelayType", DelayType.S));
    public Setting<Integer> delay = register(new Setting("DelayS", 10, 1, 20, v -> delayType.getValue() == DelayType.S));
    public Setting<Integer> delayDS = register(new Setting("DelayDS", 10, 1, 500, v -> delayType.getValue() == DelayType.DS));
    public Setting<Integer> delayMS = register(new Setting("DelayDS", 10, 1, 1000, v -> delayType.getValue() == DelayType.MS));
    public Setting<String> msgTarget = register(new Setting("MsgTarget", "Target...", v -> mode.getValue() == Mode.PWORD && type.getValue() == PwordMode.MSG));
    public Setting<Boolean> greentext = register(new Setting("Greentext", false, v -> mode.getValue() == Mode.FILE));
    public Setting<Boolean> random = register(new Setting("Random", false, v -> mode.getValue() == Mode.FILE));
    public Setting<Boolean> loadFile = register(new Setting("LoadFile", false, v -> mode.getValue() == Mode.FILE));

    private final Timer timer = new Timer();
    private final List<String> sendPlayers = new ArrayList<>();
    private static final String fileName = "phobos/util/Spammer.txt";
    private static final String defaultMessage = "gg";
    private static final List<String> spamMessages = new ArrayList();
    private static final Random rnd = new Random();

    public Spammer() {
        super("Spammer", "Spams stuff.", Category.MISC, true, false, false);
    }

    @Override
    public void onLoad() {
        readSpamFile();
        this.disable();
    }

    @Override
    public void onEnable() {
        if(fullNullCheck()) {
            this.disable();
            return;
        }
        readSpamFile();
    }

    @Override
    public void onLogin() {
        this.disable();
    }

    @Override
    public void onLogout() {
        this.disable();
    }

    @Override
    public void onDisable() {
        spamMessages.clear();
        timer.reset();
    }

    @Override
    public void onUpdate() {
        if(fullNullCheck()) {
            this.disable();
            return;
        }

        if(loadFile.getValue()) {
            readSpamFile();
            loadFile.setValue(false);
        }

        switch (delayType.getValue()) {
            case MS:
                if (!timer.passedMs(delayMS.getValue())) {
                    return;
                }
                break;
            case S:
                if (!timer.passedS(delay.getValue())) {
                    return;
                }
                break;
            case DS:
                if (!timer.passedDs(delayDS.getValue())) {
                    return;
                }
                break;
            default:
        }

        if(mode.getValue() == Mode.PWORD) {
            String msg = TextUtil.pword;
            switch(type.getValue()) {
                case MSG:
                    msg = "/msg " + msgTarget.getValue() + msg;
                    break;
                case EVERYONE:
                    String target = null;
                    if(mc.getConnection() != null && mc.getConnection().getPlayerInfoMap() != null) {
                        for(NetworkPlayerInfo info : mc.getConnection().getPlayerInfoMap()) {
                            if(info != null && info.getDisplayName() != null) {
                                try {
                                    String str = info.getDisplayName().getFormattedText();
                                    String name = StringUtils.stripControlCodes(str);
                                    if (name.equals(mc.player.getName()) || sendPlayers.contains(name)) {
                                        continue;
                                    }
                                    target = name;
                                    this.sendPlayers.add(name);
                                    break;
                                } catch(Exception ignored) {}
                            }
                        }

                        if (target == null) {
                            sendPlayers.clear();
                            return;
                        } else {
                            msg = "/msg " + target + msg;
                        }
                    } else {
                        return;
                    }
                    break;
                default:
            }
            mc.player.sendChatMessage(msg);
        } else {
            if (spamMessages.size() > 0) {
                String messageOut;
                if (random.getValue()) {
                    int index = rnd.nextInt(spamMessages.size());
                    messageOut = spamMessages.get(index);
                    spamMessages.remove(index);
                } else {
                    messageOut = spamMessages.get(0);
                    spamMessages.remove(0);
                }
                spamMessages.add(messageOut);
                if (greentext.getValue()) {
                    messageOut = "> " + messageOut;
                }
                mc.player.connection.sendPacket(new CPacketChatMessage(messageOut.replaceAll(TextUtil.SECTIONSIGN, "")));
            }
        }
        timer.reset();
    }

    private void readSpamFile() {
        List<String> fileInput = FileUtil.readTextFileAllLines(fileName);
        Iterator<String> i = fileInput.iterator();
        spamMessages.clear();
        while (i.hasNext()) {
            String s = i.next();
            if (!s.replaceAll("\\s", "").isEmpty()) {
                spamMessages.add(s);
            }
        }
        if (spamMessages.size() == 0) {
            spamMessages.add(defaultMessage);
        }
    }

    public enum Mode {
        FILE,
        PWORD
    }

    public enum PwordMode {
        MSG,
        EVERYONE,
        CHAT
    }

    public enum DelayType {
        MS,
        DS,
        S
    }
}
