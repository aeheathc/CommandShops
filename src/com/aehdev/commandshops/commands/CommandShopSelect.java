package com.aehdev.commandshops.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.aehdev.commandshops.CommandShops;
import com.aehdev.commandshops.Selection;
import com.aehdev.commandshops.ShopsPlayerListener;

/**
 * Command that lets the player make a cuboid selection that will later define shop boundaries
 */
public class CommandShopSelect extends Command
{

	/**
	 * Create a Selection order.
	 * @param plugin
	 * reference to the main CommandShops plugin object
	 * @param commandLabel
	 * command name/alias
	 * @param sender
	 * who sent the command
	 * @param command
	 * command string with arguments
	 */
	public CommandShopSelect(CommandShops plugin, String commandLabel,
			CommandSender sender, String command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/**
	 * Run select command; start selection mode for the player.
	 */
	public boolean process()
	{
		if(!(sender instanceof Player))
		{
			sender.sendMessage(ChatColor.DARK_AQUA
					+ "Only players can interactively select coordinates.");
			return false;
		}

		if(!canUseCommand(CommandTypes.CREATE) && !canUseCommand(CommandTypes.MOVE))
		{
			sender.sendMessage(ChatColor.DARK_AQUA
					+ "You don't have permission for any feature that would use a selection.");
			return false;
		}
		Player player = (Player)sender;
		String playerName = player.getName();
		String world = player.getWorld().getName();
		
		if(!ShopsPlayerListener.selectingPlayers.containsKey(playerName))
		{
			Selection sel = new Selection();
			sel.world = world;
			ShopsPlayerListener.selectingPlayers.put(playerName, sel);
			sender.sendMessage(ChatColor.WHITE + "Shop selection enabled."
					+ ChatColor.DARK_AQUA + " Use " + ChatColor.WHITE
					+ "bare hands " + ChatColor.DARK_AQUA + "to select!");
			sender.sendMessage(ChatColor.DARK_AQUA
					+ "Left click to select the bottom corner for a shop");
			sender.sendMessage(ChatColor.DARK_AQUA
					+ "Right click to select the far upper corner for the shop");
		}else{
			ShopsPlayerListener.selectingPlayers.remove(playerName);
			sender.sendMessage(ChatColor.DARK_AQUA + "Selection disabled");
		}
		return true;
	}
}
