package com.aehdev.commandshops.commands;

import java.util.Locale;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.aehdev.commandshops.CommandShops;

/**
 * Command that queries the plugin version info.
 */
public class CommandShopReload extends Command
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
	public CommandShopReload(CommandShops plugin, String commandLabel,
			CommandSender sender, String command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/**
	 * Run version command; display the version info to the sender.
	 */
	public boolean process()
	{
		if((sender instanceof Player) && !canUseCommand(CommandTypes.ADMIN))
		{
			sender.sendMessage("Only CS adminds can reload");
			return false;
		}
		log.info(String.format((Locale)null,"[%s] Starting reload", plugin.getDescription().getName()));

		//tell Bukkit config engine to reload from disk
		plugin.reloadConfig();
		
		plugin.onDisable();
		plugin.onEnable();
		sender.sendMessage("CommandShops reloaded");
		log.info(String.format((Locale)null,"[%s] Reload finished.", plugin.getDescription().getName()));
		return true;
	}
}
