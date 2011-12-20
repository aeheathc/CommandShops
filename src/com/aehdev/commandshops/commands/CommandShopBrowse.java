package com.aehdev.commandshops.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.aehdev.commandshops.CommandShops;
import com.aehdev.commandshops.InventoryItem;
import com.aehdev.commandshops.ItemInfo;
import com.aehdev.commandshops.PlayerData;
import com.aehdev.commandshops.Search;
import com.aehdev.commandshops.Shop;
import com.aehdev.commandshops.comparator.InventoryItemSortByName;

// TODO: Auto-generated Javadoc
/**
 * The Class CommandShopBrowse.
 */
public class CommandShopBrowse extends Command
{

	/**
	 * Instantiates a new command shop browse.
	 * @param plugin
	 * the plugin
	 * @param commandLabel
	 * the command label
	 * @param sender
	 * the sender
	 * @param command
	 * the command
	 */
	public CommandShopBrowse(CommandShops plugin, String commandLabel,
			CommandSender sender, String command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/**
	 * Instantiates a new command shop browse.
	 * @param plugin
	 * the plugin
	 * @param commandLabel
	 * the command label
	 * @param sender
	 * the sender
	 * @param command
	 * the command
	 */
	public CommandShopBrowse(CommandShops plugin, String commandLabel,
			CommandSender sender, String[] command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/* (non-Javadoc)
	 * @see com.aehdev.commandshops.commands.Command#process() */
	public boolean process()
	{
		Shop shop = null;

		// Get current shop
		if(sender instanceof Player)
		{
			// Get player & data
			Player player = (Player)sender;
			PlayerData pData = plugin.getPlayerData().get(player.getName());

			// Get Current Shop
			UUID shopUuid = pData.getCurrentShop();
			if(shopUuid != null)
			{
				shop = plugin.getShopData().getShop(shopUuid);
			}
			if(shop == null)
			{
				sender.sendMessage("You are not in a shop!");
				return true;
			}

			// Check Permissions
			if(!canUseCommand(CommandTypes.BROWSE))
			{
				sender.sendMessage(CommandShops.CHAT_PREFIX
						+ ChatColor.DARK_AQUA
						+ "You don't have permission to use this command");
				return true;
			}

		}else
		{
			sender.sendMessage("Console is not implemented yet.");
			return true;
		}

		if(shop.getItems().size() == 0)
		{
			sender.sendMessage(String.format(
					"%s currently does not stock any items.", shop.getName()));
			return true;
		}

		int pageNumber = 1;

		// browse
		Pattern pattern = Pattern.compile("(?i)(bro|brow|brows|browse)$");
		Matcher matcher = pattern.matcher(command);
		if(matcher.find())
		{
			printInventory(shop, "list", pageNumber);
			return true;
		}

		// browse (buy|sell) pagenum
		matcher.reset();
		pattern = Pattern.compile("(?i)bro.*\\s+(buy|sell|info)\\s+(\\d+)");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			String type = matcher.group(1);
			pageNumber = Integer.parseInt(matcher.group(2));
			printInventory(shop, type, pageNumber);
			return true;
		}

		// browse (buy|sell)
		matcher.reset();
		pattern = Pattern.compile("(?i)bro.*\\s+(buy|sell|info)");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			String type = matcher.group(1);
			printInventory(shop, type, pageNumber);
			return true;
		}

		// browse int
		matcher.reset();
		pattern = Pattern.compile("(?i)bro.*\\s+(\\d+)");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			pageNumber = Integer.parseInt(matcher.group(1));
			printInventory(shop, "list", pageNumber);
			return true;
		}

		// browse item
		matcher.reset();
		pattern = Pattern.compile("(?i)bro.*\\s+(.+)");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			String searchName = matcher.group(1);
			printInventoryItem(shop, searchName);
			return true;
		}

		return false;
	}

	/**
	 * Prints an inventory sheet with just the item requested.
	 * @param shop
	 * the shop
	 * @param searchName
	 * Player's input item name to search for
	 */
	private void printInventoryItem(Shop shop, String searchName)
	{
		String inShopName = shop.getName();
		HashMap<String,InventoryItem> items = shop.getInventory();
		ItemInfo itemn = Search.itemByName(searchName);
		if(itemn == null)
		{
			sender.sendMessage(String
					.format("No item found for %s", searchName));
			return;
		}
		InventoryItem item = items.get(itemn.name);
		if(item == null)
		{
			sender.sendMessage(String.format("%s does not have %s", inShopName,
					itemn.name));
			return;
		}

		int stock = item.getStock(), maxstock = item.getMaxStock();

		String sellPrice;
		if(item.getBuyPrice() <= 0)
		{
			sellPrice = "--";
		}else
		{
			sellPrice = (stock == 0 ? ChatColor.RED : "")
					+ plugin.getEconManager().format(item.getBuyPrice())
					+ (stock == 0 ? (ChatColor.GOLD) : "");
		}

		String buyPrice;
		if(item.getSellPrice() <= 0)
		{
			buyPrice = "--";
		}else
		{
			buyPrice = (maxstock > 0 && stock > maxstock ? ChatColor.RED : "")
					+ plugin.getEconManager().format(item.getSellPrice())
					+ (maxstock > 0 && stock > maxstock ? (ChatColor.GREEN)
							: "");
		}

		String message = ChatColor.DARK_AQUA + "The shop " + ChatColor.WHITE
				+ inShopName + ChatColor.DARK_AQUA + " has " + ChatColor.WHITE
				+ itemn.name + ':';
		sender.sendMessage(message);
		message = ChatColor.DARK_AQUA
				+ "["
				+ ChatColor.WHITE
				+ "Stock: "
				+ (shop.isUnlimitedStock() ? "Inf." : item.getStock())
				+ ChatColor.DARK_AQUA
				+ "]"
				+ ((maxstock > 0 && !shop.isUnlimitedStock()) ? (" [" + ChatColor.WHITE + "Max Stock: "
						+ maxstock + ChatColor.DARK_AQUA + "]") : "");
		sender.sendMessage(message);
		message = ChatColor.GOLD
				+ "Selling: "
				+ ChatColor.DARK_AQUA
				+ " ["
				+ ChatColor.WHITE
				+ sellPrice
				+ ChatColor.DARK_AQUA
				+ "]";
		sender.sendMessage(message);
		message = ChatColor.GREEN
				+ "Buying: "
				+ ChatColor.DARK_AQUA
				+ " ["
				+ ChatColor.WHITE
				+ buyPrice
				+ ChatColor.DARK_AQUA
				+ "]";
		sender.sendMessage(message);
	}

	/**
	 * Prints shop inventory list. Takes buy, sell, or list as arguments for
	 * which format to print.
	 * @param shop
	 * the shop
	 * @param buySellorList
	 * the buy sellor list
	 * @param pageNumber
	 * the page number
	 */
	private void printInventory(Shop shop, String buySellorList, int pageNumber)
	{
		String inShopName = shop.getName();
		List<InventoryItem> items = shop.getItems();
		Collections.sort(items, new InventoryItemSortByName());

		boolean buy = buySellorList.equalsIgnoreCase("buy");
		boolean sell = buySellorList.equalsIgnoreCase("sell");
		boolean list = buySellorList.equalsIgnoreCase("list");

		ArrayList<String> inventoryMessage = new ArrayList<String>();
		for(InventoryItem item: items)
		{

			String subMessage = "   " + item.getInfo().name;
			int maxStock = 0;

			// NOT list
			if(!list)
			{
				double price = 0;
				if(buy)
				{
					// get buy price
					price = item.getBuyPrice();
				}
				if(sell)
				{
					price = item.getSellPrice();
				}
				if(price == 0)
				{
					continue;
				}
				subMessage += ChatColor.DARK_AQUA + " [" + ChatColor.WHITE
						+ plugin.getEconManager().format(price)
						+ ChatColor.DARK_AQUA + "]";
				if(sell)
				{
					int stock = item.getStock();
					maxStock = item.getMaxStock();

					if(stock >= maxStock && !(maxStock == 0))
					{
						continue;
					}
				}
			}

			// get stock
			int stock = item.getStock();
			if(buy)
			{
				if(stock == 0 && !shop.isUnlimitedStock()) continue;
			}
			if(!shop.isUnlimitedStock())
			{
				subMessage += ChatColor.DARK_AQUA + " [" + ChatColor.WHITE
						+ "Stock: " + stock + ChatColor.DARK_AQUA + "]";

				maxStock = item.getMaxStock();
				if(maxStock > 0)
				{
					subMessage += ChatColor.DARK_AQUA + " [" + ChatColor.WHITE
							+ "Max Stock: " + maxStock + ChatColor.DARK_AQUA
							+ "]";
				}
			}

			inventoryMessage.add(subMessage);
		}

		String message = ChatColor.DARK_AQUA + "The shop " + ChatColor.WHITE
				+ inShopName + ChatColor.DARK_AQUA;

		if(buy)
		{
			message += " is selling:";
		}else if(sell)
		{
			message += " is buying:";
		}else
		{
			message += " trades in: ";
		}

		message += " (Page " + pageNumber + " of "
				+ (int)Math.ceil((double)inventoryMessage.size() / (double)7)
				+ ")";

		sender.sendMessage(message);

		if(inventoryMessage.size() <= (pageNumber - 1) * 7)
		{
			sender.sendMessage(String.format(
					"%s does not have this many pages!", shop.getName()));
			return;
		}

		int amount = (pageNumber > 0 ? (pageNumber - 1) * 7 : 0);
		for(int i = amount; i < amount + 7; i++)
		{
			if(inventoryMessage.size() > i)
			{
				sender.sendMessage(inventoryMessage.get(i));
			}
		}

		if(!list)
		{
			String buySell = (buy ? "buy" : "sell");
			message = ChatColor.DARK_AQUA + "To " + buySell
					+ " an item on the list type: " + ChatColor.WHITE + "/"
					+ commandLabel + " " + buySell + " ItemName [amount]";
			sender.sendMessage(message);
		}else
		{
			sender.sendMessage(ChatColor.DARK_AQUA + "Type " + ChatColor.WHITE
					+ "/" + commandLabel + " browse buy" + ChatColor.DARK_AQUA
					+ " or " + ChatColor.WHITE + "/" + commandLabel
					+ " browse sell");
			sender.sendMessage(ChatColor.DARK_AQUA
					+ "to see details about price and quantity.");
		}
	}

}
