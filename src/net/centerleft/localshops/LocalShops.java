package net.centerleft.localshops;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import net.centerleft.localshops.modules.economy.EconomyManager;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

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
    protected UUID uuid = null;
    protected boolean report = false;
    protected ReportThread reportThread = null;
    protected EconomyManager econManager = null;

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
        log.info(String.format("[%s] %s", pdfFile.getName(), "Version " + pdfFile.getVersion() + " is enabled!"));

        // check which shops players are inside
        for (Player player : this.getServer().getOnlinePlayers()) {
            playerListener.checkPlayerPosition(player);
        }
        
        // Start reporting thread
        if(report) {
            reportThread = new ReportThread(this, uuid, false);
            reportThread.start();
        }
        
        econManager = new EconomyManager(this);
        econManager.loadEconomies();
        
    }

    public void onDisable() {
        // Save all shops
        shopData.saveAllShops();
        
        // Stop Reporting thread
        if(report && reportThread != null && reportThread.isAlive()) {
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
        
        if(properties.keyExists("uuid")) {
            uuid = properties.getUuid("uuid");
        } else {
            uuid = UUID.randomUUID();
            properties.setUuid("uuid", uuid);
        }
        
        if(properties.keyExists("report-stats")) {
            report = properties.getBoolean("report-stats");
        } else {
            report = true;
            properties.setBoolean("report-stats", report);
        }
    }
}