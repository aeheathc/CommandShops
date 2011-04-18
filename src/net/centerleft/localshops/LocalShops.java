package net.centerleft.localshops;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import com.nijiko.coelho.iConomy.iConomy;
import com.nijikokun.bukkit.Permissions.Permissions;

import cuboidLocale.BookmarkedResult;
import cuboidLocale.QuadTree;

/**
 * Local Shops Plugin
 * 
 * @author Jonbas
 */
public class LocalShops extends JavaPlugin {
	protected final ShopsPlayerListener playerListener = new ShopsPlayerListener(this);
	protected final ShopsPluginListener pluginListener = new ShopsPluginListener(this);
	protected PluginDescriptionFile pdfFile = null;
	
	private final Logger log = Logger.getLogger("Minecraft");
	
	static String pluginName = "LocalShops";
	static String pluginVersion;
	
	static QuadTree cuboidTree = new QuadTree();
	static String folderPath = "plugins/LocalShops/";
	static File folderDir;
	static String shopsPath = "shops/";
	static File shopsDir;
	static List<World> foundWorlds;
	
	static PropertyHandler properties;
	
	
	static ItemData itemList = new ItemData();
	protected Map<String, PlayerData> playerData; //synchronized player hash
	
	public Map<String, BookmarkedResult> playerResult;  //synchronized result buffer hash
	
	public void onEnable() {
		
		pdfFile = getDescription();
		
		QuadTree cuboidTree = new QuadTree();
		playerResult = Collections.synchronizedMap(new HashMap<String, BookmarkedResult>());
		playerData = Collections.synchronizedMap(new HashMap<String, PlayerData>());
		
		// add all the online users to the data trees
		for( Player player : this.getServer().getOnlinePlayers() ) {
			if( !this.playerResult.containsKey(player.getName())) {
				this.playerResult.put(player.getName(), new BookmarkedResult());
			}
			if( !PlayerData.playerShopList.containsKey(player.getName())) {
				PlayerData.playerShopList.put(player.getName(), Collections.synchronizedList(new ArrayList<String>()));	
			}
		}

		// Register our events
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLUGIN_ENABLE, pluginListener, Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLUGIN_DISABLE, pluginListener, Priority.Monitor, this);
		
		
		//check hook for permissions
		Plugin p = pm.getPlugin("Permissions");
		if (p != null) {
            if (p.isEnabled()) {
                Permissions gm = (Permissions) p;
                ShopsPluginListener.permissions = gm;
                ShopsPluginListener.gmPermissionCheck = gm.getHandler();
                log.info(String.format("[%s] %s", pdfFile.getName(), "Permissions found."));
                ShopsPluginListener.usePermissions = true;
            } else {
            	ShopsPluginListener.usePermissions = false;
            }
        } else {
        	log.severe(String.format("[%s] %s", pdfFile.getName(), "Permissions not found."));
        	ShopsPluginListener.usePermissions = false;
        }
		
		//check hook for iConomy
		Plugin ic = pm.getPlugin("iConomy");
		if (ic != null) {
            if (ic.isEnabled()) {
                iConomy icon = (iConomy) ic;
                ShopsPluginListener.iConomy = icon;
                log.info(String.format("[%s] %s", pdfFile.getName(), "iConomy found."));
                ShopsPluginListener.useiConomy = true;
            } else {
            	ShopsPluginListener.useiConomy = false;
            	log.info(String.format("[%s] %s", pdfFile.getName(), "Waiting for iConomy to start."));
            }
        } else {
        	log.severe(String.format("[%s] %s", pdfFile.getName(), "iConomy not found."));
        	ShopsPluginListener.useiConomy = false;
        }
		
		

		
		// setup the file IO
		folderDir = new File(folderPath);
		folderDir.mkdir();
		shopsDir = new File(folderPath + shopsPath); 
		shopsDir.mkdir();
		
		//build data table for item names and values
		itemList.loadData(new File(folderPath + "items.txt"));
		
		properties = new PropertyHandler(folderPath + "localshops.properties");
		properties.load();
		PluginProperties.loadProperties(properties);
		properties.save();
		
		foundWorlds = getServer().getWorlds();
		// read the shops into memory
		ShopData.LoadShops( shopsDir );

		//update the console that we've started
		log.info(String.format("[%s] %s", pdfFile.getName(), "Loaded with " + ShopData.shops.size() + " shop(s)"));
		log.info(String.format("[%s] %s", pdfFile.getName(), "Version " + pdfFile.getVersion() + " is enabled!"));
		
		// check which shops players are inside
		for( Player player : this.getServer().getOnlinePlayers() ) {
			ShopsPlayerListener.checkPlayerPosition(this, player);
		}

	}

	public void onDisable() {
		//update the console that we've stopped
		log.info(String.format("[%s] %s", pdfFile.getName(), "Version " + pdfFile.getVersion() + " is disabled!"));
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		Commands commands = new Commands(this, sender, args);
		
		String commandName = command.getName().toLowerCase();

		if (commandName.equalsIgnoreCase("lshop")) {
			if(args.length >= 1) {
				if(args[0].equalsIgnoreCase("create")) {
					commands.shopCreate();
					for(Player player: this.getServer().getOnlinePlayers()) {
						ShopsPlayerListener.checkPlayerPosition(this, player);
					}
				} else if(args[0].equalsIgnoreCase("destroy")) {
					commands.shopDestroy();
					for(Player player: this.getServer().getOnlinePlayers()) {
						ShopsPlayerListener.checkPlayerPosition(this, player);
					}
				} else if(args[0].equalsIgnoreCase("move")) {
					commands.shopMove();
					for(Player player: this.getServer().getOnlinePlayers()) {
						ShopsPlayerListener.checkPlayerPosition(this, player);
					}
				} else if(args[0].equalsIgnoreCase("list")) {
					commands.shopList();
				} else if(args[0].equalsIgnoreCase("reload")) {
					commands.shopReload();
				} else if(args[0].equalsIgnoreCase("sell")) {
					commands.shopSellItem();
				} else if(args[0].equalsIgnoreCase("add")) {
					commands.shopAddItem();
				} else if(args[0].equalsIgnoreCase("remove")) {
					commands.shopRemoveItem();
				}else if(args[0].equalsIgnoreCase("buy")) {
					commands.shopBuyItem();
				} else if(args[0].equalsIgnoreCase("set")) {
					commands.shopSetItem();
				} else if(args[0].equalsIgnoreCase("select")) {
					return commands.shopSelect();
				} else {
					return commands.shopHelp();
				}
				
			} else {
				return commands.shopHelp();
			}
			
			return true;
		}
		return false;
	}
}
