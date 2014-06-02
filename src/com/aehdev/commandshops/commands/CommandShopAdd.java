package com.aehdev.commandshops.commands;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.aehdev.commandshops.CommandShops;
import com.aehdev.commandshops.Config;
import com.aehdev.commandshops.ItemInfo;
import com.aehdev.commandshops.Search;
import com.aehdev.commandshops.Shop;

/**
 * Command that allows shop owners/managers to put items in their shop. 
 */
public class CommandShopAdd extends Command
{

	/**
	 * Creates an Add order.
	 * @param plugin
	 * reference to the main plugin object
	 * @param commandLabel
	 * command alias typed by sender
	 * @param sender
	 * the sender
	 * @param command
	 * input command and arguments
	 */
	public CommandShopAdd(CommandShops plugin, String commandLabel, CommandSender sender, String command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/**
	 * Run this Add command.
	 * @return true on success
	 */
	public boolean process()
	{
		if(!(sender instanceof Player))
		{
			sender.sendMessage("Console can't add items because it doesn't have an inventory and is unable to be in a shop.");
			return false;
		}
		Player player = (Player)sender;
		long shop = Shop.getCurrentShop(player);
		if(shop == -1)
		{
			sender.sendMessage("You are not in a shop!");
			return false;
		}
		
		// Check Permissions
		if(!canUseCommand(CommandTypes.ADD))
		{
			sender.sendMessage(CommandShops.CHAT_PREFIX
					+ ChatColor.DARK_AQUA
					+ "You don't have permission to use this command");
			return false;
		}
		
		// Check if Player can Modify
		if(!isShopController(shop) && !canUseCommand(CommandTypes.ADMIN))
		{
			player.sendMessage(ChatColor.DARK_AQUA
					+ "You must be the owner, a manager, or an admin to stock a shop.");
			return false;
		}
		
		//> /shop add
		// Add the currently held item stack.
		Pattern pattern = Pattern.compile("(?i)add$");
		Matcher matcher = pattern.matcher(command);
		if(matcher.find())
		{
			ItemStack itemStack = player.getItemInHand();
			if(itemStack == null){ return false; }
			ItemInfo item = null;
			int amount = itemStack.getAmount();
			item = Search.itemById(itemStack);
			if(itemStack.getType().getMaxDurability() > 0)
			{
				if(calcDurabilityPercentage(itemStack) > Config.MAX_DAMAGE && Config.MAX_DAMAGE != 0)
				{
					sender.sendMessage(ChatColor.DARK_AQUA + "Your "
							+ ChatColor.WHITE + item.name
							+ ChatColor.DARK_AQUA
							+ " is too damaged to add to stock!");
					sender.sendMessage(ChatColor.DARK_AQUA
							+ "Items must be damanged less than "
							+ ChatColor.WHITE + Config.MAX_DAMAGE
							+ "%");
					return true;
				}
			}
			if(item == null)
			{
				sender.sendMessage("Could not find an item.");
				return false;
			}
			return shopAdd(shop, item, amount);
		}
		
		//> /shop add all
		// Add all items that are the same as what is currently held
		matcher.reset();
		pattern = Pattern.compile("(?i)add\\s+all$");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			ItemStack itemStack = player.getItemInHand();
			if(itemStack == null){ return false; }
			ItemInfo item = null;
			if(itemStack.getType().getMaxDurability() > 0)
			{
				item = Search.itemById(itemStack);
				if(calcDurabilityPercentage(itemStack) > Config.MAX_DAMAGE
						&& Config.MAX_DAMAGE != 0)
				{
					sender.sendMessage(ChatColor.DARK_AQUA + "Your "
							+ ChatColor.WHITE + item.name
							+ ChatColor.DARK_AQUA
							+ " is too damaged to add to stock!");
					sender.sendMessage(ChatColor.DARK_AQUA
							+ "Items must be damanged less than "
							+ ChatColor.WHITE + Config.MAX_DAMAGE
							+ "%");
					return true;
				}
			}else{
				item = Search.itemById(itemStack);
			}
			if(item == null)
			{
				sender.sendMessage("Could not find an item.");
				return false;
			}
			int amount = countItemsInInventory(player.getInventory(), item);
			return shopAdd(shop, item, amount);
		}
		
		//> /shop add (id) all
		// Add all items with the specified ID and 0 damage
		matcher.reset();
		pattern = Pattern.compile("(?i)add\\s+(\\d+)\\s+all$");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			ItemInfo item = Search.itemById(id);
			if(item == null)
			{
				sender.sendMessage("Could not find an item.");
				return false;
			}
			int count = countItemsInInventory(player.getInventory(), item);
			return shopAdd(shop, item, count);
		}
		
		//> /shop add (id):(dam) all
		// add all items with the specified ID and damage
		matcher.reset();
		pattern = Pattern.compile("(?i)add\\s+(\\d+):(\\d+)\\s+all$");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			short type = Short.parseShort(matcher.group(2));
			ItemInfo item = Search.itemById(id, type);
			if(item == null)
			{
				sender.sendMessage("Could not find an item.");
				return false;
			}
			int count = countItemsInInventory(player.getInventory(), item);
			return shopAdd(shop, item, count);
		}
		
		//> /shop add (name), ... all
		// add all items with the specified name
		matcher.reset();
		pattern = Pattern.compile("(?i)add\\s+(.*)\\s+all$");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			String itemName = matcher.group(1);
			ItemInfo item = Search.itemByName(itemName);
			if(item == null)
			{
				sender.sendMessage("Could not find an item.");
				return false;
			}
			int count = countItemsInInventory(player.getInventory(), item);
			return shopAdd(shop, item, count);
		}

		//> /shop add (id)
		// add 1 item with the specified ID
		pattern = Pattern.compile("(?i)add\\s+(\\d+)$");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			ItemInfo item = Search.itemById(id);
			if(item == null)
			{
				sender.sendMessage("Could not find an item.");
				return false;
			}
			return shopAdd(shop, item, 1);
		}

		//> /shop add (id) (amt)
		// Add specified amount of items with specified ID
		matcher.reset();
		pattern = Pattern.compile("(?i)add\\s+(\\d+)\\s+(\\d+)$");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			int count = Integer.parseInt(matcher.group(2));
			ItemInfo item = Search.itemById(id);
			if(item == null)
			{
				sender.sendMessage("Could not find an item.");
				return false;
			}
			return shopAdd(shop, item, count);
		}

		//> /shop add (id):(dam)
		// Add 1 item with specified ID and specified damage
		matcher.reset();
		pattern = Pattern.compile("(?i)add\\s+(\\d+):(\\d+)$");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			short type = Short.parseShort(matcher.group(2));
			ItemInfo item = Search.itemById(id, type);
			if(item == null)
			{
				sender.sendMessage("Could not find an item.");
				return false;
			}
			return shopAdd(shop, item, 1);
		}

		//> /shop add (id):(dam) (amt)
		// Add specified amount of items with specified id and damage
		matcher.reset();
		pattern = Pattern.compile("(?i)add\\s+(\\d+):(\\d+)\\s+(\\d+)$");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			short type = Short.parseShort(matcher.group(2));
			ItemInfo item = Search.itemById(id, type);
			int count = Integer.parseInt(matcher.group(3));
			if(item == null)
			{
				sender.sendMessage("Could not find an item.");
				return false;
			}
			return shopAdd(shop, item, count);
		}

		//> /shop add (name), ... (amt)
		// Add specified amount of items with specified name
		matcher.reset();
		pattern = Pattern.compile("(?i)add\\s+(.*)\\s+(\\d+)$");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			String itemName = matcher.group(1);
			ItemInfo item = Search.itemByName(itemName);
			int count = Integer.parseInt(matcher.group(2));
			if(item == null)
			{
				sender.sendMessage("Could not find an item.");
				return false;
			}
			return shopAdd(shop, item, count);
		}

		//> /shop add (name), ...
		// Add 1 item with specified name
		matcher.reset();
		pattern = Pattern.compile("(?i)add\\s+(.*)$");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			String itemName = matcher.group(1);
			ItemInfo item = Search.itemByName(itemName);
			if(item == null)
			{
				sender.sendMessage("Could not find an item.");
				return false;
			}
			return shopAdd(shop, item, 1);
		}

		// Show add help
		sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel
				+ " add [itemname] [number] " + ChatColor.DARK_AQUA
				+ "- Add this item from your inventory to the shop.");
		return true;
	}

	/**
	 * Stock some amount of an item into the shop from the player's inventory.
	 * @param shop
	 * the shop recieving the item(s)
	 * @param item
	 * the item type
	 * @param amount
	 * how many
	 * @return true, if successful
	 */
	private boolean shopAdd(long shop, ItemInfo item, int amount)
	{
		Player player = (Player)sender;
		String playerName = player.getName();

		if(!(sender instanceof Player))
		{
			sender.sendMessage("You need an inventory to add items to a shop");
			return false;
		}

		//Validate amount
		if(amount<0) return false;
		if(amount==0) return true;
		
		//Get basic info about shop
		boolean shopUnlimitedStock = false;
		String shopName = "";
		long itemStock = 0, stockId = -1;
		boolean itemNew = false;
		try{
			ResultSet resShop = CommandShops.db.query("SELECT name,unlimitedStock FROM shops WHERE id="+shop+" LIMIT 1");
			resShop.next();
			shopUnlimitedStock = resShop.getInt("unlimitedStock") == 1;
			shopName = resShop.getString("name");
			resShop.close();
			String itemQuery = String.format((Locale)null,"SELECT id,stock,buy,sell FROM shop_items WHERE "
					+ "	shop=%d AND itemid=%d AND	itemdamage=%d",
						shop,		item.typeId,	item.subTypeId);
			ResultSet resItem = CommandShops.db.query(itemQuery);
			if(resItem.next())
			{
				stockId = resItem.getLong("id");
				itemStock = resItem.getLong("stock");
				resItem.getDouble("buy");	boolean hasBuy = !resItem.wasNull();
				resItem.getDouble("sell");	boolean hasSell= !resItem.wasNull();
				if(!hasBuy && !hasSell) itemNew = true;
			}else{
				String insertQuery = String.format((Locale)null,"INSERT INTO shop_items"
						+ "(shop,	itemid,		itemdamage,		stock,	maxstock,	buy,	sell) VALUES"
						+ "(%d,		%d,			%d,				0,		10,			NULL,	NULL)"
						,	shop,	item.typeId,item.subTypeId);
						
				CommandShops.db.query(insertQuery);
				itemStock = 0;
				itemNew = true;
				String stockQuery = String.format((Locale)null,"SELECT MAX(id) FROM shop_items WHERE shop=%d AND itemid=%d AND itemdamage=%d"
												, shop, item.typeId, item.subTypeId);
				ResultSet resStock = db.query(stockQuery);
				resStock.next();
				stockId = resStock.getLong(1);
				resStock.close();
			}
			resItem.close();
		}catch(Exception e){
			log.warning(String.format((Locale)null,"[%s] Couldn't get shop info: %s", CommandShops.pdfFile.getName(), e));
			sender.sendMessage(ChatColor.DARK_AQUA + "Add cancelled due to DB error.");
			return false;
		}
		
		// Calculate number of items player has
		int playerItemCount = countItemsInInventory(player.getInventory(), item);
		// Validate Count
		if(playerItemCount < amount)
		{
			// Nag player
			sender.sendMessage(ChatColor.DARK_AQUA + "You only have "
					+ ChatColor.WHITE + playerItemCount
					+ ChatColor.DARK_AQUA
					+ " in your inventory that can be added.");
			return false;
		}

		if(shopUnlimitedStock)
		{
			sender.sendMessage(String.format((Locale)null,
					"%s has unlimited stock, no need to stock it!",
					shopName, item.name));
			return false;
		}

		long newStock = itemStock + amount;
		try{
			String addQuery = String.format((Locale)null,"UPDATE shop_items SET stock=stock+%d WHERE id=%d", amount, stockId);
			CommandShops.db.query(addQuery);
		}catch(Exception e){
			log.warning(String.format((Locale)null,"[%s] Couldn't add item to shop: %s", CommandShops.pdfFile.getName(), e));
			sender.sendMessage(ChatColor.DARK_AQUA + "Add cancelled due to DB error.");
			return false;
		}
		
		removeItemsFromInventory(player.getInventory(), item, amount);
		
		sender.sendMessage(ChatColor.DARK_AQUA + "Succesfully added "
				+ ChatColor.WHITE + item.name + ChatColor.DARK_AQUA
				+ " to the shop. Stock is now " + ChatColor.WHITE
				+ newStock);

		if(itemNew)
		{
			sender.sendMessage(ChatColor.DARK_AQUA + item.name
					+ " is almost ready to be purchased or sold!");
			sender.sendMessage(ChatColor.DARK_AQUA + "Use " + ChatColor.WHITE
					+ "\"/shop set sell " + item.name + " price\""
					+ ChatColor.DARK_AQUA + " to sell this item!");
			sender.sendMessage(ChatColor.DARK_AQUA + "Use " + ChatColor.WHITE
					+ "\"/shop set buy " + item.name + " price\""
					+ ChatColor.DARK_AQUA + " to  buy this item!");
		}

		// log the transaction
		log.info(String.format((Locale)null,"[%s] Add %d of %s to %s by %s",
				CommandShops.pdfFile.getName(), amount, item.name, shop, playerName));
		try{
			String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			String logQuery = String.format((Locale)null,"INSERT INTO log " 
				+"(	`datetime`,	`user`,					`shop`,	`action`,	`itemid`,	`itemdamage`,	`amount`,	`cost`,	`total`,`comment`) VALUES"
				+"(	'%s',		'%s',					%d,		'add',		%d,			%d,				%d,			NULL,	NULL,		NULL)"
				,	now,		db.escape(playerName),	shop,				item.typeId,item.subTypeId,	amount);
			CommandShops.db.query(logQuery);
		}catch(Exception e){
			log.warning(String.format((Locale)null,"[%s] Couldn't log transaction: %s",
					CommandShops.pdfFile.getName(), e));
		}
		return true;
	}
}
