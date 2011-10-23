package com.aehdev.commandshops.commands;


import org.bukkit.command.CommandSender;

import com.aehdev.commandshops.CommandShops;

public class CommandShopVersion extends Command {

    public CommandShopVersion(CommandShops plugin, String commandLabel, CommandSender sender, String command) {
        super(plugin, commandLabel, sender, command);
    }
    
    public CommandShopVersion(CommandShops plugin, String commandLabel, CommandSender sender, String[] command) {
        super(plugin, commandLabel, sender, command);
    }

    public boolean process() {
        sender.sendMessage(String.format("CommandShops Version %s", plugin.getDescription().getVersion()));
        return true;
    }
}
