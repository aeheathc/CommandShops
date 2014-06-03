package com.aehdev.commandshops.commands;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.aehdev.commandshops.CommandShops;
import com.aehdev.commandshops.ItemInfo;
import com.aehdev.commandshops.Search;
import com.aehdev.commandshops.Shop;

/**
 * Command for buying items from a shop!
 */
public class CommandShopBuy extends Command
{

	/**
	 * Create a new Buy order.
	 * @param plugin
	 * reference to the main CommandShops plugin object
	 * @param commandLabel
	 * the actual main command name
	 * @param sender
	 * the sender of the command, we will need this to be a player
	 * @param command
	 * the whole argument string, including the "buy" part but not the "/shop" part
	 */
	public CommandShopBuy(CommandShops plugin, String commandLabel,
			CommandSender sender, String command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/**
	 * Parse their buy command and attempt to execute the purchase using
	 * {@link #shopBuy}, first checking that:
	 * <ul><li>we can understand their command</li><li>they're in a shop</li>
	 * <li>they are a player and have permission</li></ul>
	 * @return true on success
	 */
	@Override
	public boolean process()
	{
		if(!(sender instanceof Player))
		{
			sender.sendMessage("You can only buy things if you have an inventory to put them in.");
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
		if(!canUseCommand(CommandTypes.SELL))
		{
			sender.sendMessage(CommandShops.CHAT_PREFIX
					+ ChatColor.DARK_AQUA
					+ "You don't have permission to use this command");
			return false;
		}
		
		//> /shop buy
		// buy 1 of the same item you are holding
		Pattern pattern = Pattern.compile("(?i)buy$");
		Matcher matcher = pattern.matcher(command);
		if(matcher.find())
		{
			ItemStack itemStack = player.getItemInHand();
			if(itemStack == null){ return false; }
			ItemInfo item = Search.itemById(itemStack);
			if(item == null)
			{
				sender.sendMessage("Could not find an item.");
				return true;
			}
			return shopBuy(shop, item, 1);
		}
		
		//> /shop buy all
		// buy as much as you can of the item you are holding
		matcher.reset();
		pattern = Pattern.compile("(?i)buy\\s+all$");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			ItemStack itemStack = player.getItemInHand();
			if(itemStack == null)
			{
				sender.sendMessage("You must be holding an item, or specify an item.");
				return true;
			}
			ItemInfo item = Search.itemById(itemStack);
			if(item == null)
			{
				sender.sendMessage("Could not find an item.");
				return true;
			}
			int count = countAvailableSpaceForItemInInventory(player.getInventory(), item);
			return shopBuy(shop, item, count);
		}
		
		//> /shop buy int all
		// buy as much as you can of the item with specified id
		matcher.reset();
		pattern = Pattern.compile("(?i)buy\\s+(\\d+)\\s+all$");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			ItemInfo item = Search.itemById(id);
			if(item == null)
			{
				sender.sendMessage("Could not find an item.");
				return true;
			}
			int count = countAvailableSpaceForItemInInventory(player.getInventory(), item);
			return shopBuy(shop, item, count);
		}
		
		//> /shop buy int:int all
		// buy as much as you can of the item with specified id and damage
		matcher.reset();
		pattern = Pattern.compile("(?i)buy\\s+(\\d+):(\\d+)\\s+all$");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			short type = Short.parseShort(matcher.group(2));
			ItemInfo item = Search.itemById(id, type);
			if(item == null)
			{
				sender.sendMessage("Could not find an item.");
				return true;
			}
			int count = countAvailableSpaceForItemInInventory(player.getInventory(), item);
			return shopBuy(shop, item, count);
		}
		
		//> /shop buy name, ... all
		// buy as much as you can of the item with the specified name
		matcher.reset();
		pattern = Pattern.compile("(?i)buy\\s+(.*)\\s+all$");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			String itemName = matcher.group(1);
			ItemInfo item = Search.itemByName(itemName);
			if(item == null)
			{
				sender.sendMessage("Could not find an item.");
				return true;
			}
			int count = countAvailableSpaceForItemInInventory(player.getInventory(), item);
			return shopBuy(shop, item, count);
		}

		//> /shop buy int
		// buy 1 of the item with the specified id
		pattern = Pattern.compile("(?i)buy\\s+(\\d+)$");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			ItemInfo item = Search.itemById(id);
			if(item == null)
			{
				sender.sendMessage("Could not find an item.");
				return true;
			}
			return shopBuy(shop, item, 1);
		}

		//> /shop buy int int
		// buy a specified amount of an item with the specified id
		matcher.reset();
		pattern = Pattern.compile("(?i)buy\\s+(\\d+)\\s+(\\d+)$");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			int count = Integer.parseInt(matcher.group(2));
			ItemInfo item = Search.itemById(id);
			if(item == null)
			{
				sender.sendMessage("Could not find an item.");
				return true;
			}
			return shopBuy(shop, item, count);
		}

		//> /shop buy int:int
		// buy 1 of the item with the specified id and damage
		matcher.reset();
		pattern = Pattern.compile("(?i)buy\\s+(\\d+):(\\d+)$");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			short type = Short.parseShort(matcher.group(2));
			ItemInfo item = Search.itemById(id, type);
			if(item == null)
			{
				sender.sendMessage("Could not find an item.");
				return true;
			}
			return shopBuy(shop, item, 1);
		}

		//> /shop buy int:int int
		// buy a specified anount of the item with the specified id and damage
		matcher.reset();
		pattern = Pattern.compile("(?i)buy\\s+(\\d+):(\\d+)\\s+(\\d+)$");
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
				return true;
			}
			return shopBuy(shop, item, count);
		}

		//> /shop buy name, ... int
		// buy a specified amount of the item with the specified name
		matcher.reset();
		pattern = Pattern.compile("(?i)buy\\s+(.*)\\s+(\\d+)$");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			String itemName = matcher.group(1);
			ItemInfo item = Search.itemByName(itemName);
			int count = Integer.parseInt(matcher.group(2));
			if(item == null)
			{
				sender.sendMessage("Could not find an item.");
				return true;
			}
			return shopBuy(shop, item, count);
		}

		//> /shop buy name, ...
		// buy 1 of the item with the specified name
		matcher.reset();
		pattern = Pattern.compile("(?i)buy\\s+(.*)$");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			String itemName = matcher.group(1);
			ItemInfo item = Search.itemByName(itemName);
			if(item == null)
			{
				sender.sendMessage("Could not find an item.");
				return true;
			}
			return shopBuy(shop, item, 1);
		}

		// Show buy help
		sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel
				+ " buy [itemname] [number] " + ChatColor.DARK_AQUA
				+ "- Buy an item.");
		return true;
	}

	/**
	 * Execute a purchase, stock/space/cash permitting.
	 * @param shop
	 * the shop from which the item is being bought
	 * @param item
	 * the item type requested
	 * @param amount
	 * the requested quantity of the item
	 * @return true on success
	 */
	private boolean shopBuy(long shop, ItemInfo item, int amount)
	{
		if(!(sender instanceof Player))
		{
			sender.sendMessage("You can only buy things if you have an inventory to put them in.");
			return false;
		}
		Player player = (Player)sender;
		String playerName = player.getName();
		
		if(amount < 1)
		{
			player.sendMessage(ChatColor.DARK_AQUA + "Can't buy less than 1 item.");
			return false;
		}
		
		//Get info about shop and item
		boolean shopUnlimitedStock = false, shopUnlimitedMoney = false;;
		String shopName = "", shopOwner = "";
		double sell = -1;
		int stock = 0, stockId = -1;
		try{
			ResultSet resShop = CommandShops.db.query("SELECT name,owner,unlimitedStock,unlimitedMoney FROM shops WHERE id="+shop+" LIMIT 1");
			resShop.next();
			shopName = resShop.getString("name");
			shopOwner = resShop.getString("owner");
			shopUnlimitedStock = resShop.getInt("unlimitedStock") == 1;
			shopUnlimitedMoney = resShop.getInt("unlimitedMoney") == 1;
			resShop.close();
			String itemQuery = String.format((Locale)null,"SELECT id,stock,sell FROM shop_items WHERE"
								+ "	shop=%d AND	itemid=%d AND	itemdamage=%d	LIMIT 1"
								,	shop,		item.typeId,	item.subTypeId);
			ResultSet resItem = CommandShops.db.query(itemQuery);
			if(resItem.next())
			{
				stockId = resItem.getInt("id");
				stock = (int)Math.floor(resItem.getDouble("stock"));
				sell = resItem.getDouble("sell");
				if(resItem.wasNull()) sell = -1;
			}
			resItem.close();
		}catch(Exception e){
			log.warning(String.format((Locale)null,"[%s] Couldn't get shop info: %s", CommandShops.pdfFile.getName(), e));
			sender.sendMessage(ChatColor.DARK_AQUA + "Buy cancelled due to DB error.");
			return false;
		}
		OfflinePlayer shopOwnerPlayer = Bukkit.getServer().getOfflinePlayer(shopOwner);

		// check if the shop is selling that item
		if(sell == -1 && !isShopController(shop))
		{
			player.sendMessage(ChatColor.DARK_AQUA + "Sorry, "
					+ ChatColor.WHITE + shopName + ChatColor.DARK_AQUA
					+ " is not selling " + ChatColor.WHITE + item.name
					+ ChatColor.DARK_AQUA + " right now.");
			return false;
		}else if(stock < 1 && !shopUnlimitedStock){
			player.sendMessage(ChatColor.DARK_AQUA + "Sorry, "
					+ ChatColor.WHITE + shopName + ChatColor.DARK_AQUA
					+ " is sold out of " + ChatColor.WHITE + item.name
					+ ChatColor.DARK_AQUA + " right now.");
			return false;
		}

		//Limit buy amount by shop's stock
		if(amount > stock && !shopUnlimitedStock)
		{
			amount = stock;
				player.sendMessage(ChatColor.DARK_AQUA + "The shop only has "
						+ ChatColor.WHITE + stock + " " + item.name);
		}

		//Limit buy amount by inventory space
		int freeSpots = countAvailableSpaceForItemInInventory(player.getInventory(), item);
		if(amount > freeSpots)
		{
			amount = freeSpots;
			player.sendMessage(ChatColor.DARK_AQUA + "You only have room for "
					+ ChatColor.WHITE + amount);
		}
		
		//Limit buy amount by cash on hand
		double balance = plugin.econ.getBalance(player);
		int amtCanAfford = (sell > 0) ? (int)Math.floor(balance/sell) : Integer.MAX_VALUE;
		if(amount > amtCanAfford && !isShopController(shop))
		{
			amount = amtCanAfford;
			player.sendMessage(ChatColor.DARK_AQUA + "You only have the money for "
					+ ChatColor.WHITE + amount);
		}

		double totalCost = amount * sell;

		// move the money
		if(!isShopController(shop))
		{
			//first pay the shop owner. Skip this step for unlimited money shops.
			if(!shopUnlimitedMoney)
			{
				if(!plugin.econ.depositPlayer(shopOwnerPlayer, totalCost).transactionSuccess())
				{
					player.sendMessage(CommandShops.CHAT_PREFIX
							+ ChatColor.DARK_AQUA
							+ "Vault error crediting shop owner: could not complete sale.");
					log.warning(String.format((Locale)null,"[%s] Couldn't pay shop owner %s. (Ending state OK)", CommandShops.pdfFile.getName(), shopOwner));
					return false;
				}
			}
			
			//then charge the buyer.
			if(!plugin.econ.withdrawPlayer(player, totalCost).transactionSuccess())
			{
				player.sendMessage(CommandShops.CHAT_PREFIX
						+ ChatColor.DARK_AQUA
						+ "Vault error paying for items: could not complete sale.");
				log.warning(String.format((Locale)null,"[%s] Couldn't charge buyer %s. Attempting rollback of seller credit...", CommandShops.pdfFile.getName(), playerName));
				// if we can't charge the buyer, try to rollback the transaction
				if(!plugin.econ.withdrawPlayer(shopOwnerPlayer, totalCost).transactionSuccess())
				{
					log.warning(String.format((Locale)null,"[%s] Couldn't rollback failed transaction. %s likely has an extra %s !", CommandShops.pdfFile.getName(), shopOwner, plugin.econ.format(totalCost)));
				}else{
					log.warning(String.format((Locale)null,"[%s] Rolled back failed transaction with %s. (Ending state OK)", CommandShops.pdfFile.getName(), shopOwner));
				}
				return false;
			}
		}else{
			totalCost = 0;
		}
		
		//give items to player
		givePlayerItem(item, amount);

		//remove items from shop
		if(!shopUnlimitedStock)
		{
			try{
				String removeQuery = String.format((Locale)null,"UPDATE shop_items SET `stock`=(`stock`-%d) WHERE id=%d LIMIT 1",
																						amount,		stockId);
				CommandShops.db.query(removeQuery);
			}catch(Exception e){
				//worst possible time to have an error because now we have to roll back everything
				log.warning(String.format((Locale)null,"[%s] Couldn't remove items from shop: %s. Rolling back buy transaction...", CommandShops.pdfFile.getName(), e));
				sender.sendMessage(ChatColor.DARK_AQUA + "Buy cancelled due to DB error.");
				//refund buyer
				if(!plugin.econ.depositPlayer(player, totalCost).transactionSuccess())
				{
					log.warning(String.format((Locale)null,"[%s] Couldn't rollback buy: Items were delivered and money transferred, but duplicate item remains in shop %s. ", CommandShops.pdfFile.getName(), shopName));
					return false;
				}
				//reclaim money from shop owner
				if(!plugin.econ.withdrawPlayer(shopOwnerPlayer, totalCost).transactionSuccess())
				{
					log.warning(String.format((Locale)null,"[%s] Couldn't rollback buy: Items were delivered and money recieved by shop, but duplicate item remains in shop %s and %s likely has an extra %s ", CommandShops.pdfFile.getName(), shopName, shopOwner, plugin.econ.format(totalCost)));
					return false;
				}
				//take back items
				removeItemsFromInventory(player.getInventory(), item, amount);
				log.warning(String.format((Locale)null,"[%s] Rolled back failed buy from %s. (Ending state OK)", CommandShops.pdfFile.getName(), shopName));
				return false;
			}
		}
		
		if(isShopController(shop))
		{
			player.sendMessage(ChatColor.DARK_AQUA + "You removed "
					+ ChatColor.WHITE + amount + " " + item.name
					+ ChatColor.DARK_AQUA + " from the shop");
		}else{
			player.sendMessage(ChatColor.DARK_AQUA + "You purchased "
					+ ChatColor.WHITE + amount + " " + item.name
					+ ChatColor.DARK_AQUA + " for " + ChatColor.WHITE
					+ plugin.econ.format(totalCost));
		}

		// log the transaction
		int newStock = stock - amount;
		String newstockStr = shopUnlimitedStock ? "Unlimited" : (""+newStock);
		log.info(String.format((Locale)null,"[%s] %s bought %d of %s from %d (%s) for %s; shop's stock is %s",
				CommandShops.pdfFile.getName(), playerName, amount, item.name, shop, shopName, plugin.econ.format(totalCost), newstockStr));
		try{
			String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			String logQuery = String.format((Locale)null,"INSERT INTO log " 
				+"(	`datetime`,	`user`,					`shop`,	`action`,	`itemid`,	`itemdamage`,	`amount`,	`cost`,	`total`,	`comment`) VALUES"
				+"(	'%s',		'%s',					%d,		'buy',		%d,			%d,				%d,			%f,		%f,			%s)"
				,	now,		db.escape(playerName),	shop,				item.typeId,item.subTypeId,	amount,		sell,	totalCost,	isShopController(shop)?"'Own shop; no cost.'":"NULL");
			CommandShops.db.query(logQuery);
		}catch(Exception e){
			log.warning(String.format((Locale)null,"[%s] Couldn't log transaction: %s",
					CommandShops.pdfFile.getName(), e));
		}
		return true;
	}
}
