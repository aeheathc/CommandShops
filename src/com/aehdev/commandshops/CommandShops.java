package com.aehdev.commandshops;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import com.aehdev.commandshops.commands.ShopCommandExecutor;
import com.aehdev.commandshops.modules.economy.EconomyManager;
import com.aehdev.commandshops.threads.NotificationThread;

import cuboidLocale.QuadTree;

//TODO: Auto-generated Javadoc
/**
 * The main Bukkit plugin class for CommandShops.
 */
public class CommandShops extends JavaPlugin
{
	// Listeners & Objects
	/** Our class extending PlayerListener that handles player interaction */
	public ShopsPlayerListener playerListener = new ShopsPlayerListener(this);

	/** Single object that holds all data for everyone's shops */
	private ShopData shopData = new ShopData(this);

	/** General plugin info that comes directly from the private
	 * {@link JavaPlugin#description} in the parent. Effectively all we're
	 * doing here is converting it to public. */
	public PluginDescriptionFile pdfFile = null;

	/** Thread that notifies managers of transactions. */
	protected NotificationThread notificationThread = null;

	/** Abstracts supported economies. */
	private EconomyManager econManager = null;

	/** Main logger with which we write to the server log. */
	private final Logger log = Logger.getLogger("Minecraft");

	/** Plugin-identifying string that prefixes every message we show to players */
	public static final String CHAT_PREFIX = ChatColor.DARK_AQUA + "["
			+ ChatColor.WHITE + "Shop" + ChatColor.DARK_AQUA + "] ";

	/** All shop locations stored in a cuboid tree for fast chacking. */
	private static QuadTree cuboidTree = new QuadTree();

	/** Path to all our data files. */
	static String folderPath = "plugins/CommandShops/";

	/** Folder within {@link folderPath} that contains the shop files. */
	static String shopsPath = "shops/";

	/** The item list. */
	private static ItemData itemList = new ItemData();

	/** The player data. */
	private Map<String,PlayerData> playerData; // synchronized player hash

	/* (non-Javadoc)
	 * @see org.bukkit.plugin.Plugin#onEnable() */
	public void onEnable()
	{
		pdfFile = getDescription();
		setPlayerData(Collections
				.synchronizedMap(new HashMap<String,PlayerData>()));

		// add all the online users to the data trees
		for(Player player: this.getServer().getOnlinePlayers())
		{
			getPlayerData().put(player.getName(),
					new PlayerData(this, player.getName()));
		}

		// Register our events
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener,
				Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener,
				Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener,
				Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener,
				Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLAYER_KICK, playerListener,
				Priority.Monitor, this);

		// Register Commands
		getCommand("shop").setExecutor(new ShopCommandExecutor(this));

		// setup the file IO
		(new File(folderPath)).mkdir();

		File shopsDir = new File(folderPath + shopsPath);
		shopsDir.mkdir();

		Config.loadProperties(this);		

		// read the shops into memory
		getShopData().loadShops(shopsDir);

		// update the console that we've started
		log.info(String.format("[%s] %s", pdfFile.getName(), "Loaded with "
				+ getShopData().getNumShops() + " shop(s)"));
		log.info(String.format("[%s] %s", pdfFile.getName(),
				"Version " + pdfFile.getVersion() + " is enabled: "));

		// check which shops players are inside
		for(Player player: this.getServer().getOnlinePlayers())
		{
			playerListener.checkPlayerPosition(player);
		}

		// Start Notification thread
		if(Config.NOTIFY_INTERVAL > 0)
		{
			notificationThread = new NotificationThread(this);
			notificationThread.start();
		}

		setEconManager(new EconomyManager(this));
		log.info(String
				.format("[%s][Economy] Register activated. It should report 'Payment method found' soon.",
						pdfFile.getName()));
	}

	/* (non-Javadoc)
	 * @see org.bukkit.plugin.Plugin#onDisable() */
	public void onDisable()
	{
		// Save all shops
		getShopData().saveAllShops();

		// Stop notification thread
		if((Config.NOTIFY_INTERVAL > 0) && notificationThread != null
				&& notificationThread.isAlive())
		{
			try
			{
				notificationThread.setRun(false);
				notificationThread.join(2000);
			}catch(InterruptedException e){
				// hmm, thread didn't die
				log.warning(String.format("[%s] %s", pdfFile.getName(),
						"NotificationThread did not exit"));
			}
		}

		// update the console that we've stopped
		log.info(String.format("[%s] %s", pdfFile.getName(), "Version "
				+ pdfFile.getVersion() + " is disabled!"));
	}



	/**
	 * Sets the shop data.
	 * @param shopData
	 * the new shop data
	 */
	public void setShopData(ShopData shopData)
	{
		this.shopData = shopData;
	}

	/**
	 * Gets the shop data.
	 * @return the shop data
	 */
	public ShopData getShopData() {return shopData;}

	/**
	 * Sets the player data.
	 * @param playerData
	 * the player data
	 */
	public void setPlayerData(Map<String,PlayerData> playerData)
	{
		this.playerData = playerData;
	}

	/**
	 * Gets the player data.
	 * @return the player data
	 */
	public Map<String,PlayerData> getPlayerData() {return playerData;}

	/**
	 * Sets the item list.
	 * @param itemList
	 * the new item list
	 */
	public static void setItemList(ItemData itemList)
	{
		CommandShops.itemList = itemList;
	}

	/**
	 * Gets the item list.
	 * @return the item list
	 */
	public static ItemData getItemList() {return itemList;}

	/**
	 * Sets the cuboid tree.
	 * @param cuboidTree
	 * the new cuboid tree
	 */
	public static void setCuboidTree(QuadTree cuboidTree)
	{
		CommandShops.cuboidTree = cuboidTree;
	}

	/**
	 * Gets the cuboid tree.
	 * @return the cuboid tree
	 */
	public static QuadTree getCuboidTree() {return cuboidTree;}

	/**
	 * Sets the econ manager.
	 * @param econManager
	 * the new econ manager
	 */
	public void setEconManager(EconomyManager econManager)
	{
		this.econManager = econManager;
	}

	/**
	 * Gets the econ manager.
	 * @return the econ manager
	 */
	public EconomyManager getEconManager() {return econManager;}
}
