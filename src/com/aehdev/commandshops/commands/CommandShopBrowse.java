package com.aehdev.commandshops.commands;

import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.aehdev.commandshops.CommandShops;
import com.aehdev.commandshops.ItemInfo;
import com.aehdev.commandshops.Search;
import com.aehdev.commandshops.Shop;

/**
 * Command that lets users view the contents and prices of a shop.
 */
public class CommandShopBrowse extends Command
{
	/**
	 * Creates a new Browse order.
	 * @param plugin
	 * reference to the main plugin object
	 * @param commandLabel
	 * command alias typed by sender
	 * @param sender
	 * the sender
	 * @param command
	 * input command and arguments
	 */
	public CommandShopBrowse(CommandShops plugin, String commandLabel,
			CommandSender sender, String command)
	{
		super(plugin, commandLabel, sender, command);
	}


	/**
	 * Run this Browse command.
	 * @return true on success
	 */
	public boolean process()
	{
		if(!(sender instanceof Player))
		{
			sender.sendMessage("Console can't browse because it is unable to be in a shop.");
			return false;
		}
		Player player = (Player)sender;
		long shop = Shop.getCurrentShop(player);

		if(shop == -1)
		{
			sender.sendMessage("You are not in a shop!");
			return false;
		}

		if(!canUseCommand(CommandTypes.BROWSE))
		{
			sender.sendMessage(CommandShops.CHAT_PREFIX
					+ ChatColor.DARK_AQUA
					+ "You don't have permission to use this command");
			return false;
		}

		int pageNumber = 1;

		//> /shop browse
		// display 1st page of all stocked items
		Pattern pattern = Pattern.compile("(?i)(bro|brow|brows|browse)$");
		Matcher matcher = pattern.matcher(command);
		if(matcher.find())
		{
			printInventory(shop, "list", 1);
			return true;
		}

		//> /shop browse (buy|sell) pagenum
		// show specified page of shop's items with specified transaction type available.
		matcher.reset();
		pattern = Pattern.compile("(?i)bro.*\\s+(buy|sell|list)\\s+(\\d+)");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			String type = matcher.group(1);
			pageNumber = Integer.parseInt(matcher.group(2));
			printInventory(shop, type, pageNumber);
			return true;
		}

		//> /shop browse (buy|sell)
		// show page 1 of shop's items with specified transaction type available.
		matcher.reset();
		pattern = Pattern.compile("(?i)bro.*\\s+(buy|sell|list)");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			String type = matcher.group(1);
			printInventory(shop, type, 1);
			return true;
		}

		//> /shop browse int
		// show specified page of all stocked items
		matcher.reset();
		pattern = Pattern.compile("(?i)bro.*\\s+(\\d+)");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			pageNumber = Integer.parseInt(matcher.group(1));
			printInventory(shop, "list", pageNumber);
			return true;
		}

		//> /shop browse item
		// display all information about specified item
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
	private void printInventoryItem(long shop, String searchName)
	{
		//resolve indicated item
		ItemInfo itemn = Search.itemByName(searchName);
		if(itemn == null)
		{
			sender.sendMessage(String
					.format("No item found for %s", searchName));
			return;
		}
		//Get info about shop and item
		boolean shopUnlimitedStock = false;
		String shopName = "";
		int maxstock = 10;
		double stock = 0, buy = -1, sell = -1;
		try{
			ResultSet resShop = CommandShops.db.query("SELECT name,unlimitedStock FROM shops WHERE id="+shop+" LIMIT 1");
			resShop.next();
			shopName = resShop.getString("name");
			shopUnlimitedStock = resShop.getInt("unlimitedStock") == 1;
			resShop.close();
			String itemQuery = String.format("SELECT stock,maxstock,buy,sell FROM shop_items WHERE"
								+ "	shop=%d AND	itemid=%d AND	itemdamage=%d	LIMIT 1"
								,	shop,		itemn.typeId,	itemn.subTypeId);
			ResultSet resItem = CommandShops.db.query(itemQuery);
			if(resItem.next())
			{
				stock = resItem.getDouble("stock");
				maxstock = resItem.getInt("maxstock");
				buy = resItem.getDouble("buy");
				if(resItem.wasNull()) buy = -1;
				sell = resItem.getDouble("sell");
				if(resItem.wasNull()) sell = -1;
				resItem.close();
			}else{
				resItem.close();
				sender.sendMessage(String.format("%s does not have %s", shopName, itemn.name));
				return;
			}
		}catch(Exception e){
			log.warning(String.format("[%s] Couldn't get shop info: %s", CommandShops.pdfFile.getName(), e));
			sender.sendMessage(ChatColor.DARK_AQUA + "Browse cancelled due to DB error.");
			return;
		}

		String sellPrice;
		if(sell < 0)
		{
			sellPrice = "--";
		}else{
			sellPrice = (stock == 0 ? ChatColor.RED : "")
					+ plugin.econ.format(sell)
					+ (stock == 0 ? (ChatColor.GOLD) : "");
		}

		String buyPrice;
		if(buy < 0)
		{
			buyPrice = "--";
		}else{
			buyPrice = (maxstock > 0 && stock > maxstock ? ChatColor.RED : "")
					+ plugin.econ.format(buy)
					+ (maxstock > 0 && stock > maxstock ? (ChatColor.GREEN)
							: "");
		}

		String[] message = {"","","","",""};
		message[0] = ChatColor.DARK_AQUA + "The shop " + ChatColor.WHITE + shopName;
		message[1] = ChatColor.DARK_AQUA + "has " + ChatColor.WHITE + itemn.name + ':';
		message[2] = ChatColor.DARK_AQUA + "[" + ChatColor.WHITE + "Stock: "
				+ (shopUnlimitedStock ? "Inf." : stock)
				+ ChatColor.DARK_AQUA + "]"
				+ ((!shopUnlimitedStock) ? (" [" + ChatColor.WHITE + "Max Stock: "
						+ maxstock + ChatColor.DARK_AQUA + "]") : "");
		message[3] = ChatColor.GOLD + "Selling for: " + ChatColor.DARK_AQUA + " ["
				+ ChatColor.WHITE + sellPrice + ChatColor.DARK_AQUA + "]";
		message[4] = ChatColor.GREEN + "Buying for: " + ChatColor.DARK_AQUA + " ["
				+ ChatColor.WHITE + buyPrice + ChatColor.DARK_AQUA + "]";
		sender.sendMessage(message);
	}

	/**
	 * Prints shop inventory list. Takes buy, sell, or list as arguments for
	 * which format to print.
	 * @param shop
	 * shop to lookup
	 * @param buySellorList
	 * Which mode to display
	 * @param pageNumber
	 * the page number
	 */
	private void printInventory(long shop, String buySellorList, int pageNumber)
	{
		//Get info about shop and item
		boolean shopUnlimitedStock = false;
		String shopName = "";
		String filter = "";
		String mode = "list";
		if(buySellorList.equalsIgnoreCase("buy"))
		{
			mode = "buy";
			filter = "`sell` IS NOT NULL";
		}else if(buySellorList.equalsIgnoreCase("sell")){
			mode = "sell";
			filter = "`buy` IS NOT NULL";
		}else{
			mode = "list";
			filter = "`sell` IS NOT NULL OR `buy` IS NOT NULL OR `stock`>=1";
		}
		int start = (pageNumber-1) * 5;
		int total = 0;
		StringBuffer output = new StringBuffer(60);
		LinkedList<String> msg = new LinkedList<String>();
		
		try{
			ResultSet resShop = CommandShops.db.query("SELECT name,unlimitedStock FROM shops WHERE id="+shop+" LIMIT 1");
			resShop.next();
			shopName = resShop.getString("name");
			shopUnlimitedStock = resShop.getInt("unlimitedStock") == 1;
			resShop.close();
			String countQuery= String.format("SELECT COUNT(*) FROM shop_items WHERE	shop=%d AND	(%s)", shop, filter);
			ResultSet resCount = CommandShops.db.query(countQuery);
			resCount.next();
			total = resCount.getInt(1);
			resCount.close();
			if(total == 0)
			{
				sender.sendMessage(ChatColor.DARK_AQUA + "No items to "+ buySellorList + ".");
				return;
			}
			output.append(ChatColor.DARK_AQUA);
			output.append("The shop ");
			output.append(ChatColor.WHITE);
			output.append(shopName);
			output.append(ChatColor.DARK_AQUA);
			output.append(mode.equals("buy") ? " is selling" : (mode.equals("sell") ? " is buying" : " has"));
			output.append(String.format(": (Page %d of %d)", pageNumber, (int)Math.ceil(((double)total) / 5.0)));
			msg.add(output.toString());
			String itemQuery = String.format("SELECT itemid,itemdamage,stock,maxstock,buy,sell FROM shop_items WHERE"
								+ "	shop=%d AND	(%s) ORDER BY itemid,itemdamage LIMIT %d,5"
								,	shop,		filter,								start);
			ResultSet resItem = CommandShops.db.query(itemQuery);
			while(resItem.next())
			{
				output = new StringBuffer(60);
				ItemInfo ii = Search.itemById(resItem.getInt("itemid"),(short)resItem.getInt("itemdamage"));
				int stock = (int)Math.floor(resItem.getDouble("stock"));
				int maxstock = resItem.getInt("maxstock");
				String stockstr = shopUnlimitedStock ? "Inf." :
								(stock + "" + ChatColor.DARK_AQUA + "/" + ChatColor.WHITE + maxstock);
				String buy = plugin.econ.format(resItem.getDouble("buy"));
				if(resItem.wasNull()) buy = "--";
				String sell = plugin.econ.format(resItem.getDouble("sell"));
				if(resItem.wasNull()) sell = "--";
				
				output.append(ChatColor.WHITE);
				output.append(ii.name);
				output.append(ChatColor.DARK_AQUA);
				output.append(" Stock:");
				output.append(ChatColor.WHITE);
				output.append(stockstr);
				msg.add(output.toString());
				output = new StringBuffer(60);
				output.append(ChatColor.GOLD);
				output.append(" selling@");
				if(stock<1 && !shopUnlimitedStock) output.append(ChatColor.RED);
				output.append(sell);
				output.append(ChatColor.GREEN);
				output.append(" buying@");
				if(stock>=maxstock) output.append(ChatColor.RED);
				output.append(buy);
				msg.add(output.toString());
			}
			resItem.close();
			String[] example = new String[1];
			sender.sendMessage(msg.toArray(example));
		}catch(Exception e){
			log.warning(String.format("[%s] Couldn't get shop info list: %s", CommandShops.pdfFile.getName(), e));
			sender.sendMessage(ChatColor.DARK_AQUA + "Browse cancelled due to DB error.");
			return;
		}
	}
}
