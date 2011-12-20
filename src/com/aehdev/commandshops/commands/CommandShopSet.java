package com.aehdev.commandshops.commands;

import java.util.Iterator;
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

// TODO: Auto-generated Javadoc
/**
 * The Class CommandShopSet.
 */
public class CommandShopSet extends Command
{

	/**
	 * Instantiates a new command shop set.
	 * @param plugin
	 * the plugin
	 * @param commandLabel
	 * the command label
	 * @param sender
	 * the sender
	 * @param command
	 * the command
	 */
	public CommandShopSet(CommandShops plugin, String commandLabel,
			CommandSender sender, String command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/**
	 * Instantiates a new command shop set.
	 * @param plugin
	 * the plugin
	 * @param commandLabel
	 * the command label
	 * @param sender
	 * the sender
	 * @param command
	 * the command
	 */
	public CommandShopSet(CommandShops plugin, String commandLabel,
			CommandSender sender, String[] command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/* (non-Javadoc)
	 * @see com.aehdev.commandshops.commands.Command#process() */
	public boolean process()
	{
		// Check Permissions
		if(!canUseCommand(CommandTypes.SET))
		{
			sender.sendMessage(CommandShops.CHAT_PREFIX + ChatColor.DARK_AQUA
					+ "You don't have permission to use this command");
			return true;
		}

		log.info(String.format("[%s] Command issued: %s",
				plugin.pdfFile.getName(), command));

		// Parse Arguments
		if(command.matches("(?i)set\\s+sell.*"))
		{
			return shopSetSell();
		}else if(command.matches("(?i)set\\s+buy.*")){
			return shopSetBuy();
		}else if(command.matches("(?i)set\\s+max.*")){
			return shopSetMax();
		}else if(command.matches("(?i)set\\s+unlimited.*")){
			return shopSetUnlimited();
		}else if(command.matches("(?i)set\\s+manager.*")){
			return shopSetManager();
		}else if(command.matches("(?i)set\\s+minbalance.*")){
			return shopSetMinBalance();
		}else if(command.matches("(?i)set\\s+notification.*")){
			return shopSetNotification();
		}else if(command.matches("(?i)set\\s+owner.*")){
			return shopSetOwner();
		}else if(command.matches("(?i)set\\s+name.*")){
			return shopSetName();
		}else{
			return shopSetHelp();
		}
	}

	/**
	 * Shop set buy.
	 * @return true, if successful
	 */
	private boolean shopSetBuy()
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

			// Check if Player can Modify
			if(!isShopController(shop) && !canUseCommand(CommandTypes.ADMIN))
			{
				player.sendMessage(ChatColor.DARK_AQUA
						+ "You must be the shop owner or a manager to set this.");
				player.sendMessage(ChatColor.DARK_AQUA
						+ "The current shop owner is " + ChatColor.WHITE
						+ shop.getOwner());
				return true;
			}
		}else{
			sender.sendMessage("Console is not implemented yet.");
			return true;
		}

		// Command matching
		Pattern pattern;
		Matcher matcher;

		// set sell int int

		pattern = Pattern.compile("(?i)set\\s+buy\\s+(\\d+)\\s+("
				+ DECIMAL_REGEX + ")");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			ItemInfo item = Search.itemById(id);
			double price = Double.parseDouble(matcher.group(2));
			return shopSetBuy(shop, item, price);
		}

		// set sell int:int int
		matcher.reset();
		pattern = Pattern.compile("(?i)set\\s+buy\\s+(\\d+):(\\d+)\\s+("
				+ DECIMAL_REGEX + ")");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			short type = Short.parseShort(matcher.group(2));
			ItemInfo item = Search.itemById(id, type);
			double price = Double.parseDouble(matcher.group(3));
			return shopSetBuy(shop, item, price);
		}

		// set sell (chars) int
		matcher.reset();
		pattern = Pattern.compile("(?i)set\\s+buy\\s+(.*)\\s+(" + DECIMAL_REGEX
				+ ")");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			String name = matcher.group(1);
			ItemInfo item = Search.itemByName(name);
			double price = Double.parseDouble(matcher.group(2));
			return shopSetBuy(shop, item, price);
		}

		// show set sell usage
		sender.sendMessage("   " + "/" + commandLabel
				+ " set buy [item name] [price] <bundle size>");
		return true;
	}

	/**
	 * Set the buying price of an item.
	 * "Buy" is from the shop's perspective.
	 * @param shop
	 * reference to the shop to modify
	 * @param item
	 * identify the item type to buy
	 * @param price
	 * price per item
	 * @return true, if successful
	 */
	private boolean shopSetBuy(Shop shop, ItemInfo item, double price)
	{
		if(item == null)
		{
			sender.sendMessage("Item was not found.");
			return true;
		}

		// Check if Shop has item
		if(!shop.containsItem(item))
		{
			// nicely message user
			sender.sendMessage(String.format("This shop does not carry %s!",
					item.name));
			return true;
		}

		// Warn about faulty pricing
		if(price < 0)
		{
			sender.sendMessage("Cannot set negative buy price!");
			return true;
		}
		if(price > shop.getItem(item.name).getBuyPrice())
		{
			sender.sendMessage("Cannot set buy price greater than sell price!");
			return true;
		}

		// Set new values
		shop.setItemSellPrice(item.name, price);

		// Save Shop
		plugin.getShopData().saveShop(shop);

		// Send Result
		sender.sendMessage(ChatColor.WHITE + item.name + ChatColor.DARK_AQUA
				+ " now is purchased for " + ChatColor.WHITE
				+ plugin.getEconManager().format(price));
		return true;
	}

	/**
	 * Shop set sell.
	 * @return true, if successful
	 */
	private boolean shopSetSell()
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

			// Check if Player can Modify
			if(!isShopController(shop) && !canUseCommand(CommandTypes.ADMIN))
			{
				player.sendMessage(ChatColor.DARK_AQUA
						+ "You must be the shop owner or a manager to set this.");
				player.sendMessage(ChatColor.DARK_AQUA
						+ "The current shop owner is " + ChatColor.WHITE
						+ shop.getOwner());
				return true;
			}
		}else
		{
			sender.sendMessage("Console is not implemented yet.");
			return true;
		}

		// Command matching
		Pattern pattern;
		Matcher matcher;

		// set buy int int
		pattern = Pattern.compile("(?i)set\\s+sell\\s+(\\d+)\\s+("
				+ DECIMAL_REGEX + ")");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			ItemInfo item = Search.itemById(id);
			double price = Double.parseDouble(matcher.group(2));
			return shopSetSell(shop, item, price);
		}

		// set buy int:int int
		matcher.reset();
		pattern = Pattern.compile("(?i)set\\s+sell\\s+(\\d+):(\\d+)\\s+("
				+ DECIMAL_REGEX + ")");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			short type = Short.parseShort(matcher.group(2));
			ItemInfo item = Search.itemById(id, type);
			double price = Double.parseDouble(matcher.group(3));
			return shopSetSell(shop, item, price);
		}

		// set buy (chars) int
		matcher.reset();
		pattern = Pattern.compile("(?i)set\\s+sell\\s+(.*)\\s+("
				+ DECIMAL_REGEX + ")");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			String name = matcher.group(1);
			ItemInfo item = Search.itemByName(name);
			double price = Double.parseDouble(matcher.group(2));
			return shopSetSell(shop, item, price);
		}

		// show set buy usage
		sender.sendMessage("   " + "/" + commandLabel
				+ " set sell [item name] [price] <bundle size>");
		return true;
	}

	/**
	 * Set the selling price of an item.
	 * "Sell" is from the shop's perspective.
	 * @param shop
	 * reference to the shop to modify
	 * @param item
	 * identify the item type to sell
	 * @param price
	 * price per item
	 * @return true, if successful
	 */
	private boolean shopSetSell(Shop shop, ItemInfo item, double price)
	{
		if(item == null)
		{
			sender.sendMessage("Item was not found.");
			return true;
		}

		// Check if Shop has item
		if(!shop.containsItem(item))
		{
			// nicely message user
			sender.sendMessage(String.format("This shop does not carry %s!",
					item.name));
			return true;
		}

		// Warn about faulty pricing
		if(price < 0)
		{
			sender.sendMessage("Cannot set negative sell price!");
			return true;
		}
		if(price < shop.getItem(item.name).getSellPrice())
		{
			sender.sendMessage("Cannot set sell price less than buy price!");
			return true;
		}

		// Set new values
		shop.setItemBuyPrice(item.name, price);

		// Save Shop
		plugin.getShopData().saveShop(shop);

		// Send Result
		sender.sendMessage(ChatColor.WHITE + item.name + ChatColor.DARK_AQUA
				+ " now sells for " + ChatColor.WHITE
				+ plugin.getEconManager().format(price));

		return true;
	}

	/**
	 * Shop set max.
	 * @return true, if successful
	 */
	private boolean shopSetMax()
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

			// Check if Player can Modify
			if(!isShopController(shop) && !canUseCommand(CommandTypes.ADMIN))
			{
				player.sendMessage(ChatColor.DARK_AQUA
						+ "You must be the shop owner or a manager to set this.");
				player.sendMessage(ChatColor.DARK_AQUA
						+ "The current shop owner is " + ChatColor.WHITE
						+ shop.getOwner());
				return true;
			}
		}else
		{
			sender.sendMessage("Console is not implemented yet.");
			return true;
		}

		// Command matching

		// shop set max int int
		Pattern pattern = Pattern.compile("(?i)set\\s+max\\s+(\\d+)\\s+(\\d+)");
		Matcher matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			ItemInfo item = Search.itemById(id);
			int max = Integer.parseInt(matcher.group(2));
			return shopSetMax(shop, item, max);
		}

		// shop set max int:int int
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

		// shop set max chars int
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

		// show set buy usage
		sender.sendMessage("   " + "/" + commandLabel
				+ " set max [item name] [max number]");
		return true;
	}

	/**
	 * Shop set max.
	 * @param shop
	 * the shop
	 * @param item
	 * the item
	 * @param max
	 * the max
	 * @return true, if successful
	 */
	private boolean shopSetMax(Shop shop, ItemInfo item, int max)
	{
		if(item == null)
		{
			sender.sendMessage("Item was not found.");
			return true;
		}

		// Check if Shop has item
		if(!shop.containsItem(item))
		{
			// nicely message user
			sender.sendMessage(String.format("This shop does not carry %s!",
					item.name));
			return true;
		}

		// Check negative values
		if(max < 0)
		{
			sender.sendMessage("Only positive values allowed");
			return true;
		}

		// Set new values
		shop.setItemMaxStock(item.name, max);

		// Save Shop
		plugin.getShopData().saveShop(shop);

		// Send Message
		sender.sendMessage(item.name + " maximum stock is now " + max);

		return true;
	}

	/**
	 * Shop set unlimited.
	 * @return true, if successful
	 */
	private boolean shopSetUnlimited()
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
			if(!canUseCommand(CommandTypes.ADMIN))
			{
				player.sendMessage(CommandShops.CHAT_PREFIX
						+ ChatColor.DARK_AQUA
						+ "You must be a shop admin to do this.");
				return true;
			}
		}else
		{
			sender.sendMessage("Console is not implemented yet.");
			return true;
		}

		// Command matching

		// shop set max int int
		Pattern pattern = Pattern.compile("(?i)set\\s+max\\s+(\\d+)\\s+(\\d+)");
		Matcher matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			ItemInfo item = Search.itemById(id);
			int max = Integer.parseInt(matcher.group(2));
			return shopSetMax(shop, item, max);
		}

		// shop set unlimited money
		matcher.reset();
		pattern = Pattern.compile("(?i)set\\s+unlimited\\s+money");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			shop.setUnlimitedMoney(!shop.isUnlimitedMoney());
			sender.sendMessage(CommandShops.CHAT_PREFIX + ChatColor.DARK_AQUA
					+ "Unlimited money was set to " + ChatColor.WHITE
					+ shop.isUnlimitedMoney());
			plugin.getShopData().saveShop(shop);
			return true;
		}

		// shop set unlimited stock
		matcher.reset();
		pattern = Pattern.compile("(?i)set\\s+unlimited\\s+stock");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			shop.setUnlimitedStock(!shop.isUnlimitedStock());
			sender.sendMessage(CommandShops.CHAT_PREFIX + ChatColor.DARK_AQUA
					+ "Unlimited stock was set to " + ChatColor.WHITE
					+ shop.isUnlimitedStock());
			plugin.getShopData().saveShop(shop);
			return true;
		}

		// show set buy usage
		sender.sendMessage("   " + "/" + commandLabel + " set unlimited money");
		sender.sendMessage("   " + "/" + commandLabel + " set unlimited stock");
		return true;
	}

	/**
	 * Shop set manager.
	 * @return true, if successful
	 */
	private boolean shopSetManager()
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

			// Check if Player can Modify
			if(!shop.getOwner().equalsIgnoreCase(player.getName()))
			{
				player.sendMessage(ChatColor.DARK_AQUA
						+ "You must be the shop owner to set this.");
				player.sendMessage(ChatColor.DARK_AQUA
						+ "The current shop owner is " + ChatColor.WHITE
						+ shop.getOwner());
				return true;
			}
		}else
		{
			sender.sendMessage("Console is not implemented yet.");
			return true;
		}

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
					// add manager
					shop.addManager(arg.replaceFirst("\\+", ""));
				}else if(arg.matches("\\-.*"))
				{
					// remove manager
					shop.removeManager(arg.replaceFirst("\\-", ""));
				}
			}

			// Save Shop
			plugin.getShopData().saveShop(shop);

			notifyPlayers(
					shop,
					new String[]{
							ChatColor.DARK_AQUA
									+ "The shop managers have been updated. The current managers are:",
							Search.join(shop.getManagers(), ", ")});
			return true;
		}

		// show set manager usage
		sender.sendMessage("   " + "/" + commandLabel
				+ " set manager +[playername] -[playername2]");
		return true;
	}

	/**
	 * Shop set notification.
	 * @return true, if successful
	 */
	private boolean shopSetNotification()
	{
		Shop shop = null;

		// Get current shop
		if(sender instanceof Player)
		{
			// Get player & data
			Player player = (Player)sender;
			PlayerData pData = plugin.getPlayerData().get(player.getName());

			// Get current shop
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

			// Check if Player can Modify
			if(!shop.getOwner().equalsIgnoreCase(player.getName()))
			{
				sender.sendMessage(CommandShops.CHAT_PREFIX
						+ ChatColor.DARK_AQUA
						+ "You must be the shop owner to set this.");
				sender.sendMessage(ChatColor.DARK_AQUA
						+ " The current shop owner is " + ChatColor.WHITE
						+ shop.getOwner());
				return true;
			}
		}else
		{
			sender.sendMessage("Console is not implemented yet.");
			return true;
		}

		// set notification
		shop.setNotification(!shop.getNotification());

		// Save Shop
		plugin.getShopData().saveShop(shop);

		// Output
		sender.sendMessage(String.format(ChatColor.DARK_AQUA + "Notices for "
				+ ChatColor.WHITE + "%s" + ChatColor.DARK_AQUA + " are now "
				+ ChatColor.WHITE + "%s", shop.getName(),
				shop.getNotification() ? "on" : "off"));
		return true;
	}

	/**
	 * Shop set min balance.
	 * @return true, if successful
	 */
	private boolean shopSetMinBalance()
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

			// Check if Player can Modify
			if(!shop.getOwner().equalsIgnoreCase(player.getName()))
			{
				sender.sendMessage(CommandShops.CHAT_PREFIX
						+ ChatColor.DARK_AQUA
						+ "You must be the shop owner to set this.");
				sender.sendMessage(ChatColor.DARK_AQUA
						+ " The current shop owner is " + ChatColor.WHITE
						+ shop.getOwner());
				return true;
			}
		}else
		{
			sender.sendMessage("Console is not implemented yet.");
			return true;
		}

		// set minbalance amount
		Pattern pattern = Pattern.compile("(?i)set\\s+minbalance\\s+(\\d+)");
		Matcher matcher = pattern.matcher(command);
		if(matcher.find())
		{
			double min = Double.parseDouble(matcher.group(1));
			shop.setMinBalance(min);
			// Save Shop
			plugin.getShopData().saveShop(shop);

			sender.sendMessage(ChatColor.WHITE + shop.getName()
					+ ChatColor.DARK_AQUA + " now has a minimum balance of "
					+ ChatColor.WHITE + plugin.getEconManager().format(min));
			return true;
		}

		sender.sendMessage(" " + "/" + commandLabel
				+ " set minbalance [amount]");
		return true;
	}

	/**
	 * Shop set owner.
	 * @return true, if successful
	 */
	private boolean shopSetOwner()
	{
		Shop shop = null;
		boolean reset = false;

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

			// Check if Player can Modify
			if(!canUseCommand(CommandTypes.ADMIN)
					&& !shop.getOwner().equalsIgnoreCase(player.getName()))
			{
				sender.sendMessage(CommandShops.CHAT_PREFIX
						+ ChatColor.DARK_AQUA
						+ "You must be the shop owner to set this.");
				sender.sendMessage(ChatColor.DARK_AQUA
						+ "  The current shop owner is " + ChatColor.WHITE
						+ shop.getOwner());
				return true;
			}

			if(!canUseCommand(CommandTypes.SET_OWNER)
					&& !canUseCommand(CommandTypes.ADMIN))
			{
				sender.sendMessage(CommandShops.CHAT_PREFIX
						+ ChatColor.DARK_AQUA
						+ "You don't have permission to use this command");
				return true;
			}

			if(!canUseCommand(CommandTypes.ADMIN))
			{
				sender.sendMessage(CommandShops.CHAT_PREFIX
						+ ChatColor.DARK_AQUA + shop.getName()
						+ " is no longer buying items.");
				reset = true;
			}
		}else
		{
			sender.sendMessage("Console is not implemented yet.");
			return true;
		}

		// set owner name
		Pattern pattern = Pattern.compile("(?i)set\\s+owner\\s+(.*)");
		Matcher matcher = pattern.matcher(command);
		if(matcher.find())
		{
			String name = matcher.group(1);
			if(!canUseCommand(CommandTypes.SET_OWNER))
			{
				sender.sendMessage(CommandShops.CHAT_PREFIX
						+ ChatColor.DARK_AQUA
						+ "You do not have permission to do this.");
				return true;
			}else if(!canCreateShop(name))
			{
				sender.sendMessage(CommandShops.CHAT_PREFIX
						+ ChatColor.DARK_AQUA
						+ "that player already has the maximum number of shops!");
				return true;
			}else
			{
				shop.setOwner(name);

				// Save Shop
				plugin.getShopData().saveShop(shop);

				// Reset buy prices (0)
				if(reset)
				{
					Iterator<InventoryItem> it = shop.getItems().iterator();
					while(it.hasNext())
					{
						InventoryItem item = it.next();
						item.setSellPrice(0);
					}
				}

				notifyPlayers(shop, new String[]{CommandShops.CHAT_PREFIX
						+ ChatColor.DARK_AQUA + shop.getName()
						+ " is now under new management!  The new owner is "
						+ ChatColor.WHITE + shop.getOwner()});
				return true;
			}
		}

		sender.sendMessage("   " + "/" + commandLabel
				+ " set owner [player name]");
		return true;
	}

	/**
	 * Shop set name.
	 * @return true, if successful
	 */
	private boolean shopSetName()
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

			// Check if Player can Modify
			if(!canModifyShop(shop))
			{
				sender.sendMessage(CommandShops.CHAT_PREFIX
						+ ChatColor.DARK_AQUA
						+ "You must be the shop owner to set this.");
				sender.sendMessage(ChatColor.DARK_AQUA
						+ "  The current shop owner is " + ChatColor.WHITE
						+ shop.getOwner());
				return true;
			}
		}else
		{
			sender.sendMessage("Console is not implemented yet.");
			return true;
		}

		Pattern pattern = Pattern.compile("(?i)set\\s+name\\s+(.*)");
		Matcher matcher = pattern.matcher(command);
		if(matcher.find())
		{
			String name = matcher.group(1).trim();
			shop.setName(name);
			plugin.getShopData().saveShop(shop);
			notifyPlayers(shop, new String[]{CommandShops.CHAT_PREFIX
					+ ChatColor.DARK_AQUA + "Shop name is now "
					+ ChatColor.WHITE + shop.getName()});
			return true;
		}

		sender.sendMessage("   " + "/" + commandLabel + " set name [shop name]");
		return true;
	}

	/**
	 * Shop set help.
	 * @return true, if successful
	 */
	private boolean shopSetHelp()
	{
		// Display list of set commands & return
		sender.sendMessage(CommandShops.CHAT_PREFIX + ChatColor.DARK_AQUA
				+ "The following set commands are available: ");
		sender.sendMessage("   " + "/" + commandLabel
				+ " set buy [item name] [price] <bundle size>");
		sender.sendMessage("   " + "/" + commandLabel
				+ " set sell [item name] [price] <bundle size>");
		sender.sendMessage("   " + "/" + commandLabel
				+ " set max [item name] [max number]");
		sender.sendMessage("   " + "/" + commandLabel
				+ " set manager +[playername] -[playername2]");
		sender.sendMessage("   " + "/" + commandLabel
				+ " set minbalance [amount]");
		sender.sendMessage("   " + "/" + commandLabel + " set name [shop name]");
		sender.sendMessage("   " + "/" + commandLabel
				+ " set owner [player name]");
		if(canUseCommand(CommandTypes.ADMIN))
		{
			sender.sendMessage("   " + "/" + commandLabel
					+ " set unlimited money");
			sender.sendMessage("   " + "/" + commandLabel
					+ " set unlimited stock");
		}
		return true;
	}
}
