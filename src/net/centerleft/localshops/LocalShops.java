package net.centerleft.localshops;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import net.centerleft.localshops.modules.economy.EconomyManager;
import net.centerleft.localshops.modules.permission.PermissionManager;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import cuboidLocale.QuadTree;

/**
 * Local Shops Plugin
 * 
 * @author Jonbas
 */
public class LocalShops extends JavaPlugin {
    // Listeners & Objects
    protected ShopsPlayerListener playerListener = new ShopsPlayerListener(this);
    protected ShopData shopData = new ShopData(this);
    protected PluginDescriptionFile pdfFile = null;
    protected ReportThread reportThread = null;
    protected EconomyManager econManager = null;
    protected PermissionManager permManager = null;

    // Logging
    private final Logger log = Logger.getLogger("Minecraft");

    // Constants
    public static final String CHAT_PREFIX = ChatColor.DARK_AQUA + "[" + ChatColor.WHITE + "Shop" + ChatColor.DARK_AQUA + "] ";

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
        pdfFile = getDescription();
        playerData = Collections.synchronizedMap(new HashMap<String, PlayerData>());

        // add all the online users to the data trees
        for (Player player : this.getServer().getOnlinePlayers()) {
            playerData.put(player.getName(), new PlayerData(this, player.getName()));
        }

        // Register our events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Monitor, this);
        // TODO: add PLAYER_JOIN, PLAYER_QUIT, PLAYER_KICK events

        // setup the file IO
        folderDir = new File(folderPath);
        folderDir.mkdir();
        shopsDir = new File(folderPath + shopsPath);
        shopsDir.mkdir();

        properties = new PropertyHandler(folderPath + "localshops.properties");
        properties.load();
        loadProperties(properties);
        properties.save();

        foundWorlds = getServer().getWorlds();
        // read the shops into memory
        shopData.loadShops(shopsDir);

        // update the console that we've started
        log.info(String.format("[%s] %s", pdfFile.getName(), "Loaded with " + shopData.getNumShops() + " shop(s)"));
        log.info(String.format("[%s] %s", pdfFile.getName(), "Version " + pdfFile.getVersion() + " is enabled: " + Config.SRV_UUID.toString()));

        // check which shops players are inside
        for (Player player : this.getServer().getOnlinePlayers()) {
            playerListener.checkPlayerPosition(player);
        }
        
        // Start reporting thread
        if(Config.SRV_REPORT) {
            reportThread = new ReportThread(this, Config.SRV_UUID, false);
            reportThread.start();
        }
        
        econManager = new EconomyManager(this);
        if(!econManager.loadEconomies()) {
            // No valid economies, display error message and disables
            log.warning(String.format("[%s] FATAL: No economic plugins found, please refer to the documentation.", pdfFile.getName()));
            getPluginLoader().disablePlugin(this);
        }
        
        permManager = new PermissionManager(this);
        if(!permManager.load()) {
            // no valid permissions, display error message and disables
            log.warning(String.format("[%s] FATAL: No permission plugins found, please refer to the documentation.", pdfFile.getName()));
            getPluginLoader().disablePlugin(this);
        }
    }

    public void onDisable() {
        // Save all shops
        shopData.saveAllShops();
        
        // Stop Reporting thread
        if(Config.SRV_REPORT && reportThread != null && reportThread.isAlive()) {
            try {
                reportThread.setRun(false);
                reportThread.join(2000);
            } catch (InterruptedException e) {
                // hmm, thread didn't die
                log.warning(String.format("[%s] %s", pdfFile.getName(), "ReportThread did not exit"));
            }
        }
        
        // update the console that we've stopped
        log.info(String.format("[%s] %s", pdfFile.getName(), "Version " + pdfFile.getVersion() + " is disabled!"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        Commands commands = null;
        String type = null;
        String user = "CONSOLE";
        if(sender instanceof Player) {
            user = ((Player)sender).getName();
        }
        
        if (commandLabel.equalsIgnoreCase("buy")) {
            commands = new Commands(this, commandLabel, sender, "buy " + Search.join(args, " "));
            type = "buy";
        } else if (commandLabel.equalsIgnoreCase("sell")) {
            commands = new Commands(this, commandLabel, sender, "sell " + Search.join(args, " "));
            type = "sell";
        } else {
            commands = new Commands(this, commandLabel, sender, args);
            if(args.length > 0) {
                type = args[0];
            } else {
                return commands.shopHelp();
            }
        }
        
        log.info(String.format("[%s] %s issued: %s", pdfFile.getName(), user, commands.getCommand()));

        String commandName = command.getName().toLowerCase();

        if (commandName.equalsIgnoreCase("lshop") || commandLabel.equalsIgnoreCase("buy") || commandLabel.equalsIgnoreCase("sell")) {
            if (type.equalsIgnoreCase("search")) {
                return commands.shopSearch();
            } else if (type.equalsIgnoreCase("debug")) {
                return  commands.shopDebug();
            } else if (type.equalsIgnoreCase("create")) {
                commands.shopCreate();
                for (Player player : this.getServer().getOnlinePlayers()) {
                    playerListener.checkPlayerPosition(player);
                }
                return true;
            } else if (type.equalsIgnoreCase("destroy")) {
                commands.shopDestroy();
                for (Player player : this.getServer().getOnlinePlayers()) {
                    playerListener.checkPlayerPosition(player);
                }
                return true;
            } else if (type.equalsIgnoreCase("move")) {
                commands.shopMove();
                for (Player player : this.getServer().getOnlinePlayers()) {
                    playerListener.checkPlayerPosition(player);
                }
                return true;
            } else if (type.equalsIgnoreCase("browse") || type.equalsIgnoreCase("bro")) {
                return commands.shopBrowse();
            } else if (type.equalsIgnoreCase("sell")) {
                return commands.shopSell();
            } else if (type.equalsIgnoreCase("add")) {
                return commands.shopAdd();
            } else if (type.equalsIgnoreCase("remove")) {
                return commands.shopRemove();
            } else if (type.equalsIgnoreCase("buy")) {
                return commands.shopBuy();
            } else if (type.equalsIgnoreCase("set")) {
                return commands.shopSet();
            } else if (type.equalsIgnoreCase("select")) {
                return commands.shopSelect();
            } else if (type.equalsIgnoreCase("list")) {
                return commands.shopList();
            } else if (type.equalsIgnoreCase("info")) {
                return commands.shopInfo();
            } else if (type.equalsIgnoreCase("version")) {
                sender.sendMessage(String.format("LocalShops Version %s", pdfFile.getVersion()));
                return true;
            } else {
                return commands.shopHelp();
            }
        }
        return false;
    }

    private void loadProperties(PropertyHandler properties) {
        if (properties.keyExists("charge-for-shop")) {
            Config.SHOP_CHARGE_CREATE = properties.getBoolean("charge-for-shop");
            Config.SHOP_CHARGE_MOVE = properties.getBoolean("charge-for-shop");
        } else {
            properties.setBoolean("charge-for-shop", Config.SHOP_CHARGE_CREATE);
        }

        if (properties.keyExists("shop-cost")) {
            Config.SHOP_CHARGE_CREATE_COST = properties.getDouble("shop-cost");
        } else {
            properties.setDouble("shop-cost", Config.SHOP_CHARGE_CREATE_COST);
        }

        if (properties.keyExists("move-cost")) {
            Config.SHOP_CHARGE_MOVE_COST = properties.getDouble("move-cost");
        } else {
            properties.setDouble("move-cost", Config.SHOP_CHARGE_MOVE_COST);
        }

        if (properties.keyExists("shop-width")) {
            Config.SHOP_SIZE_DEF_WIDTH = properties.getLong("shop-width");
        } else {
            properties.setLong("shop-width", Config.SHOP_SIZE_DEF_WIDTH);
        }

        if (properties.keyExists("shop-height")) {
            Config.SHOP_SIZE_DEF_HEIGHT = properties.getLong("shop-height");
        } else {
            properties.setLong("shop-height", Config.SHOP_SIZE_DEF_HEIGHT);
        }

        if (properties.keyExists("max-width")) {
            Config.SHOP_SIZE_MAX_WIDTH = properties.getLong("max-width");
        } else {
            properties.setLong("max-width", Config.SHOP_SIZE_MAX_WIDTH);
        }

        if (properties.keyExists("max-height")) {
            Config.SHOP_SIZE_MAX_HEIGHT = properties.getLong("max-height");
        } else {
            properties.setLong("max-height", Config.SHOP_SIZE_MAX_HEIGHT);
        }
        if (properties.keyExists("shops-per-player")) {
            Config.PLAYER_MAX_SHOPS = properties.getInt("shops-per-player");
        } else {
            properties.setInt("shops-per-player", Config.PLAYER_MAX_SHOPS);
        }

        if (properties.keyExists("log-transactions")) {
            Config.SRV_LOG_TRANSACTIONS = properties.getBoolean("log-transactions");
        } else {
            properties.setBoolean("log-transactions", Config.SRV_LOG_TRANSACTIONS);
        }

        if (properties.keyExists("max-damage")) {
            Config.ITEM_MAX_DAMAGE = properties.getInt("max-damage");
            if (Config.ITEM_MAX_DAMAGE < 0)
                Config.ITEM_MAX_DAMAGE = 0;
        } else {
            properties.setInt("max-damage", Config.ITEM_MAX_DAMAGE);
        }
        
        if(properties.keyExists("uuid")) {
            Config.SRV_UUID = properties.getUuid("uuid");
        } else {
            Config.SRV_UUID = UUID.randomUUID();
            properties.setUuid("uuid", Config.SRV_UUID);
        }
        
        if(properties.keyExists("report-stats")) {
            Config.SRV_REPORT = properties.getBoolean("report-stats");
        } else {
            properties.setBoolean("report-stats", Config.SRV_REPORT);
        }
        
        if(properties.keyExists("debug")) {
            Config.SRV_DEBUG = properties.getBoolean("debug");
        } else {
            properties.setBoolean("debug", Config.SRV_DEBUG);
        }
        
        if(properties.keyExists("search-max-distance")) {
            Config.SEARCH_MAX_DISTANCE = properties.getInt("search-max-distance");
        } else {
            properties.setInt("search-max-distance", Config.SEARCH_MAX_DISTANCE);
        }
    }
}