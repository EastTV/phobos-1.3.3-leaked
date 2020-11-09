package me.earth.phobos.features.modules.misc;

import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.manager.FileManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Announcer extends Module {

    private final Setting<Boolean> join = register(new Setting("Join", true));
    private final Setting<Boolean> leave = register(new Setting("Leave", true));
    private final Setting<Boolean> eat = register(new Setting("Eat", true));
    private final Setting<Boolean> walk = register(new Setting("Walk", true));
    private final Setting<Boolean> mine = register(new Setting("Mine", true));
    private final Setting<Boolean> place = register(new Setting("Place", true));
    private final Setting<Boolean> totem = register(new Setting("TotemPop", true));

    private final Setting<Boolean> random = register(new Setting("Random", true));
    private final Setting<Boolean> greentext = register(new Setting("Greentext", false));
    private final Setting<Boolean> loadFiles = register(new Setting("LoadFiles", false));
    private final Setting<Integer> delay = register(new Setting("SendDelay", 40));
    private final Setting<Integer> queueSize = register(new Setting("QueueSize", 5, 1, 100));
    private final Setting<Integer> mindistance = register(new Setting("Min Distance", 10, 1, 100));
    private final Setting<Boolean> clearQueue = register(new Setting("ClearQueue", false));

    private static final String directory = "phobos/announcer/";

    private Map<Action, ArrayList<String>> loadedMessages = new HashMap<>();

    private Map<Action, Message> queue = new HashMap<>();

    public Announcer() {
        super("Announcer", "How to get muted quick.", Category.MISC, true, false, false);
    }

    @Override
    public void onLoad() {
        loadMessages();
    }

    @Override
    public void onEnable() {
        loadMessages();
    }

    @Override
    public void onUpdate() {
        if (loadFiles.getValue()) {
            loadMessages();
            Command.sendMessage("<Announcer> Loaded messages.");
            loadFiles.setValue(false);
        }
    }

    public void loadMessages() {
        HashMap<Action, ArrayList<String>> newLoadedMessages = new HashMap<>();
        for(Action action : Action.values()) {
            String fileName = directory + action.getName() + ".txt";
            List<String> fileInput = FileManager.readTextFileAllLines(fileName);
            Iterator<String> i = fileInput.iterator();
            ArrayList<String> msgs = new ArrayList<>();
            while (i.hasNext()) {
                String string = i.next();
                if (!string.replaceAll("\\s", "").isEmpty()) {
                    msgs.add(string);
                }
            }

            if(msgs.isEmpty()) {
                msgs.add(action.getStandartMessage());
            }

            newLoadedMessages.put(action, msgs);
        }
        this.loadedMessages = newLoadedMessages;
    }

    private String getMessage(Action action, int number, String info) {

        return "";
    }

    private Action getRandomAction() {
        Random rnd = new Random();
        int index = rnd.nextInt(7);
        int i = 0;
        for(Action action : Action.values()) {
            if(i == index) {
                return action;
            }
            i++;
        }
        return Action.WALK;
    }

    public static class Message {

        public final Action action;
        public final String name;
        public final int amount;

        public Message(Action action, String name, int amount) {
            this.action = action;
            this.name = name;
            this.amount = amount;
        }
    }

    public enum Action {
        JOIN("Join", "Welcome _!"),
        LEAVE("Leave", "Goodbye _!"),
        EAT("Eat", "I just ate % _!"),
        WALK("Walk", "I just walked % Blocks!"),
        MINE("Mine", "I mined % _!"),
        PLACE("Place","I just placed % _!"),
        TOTEM("Totem", "_ just popped % Totems!");

        private final String name;
        private final String standartMessage;

        Action(String name, String standartMessage) {
            this.name = name;
            this.standartMessage = standartMessage;
        }

        public String getName() {
            return this.name;
        }

        public String getStandartMessage() {
            return this.standartMessage;
        }
    }
}