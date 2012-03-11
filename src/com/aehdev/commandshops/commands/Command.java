package com.aehdev.commandshops.commands;

import java.sql.ResultSet;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.aehdev.commandshops.CommandShops;
import com.aehdev.commandshops.Config;
import com.aehdev.commandshops.ItemInfo;
import com.aehdev.commandshops.Search;
import com.aehdev.commandshops.Shop;
import com.aehdev.lib.PatPeter.SQLibrary.DatabaseHandler;

import cuboidLocale.BookmarkedResult;
import cuboidLocale.PrimitiveCuboid;

/**
 * Represents a CommandShops command. That is, each object is a sub-command of
 * /shop. For example, "/shop buy"
 */
public abstract class Command
{
	/** Reference to the main plugin object. */
	protected CommandShops plugin = null;
	
	/** Reference to database handler */
	protected DatabaseHandler db = CommandShops.db;

	/** The command name that the user typed (might be an alias). */
	protected String commandLabel = null;

	/** The sender/origin of the command. We will almost always need this to be
	 * a player, especially for commands that aren't read-only. */
	protected CommandSender sender = null;

	/** The actual backend command being executed. */
	protected String command = null;

	/** Matches valid numbers. */
	protected static String DECIMAL_REGEX = "(\\d+\\.\\d+)|(\\d+\\.)|(\\.\\d+)|(\\d+)";

	/** The logging object with which we write to the server log. */
	protected static final Logger log = Logger.getLogger("Minecraft");

	/**
	 * Groups commands by the permission needed to execute them.
	 */
	public static enum CommandTypes
	{

		/** Includes all other types as well as admin-only actions. */
		ADMIN(0, "commandshops.admin"),

		/** Adding items to shops. */
		ADD(1, "commandshops.manager.add"),

		/** Buying from shops */
		BUY(2, "commandshops.user.buy"),

		/** Creation of new shops. */
		CREATE(3, "commandshops.manager.create"),

		/** Bypass the fee for creating new shops. */
		CREATE_FREE(4, "commandshops.free.create"),

		/** Delete shops. */
		DESTROY(5, "commandshops.manager.destroy"),

		/** Browsing contents of shops. */
		BROWSE(6, "commandshops.user.browse"),

		/** Relocating existing shops. */
		MOVE(7, "commandshops.manager.move"),

		/** Bypass the fee for moving shops. */
		MOVE_FREE(8, "commandshops.free.move"),

		/** Taking items out of shops. */
		REMOVE(9, "commandshops.manager.remove"),

		/** Selling to shops. */
		SELL(10, "commandshops.user.sell"),

		/** Changing the owner of shops currently under one's authority. */
		SET_OWNER(11,"commandshops.manager.set.owner"),

		/** Set shop parameters like prices. */
		SET(12, "commandshops.manager.set");

		/** Identifies this CommandType. */
		int id = -1;

		/** List of all permissions needed to use a certain type of command. */
		String permission = null;

		/**
		 * Define a command type.
		 * @param id
		 * the id
		 */
		CommandTypes(int id)
		{
			this.id = id;
		}

		/**
		 * Define a command type.
		 * @param id
		 * the id
		 * @param permission
		 * Permission needed for this command type.
		 */
		CommandTypes(int id, String permission)
		{
			this(id);
			this.permission = permission;
		}
	}

	/**
	 * Define a new command.
	 * @param plugin
	 * Reference back to main plugin object.
	 * @param commandLabel
	 * Alias typed by user for this command.
	 * @param sender
	 * Who sent the command. Should be a player, but might be console.
	 * @param command
	 * Name of command being run.
	 */
	public Command(CommandShops plugin, String commandLabel,
			CommandSender sender, String command)
	{
		this.plugin = plugin;
		this.commandLabel = commandLabel;
		this.sender = sender;
		this.command = command.trim();
	}

	/**
	 * Define a new command.
	 * @param plugin
	 * Reference back to main plugin object.
	 * @param commandLabel
	 * Alias typed by user for this command.
	 * @param sender
	 * Who sent the command. Should be a player, but might be console.
	 * @param args
	 * Arguments passed to the command.
	 */
	public Command(CommandShops plugin, String commandLabel,
			CommandSender sender, String[] args)
	{
		this(plugin, commandLabel, sender, Search.join(args, " ").trim());
	}

	/**
	 * Gets the name of the command represented by this object.
	 * @return the command
	 */
	public String getCommand()
	{
		return command;
	}

	/**
	 * Run the command.
	 * @return true, if successful
	 */
	public abstract boolean process();

	/**
	 * Determine if the sender of this command has permissions for commands of a given type.
	 * @param type
	 * Command type being checked for.
	 * @return true if the sender can access the given command type
	 */
	protected boolean canUseCommand(CommandTypes type)
	{
		if(sender instanceof Player)
		{
			Player player = (Player)sender;
			if(player.hasPermission(CommandTypes.ADMIN.permission) || player.hasPermission(type.permission))
				return true;
			return false;
		}else{
			return true;
		}
	}

	/**
	 * Check if the given player has permission to create shops.
	 * @param playerName
	 * Name of the player in question.
	 * @return true if the given player has permission to create shops.
	 */
	protected boolean canCreateShop(String playerName)
	{
		if(canUseCommand(CommandTypes.ADMIN))
		{
			return true;
		}else if(canUseCommand(CommandTypes.CREATE)){
			int owned = 0;
			try{
				ResultSet rsTotal = CommandShops.db.query("SELECT COUNT(*) FROM shops WHERE owner='" + playerName + "'");
				rsTotal.next();
				owned = rsTotal.getInt(1);
				rsTotal.close();
			}catch(Exception e){
				log.warning(String.format("[%s] - DB lost trying to check owned shop total.", CommandShops.pdfFile.getName()));
				return false;
			}
			
			if(owned <= Config.MAX_SHOPS_PER_PLAYER || Config.MAX_SHOPS_PER_PLAYER < 0)
				return true;
		}
		return false;
	}

	/**
	 * Check if this command's sender has access to modify a given shop.
	 * @param shop
	 * the shop they are attempting to modify
	 * @return true if this command's sender has access to modify the given shop.
	 */
	protected boolean canModifyShop(Shop shop)
	{
		if(sender instanceof Player)
		{
			Player player = (Player)sender;
			// If owner, true
			if(shop.getOwner().equals(player.getName())){ return true; }
			// If manager, true
			if(shop.getManagers().contains(player.getName())){ return true; }
			// If admin, true
			if(canUseCommand(CommandTypes.ADMIN)){ return true; }
			return false;
		}else{
			// Console, true
			return true;
		}
	}

	/**
	 * Check if a theoretical shop position would be acceptable.
	 * To meet this criteria, it must not overlap any existing shops.
	 * @param xyzA
	 * First of 2 points defining the cuboid
	 * @param xyzB
	 * Second of 2 points defining the cuboid
	 * @param worldName
	 * The world to check in
	 * @return true if the position is fine
	 */
	protected boolean shopPositionOk(double[] xyzA, double[] xyzB, String worldName)
	{
		BookmarkedResult res = new BookmarkedResult();

		// make sure coords are in right order
		for(int i = 0; i < 3; i++)
		{
			if(xyzA[i] > xyzB[i])
			{
				double temp = xyzA[i];
				xyzA[i] = xyzB[i];
				xyzB[i] = temp;
			}
		}

		// Need to test every position to account for variable shop sizes

		for(double x = xyzA[0]; x <= xyzB[0]; x++)
		{
			for(double z = xyzA[2]; z <= xyzB[2]; z++)
			{
				for(double y = xyzA[1]; y <= xyzB[1]; y++)
				{
					res = CommandShops.getCuboidTree().relatedSearch(
							res.bookmark, x, y, z);
					if(shopOverlaps(res, worldName)) return false;
				}
			}
		}
		return true;
	}

	/**
	 * Check if a list of numerically-overlapping shops are actually in the same world
	 * as the one we were checking against.
	 * @param res
	 * the set of shops found by the numeric overlap algorithm
	 * @param worldName
	 * name of the world to check in
	 * @return true if the overlap is real
	 */
	protected boolean shopOverlaps(BookmarkedResult res, String worldName)
	{
		if(res.results.size() != 0)
		{
			for(PrimitiveCuboid cuboid: res.results)
			{
				if(cuboid.world.equalsIgnoreCase(worldName))
				{
					sender.sendMessage(CommandShops.CHAT_PREFIX
							+ ChatColor.DARK_AQUA
							+ "Could not create shop, it overlaps with shop "
							+ ChatColor.WHITE + cuboid.id);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Give some amount of an item to a player.
	 * @param item
	 * the item type
	 * @param amount
	 * how many
	 */
	protected void givePlayerItem(ItemStack item, int amount)
	{
		Player player = (Player)sender;
		ItemInfo info = Search.itemById(item.getTypeId(), item.getDurability());
		int maxStackSize = info.maxStackSize;

		// fill all the existing stacks first
		for(int i: player.getInventory().all(item.getType()).keySet())
		{
			if(amount == 0) continue;
			ItemStack thisStack = player.getInventory().getItem(i);
			if(thisStack.getType().equals(item.getType())
					&& thisStack.getDurability() == item.getDurability())
			{
				if(thisStack.getAmount() < maxStackSize)
				{
					int remainder = maxStackSize - thisStack.getAmount();
					if(remainder <= amount)
					{
						amount -= remainder;
						thisStack.setAmount(maxStackSize);
					}else{
						thisStack.setAmount(maxStackSize - remainder + amount);
						amount = 0;
					}
				}
			}

		}

		for(int i = 0; i < 36; i++)
		{
			ItemStack thisSlot = player.getInventory().getItem(i);
			if(thisSlot == null || thisSlot.getType() == Material.AIR)
			{
				if(amount == 0) continue;
				if(amount >= maxStackSize)
				{
					item.setAmount(maxStackSize);
					player.getInventory().setItem(i, item);
					amount -= maxStackSize;
				}else
				{
					item.setAmount(amount);
					player.getInventory().setItem(i, item);
					amount = 0;
				}
			}
		}

		while(amount > 0)
		{
			if(amount >= maxStackSize)
			{
				item.setAmount(maxStackSize);
				amount -= maxStackSize;
			}else
			{
				item.setAmount(amount - maxStackSize);
				amount = 0;
			}
			player.getWorld().dropItemNaturally(player.getLocation(), item);
		}

	}

	/**
	 * Returns true if the sender is the owner or a manager of a given shop.
	 * @param shop
	 * the shop to check against
	 * @return true, if the sender can control the shop
	 */
	protected boolean isShopController(long shop)
	{
		if(!(sender instanceof Player)) return false;
		Player player = (Player)sender;
		try{
			String ownQuery = String.format("SELECT id FROM shops WHERE id=%d AND owner='%s' LIMIT 1",shop,db.escape(player.getName()));
			ResultSet resOwn = CommandShops.db.query(ownQuery);
			boolean owner = resOwn.next();
			resOwn.close();
			if(owner) return true;
			String manQuery = String.format("SELECT shop FROM managers WHERE shop=%d AND manager='%s' LIMIT 1",shop,db.escape(player.getName()));
			ResultSet resMan = CommandShops.db.query(manQuery);
			boolean manager = resMan.next();
			resMan.close();
			if(manager) return true;
		}catch(Exception e){
			log.warning(String.format("[%s] - Problem reading shop controllers: "+e, CommandShops.pdfFile.getName()));
		}
		return false;
	}

	/**
	 * Get how many of a certain item is in a player's inventory.
	 * @param inventory
	 * player's inventory
	 * @param item
	 * item type to count
	 * @return the total
	 */
	protected int countItemsInInventory(PlayerInventory inventory, ItemStack item)
	{
		int totalAmount = 0;
		boolean isDurable = CommandShops.getItemList().isDurable(item);

		for(Integer i: inventory.all(item.getType()).keySet())
		{
			ItemStack thisStack = inventory.getItem(i);
			if(isDurable)
			{
				int damage = calcDurabilityPercentage(thisStack);
				if(damage > Config.MAX_DAMAGE
						&& Config.MAX_DAMAGE != 0) continue;
			}else{
				if(thisStack.getDurability() != item.getDurability()) continue;
			}
			totalAmount += thisStack.getAmount();
		}

		return totalAmount;
	}

	/**
	 * Compute the durability percentage of an item based on it's current and max. 
	 * @param item
	 * the item to inspect
	 * @return the item's durability as a percentage
	 */
	protected static int calcDurabilityPercentage(ItemStack item)
	{
		// calc durability prcnt
		return (short)((double)item.getDurability()
				/ (double)item.getType().getMaxDurability() * 100);
	}

	/**
	 * Take items from a player.
	 * @param inventory
	 * player's inventory
	 * @param item
	 * item type to take
	 * @param amount
	 * how many of the item to take
	 * @return remaining number of items that should have been taken but were not
	 * because the player ran out. Will be zero on success. 
	 */
	protected int removeItemsFromInventory(PlayerInventory inventory, ItemStack item, int amount)
	{

		boolean isDurable = CommandShops.getItemList().isDurable(item);

		// remove number of items from player adding stock
		for(int i: inventory.all(item.getType()).keySet())
		{
			if(amount == 0) continue;
			ItemStack thisStack = inventory.getItem(i);
			if(isDurable)
			{
				int damage = calcDurabilityPercentage(thisStack);
				if(damage > Config.MAX_DAMAGE
						&& Config.MAX_DAMAGE != 0) continue;
			}else{
				if(thisStack.getDurability() != item.getDurability()) continue;
			}

			int foundAmount = thisStack.getAmount();
			if(amount >= foundAmount)
			{
				amount -= foundAmount;
				inventory.setItem(i, null);
			}else{
				thisStack.setAmount(foundAmount - amount);
				inventory.setItem(i, thisStack);
				amount = 0;
			}
		}

		return amount;

	}

	/**
	 * Figure out how many of a particular item type the player has space for.
	 * @param inventory
	 * player's inventory
	 * @param item
	 * item type
	 * @return how many
	 */
	protected int countAvailableSpaceForItemInInventory(PlayerInventory inventory, ItemInfo item)
	{
		int count = 0;
		for(ItemStack thisSlot: inventory.getContents())
		{
			if(thisSlot == null || thisSlot.getType() == Material.AIR)
			{
				count += item.maxStackSize;
				continue;
			}
			if(thisSlot.getTypeId() == item.typeId
					&& thisSlot.getDurability() == item.subTypeId)
			{
				count += item.maxStackSize - thisSlot.getAmount();
			}
		}

		return count;
	}
}
