package com.aehdev.commandshops.commands;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.aehdev.commandshops.CommandShops;
import com.aehdev.commandshops.ItemInfo;
import com.aehdev.commandshops.Search;

/**
 * Command that queries the text-matching mechanism for item names.
 */
public class CommandShopSearch extends Command
{

	/**
	 * Create a Search order.
	 * @param plugin
	 * reference to the main CommandShops plugin object
	 * @param commandLabel
	 * command name/alias
	 * @param sender
	 * who sent the command
	 * @param command
	 * command string with arguments
	 */
	public CommandShopSearch(CommandShops plugin, String commandLabel,
			CommandSender sender, String command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/**
	 * Execute the search.
	 */
	public boolean process()
	{
		Player player = null;
		if(sender instanceof Player) player = (Player)sender;
		
		//> /shop search
		// query item in hand
		Pattern pattern = Pattern.compile("(?i)search$");
		Matcher matcher = pattern.matcher(command);
		if(matcher.find())
		{
			if(player == null)
			{
				sender.sendMessage("You need an item in hand to search without specifying an item.");
				return false;
			}
			ItemStack itemStack = player.getItemInHand();
			if(itemStack == null){ return true; }
			ItemInfo found = null;
			if(CommandShops.getItemList().isDurable(itemStack))
			{
				found = Search.itemById(itemStack.getTypeId());
			}else{
				found = Search.itemById(itemStack.getTypeId(),
						itemStack.getDurability());
			}
			if(found == null)
			{
				sender.sendMessage("Could not find an item for "+itemStack.getTypeId()+":"+itemStack.getDurability());
			}else{
				sender.sendMessage(found.toString());
			}
			return true;
		}
		
		//> /shop search [itemname]
		// query item with given name
		pattern = Pattern.compile("(?i)search\\s+(.*)");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			String name = matcher.group(1);
			ItemInfo found = Search.itemByName(name);
			if(found == null)
			{
				sender.sendMessage(String.format((Locale)null,
						"No item was found matching \"%s\"", name));
			}else{
				sender.sendMessage(found.toString());
			}
			return true;
		}

		// Show search stuff
		sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel
				+ " search [item name]" + ChatColor.DARK_AQUA
				+ " - Searches for and displays information about an item.");
		return true;
	}
}
