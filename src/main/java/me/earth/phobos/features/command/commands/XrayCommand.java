package me.earth.phobos.features.command.commands;

import me.earth.phobos.Phobos;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.modules.render.XRay;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.TextUtil;

public class XrayCommand extends Command {

    public XrayCommand() {
        super("xray", new String[]{"<add/del>", "<block>"});
    }

    @Override
    public void execute(String[] commands) {
        XRay module = Phobos.moduleManager.getModuleByClass(XRay.class);
        if(module != null) {
            if (commands.length == 1) {
                StringBuilder blocks = new StringBuilder();
                for (Setting setting : module.getSettings()) {
                    if (setting.equals(module.enabled) || setting.equals(module.drawn) || setting.equals(module.bind) || setting.equals(module.newBlock) || setting.equals(module.showBlocks)) {
                        continue;
                    }

                    blocks.append(setting.getName()).append(", ");
                }
                Command.sendMessage(blocks.toString());
                return;
            } else if(commands.length == 2) {
                sendMessage("Please specify a block.");
                return;
            }

            String addRemove = commands[0];
            String blockName = commands[1];
            if(addRemove.equalsIgnoreCase("del") || addRemove.equalsIgnoreCase("remove")) {
                Setting setting = module.getSettingByName(blockName);
                if(setting != null) {
                    if (setting.equals(module.enabled) || setting.equals(module.drawn) || setting.equals(module.bind) || setting.equals(module.newBlock) || setting.equals(module.showBlocks)) {
                        return;
                    }
                    module.unregister(setting);
                }
                sendMessage("<XRay>" + TextUtil.RED + " Removed: " + blockName);
            } else if(addRemove.equalsIgnoreCase("add")) {
                if (!module.shouldRender(blockName)) {
                    module.register(new Setting(blockName, true, v -> module.showBlocks.getValue()));
                    sendMessage("<Xray> Added new Block: " + blockName);
                }
            } else {
                sendMessage(TextUtil.RED + "An error occured, block either exists or wrong use of command: .xray <add/del(remove)> <block>");
            }
        }
    }
}
