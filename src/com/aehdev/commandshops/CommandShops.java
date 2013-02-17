package com.aehdev.commandshops;

import java.io.File;
import java.sql.ResultSet;
import java.util.Locale;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import cuboidLocale.QuadTree;
import net.milkbowl.vault.economy.Economy;
import com.aehdev.lib.PatPeter.SQLibrary.Database;
import com.aehdev.lib.PatPeter.SQLibrary.MySQL;
import com.aehdev.lib.PatPeter.SQLibrary.SQLite;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.GlobalRegionManager;

import com.aehdev.commandshops.commands.ShopCommandExecutor;
import com.aehdev.commandshops.threads.NotificationThread;

/**
 * The main Bukkit plugin class for CommandShops.
 */
public class CommandShops extends JavaPlugin
{
	/** Our class implementing Listener that handles player interaction */
	public ShopsPlayerListener playerListener = new ShopsPlayerListener();

	/** Single object that holds all data for everyone's shops */
	private ShopData shopData = new ShopData();

	/** General plugin info that comes directly from the private
	 * {@link JavaPlugin#description} in the parent. Effectively all we're
	 * doing here is converting it to public. */
	public static PluginDescriptionFile pdfFile = null;

	/** Thread that notifies managers of transactions. */
	protected NotificationThread notificationThread = null;

	/** Abstracts supported economies. */
	public Economy econ = null;
	
	/** Abstracts supported databases. */
	public static Database db = null;

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

	/** Database of item data. */
	private static ItemData itemList = new ItemData();
	
	/** Reference to WorldGuard region manager. */
	public static GlobalRegionManager worldguard = null;

	/**
	 * Setup method for when this plugin is enabled by Bukkit
	 */
	public void onEnable()
	{
		pdfFile = getDescription();	//cache plugin info
		Config.loadProperties(this);//Get the configuration via Bukkit's builtin method
		Search.reload(this);
		
        if(!setupEconomy())
        {
            log.severe(String.format((Locale)null,"[%s] - Shutting down: Vault economy hook not found!", pdfFile.getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        //Connect to database, do any necessary setup
        if(Config.STORAGE_SYSTEM.equalsIgnoreCase("sqlite"))
        {
        	db = new SQLite(log, "CommandShops","commandshops",folderPath);
        	if(!setupDB()) return;
        }else if(Config.STORAGE_SYSTEM.equalsIgnoreCase("mysql")){
        	db = new MySQL(log, "CommandShops", Config.DB_HOST, ""+Config.DB_PORT,
        			Config.DB_NAME, Config.DB_USER, Config.DB_PASS);
        	if(!setupDB()) return;
        }else{
            log.severe(String.format((Locale)null,"[%s] Shutting down: Unrecognized storage system.", pdfFile.getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
		// read old shopfile format and dump into database
		(new File(folderPath)).mkdir();
		File shopsDir = new File(folderPath + shopsPath);
		shopsDir.mkdir();
		shopData.loadShops(shopsDir);
		
		//optional -- try to hook to worldguard
		Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
        if(plugin == null || !(plugin instanceof WorldGuardPlugin))
        {
        	if(Config.REQUIRE_OWNER)
        	{
        		log.warning(String.format((Locale)null,"[%s] %s", pdfFile.getName(), "No supported region plugin found, but config says to require owned regions! Existing shops will work but no shops can be created/moved like this."));
        	}
        	log.info(String.format((Locale)null,"[%s] %s", pdfFile.getName(), "No supported region plugin found, using free selection only."));
        }else{
        	worldguard = ((WorldGuardPlugin)plugin).getGlobalRegionManager();
        	log.info(String.format((Locale)null,"[%s] %s", pdfFile.getName(), "WorldGuard support enabled."));
        }		
        
		// Register our events
		getServer().getPluginManager().registerEvents(playerListener, this);

		// Register Commands
		getCommand("shop").setExecutor(new ShopCommandExecutor(this));

		// update the console that we've started
		try{
			ResultSet shoptot = db.query("SELECT COUNT(*) FROM shops"); shoptot.next();
			long totalShops = shoptot.getLong(1);
			shoptot.close();
			log.info(String.format((Locale)null,"[%s] %s", pdfFile.getName(), "Loaded with "
					+ totalShops + " shop(s)"));
			log.info(String.format((Locale)null,"[%s] %s", pdfFile.getName(),
					"Version " + pdfFile.getVersion() + " is enabled: "));
		}catch(Exception e){
            log.severe(String.format((Locale)null,"[%s] - Shutting down: Can't select from DB.", pdfFile.getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
		}

		// Start Notification thread
		if(Config.NOTIFY_INTERVAL > 0)
		{
			notificationThread = new NotificationThread(this);
			notificationThread.start();
		}
	}

	/**
	 * Shut down the plugin.
	 * Called by Bukkit when the server is shutting down, plugins are being reloaded,
	 * or we voluntarily shutdown due to errors. 
	 */
	public void onDisable()
	{
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
				log.warning(String.format((Locale)null,"[%s] %s", pdfFile.getName(),
						"NotificationThread did not exit"));
			}
		}

		// update the console that we've stopped
		log.info(String.format((Locale)null,"[%s] %s", pdfFile.getName(), "Version "
				+ pdfFile.getVersion() + " is disabled!"));
	}

	/**
	 * Saves the item database.
	 * @param itemList
	 * the new item list
	 */
	public static void setItemList(ItemData itemList)
	{
		CommandShops.itemList = itemList;
	}

	/**
	 * Gets the item database.
	 * @return the item list
	 */
	public static ItemData getItemList() {return itemList;}

	/**
	 * Sets the structure of shop coords.
	 * @param cuboidTree
	 * the new cuboid tree
	 */
	public static void setCuboidTree(QuadTree cuboidTree)
	{
		CommandShops.cuboidTree = cuboidTree;
	}

	/**
	 * Gets the structure of shop coords.
	 * @return the cuboid tree
	 */
	public static QuadTree getCuboidTree() {return cuboidTree;}
	
	/**
	 * Attach to Vault's Economy support
	 * @return true on success
	 */
    private boolean setupEconomy()
    {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        econ = rsp.getProvider();
        return econ != null;
    }
    
    /**
     * Ensure that the database contains the right schema.
     * @return true on success
     */
    private boolean setupDB()
    {
    	String tables,addedcolumns,indexes;
    	//Start with MySQL version
		tables = "CREATE TABLE IF NOT EXISTS `shops` (`id` INTEGER  PRIMARY KEY AUTO_INCREMENT NOT NULL,`name` TEXT  NOT NULL,`owner` TEXT  NOT NULL,`creator` TEXT  NOT NULL,`x` INTEGER  NOT NULL,`y` INTEGER  NOT NULL,`z` INTEGER  NOT NULL,`x2` INTEGER  NOT NULL,`y2` INTEGER  NOT NULL,`z2` INTEGER  NOT NULL,`world` TEXT  NOT NULL,`minbalance` REAL DEFAULT '0' NOT NULL,`unlimitedMoney` INTEGER DEFAULT '0' NOT NULL,`unlimitedStock` INTEGER DEFAULT '0' NOT NULL,`notify` INTEGER DEFAULT '1' NOT NULL,`service_repair` INTEGER DEFAULT '1' NOT NULL,`service_disenchant` INTEGER DEFAULT '1' NOT NULL);CREATE TABLE IF NOT EXISTS `shop_items` (`id` INTEGER  PRIMARY KEY AUTO_INCREMENT NOT NULL,`shop` INTEGER  NOT NULL,`itemid` INTEGER  NOT NULL,`itemdamage` INTEGER  NOT NULL,`stock` REAL DEFAULT '0' NOT NULL,`maxstock` INTEGER DEFAULT '10' NOT NULL,`buy` REAL  NULL,`sell` REAL  NULL,FOREIGN KEY(shop) REFERENCES shops(id) ON DELETE CASCADE ON UPDATE CASCADE);CREATE TABLE IF NOT EXISTS `managers` (`shop` INTEGER NOT NULL, `manager` VARCHAR(255) NOT NULL, FOREIGN KEY(shop) REFERENCES shops(id) ON DELETE CASCADE ON UPDATE CASCADE);CREATE TABLE IF NOT EXISTS `log` (`id` INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL, `datetime` TEXT NOT NULL, `user` TEXT NOT NULL, `shop` INTEGER, `action` TEXT NOT NULL, `itemid` INTEGER, `itemdamage` INTEGER, `amount` INTEGER, `cost` REAL, `total` REAL, `comment` TEXT,FOREIGN KEY(shop) REFERENCES shops(id) ON DELETE CASCADE ON UPDATE CASCADE);";
		addedcolumns = "ALTER TABLE `shops` ADD COLUMN `region` VARCHAR(63) DEFAULT NULL;";
		indexes = "CREATE INDEX `IDX_LOG_DATETIME` ON `log`(`datetime`(22)  ASC);CREATE INDEX `IDX_LOG_SHOP` ON `log`(`shop`  ASC);CREATE INDEX `IDX_LOG_ACTION` ON `log`(`action`(22)  ASC);CREATE INDEX `IDX_MANAGERS_MANAGER` ON `managers`(`manager`(22)  ASC);CREATE INDEX `IDX_SHOP_ITEMS_SHOP` ON `shop_items`(`shop`  ASC);CREATE INDEX `IDX_SHOP_ITEMS_ITEMID` ON `shop_items`(`itemid`  ASC);CREATE INDEX `IDX_SHOP_ITEMS_ITEMDAMAGE` ON `shop_items`(`itemdamage`  ASC);CREATE INDEX `IDX_SHOPS_X` ON `shops`(`x`  ASC);CREATE INDEX `IDX_SHOPS_Y` ON `shops`(`y`  ASC);CREATE INDEX `IDX_SHOPS_Z` ON `shops`(`z`  ASC);CREATE INDEX `IDX_SHOPS_X2` ON `shops`(`x2`  ASC);CREATE INDEX `IDX_SHOPS_Y2` ON `shops`(`y2`  ASC);CREATE INDEX `IDX_SHOPS_Z2` ON `shops`(`z2`  ASC);CREATE INDEX `IDX_SHOPS_OWNER` ON `shops`(`owner`(22)  ASC);CREATE INDEX `IDX_SHOPS_WORLD` ON `shops`(`world`(22)  ASC);CREATE UNIQUE INDEX IDX_MANAGERS_ ON managers(manager,shop);CREATE UNIQUE INDEX IDX_SHOP_ITEMS_ ON shop_items(shop,itemid,itemdamage);CREATE UNIQUE INDEX `IDX_SHOPS_REGION` ON `shops`(`region` ASC);";
		
		
    	if(db instanceof com.aehdev.lib.PatPeter.SQLibrary.SQLite)
    	{
    		//convert to SQLite compatible.
    		tables = tables.replaceAll("AUTO_INCREMENT","AUTOINCREMENT");
    		tables = tables.replaceAll("VARCHAR\\((\\d+)\\)","TEXT");
    		indexes = indexes.replaceAll("`\\((\\d+)\\)","`");
    	}

    	//Run create table statements only if the tables do not exist. Any errors here will be very bad.
    	try{
    		for(String query : tables.split(";")) db.query(query);
    	}catch(Exception e){
    		log.severe(String.format((Locale)null,"[%s] [SQLibrary] - %s", pdfFile.getName(),e));
            log.severe(String.format((Locale)null,"[%s] - Shutting down: Problem checking schema.", pdfFile.getName()));
            getServer().getPluginManager().disablePlugin(this);
    		return false;
    	}
    	
    	/* Additional column queries are expected to cause errors if the columns already exist because
    	 * querying for existence of individual columns is not worth the trouble 
    	 */
    	for(String query : addedcolumns.split(";")) try{db.query(query,true);}catch(Exception e){}
    	
    	/* Index creation queries are expected to cause errors if the indexes already exist because
    	 * MySQL does not support "CREATE INDEX IF NOT EXISTS" 
    	 */
    	for(String query : indexes.split(";")) try{db.query(query,true);}catch(Exception e){}
    	
    	//trim log
    	try{
    		ResultSet reslog = db.query("SELECT COUNT(*) FROM `log`");
    		reslog.next();
    		long count = reslog.getLong(1);
    		reslog.close();
    		if(count>Config.LOG_LIMIT)
    		{
    			ResultSet resPivot = db.query("SELECT `datetime` FROM `log` ORDER BY `datetime` DESC LIMIT " + Config.LOG_LIMIT + ",1");
    			String pivot = resPivot.getString("datetime");
    			resPivot.close();
    			db.query("DELETE FROM `log` WHERE `datetime`<='" + pivot + "'");
    		}
    	}catch(Exception e){
    		log.warning(String.format((Locale)null,"[%s] - Couldn't trim log. Beware of ballooning log table. %s", pdfFile.getName(), e));
    	}
    	
    	return true;
    }
}
