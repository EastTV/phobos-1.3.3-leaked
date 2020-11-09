package me.earth.phobos.manager;

import me.earth.phobos.features.Feature;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.command.commands.BindCommand;
import me.earth.phobos.features.command.commands.BookCommand;
import me.earth.phobos.features.command.commands.ConfigCommand;
import me.earth.phobos.features.command.commands.CrashCommand;
import me.earth.phobos.features.command.commands.FriendCommand;
import me.earth.phobos.features.command.commands.HelpCommand;
import me.earth.phobos.features.command.commands.HistoryCommand;
import me.earth.phobos.features.command.commands.ModuleCommand;
import me.earth.phobos.features.command.commands.PeekCommand;
import me.earth.phobos.features.command.commands.PrefixCommand;
import me.earth.phobos.features.command.commands.ReloadCommand;
import me.earth.phobos.features.command.commands.ReloadSoundCommand;
import me.earth.phobos.features.command.commands.UnloadCommand;
import me.earth.phobos.features.command.commands.XrayCommand;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CommandManager extends Feature {

    //TODO: space at the end of "<Phobos.eu> " doesnt save!
    private String clientMessage = "<Phobos.eu>";
    private String prefix = ".";

    //public Setting<String> clientMessage = register(new Setting("clientMessage", "<Phobos.eu>"));
    //public Setting<String> prefix = register(new Setting("prefix", "."));
    private ArrayList<Command> commands;

    public CommandManager() {
        super("Command");
        commands = new ArrayList<>();
        commands.add(new BindCommand());
        commands.add(new ModuleCommand());
        commands.add(new PrefixCommand());
        commands.add(new ConfigCommand());
        commands.add(new FriendCommand());
        commands.add(new HelpCommand());
        commands.add(new ReloadCommand());
        commands.add(new UnloadCommand());
        commands.add(new ReloadSoundCommand());
        commands.add(new PeekCommand());
        commands.add(new XrayCommand());
        commands.add(new BookCommand());
        commands.add(new CrashCommand());
        commands.add(new HistoryCommand());
    }

    public void executeCommand(String command){
        String[] parts = command.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        String name = parts[0].substring(1);
        String[] args = removeElement(parts, 0);
        for (int i = 0; i < args.length; i++){
            if (args[i] == null) continue;
            args[i] = strip(args[i], "\"");
        }
        for (Command c : commands){
            if (c.getName().equalsIgnoreCase(name)){
                c.execute(parts);
                return;
            }
        }
        Command.sendMessage("Unknown command. try 'commands' for a list of commands.");
    }

    public static String[] removeElement(String[] input, int indexToDelete) {
        List result = new LinkedList();
        for (int i = 0; i < input.length; i++){
            if (i != indexToDelete) result.add(input[i]);
        }
        return (String[]) result.toArray(input);
    }

    private static String strip(String str, String key) {
        if (str.startsWith(key) && str.endsWith(key)) return str.substring(key.length(), str.length()-key.length());
        return str;
    }

    public Command getCommandByName(String name){
        for (Command command : commands){
            if (command.getName().equals(name)) {
                return command;
            }
        }
        return null;
    }

    public ArrayList<Command> getCommands() {
        return commands;
    }

    public String getClientMessage() {
        return clientMessage;
    }

    public void setClientMessage(String clientMessage) {
        this.clientMessage = clientMessage;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
