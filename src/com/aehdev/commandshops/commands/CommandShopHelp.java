package com.aehdev.commandshops.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.aehdev.commandshops.CommandShops;

/**
 * Command that shows syntax for all commands.
 */
public class CommandShopHelp extends Command
{

	/**
	 * Create a help request.
	 * @param plugin
	 * reference to the main plugin object
	 * @param commandLabel
	 * command name/alias
	 * @param sender
	 * who sent the command
	 * @param command
	 * command string with arguments
	 */
	public CommandShopHelp(CommandShops plugin, String commandLabel,
			CommandSender sender, String command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/**
	 * Create a help request.
	 * @param plugin
	 * reference to the main plugin object
	 * @param commandLabel
	 * command name/alias
	 * @param sender
	 * who sent the command
	 * @param command
	 * command string with arguments
	 */
	public CommandShopHelp(CommandShops plugin, String commandLabel,
			CommandSender sender, String[] command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/**
	 * Run the request and display the help.
	 */
	public boolean process()
	{
		sender.sendMessage(CommandShops.CHAT_PREFIX + ChatColor.DARK_AQUA
				+ "Here are the available commands [required] <optional>");

		if(canUseCommand(CommandTypes.ADD))
		{
			sender.sendMessage(ChatColor.WHITE + "   /shop add"
					+ ChatColor.DARK_AQUA
					+ " - Add the item that you are holding to the shop.");
		}
		if(canUseCommand(CommandTypes.BROWSE))
		{
			sender.sendMessage(ChatColor.WHITE + "   /shop browse <buy|sell|itemname> " + ChatColor.DARK_AQUA
					+ "-List the shop's inventory");
		}
		if(canUseCommand(CommandTypes.BUY))
		{
			sender.sendMessage(ChatColor.WHITE + "   /shop buy [itemname] [number] " + ChatColor.DARK_AQUA
					+ "- Buy this item.");
		}
		if(canUseCommand(CommandTypes.CREATE))
		{
			sender.sendMessage(ChatColor.WHITE + "   /shop create [ShopName]" + ChatColor.DARK_AQUA
					+ " - Create a shop at your location or selection.");
		}
		if(canUseCommand(CommandTypes.DESTROY))
		{
			sender.sendMessage(ChatColor.WHITE + "   /shop destroy" + ChatColor.DARK_AQUA
					+ " - Destroy the shop you're in.");
		}
		sender.sendMessage(ChatColor.WHITE + "   /shop find [itemname]" + ChatColor.DARK_AQUA
				+ " - Find closest shops by item name.");
		if(canUseCommand(CommandTypes.MOVE))
		{
			sender.sendMessage(ChatColor.WHITE + "   /shop move [ShopID]" + ChatColor.DARK_AQUA
					+ " - Move a shop to your location or selection.");
		}
		sender.sendMessage(ChatColor.WHITE + "   /shop search [itemname]" + ChatColor.DARK_AQUA
				+ " - Search for an item by name.");
		if(canUseCommand(CommandTypes.CREATE) || canUseCommand(CommandTypes.MOVE))
		{
			sender.sendMessage(ChatColor.WHITE + "   /shop select" + ChatColor.DARK_AQUA
					+ " - Select two corners for custom shop size.");
		}
		if(canUseCommand(CommandTypes.SELL))
		{
			sender.sendMessage(ChatColor.WHITE + "   /shop sell <#|all>" + ChatColor.DARK_AQUA
					+ " - Sell the item in your hand.");
			sender.sendMessage(ChatColor.WHITE + "   /shop sell [itemname] [number]");
		}
		if(canUseCommand(CommandTypes.SET))
		{
			sender.sendMessage(ChatColor.WHITE + "   /shop set"
					+ ChatColor.DARK_AQUA + " - Display list of set commands");
		}
		if(canUseCommand(CommandTypes.REMOVE))
		{
			sender.sendMessage(ChatColor.WHITE + "   /shop remove [itemname]" + ChatColor.DARK_AQUA
					+ " - Remove all stock of certain item from shop.");
		}
		if(true)
		{
			sender.sendMessage(ChatColor.WHITE + "   /shop list" + ChatColor.DARK_AQUA
					+ " - List all shops you control (owns/manage)");
		}
		if(true)
		{
			sender.sendMessage(ChatColor.WHITE + "   /shop log <filterparams>" + ChatColor.DARK_AQUA
					+ " - View transaction log. Must provide a shop ID (like 'shop=2') if not in a shop.");
		}
		return true;
	}
}
