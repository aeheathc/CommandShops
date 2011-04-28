package net.centerleft.localshops;

import java.io.File;
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

import cuboidLocale.QuadTree;

/**
 * Local Shops Plugin
 * 
 * @author Jonbas
 */
public class LocalShops extends JavaPlugin {
    // Listeners & Objects
    protected ShopsPlayerListener playerListener = new ShopsPlayerListener(this);
    protected ShopsPluginListener pluginListener = new ShopsPluginListener(this);
    protected ShopData shopData = new ShopData(this);
    protected PluginDescriptionFile pdfFile = null;

    // Logging
    private final Logger log = Logger.getLogger("Minecraft");

    // Constants
    public static final String CHAT_PREFIX = ChatColor.AQUA + "[" + ChatColor.WHITE + "Shop" + ChatColor.AQUA + "] ";

    // TBD
    static QuadTree cuboidTree = new QuadTree();
    static String folderPath = "plugins/LocalShops/";
    static File folderDir;
    static String shopsPath = "shops/";
    static File shopsDir;
    static List<World> foundWorlds;

    static PropertyHandler properties;

    static ItemData itemList = new ItemData();
    protected Map<String, PlayerData> playerData; // synchronized player hash

    public void onEnable() {
        
        Search.loadItems();

        pdfFile = getDescription();

        QuadTree cuboidTree = new QuadTree();
        playerData = Collections.synchronizedMap(new HashMap<String, PlayerData>());

        // add all the online users to the data trees
        for (Player player : this.getServer().getOnlinePlayers()) {
            playerData.put(player.getName(), new PlayerData(this, player.getName()));
        }

        // Register our events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLUGIN_ENABLE, pluginListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLUGIN_DISABLE, pluginListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Monitor, this);
        // TODO: add PLAYER_JOIN, PLAYER_QUIT, PLAYER_KICK events

        // check hook for permissions
        Plugin p = pm.getPlugin("Permissions");
        if (p != null) {
            if (p.isEnabled()) {
                Permissions gm = (Permissions) p;
                pluginListener.permissions = gm;
                pluginListener.gmPermissionCheck = gm.getHandler();
                log.info(String.format("[%s] %s", pdfFile.getName(), "Permissions found."));
                pluginListener.usePermissions = true;
            } else {
                pluginListener.usePermissions = false;
            }
        } else {
            log.severe(String.format("[%s] %s", pdfFile.getName(), "Permissions not found."));
            pluginListener.usePermissions = false;
        }

        // check hook for iConomy
        Plugin ic = pm.getPlugin("iConomy");
        if (ic != null) {
            if (ic.isEnabled()) {
                iConomy icon = (iConomy) ic;
                pluginListener.iConomy = icon;
                log.info(String.format("[%s] %s", pdfFile.getName(), "iConomy found."));
                pluginListener.useiConomy = true;
            } else {
                pluginListener.useiConomy = false;
                log.info(String.format("[%s] %s", pdfFile.getName(), "Waiting for iConomy to start."));
            }
        } else {
            log.severe(String.format("[%s] %s", pdfFile.getName(), "iConomy not found."));
            pluginListener.useiConomy = false;
        }

        // setup the file IO
        folderDir = new File(folderPath);
        folderDir.mkdir();
        shopsDir = new File(folderPath + shopsPath);
        shopsDir.mkdir();

        // build data table for item names and values
        itemList.loadData(new File(folderPath + "items.txt"));

        properties = new PropertyHandler(folderPath + "localshops.properties");
        properties.load();
        loadProperties(properties);
        properties.save();

        foundWorlds = getServer().getWorlds();
        // read the shops into memory
        shopData.loadShops(shopsDir);

        // update the console that we've started
        log.info(String.format("[%s] %s", pdfFile.getName(), "Loaded with " + shopData.getNumShops() + " shop(s)"));
        log.info(String.format("[%s] %s", pdfFile.getName(), "Version " + pdfFile.getVersion() + " is enabled!"));

        // check which shops players are inside
        for (Player player : this.getServer().getOnlinePlayers()) {
            playerListener.checkPlayerPosition(player);
        }

    }

    public void onDisable() {
        // update the console that we've stopped
        log.info(String.format("[%s] %s", pdfFile.getName(), "Version " + pdfFile.getVersion() + " is disabled!"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        Commands commands = new Commands(this, sender, args);

        String commandName = command.getName().toLowerCase();

        if (commandName.equalsIgnoreCase("lshop")) {
            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("search")) {
                    commands.shopSearch();
                } else if (args[0].equalsIgnoreCase("debug")) {
                    commands.shopDebug();
                } else if (args[0].equalsIgnoreCase("create")) {
                    commands.shopCreate();
                    for (Player player : this.getServer().getOnlinePlayers()) {
                        playerListener.checkPlayerPosition(player);
                    }
                } else if (args[0].equalsIgnoreCase("destroy")) {
                    commands.shopDestroy();
                    for (Player player : this.getServer().getOnlinePlayers()) {
                        playerListener.checkPlayerPosition(player);
                    }
                } else if (args[0].equalsIgnoreCase("move")) {
                    commands.shopMove();
                    for (Player player : this.getServer().getOnlinePlayers()) {
                        playerListener.checkPlayerPosition(player);
                    }
                } else if (args[0].equalsIgnoreCase("list")) {
                    commands.shopList();
                } else if (args[0].equalsIgnoreCase("reload")) {
                    commands.shopReload();
                } else if (args[0].equalsIgnoreCase("sell")) {
                    commands.shopSellItem();
                } else if (args[0].equalsIgnoreCase("add")) {
                    commands.shopAddItem();
                } else if (args[0].equalsIgnoreCase("remove")) {
                    commands.shopRemoveItem();
                } else if (args[0].equalsIgnoreCase("buy")) {
                    commands.shopBuyItem();
                } else if (args[0].equalsIgnoreCase("set")) {
                    commands.shopSetItem();
                } else if (args[0].equalsIgnoreCase("select")) {
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

    private void loadProperties(PropertyHandler properties) {
        if (properties.keyExists("charge-for-shop")) {
            shopData.chargeForShop = properties.getBoolean("charge-for-shop");
        } else {
            properties.setBoolean("charge-for-shop", shopData.chargeForShop);
        }

        if (properties.keyExists("shop-cost")) {
            shopData.shopCost = properties.getLong("shop-cost");
        } else {
            properties.setLong("shop-cost", shopData.shopCost);
        }

        if (properties.keyExists("move-cost")) {
            shopData.moveCost = properties.getLong("move-cost");
        } else {
            properties.setLong("move-cost", shopData.moveCost);
        }

        if (properties.keyExists("shop-width")) {
            shopData.shopSize = properties.getLong("shop-width");
        } else {
            properties.setLong("shop-width", shopData.shopSize);
        }

        if (properties.keyExists("shop-height")) {
            shopData.shopHeight = properties.getLong("shop-height");
        } else {
            properties.setLong("shop-height", shopData.shopHeight);
        }

        if (properties.keyExists("max-width")) {
            shopData.maxWidth = properties.getLong("max-width");
        } else {
            properties.setLong("max-width", shopData.maxWidth);
        }

        if (properties.keyExists("max-height")) {
            shopData.maxHeight = properties.getLong("max-height");
        } else {
            properties.setLong("max-height", shopData.maxHeight);
        }

        if (properties.keyExists("log-transactions")) {
            shopData.logTransactions = properties.getBoolean("log-transactions");
        } else {
            properties.setBoolean("log-transactions", shopData.logTransactions);
        }

        if (properties.keyExists("max-damage")) {
            shopData.maxDamage = properties.getInt("max-damage");
            if (shopData.maxDamage < 0)
                shopData.maxDamage = 0;
        } else {
            properties.setInt("max-damage", shopData.maxDamage);
        }
    }
}
