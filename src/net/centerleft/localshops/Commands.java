package net.centerleft.localshops;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.nijiko.permissions.PermissionHandler;

import cuboidLocale.BookmarkedResult;
import cuboidLocale.PrimitiveCuboid;

public class Commands {
    // Attributes
    private LocalShops plugin = null;
    private String commandLabel = null;
    private CommandSender sender = null;
    private String command = null;
    private static String DECIMAL_REGEX = "(\\d+\\.\\d+)|(\\d+\\.)|(\\.\\d+)|(\\d+)";

    // Command Types Enum
    static enum CommandTypes {
        ADMIN(0, new String[] { "localshops.admin" }),
        ADD(1, new String[] { "localshops.manager.add" }),
        BUY(2, new String[] { "localshops.user.buy" }),
        CREATE(3, new String[] { "localshops.manager.create" }),
        CREATE_FREE(4, new String[] { "localshops.free.create" }),
        DESTROY(5, new String[] { "localshops.manager.destroy" }),
        HELP(6, new String[] {}),
        BROWSE(7, new String[] { "localshops.user.browse" }),
        MOVE(8, new String[] { "localshops.manager.move" }),
        MOVE_FREE(9, new String[] { "localshops.free.move" }),
        REMOVE(10, new String[] { "localshops.manager.remove" }),
        SEARCH(11, new String[] {}),
        SELECT(12, new String[] { "localshops.manager.select" }),
        SELL(13, new String[] { "localshops.user.sell" }),
        SET_OWNER(14, new String[] { "localshops.manager.set.owner" }),
        SET(15, new String[] { "localshops.manager.set" });

        int id = -1;
        String[] permissions = null;

        CommandTypes(int id) {
            this.id = id;
        }

        CommandTypes(int id, String[] permissions) {
            this(id);
            this.permissions = permissions;
        }

        public int getId() {
            return id;
        }

        public String[] getPermissions() {
            return permissions;
        }
    }

    // Logging
    private static final Logger log = Logger.getLogger("Minecraft");

    public Commands(LocalShops plugin, String commandLabel, CommandSender sender, String command) {
        this.plugin = plugin;
        this.commandLabel = commandLabel;
        this.sender = sender;
        this.command = command.trim();
    }

    public Commands(LocalShops plugin, String commandLabel, CommandSender sender, String[] args) {
        this(plugin, commandLabel, sender, Search.join(args, " ").trim());
    }

    public String getCommand() {
        return command;
    }

    public boolean shopList() {
        int idWidth = Config.UUID_MIN_LENGTH + 1;
        if(idWidth < 4) {
            idWidth = 4;
        }

        boolean showAll = false;
        boolean isPlayer = false;

        // list all
        Pattern pattern = Pattern.compile("(?i)list\\s+all$");
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            showAll = true;
        }        

        if(sender instanceof Player) {
            isPlayer = true;
        }

        if(isPlayer) {
            sender.sendMessage(String.format("%-"+idWidth+"s  %s", "Id", "Name"));
        } else {
            sender.sendMessage(String.format("%-"+idWidth+"s  %-25s %s", "Id", "Name", "Owner"));
        }
        Iterator<Shop> it = plugin.shopData.getAllShops().iterator();
        while(it.hasNext()) {
            Shop shop = it.next();
            if(!showAll && isPlayer && !isShopController(shop)) {
                continue;
            }
            
            if(isPlayer) {
                sender.sendMessage(String.format("%-"+idWidth+"s  %s", shop.getShortUuidString(), shop.getName()));
            } else {
                sender.sendMessage(String.format("%-"+idWidth+"s  %-25s %s", shop.getShortUuidString(), shop.getName(), shop.getOwner()));
            }
        }
        return true;
    }

    public boolean shopDebug() {
        Shop shop = null;
        if (sender instanceof Player) {
            Player player = (Player) sender;
            PlayerData pData = plugin.playerData.get(player.getName());

            // info (player only command)
            Pattern pattern = Pattern.compile("(?i)debug$");
            Matcher matcher = pattern.matcher(command);
            if (matcher.find()) {
                // Get Current Shop
                UUID shopUuid = pData.getCurrentShop();
                if (shopUuid != null) {
                    shop = plugin.shopData.getShop(shopUuid);
                }
                if (shop == null) {
                    sender.sendMessage("You are not in a shop!");
                    return false;
                }
            }
        } else {
            // ignore?
        }

        // info id
        Pattern pattern = Pattern.compile("(?i)debug\\s+(.*)$");
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            String input = matcher.group(1);
            shop = plugin.shopData.getShop(input);
            if (shop == null) {
                sender.sendMessage("Could not find shop with ID " + input);
                return false;
            }
        }

        if(shop != null) {
            shop.log();
            if(sender instanceof Player) {
                sender.sendMessage("Shop has been logged to console!");
            }
            return true;
        } else {
            sender.sendMessage("Could not find shop!");
            return false;
        }
    }

    public boolean shopSearch() {

        Pattern pattern = Pattern.compile("(?i)search\\s+(.*)");
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            String name = matcher.group(1);
            ItemInfo found = Search.itemByName(name);
            if (found == null) {
                sender.sendMessage(String.format("No item was not found matching \"%s\"", name));
            } else {
                sender.sendMessage(found.toString());
            }
            return true;            
        }

        // Show search stuff
        sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " search [item name]" + ChatColor.DARK_AQUA + " - Searches for and displays information about an item.");
        return true;
    }

    public boolean shopSelect() {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.DARK_AQUA + "Only players can interactively select coordinates.");
            return false;
        }

        if (canUseCommand(CommandTypes.SELECT)) {

            Player player = (Player) sender;

            String playerName = player.getName();
            if (!plugin.playerData.containsKey(playerName)) {
                plugin.playerData.put(playerName, new PlayerData(plugin, playerName));
            }
            plugin.playerData.get(playerName).isSelecting = !plugin.playerData.get(playerName).isSelecting;

            if (plugin.playerData.get(playerName).isSelecting) {
                sender.sendMessage(ChatColor.WHITE + "Shop selection enabled, follow the directions to create a shop");
                sender.sendMessage(ChatColor.DARK_AQUA + "Left click to select the bottom corner for a shop");
                sender.sendMessage(ChatColor.DARK_AQUA + "Right click to select the far upper corner for the shop");
            } else {
                sender.sendMessage(ChatColor.DARK_AQUA + "Selection disabled");
                plugin.playerData.put(playerName, new PlayerData(plugin, playerName));
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean shopCreate() {
        String creator = null;
        String world = null;
        long[] xyzA = new long[3];
        long[] xyzB = new long[3];

        // Get current shop
        if (sender instanceof Player) {
            Player player = (Player) sender;
            PlayerData pData = plugin.playerData.get(player.getName());          

            creator = player.getName();
            world = player.getWorld().getName();

            //Check permissions
            if (!canCreateShop(creator)) {
                sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + "You already have the maximum number of shops or don't have permission to create them!");
                return false;
            }

            // If player is select, use their selection
            if (pData.isSelecting) {
                if (!pData.checkSize()) {
                    String size = Config.SHOP_SIZE_MAX_WIDTH + "x" + Config.SHOP_SIZE_MAX_HEIGHT + "x" + Config.SHOP_SIZE_MAX_WIDTH;
                    player.sendMessage(ChatColor.DARK_AQUA + "Problem with selection. Max size is " + ChatColor.WHITE + size);
                    return false;
                }

                xyzA = pData.getPositionA();
                xyzB = pData.getPositionB();

                if (xyzA == null || xyzB == null) {
                    player.sendMessage(ChatColor.DARK_AQUA + "Problem with selection. Only one point selected");
                    return false;
                }
            } else {
                // get current position
                Location loc = player.getLocation();
                long x = loc.getBlockX();
                long y = loc.getBlockY();
                long z = loc.getBlockZ();

                if (Config.SHOP_SIZE_DEF_WIDTH % 2 == 0) {
                    xyzA[0] = x - (Config.SHOP_SIZE_DEF_WIDTH / 2);
                    xyzB[0] = x + (Config.SHOP_SIZE_DEF_WIDTH / 2);
                    xyzA[2] = z - (Config.SHOP_SIZE_DEF_WIDTH / 2);
                    xyzB[2] = z + (Config.SHOP_SIZE_DEF_WIDTH / 2);
                } else {
                    xyzA[0] = x - (Config.SHOP_SIZE_DEF_WIDTH / 2) + 1;
                    xyzB[0] = x + (Config.SHOP_SIZE_DEF_WIDTH / 2);
                    xyzA[2] = z - (Config.SHOP_SIZE_DEF_WIDTH / 2) + 1;
                    xyzB[2] = z + (Config.SHOP_SIZE_DEF_WIDTH / 2);
                }

                xyzA[1] = y - 1;
                xyzB[1] = y + Config.SHOP_SIZE_DEF_HEIGHT - 1;
            }

            if(!shopPositionOk(xyzA, xyzB, world)) {
                sender.sendMessage("A shop already exists here!");
                return false;
            }

            if (Config.SHOP_CHARGE_CREATE) {
                if (!canUseCommand(CommandTypes.CREATE_FREE)) {
                    if (!plugin.playerData.get(player.getName()).chargePlayer(player.getName(), Config.SHOP_CHARGE_CREATE_COST)) {
                        sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + "You need " + plugin.econManager.format(Config.SHOP_CHARGE_CREATE_COST) + " to create a shop.");
                        return false;
                    }
                }
            }

        } else {
            sender.sendMessage("Console is not implemented yet.");
            return false;
        }

        // Command matching     

        Pattern pattern = Pattern.compile("(?i)create\\s+(.*)");
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            String name = matcher.group(1);

            Shop shop = new Shop(UUID.randomUUID());
            shop.setCreator(creator);
            shop.setOwner(creator);
            shop.setName(name);
            shop.setWorld(world);
            shop.setLocations(new ShopLocation(xyzA), new ShopLocation(xyzB));

            // insert the shop into the world
            LocalShops.cuboidTree.insert(shop.getCuboid());
            log.info(String.format("[%s] Created: %s", plugin.pdfFile.getName(), shop.toString()));
            plugin.shopData.addShop(shop);

            for (Player player : plugin.getServer().getOnlinePlayers()) {
                plugin.playerListener.checkPlayerPosition(player);
            }

            // Disable selecting for player (if player)
            if(sender instanceof Player) {
                Player player = (Player) sender;
                plugin.playerData.get(player.getName()).isSelecting = false;
            }

            // write the file
            if (plugin.shopData.saveShop(shop)) {
                sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.WHITE + shop.getName() + ChatColor.DARK_AQUA + " was created successfully.");
                return true;
            } else {
                sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + "There was an error, could not create shop.");
                return false;
            }
        }

        // Show usage
        sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " create [ShopName]" + ChatColor.DARK_AQUA + " - Create a shop at your location.");
        return true;
    }

    public boolean shopMove() {

        if(!canUseCommand(CommandTypes.MOVE)) {
            sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + "You don't have permission to use this command");
            return false;
        }

        if(!(sender instanceof Player)) {
            sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + "Console is not implemented yet.");
            return false;
        }

        Pattern pattern = Pattern.compile("(?i)move\\s+(.*)");
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            String id = matcher.group(1);

            Player player = (Player) sender;
            Location location = player.getLocation();
            Shop thisShop = null;

            long[] xyzAold = new long[3];
            long[] xyzBold = new long[3];

            // check to see if that shop exists
            thisShop = plugin.shopData.getShop(id);
            if(thisShop == null) {
                sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + "Could not find shop: " + ChatColor.WHITE + id);
                return false;
            }

            // check if player has access
            if (!thisShop.getOwner().equalsIgnoreCase(player.getName())) {
                player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + "You must be the shop owner to move this shop.");
                return false;
            }

            // store shop info
            String shopName = thisShop.getName();
            xyzAold = thisShop.getLocationA().toArray();
            xyzBold = thisShop.getLocationB().toArray();

            long x = (long) location.getX();
            long y = (long) location.getY();
            long z = (long) location.getZ();

            // setup the cuboid for the tree
            long[] xyzA = new long[3];
            long[] xyzB = new long[3];

            if (plugin.playerData.containsKey(player.getName()) && plugin.playerData.get(player.getName()).isSelecting) {

                // Check if size is ok
                if (!plugin.playerData.get(player.getName()).checkSize()) {
                    String size = Config.SHOP_SIZE_MAX_WIDTH + "x" + Config.SHOP_SIZE_MAX_HEIGHT + "x" + Config.SHOP_SIZE_MAX_WIDTH;
                    player.sendMessage(ChatColor.DARK_AQUA + "Problem with selection. Max size is " + ChatColor.WHITE + size);
                    return false;
                }

                // if a custom size had been set, use that
                PlayerData data = plugin.playerData.get(player.getName());
                xyzA = data.getPositionA().clone();
                xyzB = data.getPositionB().clone();

                if (xyzA == null || xyzB == null) {
                    player.sendMessage(ChatColor.DARK_AQUA + "Problem with selection.");
                    return false;
                }
            } else {
                // otherwise calculate the shop from the player's location
                if (Config.SHOP_SIZE_DEF_WIDTH % 2 == 0) {
                    xyzA[0] = x - (Config.SHOP_SIZE_DEF_WIDTH / 2);
                    xyzB[0] = x + (Config.SHOP_SIZE_DEF_WIDTH / 2);
                    xyzA[2] = z - (Config.SHOP_SIZE_DEF_WIDTH / 2);
                    xyzB[2] = z + (Config.SHOP_SIZE_DEF_WIDTH / 2);
                } else {
                    xyzA[0] = x - (Config.SHOP_SIZE_DEF_WIDTH / 2) + 1;
                    xyzB[0] = x + (Config.SHOP_SIZE_DEF_WIDTH / 2);
                    xyzA[2] = z - (Config.SHOP_SIZE_DEF_WIDTH / 2) + 1;
                    xyzB[2] = z + (Config.SHOP_SIZE_DEF_WIDTH / 2);
                }

                xyzA[1] = y - 1;
                xyzB[1] = y + Config.SHOP_SIZE_DEF_HEIGHT - 1;

            }

            // remove the old shop from the cuboid
            long[] xyz = thisShop.getLocation();
            BookmarkedResult res = new BookmarkedResult();
            res = LocalShops.cuboidTree.relatedSearch(res.bookmark, xyz[0], xyz[1], xyz[2]);

            // get the shop's tree node and delete it
            for (PrimitiveCuboid shopLocation : res.results) {

                // for each shop that you find, check to see if we're already in
                // it
                // this should only find one shop node
                if (shopLocation.uuid == null)
                    continue;
                if (!shopLocation.world.equalsIgnoreCase(thisShop.getWorld()))
                    continue;

                LocalShops.cuboidTree.delete(shopLocation);
            }

            // need to check to see if the shop overlaps another shop
            if (shopPositionOk(xyzA, xyzB, player.getWorld().getName())) {

                PrimitiveCuboid tempShopCuboid = new PrimitiveCuboid(xyzA, xyzB);
                tempShopCuboid.uuid = thisShop.getUuid();
                tempShopCuboid.world = player.getWorld().getName();

                if (Config.SHOP_CHARGE_MOVE) {
                    if (!canUseCommand(CommandTypes.MOVE_FREE)) {
                        if (!plugin.playerData.get(player.getName()).chargePlayer(player.getName(), Config.SHOP_CHARGE_MOVE_COST)) {
                            // insert the old cuboid back into the world
                            tempShopCuboid = new PrimitiveCuboid(xyzAold, xyzBold);
                            tempShopCuboid.uuid = thisShop.getUuid();
                            tempShopCuboid.world = thisShop.getWorld();
                            LocalShops.cuboidTree.insert(tempShopCuboid);

                            player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + "You need " + plugin.econManager.format(Config.SHOP_CHARGE_MOVE_COST) + " to move a shop.");
                            return false;
                        }
                    }
                }

                // insert the shop into the world
                thisShop.setWorld(player.getWorld().getName());
                thisShop.setLocations(new ShopLocation(xyzA), new ShopLocation(xyzB));
                log.info(thisShop.getUuid().toString());
                plugin.shopData.deleteShop(thisShop);
                plugin.shopData.addShop(thisShop);
                LocalShops.cuboidTree.insert(tempShopCuboid);

                plugin.playerData.put(player.getName(), new PlayerData(plugin, player.getName()));

                // write the file
                if (plugin.shopData.saveShop(thisShop)) {
                    player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.WHITE + shopName + ChatColor.DARK_AQUA + " was moved successfully.");
                    return true;
                } else {
                    player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + "There was an error, could not move shop.");
                    return false;
                }
            } else {
                // insert the old cuboid back into the world
                PrimitiveCuboid tempShopCuboid = new PrimitiveCuboid(xyzAold, xyzBold);
                tempShopCuboid.uuid = thisShop.getUuid();
                tempShopCuboid.world = thisShop.getWorld();
                LocalShops.cuboidTree.insert(tempShopCuboid);
                return true;
            }            
        }

        // Show usage
        sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + "The command format is " + ChatColor.WHITE + "/" + commandLabel + " move [id]");
        sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + "Use " + ChatColor.WHITE + "/" + commandLabel + " info" + ChatColor.DARK_AQUA + " to obtain the id.");
        return true;
    }

    public boolean canUseCommand(CommandTypes type) {
        PermissionHandler pm = plugin.pluginListener.gmPermissionCheck;

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (plugin.pluginListener.usePermissions) {
                // Using permissions, check them

                // check if admin first
                for (String permission : CommandTypes.ADMIN.getPermissions()) {
                    if (pm.has(player, permission)) {
                        return true;
                    }
                }

                // fail back to provided permissions second
                for (String permission : type.getPermissions()) {
                    if (!pm.has(player, permission)) {
                        return false;
                    }
                }
                return true;
            } else {
                // Not using permissions, use op status
                return player.isOp();
            }
        } else {
            return true;
        }
    }

    public boolean shopHelp() {
        sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + "Here are the available commands [required] <optional>");

        if (canUseCommand(CommandTypes.ADD)) {
            sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " add" + ChatColor.DARK_AQUA + " - Add the item that you are holding to the shop.");
        }
        if (canUseCommand(CommandTypes.BROWSE)) {
            sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " browse <buy|sell> " + ChatColor.DARK_AQUA + "- List the shop's inventory.");
        }
        if (canUseCommand(CommandTypes.BUY)) {
            sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " buy [itemname] [number] " + ChatColor.DARK_AQUA + "- Buy this item.");
        }
        if (canUseCommand(CommandTypes.CREATE)) {
            sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " create [ShopName]" + ChatColor.DARK_AQUA + " - Create a shop at your location.");
        }
        if (canUseCommand(CommandTypes.DESTROY)) {
            sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " destroy" + ChatColor.DARK_AQUA + " - Destroy the shop you're in.");
        }
        if (canUseCommand(CommandTypes.MOVE)) {
            sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " move [ShopID]" + ChatColor.DARK_AQUA + " - Move a shop to your location.");
        }
        sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " search" + ChatColor.DARK_AQUA + " - Search for an item name.");
        if (canUseCommand(CommandTypes.SELECT)) {
            sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " select" + ChatColor.DARK_AQUA + " - Select two corners for custom shop size.");
        }
        if (canUseCommand(CommandTypes.SELL)) {
            sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " sell <#|all>" + ChatColor.DARK_AQUA + " - Sell the item in your hand.");
            sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " sell [itemname] [number]");
        }
        if (canUseCommand(CommandTypes.SET)) {
            sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " set" + ChatColor.DARK_AQUA + " - Display list of set commands");
        }
        if (canUseCommand(CommandTypes.REMOVE)) {
            sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " remove [itemname]" + ChatColor.DARK_AQUA + " - Stop selling item in shop.");
        }
        return true;
    }

    private boolean shopPositionOk(long[] xyzA, long[] xyzB, String worldName) {
        BookmarkedResult res = new BookmarkedResult();

        // make sure coords are in right order
        for (int i = 0; i < 3; i++) {
            if (xyzA[i] > xyzB[i]) {
                long temp = xyzA[i];
                xyzA[i] = xyzB[i];
                xyzB[i] = temp;
            }
        }

        // Need to test every position to account for variable shop sizes

        for (long x = xyzA[0]; x <= xyzB[0]; x++) {
            for (long z = xyzA[2]; z <= xyzB[2]; z++) {
                for (long y = xyzA[1]; y <= xyzB[1]; y++) {
                    res = LocalShops.cuboidTree.relatedSearch(res.bookmark, x, y, z);
                    if (shopOverlaps(res, worldName))
                        return false;
                }
            }
        }
        return true;
    }

    private boolean shopOverlaps(BookmarkedResult res, String worldName) {
        if (res.results.size() != 0) {
            for (PrimitiveCuboid cuboid : res.results) {
                if (cuboid.uuid != null) {
                    if (cuboid.world.equalsIgnoreCase(worldName)) {
                        Shop shop = plugin.shopData.getShop(cuboid.uuid);
                        sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + "Could not create shop, it overlaps with " + ChatColor.WHITE + shop.getName());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean shopBrowse() {
        Shop shop = null;

        // Get current shop
        if (sender instanceof Player) {
            // Get player & data
            Player player = (Player) sender;
            PlayerData pData = plugin.playerData.get(player.getName());

            // Get Current Shop
            UUID shopUuid = pData.getCurrentShop();
            if (shopUuid != null) {
                shop = plugin.shopData.getShop(shopUuid);
            }
            if (shop == null) {
                sender.sendMessage("You are not in a shop!");
                return true;
            }

            // Check Permissions
            if (!canUseCommand(CommandTypes.BROWSE)) {
                sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + "You don't have permission to use this command");
                return true;
            }

        } else {
            sender.sendMessage("Console is not implemented yet.");
            return true;
        }

        if(shop.getItems().size() == 0) {
            sender.sendMessage(String.format("%s currently does not stock any items.", shop.getName()));
            return true;
        }

        int pageNumber = 1;

        // browse
        Pattern pattern = Pattern.compile("(?i)(bro|brow|brows|browse)$");
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            printInventory(shop, "list", pageNumber);
            return true;
        }

        // browse (buy|sell) pagenum
        matcher.reset();
        pattern = Pattern.compile("(?i)bro.*\\s+(buy|sell|info)\\s+(\\d+)");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            String type = matcher.group(1);
            pageNumber = Integer.parseInt(matcher.group(2));
            printInventory(shop, type, pageNumber);
            return true;
        }

        // browse (buy|sell)
        matcher.reset();
        pattern = Pattern.compile("(?i)bro.*\\s+(buy|sell|info)");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            String type = matcher.group(1);
            printInventory(shop, type, pageNumber);
            return true;
        }        

        // browse int
        matcher.reset();
        pattern = Pattern.compile("(?i)bro.*\\s+(\\d+)");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            pageNumber = Integer.parseInt(matcher.group(1));
            printInventory(shop, "list", pageNumber);
            return true;
        }

        return false;
    }

    /**
     * Prints shop inventory with default page # = 1
     * 
     * @param shop
     * @param player
     * @param buySellorList
     */
    public void printInventory(Shop shop, String buySellorList) {
        printInventory(shop, buySellorList, 1);
    }

    /**
     * Prints shop inventory list. Takes buy, sell, or list as arguments for
     * which format to print.
     * 
     * @param shop
     * @param player
     * @param buySellorList
     * @param pageNumber
     */
    public void printInventory(Shop shop, String buySellorList, int pageNumber) {
        String inShopName = shop.getName();
        Collection<InventoryItem> items = shop.getItems();

        boolean buy = buySellorList.equalsIgnoreCase("buy");
        boolean sell = buySellorList.equalsIgnoreCase("sell");
        boolean list = buySellorList.equalsIgnoreCase("list");

        ArrayList<String> inventoryMessage = new ArrayList<String>();
        for (InventoryItem item : items) {

            String subMessage = "   " + item.getInfo().name;
            int maxStock = 0;

            // NOT list
            if (!list) {
                double price = 0;
                if (buy) {
                    // get buy price
                    price = item.getBuyPrice();
                }
                if (sell) {
                    price = item.getSellPrice();
                }
                if (price == 0) {
                    continue;
                }
                subMessage += ChatColor.DARK_AQUA + " [" + ChatColor.WHITE + plugin.econManager.format(price) + ChatColor.DARK_AQUA + "]";
                // get stack size
                int stack = 0;
                if (buy) {
                    stack = item.getBuySize();
                }
                if (sell) {
                    stack = item.getSellSize();
                    int stock = item.getStock();
                    maxStock = item.getMaxStock();

                    if (stock >= maxStock && !(maxStock == 0)) {
                        continue;
                    }
                }
                if (stack > 1) {
                    subMessage += ChatColor.DARK_AQUA + " [" + ChatColor.WHITE + "Bundle: " + stack + ChatColor.DARK_AQUA + "]";
                }
            }

            // get stock
            int stock = item.getStock();
            if (buy) {
                if (stock == 0 && !shop.isUnlimitedStock())
                    continue;
            }
            if (!shop.isUnlimitedStock()) {
                subMessage += ChatColor.DARK_AQUA + " [" + ChatColor.WHITE + "Stock: " + stock + ChatColor.DARK_AQUA + "]";

                maxStock = item.getMaxStock();
                if (maxStock > 0) {
                    subMessage += ChatColor.DARK_AQUA + " [" + ChatColor.WHITE + "Max Stock: " + maxStock + ChatColor.DARK_AQUA + "]";
                }
            }

            inventoryMessage.add(subMessage);
        }

        String message = ChatColor.DARK_AQUA + "The shop " + ChatColor.WHITE + inShopName + ChatColor.DARK_AQUA;

        if (buy) {
            message += " is selling:";
        } else if (sell) {
            message += " is buying:";
        } else {
            message += " trades in: ";
        }

        message += " (Page " + pageNumber + " of " + (int) Math.ceil((double) inventoryMessage.size() / (double) 7) + ")";

        sender.sendMessage(message);

        if(inventoryMessage.size() <= (pageNumber - 1) * 7) {
            sender.sendMessage(String.format("%s does not have this many pages!", shop.getName()));
            return;
        }

        int amount = (pageNumber > 0 ? (pageNumber - 1) * 7 : 0);
        for (int i = amount; i < amount + 7; i++) {
            if (inventoryMessage.size() > i) {
                sender.sendMessage(inventoryMessage.get(i));
            }
        }

        if (!list) {
            String buySell = (buy ? "buy" : "sell");
            message = ChatColor.DARK_AQUA + "To " + buySell + " an item on the list type: " + ChatColor.WHITE + "/" + commandLabel + " " + buySell + " ItemName [amount]";
            sender.sendMessage(message);
        } else {
            sender.sendMessage(ChatColor.DARK_AQUA + "Type " + ChatColor.WHITE + "/" + commandLabel + " browse buy"  + ChatColor.DARK_AQUA + " or " + ChatColor.WHITE + "/" + commandLabel + " browse sell");
            sender.sendMessage(ChatColor.DARK_AQUA + "to see details about price and quantity.");
        }
    }

    private boolean shopSell(Shop shop, ItemInfo item, int amount) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("/shop sell can only be used for players!");
            return false;
        }

        Player player = (Player) sender;
        InventoryItem invItem = shop.getItem(item.name);
        PlayerData pData = plugin.playerData.get(player.getName());

        // check if the shop is buying that item
        if (!shop.containsItem(item) || shop.getItem(item.name).getSellPrice() == 0) {
            player.sendMessage(ChatColor.DARK_AQUA + "Sorry, " + ChatColor.WHITE + shop.getName() + ChatColor.DARK_AQUA + " is not buying " + ChatColor.WHITE + item.name + ChatColor.DARK_AQUA + " right now.");
            return false;
        }

        // check how many items the player has
        int playerInventory = countItemsInInventory(player.getInventory(), item.toStack());
        if (amount < 0) {
            amount = 0;
        }

        // check if the amount to add is okay
        if (amount > playerInventory) {
            player.sendMessage(ChatColor.DARK_AQUA + "You only have " + ChatColor.WHITE + playerInventory + ChatColor.DARK_AQUA + " in your inventory that can be added.");
            amount = playerInventory;
        }

        // check if the shop has a max stock level set
        if (invItem.getMaxStock() != 0 && !shop.isUnlimitedStock()) {
            if (invItem.getStock() >= invItem.getMaxStock()) {
                player.sendMessage(ChatColor.DARK_AQUA + "Sorry, " + ChatColor.WHITE + shop.getName() + ChatColor.DARK_AQUA + " is not buying any more " + ChatColor.WHITE + item.name + ChatColor.DARK_AQUA + " right now.");
                return false;
            }

            if (amount > (invItem.getMaxStock() - invItem.getStock())) {
                amount = invItem.getMaxStock() - invItem.getStock();
            }
        }

        // calculate cost
        int bundles = amount / invItem.getSellSize();

        if (bundles == 0 && amount > 0) {
            player.sendMessage(ChatColor.DARK_AQUA + "The minimum number to sell is  " + ChatColor.WHITE + invItem.getSellSize());
            return false;
        }

        double itemPrice = invItem.getSellPrice();
        // recalculate # of items since may not fit cleanly into bundles
        // notify player if there is a change
        if (amount % invItem.getSellSize() != 0) {
            player.sendMessage(ChatColor.DARK_AQUA + "The bundle size is  " + ChatColor.WHITE + invItem.getSellSize() + ChatColor.DARK_AQUA + " order reduced to " + ChatColor.WHITE + bundles * invItem.getSellSize());
        }
        amount = bundles * invItem.getSellSize();
        double totalCost = bundles * itemPrice;

        // try to pay the player for order
        if (shop.isUnlimitedMoney()) {
            pData.payPlayer(player.getName(), totalCost);
        } else {
            if (!isShopController(shop)) {
                log.info(String.format("From: %s, To: %s, Cost: %f", shop.getOwner(), player.getName(), totalCost));
                if (!pData.payPlayer(shop.getOwner(), player.getName(), totalCost)) {
                    // lshop owner doesn't have enough money
                    // get shop owner's balance and calculate how many it can
                    // buy
                    double shopBalance = plugin.playerData.get(player.getName()).getBalance(shop.getOwner());
                    // the current shop balance must be greater than the minimum
                    // balance to do the transaction.
                    if (shopBalance <= shop.getMinBalance()) {
                        player.sendMessage(ChatColor.DARK_AQUA + shop.getName() + " is broke!");
                        return false;
                    }
                    // Added Min Balance calculation for maximum items the shop can afford
                    int bundlesCanAford = (int) Math.floor(shopBalance - shop.getMinBalance() / itemPrice);
                    totalCost = bundlesCanAford * itemPrice;
                    amount = bundlesCanAford * invItem.getSellSize();
                    player.sendMessage(ChatColor.DARK_AQUA + shop.getName() + " could only afford " + ChatColor.WHITE + bundlesCanAford + ChatColor.DARK_AQUA + " bundles.");
                    if (!pData.payPlayer(shop.getOwner(), player.getName(), totalCost)) {
                        player.sendMessage(ChatColor.DARK_AQUA + "Unexpected money problem: could not complete sale.");
                        return false;
                    }
                }
            }
        }

        if (!shop.isUnlimitedStock()) {
            shop.addStock(item.name, amount);
        }

        if (isShopController(shop)) {
            player.sendMessage(ChatColor.DARK_AQUA + "You added " + ChatColor.WHITE + amount + " " + item.name + ChatColor.DARK_AQUA + " to the shop");
        } else {
            player.sendMessage(ChatColor.DARK_AQUA + "You sold " + ChatColor.WHITE + amount + " " + item.name + ChatColor.DARK_AQUA + " and gained " + ChatColor.WHITE + plugin.econManager.format(totalCost));
        }

        // log the transaction
        int itemInv = invItem.getStock();
        int startInv = itemInv - amount;
        if (startInv < 0)
            startInv = 0;
        plugin.shopData.logItems(player.getName(), shop.getName(), "sell-item", item.name, amount, startInv, itemInv);

        removeItemsFromInventory(player.getInventory(), item.toStack(), amount);
        plugin.shopData.saveShop(shop);

        return true;
    }

    /**
     * Processes sell command.
     * 
     * @param sender
     * @param args
     * @return true - if command succeeds false otherwise
     */
    public boolean shopSell() {
        Shop shop = null;

        // Get current shop
        if (sender instanceof Player) {
            // Get player & data
            Player player = (Player) sender;
            PlayerData pData = plugin.playerData.get(player.getName());

            // Get Current Shop
            UUID shopUuid = pData.getCurrentShop();
            if (shopUuid != null) {
                shop = plugin.shopData.getShop(shopUuid);
            }
            if (shop == null) {
                sender.sendMessage("You are not in a shop!");
                return true;
            }

            // Check Permissions
            if (!canUseCommand(CommandTypes.SELL)) {
                sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + "You don't have permission to use this command");
                return true;
            }

            // sell all (player only command)
            Pattern pattern = Pattern.compile("(?i)sell\\s+all");
            Matcher matcher = pattern.matcher(command);
            if (matcher.find()) {
                ItemStack itemStack = player.getItemInHand();
                if (itemStack == null) {
                    sender.sendMessage("You must be holding an item, or specify an item.");
                    return true;
                }
                ItemInfo item = null;
                int amount = itemStack.getAmount();
                if(LocalShops.itemList.isDurable(itemStack)) {
                    item = Search.itemById(itemStack.getTypeId());
                    if (calcDurabilityPercentage(itemStack) > Config.ITEM_MAX_DAMAGE && Config.ITEM_MAX_DAMAGE != 0) {
                        sender.sendMessage(ChatColor.DARK_AQUA + "Your " + ChatColor.WHITE + item.name + ChatColor.DARK_AQUA + " is too damaged to sell!");
                        sender.sendMessage(ChatColor.DARK_AQUA + "Items must be damanged less than " + ChatColor.WHITE + Config.ITEM_MAX_DAMAGE + "%");
                        return true;
                    }
                } else {
                    item = Search.itemById(itemStack.getTypeId(), itemStack.getDurability());
                }
                if(item == null) {
                    sender.sendMessage("Could not find an item.");
                    return true;
                }
                return shopSell(shop, item, amount);
            }

            // sell (player only command)
            matcher.reset();
            pattern = Pattern.compile("(?i)sell$");
            matcher = pattern.matcher(command);
            if (matcher.find()) {
                ItemStack itemStack = player.getItemInHand();
                if (itemStack == null) {
                    return true;
                }
                ItemInfo item = null;
                int amount = itemStack.getAmount();
                if(LocalShops.itemList.isDurable(itemStack)) {
                    item = Search.itemById(itemStack.getTypeId());
                    if (calcDurabilityPercentage(itemStack) > Config.ITEM_MAX_DAMAGE && Config.ITEM_MAX_DAMAGE != 0) {
                        sender.sendMessage(ChatColor.DARK_AQUA + "Your " + ChatColor.WHITE + item.name + ChatColor.DARK_AQUA + " is too damaged to sell!");
                        sender.sendMessage(ChatColor.DARK_AQUA + "Items must be damanged less than " + ChatColor.WHITE + Config.ITEM_MAX_DAMAGE + "%");
                        return true;
                    }
                } else {
                    item = Search.itemById(itemStack.getTypeId(), itemStack.getDurability());
                }
                if(item == null) {
                    sender.sendMessage("Could not find an item.");
                    return true;
                }
                return shopSell(shop, item, amount);
            }            
        } else {
            sender.sendMessage("Console is not implemented yet.");
            return true;
        }

        // Command matching

        // sell int
        Pattern pattern = Pattern.compile("(?i)sell\\s+(\\d+)");
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            ItemInfo item = Search.itemById(id);
            if(item == null) {
                sender.sendMessage("Could not find an item.");
                return true;
            }
            return shopSell(shop, item, 0);
        }

        // sell int int
        matcher.reset();
        pattern = Pattern.compile("(?i)sell\\s+(\\d+)\\s+(\\d+)");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            int count = Integer.parseInt(matcher.group(2));
            ItemInfo item = Search.itemById(id);
            if(item == null) {
                sender.sendMessage("Could not find an item.");
                return true;
            }
            return shopSell(shop, item, count);
        }

        // sell int all
        matcher.reset();
        pattern = Pattern.compile("(?i)sell\\s+(\\d+)\\s+all");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            int count = Integer.parseInt(matcher.group(2));
            ItemInfo item = Search.itemById(id);
            if(item == null) {
                sender.sendMessage("Could not find an item.");
                return true;
            }
            return shopSell(shop, item, count);
        }        

        // sell int:int
        matcher.reset();
        pattern = Pattern.compile("(?i)sell\\s+(\\d+):(\\d+)");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            short type = Short.parseShort(matcher.group(2));
            ItemInfo item = Search.itemById(id, type);
            if(item == null) {
                sender.sendMessage("Could not find an item.");
                return true;
            }
            return shopSell(shop, item, 0);
        }

        // sell int:int int
        matcher.reset();
        pattern = Pattern.compile("(?i)sell\\s+(\\d+):(\\d+)\\s+(\\d+)");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            short type = Short.parseShort(matcher.group(2));
            ItemInfo item = Search.itemById(id, type);
            int count = Integer.parseInt(matcher.group(3));
            if(item == null) {
                sender.sendMessage("Could not find an item.");
                return true;
            }
            return shopSell(shop, item, count);
        }

        // sell int:int all
        matcher.reset();
        pattern = Pattern.compile("(?i)sell\\s+(\\d+):(\\d+)\\s+all");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            short type = Short.parseShort(matcher.group(2));
            ItemInfo item = Search.itemById(id, type);
            int count = Integer.parseInt(matcher.group(3));
            if(item == null) {
                sender.sendMessage("Could not find an item.");
                return true;
            }
            return shopSell(shop, item, count);
        }        

        // shop sell name, ... int
        matcher.reset();
        pattern = Pattern.compile("(?i)sell\\s+(.*)\\s+(\\d+)");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            String itemName = matcher.group(1);
            ItemInfo item = Search.itemByName(itemName);
            int count = Integer.parseInt(matcher.group(2));
            if(item == null) {
                sender.sendMessage("Could not find an item.");
                return true;
            }
            return shopSell(shop, item, count);
        }

        // shop sell name, ... all
        matcher.reset();
        pattern = Pattern.compile("(?i)sell\\s+(.*)\\s+all");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            Player player = (Player) sender;
            String itemName = matcher.group(1);
            ItemInfo item = Search.itemByName(itemName);
            if(item == null) {
                sender.sendMessage("Could not find an item.");
                return true;
            }
            int count = this.countItemsInInventory(player.getInventory(), item.toStack());
            return shopSell(shop, item, count);
        }        

        // shop sell name, ...
        matcher.reset();
        pattern = Pattern.compile("(?i)sell\\s+(.*)");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            String itemName = matcher.group(1);
            ItemInfo item = Search.itemByName(itemName);
            if(item == null) {
                sender.sendMessage("Could not find an item.");
                return true;
            }
            return shopSell(shop, item, 1);
        }

        // Show sell help
        sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " sell [itemname] [number] " + ChatColor.DARK_AQUA + "- Sell this item.");
        return true;
    }

    private boolean shopAdd(Shop shop, ItemInfo item, int amount) {
        Player player = null;

        // Assign in sender is a Player
        if (sender instanceof Player) {
            player = (Player) sender;
        }

        // Calculate number of items player has
        if (player != null) {
            int playerItemCount = countItemsInInventory(player.getInventory(), item.toStack());
            // Validate Count
            if (playerItemCount >= amount) {
                // Perform add
                log.info(String.format("Add %d of %s to %s", amount, item, shop));
            } else {
                // Nag player
                sender.sendMessage(ChatColor.DARK_AQUA + "You only have " + ChatColor.WHITE + playerItemCount + ChatColor.DARK_AQUA + " in your inventory that can be added.");
                // amount = playerItemCount;
                return false;
            }

            // If ALL (amount == -1), set amount to the count the player has
            if (amount == -1) {
                amount = playerItemCount;
            }
        }

        // If shop contains item
        if (shop.containsItem(item)) {
            // Check if stock is unlimited
            if (shop.isUnlimitedStock()) {
                // nicely message user
                sender.sendMessage(String.format("%s has unlimited stock and already carries %s!", shop.getName(), item.name));
                return true;
            }

            // Check if amount to be added is 0 (no point adding 0)
            if (amount == 0) {
                // nicely message user
                sender.sendMessage(String.format("%s already carries %s!", shop.getName(), item.name));
                return true;
            }
        }

        // Add item to shop if needed
        if (!shop.containsItem(item)) {
            shop.addItem(item.typeId, item.subTypeId, 0, 1, 0, 1, 0, 0);
        }

        // Check stock settings, add stock if necessary
        if (shop.isUnlimitedStock()) {
            sender.sendMessage(ChatColor.DARK_AQUA + "Succesfully added " + ChatColor.WHITE + item.name + ChatColor.DARK_AQUA + " to the shop.");
        } else {
            shop.addStock(item.name, amount);
            sender.sendMessage(ChatColor.DARK_AQUA + "Succesfully added " + ChatColor.WHITE + item.name + ChatColor.DARK_AQUA + " to the shop. Stock is now " + ChatColor.WHITE + shop.getItem(item.name).getStock());
        }
        
        if(amount == 0) {
            sender.sendMessage(ChatColor.DARK_AQUA + item.name + " is almost ready to be purchased or sold!");
            sender.sendMessage(ChatColor.DARK_AQUA + "Use " + ChatColor.WHITE + "\"/shop set sell " + item.name + " price bundle\"" + ChatColor.DARK_AQUA + " to sell this item!");
            sender.sendMessage(ChatColor.DARK_AQUA + "Use " + ChatColor.WHITE + "\"/shop set  buy " + item.name + " price bundle\"" + ChatColor.DARK_AQUA + " to  buy this item!");
        }

        // log the transaction
        if (player != null) {
            int itemInv = shop.getItem(item.name).getStock();
            int startInv = itemInv - amount;
            if (startInv < 0) {
                startInv = 0;
            }
            plugin.shopData.logItems(player.getName(), shop.getName(), "add-item", item.name, amount, startInv, itemInv);

            // take items from player only if shop doesn't have unlim stock
            if(!shop.isUnlimitedStock()) {
                removeItemsFromInventory(player.getInventory(), item.toStack(), amount);
            }
        }
        plugin.shopData.saveShop(shop);
        return true;
    }

    public boolean shopInfo() {
        Shop shop = null;

        // Get current shop
        if (sender instanceof Player) {
            // Get player & data
            Player player = (Player) sender;
            PlayerData pData = plugin.playerData.get(player.getName());

            // info (player only command)
            Pattern pattern = Pattern.compile("(?i)info$");
            Matcher matcher = pattern.matcher(command);
            if (matcher.find()) {
                // Get Current Shop
                UUID shopUuid = pData.getCurrentShop();
                if (shopUuid != null) {
                    shop = plugin.shopData.getShop(shopUuid);
                }
                if (shop == null) {
                    sender.sendMessage("You are not in a shop!");
                    return false;
                }
            }

            // info id
            matcher.reset();
            pattern = Pattern.compile("(?i)info\\s+(.*)$");
            matcher = pattern.matcher(command);
            if (matcher.find()) {
                String input = matcher.group(1);
                shop = plugin.shopData.getShop(input);
                if (shop == null) {
                    sender.sendMessage("Could not find shop with ID " + input);
                    return false;
                }
            }

        } else {
            sender.sendMessage("Console is not implemented yet.");
            return false;
        }

        int managerCount = shop.getManagers().size();

        sender.sendMessage(String.format(ChatColor.DARK_AQUA + "Shop Info about " + ChatColor.WHITE + "\"%s\"" + ChatColor.DARK_AQUA + " ID: " + ChatColor.WHITE + "%s", shop.getName(), shop.getShortUuidString()));
        if(shop.getCreator().equalsIgnoreCase(shop.getOwner())) {
            if(managerCount == 0) {
                sender.sendMessage(String.format("  Owned & Created by %s with no managers.", shop.getCreator()));
            } else {
                sender.sendMessage(String.format("  Owned & Created by %s with %d managers.", shop.getCreator(), managerCount));
            }
        }
        if(managerCount > 0) {
            sender.sendMessage(String.format("  Managed by %s", Search.join(shop.getManagers(), " ")));
        }

        if(command.matches("info\\s+full")) {
            sender.sendMessage(String.format("  Full Id: %s", shop.getUuid().toString()));
        }

        sender.sendMessage(String.format("  Located at %s x %s in \"%s\"", shop.getLocationA().toString(), shop.getLocationB().toString(), shop.getWorld()));

        // Calculate values
        int sellCount = 0;
        int buyCount = 0;
        int worth = 0;

        Iterator<InventoryItem> it = shop.getItems().iterator();
        while(it.hasNext()) {
            InventoryItem i = it.next();
            if(i.getBuyPrice() > 0) {
                sellCount++;
                worth += (i.getStock()/i.getBuySize()) * i.getBuyPrice();
            }

            if(i.getSellPrice() > 0) {
                buyCount++;
            }
        }

        // Selling %d items & buying %d items
        sender.sendMessage(String.format("  Selling %d items & buying %d items", sellCount, buyCount));

        // Shop stock is worth %d coins
        sender.sendMessage(String.format("  Inventory worth %s", plugin.econManager.format(worth)));

        if(shop.isUnlimitedMoney() || shop.isUnlimitedStock()) {
            sender.sendMessage(String.format("  Shop %s unlimited money and %s unlimited stock.", shop.isUnlimitedMoney() ? "has" : "doesn't have", shop.isUnlimitedStock() ? "has" : "doesn't have"));
        }

        return true;
    }

    /**
     * Add an item to the shop. Checks if shop manager or owner and takes item
     * from inventory of player. If item is not sold in the shop yet, adds the
     * item with buy and sell price of 0 and default bundle sizes of 1.
     * 
     * Format: shop add shop add all
     * 
     * shop add [name, ...] shop add [name, ...] count
     * 
     * shop add id:type shop add id:type count
     * 
     * shop add id shop add id count
     * 
     * @return true if the commands succeeds, otherwise false
     */
    public boolean shopAdd() {
        Shop shop = null;

        // Get current shop
        if (sender instanceof Player) {
            // Get player & data
            Player player = (Player) sender;
            PlayerData pData = plugin.playerData.get(player.getName());

            // Get Current Shop
            UUID shopUuid = pData.getCurrentShop();
            if (shopUuid != null) {
                shop = plugin.shopData.getShop(shopUuid);
            }
            if (shop == null) {
                sender.sendMessage("You are not in a shop!");
                return false;
            }

            // Check Permissions
            if (!canUseCommand(CommandTypes.ADD)) {
                sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + "You don't have permission to use this command");
                return false;
            }            

            // Check if Player can Modify
            if (!isShopController(shop)) {
                player.sendMessage(ChatColor.DARK_AQUA + "You must be the shop owner or a manager to set this.");
                player.sendMessage(ChatColor.DARK_AQUA + "The current shop owner is " + ChatColor.WHITE + shop.getOwner());
                return true;
            }

            // add (player only command)
            Pattern pattern = Pattern.compile("(?i)add$");
            Matcher matcher = pattern.matcher(command);
            if (matcher.find()) {
                ItemStack itemStack = player.getItemInHand();
                if (itemStack == null) {
                    return false;
                }
                ItemInfo item = null;
                int amount = itemStack.getAmount();
                if(LocalShops.itemList.isDurable(itemStack)) {
                    item = Search.itemById(itemStack.getTypeId());
                    if (calcDurabilityPercentage(itemStack) > Config.ITEM_MAX_DAMAGE && Config.ITEM_MAX_DAMAGE != 0) {
                        sender.sendMessage(ChatColor.DARK_AQUA + "Your " + ChatColor.WHITE + item.name + ChatColor.DARK_AQUA + " is too damaged to add to stock!");
                        sender.sendMessage(ChatColor.DARK_AQUA + "Items must be damanged less than " + ChatColor.WHITE + Config.ITEM_MAX_DAMAGE + "%");
                        return true;
                    }
                } else {
                    item = Search.itemById(itemStack.getTypeId(), itemStack.getDurability());
                }
                if(item == null) {
                    sender.sendMessage("Could not find an item.");
                    return false;
                }
                return shopAdd(shop, item, amount);
            }

            // add all (player only command)
            matcher.reset();
            pattern = Pattern.compile("(?i)add\\s+all$");
            matcher = pattern.matcher(command);
            if (matcher.find()) {
                ItemStack itemStack = player.getItemInHand();
                if (itemStack == null) {
                    return false;
                }
                ItemInfo item = null;
                if(LocalShops.itemList.isDurable(itemStack)) {
                    item = Search.itemById(itemStack.getTypeId());
                    if (calcDurabilityPercentage(itemStack) > Config.ITEM_MAX_DAMAGE && Config.ITEM_MAX_DAMAGE != 0) {
                        sender.sendMessage(ChatColor.DARK_AQUA + "Your " + ChatColor.WHITE + item.name + ChatColor.DARK_AQUA + " is too damaged to add to stock!");
                        sender.sendMessage(ChatColor.DARK_AQUA + "Items must be damanged less than " + ChatColor.WHITE + Config.ITEM_MAX_DAMAGE + "%");
                        return true;
                    }
                } else {
                    item = Search.itemById(itemStack.getTypeId(), itemStack.getDurability());
                }
                if(item == null) {
                    sender.sendMessage("Could not find an item.");
                    return false;
                }
                int amount = countItemsInInventory(player.getInventory(), itemStack);
                return shopAdd(shop, item, amount);
            }

            // add int all
            matcher.reset();
            pattern = Pattern.compile("(?i)add\\s+(\\d+)\\s+all$");
            matcher = pattern.matcher(command);
            if (matcher.find()) {
                int id = Integer.parseInt(matcher.group(1));
                ItemInfo item = Search.itemById(id);
                if(item == null) {
                    sender.sendMessage("Could not find an item.");
                    return false;
                }
                int count = countItemsInInventory(player.getInventory(), item.toStack());
                return shopAdd(shop, item, count);
            }

            // add int:int all
            matcher.reset();
            pattern = Pattern.compile("(?i)add\\s+(\\d+):(\\d+)\\s+all$");
            matcher = pattern.matcher(command);
            if (matcher.find()) {
                int id = Integer.parseInt(matcher.group(1));
                short type = Short.parseShort(matcher.group(2));
                ItemInfo item = Search.itemById(id, type);
                if(item == null) {
                    sender.sendMessage("Could not find an item.");
                    return false;
                }
                int count = countItemsInInventory(player.getInventory(), item.toStack());
                return shopAdd(shop, item, count);
            }

            // shop add name, ... all
            matcher.reset();
            pattern = Pattern.compile("(?i)add\\s+(.*)\\s+all$");
            matcher = pattern.matcher(command);
            if (matcher.find()) {
                String itemName = matcher.group(1);
                ItemInfo item = Search.itemByName(itemName);
                if(item == null) {
                    sender.sendMessage("Could not find an item.");
                    return false;
                }
                int count = countItemsInInventory(player.getInventory(), item.toStack());
                return shopAdd(shop, item, count);
            }

        } else {
            sender.sendMessage("Console is not implemented yet.");
            return false;
        }

        // Command matching     

        // add int
        Pattern pattern = Pattern.compile("(?i)add\\s+(\\d+)$");
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            ItemInfo item = Search.itemById(id);
            if(item == null) {
                sender.sendMessage("Could not find an item.");
                return false;
            }            
            return shopAdd(shop, item, 0);
        }

        // add int int
        matcher.reset();
        pattern = Pattern.compile("(?i)add\\s+(\\d+)\\s+(\\d+)$");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            int count = Integer.parseInt(matcher.group(2));
            ItemInfo item = Search.itemById(id);
            if(item == null) {
                sender.sendMessage("Could not find an item.");
                return false;
            }            
            return shopAdd(shop, item, count);
        }

        // add int:int
        matcher.reset();
        pattern = Pattern.compile("(?i)add\\s+(\\d+):(\\d+)$");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            short type = Short.parseShort(matcher.group(2));
            ItemInfo item = Search.itemById(id, type);
            if(item == null) {
                sender.sendMessage("Could not find an item.");
                return false;
            }
            return shopAdd(shop, item, 0);
        }

        // add int:int int
        matcher.reset();
        pattern = Pattern.compile("(?i)add\\s+(\\d+):(\\d+)\\s+(\\d+)$");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            short type = Short.parseShort(matcher.group(2));
            ItemInfo item = Search.itemById(id, type);
            int count = Integer.parseInt(matcher.group(3));
            if(item == null) {
                sender.sendMessage("Could not find an item.");
                return false;
            }
            return shopAdd(shop, item, count);
        }

        // shop add name, ... int
        matcher.reset();
        pattern = Pattern.compile("(?i)add\\s+(.*)\\s+(\\d+)$");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            String itemName = matcher.group(1);
            ItemInfo item = Search.itemByName(itemName);
            int count = Integer.parseInt(matcher.group(2));
            if(item == null) {
                sender.sendMessage("Could not find an item.");
                return false;
            }
            return shopAdd(shop, item, count);
        }

        // shop add name, ...
        matcher.reset();
        pattern = Pattern.compile("(?i)add\\s+(.*)$");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            String itemName = matcher.group(1);
            ItemInfo item = Search.itemByName(itemName);
            if(item == null) {
                sender.sendMessage("Could not find an item.");
                return false;
            }
            return shopAdd(shop, item, 0);
        }

        // Show buy help
        sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " buy [itemname] [number] " + ChatColor.DARK_AQUA + "- Buy this item.");
        return true;
    }

    /**
     * Returns true if the player is in the shop manager list or is the shop
     * owner
     * 
     * @param player
     * @param shop
     * @return
     */
    private boolean isShopController(Shop shop) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (shop.getOwner().equalsIgnoreCase(player.getName()))
                return true;
            if (shop.getManagers() != null) {
                for (String manager : shop.getManagers()) {
                    if (player.getName().equalsIgnoreCase(manager)) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            return true;
        }
    }

    private boolean shopBuy(Shop shop, ItemInfo item, int amount) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("/shop sell can only be used for players!");
            return false;
        }

        Player player = (Player) sender;
        InventoryItem invItem = shop.getItem(item.name);
        PlayerData pData = plugin.playerData.get(player.getName());

        // check if the shop is buying that item
        if (invItem == null || invItem.getBuyPrice() == 0) {
            player.sendMessage(ChatColor.DARK_AQUA + "Sorry, " + ChatColor.WHITE + shop.getName() + ChatColor.DARK_AQUA + " is not selling " + ChatColor.WHITE + item.name + ChatColor.DARK_AQUA + " right now.");
            return false;
        } else if(invItem.getStock() == 0 && !shop.isUnlimitedStock()) {
            player.sendMessage(ChatColor.DARK_AQUA + "Sorry, " + ChatColor.WHITE + shop.getName() + ChatColor.DARK_AQUA + " is sold out of " + ChatColor.WHITE + item.name + ChatColor.DARK_AQUA + " right now.");
            return false;
        }

        // check if the item has a price, or if this is a shop owner
        if (invItem.getBuyPrice() == 0 && !isShopController(shop)) {
            player.sendMessage(ChatColor.DARK_AQUA + "Sorry, " + ChatColor.WHITE + shop.getName() + ChatColor.DARK_AQUA + " is not selling " + ChatColor.WHITE + item.name + ChatColor.DARK_AQUA + " right now.");
            return false;
        }

        // if amount = 0, assume single stack size
        if(amount == 0) {
            amount = invItem.getBuySize();
        }

        int totalAmount;
        totalAmount = invItem.getStock();

        if (totalAmount == 0 && !shop.isUnlimitedStock()) {
            player.sendMessage(ChatColor.DARK_AQUA + "The shop has " + ChatColor.WHITE + totalAmount + " " + item.name);
            return true;
        }

        if (amount < 0) {
            amount = 0;
        }

        if (shop.isUnlimitedStock()) {
            totalAmount = amount;
        }
        if (amount > totalAmount) {
            amount = totalAmount - (totalAmount % invItem.getBuySize());
            if (!shop.isUnlimitedStock()) {
                player.sendMessage(ChatColor.DARK_AQUA + "The shop has " + ChatColor.WHITE + totalAmount + " " + item.name);
            }
        } else if(amount % invItem.getBuySize() != 0){
            amount = amount - (amount % invItem.getBuySize());
            player.sendMessage(ChatColor.DARK_AQUA + "The bundle size is  " + ChatColor.WHITE + invItem.getBuySize() + ChatColor.DARK_AQUA + " order reduced to " + ChatColor.WHITE + amount);
        }

        // check how many items the user has room for
        int freeSpots = 0;
        for (ItemStack thisSlot : player.getInventory().getContents()) {
            if (thisSlot == null || thisSlot.getType() == Material.AIR) {
                freeSpots += 64;
                continue;
            }
            if (thisSlot.getTypeId() == item.typeId && thisSlot.getDurability() == item.subTypeId) {
                freeSpots += 64 - thisSlot.getAmount();
            }
        }

        // Calculate the amount the player can store
        if (amount > freeSpots) {
            amount = freeSpots - (freeSpots % invItem.getBuySize());
            player.sendMessage(ChatColor.DARK_AQUA + "You only have room for " + ChatColor.WHITE + amount);
        }

        // calculate cost
        int bundles = amount / invItem.getBuySize();
        double itemPrice = invItem.getBuyPrice();
        // recalculate # of items since may not fit cleanly into bundles
        amount = bundles * invItem.getBuySize();
        double totalCost = bundles * itemPrice;

        // try to pay the shop owner
        if (!isShopController(shop)) {
            if (!pData.payPlayer(player.getName(), shop.getOwner(), totalCost)) {
                // player doesn't have enough money
                // get player's balance and calculate how many it can buy
                double playerBalance = pData.getBalance(player.getName());
                int bundlesCanAford = (int) Math.floor(playerBalance / itemPrice);
                totalCost = bundlesCanAford * itemPrice;
                amount = bundlesCanAford * invItem.getSellSize();
                player.sendMessage(ChatColor.DARK_AQUA + "You could only afford " + ChatColor.WHITE + amount);

                if (!pData.payPlayer(player.getName(), shop.getOwner(), totalCost)) {
                    player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + "Unexpected money problem: could not complete sale.");
                    return false;
                }
            }
        }

        if (!shop.isUnlimitedStock()) {
            shop.removeStock(item.name, amount);
        }
        if (isShopController(shop)) {
            player.sendMessage(ChatColor.DARK_AQUA + "You removed " + ChatColor.WHITE + amount + " " + item.name + ChatColor.DARK_AQUA + " from the shop");
        } else {
            player.sendMessage(ChatColor.DARK_AQUA + "You purchased " + ChatColor.WHITE + amount + " " + item.name + ChatColor.DARK_AQUA + " for " + ChatColor.WHITE + plugin.econManager.format(totalCost));
        }

        // log the transaction
        int stock = invItem.getStock();
        int startStock = stock + amount;
        if (shop.isUnlimitedStock()) {
            startStock = 0;
        }
        plugin.shopData.logItems(player.getName(), shop.getName(), "buy-item", item.name, amount, startStock, stock);

        givePlayerItem(item.toStack(), amount);
        plugin.shopData.saveShop(shop);        

        return true;
    }

    public boolean shopBuy() {
        Shop shop = null;

        // Get current shop
        if (sender instanceof Player) {
            // Get player & data
            Player player = (Player) sender;
            PlayerData pData = plugin.playerData.get(player.getName());

            // Get Current Shop
            UUID shopUuid = pData.getCurrentShop();
            if (shopUuid != null) {
                shop = plugin.shopData.getShop(shopUuid);
            }
            if (shop == null) {
                sender.sendMessage("You are not in a shop!");
                return true;
            }

            // Check Permissions
            if (!canUseCommand(CommandTypes.SELL)) {
                sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + "You don't have permission to use this command");
                return true;
            }

            // buy (player only command)
            Pattern pattern = Pattern.compile("(?i)buy$");
            Matcher matcher = pattern.matcher(command);
            if (matcher.find()) {
                ItemStack itemStack = player.getItemInHand();
                if (itemStack == null) {
                    return false;
                }
                ItemInfo item = Search.itemById(itemStack.getTypeId(), itemStack.getDurability());
                if(item == null) {
                    sender.sendMessage("Could not find an item.");
                    return true;
                }
                return shopBuy(shop, item, 0);
            }

            // buy all (player only command)
            matcher.reset();
            pattern = Pattern.compile("(?i)buy\\s+all$");
            matcher = pattern.matcher(command);
            if (matcher.find()) {
                ItemStack itemStack = player.getItemInHand();
                if (itemStack == null) {
                    sender.sendMessage("You must be holding an item, or specify an item.");
                    return true;
                }
                ItemInfo item = Search.itemById(itemStack.getTypeId(), itemStack.getDurability());
                if(item == null) {
                    sender.sendMessage("Could not find an item.");
                    return true;
                }
                int count;
                if(shop.isUnlimitedStock()) {
                    // get player avail space
                    count = countAvailableSpaceForItemInInventory(player.getInventory(), item);
                } else {
                    // use shop stock
                    count = shop.getItem(item.name).getStock();
                }

                return shopBuy(shop, item, count);
            }

            // buy int all
            matcher.reset();
            pattern = Pattern.compile("(?i)buy\\s+(\\d+)\\s+all");
            matcher = pattern.matcher(command);
            if (matcher.find()) {
                int id = Integer.parseInt(matcher.group(1));
                ItemInfo item = Search.itemById(id);
                if(item == null) {
                    sender.sendMessage("Could not find an item.");
                    return true;
                }
                int count;
                if(shop.isUnlimitedStock()) {
                    // get player avail space
                    count = countAvailableSpaceForItemInInventory(player.getInventory(), item);
                } else {
                    // use shop stock
                    count = shop.getItem(item.name).getStock();
                }
                if(count < 1) {
                    sender.sendMessage("You must buy at least one " + item.name + "!");
                    return true;
                }
                return shopBuy(shop, item, count);
            }

            // buy int:int all
            matcher.reset();
            pattern = Pattern.compile("(?i)buy\\s+(\\d+):(\\d+)\\s+all");
            matcher = pattern.matcher(command);
            if (matcher.find()) {
                int id = Integer.parseInt(matcher.group(1));
                short type = Short.parseShort(matcher.group(2));
                ItemInfo item = Search.itemById(id, type);
                if(item == null) {
                    sender.sendMessage("Could not find an item.");
                    return true;
                }
                int count;
                if(shop.isUnlimitedStock()) {
                    // get player avail space
                    count = countAvailableSpaceForItemInInventory(player.getInventory(), item);
                } else {
                    // use shop stock
                    count = shop.getItem(item.name).getStock();
                }
                if(count < 1) {
                    sender.sendMessage("You must buy at least one " + item.name + "!");
                    return true;
                }
                return shopBuy(shop, item, count);
            }

            // buy name, ... all
            matcher.reset();
            pattern = Pattern.compile("(?i)buy\\s+(.*)\\s+all");
            matcher = pattern.matcher(command);
            if (matcher.find()) {
                String itemName = matcher.group(1);
                ItemInfo item = Search.itemByName(itemName);
                if(item == null) {
                    sender.sendMessage("Could not find an item.");
                    return true;
                }
                int count;
                if(shop.isUnlimitedStock()) {
                    // get player avail space
                    count = countAvailableSpaceForItemInInventory(player.getInventory(), item);
                } else {
                    // use shop stock
                    count = shop.getItem(item.name).getStock();
                }
                if(count < 1) {
                    sender.sendMessage("You must buy at least one " + item.name + "!");
                    return true;
                }
                return shopBuy(shop, item, count);
            }

        } else {
            sender.sendMessage("Console is not implemented yet.");
            return true;
        }

        // Command matching

        // buy int
        Pattern pattern = Pattern.compile("(?i)buy\\s+(\\d+)");
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            ItemInfo item = Search.itemById(id);
            if(item == null) {
                sender.sendMessage("Could not find an item.");
                return true;
            }
            return shopBuy(shop, item, 0);
        }

        // buy int int
        matcher.reset();
        pattern = Pattern.compile("(?i)buy\\s+(\\d+)\\s+(\\d+)");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            int count = Integer.parseInt(matcher.group(2));
            ItemInfo item = Search.itemById(id);
            if(item == null) {
                sender.sendMessage("Could not find an item.");
                return true;
            }
            if(count < 1) {
                sender.sendMessage("You must buy at least one " + item.name + "!");
                return true;
            }
            return shopBuy(shop, item, count);
        }

        // buy int:int
        matcher.reset();
        pattern = Pattern.compile("(?i)buy\\s+(\\d+):(\\d+)");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            short type = Short.parseShort(matcher.group(2));
            ItemInfo item = Search.itemById(id, type);
            if(item == null) {
                sender.sendMessage("Could not find an item.");
                return true;
            }
            return shopBuy(shop, item, 0);
        }

        // buy int:int int
        matcher.reset();
        pattern = Pattern.compile("(?i)buy\\s+(\\d+):(\\d+)\\s+(\\d+)");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            short type = Short.parseShort(matcher.group(2));
            ItemInfo item = Search.itemById(id, type);
            int count = Integer.parseInt(matcher.group(3));
            if(item == null) {
                sender.sendMessage("Could not find an item.");
                return true;
            }
            if(count < 1) {
                sender.sendMessage("You must buy at least one " + item.name + "!");
                return true;
            }
            return shopBuy(shop, item, count);
        }

        // buy name, ... int
        matcher.reset();
        pattern = Pattern.compile("(?i)buy\\s+(.*)\\s+(\\d+)");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            String itemName = matcher.group(1);
            ItemInfo item = Search.itemByName(itemName);
            int count = Integer.parseInt(matcher.group(2));
            if(item == null) {
                sender.sendMessage("Could not find an item.");
                return true;
            }
            if(count < 1) {
                sender.sendMessage("You must buy at least one " + item.name + "!");
                return true;
            }
            return shopBuy(shop, item, count);
        }

        // buy name, ...
        matcher.reset();
        pattern = Pattern.compile("(?i)buy\\s+(.*)");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            String itemName = matcher.group(1);
            ItemInfo item = Search.itemByName(itemName);
            if(item == null) {
                sender.sendMessage("Could not find an item.");
                return true;
            }
            return shopBuy(shop, item, 0);
        }

        // Show sell help
        sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " buy [itemname] [number] " + ChatColor.DARK_AQUA + "- Buy an item.");
        return true;
    }

    public boolean shopSet() {
        // Check Permissions
        if (!canUseCommand(CommandTypes.SET)) {
            sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + "You don't have permission to use this command");
            return true;
        }

        log.info(String.format("[%s] Command issued: %s", plugin.pdfFile.getName(), command));

        // Parse Arguments
        if (command.matches("(?i)set\\s+sell.*")) {
            return shopSetSell();
        } else if (command.matches("(?i)set\\s+buy.*")) {
            return shopSetBuy();
        } else if (command.matches("(?i)set\\s+max.*")) {
            return shopSetMax();
        } else if (command.matches("(?i)set\\s+unlimited.*")) {
            return shopSetUnlimited();
        } else if (command.matches("(?i)set\\s+manager.*")) {
            return shopSetManager();
        } else if (command.matches("(?i)set\\s+minbalance.*")) {
            return shopSetMinBalance();
        } else if (command.matches("(?i)set\\s+owner.*")) {
            return shopSetOwner();
        } else if (command.matches("(?i)set\\s+name.*")) {
            return shopSetName();
        } else {
            return shopSetHelp();
        }
    }

    private boolean shopSetMinBalance() {
        Shop shop = null;
        boolean reset = false;

        // Get current shop
        if (sender instanceof Player) {
            // Get player & data
            Player player = (Player) sender;
            PlayerData pData = plugin.playerData.get(player.getName());

            // Get Current Shop
            UUID shopUuid = pData.getCurrentShop();
            if (shopUuid != null) {
                shop = plugin.shopData.getShop(shopUuid);
            }
            if (shop == null) {
                sender.sendMessage("You are not in a shop!");
                return true;
            }

            // Check if Player can Modify
            if (!shop.getOwner().equalsIgnoreCase(player.getName())) {
                sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + "You must be the shop owner to set this.");
                sender.sendMessage(ChatColor.DARK_AQUA + " The current shop owner is " + ChatColor.WHITE + shop.getOwner());
                return true;
            }
        } else {
            sender.sendMessage("Console is not implemented yet.");
            return true;
        }

        // set minbalance amount
        Pattern pattern = Pattern.compile("(?i)set\\s+minbalance\\s+(\\d+)");
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            double min = Double.parseDouble(matcher.group(1));
            shop.setMinBalance(min);
            // Save Shop
            plugin.shopData.saveShop(shop);

            sender.sendMessage(ChatColor.WHITE + shop.getName() + ChatColor.DARK_AQUA + " now has a minimum balance of "+ ChatColor.WHITE + plugin.econManager.format(min));
            return true;
        }

        sender.sendMessage(" " + "/" + commandLabel + " set minbalance [amount]");
        return true;
    }

    private boolean shopSetSell(Shop shop, ItemInfo item, double price, int size) {
        if (item == null) {
            sender.sendMessage("Item was not found.");
            return true;
        }

        // Check if Shop has item
        if (!shop.containsItem(item)) {
            // nicely message user
            sender.sendMessage(String.format("This shop does not carry %s!", item.name));
            return true;
        }

        // Warn about negative items
        if (price < 0) {
            sender.sendMessage("[WARNING] This shop will loose money with negative values!");
        }
        if (size < 0) {
            sender.sendMessage("[ERROR] Stacks cannot be negative!");
            return true;
        }

        // Set new values
        shop.setItemBuyAmount(item.name, size);
        shop.setItemBuyPrice(item.name, price);

        // Save Shop
        plugin.shopData.saveShop(shop);

        // Send Result
        sender.sendMessage(ChatColor.WHITE + item.name + ChatColor.DARK_AQUA + " now sells for "+ ChatColor.WHITE + plugin.econManager.format(price) + ChatColor.DARK_AQUA + " [" + ChatColor.WHITE + "Bundle: " + size + ChatColor.DARK_AQUA + "]");

        return true;
    }

    private boolean shopSetSell(Shop shop, ItemInfo item, double price) {
        if (item == null) {
            sender.sendMessage("Item was not found.");
            return true;
        }

        // Check if Shop has item
        if (!shop.containsItem(item)) {
            // nicely message user
            sender.sendMessage(String.format("This shop does not carry %s!", item.name));
            return true;
        }

        // Warn about negative items
        if (price < 0) {
            sender.sendMessage("[WARNING] This shop will loose money with negative values!");
        }

        // Set new values
        shop.setItemBuyPrice(item.name, price);

        // Save Shop
        plugin.shopData.saveShop(shop);

        // Send Result
        sender.sendMessage(ChatColor.WHITE + item.name + ChatColor.DARK_AQUA + " now sells for "+ ChatColor.WHITE + plugin.econManager.format(price));

        return true;
    }

    private boolean shopSetSell() {
        Shop shop = null;

        // Get current shop
        if (sender instanceof Player) {
            // Get player & data
            Player player = (Player) sender;
            PlayerData pData = plugin.playerData.get(player.getName());

            // Get Current Shop
            UUID shopUuid = pData.getCurrentShop();
            if (shopUuid != null) {
                shop = plugin.shopData.getShop(shopUuid);
            }
            if (shop == null) {
                sender.sendMessage("You are not in a shop!");
                return true;
            }

            // Check if Player can Modify
            if (!isShopController(shop)) {
                player.sendMessage(ChatColor.DARK_AQUA + "You must be the shop owner or a manager to set this.");
                player.sendMessage(ChatColor.DARK_AQUA + "The current shop owner is " + ChatColor.WHITE + shop.getOwner());
                return true;
            }
        } else {
            sender.sendMessage("Console is not implemented yet.");
            return true;
        }

        // Command matching

        // set buy int int int
        Pattern pattern = Pattern.compile("(?i)set\\s+sell\\s+(\\d+)\\s+("+DECIMAL_REGEX+")\\s+(\\d+)");
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            ItemInfo item = Search.itemById(id);
            double price = Double.parseDouble(matcher.group(2));
            int size = Integer.parseInt(matcher.group(7));
            return shopSetSell(shop, item, price, size);
        }

        // set buy int:int int int
        matcher.reset();
        pattern = Pattern.compile("(?i)set\\s+sell\\s+(\\d+):(\\d+)\\s+("+DECIMAL_REGEX+")\\s+(\\d+)");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            short type = Short.parseShort(matcher.group(2));
            ItemInfo item = Search.itemById(id, type);
            log.info(matcher.group(3));
            double price = Double.parseDouble(matcher.group(3));
            int size = Integer.parseInt(matcher.group(8));
            return shopSetSell(shop, item, price, size);
        }

        // set buy int int
        matcher.reset();
        pattern = Pattern.compile("(?i)set\\s+sell\\s+(\\d+)\\s+("+DECIMAL_REGEX+")");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            ItemInfo item = Search.itemById(id);
            double price = Double.parseDouble(matcher.group(2));
            return shopSetSell(shop, item, price);
        }

        // set buy int:int int
        matcher.reset();
        pattern = Pattern.compile("(?i)set\\s+sell\\s+(\\d+):(\\d+)\\s+("+DECIMAL_REGEX+")");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            short type = Short.parseShort(matcher.group(2));
            ItemInfo item = Search.itemById(id, type);
            double price = Double.parseDouble(matcher.group(3));
            return shopSetSell(shop, item, price);
        }

        // set buy (chars) int int
        matcher.reset();
        pattern = Pattern.compile("(?i)set\\s+sell\\s+(.*)\\s+("+DECIMAL_REGEX+")\\s+(\\d+)");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            String name = matcher.group(1);
            ItemInfo item = Search.itemByName(name);
            double price = Double.parseDouble(matcher.group(2));
            int size = Integer.parseInt(matcher.group(7));
            return shopSetSell(shop, item, price, size);
        }

        // set buy (chars) int
        matcher.reset();
        pattern = Pattern.compile("(?i)set\\s+sell\\s+(.*)\\s+("+DECIMAL_REGEX+")");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            String name = matcher.group(1);
            ItemInfo item = Search.itemByName(name);
            double price = Double.parseDouble(matcher.group(2));
            return shopSetSell(shop, item, price);
        }

        // show set buy usage
        sender.sendMessage("   " + "/" + commandLabel + " set sell [item name] [price] <bundle size>");
        return true;
    }

    private boolean shopSetBuy(Shop shop, ItemInfo item, double price, int size) {
        if (item == null) {
            sender.sendMessage("Item was not found.");
            return true;
        }

        // Check if Shop has item
        if (!shop.containsItem(item)) {
            // nicely message user
            sender.sendMessage(String.format("This shop does not carry %s!", item.name));
            return true;
        }

        // Warn about negative items
        if (price < 0) {
            sender.sendMessage("[WARNING] This shop will loose money with negative values!");
        }
        if (size < 0) {
            sender.sendMessage("[ERROR] Stacks cannot be negative!");
            return true;
        }

        // Set new values
        shop.setItemSellAmount(item.name, size);
        shop.setItemSellPrice(item.name, price);

        // Save Shop
        plugin.shopData.saveShop(shop);

        // Send Result
        sender.sendMessage(ChatColor.WHITE + item.name + ChatColor.DARK_AQUA + " now is purchased for "+ ChatColor.WHITE + plugin.econManager.format(price) + ChatColor.DARK_AQUA + " [" + ChatColor.WHITE + "Bundle: " + size + ChatColor.DARK_AQUA + "]");
        return true;
    }

    private boolean shopSetBuy(Shop shop, ItemInfo item, double price) {
        if (item == null) {
            sender.sendMessage("Item was not found.");
            return true;
        }

        // Check if Shop has item
        if (!shop.containsItem(item)) {
            // nicely message user
            sender.sendMessage(String.format("This shop does not carry %s!", item.name));
            return true;
        }

        // Warn about negative items
        if (price < 0) {
            sender.sendMessage("[WARNING] This shop will loose money with negative values!");
        }

        // Set new values
        shop.setItemSellPrice(item.name, price);

        // Save Shop
        plugin.shopData.saveShop(shop);

        // Send Result
        sender.sendMessage(ChatColor.WHITE + item.name + ChatColor.DARK_AQUA + " now is purchased for "+ ChatColor.WHITE + plugin.econManager.format(price));
        return true;
    }

    private boolean shopSetBuy() {
        Shop shop = null;

        // Get current shop
        if (sender instanceof Player) {
            // Get player & data
            Player player = (Player) sender;
            PlayerData pData = plugin.playerData.get(player.getName());

            // Get Current Shop
            UUID shopUuid = pData.getCurrentShop();
            if (shopUuid != null) {
                shop = plugin.shopData.getShop(shopUuid);
            }
            if (shop == null) {
                sender.sendMessage("You are not in a shop!");
                return true;
            }

            // Check if Player can Modify
            if (!isShopController(shop)) {
                player.sendMessage(ChatColor.DARK_AQUA + "You must be the shop owner or a manager to set this.");
                player.sendMessage(ChatColor.DARK_AQUA + "The current shop owner is " + ChatColor.WHITE + shop.getOwner());
                return true;
            }
        } else {
            sender.sendMessage("Console is not implemented yet.");
            return true;
        }

        // Command matching

        // set sell int int int
        Pattern pattern = Pattern.compile("(?i)set\\s+buy\\s+(\\d+)\\s+("+DECIMAL_REGEX+")\\s+(\\d+)");
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            ItemInfo item = Search.itemById(id);
            double price = Double.parseDouble(matcher.group(2));
            int size = Integer.parseInt(matcher.group(7));
            return shopSetBuy(shop, item, price, size);
        }

        // set sell int:int int int
        matcher.reset();
        pattern = Pattern.compile("(?i)set\\s+buy\\s+(\\d+):(\\d+)\\s+("+DECIMAL_REGEX+")\\s+(\\d+)");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            short type = Short.parseShort(matcher.group(2));
            ItemInfo item = Search.itemById(id, type);
            double price = Double.parseDouble(matcher.group(3));
            int size = Integer.parseInt(matcher.group(8));
            return shopSetBuy(shop, item, price, size);
        }

        // set sell int int
        matcher.reset();
        pattern = Pattern.compile("(?i)set\\s+buy\\s+(\\d+)\\s+("+DECIMAL_REGEX+")");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            ItemInfo item = Search.itemById(id);
            double price = Double.parseDouble(matcher.group(2));
            return shopSetBuy(shop, item, price);
        }

        // set sell int:int int
        matcher.reset();
        pattern = Pattern.compile("(?i)set\\s+buy\\s+(\\d+):(\\d+)\\s+("+DECIMAL_REGEX+")");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            short type = Short.parseShort(matcher.group(2));
            ItemInfo item = Search.itemById(id, type);
            double price = Double.parseDouble(matcher.group(3));
            return shopSetBuy(shop, item, price);
        }

        // set sell (chars) int int
        matcher.reset();
        pattern = Pattern.compile("(?i)set\\s+buy\\s+(.*)\\s+("+DECIMAL_REGEX+")\\s+(\\d+)");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            String name = matcher.group(1);
            ItemInfo item = Search.itemByName(name);
            double price = Double.parseDouble(matcher.group(2));
            int size = Integer.parseInt(matcher.group(7));
            return shopSetBuy(shop, item, price, size);
        }

        // set sell (chars) int
        matcher.reset();
        pattern = Pattern.compile("(?i)set\\s+buy\\s+(.*)\\s+("+DECIMAL_REGEX+")");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            String name = matcher.group(1);
            ItemInfo item = Search.itemByName(name);
            double price = Double.parseDouble(matcher.group(2));
            return shopSetBuy(shop, item, price);
        }

        // show set sell usage
        sender.sendMessage("   " + "/" + commandLabel + " set buy [item name] [price] <bundle size>");
        return true;
    }

    private boolean shopSetHelp() {
        // Display list of set commands & return
        sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + "The following set commands are available: ");
        sender.sendMessage("   " + "/" + commandLabel + " set buy [item name] [price] <bundle size>");
        sender.sendMessage("   " + "/" + commandLabel + " set sell [item name] [price] <bundle size>");
        sender.sendMessage("   " + "/" + commandLabel + " set max [item name] [max number]");
        sender.sendMessage("   " + "/" + commandLabel + " set manager +[playername] -[playername2]");
        sender.sendMessage("   " + "/" + commandLabel + " set minbalance [amount]");
        sender.sendMessage("   " + "/" + commandLabel + " set name [shop name]");
        sender.sendMessage("   " + "/" + commandLabel + " set owner [player name]");
        if (canUseCommand(CommandTypes.ADMIN)) {
            sender.sendMessage("   " + "/" + commandLabel + " set unlimited money");
            sender.sendMessage("   " + "/" + commandLabel + " set unlimited stock");
        }
        return true;
    }

    private boolean shopSetName() {
        Shop shop = null;

        // Get current shop
        if(sender instanceof Player) {
            // Get player & data
            Player player = (Player) sender;
            PlayerData pData = plugin.playerData.get(player.getName());

            // Get Current Shop
            UUID shopUuid = pData.getCurrentShop();
            if(shopUuid != null) {
                shop = plugin.shopData.getShop(shopUuid);
            }
            if(shop == null) {
                sender.sendMessage("You are not in a shop!");
                return true;                
            }

            // Check if Player can Modify  
            if(!canModifyShop(shop)) {
                sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + "You must be the shop owner to set this.");
                sender.sendMessage(ChatColor.DARK_AQUA + "  The current shop owner is " + ChatColor.WHITE + shop.getOwner());                
                return true;
            }
        } else {
            sender.sendMessage("Console is not implemented yet.");
            return true;            
        }

        Pattern pattern = Pattern.compile("(?i)set\\s+name\\s+(.*)");
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            String name = matcher.group(1).trim();
            shop.setName(name);
            plugin.shopData.saveShop(shop);
            notifyPlayers(shop, new String[] { LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + "Shop name is now " + ChatColor.WHITE + shop.getName() } );
            return true;
        }

        sender.sendMessage("   " + "/" + commandLabel + " set name [shop name]");
        return true;
    }

    private boolean notifyPlayers(Shop shop, String[] messages) {
        Iterator<PlayerData> it = plugin.playerData.values().iterator();
        while(it.hasNext()) {
            PlayerData p = it.next();
            if(p.shopList.contains(shop.getUuid())) {
                Player thisPlayer = plugin.getServer().getPlayer(p.playerName);
                for(String message : messages) {
                    thisPlayer.sendMessage(message);
                }
            }
        }
        return true;
    }

    private boolean shopSetOwner() {
        Shop shop = null;
        boolean reset = false;

        // Get current shop
        if (sender instanceof Player) {
            // Get player & data
            Player player = (Player) sender;
            PlayerData pData = plugin.playerData.get(player.getName());

            // Get Current Shop
            UUID shopUuid = pData.getCurrentShop();
            if (shopUuid != null) {
                shop = plugin.shopData.getShop(shopUuid);
            }
            if (shop == null) {
                sender.sendMessage("You are not in a shop!");
                return true;
            }

            // Check if Player can Modify
            if (!canUseCommand(CommandTypes.ADMIN) && !shop.getOwner().equalsIgnoreCase(player.getName())) {
                sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + "You must be the shop owner to set this.");
                sender.sendMessage(ChatColor.DARK_AQUA + "  The current shop owner is " + ChatColor.WHITE + shop.getOwner());
                return true;
            }

            if (!canUseCommand(CommandTypes.SET_OWNER) && !canUseCommand(CommandTypes.ADMIN)) {
                sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + "You don't have permission to use this command");
                return true;
            }

            if(!canUseCommand(CommandTypes.ADMIN)) {
                sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + shop.getName() + " is no longer buying items.");
                reset = true;
            }
        } else {
            sender.sendMessage("Console is not implemented yet.");
            return true;
        }

        // set owner name
        Pattern pattern = Pattern.compile("(?i)set\\s+owner\\s+(.*)");
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            String name = matcher.group(1);
            if (!canUseCommand(CommandTypes.SET_OWNER)) {
                sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + "You do not have permission to do this.");
                return true;
            }  else if ( !canCreateShop(name) ) {
                sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + "that player already has the maximum number of shops!");
                return true;
            } else {
                shop.setOwner(name);

                // Save Shop
                plugin.shopData.saveShop(shop);

                // Reset buy prices (0)
                if(reset) {
                    Iterator<InventoryItem> it = shop.getItems().iterator();
                    while(it.hasNext()) {
                        InventoryItem item = it.next();
                        item.setSellPrice(0);
                    }
                }

                notifyPlayers(shop, new String[] { LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + shop.getName() + " is now under new management!  The new owner is " + ChatColor.WHITE + shop.getOwner() } );
                return true;
            }
        }

        sender.sendMessage("   " + "/" + commandLabel + " set owner [player name]");
        return true;
    }

    private boolean shopSetManager() {
        Shop shop = null;

        // Get current shop
        if (sender instanceof Player) {
            // Get player & data
            Player player = (Player) sender;
            PlayerData pData = plugin.playerData.get(player.getName());

            // Get Current Shop
            UUID shopUuid = pData.getCurrentShop();
            if (shopUuid != null) {
                shop = plugin.shopData.getShop(shopUuid);
            }
            if (shop == null) {
                sender.sendMessage("You are not in a shop!");
                return true;
            }

            // Check if Player can Modify
            if (!shop.getOwner().equalsIgnoreCase(player.getName())) {
                player.sendMessage(ChatColor.DARK_AQUA + "You must be the shop owner to set this.");
                player.sendMessage(ChatColor.DARK_AQUA + "The current shop owner is " + ChatColor.WHITE + shop.getOwner());
                return true;
            }
        } else {
            sender.sendMessage("Console is not implemented yet.");
            return true;
        }

        // set manager +name -name ...
        Pattern pattern = Pattern.compile("(?i)set\\s+manager\\s+(.*)");
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            String names = matcher.group(1);
            String[] args = names.split(" ");

            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg.matches("\\+.*")) {
                    // add manager
                    shop.addManager(arg.replaceFirst("\\+", ""));
                } else if (arg.matches("\\-.*")) {
                    // remove manager
                    shop.removeManager(arg.replaceFirst("\\-", ""));
                } 
            }

            // Save Shop
            plugin.shopData.saveShop(shop);

            notifyPlayers(shop, new String[] { ChatColor.DARK_AQUA + "The shop managers have been updated. The current managers are:", Search.join(shop.getManagers(), ", ") } );
            return true;            
        }

        // show set manager usage
        sender.sendMessage("   " + "/" + commandLabel + " set manager +[playername] -[playername2]");
        return true;
    }

    private boolean shopSetUnlimited() {
        Shop shop = null;

        // Get current shop
        if (sender instanceof Player) {
            // Get player & data
            Player player = (Player) sender;
            PlayerData pData = plugin.playerData.get(player.getName());

            // Get Current Shop
            UUID shopUuid = pData.getCurrentShop();
            if (shopUuid != null) {
                shop = plugin.shopData.getShop(shopUuid);
            }
            if (shop == null) {
                sender.sendMessage("You are not in a shop!");
                return true;
            }

            // Check Permissions
            if (!canUseCommand(CommandTypes.ADMIN)) {
                player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + "You must be a shop admin to do this.");
                return true;
            }            
        } else {
            sender.sendMessage("Console is not implemented yet.");
            return true;
        }

        // Command matching

        // shop set max int int
        Pattern pattern = Pattern.compile("(?i)set\\s+max\\s+(\\d+)\\s+(\\d+)");
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            ItemInfo item = Search.itemById(id);
            int max = Integer.parseInt(matcher.group(2));
            return shopSetMax(shop, item, max);
        }

        // shop set unlimited money
        matcher.reset();
        pattern = Pattern.compile("(?i)set\\s+unlimited\\s+money");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            shop.setUnlimitedMoney(!shop.isUnlimitedMoney());
            sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + "Unlimited money was set to " + ChatColor.WHITE + shop.isUnlimitedMoney());
            plugin.shopData.saveShop(shop);
            return true;
        }

        // shop set unlimited stock
        matcher.reset();
        pattern = Pattern.compile("(?i)set\\s+unlimited\\s+stock");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            shop.setUnlimitedStock(!shop.isUnlimitedStock());
            sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + "Unlimited stock was set to " + ChatColor.WHITE + shop.isUnlimitedStock());
            plugin.shopData.saveShop(shop);
            return true;
        }

        // show set buy usage
        sender.sendMessage("   " + "/" + commandLabel + " set unlimited money");
        sender.sendMessage("   " + "/" + commandLabel + " set unlimited stock");
        return true;
    }

    private boolean shopSetMax(Shop shop, ItemInfo item, int max) {
        if (item == null) {
            sender.sendMessage("Item was not found.");
            return true;
        }

        // Check if Shop has item
        if (!shop.containsItem(item)) {
            // nicely message user
            sender.sendMessage(String.format("This shop does not carry %s!", item.name));
            return true;
        }

        // Check negative values
        if (max < 0) {
            sender.sendMessage("Only positive values allowed");
            return true;
        }

        // Set new values
        shop.setItemMaxStock(item.name, max);

        // Save Shop
        plugin.shopData.saveShop(shop);

        // Send Message
        sender.sendMessage(item.name + " maximum stock is now " + max);

        return true;
    }

    private boolean shopSetMax() {
        Shop shop = null;

        // Get current shop
        if (sender instanceof Player) {
            // Get player & data
            Player player = (Player) sender;
            PlayerData pData = plugin.playerData.get(player.getName());

            // Get Current Shop
            UUID shopUuid = pData.getCurrentShop();
            if (shopUuid != null) {
                shop = plugin.shopData.getShop(shopUuid);
            }
            if (shop == null) {
                sender.sendMessage("You are not in a shop!");
                return true;
            }

            // Check if Player can Modify
            if (!isShopController(shop)) {
                player.sendMessage(ChatColor.DARK_AQUA + "You must be the shop owner or a manager to set this.");
                player.sendMessage(ChatColor.DARK_AQUA + "The current shop owner is " + ChatColor.WHITE + shop.getOwner());
                return true;
            }
        } else {
            sender.sendMessage("Console is not implemented yet.");
            return true;
        }

        // Command matching

        // shop set max int int
        Pattern pattern = Pattern.compile("(?i)set\\s+max\\s+(\\d+)\\s+(\\d+)");
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            ItemInfo item = Search.itemById(id);
            int max = Integer.parseInt(matcher.group(2));
            return shopSetMax(shop, item, max);
        }

        // shop set max int:int int
        matcher.reset();
        pattern = Pattern.compile("(?i)set\\s+max\\s+(\\d+):(\\d+)\\s+(\\d+)");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            short type = Short.parseShort(matcher.group(2));
            ItemInfo item = Search.itemById(id, type);
            int max = Integer.parseInt(matcher.group(3));
            return shopSetMax(shop, item, max);
        }

        // shop set max chars int
        matcher.reset();
        pattern = Pattern.compile("(?i)set\\s+max\\s+(.*)\\s+(\\d+)");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            String name = matcher.group(1);
            ItemInfo item = Search.itemByName(name);
            int max = Integer.parseInt(matcher.group(2));
            return shopSetMax(shop, item, max);
        }

        // show set buy usage
        sender.sendMessage("   " + "/" + commandLabel + " set max [item name] [max number]");
        return true;
    }

    private boolean shopRemove(Shop shop, ItemInfo item) {
        if (item == null) {
            sender.sendMessage(ChatColor.DARK_AQUA + "Item not found.");
            return false;
        }

        if(!shop.containsItem(item)) {
            sender.sendMessage(ChatColor.DARK_AQUA + "The shop is not selling " + ChatColor.WHITE + item.name);
            return true;
        }

        sender.sendMessage(ChatColor.WHITE + item.name + ChatColor.DARK_AQUA + " removed from the shop. ");
        if (!shop.isUnlimitedStock()) {
            int amount = shop.getItem(item.name).getStock();

            if (sender instanceof Player) {
                Player player = (Player) sender;
                // log the transaction
                plugin.shopData.logItems(player.getName(), shop.getName(), "remove-item", item.name, amount, amount, 0);

                givePlayerItem(item.toStack(), amount);
                player.sendMessage("" + ChatColor.WHITE + amount + ChatColor.DARK_AQUA + " have been returned to your inventory");
            }
        }

        shop.removeItem(item.name);
        plugin.shopData.saveShop(shop);        

        return true;
    }

    /**
     * Processes remove command. Removes item from shop and returns stock to
     * player.
     * 
     * @param sender
     * @param args
     * @return true - if command succeeds false otherwise
     */
    public boolean shopRemove() {
        Shop shop = null;

        // Get current shop
        if (sender instanceof Player) {
            // Get player & data
            Player player = (Player) sender;
            PlayerData pData = plugin.playerData.get(player.getName());

            // Get Current Shop
            UUID shopUuid = pData.getCurrentShop();
            if (shopUuid != null) {
                shop = plugin.shopData.getShop(shopUuid);
            }
            if (shop == null) {
                sender.sendMessage("You are not in a shop!");
                return false;
            }

            // Check Permissions
            if (!canUseCommand(CommandTypes.REMOVE)) {
                sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + "You don't have permission to use this command");
                return false;
            }            

            // Check if Player can Modify
            if (!isShopController(shop)) {
                player.sendMessage(ChatColor.DARK_AQUA + "You must be the shop owner or a manager to set this.");
                player.sendMessage(ChatColor.DARK_AQUA + "The current shop owner is " + ChatColor.WHITE + shop.getOwner());
                return true;
            }

            // remove (player only command)
            Pattern pattern = Pattern.compile("(?i)remove$");
            Matcher matcher = pattern.matcher(command);
            if (matcher.find()) {
                ItemStack itemStack = player.getItemInHand();
                if (itemStack == null) {
                    return false;
                }
                ItemInfo item = Search.itemById(itemStack.getTypeId(), itemStack.getDurability());
                if(item == null) {
                    sender.sendMessage("Could not find an item.");
                    return false;
                }
                return shopRemove(shop, item);
            }

        } else {
            sender.sendMessage("Console is not implemented yet.");
            return false;
        }

        // Command matching

        // remove int
        Pattern pattern = Pattern.compile("(?i)remove\\s+(\\d+)");
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            ItemInfo item = Search.itemById(id);
            return shopRemove(shop, item);
        }

        // remove int:int
        matcher.reset();
        pattern = Pattern.compile("(?i)remove\\s+(\\d+):(\\d+)");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            short type = Short.parseShort(matcher.group(2));
            ItemInfo item = Search.itemById(id, type);
            return shopRemove(shop, item);
        }

        // remove name
        matcher.reset();
        pattern = Pattern.compile("(?i)remove\\s+(.*)");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            String itemName = matcher.group(1);
            ItemInfo item = Search.itemByName(itemName);
            return shopRemove(shop, item);
        }        


        // Show usage
        sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " remove [itemname]" + ChatColor.DARK_AQUA + " - Stop selling item in shop.");
        return true;
    }

    /**
     * Destroys current shop. Deleting file and removing from tree.
     * 
     * @param sender
     * @param args
     * @return true - if command succeeds false otherwise
     */
    public boolean shopDestroy() {
        if (!(sender instanceof Player) || !canUseCommand(CommandTypes.DESTROY)) {
            sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + "You don't have permission to use this command");
            return false;
        }

        /*
         * Available formats: /lshop destroy
         */

        Player player = (Player) sender;
        String playerName = player.getName();

        // get the shop the player is currently in
        if (plugin.playerData.get(playerName).shopList.size() == 1) {
            UUID shopUuid = plugin.playerData.get(playerName).shopList.get(0);
            Shop shop = plugin.shopData.getShop(shopUuid);

            if (!shop.getOwner().equalsIgnoreCase(player.getName()) && !canUseCommand(CommandTypes.ADMIN)) {
                player.sendMessage(ChatColor.DARK_AQUA + "You must be the shop owner to destroy it.");
                return false;
            }

            Iterator<PlayerData> it = plugin.playerData.values().iterator();
            while(it.hasNext()) {
                PlayerData p = it.next();
                if(p.shopList.contains(shop.getUuid())) {
                    Player thisPlayer = plugin.getServer().getPlayer(p.playerName);
                    p.removePlayerFromShop(thisPlayer, shop.getUuid());
                    thisPlayer.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.WHITE + shop.getName() + ChatColor.DARK_AQUA + " has been destroyed");
                }
            }

            Collection<InventoryItem> shopItems = shop.getItems();

            if(plugin.shopData.deleteShop(shop)) {
                // return items to player (if a player)
                if(sender instanceof Player) {
                    for(InventoryItem item : shopItems) {
                        givePlayerItem(item.getInfo().toStack(), item.getStock());
                    }
                }
            } else {
                // error message :(
                sender.sendMessage("Could not return shop inventory!");
            }

        } else {
            player.sendMessage(ChatColor.DARK_AQUA + "You must be inside a shop to use /" + commandLabel + " destroy");
        }

        return true;
    }

    public int countAvailableSpaceForItemInInventory(PlayerInventory inventory, ItemInfo item) {
        int count = 0;
        for (ItemStack thisSlot : inventory.getContents()) {
            if (thisSlot == null || thisSlot.getType() == Material.AIR) {
                count += 64;
                continue;
            }
            if (thisSlot.getTypeId() == item.typeId && thisSlot.getDurability() == item.subTypeId) {
                count += 64 - thisSlot.getAmount();
            }
        }

        return count;
    }

    public int countItemsInInventory(PlayerInventory inventory, ItemStack item) {
        int totalAmount = 0;
        boolean isDurable = LocalShops.itemList.isDurable(item);

        for (Integer i : inventory.all(item.getType()).keySet()) {
            ItemStack thisStack = inventory.getItem(i);
            if (isDurable) {
                int damage = calcDurabilityPercentage(thisStack);
                if (damage > Config.ITEM_MAX_DAMAGE && Config.ITEM_MAX_DAMAGE != 0)
                    continue;
            } else {
                if (thisStack.getDurability() != item.getDurability())
                    continue;
            }
            totalAmount += thisStack.getAmount();
        }

        return totalAmount;
    }

    private int removeItemsFromInventory(PlayerInventory inventory,
            ItemStack item, int amount) {

        boolean isDurable = LocalShops.itemList.isDurable(item);

        // remove number of items from player adding stock
        for (int i : inventory.all(item.getType()).keySet()) {
            if (amount == 0)
                continue;
            ItemStack thisStack = inventory.getItem(i);
            if (isDurable) {
                int damage = calcDurabilityPercentage(thisStack);
                if (damage > Config.ITEM_MAX_DAMAGE && Config.ITEM_MAX_DAMAGE != 0)
                    continue;
            } else {
                if (thisStack.getDurability() != item.getDurability())
                    continue;
            }

            int foundAmount = thisStack.getAmount();
            if (amount >= foundAmount) {
                amount -= foundAmount;
                inventory.setItem(i, null);
            } else {
                thisStack.setAmount(foundAmount - amount);
                inventory.setItem(i, thisStack);
                amount = 0;
            }
        }

        return amount;

    }

    private static int calcDurabilityPercentage(ItemStack item) {

        // calc durability prcnt
        short damage;
        if (item.getType() == Material.IRON_SWORD) {
            damage = (short) ((double) item.getDurability() / 250 * 100);
        } else {
            damage = (short) ((double) item.getDurability() / (double) item.getType().getMaxDurability() * 100);
        }

        return damage;
    }

    private void givePlayerItem(ItemStack item, int amount) {
        Player player = (Player) sender;

        int maxStackSize = 64;

        // fill all the existing stacks first
        for (int i : player.getInventory().all(item.getType()).keySet()) {
            if (amount == 0)
                continue;
            ItemStack thisStack = player.getInventory().getItem(i);
            if (thisStack.getType().equals(item.getType()) && thisStack.getDurability() == item.getDurability()) {
                if (thisStack.getAmount() < maxStackSize) {
                    int remainder = maxStackSize - thisStack.getAmount();
                    if (remainder <= amount) {
                        amount -= remainder;
                        thisStack.setAmount(maxStackSize);
                    } else {
                        thisStack.setAmount(maxStackSize - remainder + amount);
                        amount = 0;
                    }
                }
            }

        }

        for (int i = 0; i < 36; i++) {
            ItemStack thisSlot = player.getInventory().getItem(i);
            if (thisSlot == null || thisSlot.getType() == Material.AIR) {
                if (amount == 0)
                    continue;
                if (amount >= maxStackSize) {
                    item.setAmount(maxStackSize);
                    player.getInventory().setItem(i, item);
                    amount -= maxStackSize;
                } else {
                    item.setAmount(amount);
                    player.getInventory().setItem(i, item);
                    amount = 0;
                }
            }
        }

        while (amount > 0) {
            if (amount >= maxStackSize) {
                item.setAmount(maxStackSize);
                amount -= maxStackSize;
            } else {
                item.setAmount(amount - maxStackSize);
                amount = 0;
            }
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }

    }

    private boolean canModifyShop(Shop shop) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            // If owner, true
            if(shop.getOwner().equals(player.getName())) {
                return true;
            }
            // If manager, true
            if(shop.getManagers().contains(player.getName())) {
                return true;
            }
            // If admin, true
            if(canUseCommand(CommandTypes.ADMIN)) {
                return true;
            }
            return false;
        } else {
            // Console, true
            return true;
        }
    }

    private boolean canCreateShop(String playerName) {
        if (canUseCommand(CommandTypes.ADMIN)) {
            return true;
        } else if (( plugin.shopData.numOwnedShops(playerName) < Config.PLAYER_MAX_SHOPS || Config.PLAYER_MAX_SHOPS < 0) && canUseCommand(CommandTypes.CREATE)) {
            return true;
        }

        return false;
    }
}

