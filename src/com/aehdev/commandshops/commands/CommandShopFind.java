package com.aehdev.commandshops.commands;

import java.sql.ResultSet;
import java.util.Locale;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.aehdev.commandshops.CommandShops;
import com.aehdev.commandshops.Config;
import com.aehdev.commandshops.ItemInfo;
import com.aehdev.commandshops.Search;

/**
 * This command lets you search for shops that have a certain item
 * while comparing prices.
 */
public class CommandShopFind extends Command
{

	/**
	 * Create a new Find order.
	 * @param plugin
	 * reference to the main CommandShops plugin object
	 * @param commandLabel
	 * the actual main command name
	 * @param sender
	 * the sender of the command
	 * @param command
	 * the whole argument string
	 */
	public CommandShopFind(CommandShops plugin, String commandLabel,
			CommandSender sender, String command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/**
	 * Run the Find command.
	 */
	public boolean process()
	{
		if(!(sender instanceof Player))
		{
			sender.sendMessage("You need an in game location to find shops near you.");
			return false;
		}

		Player player = (Player)sender;

		//> /shop find
		// Compare prices on item in hand.
		Pattern pattern = Pattern.compile("(?i)find$");
		Matcher matcher = pattern.matcher(command);
		if(matcher.find())
		{
			ItemStack itemStack = player.getItemInHand();
			if(itemStack == null){ return true; }
			ItemInfo found = null;
			if(itemStack.getType().getMaxDurability() > 0)
			{
				found = Search.itemById(itemStack);
			}else{
				found = Search.itemById(itemStack);
			}
			if(found == null)
			{
				sender.sendMessage("Could not find an item.");
				return true;
			}
			return shopFind(player, found);
		}

		//> /shop find int
		// compare prices on item with the specified id
		matcher.reset();
		pattern = Pattern.compile("(?i)find\\s+(\\d+)$");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			ItemInfo found = Search.itemById(id);
			if(found == null)
			{
				sender.sendMessage("Could not find an item.");
				return true;
			}
			return shopFind(player, found);
		}

		//> /shop find int:int
		// compare prices on item with the specified id and damage
		matcher.reset();
		pattern = Pattern.compile("(?i)find\\s+(\\d+):(\\d+)$");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			short type = Short.parseShort(matcher.group(2));
			ItemInfo found = Search.itemById(id, type);
			if(found == null)
			{
				sender.sendMessage("Could not find an item.");
				return true;
			}
			return shopFind(player, found);
		}

		//> /shop find name
		// compare prices on item with the specified name
		matcher.reset();
		pattern = Pattern.compile("(?i)find\\s+(.*)");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			String name = matcher.group(1);
			ItemInfo found = Search.itemByName(name);
			if(found == null)
			{
				sender.sendMessage(String.format((Locale)null,
						"No item was found matching \"%s\"", name));
				return true;
			}else
			{
				return shopFind(player, found);
			}
		}

		// Show find help
		sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel
				+ " find [itemname] " + ChatColor.DARK_AQUA
				+ "- Find shops that buy or sell this item.");
		return true;
	}

	/**
	 * Show price comparison and stock info for shops near you of the specified item
	 * @param player
	 * player who typed the command
	 * @param found
	 * the item definition to look for
	 * @return true, if successful
	 */
	private boolean shopFind(Player player, ItemInfo found)
	{
		String playerWorld = player.getWorld().getName();
		Location loc = player.getLocation();
		int x=loc.getBlockX(), y=loc.getBlockY(), z=loc.getBlockZ();
		String distCalc = String.format((Locale)null,"(abs(((x+x2)/2)- %d)*abs(((x+x2)/2)- %d)+abs(((y+y2)/2)- %d)*abs(((y+y2)/2)- %d)+abs(((z+z2)/2)- %d)*abs(((z+z2)/2)- %d))"
							,x,x,y,y,z,z);
		Vector<String> msg = new Vector<String>(11);
		msg.add("");
		final int limitsquared = (int)Math.pow(Config.FIND_MAX_DISTANCE, 2);
		try{
			//get 5 closest shops carrying that item
			String countQuery = String.format((Locale)null,"SELECT COUNT(*),%s AS distance FROM shop_items LEFT JOIN shops ON shop_items.shop=shops.id WHERE shops.world='%s' AND itemid=%d AND itemdamage=%d AND (buy IS NOT NULL OR sell IS NOT NULL) AND %s<=%d"
					,distCalc, playerWorld, found.typeId,found.subTypeId, distCalc, limitsquared);
			ResultSet resCount = CommandShops.db.query(countQuery);
			resCount.next();
			int total = resCount.getInt(1);
			resCount.close();
			
			String findQuery = String.format((Locale)null,"SELECT shops.id AS shopid,shops.`name` AS shopname,shops.unlimitedStock AS unlimitedStock,stock,maxstock,buy,sell,%s AS distance FROM shop_items LEFT JOIN shops ON shop_items.shop=shops.id WHERE shops.world='%s' AND itemid=%d AND itemdamage=%d AND (buy IS NOT NULL OR sell IS NOT NULL) AND %s<=%d ORDER BY distance ASC LIMIT 5"
					,distCalc, playerWorld, found.typeId, found.subTypeId, distCalc, limitsquared);
			ResultSet resFind = CommandShops.db.query(findQuery);
			int shops=0;
			while(resFind.next())
			{
				int shopid = resFind.getInt("shopid");
				String shopname = resFind.getString("shopname");
				boolean unlimitedStock = resFind.getInt("unlimitedStock") == 1;
				int stock = (int)Math.floor(resFind.getDouble("stock"));
				int maxstock = resFind.getInt("maxstock");
				String buy = plugin.econ.format(resFind.getDouble("buy"));
				if(resFind.wasNull()) buy = "--";
				String sell = plugin.econ.format(resFind.getDouble("sell"));
				if(resFind.wasNull()) sell = "--";
				int distance = (int)Math.floor(Math.sqrt(resFind.getDouble("distance")));
						
				String stockstr = ChatColor.WHITE + (unlimitedStock ? "Inf." :
				(stock + "" + ChatColor.DARK_AQUA + "/" + ChatColor.WHITE + maxstock));
				
				StringBuffer output = new StringBuffer(60);
				output.append(ChatColor.WHITE);
				output.append(shopname);
				output.append(ChatColor.DARK_AQUA);
				output.append(" ");
				output.append(ChatColor.DARK_AQUA);
				output.append("Dist:");
				output.append(ChatColor.WHITE);
				output.append(distance);
				output.append("m ");
				output.append(ChatColor.DARK_AQUA);
				output.append("ID:");
				output.append(ChatColor.WHITE);
				output.append(shopid);
				msg.add(output.toString());
				++shops;
				output = new StringBuffer(60);
				output.append(ChatColor.DARK_AQUA);
				output.append(" Stock:");
				output.append(stockstr);
				output.append(ChatColor.GOLD);
				output.append(" selling@");
				if(stock<1 && !unlimitedStock) output.append(ChatColor.RED);
				output.append(sell);
				output.append(ChatColor.GREEN);
				output.append(" buying@");
				if(stock>=maxstock) output.append(ChatColor.RED);
				output.append(buy);
				msg.add(output.toString());
			}
			resFind.close();
			String[] outmsg = {""};
			outmsg = msg.toArray(outmsg);
			outmsg[0] = ChatColor.DARK_AQUA + "Showing " + ChatColor.WHITE + shops + ChatColor.DARK_AQUA
					+ " of " + ChatColor.WHITE + total + ChatColor.DARK_AQUA + " shops having "
					+ ChatColor.WHITE + found.name;
			player.sendMessage(outmsg);
		}catch(Exception e){
			log.warning(String.format((Locale)null,"[%s] Couldn't get item info: %s", CommandShops.pdfFile.getName(), e));
			sender.sendMessage(ChatColor.DARK_AQUA + "Find cancelled due to DB error.");
			return true;
		}
		
		return true;
	}

}
