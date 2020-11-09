package me.earth.phobos.features.command.commands;

import me.earth.phobos.Phobos;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.manager.FriendManager;
import me.earth.phobos.util.TextUtil;

public class FriendCommand extends Command {

    public FriendCommand() {
        super("friend", new String[]{"<add/del/name/clear>" , "<name>"});
    }

    @Override
    public void execute(String[] commands) {
        if(commands.length == 1) {
            if(Phobos.friendManager.getFriends().isEmpty()) {
                sendMessage("You currently dont have any friends added.");
            } else {
                String f = "Friends: ";
                for(FriendManager.Friend friend : Phobos.friendManager.getFriends()) {
                    try {
                        f += friend.getUsername() + ", ";
                    } catch (Exception e) {
                        continue;
                    }
                }
                sendMessage(f);
            }
            return;
        }

        if(commands.length == 2) {
            switch(commands[0]) {
                case "reset":
                    Phobos.friendManager.onLoad();
                    sendMessage("Friends got reset.");
                    break;
                default:
                    sendMessage(commands[0] + (Phobos.friendManager.isFriend(commands[0]) ? " is friended." : " isnt friended."));
                    break;
            }
            return;
        }

        if(commands.length >= 2) {
            switch(commands[0]) {
                case "add" :
                    Phobos.friendManager.addFriend(commands[1]);
                    sendMessage(TextUtil.AQUA + commands[1] + " has been friended");
                    break;
                case "del" :
                    Phobos.friendManager.removeFriend(commands[1]);
                    sendMessage(TextUtil.RED + commands[1] + " has been unfriended");
                    break;
                default :
                    sendMessage(TextUtil.RED + "Bad Command, try: friend <add/del/name> <name>.");
            }
        }
    }
}
