package com.aehdev.commandshops.commands;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.aehdev.commandshops.CommandShops;
import com.aehdev.commandshops.Config;
import com.aehdev.commandshops.ItemInfo;
import com.aehdev.commandshops.Search;
import com.aehdev.commandshops.Shop;

/**
 * Command with which shop owners set many parameters of the shop.
 */
public class CommandShopSet extends Command
{

	/**
	 * Create a new Set order.
	 * @param plugin
	 * reference to the main CommandShops plugin object
	 * @param commandLabel
	 * command name/alias
	 * @param sender
	 * who sent the command
	 * @param command
	 * command string with arguments
	 */
	public CommandShopSet(CommandShops plugin, String commandLabel,
			CommandSender sender, String command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/**
	 * Determine what the player is trying to set and run the appropriate subcommand.
	 */
	public boolean process()
	{
		if(!(sender instanceof Player))
		{
			sender.sendMessage("You are not in a shop!");
			return false;
		}
		// Check Permissions
		if(!canUseCommand(CommandTypes.SET))
		{
			sender.sendMessage(CommandShops.CHAT_PREFIX + ChatColor.DARK_AQUA
					+ "You don't have permission to use this command");
			return false;
		}
		
		Player player = (Player)sender;
		long shop = Shop.getCurrentShop(player);

		if(shop == -1)
		{
			sender.sendMessage("You are not in a shop!");
			return false;
		}

		// Check if Player can Modify
		if(!isShopController(shop) && !canUseCommand(CommandTypes.ADMIN))
		{
			player.sendMessage(ChatColor.DARK_AQUA
					+ "You must be the shop owner or a manager to set things.");
			return false;
		}

		if(Config.DEBUG)
			log.info(String.format("[%s] Command issued: %s", CommandShops.pdfFile.getName(), command));

		// Parse Arguments
		if(command.matches("(?i)set\\s+sell.*"))
		{
			return shopSetSell(shop);
		}else if(command.matches("(?i)set\\s+buy.*")){
			return shopSetBuy(shop);
		}else if(command.matches("(?i)set\\s+max.*")){
			return shopSetMax(shop);
		}else if(command.matches("(?i)set\\s+unlimited.*")){
			return shopSetUnlimited(shop);
		}else if(command.matches("(?i)set\\s+manager.*")){
			return shopSetManager(shop);
		}else if(command.matches("(?i)set\\s+minbalance.*")){
			return shopSetMinBalance(shop);
		}else if(command.matches("(?i)set\\s+notification.*")){
			return shopSetNotification(shop);
		}else if(command.matches("(?i)set\\s+owner.*")){
			return shopSetOwner(shop);
		}else if(command.matches("(?i)set\\s+name.*")){
			return shopSetName(shop);
		}else{
			return shopSetHelp();
		}
	}

	/**
	 * Parse the command for setting the buy price of an item.
	 * @param shop the id of the shop to modify
	 * @return true, if successful
	 */
	private boolean shopSetBuy(long shop)
	{
		// Command matching
		Pattern pattern;
		Matcher matcher;

		//> /shop set buy int double
		// set buy price of item with specified id
		pattern = Pattern.compile("(?i)set\\s+buy\\s+(\\d+)\\s+(" + DECIMAL_REGEX + ")");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			ItemInfo item = Search.itemById(id);
			double price = Double.parseDouble(matcher.group(2));
			return shopSetBuy(shop, item, price);
		}
		
		//> /shop set buy int
		// NULL buy price of item with specified id
		pattern = Pattern.compile("(?i)set\\s+buy\\s+(\\d+)");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			ItemInfo item = Search.itemById(id);
			return shopSetBuy(shop, item, null);
		}

		//> /shop set buy int:int double
		// set buy price of item with specified id and damage
		matcher.reset();
		pattern = Pattern.compile("(?i)set\\s+buy\\s+(\\d+):(\\d+)\\s+(" + DECIMAL_REGEX + ")");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			short type = Short.parseShort(matcher.group(2));
			ItemInfo item = Search.itemById(id, type);
			double price = Double.parseDouble(matcher.group(3));
			return shopSetBuy(shop, item, price);
		}
		
		//> /shop set buy int:int
		// NULL buy price of item with specified id and damage
		matcher.reset();
		pattern = Pattern.compile("(?i)set\\s+buy\\s+(\\d+):(\\d+)");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			short type = Short.parseShort(matcher.group(2));
			ItemInfo item = Search.itemById(id, type);
			return shopSetBuy(shop, item, null);
		}

		//> /shop set buy (chars) double
		// set buy price of named item
		matcher.reset();
		pattern = Pattern.compile("(?i)set\\s+buy\\s+(.*)\\s+(" + DECIMAL_REGEX + ")");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			String name = matcher.group(1);
			ItemInfo item = Search.itemByName(name);
			double price = Double.parseDouble(matcher.group(2));
			return shopSetBuy(shop, item, price);
		}
		
		//> /shop set buy (chars)
		// NULL buy price of named item
		matcher.reset();
		pattern = Pattern.compile("(?i)set\\s+buy\\s+(.*)");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			String name = matcher.group(1);
			ItemInfo item = Search.itemByName(name);
			return shopSetBuy(shop, item, null);
		}

		// show set buy usage
		sender.sendMessage("   " + "/" + commandLabel
				+ " set buy [item name] <price>");
		return true;
	}

	/**
	 * Set the buying price of an item.
	 * "Buy" is from the shop's perspective.
	 * @param shop
	 * id of the shop to modify
	 * @param item
	 * identify the item type to buy
	 * @param price
	 * price per item
	 * @return true, if successful
	 */
	private boolean shopSetBuy(long shop, ItemInfo item, Double price)
	{
		if(item == null)
		{
			sender.sendMessage("Item was not found.");
			return true;
		}
		
		if(price != null && price < 0)
		{
			sender.sendMessage("Cannot set negative buy price!");
			return false;
		}
		
		String playerName = ((Player)sender).getName();
		
		try{
			//check if there's any entry for this shop+item, and if so, what's the current sell price
			Double sell = null;
			String sellQuery = String.format("SELECT sell FROM shop_items WHERE shop=%d AND itemid=%d AND itemdamage=%d LIMIT 1"
											, shop, item.typeId, item.subTypeId);
			ResultSet resSell = CommandShops.db.query(sellQuery);
			if(!resSell.next())
			{
				//if not, see if we were setting or nulling the buy price
				resSell.close();
				if(price == null)
				{
					//if we just wanted to null the buy price, there's already nothing, so nothing to do
					sender.sendMessage(ChatColor.DARK_AQUA + "Shop already doesn't buy " + ChatColor.WHITE + item.name);
					return true;
				}
				//if we want to set a buy price, make a new entry with the buy price
				String insQuery = String.format("INSERT INTO shop_items("
						+ "	shop,	itemid,			itemdamage,		stock,	sell,	buy) VALUES("
						+ "	%d,		%d,				%d,				0,		NULL,	%f)"
						,	shop,	item.typeId,	item.subTypeId,					price.doubleValue());
				CommandShops.db.query(insQuery);
			}else{
				//There is an entry!
				sell = resSell.getDouble("sell");
				if(resSell.wasNull()) sell = null;
				resSell.close();
				if(price == null)
				{
					//If we're nulling the price, update the current entry with null
					String nullQuery = String.format("UPDATE shop_items SET buy=NULL WHERE shop=%d AND itemid=%d AND itemdamage=%d LIMIT 1"
											, shop, item.typeId, item.subTypeId);
					CommandShops.db.query(nullQuery);
				}else{
					//if setting a buy price, make sure they're not setting it higher than sell price
					if(sell != null && price > sell)
					{
						sender.sendMessage("Cannot set buy price greater than sell price!");
						return false;
					}
					//update the current entry with new buy price
					String setBuyQuery = String.format("UPDATE shop_items SET buy=%f WHERE shop=%d AND itemid=%d AND itemdamage=%d LIMIT 1"
							, price, shop, item.typeId, item.subTypeId);
					CommandShops.db.query(setBuyQuery);
				}
			}
			
		}catch(Exception e){
			sender.sendMessage(ChatColor.DARK_AQUA + "SetBuy cancelled due to DB error");
			log.warning(String.format("[%s] Couldn't get shop info: %s", CommandShops.pdfFile.getName(), e));
			return false;
		}

		// Send Result
		sender.sendMessage(ChatColor.WHITE + item.name + ChatColor.DARK_AQUA +
				(price == null ? " is no longer purchased."
							: " is now purchased for " + ChatColor.WHITE + plugin.econ.format(price)));
		//log
		log.info(String.format("[%s] %s set buy price of %s to %s in shop %d",
				CommandShops.pdfFile.getName(), playerName, item.name, (price == null ? "NULL" : plugin.econ.format(price)), shop));
		try{
			String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			String logQuery = String.format("INSERT INTO log  " 
				+"(	`datetime`,	`user`,					`shop`,	`action`,	`itemid`,	`itemdamage`,	`amount`,	`cost`,	`total`,	`comment`) VALUES"
				+"(	'%s',		'%s',					%d,		'setBuy',	%d,			%d,				NULL,		%f,		NULL,		NULL)"
				,	now,		db.escape(playerName),	shop,				item.typeId,item.subTypeId,				price);
			CommandShops.db.query(logQuery);
		}catch(Exception e){
			log.warning(String.format("[%s] Couldn't log transaction: %s",
					CommandShops.pdfFile.getName(), e));
		}
		return true;
	}


	/**
	 * Parse the command for setting the sell price of an item.
	 * @param shop the id of the shop to modify
	 * @return true, if successful
	 */
	private boolean shopSetSell(long shop)
	{
		// Command matching
		Pattern pattern;
		Matcher matcher;

		//> /shop set sell int double
		// set sell price of item with specified id
		pattern = Pattern.compile("(?i)set\\s+sell\\s+(\\d+)\\s+(" + DECIMAL_REGEX + ")");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			ItemInfo item = Search.itemById(id);
			double price = Double.parseDouble(matcher.group(2));
			return shopSetSell(shop, item, price);
		}
		
		//> /shop set sell int
		// NULL sell price of item with specified id
		pattern = Pattern.compile("(?i)set\\s+sell\\s+(\\d+)");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			ItemInfo item = Search.itemById(id);
			return shopSetSell(shop, item, null);
		}

		//> /shop set sell int:int double
		// set sell price of item with specified id and damage
		matcher.reset();
		pattern = Pattern.compile("(?i)set\\s+sell\\s+(\\d+):(\\d+)\\s+(" + DECIMAL_REGEX + ")");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			short type = Short.parseShort(matcher.group(2));
			ItemInfo item = Search.itemById(id, type);
			double price = Double.parseDouble(matcher.group(3));
			return shopSetSell(shop, item, price);
		}
		
		//> /shop set sell int:int
		// NULL sell price of item with specified id and damage
		matcher.reset();
		pattern = Pattern.compile("(?i)set\\s+sell\\s+(\\d+):(\\d+)");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			short type = Short.parseShort(matcher.group(2));
			ItemInfo item = Search.itemById(id, type);
			return shopSetSell(shop, item, null);
		}

		//> /shop set sell (chars) double
		// set sell price of named item
		matcher.reset();
		pattern = Pattern.compile("(?i)set\\s+sell\\s+(.*)\\s+(" + DECIMAL_REGEX + ")");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			String name = matcher.group(1);
			ItemInfo item = Search.itemByName(name);
			double price = Double.parseDouble(matcher.group(2));
			return shopSetSell(shop, item, price);
		}
		
		//> /shop set sell (chars)
		// NULL sell price of named item
		matcher.reset();
		pattern = Pattern.compile("(?i)set\\s+sell\\s+(.*)");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			String name = matcher.group(1);
			ItemInfo item = Search.itemByName(name);
			return shopSetSell(shop, item, null);
		}

		// show set buy usage
		sender.sendMessage("   " + "/" + commandLabel
				+ " set sell [item name] <price>");
		return true;
	}

	/**
	 * Set the sell price of an item.
	 * "sell" is from the shop's perspective.
	 * @param shop
	 * id of the shop to modify
	 * @param item
	 * identify the item type to sell
	 * @param price
	 * price per item
	 * @return true, if successful
	 */
	private boolean shopSetSell(long shop, ItemInfo item, Double price)
	{
		if(item == null)
		{
			sender.sendMessage("Item was not found.");
			return true;
		}
		
		if(price != null && price < 0)
		{
			sender.sendMessage("Cannot set negative sell price!");
			return false;
		}
		
		String playerName = ((Player)sender).getName();
		
		try{
			//check if there's any entry for this shop+item, and if so, what's the current buy price
			Double buy = null;
			String buyQuery = String.format("SELECT buy FROM shop_items WHERE shop=%d AND itemid=%d AND itemdamage=%d LIMIT 1"
											, shop, item.typeId, item.subTypeId);
			ResultSet resBuy = CommandShops.db.query(buyQuery);
			if(!resBuy.next())
			{
				//if not, see if we were setting or nulling the sell price
				resBuy.close();
				if(price == null)
				{
					//if we just wanted to null the sell price, there's already nothing, so nothing to do
					sender.sendMessage(ChatColor.DARK_AQUA + "Shop already doesn't sell " + ChatColor.WHITE + item.name);
					return true;
				}
				//if we want to set a sell price, make a new entry with the sell price
				String insQuery = String.format("INSERT INTO shop_items("
						+ "	shop,	itemid,			itemdamage,		stock,	sell,	buy) VALUES("
						+ "	%d,		%d,				%d,				0,		%f,		NULL)"
						,	shop,	item.typeId,	item.subTypeId,					price.doubleValue());
				CommandShops.db.query(insQuery);
			}else{
				//There is an entry!
				buy = resBuy.getDouble("buy");
				if(resBuy.wasNull()) buy = null;
				resBuy.close();
				if(price == null)
				{
					//If we're nulling the price, update the current entry with null
					String nullQuery = String.format("UPDATE shop_items SET sell=NULL WHERE shop=%d AND itemid=%d AND itemdamage=%d LIMIT 1"
											, shop, item.typeId, item.subTypeId);
					CommandShops.db.query(nullQuery);
				}else{
					//if setting a sell price, make sure they're not setting it lower than buy price
					if(buy != null && price < buy)
					{
						sender.sendMessage("Cannot set sell price lower than buy price!");
						return false;
					}
					//update the current entry with new sell price
					String setSellQuery = String.format("UPDATE shop_items SET sell=%f WHERE shop=%d AND itemid=%d AND itemdamage=%d LIMIT 1"
							, price, shop, item.typeId, item.subTypeId);
					CommandShops.db.query(setSellQuery);
				}
			}
			
		}catch(Exception e){
			sender.sendMessage(ChatColor.DARK_AQUA + "SetSell cancelled due to DB error");
			log.warning(String.format("[%s] Couldn't get shop info: %s", CommandShops.pdfFile.getName(), e));
			return false;
		}

		// Send Result
		sender.sendMessage(ChatColor.WHITE + item.name + ChatColor.DARK_AQUA +
				(price == null ? " is no longer sold."
							: " is now sold for " + ChatColor.WHITE + plugin.econ.format(price)));
		//log
		log.info(String.format("[%s] %s set sell price of %s to %s in shop %d",
				CommandShops.pdfFile.getName(), playerName, item.name, (price == null ? "NULL" : plugin.econ.format(price)), shop));
		try{
			String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			String logQuery = String.format("INSERT INTO log " 
				+"(	`datetime`,	`user`,					`shop`,	`action`,	`itemid`,	`itemdamage`,	`amount`,	`cost`,	`total`,	`comment`) VALUES"
				+"(	'%s',		'%s',					%d,		'setSell',	%d,			%d,				NULL,		%f,		NULL,		NULL)"
				,	now,		db.escape(playerName),	shop,				item.typeId,item.subTypeId,				price);
			CommandShops.db.query(logQuery);
		}catch(Exception e){
			log.warning(String.format("[%s] Couldn't log transaction: %s",
					CommandShops.pdfFile.getName(), e));
		}
		return true;
	}


	/**
	 * Parse the command for setting the maximum stock of an item.
	 * @param shop the id of the shop to modify
	 * @return true, if successful
	 */
	private boolean shopSetMax(long shop)
	{
		//> /shop set max int int
		// set max stock of item with given id
		Pattern pattern = Pattern.compile("(?i)set\\s+max\\s+(\\d+)\\s+(\\d+)");
		Matcher matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			ItemInfo item = Search.itemById(id);
			int max = Integer.parseInt(matcher.group(2));
			return shopSetMax(shop, item, max);
		}

		//> /shop set max int:int int
		// set max stock of item with given id and damage
		matcher.reset();
		pattern = Pattern.compile("(?i)set\\s+max\\s+(\\d+):(\\d+)\\s+(\\d+)");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			short type = Short.parseShort(matcher.group(2));
			ItemInfo item = Search.itemById(id, type);
			int max = Integer.parseInt(matcher.group(3));
			return shopSetMax(shop, item, max);
		}

		//> /shop set max chars int
		// set max stock of item with given name
		matcher.reset();
		pattern = Pattern.compile("(?i)set\\s+max\\s+(.*)\\s+(\\d+)");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			String name = matcher.group(1);
			ItemInfo item = Search.itemByName(name);
			int max = Integer.parseInt(matcher.group(2));
			return shopSetMax(shop, item, max);
		}

		// show set max usage
		sender.sendMessage("   /shop set max [item name] [max number]");
		return true;
	}

	/**
	 * Set the maximum stock of an item.
	 * @param shop
	 * id of the shop to modify
	 * @param item
	 * identifies the item type to limit
	 * @param max
	 * the new maximum stock
	 * @return true, if successful
	 */
	private boolean shopSetMax(long shop, ItemInfo item, int max)
	{
		if(item == null)
		{
			sender.sendMessage("Item was not found.");
			return false;
		}

		// Check negative values
		if(max < 0)
		{
			sender.sendMessage("Can't set negative max");
			return false;
		}
		
		String playerName = ((Player)sender).getName();

		try{
			String checkQuery = String.format("SELECT id FROM shop_items WHERE shop=%d AND itemid=%d AND itemdamage=%d LIMIT 1"
								, shop, item.typeId, item.subTypeId);
			ResultSet resCheck = CommandShops.db.query(checkQuery);
			boolean exists = resCheck.next();
			int stockId = -1;
			if(exists) stockId = resCheck.getInt("id");
			resCheck.close();
			if(exists)
			{
				String maxQuery = String.format("UPDATE shop_items SET maxstock=%d WHERE id=%d LIMIT 1"
												, max, stockId);
				CommandShops.db.query(maxQuery);
			}else{
				String addQuery = String.format("INSERT INTO shop_items("
						+ "	shop,	itemid,			itemdamage,		stock,	maxstock,	buy,	sell) VALUES("
						+ "	%d,		%d,				%d,				0,		%d,			NULL,	NULL)"
						,	shop,	item.typeId,	item.subTypeId,			max);
				CommandShops.db.query(addQuery);
			}
			//log
			sender.sendMessage(item.name + " maximum stock is now " + max);
			log.info(String.format("[%s] %s set max stock of %s in shop %d to %d",
					CommandShops.pdfFile.getName(), playerName, item.name, shop, max));
			String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			String logQuery = String.format("INSERT INTO log " 
				+"(	`datetime`,	`user`,					`shop`,	`action`,	`itemid`,	`itemdamage`,	`amount`,	`cost`,	`total`,	`comment`) VALUES"
				+"(	'%s',		'%s',					%d,		'setMax',	%d,			%d,				%d,			NULL,	NULL,		NULL)"
				,	now,		db.escape(playerName),	shop,				item.typeId,item.subTypeId,	max);
			CommandShops.db.query(logQuery);
		}catch(Exception e){
			log.warning(String.format("[%s] Couldn't get shop info: %s", CommandShops.pdfFile.getName(), e));
			sender.sendMessage(ChatColor.DARK_AQUA + "Setmax cancelled due to DB error.");
			return false;
		}
		return true;
	}

	/**
	 * Toggle the Unlimited attributes for the shop.
	 * @param shop the id of the shop to modify 
	 * @return true, if successful
	 */
	private boolean shopSetUnlimited(long shop)
	{
		Player player = (Player)sender;
		String playerName = player.getName();
			
		// Check Permissions
		if(!canUseCommand(CommandTypes.ADMIN))
		{
			player.sendMessage(CommandShops.CHAT_PREFIX
					+ ChatColor.DARK_AQUA
					+ "You must be a shop admin to do this.");
			return true;
		}

		// shop set unlimited money
		Pattern pattern = Pattern.compile("(?i)set\\s+unlimited\\s+money");
		Matcher matcher = pattern.matcher(command);
		if(matcher.find())
		{
			try{
				String getQuery = "SELECT unlimitedMoney FROM shops WHERE id=" + shop + " LIMIT 1";
				ResultSet resGet = CommandShops.db.query(getQuery);
				resGet.next();
				boolean unlimitedMoney = resGet.getInt("unlimitedMoney") == 0;
				resGet.close();
				String setQuery = String.format("UPDATE shops SET unlimitedMoney=%d WHERE id=%d LIMIT 1"
									, unlimitedMoney ? 1 : 0, shop);
				CommandShops.db.query(setQuery);
				sender.sendMessage(CommandShops.CHAT_PREFIX + ChatColor.DARK_AQUA
						+ "Unlimited money was set to " + ChatColor.WHITE
						+ (unlimitedMoney ? "true" : "false"));
				String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
				String logQuery = String.format("INSERT INTO log " 
					+"(	`datetime`,	`user`,					`shop`,	`action`,				`itemid`,	`itemdamage`,	`amount`,	`cost`,			`total`,`comment`) VALUES"
					+"(	'%s',		'%s',					%d,		'setUnlimitedMoney',	NULL,		NULL,			NULL,		NULL,			NULL,	'%s')"
					,	now,		db.escape(playerName),	shop,																							unlimitedMoney?"true":"false");
				CommandShops.db.query(logQuery);
				log.info(String.format("[%s] Player %s set unlimited money to %s in shop %d",
						CommandShops.pdfFile.getName(), playerName, (unlimitedMoney ? "true" : "false"), shop));
			}catch(Exception e){
				sender.sendMessage("DB error during money toggle.");
				log.warning(String.format("[%s] DB error toggling money: %s", CommandShops.pdfFile.getName(), e));
				return false;
			}
			return true;
		}

		// shop set unlimited stock
		matcher.reset();
		pattern = Pattern.compile("(?i)set\\s+unlimited\\s+stock");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			try{
				String getQuery = "SELECT unlimitedStock FROM shops WHERE id=" + shop + " LIMIT 1";
				ResultSet resGet = CommandShops.db.query(getQuery);
				resGet.next();
				boolean unlimitedStock = resGet.getInt("unlimitedStock") == 0;
				resGet.close();
				String setQuery = String.format("UPDATE shops SET unlimitedStock=%d WHERE id=%d LIMIT 1"
									, unlimitedStock ? 1 : 0, shop);
				CommandShops.db.query(setQuery);
				sender.sendMessage(CommandShops.CHAT_PREFIX + ChatColor.DARK_AQUA
						+ "Unlimited stock was set to " + ChatColor.WHITE
						+ (unlimitedStock ? "true" : "false"));
				String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
				String logQuery = String.format("INSERT INTO log " 
					+"(	`datetime`,	`user`,					`shop`,	`action`,				`itemid`,	`itemdamage`,	`amount`,	`cost`,			`total`,`comment`) VALUES"
					+"(	'%s',		'%s',					%d,		'setUnlimitedStock',	NULL,		NULL,			NULL,		NULL,			NULL,	'%s')"
					,	now,		db.escape(playerName),	shop,																							unlimitedStock?"true":"false");
				CommandShops.db.query(logQuery);
				log.info(String.format("[%s] Player %s set unlimited stock to %s in shop %d",
						CommandShops.pdfFile.getName(), playerName, (unlimitedStock ? "true" : "false"), shop));
			}catch(Exception e){
				sender.sendMessage("DB error during stock toggle.");
				log.warning(String.format("[%s] DB error toggling money: %s", CommandShops.pdfFile.getName(), e));
				return false;
			}			
			return true;
		}

		// show set buy usage
		sender.sendMessage("   /shop set unlimited money");
		sender.sendMessage("   /shop set unlimited stock");
		return true;
	}

	/**
	 * Manage the set of managers for this shop.
	 * @param shop the id of the shop to modify
	 * @return true, if successful
	 */
	private boolean shopSetManager(long shop)
	{
		String playerName = ((Player)sender).getName();
		// set manager +name -name ...
		Pattern pattern = Pattern.compile("(?i)set\\s+manager\\s+(.*)");
		Matcher matcher = pattern.matcher(command);
		if(matcher.find())
		{
			String names = matcher.group(1);
			String[] args = names.split(" ");

			for(int i = 0; i < args.length; i++)
			{
				String arg = args[i];
				if(arg.matches("\\+.*"))
				{
					String manager = arg.replaceFirst("\\+", "");
					// add manager
					try{
						String manCheck = String.format("SELECT COUNT(*) FROM managers WHERE shop=%d AND manager='%s'"
													, shop, manager);
						ResultSet resCheck = CommandShops.db.query(manCheck);
						resCheck.next();
						int dup = resCheck.getInt(1);
						resCheck.close();
						if(dup > 0)
						{
							sender.sendMessage(ChatColor.DARK_AQUA + "That player is already a manager here.");
							continue;
						}
						CommandShops.db.query(String.format("INSERT INTO managers (shop,manager) VALUES(%d,'%s')"
											, shop, manager));
						sender.sendMessage(ChatColor.DARK_AQUA + "Added manager " + ChatColor.WHITE + manager);
						String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
						String logQuery = String.format("INSERT INTO log " 
							+"(	`datetime`,	`user`,					`shop`,	`action`,		`itemid`,	`itemdamage`,	`amount`,	`cost`,	`total`,`comment`) VALUES"
							+"(	'%s',		'%s',					%d,		'addManager',	NULL,		NULL,			NULL,		NULL,	NULL,	'%s')"
							,	now,		db.escape(playerName),	shop,																			db.escape(manager));
						CommandShops.db.query(logQuery);
						log.info(String.format("[%s] Player %s added manager %s to shop %d",
								CommandShops.pdfFile.getName(), playerName, manager, shop));
					}catch(Exception e){
						log.warning(String.format("[%s] Couldn't add manager: %s", CommandShops.pdfFile.getName(), e));
						sender.sendMessage(ChatColor.DARK_AQUA + "DB error during manager add.");
						return false;
					}
				}else if(arg.matches("\\-.*")){
					String manager = arg.replaceFirst("\\-", "");
					// remove manager
					try{
						CommandShops.db.query(String.format("DELETE FROM managers WHERE shop=%d AND manager='%s' LIMIT 1"
											, shop, manager));
						sender.sendMessage(ChatColor.DARK_AQUA + "Removed manager " + ChatColor.WHITE + manager);
						String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
						String logQuery = String.format("INSERT INTO log " 
							+"(	`datetime`,	`user`,					`shop`,	`action`,		`itemid`,	`itemdamage`,	`amount`,	`cost`,	`total`,`comment`) VALUES"
							+"(	'%s',		'%s',					%d,		'removeManager',NULL,		NULL,			NULL,		NULL,	NULL,	'%s')"
							,	now,		db.escape(playerName),	shop,																			db.escape(manager));
						CommandShops.db.query(logQuery);
						log.info(String.format("[%s] Player %s removed manager %s from shop %d",
								CommandShops.pdfFile.getName(), playerName, manager, shop));
					}catch(Exception e){
						log.warning(String.format("[%s] Couldn't remove manager: %s", CommandShops.pdfFile.getName(), e));
						sender.sendMessage(ChatColor.DARK_AQUA + "DB error during manager remove.");
						return false;
					}
				}
			}
			return true;
		}

		// show set manager usage
		sender.sendMessage("   /shop set manager +[playername] -[playername2]");
		return true;
	}

	/**
	 * Toggle notifications for this shop.
	 * @param shop the id of the shop to modify
	 * @return true, if successful
	 */
	private boolean shopSetNotification(long shop)
	{
		String playerName = ((Player)sender).getName();
		boolean notify = false;
		String shopName = "";
		try{
			ResultSet resCheck = CommandShops.db.query("SELECT notify,`name` FROM shops WHERE id=" + shop + " LIMIT 1");
			resCheck.next();
			notify = resCheck.getInt("notify") == 0;
			shopName = resCheck.getString("name");
			resCheck.close();
			CommandShops.db.query("UPDATE shops SET notify=" + (notify?1:0) + " WHERE id=" + shop + " LIMIT 1");
			log.info(String.format("[%s] Player %s changed notify status of shop %d to %s",
					CommandShops.pdfFile.getName(), playerName, shop, (notify?"true":"false")));
		}catch(Exception e){
			log.warning(String.format("[%s] Couldn't toggle notify: %s", CommandShops.pdfFile.getName(), e));
			sender.sendMessage(ChatColor.DARK_AQUA + "DB error during notify toggle");
			return false;
		}

		// Output
		sender.sendMessage(ChatColor.DARK_AQUA + "Notices for "
				+ ChatColor.WHITE + shopName + ChatColor.DARK_AQUA + " are now "
				+ ChatColor.WHITE + (notify ? "on" : "off"));
		return true;
	}

	/**
	 * Set a minimum balance for this shop.
	 * @param shop the id of the shop to modify
	 * @return true, if successful
	 */
	private boolean shopSetMinBalance(long shop)
	{
		// set minbalance amount
		Pattern pattern = Pattern.compile("(?i)set\\s+minbalance\\s+(" +  DECIMAL_REGEX + ")");
		Matcher matcher = pattern.matcher(command);
		if(matcher.find())
		{
			double min = Double.parseDouble(matcher.group(1));
			String playerName = ((Player)sender).getName();
			if(min < 0)
			{
				sender.sendMessage("Can't set negative minbalance");
				return false;
			}
			try{
				CommandShops.db.query("UPDATE shops SET minbalance=" + min + " WHERE id=" + shop + " LIMIT 1");
				String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
				String logQuery = String.format("INSERT INTO log " 
					+"(	`datetime`,	`user`,					`shop`,	`action`,		`itemid`,	`itemdamage`,	`amount`,	`cost`,	`total`,`comment`) VALUES"
					+"(	'%s',		'%s',					%d,		'setMinbalance',NULL,		NULL,			NULL,		NULL,	%f,		NULL)"
					,	now,		db.escape(playerName),	shop,																	min);
				CommandShops.db.query(logQuery);
				log.info(String.format("[%s] Player %s changed minbalance of shop %d to %s",
						CommandShops.pdfFile.getName(), playerName, shop, plugin.econ.format(min)));
			}catch(Exception e){
				log.warning(String.format("[%s] Couldn't set minbalance: %s", CommandShops.pdfFile.getName(), e));
				sender.sendMessage(ChatColor.DARK_AQUA + "Set of minbalance cancelled due to DB error.");
				return false;
			}

			sender.sendMessage(ChatColor.DARK_AQUA + "Shop now has a minimum balance of "
					+ ChatColor.WHITE + plugin.econ.format(min));
			return true;
		}

		sender.sendMessage(" /shop set minbalance [amount]");
		return true;
	}

	/**
	 * Assign ownership of the shop to someone else.
	 * @param shop the id of the shop to modify
	 * @return true, if successful
	 */
	private boolean shopSetOwner(long shop)
	{
		if(!canUseCommand(CommandTypes.SET_OWNER) && !canUseCommand(CommandTypes.ADMIN))
		{
			sender.sendMessage(CommandShops.CHAT_PREFIX
					+ ChatColor.DARK_AQUA
					+ "You don't have permission to use this command");
			return false;
		}
		String playerName = ((Player)sender).getName();
		
		// set owner name
		Pattern pattern = Pattern.compile("(?i)set\\s+owner\\s+(.*)");
		Matcher matcher = pattern.matcher(command);
		if(matcher.find())
		{
			String name = matcher.group(1);
			
			try{
				if(!canUseCommand(CommandTypes.ADMIN))
				{
					String ownQuery = String.format("SELECT id FROM shops WHERE id=%d AND owner='%s' LIMIT 1"
														, shop, db.escape(playerName)); 
					ResultSet resOwn = CommandShops.db.query(ownQuery);
					if(!resOwn.next())
					{
						resOwn.close();
						sender.sendMessage(CommandShops.CHAT_PREFIX
								+ ChatColor.DARK_AQUA + "You must be the shop owner to set this.");
						return false;
					}
					resOwn.close();					
				}
			}catch(Exception e){
				log.warning(String.format("[%s] Couldn't get shop info: %s", CommandShops.pdfFile.getName(), e));
				sender.sendMessage(ChatColor.DARK_AQUA + "Transfer cancelled due to DB error.");
				return false;
			}

			if(!canCreateShop(name))
			{
				sender.sendMessage(CommandShops.CHAT_PREFIX
						+ ChatColor.DARK_AQUA
						+ "that player already has the maximum number of shops!");
				return false;
			}
		
			try{
				CommandShops.db.query("UPDATE shops SET owner='" + db.escape(name) + "' WHERE id=" + shop + " LIMIT 1");
				String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
				String logQuery = String.format("INSERT INTO log " 
					+"(	`datetime`,	`user`,					`shop`,	`action`,	`itemid`,	`itemdamage`,	`amount`,	`cost`,	`total`,`comment`) VALUES"
					+"(	'%s',		'%s',					%d,		'setOwner',	NULL,		NULL,			NULL,		NULL,	NULL,	'%s')"
					,	now,		db.escape(playerName),	shop,																		db.escape(name));
				CommandShops.db.query(logQuery);
				sender.sendMessage(ChatColor.DARK_AQUA + "Owner of shop " + ChatColor.WHITE + shop
									+ ChatColor.DARK_AQUA + " is now " + ChatColor.WHITE + name);
				log.info(String.format("[%s] Player %s changed owner of shop %d to %s",
						CommandShops.pdfFile.getName(), playerName, shop, name));
			}catch(Exception e){
				log.warning(String.format("[%s] DB error while changing shop owner: %s", CommandShops.pdfFile.getName(), e));
				sender.sendMessage(ChatColor.DARK_AQUA + "Transfer cancelled due to DB error.");
				return false;
			}

			return true;
		}

		sender.sendMessage("   /shop set owner [player name]");
		return true;
	}

	/**
	 * Change the name of the shop.
	 * @param shop the id of the shop to modify
	 * @return true, if successful
	 */
	private boolean shopSetName(long shop)
	{
		String playerName = ((Player)sender).getName();
		Pattern pattern = Pattern.compile("(?i)set\\s+name\\s+(.*)");
		Matcher matcher = pattern.matcher(command);
		if(matcher.find())
		{
			String name = matcher.group(1).trim();
			try{
				CommandShops.db.query("UPDATE shops SET `name`='" + db.escape(name) + "' WHERE id=" + shop + " LIMIT 1");
				sender.sendMessage(ChatColor.DARK_AQUA + "Shop " + ChatColor.WHITE + shop + ChatColor.DARK_AQUA + " name changed to " + ChatColor.WHITE + name);
				String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
				String logQuery = String.format("INSERT INTO log " 
					+"(	`datetime`,	`user`,					`shop`,	`action`,	`itemid`,	`itemdamage`,	`amount`,	`cost`,	`total`,`comment`) VALUES"
					+"(	'%s',		'%s',					%d,		'setName',	NULL,		NULL,			NULL,		NULL,	NULL,	'%s')"
					,	now,		db.escape(playerName),	shop,																		db.escape(name));
				CommandShops.db.query(logQuery);
				log.info(String.format("[%s] Player %s changed name of shop %d to %s",
						CommandShops.pdfFile.getName(), playerName, shop, name));
			}catch(Exception e){
				log.warning(String.format("[%s] DB error while changing shop name: %s", CommandShops.pdfFile.getName(), e));
				sender.sendMessage(ChatColor.DARK_AQUA + "DB error during name change.");
				return false;
			}

			return true;
		}

		sender.sendMessage("   /shop set name [shop name]");
		return true;
	}

	/**
	 * Show syntax for all subcommands of Set.
	 * @return true, if successful
	 */
	private boolean shopSetHelp()
	{
		// Display list of set commands & return
		sender.sendMessage(CommandShops.CHAT_PREFIX + ChatColor.DARK_AQUA
				+ "The following set commands are available: ");
		sender.sendMessage("   /shop set buy [item name] <price>");
		sender.sendMessage("   /shop set sell [item name] <price>");
		sender.sendMessage("   /shop set max [item name] [max number]");
		sender.sendMessage("   /shop set manager +[playername] -[playername2]");
		sender.sendMessage("   /shop set minbalance [amount]");
		sender.sendMessage("   /shop set name [shop name]");
		sender.sendMessage("   /shop set owner [player name]");
		if(canUseCommand(CommandTypes.ADMIN))
		{
			sender.sendMessage("   " + "/shop set unlimited money");
			sender.sendMessage("   " + "/shop set unlimited stock");
		}
		return true;
	}
}
