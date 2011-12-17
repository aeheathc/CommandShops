package com.aehdev.commandshops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Stores global basic configuration for CommandShops.
 * Most of this is user-configurable via the config file.
 * Config file is managed in {@link CommandShops#loadProperties}
 */
public class Config
{
	// Shop Size settings
	/** Default width of a shop created without specifying custom boundaries
	 * (both horizontal dimensions). */
	public static int DEFAULT_WIDTH = 5;

	/** Default height of a shop created without specifying custom boundaries*/
	public static int DEFAULT_HEIGHT = 3;

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

	/** Maximum number of transactions that a shop will store for notification
	 * purposes. Limited by default to prevent infinite memory usage. */
	public static int LOG_LIMIT = 100;

	// Search Settings
	/** Maximum number of meters away from a player to include in /shop find
	 * results. */
	public static int FIND_MAX_DISTANCE = 150;

	// Server Settings
	/** Indicates whether transactions will be logged. */
	public static boolean LOG_ENABLE = true;

	/** When true, log verbose debugging information to console. */
	public static boolean DEBUG = false;

	// Player Settings
	/** Maximum number of shops each player can have; unlimited is -1. */
	public static int MAX_SHOPS_PER_PLAYER = -1;

	// Item Settings
	/** Maximum damage percentage a durable item can have and be allowed to be
	 * added to a shop (note that adding an item to a shop will discard damage,
	 * i.e. fully repair it). Default is 35 (that is, 65% or more of the
	 * 'health' of the item is remaining). */
	public static int MAX_DAMAGE = 35;

	// UUID settings
	/** Minimum length of shop UUID. */
	public static int UUID_MIN_LENGTH = 1;

	/** List of all shop UUIDs. */
	protected static List<String> UUID_LIST =
						Collections.synchronizedList(new ArrayList<String>());
	
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

		SHOP_COST =				config.getDouble(	"fees.create");
		MOVE_COST =				config.getDouble(	"fees.move");
		DEFAULT_WIDTH =			config.getInt(		"size.default-width");
		DEFAULT_HEIGHT =		config.getInt(		"size.default-height");
		MAX_WIDTH =				config.getInt(		"size.max-width");
		MAX_HEIGHT =			config.getInt(		"size.max-height");
		MAX_SHOPS_PER_PLAYER =	config.getInt(		"limits.shops-per-player");
		MAX_DAMAGE = 			config.getInt(		"limits.item-damage");
		FIND_MAX_DISTANCE =		config.getInt(		"limits.find-distance");
		LOG_ENABLE =			config.getBoolean(	"log.enable");
		NOTIFY_INTERVAL =		config.getInt(		"log.notify-interval");
		LOG_LIMIT =				config.getInt(		"log.limit");
		DEBUG =					config.getBoolean(	"debug");
		//Logger log = Logger.getLogger("Minecraft");
		//log.warning(""+SHOP_COST);
		plugin.saveConfig();
	}
}
