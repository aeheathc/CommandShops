package com.aehdev.commandshops;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

import cuboidLocale.PrimitiveCuboid;

// TODO: Auto-generated Javadoc
/**
 * The Class Shop.
 */
public class Shop implements Comparator<Shop>
{
	// Attributes
	/** The uuid. */
	private UUID uuid = null;

	/** The world. */
	private String world = null;

	/** The name. */
	private String name = null;

	/** The location a. */
	private ShopLocation locationA = null;

	/** The location b. */
	private ShopLocation locationB = null;

	/** The owner. */
	private String owner = null;

	/** The creator. */
	private String creator = null;

	/** The managers. */
	private ArrayList<String> managers = new ArrayList<String>();

	/** The unlimited money. */
	private boolean unlimitedMoney = false;

	/** The unlimited stock. */
	private boolean unlimitedStock = false;

	/** The inventory. */
	private HashMap<String,InventoryItem> inventory = new HashMap<String,InventoryItem>();

	/** The cuboid. */
	private PrimitiveCuboid cuboid = null;

	/** The min balance. */
	private double minBalance = 0;

	/** The transactions. */
	private ArrayBlockingQueue<Transaction> transactions;

	/** The notification. */
	private boolean notification = true;

	// Logging
	/** The Constant log. */
	private static final Logger log = Logger.getLogger("Minecraft");

	/**
	 * Instantiates a new shop.
	 * @param uuid
	 * the uuid
	 */
	public Shop(UUID uuid)
	{
		this.uuid = uuid;
		transactions = new ArrayBlockingQueue<Transaction>(
				Config.LOG_LIMIT);
	}

	/**
	 * Gets the uuid.
	 * @return the uuid
	 */
	public UUID getUuid()
	{
		return uuid;
	}

	/**
	 * Sets the world.
	 * @param name
	 * the new world
	 */
	public void setWorld(String name)
	{
		world = name;
	}

	/**
	 * Gets the world.
	 * @return the world
	 */
	public String getWorld()
	{
		return world;
	}

	/**
	 * Sets the name.
	 * @param name
	 * the new name
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Gets the name.
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the locations.
	 * @param locationA
	 * the location a
	 * @param locationB
	 * the location b
	 */
	public void setLocations(ShopLocation locationA, ShopLocation locationB)
	{
		this.locationA = locationA;
		this.locationB = locationB;
	}

	/**
	 * Sets the location a.
	 * @param locationA
	 * the new location a
	 */
	public void setLocationA(ShopLocation locationA)
	{
		this.locationA = locationA;
	}

	/**
	 * Sets the location a.
	 * @param x
	 * the x
	 * @param y
	 * the y
	 * @param z
	 * the z
	 */
	public void setLocationA(long x, long y, long z)
	{
		locationA = new ShopLocation(x, y, z);
	}

	/**
	 * Sets the location b.
	 * @param locationB
	 * the new location b
	 */
	public void setLocationB(ShopLocation locationB)
	{
		this.locationB = locationB;
	}

	/**
	 * Sets the location b.
	 * @param x
	 * the x
	 * @param y
	 * the y
	 * @param z
	 * the z
	 */
	public void setLocationB(long x, long y, long z)
	{
		locationB = new ShopLocation(x, y, z);
	}

	/**
	 * Gets the locations.
	 * @return the locations
	 */
	public ShopLocation[] getLocations()
	{
		return new ShopLocation[]{locationA, locationB};
	}

	/**
	 * Gets the location a.
	 * @return the location a
	 */
	public ShopLocation getLocationA()
	{
		return locationA;
	}

	/**
	 * Gets the location b.
	 * @return the location b
	 */
	public ShopLocation getLocationB()
	{
		return locationB;
	}

	/**
	 * Gets the location center.
	 * @return the location center
	 */
	public ShopLocation getLocationCenter()
	{
		double[] xyz = new double[3];
		double[] xyzA = locationA.toArray();
		double[] xyzB = locationA.toArray();

		for(int i = 0; i < 3; i++)
		{
			if(xyzA[i] < xyzB[i])
			{
				xyz[i] = xyzA[i] + (Math.abs(xyzA[i] - xyzB[i])) / 2;
			}else
			{
				xyz[i] = xyzA[i] - (Math.abs(xyzA[i] - xyzB[i])) / 2;
			}
		}

		return new ShopLocation(xyz);
	}

	/**
	 * Sets the owner.
	 * @param owner
	 * the new owner
	 */
	public void setOwner(String owner)
	{
		this.owner = owner;
	}

	/**
	 * Sets the creator.
	 * @param creator
	 * the new creator
	 */
	public void setCreator(String creator)
	{
		this.creator = creator;
	}

	/**
	 * Gets the owner.
	 * @return the owner
	 */
	public String getOwner()
	{
		return owner;
	}

	/**
	 * Gets the creator.
	 * @return the creator
	 */
	public String getCreator()
	{
		return creator;
	}

	/**
	 * Sets the unlimited stock.
	 * @param b
	 * the new unlimited stock
	 */
	public void setUnlimitedStock(boolean b)
	{
		unlimitedStock = b;
	}

	/**
	 * Sets the unlimited money.
	 * @param b
	 * the new unlimited money
	 */
	public void setUnlimitedMoney(boolean b)
	{
		unlimitedMoney = b;
	}

	/**
	 * Gets the item.
	 * @param item
	 * the item
	 * @return the item
	 */
	public InventoryItem getItem(String item)
	{
		return inventory.get(item);
	}

	/**
	 * Contains item.
	 * @param item
	 * the item
	 * @return true, if successful
	 */
	public boolean containsItem(ItemInfo item)
	{
		Iterator<InventoryItem> it = inventory.values().iterator();
		while(it.hasNext())
		{
			InventoryItem invItem = it.next();
			ItemInfo invItemInfo = invItem.getInfo();
			if(invItemInfo.typeId == item.typeId
					&& invItemInfo.subTypeId == item.subTypeId){ return true; }
		}
		return false;
	}

	/**
	 * Gets the short uuid string.
	 * @return the short uuid string
	 */
	public String getShortUuidString()
	{
		String sUuid = uuid.toString();
		return sUuid.substring(sUuid.length() - Config.UUID_MIN_LENGTH);
	}

	/**
	 * Gets the minimum account balance this shop allows.
	 * @return int minBalance
	 */
	public double getMinBalance()
	{
		return this.minBalance;
	}

	/**
	 * Sets the minBalance this shop allows.
	 * @param newBalance
	 * the new min balance
	 */
	public void setMinBalance(double newBalance)
	{
		this.minBalance = newBalance;
	}

	/**
	 * Sets the notification.
	 * @param setting
	 * the new notification
	 */
	public void setNotification(boolean setting)
	{
		this.notification = setting;
	}

	/**
	 * Gets the notification.
	 * @return the notification
	 */
	public boolean getNotification()
	{
		return notification;
	}

	/**
	 * Adds the item.
	 * @param itemNumber
	 * the item number
	 * @param itemData
	 * the item data
	 * @param buyPrice
	 * the buy price
	 * @param buyStackSize
	 * the buy stack size
	 * @param sellPrice
	 * the sell price
	 * @param sellStackSize
	 * the sell stack size
	 * @param stock
	 * the stock
	 * @param maxStock
	 * the max stock
	 * @return true, if successful
	 */
	public boolean addItem(int itemNumber, short itemData, double buyPrice,
			int buyStackSize, double sellPrice, int sellStackSize, int stock,
			int maxStock)
	{
		// TODO add maxStock to item object
		ItemInfo item = Search.itemById(itemNumber, itemData);
		if(item == null){ return false; }

		InventoryItem thisItem = new InventoryItem(item);

		thisItem.setBuy(buyPrice, buyStackSize);
		thisItem.setSell(sellPrice, sellStackSize);

		thisItem.setStock(stock);

		thisItem.maxStock = maxStock;

		if(inventory.containsKey(item.name))
		{
			inventory.remove(item.name);
		}

		inventory.put(item.name, thisItem);

		return true;
	}

	/**
	 * Sets the managers.
	 * @param managers
	 * the new managers
	 */
	public void setManagers(String[] managers)
	{
		this.managers = new ArrayList<String>();

		for(String manager: managers)
		{
			if(!manager.equals(""))
			{
				this.managers.add(manager);
			}
		}
	}

	/**
	 * Adds the manager.
	 * @param manager
	 * the manager
	 */
	public void addManager(String manager)
	{
		managers.add(manager);
	}

	/**
	 * Removes the manager.
	 * @param manager
	 * the manager
	 */
	public void removeManager(String manager)
	{
		managers.remove(manager);
	}

	/**
	 * Gets the managers.
	 * @return the managers
	 */
	public List<String> getManagers()
	{
		return managers;
	}

	/**
	 * Gets the items.
	 * @return the items
	 */
	public List<InventoryItem> getItems()
	{
		return new ArrayList<InventoryItem>(inventory.values());
	}

	/**
	 * Gets the inventory.
	 * @return the inventory
	 */
	public HashMap<String,InventoryItem> getInventory()
	{
		return new HashMap<String,InventoryItem>(inventory);
	}

	/**
	 * Checks if is unlimited stock.
	 * @return true, if is unlimited stock
	 */
	public boolean isUnlimitedStock()
	{
		return unlimitedStock;
	}

	/**
	 * Checks if is unlimited money.
	 * @return true, if is unlimited money
	 */
	public boolean isUnlimitedMoney()
	{
		return unlimitedMoney;
	}

	/**
	 * Adds the stock.
	 * @param itemName
	 * the item name
	 * @param amount
	 * the amount
	 * @return true, if successful
	 */
	public boolean addStock(String itemName, int amount)
	{
		if(!inventory.containsKey(itemName)){ return false; }
		inventory.get(itemName).addStock(amount);
		return true;
	}

	/**
	 * Removes the stock.
	 * @param itemName
	 * the item name
	 * @param amount
	 * the amount
	 * @return true, if successful
	 */
	public boolean removeStock(String itemName, int amount)
	{
		if(!inventory.containsKey(itemName)) return false;
		inventory.get(itemName).removeStock(amount);
		return true;
	}

	/**
	 * Sets the item buy price.
	 * @param itemName
	 * the item name
	 * @param price
	 * the price
	 */
	public void setItemBuyPrice(String itemName, double price)
	{
		inventory.get(itemName).setBuyPrice(price);
	}

	/**
	 * Sets the item buy amount.
	 * @param itemName
	 * the item name
	 * @param buySize
	 * the buy size
	 */
	public void setItemBuyAmount(String itemName, int buySize)
	{
		inventory.get(itemName).setBuySize(buySize);
	}

	/**
	 * Sets the item sell price.
	 * @param itemName
	 * the item name
	 * @param price
	 * the price
	 */
	public void setItemSellPrice(String itemName, double price)
	{
		inventory.get(itemName).setSellPrice(price);
	}

	/**
	 * Sets the item sell amount.
	 * @param itemName
	 * the item name
	 * @param sellSize
	 * the sell size
	 */
	public void setItemSellAmount(String itemName, int sellSize)
	{
		inventory.get(itemName).setSellSize(sellSize);
	}

	/**
	 * Removes the item.
	 * @param itemName
	 * the item name
	 */
	public void removeItem(String itemName)
	{
		inventory.remove(itemName);
	}

	/**
	 * Item max stock.
	 * @param itemName
	 * the item name
	 * @return the int
	 */
	public int itemMaxStock(String itemName)
	{
		return inventory.get(itemName).maxStock;
	}

	/**
	 * Sets the item max stock.
	 * @param itemName
	 * the item name
	 * @param maxStock
	 * the max stock
	 */
	public void setItemMaxStock(String itemName, int maxStock)
	{
		inventory.get(itemName).maxStock = maxStock;
	}

	/**
	 * Gets the transactions.
	 * @return the transactions
	 */
	public Queue<Transaction> getTransactions()
	{
		return transactions;
	}

	/**
	 * Removes the transaction.
	 * @param trans
	 * the trans
	 */
	public void removeTransaction(Transaction trans)
	{
		transactions.remove(trans);
	}

	/**
	 * Adds the transaction.
	 * @param trans
	 * the trans
	 */
	public void addTransaction(Transaction trans)
	{
		if(transactions.remainingCapacity() >= 1)
		{
			transactions.add(trans);
		}else
		{
			transactions.remove();
			transactions.add(trans);
		}
	}

	/**
	 * Clear transactions.
	 */
	public void clearTransactions()
	{
		transactions.clear();
	}

	/**
	 * Gets the cuboid.
	 * @return the cuboid
	 */
	public PrimitiveCuboid getCuboid()
	{
		// If no cuboid, create it
		if(cuboid == null)
		{
			// Check if either locaiton is null and return appropriately
			if(locationA == null || locationB == null){ return null; }
			cuboid = new PrimitiveCuboid(getLocationA().toArray(),
					getLocationB().toArray());
			cuboid.uuid = uuid;
			cuboid.world = world;
		}

		return cuboid;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString() */
	public String toString()
	{
		return String.format("Shop \"%s\" at [%s], [%s] %d items - %s",
				this.name, locationA.toString(), locationB.toString(),
				inventory.size(), uuid.toString());
	}

	/**
	 * Log.
	 */
	public void log()
	{
		// Details
		log.info("Shop Information");
		log.info(String.format("   %-16s %s", "UUID:", uuid.toString()));
		log.info(String.format("   %-16s %s", "Name:", name));
		log.info(String.format("   %-16s %s", "Creator:", creator));
		log.info(String.format("   %-16s %s", "Owner:", owner));
		log.info(String.format("   %-16s %s", "Managers:",
				Search.join(managers, ",")));
		log.info(String.format("   %-16s %.2f", "Minimum Balance:", minBalance));
		log.info(String.format("   %-16s %s", "Unlimited Money:",
				unlimitedMoney ? "Yes" : "No"));
		log.info(String.format("   %-16s %s", "Unlimited Stock:",
				unlimitedStock ? "Yes" : "No"));
		log.info(String.format("   %-16s %s", "Location A:",
				locationA.toString()));
		log.info(String.format("   %-16s %s", "Location B:",
				locationB.toString()));
		log.info(String.format("   %-16s %s", "World:", world));

		// Items
		log.info("Shop Inventory");
		log.info("   BP=Buy Price, BS=Buy Size, SP=Sell Price, SS=Sell Size, ST=Stock, MX=Max Stock");
		log.info(String.format("   %-9s %-6s %-3s %-6s %-3s %-3s %-3s", "Id",
				"BP", "BS", "SP", "SS", "ST", "MX"));
		Iterator<InventoryItem> it = inventory.values().iterator();
		while(it.hasNext())
		{
			InventoryItem item = it.next();
			ItemInfo info = item.getInfo();
			log.info(String.format(
					"   %6d:%-2d %-6.2f %-3d %-6.2f %-3d %-3d %-3d",
					info.typeId, info.subTypeId, item.getBuyPrice(),
					item.getBuySize(), item.getSellPrice(), item.getSellSize(),
					item.getStock(), item.getMaxStock()));
		}
	}

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object) */
	@Override
	public int compare(Shop o1, Shop o2)
	{
		return o1.getUuid().compareTo(o2.uuid);
	}
}
