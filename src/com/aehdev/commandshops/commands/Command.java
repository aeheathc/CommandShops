package com.aehdev.commandshops.commands;

import java.util.Iterator;
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
import com.aehdev.commandshops.PlayerData;
import com.aehdev.commandshops.Search;
import com.aehdev.commandshops.Shop;
import com.aehdev.commandshops.ShopLocation;

import cuboidLocale.BookmarkedResult;
import cuboidLocale.PrimitiveCuboid;

// TODO: Auto-generated Javadoc
/**
 * Represents a CommandShops command. That is, each object is a sub-command of
 * /shop. For example, "/shop buy"
 */
public abstract class Command
{
	/** Reference to the main plugin object. */
	protected CommandShops plugin = null;

	/** The command label. */
	protected String commandLabel = null;

	/** The sender/origin of the command. We will almost always need this to be
	 * a player, especially for commands that aren't read-only. */
	protected CommandSender sender = null;

	/** The command. */
	protected String command = null;

	/** The DECIMA l_ regex. */
	protected static String DECIMAL_REGEX = "(\\d+\\.\\d+)|(\\d+\\.)|(\\.\\d+)|(\\d+)";

	/** The logging object with which we write to the server log. */
	protected static final Logger log = Logger.getLogger("Minecraft");

	/**
	 * The Enum CommandTypes.
	 */
	public static enum CommandTypes
	{

		/** The ADMIN. */
		ADMIN(0, new String[]{"commandshops.admin"}),

		/** The ADD. */
		ADD(1, new String[]{"commandshops.manager.add"}),

		/** The BUY. */
		BUY(2, new String[]{"commandshops.user.buy"}),

		/** The CREATE. */
		CREATE(3, new String[]{"commandshops.manager.create"}),

		/** The CREAT e_ free. */
		CREATE_FREE(4, new String[]{"commandshops.free.create"}),

		/** The DESTROY. */
		DESTROY(5, new String[]{"commandshops.manager.destroy"}),

		/** The HELP. */
		HELP(6, new String[]{}),

		/** The BROWSE. */
		BROWSE(7, new String[]{"commandshops.user.browse"}),

		/** The MOVE. */
		MOVE(8, new String[]{"commandshops.manager.move"}),

		/** The MOV e_ free. */
		MOVE_FREE(9, new String[]{"commandshops.free.move"}),

		/** The REMOVE. */
		REMOVE(10, new String[]{"commandshops.manager.remove"}),

		/** The SEARCH. */
		SEARCH(11, new String[]{}),

		/** The SELECT. */
		SELECT(12, new String[]{"commandshops.manager.select"}),

		/** The SELL. */
		SELL(13, new String[]{"commandshops.user.sell"}),

		/** The SE t_ owner. */
		SET_OWNER(14, new String[]{"commandshops.manager.set.owner"}),

		/** The SET. */
		SET(15, new String[]{"commandshops.manager.set"});

		/** The id. */
		int id = -1;

		/** The permissions. */
		String[] permissions = null;

		/**
		 * Instantiates a new command types.
		 * @param id
		 * the id
		 */
		CommandTypes(int id)
		{
			this.id = id;
		}

		/**
		 * Instantiates a new command types.
		 * @param id
		 * the id
		 * @param permissions
		 * the permissions
		 */
		CommandTypes(int id, String[] permissions)
		{
			this(id);
			this.permissions = permissions;
		}

		/**
		 * Gets the id.
		 * @return the id
		 */
		public int getId()
		{
			return id;
		}

		/**
		 * Gets the permissions.
		 * @return the permissions
		 */
		public String[] getPermissions()
		{
			return permissions;
		}
	}

	/**
	 * Instantiates a new command.
	 * @param plugin
	 * the plugin
	 * @param commandLabel
	 * the command label
	 * @param sender
	 * the sender
	 * @param command
	 * the command
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
	 * Instantiates a new command.
	 * @param plugin
	 * the plugin
	 * @param commandLabel
	 * the command label
	 * @param sender
	 * the sender
	 * @param args
	 * the args
	 */
	public Command(CommandShops plugin, String commandLabel,
			CommandSender sender, String[] args)
	{
		this(plugin, commandLabel, sender, Search.join(args, " ").trim());
	}

	/**
	 * Gets the command.
	 * @return the command
	 */
	public String getCommand()
	{
		return command;
	}

	/**
	 * Process.
	 * @return true, if successful
	 */
	public boolean process()
	{
		// Does nothing and needs to be overloaded by subclasses
		return false;
	}

	/**
	 * Can use command.
	 * @param type
	 * the type
	 * @return true, if successful
	 */
	protected boolean canUseCommand(CommandTypes type)
	{
		if(sender instanceof Player)
		{
			Player player = (Player)sender;

			// check if admin first
			for(String permission: CommandTypes.ADMIN.getPermissions())
			{
				if(player.hasPermission(permission)){ return true; }
			}

			// fail back to provided permissions second
			for(String permission: type.getPermissions())
			{
				if(!player.hasPermission(permission)){ return false; }
			}
			return true;
		}else
		{
			return true;
		}
	}

	/**
	 * Can create shop.
	 * @param playerName
	 * the player name
	 * @return true, if successful
	 */
	protected boolean canCreateShop(String playerName)
	{
		if(canUseCommand(CommandTypes.ADMIN))
		{
			return true;
		}else if((plugin.getShopData().numOwnedShops(playerName) < Config.MAX_SHOPS_PER_PLAYER || Config.MAX_SHOPS_PER_PLAYER < 0)
				&& canUseCommand(CommandTypes.CREATE)){ return true; }

		return false;
	}

	/**
	 * Can modify shop.
	 * @param shop
	 * the shop
	 * @return true, if successful
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
		}else
		{
			// Console, true
			return true;
		}
	}

	/**
	 * Shop position ok.
	 * @param xyzA
	 * the xyz a
	 * @param xyzB
	 * the xyz b
	 * @param worldName
	 * the world name
	 * @return true, if successful
	 */
	protected boolean shopPositionOk(double[] xyzA, double[] xyzB,
			String worldName)
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
	 * Shop overlaps.
	 * @param res
	 * the res
	 * @param worldName
	 * the world name
	 * @return true, if successful
	 */
	protected boolean shopOverlaps(BookmarkedResult res, String worldName)
	{
		if(res.results.size() != 0)
		{
			for(PrimitiveCuboid cuboid: res.results)
			{
				if(cuboid.uuid != null)
				{
					if(cuboid.world.equalsIgnoreCase(worldName))
					{
						Shop shop = plugin.getShopData().getShop(cuboid.uuid);
						sender.sendMessage(CommandShops.CHAT_PREFIX
								+ ChatColor.DARK_AQUA
								+ "Could not create shop, it overlaps with "
								+ ChatColor.WHITE + shop.getName());
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Give player item.
	 * @param item
	 * the item
	 * @param amount
	 * the amount
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
					}else
					{
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
	 * Returns true if the player is in the shop manager list or is the shop
	 * owner.
	 * @param shop
	 * the shop
	 * @return true, if is shop controller
	 * @return
	 */
	protected boolean isShopController(Shop shop)
	{
		if(sender instanceof Player)
		{
			Player player = (Player)sender;
			if(shop.getOwner().equalsIgnoreCase(player.getName())) return true;
			if(shop.getManagers() != null)
			{
				for(String manager: shop.getManagers())
				{
					if(player.getName().equalsIgnoreCase(manager)){ return true; }
				}
			}
			return false;
		}else
		{
			return true;
		}
	}

	/**
	 * Count items in inventory.
	 * @param inventory
	 * the inventory
	 * @param item
	 * the item
	 * @return the int
	 */
	protected int countItemsInInventory(PlayerInventory inventory,
			ItemStack item)
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
			}else
			{
				if(thisStack.getDurability() != item.getDurability()) continue;
			}
			totalAmount += thisStack.getAmount();
		}

		return totalAmount;
	}

	/**
	 * Calc durability percentage.
	 * @param item
	 * the item
	 * @return the int
	 */
	protected static int calcDurabilityPercentage(ItemStack item)
	{

		// calc durability prcnt
		short damage;
		if(item.getType() == Material.IRON_SWORD)
		{
			damage = (short)((double)item.getDurability() / 250 * 100);
		}else
		{
			damage = (short)((double)item.getDurability()
					/ (double)item.getType().getMaxDurability() * 100);
		}

		return damage;
	}

	/**
	 * Removes the items from inventory.
	 * @param inventory
	 * the inventory
	 * @param item
	 * the item
	 * @param amount
	 * the amount
	 * @return the int
	 */
	protected int removeItemsFromInventory(PlayerInventory inventory,
			ItemStack item, int amount)
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
			}else
			{
				if(thisStack.getDurability() != item.getDurability()) continue;
			}

			int foundAmount = thisStack.getAmount();
			if(amount >= foundAmount)
			{
				amount -= foundAmount;
				inventory.setItem(i, null);
			}else
			{
				thisStack.setAmount(foundAmount - amount);
				inventory.setItem(i, thisStack);
				amount = 0;
			}
		}

		return amount;

	}

	/**
	 * Count available space for item in inventory.
	 * @param inventory
	 * the inventory
	 * @param item
	 * the item
	 * @return the int
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

	/**
	 * Notify players.
	 * @param shop
	 * the shop
	 * @param messages
	 * the messages
	 * @return true, if successful
	 */
	protected boolean notifyPlayers(Shop shop, String[] messages)
	{
		Iterator<PlayerData> it = plugin.getPlayerData().values().iterator();
		while(it.hasNext())
		{
			PlayerData p = it.next();
			if(p.shopList.contains(shop.getUuid()))
			{
				Player thisPlayer = plugin.getServer().getPlayer(p.playerName);
				for(String message: messages)
				{
					thisPlayer.sendMessage(message);
				}
			}
		}
		return true;
	}

	/**
	 * Calculate distance.
	 * @param from
	 * the from
	 * @param to
	 * the to
	 * @return the double
	 */
	protected double calculateDistance(ShopLocation from, ShopLocation to)
	{
		double x1 = from.getX();
		double x2 = to.getX();

		double y1 = from.getY();
		double y2 = to.getY();

		double z1 = from.getZ();
		double z2 = to.getZ();

		double distance = Math.sqrt(Math.pow((x1 - x2), 2)
				+ Math.pow((y1 - y2), 2) + Math.pow((z1 - z2), 2));

		return distance;
	}
}
