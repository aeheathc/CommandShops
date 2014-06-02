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
import com.aehdev.commandshops.Config;
import com.aehdev.commandshops.ItemInfo;
import com.aehdev.commandshops.Search;
import com.aehdev.commandshops.Shop;

/**
 * Processes the "/shop sell" command. This means "sell" is from the player's perspective.
 */
public class CommandShopSell extends Command
{
	/**
	 * Create a new sell order.
	 * @param plugin
	 * reference to the main CommandShops plugin object
	 * @param commandLabel
	 * command name/alias
	 * @param sender
	 * who sent the command
	 * @param command
	 * command string with arguments
	 */
	public CommandShopSell(CommandShops plugin, String commandLabel, CommandSender sender, String command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/**
	 * Parse and run Sell order.
	 */
	public boolean process()
	{
		if(!(sender instanceof Player))
		{
			sender.sendMessage("You are not in a shop!");
			return true;
		}

		Player player = (Player)sender;
		long shop = Shop.getCurrentShop(player);
		if(shop == -1)
		{
			sender.sendMessage("You are not in a shop!");
			return true;
		}
		// Check Permissions
		if(!canUseCommand(CommandTypes.SELL))
		{
			sender.sendMessage(CommandShops.CHAT_PREFIX
					+ ChatColor.DARK_AQUA
					+ "You don't have permission to use this command");
			return true;
		}

		//> /shop sell all
		// sell all items in your inventory of the same type as what you're holding
		Pattern pattern = Pattern.compile("(?i)sell\\s+all$");
		Matcher matcher = pattern.matcher(command);
		if(matcher.find())
		{
			ItemStack itemStack = player.getItemInHand();
			if(itemStack == null)
			{
				sender.sendMessage("You must be holding an item, or specify an item.");
				return true;
			}
			ItemInfo item = Search.itemById(itemStack);
			if(itemStack.getType().getMaxDurability() > 0)
			{
				if(calcDurabilityPercentage(itemStack) > Config.MAX_DAMAGE
						&& Config.MAX_DAMAGE != 0)
				{
					sender.sendMessage(ChatColor.DARK_AQUA + "Your "
							+ ChatColor.WHITE + item.name
							+ ChatColor.DARK_AQUA
							+ " is too damaged to sell!");
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
				return true;
			}
			int amount = countItemsInInventory(player.getInventory(), item);
			return shopSell(shop, item, amount);
		}

		//> /shop sell
		// sell the currently held stack of items
		matcher.reset();
		pattern = Pattern.compile("(?i)sell$");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			ItemStack itemStack = player.getItemInHand();
			if(itemStack == null){ return true; }
			ItemInfo item = Search.itemById(itemStack);
			int amount = itemStack.getAmount();
			if(itemStack.getType().getMaxDurability() > 0)
			{
				if(calcDurabilityPercentage(itemStack) > Config.MAX_DAMAGE
						&& Config.MAX_DAMAGE != 0)
				{
					sender.sendMessage(ChatColor.DARK_AQUA + "Your "
							+ ChatColor.WHITE + item.name
							+ ChatColor.DARK_AQUA
							+ " is too damaged to sell!");
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
				return true;
			}
			return shopSell(shop, item, amount);
		}

		//> /shop sell int
		// sell 1 of the item with the specified id
		pattern = Pattern.compile("(?i)sell\\s+(\\d+)");
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
			return shopSell(shop, item, 1);
		}

		//> /shop sell int int
		// sell a specified number of the item with the specified id
		matcher.reset();
		pattern = Pattern.compile("(?i)sell\\s+(\\d+)\\s+(\\d+)");
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
			return shopSell(shop, item, count);
		}

		//> /shop sell int all
		// sell all items in your inventory with a certain id
		matcher.reset();
		pattern = Pattern.compile("(?i)sell\\s+(\\d+)\\s+all");
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
			return shopSell(shop, item, count);
		}

		//> /shop sell int:int
		// sell 1 of the item with the specified id and damage
		matcher.reset();
		pattern = Pattern.compile("(?i)sell\\s+(\\d+):(\\d+)");
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
			return shopSell(shop, item, 1);
		}

		//> /shop sell int:int int
		// sell a specified amoutn of the item with the specified id and damage
		matcher.reset();
		pattern = Pattern.compile("(?i)sell\\s+(\\d+):(\\d+)\\s+(\\d+)");
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
			return shopSell(shop, item, count);
		}

		//> /shop sell int:int all
		// sell all items with the specified id and damage
		matcher.reset();
		pattern = Pattern.compile("(?i)sell\\s+(\\d+):(\\d+)\\s+all");
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
			return shopSell(shop, item, count);
		}

		//> /shop sell name, ... int
		// sell a specified number of items with the specified name
		matcher.reset();
		pattern = Pattern.compile("(?i)sell\\s+(.*)\\s+(\\d+)");
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
			return shopSell(shop, item, count);
		}

		//> /shop sell name, ... all
		// sell all items with the specified name
		matcher.reset();
		pattern = Pattern.compile("(?i)sell\\s+(.*)\\s+all");
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
			int count = countItemsInInventory(player.getInventory(), item);
			return shopSell(shop, item, count);
		}

		//> /shop sell name, ...
		// sell 1 of the item with the specified name
		matcher.reset();
		pattern = Pattern.compile("(?i)sell\\s+(.*)");
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
			return shopSell(shop, item, 1);
		}

		// Show sell help
		sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel
				+ " sell [itemname] [number] " + ChatColor.DARK_AQUA
				+ "- Sell this item.");
		return true;
	}

	/**
	 * Execute a sale, inventory/maxstock/minbalance permitting.
	 * @param shop
	 * the shop to which the item is being sold
	 * @param item
	 * the item type presented
	 * @param amount
	 * the presented quantity of the item
	 * @return true, if successful
	 */
	private boolean shopSell(long shop, ItemInfo item, int amount)
	{
		Player player = (Player)sender;
		String playerName = player.getName();
		if(amount < 1)
		{
			player.sendMessage(ChatColor.DARK_AQUA + "Can't sell less than 1 item.");
			return false;
		}
		
		int stock = 0, maxstock = 10;
		double buy = -1, minbalance = 0;
		String shopName = "", owner = "";
		boolean unlimitedMoney = false, unlimitedStock = false;
		try{
			String infoQuery = String.format((Locale)null,"SELECT stock,maxstock,buy,`name`,owner,minbalance,unlimitedMoney,unlimitedStock FROM shop_items LEFT JOIN shops ON shop_items.shop=shops.id WHERE shops.id=%d AND itemid=%d AND itemdamage=%d LIMIT 1"
								, shop, item.typeId, item.subTypeId);
			ResultSet resInf = CommandShops.db.query(infoQuery);
			if(resInf.next())
			{
				stock = (int)Math.floor(resInf.getDouble("stock"));
				maxstock = resInf.getInt("maxstock");
				buy = resInf.getDouble("buy");
				if(resInf.wasNull()) buy = -1;
				shopName = resInf.getString("name");
				owner = resInf.getString("owner");
				minbalance = resInf.getDouble("minbalance");
				unlimitedMoney = resInf.getInt("unlimitedMoney") == 1;
				unlimitedStock = resInf.getInt("unlimitedStock") == 1;
				resInf.close();
			}else{
				resInf.close();
				player.sendMessage(ChatColor.DARK_AQUA + "Sorry, this shop is not buying "
						+ ChatColor.WHITE + item.name + ChatColor.DARK_AQUA + " right now.");
				return false;
			}
		}catch(Exception e){
			log.warning(String.format((Locale)null,"[%s] Couldn't get shop info: %s", CommandShops.pdfFile.getName(), e));
			sender.sendMessage(ChatColor.DARK_AQUA + "Sell cancelled due to DB error.");
			return false;
		}
		
		OfflinePlayer shopOwnerPlayer = Bukkit.getServer().getOfflinePlayer(owner);
		
		double ownerbalance = plugin.econ.getBalance(shopOwnerPlayer);
		
		// Block if shop is not buying this item, is overstocked, or is broke
		if(buy == -1)
		{
			player.sendMessage(ChatColor.DARK_AQUA + "Sorry, "
					+ ChatColor.WHITE + shopName + ChatColor.DARK_AQUA
					+ " is not buying " + ChatColor.WHITE + item.name
					+ ChatColor.DARK_AQUA + " right now.");
			return false;
		}else if(stock >= maxstock && !unlimitedStock && !isShopController(shop)){
			player.sendMessage(ChatColor.DARK_AQUA + "Sorry, "
					+ ChatColor.WHITE + shopName + ChatColor.DARK_AQUA
					+ " is overstocked on " + ChatColor.WHITE + item.name
					+ ChatColor.DARK_AQUA + " right now.");
			return false;
		}else if(ownerbalance <= minbalance && !unlimitedMoney && buy != 0 && !isShopController(shop)){
			player.sendMessage(ChatColor.DARK_AQUA + "Sorry, "
					+ ChatColor.WHITE + shopName + ChatColor.DARK_AQUA
					+ " is broke!");
			return false;
		}
		
		// Limit amount by inventory
		int playerInventory = countItemsInInventory(player.getInventory(), item);
		if(amount > playerInventory)
		{
			player.sendMessage(ChatColor.DARK_AQUA + "You only have "
					+ ChatColor.WHITE + playerInventory + ChatColor.DARK_AQUA
					+ " in your inventory that can be sold.");
			amount = playerInventory;
		}
		
		//Limit amount by maxstock
		int shopCapacity = maxstock - stock;
		if(amount > shopCapacity && !unlimitedStock && !isShopController(shop))
		{
			player.sendMessage(ChatColor.DARK_AQUA + "Shop only has space for "
					+ ChatColor.WHITE + shopCapacity);
			amount = shopCapacity;
		}
		
		//Limit amount by owner balance
		double shopCapital = ownerbalance - minbalance;
		double totalCost = buy * amount;
		if(totalCost > shopCapital && buy != 0 && !unlimitedMoney && !isShopController(shop))
		{
			int canAfford = (int)Math.floor(shopCapital/buy);
			player.sendMessage(ChatColor.DARK_AQUA + "Shop only has money for "
					+ ChatColor.WHITE + canAfford);
			amount = canAfford;
			totalCost = canAfford * buy;
		}
		
		//Some limits may have made selling impossible
		if(amount < 1) return false;

		//move the money
		if(!isShopController(shop))
		{
			if(!unlimitedMoney)
			{
				if(!plugin.econ.withdrawPlayer(shopOwnerPlayer, totalCost).transactionSuccess())
				{
					log.warning(String.format((Locale)null,"[%s] Failed sell due to Vault error (Ending state OK)", CommandShops.pdfFile.getName()));
					player.sendMessage(ChatColor.DARK_AQUA + "Sell cancelled due to Vault error.");
					return false;
				}
			}
			if(!plugin.econ.depositPlayer(player, totalCost).transactionSuccess())
			{
				if(!unlimitedMoney)
				{
					if(!plugin.econ.depositPlayer(shopOwnerPlayer, totalCost).transactionSuccess())
					{
						log.warning(String.format((Locale)null,"[%s] Failed sell due to Vault error, couldn't rollback payment! Owner %s is likely missing %s!", CommandShops.pdfFile.getName(), owner, plugin.econ.format(totalCost)));
					}else{
						log.warning(String.format((Locale)null,"[%s] Failed sell due to Vault error, payment rolled back (Ending state OK)", CommandShops.pdfFile.getName()));
					}
				}else{
					log.warning(String.format((Locale)null,"[%s] Failed sell due to Vault error (Ending state OK)", CommandShops.pdfFile.getName()));
				}
				player.sendMessage(ChatColor.DARK_AQUA + "Sell cancelled due to Vault error.");
				return false;
			}
		}else{
			totalCost = 0;
		}
		
		//move the items
		if(!unlimitedStock)
		{
			try{
				String addQuery = String.format((Locale)null,"UPDATE shop_items SET stock=stock+%d WHERE shop=%d AND itemid=%d AND itemdamage=%d LIMIT 1"
												, amount, shop, item.typeId, item.subTypeId);
				CommandShops.db.query(addQuery);
			}catch(Exception e){
				boolean rbPlayer = true, rbOwner = true;
				if(!isShopController(shop))
				{
					rbPlayer = plugin.econ.withdrawPlayer(player, totalCost).transactionSuccess();
					if(!unlimitedMoney)
						rbOwner = plugin.econ.depositPlayer(shopOwnerPlayer, totalCost).transactionSuccess();
				}
				if(!rbPlayer || !rbOwner)
				{
					String failedRB = "";
					if(!rbPlayer) failedRB += String.format((Locale)null," %s has an extra %s.", playerName, plugin.econ.format(totalCost));
					if(!rbOwner) failedRB += String.format((Locale)null," %s is missing %s.", playerName, plugin.econ.format(totalCost));
					log.warning(String.format((Locale)null,"[%s] Failed sell due to DB error, payments couldn't be rolled back; %s: %s", CommandShops.pdfFile.getName(), failedRB, e));
				}else{
					log.warning(String.format((Locale)null,"[%s] Failed sell due to DB error, payments rolled back successfully (Ending state OK): %s", CommandShops.pdfFile.getName(), e));
				}
				player.sendMessage(ChatColor.DARK_AQUA + "Sell cancelled due to DB error.");
				return false;
			}
		}
		removeItemsFromInventory(player.getInventory(), item, amount);

		if(isShopController(shop))
		{
			player.sendMessage(ChatColor.DARK_AQUA + "You added "
					+ ChatColor.WHITE + amount + " " + item.name
					+ ChatColor.DARK_AQUA + " to the shop");
		}else{
			player.sendMessage(ChatColor.DARK_AQUA + "You sold "
					+ ChatColor.WHITE + amount + " " + item.name
					+ ChatColor.DARK_AQUA + " and gained " + ChatColor.WHITE
					+ plugin.econ.format(totalCost));
		}

		// log the transaction
		int newStock = stock + amount;
		log.info(String.format((Locale)null,"[%s] %s sold %d of %s to %d (%s) for %s; shop's stock is %d",
				CommandShops.pdfFile.getName(), playerName, amount, item.name, shop, shopName, plugin.econ.format(totalCost), newStock));
		try{
			String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			String logQuery = String.format((Locale)null,"INSERT INTO log " 
				+"(	`datetime`,	`user`,					`shop`,	`action`,	`itemid`,	`itemdamage`,	`amount`,	`cost`,	`total`,	`comment`) VALUES"
				+"(	'%s',		'%s',					%d,		'sell',		%d,			%d,				%d,			%f,		%f,			%s)"
				,	now,		db.escape(playerName),	shop,				item.typeId,item.subTypeId,	amount,		buy,	totalCost,	isShopController(shop)?"'Own shop; no cost.'":"NULL");
			CommandShops.db.query(logQuery);
		}catch(Exception e){
			log.warning(String.format((Locale)null,"[%s] Couldn't log transaction: %s",
					CommandShops.pdfFile.getName(), e));
		}
		return true;
	}
}
