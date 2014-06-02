package com.aehdev.commandshops;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Stores global basic configuration for CommandShops.
 * Most of this is user-configurable via the config file.
 * Config file is managed in {@link CommandShops#loadProperties}
 */
public class Config
{
	// Shop Size settings
	/** Maximum width of a custom sized shop (both horiontal dimensions). */
	public static int MAX_WIDTH = 30;

	/** Maximum height of a custom sized shop. */
	public static int MAX_HEIGHT = 10;

	// Shop Cost settings
	/** Cost of creating a shop. */
	public static double SHOP_COST = 100;

	/** Cost of moving a shop. */
	public static double MOVE_COST = 10;

	/** Number of seconds between transaction notifications. */
	public static int NOTIFY_INTERVAL = 300;

	/** Maximum number of transactions that our log will store for notification purposes. */
	public static int LOG_LIMIT = 500;

	// Search Settings
	/** Maximum number of meters away from a player to include in /shop find
	 * results. */
	public static int FIND_MAX_DISTANCE = 1500;

	// Server Settings
	/** When true, log verbose debugging information to console. */
	public static boolean DEBUG = false;
	
	/** Use custom item set from items.yml. When False, CS will make its own items.yml with the vanilla items. */
	public static boolean CUSTOM_ITEMS = false;

	// Player Settings
	/** Maximum number of shops each player can have; unlimited is -1. */
	public static int MAX_SHOPS_PER_PLAYER = -1;

	// Item Settings
	/** Maximum damage percentage a durable item can have and be allowed to be
	 * added to a shop (note that adding an item to a shop will discard damage,
	 * i.e. fully repair it). Default is 35 (that is, 65% or more of the
	 * 'health' of the item is remaining). */
	public static int MAX_DAMAGE = 35;

	// Storage settings
	/** Which RDBMS to use */
	public static String STORAGE_SYSTEM = "sqlite";
	
	/** Where the database server is located */
	public static String DB_HOST = "localhost";
	
	/** Port number that the database server is listening on */
	public static int DB_PORT = 3306;
	
	/** Username to connect to the database server */
	public static String DB_USER = "minecraft";
	
	/** Password to connect to the database server */
	public static String DB_PASS = "password";
	
	/** Name of the database containing CommandShops data */
	public static String DB_NAME = "commandshops";
	
	/** Allows moving a shop from one world to another */
	public static boolean ALLOW_INTERWORLD_MOVE = true;
	
	//Region settings
	/** When true, only allow shops to be created/moved to a selected region that is owned by the player */
	public static boolean REQUIRE_OWNER = false;
	
	/** The set of "market" regions where shops can be made. If this is empty, shops can be made anywhere. */
	public static String[] MARKETS = {};
	
	/**
	 * Read the config file and load options when present, or write default
	 * options when not present.
	 * @param plugin
	 * main plugin class reference so we can call its inherited method {@link getConfig}
	 */
	public static void loadProperties(CommandShops plugin)
	{
		FileConfiguration config = plugin.getConfig();
		config.options().copyDefaults(true);

		SHOP_COST =				config.getDouble(		"fees.create"				);
		MOVE_COST =				config.getDouble(		"fees.move"					);
		MAX_WIDTH =				config.getInt(			"size.max-width"			);
		MAX_HEIGHT =			config.getInt(			"size.max-height"			);
		MAX_SHOPS_PER_PLAYER =	config.getInt(			"limits.shops-per-player"	);
		MAX_DAMAGE = 			config.getInt(			"limits.item-damage"		);
		FIND_MAX_DISTANCE =		config.getInt(			"limits.find-distance"		);
		NOTIFY_INTERVAL =		config.getInt(			"log.notify-interval"		);
		LOG_LIMIT =				config.getInt(			"log.limit"					);
		DEBUG =					config.getBoolean(		"debug"						);
		STORAGE_SYSTEM = 		config.getString(		"storage.system"			);
		DB_HOST = 				config.getString(		"storage.connect.host"		);
		DB_PORT = 				config.getInt(			"storage.connect.port"		);
		DB_USER = 				config.getString(		"storage.connect.user"		);
		DB_PASS = 				config.getString(		"storage.connect.pass"		);
		DB_NAME = 				config.getString(		"storage.connect.db"		);
		REQUIRE_OWNER =			config.getBoolean(		"regions.require-owner"		);
		MARKETS =				config.getStringList(	"regions.markets"			).toArray(MARKETS);
		CUSTOM_ITEMS =			config.getBoolean(		"custom-items"				);
		ALLOW_INTERWORLD_MOVE =	config.getBoolean(		"allow-interworld-move"		);

		plugin.saveConfig();
	}
}
