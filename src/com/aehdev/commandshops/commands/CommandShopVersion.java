package com.aehdev.commandshops.commands;

import java.util.Locale;

import org.bukkit.command.CommandSender;

import com.aehdev.commandshops.CommandShops;

/**
 * Command that queries the plugin version info.
 */
public class CommandShopVersion extends Command
{

	/**
	 * Create a version order.
	 * @param plugin
	 * reference to the main CommandShops plugin object
	 * @param commandLabel
	 * command name/alias
	 * @param sender
	 * who sent the command
	 * @param command
	 * command string with arguments
	 */
	public CommandShopVersion(CommandShops plugin, String commandLabel,
			CommandSender sender, String command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/**
	 * Run version command; display the version info to the sender.
	 */
	public boolean process()
	{
		sender.sendMessage(String.format((Locale)null,"CommandShops Version %s", plugin.getDescription().getVersion()));
		return true;
	}
}
