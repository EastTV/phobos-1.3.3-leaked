package me.earth.phobos.features.modules.client;

import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.ClientEvent;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.manager.FileManager;
import me.earth.phobos.util.TextUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Notifications extends Module {

    public Setting<Boolean> totemPops = register(new Setting("TotemPops", false));
    public Setting<Boolean> totemNoti = register(new Setting("TotemNoti", true,v->totemPops.getValue()));
    public Setting<Integer> delay = register(new Setting("Delay", 2000, 0, 5000, v -> totemPops.getValue(), "Delays messages."));
    public Setting<Boolean> clearOnLogout = register(new Setting("LogoutClear", false));
    public Setting<Boolean> moduleMessage = register(new Setting("ModuleMessage", false));
    public Setting<Boolean> list = register(new Setting("List", false, v -> moduleMessage.getValue()));
    private Setting<Boolean> readfile = register(new Setting("LoadFile", false, v -> moduleMessage.getValue()));
    public Setting<Boolean> watermark = register(new Setting("Watermark", true, v -> moduleMessage.getValue()));
    public Setting<Boolean> visualRange = register(new Setting("VisualRange", false));
    public Setting<Boolean> coords = register(new Setting("Coords", true, v -> visualRange.getValue()));
    public Setting<Boolean> leaving = register(new Setting("Leaving", false, v -> visualRange.getValue()));
    public Setting<Boolean> crash = register(new Setting("Crash", false));
    private List<EntityPlayer> knownPlayers = new ArrayList<>();
    private static List<String> modules = new ArrayList();
    private static final String fileName = "phobos/util/ModuleMessage_List.txt";
    private final Timer timer = new Timer();
    public Timer totemAnnounce = new Timer();
    private boolean check;
    private static Notifications INSTANCE = new Notifications();

    public Notifications() {
        super("Notifications", "Sends Messages.", Category.CLIENT, true, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Override
    public void onLoad() {
        check = true;
        loadFile();
        check = false;
    }

    @Override
    public void onEnable() {
        this.knownPlayers = new ArrayList<>();
        if (!check) {
            loadFile();
        }
    }

    @Override
    public void onUpdate() {
        if (readfile.getValue()) {
            if (!check) {
                Command.sendMessage("Loading File...");
                timer.reset();
                loadFile();
            }
            check = true;
        }

        if (check && timer.passedMs(750)) {
            readfile.setValue(false);
            check = false;
        }

        if (visualRange.getValue()) {
            List<EntityPlayer> tickPlayerList = new ArrayList<>(mc.world.playerEntities);
            if (tickPlayerList.size() > 0) {
                for (final EntityPlayer player : tickPlayerList) {
                    if (player.getName().equals(mc.player.getName())) {
                        continue;
                    }
                    if (!knownPlayers.contains(player)) {
                        knownPlayers.add(player);
                        if (Phobos.friendManager.isFriend(player)) {
                            Command.sendMessage("Player " + TextUtil.GREEN + player.getName() + TextUtil.RESET + " entered your visual range" + (coords.getValue() ? " at (" + (int) player.posX + ", " + (int) player.posY + ", " + (int) player.posZ + ")!" : "!"),true);
                        } else {
                            Command.sendMessage("Player " + TextUtil.RED + player.getName() + TextUtil.RESET + " entered your visual range" + (coords.getValue() ? " at (" + (int) player.posX + ", " + (int) player.posY + ", " + (int) player.posZ + ")!" : "!"),true);
                        }
                        return;
                    }
                }
            }

            if (knownPlayers.size() > 0) {
                for (EntityPlayer player : knownPlayers) {
                    if (!tickPlayerList.contains(player)) {
                        knownPlayers.remove(player);
                        if (leaving.getValue()) {
                            if (Phobos.friendManager.isFriend(player)) {
                                Command.sendMessage("Player " + TextUtil.GREEN + player.getName() + TextUtil.RESET + " left your visual range" + (coords.getValue() ? " at (" + (int) player.posX + ", " + (int) player.posY + ", " + (int) player.posZ + ")!" : "!"),true);
                            } else {
                                Command.sendMessage("Player " + TextUtil.RED + player.getName() + TextUtil.RESET + " left your visual range" + (coords.getValue() ? " at (" + (int) player.posX + ", " + (int) player.posY + ", " + (int) player.posZ + ")!" : "!"),true);
                            }
                        }
                        return;
                    }
                }
            }
        }
    }

    public void loadFile() {
     List<String> fileInput = FileManager.readTextFileAllLines(fileName);
     Iterator<String> i = fileInput.iterator();
        modules.clear();
        while (i.hasNext()) {
            String s = i.next();
            if (!s.replaceAll("\\s", "").isEmpty()) {
                modules.add(s);
            }
        }
    }

    @SubscribeEvent
    public void onToggleModule(ClientEvent event) {
        if (!moduleMessage.getValue()) {
            return;
        }

        if (event.getStage() == 0) {
            Module module = (Module) event.getFeature();
            if (!module.equals(this) && (modules.contains(module.getDisplayName()) || !list.getValue())) {
                if (watermark.getValue()) {
                Command.sendMessage(TextUtil.RED + module.getDisplayName() + " disabled."); 
                }  else {
                    Command.sendSilentMessage(TextUtil.RED + module.getDisplayName() + " disabled.");
                }
            }
        }
         
        if (event.getStage() == 1) {
            Module module = (Module) event.getFeature();
            if (modules.contains(module.getDisplayName()) || !list.getValue()) {
                if (watermark.getValue()) {
                Command.sendMessage(TextUtil.GREEN + module.getDisplayName() + " enabled.");
            } else {
                Command.sendSilentMessage(TextUtil.GREEN + module.getDisplayName() + " enabled.");
            }
            
        }  
    }   
        
    } // noob code

    public static Notifications getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Notifications();
        }
        return INSTANCE;
    }

    public static void displayCrash(Exception e) {
        Command.sendMessage(TextUtil.RED + "Exception caught: " + e.getMessage());
    }
}
