package me.earth.phobos.features.modules.misc;

import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.TextUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatModifier extends Module {

    public Setting<Suffix> suffix = register(new Setting("Suffix", Suffix.NONE, "Your Suffix."));
    public Setting<Boolean> clean = register(new Setting("CleanChat", false, "Cleans your chat"));
    public Setting<Boolean> infinite = register(new Setting("Infinite", false, "Makes your chat infinite."));
    public Setting<Boolean> autoQMain = register(new Setting("AutoQMain", false, "Spams AutoQMain"));
    public Setting<Boolean> qNotification = register(new Setting("QNotification", false, v -> autoQMain.getValue()));
    public Setting<Integer> qDelay = register(new Setting("QDelay", 9, 1, 90, v -> autoQMain.getValue()));
    public Setting<TextUtil.Color> timeStamps = register(new Setting("Time", TextUtil.Color.NONE));
    public Setting<TextUtil.Color> bracket = register(new Setting("Bracket", TextUtil.Color.WHITE, v -> timeStamps.getValue() != TextUtil.Color.NONE));
    public Setting<Boolean> space = register(new Setting("Space", true, v -> timeStamps.getValue() != TextUtil.Color.NONE));
    public Setting<Boolean> all = register(new Setting("All", false, v -> timeStamps.getValue() != TextUtil.Color.NONE));
    public Setting<Boolean> shrug = register(new Setting("Shrug", false));
    public Setting<Boolean> disability = register(new Setting("Disability", false));

    private final Timer timer = new Timer();
    private static ChatModifier INSTANCE = new ChatModifier();

    public ChatModifier() {
        super("Chat", "Modifies your chat", Module.Category.MISC, true, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static ChatModifier getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new ChatModifier();
        }
        return INSTANCE;
    }

    @Override
    public void onUpdate() {
        if(shrug.getValue()) {
            mc.player.sendChatMessage(TextUtil.shrug);
            shrug.setValue(false);
        }

        if(autoQMain.getValue()) {
            if (!shouldSendMessage(mc.player)) {
                return;
            }
            if (qNotification.getValue()) {
                Command.sendMessage("<AutoQueueMain> Sending message: /queue main");
            }
            mc.player.sendChatMessage("/queue main");
            timer.reset();
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if(event.getStage() == 0 && event.getPacket() instanceof CPacketChatMessage) {
            CPacketChatMessage packet = event.getPacket();
            String s = packet.getMessage();
            if (s.startsWith("/")) return;
            switch(suffix.getValue()) {
                case EARTH:
                    s += Phobos.CHAT_SUFFIX;
                    break;
                case PHOBOS:
                    s += Phobos.PHOBOS_SUFFIX;
                    break;
                default:
            }
            if (s.length() >= 256) s = s.substring(0,256);
            packet.message = s;
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if(event.getStage() == 0 && timeStamps.getValue() != TextUtil.Color.NONE && event.getPacket() instanceof SPacketChat) {
            if(!((SPacketChat)event.getPacket()).isSystem()) {
                return;
            }

            String originalMessage = ((SPacketChat)event.getPacket()).chatComponent.getUnformattedText();
            String message = getTimeString() + originalMessage;
            ((SPacketChat)event.getPacket()).chatComponent = new TextComponentString(message);
        }
    }

    public String getTimeString() {
        String date = new SimpleDateFormat("k:mm").format(new Date());
        return (bracket.getValue() == TextUtil.Color.NONE ? "" : TextUtil.coloredString("<", bracket.getValue()))
                + TextUtil.coloredString(date, timeStamps.getValue())
                + (bracket.getValue() == TextUtil.Color.NONE ? "" : TextUtil.coloredString(">", bracket.getValue()))
                + (space.getValue() ? " " : "")
                + TextUtil.RESET;
    }

    private boolean shouldSendMessage(EntityPlayer player) {

        if (player.dimension != 1) {
            return false;
        }

        if (!timer.passedS(qDelay.getValue())) {
            return false;
        }

        return player.getPosition().equals(new Vec3i(0, 240, 0));

    }

    public enum Suffix {
        NONE,
        PHOBOS,
        EARTH
    }
}
