package com.aehdev.commandshops.commands;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.aehdev.commandshops.CommandShops;
import com.aehdev.commandshops.ItemInfo;
import com.aehdev.commandshops.Search;
import com.aehdev.commandshops.Shop;

/**
 * Command that lets shop controllers remove all stock of a specified item out of a shop at once.
 */
public class CommandShopRemove extends Command
{

	/**
	 * Create a new remove order.
	 * @param plugin
	 * reference to the main CommandShops plugin object
	 * @param commandLabel
	 * command name/alias
	 * @param sender
	 * who sent the command
	 * @param command
	 * command string with arguments
	 */
	public CommandShopRemove(CommandShops plugin, String commandLabel,
			CommandSender sender, String command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/**
	 * Parse the removal command.
	 */
	public boolean process()
	{
		if(!(sender instanceof Player))
		{
			sender.sendMessage("You need an inventory to recieve items removed from a shop.");
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
		if(!canUseCommand(CommandTypes.REMOVE))
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
					+ "You must be the shop owner or a manager to set this.");
			return true;
		}

		//> /shop remove
		// remove all stock of the item being held
		Pattern pattern = Pattern.compile("(?i)remove$");
		Matcher matcher = pattern.matcher(command);
		if(matcher.find())
		{
			ItemStack itemStack = player.getItemInHand();
			if(itemStack == null){ return false; }
			ItemInfo item = Search.itemById(itemStack.getTypeId(),
					itemStack.getDurability());
			if(item == null)
			{
				sender.sendMessage("Could not find an item.");
				return false;
			}
			return shopRemove(shop, item);
		}

		//> /shop remove int
		// remove all stock of the item with specified id
		pattern = Pattern.compile("(?i)remove\\s+(\\d+)");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			ItemInfo item = Search.itemById(id);
			return shopRemove(shop, item);
		}

		//> /shop remove int:int
		// remove all stock of the item with specified id and damage
		matcher.reset();
		pattern = Pattern.compile("(?i)remove\\s+(\\d+):(\\d+)");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			short type = Short.parseShort(matcher.group(2));
			ItemInfo item = Search.itemById(id, type);
			return shopRemove(shop, item);
		}

		//> /shop remove name
		// remove all stock of the item with specified name
		matcher.reset();
		pattern = Pattern.compile("(?i)remove\\s+(.*)");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			String itemName = matcher.group(1);
			ItemInfo item = Search.itemByName(itemName);
			return shopRemove(shop, item);
		}

		// Show usage
		sender.sendMessage(ChatColor.WHITE + "   /shop remove [itemname]" + ChatColor.DARK_AQUA
				+ " - Remove all stock of certain item from shop.");
		return true;
	}

	/**
	 * Remove all items of a certain type from the shop and return them to the player.
	 * @param shop
	 * shop to remove items from
	 * @param item
	 * item type to remove
	 * @return true, if successful
	 */
	private boolean shopRemove(long shop, ItemInfo item)
	{
		Player player = (Player)sender;
		String playerName = player.getName();
		if(item == null)
		{
			sender.sendMessage(ChatColor.DARK_AQUA + "Item not found.");
			return false;
		}
		
		int stockid = -1;
		int amount = 0;
		
		try{
			String countQuery = String.format("SELECT id,stock FROM shop_items WHERE "
											+ "shop=	%d AND itemid=	%d AND itemdamage=	%d"
											, 			shop,			item.typeId,		item.subTypeId);
			ResultSet resCount = CommandShops.db.query(countQuery);
			if(resCount.next())
			{
				stockid = resCount.getInt("id");
				amount = (int)Math.floor(resCount.getDouble("stock"));
			}
			resCount.close();
			if(amount == 0)
			{
				player.sendMessage("Nothing to remove.");
				return false;
			}
			CommandShops.db.query("UPDATE shop_items SET stock=0 WHERE id=" + stockid + " LIMIT 1");
		}catch(Exception e){
			log.warning(String.format("[%s] Couldn't remove items from shop: %s", CommandShops.pdfFile.getName(), e));
			sender.sendMessage(ChatColor.DARK_AQUA + "Remove cancelled due to DB error.");
			return false;
		}
		
		givePlayerItem(item.toStack(), amount);
		player.sendMessage("" + ChatColor.WHITE + amount + " " + item.name
				+ ChatColor.DARK_AQUA
				+ " have been returned to your inventory");
		
		//log
		log.info(String.format("[%s] Removal of all %d %s from shop %d by %s",
				CommandShops.pdfFile.getName(), amount, item.name, shop, playerName));
		try{
			String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			String logQuery = String.format("INSERT INTO log" 
				+"(	`datetime`,	`user`,					`shop`,	`action`,	`itemid`,	`itemdamage`,	`amount`,	`cost`,	`total`,`comment`) VALUES"
				+"(	'%s',		'%s',					%d,		'remove',	%d,			%d,				%d,			NULL,	NULL,	NULL)"
				,	now,		db.escape(playerName),	shop,				item.typeId,item.subTypeId,	amount);
			CommandShops.db.query(logQuery);
		}catch(Exception e){
			log.warning(String.format("[%s] Couldn't log transaction: %s",
					CommandShops.pdfFile.getName(), e));
		}
		return true;
	}
}
