package com.aehdev.commandshops.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.aehdev.commandshops.CommandShops;
import com.aehdev.commandshops.PlayerData;

// TODO: Auto-generated Javadoc
/**
 * The Class CommandShopSelect.
 */
public class CommandShopSelect extends Command
{

	/**
	 * Instantiates a new command shop select.
	 * @param plugin
	 * the plugin
	 * @param commandLabel
	 * the command label
	 * @param sender
	 * the sender
	 * @param command
	 * the command
	 */
	public CommandShopSelect(CommandShops plugin, String commandLabel,
			CommandSender sender, String command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/**
	 * Instantiates a new command shop select.
	 * @param plugin
	 * the plugin
	 * @param commandLabel
	 * the command label
	 * @param sender
	 * the sender
	 * @param command
	 * the command
	 */
	public CommandShopSelect(CommandShops plugin, String commandLabel,
			CommandSender sender, String[] command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/* (non-Javadoc)
	 * @see com.aehdev.commandshops.commands.Command#process() */
	public boolean process()
	{
		if(!(sender instanceof Player))
		{
			sender.sendMessage(ChatColor.DARK_AQUA
					+ "Only players can interactively select coordinates.");
			return false;
		}

		if(canUseCommand(CommandTypes.SELECT))
		{

			Player player = (Player)sender;

			String playerName = player.getName();
			if(!plugin.getPlayerData().containsKey(playerName))
			{
				plugin.getPlayerData().put(playerName,
						new PlayerData(plugin, playerName));
			}
			plugin.getPlayerData()
					.get(playerName)
					.setSelecting(
							!plugin.getPlayerData().get(playerName)
									.isSelecting());

			if(plugin.getPlayerData().get(playerName).isSelecting())
			{
				sender.sendMessage(ChatColor.WHITE + "Shop selection enabled."
						+ ChatColor.DARK_AQUA + " Use " + ChatColor.WHITE
						+ "bare hands " + ChatColor.DARK_AQUA + "to select!");
				sender.sendMessage(ChatColor.DARK_AQUA
						+ "Left click to select the bottom corner for a shop");
				sender.sendMessage(ChatColor.DARK_AQUA
						+ "Right click to select the far upper corner for the shop");
			}else
			{
				sender.sendMessage(ChatColor.DARK_AQUA + "Selection disabled");
				plugin.getPlayerData().put(playerName,
						new PlayerData(plugin, playerName));
			}
			return true;
		}else
		{
			sender.sendMessage(CommandShops.CHAT_PREFIX + ChatColor.DARK_AQUA
					+ "You don't have permission to use this command");
			return true;
		}
	}
}
