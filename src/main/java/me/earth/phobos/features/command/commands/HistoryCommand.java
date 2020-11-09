package me.earth.phobos.features.command.commands;

import me.earth.phobos.features.command.Command;
import me.earth.phobos.util.PlayerUtil;
import me.earth.phobos.util.TextUtil;

import java.util.List;
import java.util.UUID;

public class HistoryCommand extends Command {

    public HistoryCommand() {
        super("history", new String[]{"<player>"});
    }

    @Override
    public void execute(String[] commands) {
        if(commands.length == 1 || commands.length == 0) { //idk lol
            sendMessage(TextUtil.RED + "Please specify a player.");
        }

        UUID uuid;
        try {
            uuid = PlayerUtil.getUUIDFromName(commands[0]);
        } catch (Exception e) {
            sendMessage("An error occured.");
            return;
        }

        List<String> names;
        try {
            names = PlayerUtil.getHistoryOfNames(uuid);
        } catch (Exception e) {
            sendMessage("An error occured.");
            return;
        }

        if(names != null) {
            sendMessage(commands[0] + "´s name history:");
            for(String name : names) {
                sendMessage(name);
            }
        } else {
            sendMessage("No names found.");
        }
    }
}
