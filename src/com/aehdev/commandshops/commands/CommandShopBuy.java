package com.aehdev.commandshops.commands;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.aehdev.commandshops.CommandShops;
import com.aehdev.commandshops.InventoryItem;
import com.aehdev.commandshops.ItemInfo;
import com.aehdev.commandshops.PlayerData;
import com.aehdev.commandshops.Search;
import com.aehdev.commandshops.Shop;
import com.aehdev.commandshops.Transaction;

// TODO: Auto-generated Javadoc
/**
 * Handles the "/shop buy" command.
 */
public class CommandShopBuy extends Command
{

	/**
	 * New instance requires information about the command issued and a
	 * reference to the main plugin object.
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
	 */
	@Override
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
			if(!canUseCommand(CommandTypes.SELL))
			{
				sender.sendMessage(CommandShops.CHAT_PREFIX
						+ ChatColor.DARK_AQUA
						+ "You don't have permission to use this command");
				return true;
			}

			// buy (player only command)
			Pattern pattern = Pattern.compile("(?i)buy$");
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
					return true;
				}
				return shopBuy(shop, item, 0);
			}

			// buy all (player only command)
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
				ItemInfo item = Search.itemById(itemStack.getTypeId(),
						itemStack.getDurability());
				if(item == null)
				{
					sender.sendMessage("Could not find an item.");
					return true;
				}
				int count;
				if(shop.isUnlimitedStock())
				{
					// fill the player's inventory
					count = countAvailableSpaceForItemInInventory(
							player.getInventory(), item);
				}else{
					// use shop stock
					count = shop.getItem(item.name).getStock();
				}

				return shopBuy(shop, item, count);
			}

			// buy int all
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
				int count;
				if(shop.isUnlimitedStock())
				{
					// get player avail space
					count = countAvailableSpaceForItemInInventory(
							player.getInventory(), item);
				}else{
					// use shop stock
					count = shop.getItem(item.name).getStock();
				}
				if(count < 1)
				{
					sender.sendMessage("You must buy at least one " + item.name
							+ "!");
					return true;
				}
				return shopBuy(shop, item, count);
			}

			// buy int:int all
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
				int count;
				if(shop.isUnlimitedStock())
				{
					// get player avail space
					count = countAvailableSpaceForItemInInventory(
							player.getInventory(), item);
				}else{
					// use shop stock
					count = shop.getItem(item.name).getStock();
				}
				if(count < 1)
				{
					sender.sendMessage("You must buy at least one " + item.name
							+ "!");
					return true;
				}
				return shopBuy(shop, item, count);
			}

			// buy name, ... all
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
				int count;
				if(shop.isUnlimitedStock())
				{
					// get player avail space
					count = countAvailableSpaceForItemInInventory(
							player.getInventory(), item);
				}else{
					// use shop stock
					count = shop.getItem(item.name).getStock();
				}
				if(count < 1)
				{
					sender.sendMessage("You must buy at least one " + item.name
							+ "!");
					return true;
				}
				return shopBuy(shop, item, count);
			}

		}else{
			sender.sendMessage("Shops only serve players, sorry.");
			return true;
		}

		// Command matching

		// buy int
		Pattern pattern = Pattern.compile("(?i)buy\\s+(\\d+)$");
		Matcher matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			ItemInfo item = Search.itemById(id);
			if(item == null)
			{
				sender.sendMessage("Could not find an item.");
				return true;
			}
			return shopBuy(shop, item, 0);
		}

		// buy int int
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
			if(count < 1)
			{
				sender.sendMessage("You must buy at least one " + item.name
						+ "!");
				return true;
			}
			return shopBuy(shop, item, count);
		}

		// buy int:int
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
			return shopBuy(shop, item, 0);
		}

		// buy int:int int
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
			if(count < 1)
			{
				sender.sendMessage("You must buy at least one " + item.name
						+ "!");
				return true;
			}
			return shopBuy(shop, item, count);
		}

		// buy name, ... int
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
			if(count < 1)
			{
				sender.sendMessage("You must buy at least one " + item.name
						+ "!");
				return true;
			}
			return shopBuy(shop, item, count);
		}

		// buy name, ...
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
			return shopBuy(shop, item, 0);
		}

		// Show sell help
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
	 * the item requested
	 * @param amount
	 * the requested quantity of the item
	 * @return true on success
	 */
	private boolean shopBuy(Shop shop, ItemInfo item, int amount)
	{
		if(!(sender instanceof Player))
		{
			sender.sendMessage("/shop buy can only be used for players!");
			return false;
		}

		Player player = (Player)sender;
		InventoryItem invItem = shop.getItem(item.name);
		PlayerData pData = plugin.getPlayerData().get(player.getName());

		// check if the shop is selling that item
		if(invItem == null || invItem.getBuyPrice() == 0)
		{
			player.sendMessage(ChatColor.DARK_AQUA + "Sorry, "
					+ ChatColor.WHITE + shop.getName() + ChatColor.DARK_AQUA
					+ " is not selling " + ChatColor.WHITE + item.name
					+ ChatColor.DARK_AQUA + " right now.");
			return false;
		}else if(invItem.getStock() == 0 && !shop.isUnlimitedStock())
		{
			player.sendMessage(ChatColor.DARK_AQUA + "Sorry, "
					+ ChatColor.WHITE + shop.getName() + ChatColor.DARK_AQUA
					+ " is sold out of " + ChatColor.WHITE + item.name
					+ ChatColor.DARK_AQUA + " right now.");
			return false;
		}

		// check if the item has a price, or if this is a shop owner
		if(invItem.getBuyPrice() == 0 && !isShopController(shop))
		{
			player.sendMessage(ChatColor.DARK_AQUA + "Sorry, "
					+ ChatColor.WHITE + shop.getName() + ChatColor.DARK_AQUA
					+ " is not selling " + ChatColor.WHITE + item.name
					+ ChatColor.DARK_AQUA + " right now.");
			return false;
		}

		// if amount = 0, assume single stack size
		if(amount == 0)
		{
			amount = invItem.getBuySize();
		}

		int totalAmount;
		totalAmount = invItem.getStock();

		if(totalAmount == 0 && !shop.isUnlimitedStock())
		{
			player.sendMessage(ChatColor.DARK_AQUA + "The shop has "
					+ ChatColor.WHITE + totalAmount + " " + item.name);
			return true;
		}

		if(amount < 0)
		{
			amount = 0;
		}

		if(shop.isUnlimitedStock())
		{
			totalAmount = amount;
		}
		if(amount > totalAmount)
		{
			amount = totalAmount - (totalAmount % invItem.getBuySize());
			if(!shop.isUnlimitedStock())
			{
				player.sendMessage(ChatColor.DARK_AQUA + "The shop has "
						+ ChatColor.WHITE + totalAmount + " " + item.name);
			}
		}else if(amount % invItem.getBuySize() != 0){
			amount = amount - (amount % invItem.getBuySize());
			player.sendMessage(ChatColor.DARK_AQUA + "The bundle size is  "
					+ ChatColor.WHITE + invItem.getBuySize()
					+ ChatColor.DARK_AQUA + " order reduced to "
					+ ChatColor.WHITE + amount);
		}

		// check how many items the user has room for
		int freeSpots = countAvailableSpaceForItemInInventory(player.getInventory(), item);

		// Calculate the amount the player can store
		if(amount > freeSpots)
		{
			amount = freeSpots - (freeSpots % invItem.getBuySize());
			player.sendMessage(ChatColor.DARK_AQUA + "You only have room for "
					+ ChatColor.WHITE + amount);
		}

		// calculate cost
		int bundles = amount / invItem.getBuySize();
		double itemPrice = invItem.getBuyPrice();
		// recalculate # of items since may not fit cleanly into bundles
		amount = bundles * invItem.getBuySize();
		double totalCost = bundles * itemPrice;

		// try to pay the shop owner
		if(!isShopController(shop))
		{
			if(!pData.payPlayer(player.getName(), shop.getOwner(), totalCost))
			{
				// player doesn't have enough money
				// get player's balance and calculate how many it can buy
				double playerBalance = pData.getBalance(player.getName());
				int bundlesCanAford = (int)Math
						.floor(playerBalance / itemPrice);
				totalCost = bundlesCanAford * itemPrice;
				amount = bundlesCanAford * invItem.getSellSize();

				if(bundlesCanAford == 0)
				{
					player.sendMessage(ChatColor.DARK_AQUA
							+ "You cannot afford any of " + ChatColor.WHITE
							+ item.name);
					return true;
				}else{
					player.sendMessage(ChatColor.DARK_AQUA
							+ "You could only afford " + ChatColor.WHITE
							+ amount + ChatColor.DARK_AQUA + " of "
							+ ChatColor.WHITE + item.name);
				}

				if(!pData.payPlayer(player.getName(), shop.getOwner(), totalCost))
				{
					player.sendMessage(CommandShops.CHAT_PREFIX
							+ ChatColor.DARK_AQUA
							+ "Unexpected money problem: could not complete sale.");
					return true;
				}
			}
		}

		if(!shop.isUnlimitedStock())
		{
			shop.removeStock(item.name, amount);
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
					+ plugin.getEconManager().format(totalCost));
		}

		// log the transaction
		int stock = invItem.getStock();
		int startStock = stock + amount;
		if(shop.isUnlimitedStock())
		{
			startStock = 0;
		}
		plugin.getShopData().logItems(player.getName(), shop.getName(),
				"buy-item", item.name, amount, startStock, stock);
		shop.addTransaction(new Transaction(Transaction.Type.Sell, player
				.getName(), item.name, amount, totalCost));

		//actually deliver the items
		givePlayerItem(item.toStack(), amount);
		plugin.getShopData().saveShop(shop);

		return true;
	}
}
