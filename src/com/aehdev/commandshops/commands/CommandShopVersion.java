package com.aehdev.commandshops.commands;

import org.bukkit.command.CommandSender;

import com.aehdev.commandshops.CommandShops;

// TODO: Auto-generated Javadoc
/**
 * The Class CommandShopVersion.
 */
public class CommandShopVersion extends Command
{

	/**
	 * Instantiates a new command shop version.
	 * @param plugin
	 * the plugin
	 * @param commandLabel
	 * the command label
	 * @param sender
	 * the sender
	 * @param command
	 * the command
	 */
	public CommandShopVersion(CommandShops plugin, String commandLabel,
			CommandSender sender, String command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/**
	 * Instantiates a new command shop version.
	 * @param plugin
	 * the plugin
	 * @param commandLabel
	 * the command label
	 * @param sender
	 * the sender
	 * @param command
	 * the command
	 */
	public CommandShopVersion(CommandShops plugin, String commandLabel,
			CommandSender sender, String[] command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/* (non-Javadoc)
	 * @see com.aehdev.commandshops.commands.Command#process() */
	public boolean process()
	{
		sender.sendMessage(String.format("CommandShops Version %s", plugin
				.getDescription().getVersion()));
		return true;
	}
}
