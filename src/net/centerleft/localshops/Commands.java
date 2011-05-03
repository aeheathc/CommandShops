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
    private String[] args = null;
    private String command = null;

    // Command Types Enum
    private static enum CommandTypes {
        ADMIN(0, new String[] { "localshops.admin" }),
        ADD_ITEM(1, new String[] { "localshops.manage" }),
        BUY_ITEM(2, new String[] { "localshops.buysell" }),
        CREATE_SHOP(3, new String[] { "localshops.create" }),
        CREATE_SHOP_FREE(4, new String[] { "localshops.create.free" }),
        DESTROY_SHOP(5, new String[] { "localshops.destroy" }),
        HELP(6, new String[] {}),
        INVENTORY(7, new String[] { "localshops.buysell" }),
        MOVE_SHOP(8, new String[] { "localshops.move" }),
        MOVE_SHOP_FREE(9, new String[] { "localshops.move.free" }),
        RELOAD_PLUGIN(10, new String[] { "localshops.reload" }),
        REMOVE_ITEM(11, new String[] { "localshops.manage" }),
        SEARCH_ITEM(12, new String[] {}),
        SELECT_CUBOID(13, new String[] { "localshops.create" }),
        SELL_ITEM(14, new String[] { "localshops.buysell" }),
        SET_OWNER(15, new String[] { "localshops.manage.owner" }),
        SET(16, new String[] { "localshops.manage" });

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

    public Commands(LocalShops plugin, String commandLabel, CommandSender sender, String[] args) {
        this.plugin = plugin;
        this.commandLabel = commandLabel;
        this.sender = sender;
        this.args = args;
        this.command = Search.join(args, " ").trim();
    }

    public boolean shopDebug() {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Shop shop = null;

            // Get current shop
            UUID shopUuid = plugin.playerData.get(player.getName()).getCurrentShop();
            if (shopUuid != null) {
                shop = plugin.shopData.getShop(shopUuid);
            }
            if (shop == null) {
                return false;
            }

            shop.log();
        } else {
            // TODO: implement
        }

        return false;
    }

    public boolean shopSearch() {
        if (args.length < 2) {
            return false;
        }

        ArrayList<String> searchTerms = new ArrayList<String>();
        for (int i = 1; i < args.length; i++) {
            searchTerms.add(args[i].toLowerCase());
        }

        ItemInfo found = Search.itemByName(searchTerms);
        if (found == null) {
            sender.sendMessage("Item was not found.");
        } else {
            sender.sendMessage(found.toString());
        }
        return true;
    }

    public boolean shopSelect() {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.AQUA + "Only players can interactively select coordinates.");
            return false;
        }

        if (canUseCommand(CommandTypes.SELECT_CUBOID)) {

            Player player = (Player) sender;

            String playerName = player.getName();
            if (!plugin.playerData.containsKey(playerName)) {
                plugin.playerData.put(playerName, new PlayerData(plugin, playerName));
            }
            plugin.playerData.get(playerName).isSelecting = !plugin.playerData.get(playerName).isSelecting;

            if (plugin.playerData.get(playerName).isSelecting) {
                sender.sendMessage(ChatColor.AQUA + "Left click to select the first corner for a shop.");
                sender.sendMessage(ChatColor.AQUA + "Right click to select the second corner for the shop.");
            } else {
                sender.sendMessage(ChatColor.AQUA + "Selection disabled");
                plugin.playerData.put(playerName, new PlayerData(plugin, playerName));
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean shopCreate() {
        log.info("shopCreate");
        String creator = null;
        String world = null;
        long[] xyzA = new long[3];
        long[] xyzB = new long[3];

        // Get current shop
        if (sender instanceof Player) {
            Player player = (Player) sender;
            PlayerData pData = plugin.playerData.get(player.getName());
            
            // Check Permissions
            if (!canUseCommand(CommandTypes.CREATE_SHOP)) {
                sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "You don't have permission to use this command");
                return false;
            }
            
            creator = player.getName();
            world = player.getWorld().getName();
            
            // If player is select, use their selection
            if (pData.isSelecting) {
                if (!pData.checkSize()) {
                    String size = plugin.shopData.maxWidth + "x" + plugin.shopData.maxHeight + "x" + plugin.shopData.maxWidth;
                    player.sendMessage(ChatColor.AQUA + "Problem with selection. Max size is " + ChatColor.WHITE + size);
                    return false;
                }

                xyzA = pData.getPositionA();
                xyzB = pData.getPositionB();

                if (xyzA == null || xyzB == null) {
                    player.sendMessage(ChatColor.AQUA + "Problem with selection.");
                    return false;
                }
            } else {
                // get current position
                Location loc = player.getLocation();
                long x = loc.getBlockX();
                long y = loc.getBlockY();
                long z = loc.getBlockZ();
                
                if (plugin.shopData.shopSize % 2 == 0) {
                    xyzA[0] = x - (plugin.shopData.shopSize / 2);
                    xyzB[0] = x + (plugin.shopData.shopSize / 2);
                    xyzA[2] = z - (plugin.shopData.shopSize / 2);
                    xyzB[2] = z + (plugin.shopData.shopSize / 2);
                } else {
                    xyzA[0] = x - (plugin.shopData.shopSize / 2) + 1;
                    xyzB[0] = x + (plugin.shopData.shopSize / 2);
                    xyzA[2] = z - (plugin.shopData.shopSize / 2) + 1;
                    xyzB[2] = z + (plugin.shopData.shopSize / 2);
                }

                xyzA[1] = y - 1;
                xyzB[1] = y + plugin.shopData.shopHeight - 1;
            }
            
            if(!shopPositionOk(xyzA, xyzB, world)) {
                sender.sendMessage("A shop already exists here!");
                return false;
            }
            
            if (plugin.shopData.chargeForShop) {
                if (!canUseCommand(CommandTypes.CREATE_SHOP_FREE)) {
                    if (!plugin.playerData.get(player.getName()).chargePlayer(player.getName(), plugin.shopData.shopCost)) {
                        sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "You need " + plugin.shopData.shopCost + " " + plugin.shopData.currencyName + " to create a shop.");
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

                // write the file
                if (plugin.shopData.saveShop(shop)) {
                    sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.WHITE + shop.getName() + ChatColor.AQUA + " was created successfully.");
                    return true;
                } else {
                    sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "There was an error, could not create shop.");
                    return false;
                }
        }
        
        // Show usage
        sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " create [ShopName]" + ChatColor.AQUA + " - Create a shop at your location.");
        return true;
    }

    public boolean shopMove() {
        // TODO Change this so that non players can create shops as long as they
        // send x, y, z coords
        if (canUseCommand(CommandTypes.MOVE_SHOP) && args.length == 2 && (sender instanceof Player)) {
            // command format /lshop move ShopName
            Player player = (Player) sender;
            Location location = player.getLocation();
            Shop thisShop = null;

            long[] xyzAold = new long[3];
            long[] xyzBold = new long[3];

            // check to see if that shop name exists and has access
            boolean foundShop = false;
            Collection<Shop> shops = plugin.shopData.getAllShops();
            for (Shop shop : shops) {
                if (shop.getName().equalsIgnoreCase(args[1])) {
                    thisShop = shop;
                    foundShop = true;
                    break;
                }
            }

            if (!foundShop) {
                player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "Could not find shop: " + ChatColor.WHITE + args[1]);
                return false;
            }

            if (!thisShop.getOwner().equalsIgnoreCase(player.getName())) {
                player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "You must be the shop owner to move this shop.");
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
                /**
                 * if (!plugin.playerData.get(player.getName()).sizeOkay) { if
                 * (!canUseCommand(player, "admin".split(""))) { String size =
                 * "" + plugin.shopData.maxWidth + "x" +
                 * plugin.shopData.maxHeight + "x" + plugin.shopData.maxWidth;
                 * player.sendMessage(ChatColor.AQUA +
                 * "Problem with selection. Max size is " + ChatColor.WHITE +
                 * size); return false; } }
                 */

                // Check if size is ok
                if (!plugin.playerData.get(player.getName()).checkSize()) {
                    String size = plugin.shopData.maxWidth + "x" + plugin.shopData.maxHeight + "x" + plugin.shopData.maxWidth;
                    player.sendMessage(ChatColor.AQUA + "Problem with selection. Max size is " + ChatColor.WHITE + size);
                    return false;
                }

                // if a custom size had been set, use that
                PlayerData data = plugin.playerData.get(player.getName());
                xyzA = data.getPositionA().clone();
                xyzB = data.getPositionB().clone();

                if (xyzA == null || xyzB == null) {
                    player.sendMessage(ChatColor.AQUA + "Problem with selection.");
                    return false;
                }
            } else {
                // otherwise calculate the shop from the player's location
                if (plugin.shopData.shopSize % 2 == 0) {
                    xyzA[0] = x - (plugin.shopData.shopSize / 2);
                    xyzB[0] = x + (plugin.shopData.shopSize / 2);
                    xyzA[2] = z - (plugin.shopData.shopSize / 2);
                    xyzB[2] = z + (plugin.shopData.shopSize / 2);
                } else {
                    xyzA[0] = x - (plugin.shopData.shopSize / 2) + 1;
                    xyzB[0] = x + (plugin.shopData.shopSize / 2);
                    xyzA[2] = z - (plugin.shopData.shopSize / 2) + 1;
                    xyzB[2] = z + (plugin.shopData.shopSize / 2);
                }

                xyzA[1] = y - 1;
                xyzB[1] = y + plugin.shopData.shopHeight - 1;

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

                if (plugin.shopData.chargeForMove) {
                    if (!canUseCommand(CommandTypes.MOVE_SHOP_FREE)) {
                        if (!plugin.playerData.get(player.getName()).chargePlayer(player.getName(), plugin.shopData.shopCost)) {
                            // insert the old cuboid back into the world
                            tempShopCuboid = new PrimitiveCuboid(xyzAold, xyzBold);
                            tempShopCuboid.uuid = thisShop.getUuid();
                            tempShopCuboid.world = thisShop.getWorld();
                            LocalShops.cuboidTree.insert(tempShopCuboid);

                            player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "You need " + plugin.shopData.moveCost + " " + plugin.shopData.currencyName + " to move a shop.");
                            return false;
                        }
                    }
                }

                // insert the shop into the world
                LocalShops.cuboidTree.insert(tempShopCuboid);
                thisShop.setWorld(player.getWorld().getName());
                thisShop.setLocations(new ShopLocation(xyzA), new ShopLocation(xyzB));
                plugin.shopData.addShop(thisShop);

                plugin.playerData.put(player.getName(), new PlayerData(plugin, player.getName()));

                // write the file
                if (plugin.shopData.saveShop(thisShop)) {
                    player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.WHITE + shopName + ChatColor.AQUA + " was moved successfully.");
                    return true;
                } else {
                    player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "There was an error, could not move shop.");
                    return false;
                }
            } else {
                // insert the old cuboid back into the world
                PrimitiveCuboid tempShopCuboid = new PrimitiveCuboid(xyzAold, xyzBold);
                tempShopCuboid.uuid = thisShop.getUuid();
                tempShopCuboid.world = thisShop.getWorld();
                LocalShops.cuboidTree.insert(tempShopCuboid);
            }
        }
        if (args.length != 2) {
            sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "The command format is " + ChatColor.WHITE + "/" + commandLabel + " move [ShopName]");
        }
        if (!canUseCommand(CommandTypes.MOVE_SHOP)) {
            sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "You don't have permission to use this command");
        }
        return false;
    }

    public boolean canUseCommand(CommandTypes type) {
        PermissionHandler pm = plugin.pluginListener.gmPermissionCheck;

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (plugin.pluginListener.usePermissions) {
                // Using permissions, check them
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
        sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "Here are the available commands [required] <optional>");

        if (canUseCommand(CommandTypes.ADD_ITEM)) {
            sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " add" + ChatColor.AQUA + " - Add the item that you are holding to the shop.");
        }
        if (canUseCommand(CommandTypes.BUY_ITEM)) {
            sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " buy [itemname] [number] " + ChatColor.AQUA + "- Buy this item.");
        }
        if (canUseCommand(CommandTypes.CREATE_SHOP)) {
            sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " create [ShopName]" + ChatColor.AQUA + " - Create a shop at your location.");
        }
        if (canUseCommand(CommandTypes.DESTROY_SHOP)) {
            sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " destroy" + ChatColor.AQUA + " - Destroy the shop you're in.");
        }
        if (canUseCommand(CommandTypes.INVENTORY)) {
            sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " inventory <buy|sell> " + ChatColor.AQUA + "- List the shop's inventory.");
        }
        if (canUseCommand(CommandTypes.MOVE_SHOP)) {
            sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " move [ShopName]" + ChatColor.AQUA + " - Move a shop to your location.");
        }
        sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " search" + ChatColor.AQUA + " - Search for an item name.");
        if (canUseCommand(CommandTypes.SELECT_CUBOID)) {
            sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " select" + ChatColor.AQUA + " - Select two corners for custom shop size.");
        }
        if (canUseCommand(CommandTypes.SELL_ITEM)) {
            sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " sell <#|all>" + ChatColor.AQUA + " - Sell the item in your hand.");
            sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " sell [itemname] [number]");
        }
        if (canUseCommand(CommandTypes.SET)) {
            sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " set" + ChatColor.AQUA + " - Display list of set commands");
        }
        if (canUseCommand(CommandTypes.REMOVE_ITEM)) {
            sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " remove [itemname]" + ChatColor.AQUA + " - Stop selling item in shop.");
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
                        sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "Could not create shop, it overlaps with " + ChatColor.WHITE + shop.getName());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean shopBrowse() {
        if (canUseCommand(CommandTypes.INVENTORY) && (sender instanceof Player)) {
            Player player = (Player) sender;
            String playerName = player.getName();

            int pageNumber = 1;

            if (args.length == 2) {
                try {
                    pageNumber = Integer.parseInt(args[1]);
                } catch (NumberFormatException ex) {

                }
            }

            if (args.length == 3) {
                try {
                    pageNumber = Integer.parseInt(args[2]);
                } catch (NumberFormatException ex2) {

                }
            }

            // get the shop the player is currently in
            if (plugin.playerData.get(playerName).shopList.size() == 1) {
                UUID shopUuid = plugin.playerData.get(playerName).shopList.get(0);
                Shop shop = plugin.shopData.getShop(shopUuid);

                if (args.length > 1) {
                    if (args[1].equalsIgnoreCase("buy") || args[1].equalsIgnoreCase("sell")) {
                        printInventory(shop, player, args[1], pageNumber);

                    } else if (args[1].equalsIgnoreCase("info")) {
                        player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "Info for shop " + ChatColor.WHITE + shop.getName());
                        player.sendMessage(ChatColor.AQUA + "  Shop Location 1 " + ChatColor.WHITE + shop.getLocationA().toString());
                        player.sendMessage(ChatColor.AQUA + "  Shop Location 2 " + ChatColor.WHITE + shop.getLocationB().toString());
                        player.sendMessage(ChatColor.AQUA + "  Shop Owner " + ChatColor.WHITE + shop.getOwner());
                        String message = "";
                        if (shop.getManagers() != null) {
                            for (String manager : shop.getManagers()) {
                                message += " " + manager;
                            }
                        } else {
                            message = "none";
                        }
                        player.sendMessage(ChatColor.AQUA + "  Shop managers " + ChatColor.WHITE + message);
                        player.sendMessage(ChatColor.AQUA + "  Shop Creator " + ChatColor.WHITE + shop.getCreator());
                        player.sendMessage(ChatColor.AQUA + "  Shop has unlimited Stock " + ChatColor.WHITE + shop.isUnlimitedStock());
                        player.sendMessage(ChatColor.AQUA + "  Shop has unlimited Money " + ChatColor.WHITE + shop.isUnlimitedMoney());
                    } else {
                        printInventory(shop, player, "list", pageNumber);
                    }
                } else {
                    printInventory(shop, player, "list", pageNumber);
                }
            } else {
                player.sendMessage(ChatColor.AQUA + "You must be inside a shop to use /" + commandLabel + " browse");
            }
        } else {
            sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "You don't have permission to use this command");
        }

        return true;
    }

    /**
     * Prints shop inventory with default page # = 1
     * 
     * @param shop
     * @param player
     * @param buySellorList
     */
    public void printInventory(Shop shop, Player player, String buySellorList) {
        printInventory(shop, player, buySellorList, 1);
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
    public void printInventory(Shop shop, Player player, String buySellorList, int pageNumber) {
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
                int price = 0;
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
                subMessage += ChatColor.AQUA + " [" + ChatColor.WHITE + price + " " + plugin.shopData.currencyName + ChatColor.AQUA + "]";
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
                    subMessage += ChatColor.AQUA + " [" + ChatColor.WHITE + "Bundle: " + stack + ChatColor.AQUA + "]";
                }
            }
            
            // get stock
            int stock = item.getStock();
            if (buy) {
                if (stock == 0 && !shop.isUnlimitedStock())
                    continue;
            }
            if (!shop.isUnlimitedStock()) {
                subMessage += ChatColor.AQUA + " [" + ChatColor.WHITE + "Stock: " + stock + ChatColor.AQUA + "]";

                maxStock = item.getMaxStock();
                if (maxStock > 0) {
                    subMessage += ChatColor.AQUA + " [" + ChatColor.WHITE + "Max Stock: " + maxStock + ChatColor.AQUA + "]";
                }
            }

            inventoryMessage.add(subMessage);
        }

        String message = ChatColor.AQUA + "The shop " + ChatColor.WHITE + inShopName + ChatColor.AQUA;

        if (buy) {
            message += " is selling:";
        } else if (sell) {
            message += " is buying:";
        } else {
            message += " trades in: ";
        }

        message += " (Page " + pageNumber + " of "
                + (int) Math.ceil((double) inventoryMessage.size() / (double) 7) + ")";

        player.sendMessage(message);

        int amount = (pageNumber > 0 ? (pageNumber - 1) * 7 : 0);
        for (int i = amount; i < amount + 7; i++) {
            if (inventoryMessage.size() > i) {
                player.sendMessage(inventoryMessage.get(i));
            }
        }

        if (!list) {
            String buySell = (buy ? "buy" : "sell");
            message = ChatColor.AQUA + "To " + buySell + " an item on the list type: " +
                    ChatColor.WHITE + "/" + commandLabel + " " + buySell + " ItemName [amount]";
            player.sendMessage(message);
        } else {
            player.sendMessage(ChatColor.AQUA + "Type " + ChatColor.WHITE + "/" + commandLabel + " list buy"
                    + ChatColor.AQUA + " or " + ChatColor.WHITE + "/" + commandLabel + " list sell");
            player.sendMessage(ChatColor.AQUA + "to see details about price and quantity.");
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
            player.sendMessage(ChatColor.AQUA + "Sorry, " + ChatColor.WHITE + shop.getName() + ChatColor.AQUA + " is not buying " + ChatColor.WHITE + item.name + ChatColor.AQUA + " right now.");
            return false;
        }

        // check how many items the player has
        int playerInventory = countItemsInInventory(player.getInventory(), item.toStack());
        if (amount < 0) {
            amount = 0;
        }

        // check if the amount to add is okay
        if (amount > playerInventory) {
            player.sendMessage(ChatColor.AQUA + "You only have " + ChatColor.WHITE + playerInventory + ChatColor.AQUA + " in your inventory that can be added.");
            amount = playerInventory;
        }

        // check if the shop has a max stock level set
        if (invItem.getMaxStock() != 0 && !shop.isUnlimitedStock()) {
            if (invItem.getStock() >= invItem.getMaxStock()) {
                player.sendMessage(ChatColor.AQUA + "Sorry, " + ChatColor.WHITE + shop.getName() + ChatColor.AQUA + " is not buying any more " + ChatColor.WHITE + item.name + ChatColor.AQUA + " right now.");
                return false;
            }

            if (amount > (invItem.getMaxStock() - invItem.getStock())) {
                amount = invItem.getMaxStock() - invItem.getStock();
            }
        }

        // calculate cost
        int bundles = amount / invItem.getSellSize();

        if (bundles == 0 && amount > 0) {
            player.sendMessage(ChatColor.AQUA + "The minimum number to sell is  " + ChatColor.WHITE + invItem.getSellSize());
            return false;
        }

        int itemPrice = invItem.getSellPrice();
        // recalculate # of items since may not fit cleanly into bundles
        // notify player if there is a change
        if (amount % invItem.getSellSize() != 0) {
            player.sendMessage(ChatColor.AQUA + "The bundle size is  " + ChatColor.WHITE + invItem.getSellSize() + ChatColor.AQUA + " order reduced to " + ChatColor.WHITE + bundles * invItem.getSellSize());
        }
        amount = bundles * invItem.getSellSize();
        int totalCost = bundles * itemPrice;

        // try to pay the player for order
        if (shop.isUnlimitedMoney()) {
            pData.payPlayer(player.getName(), totalCost);
        } else {
            if (!isShopController(shop)) {
                log.info(String.format("From: %s, To: %s, Cost: %d", shop.getOwner(), player.getName(), totalCost));
                if (!pData.payPlayer(shop.getOwner(), player.getName(), totalCost)) {
                    // lshop owner doesn't have enough money
                    // get shop owner's balance and calculate how many it can
                    // buy
                    long shopBalance = plugin.playerData.get(player.getName()).getBalance(shop.getOwner());
                    int bundlesCanAford = (int) shopBalance / itemPrice;
                    totalCost = bundlesCanAford * itemPrice;
                    amount = bundlesCanAford * invItem.getSellSize();
                    player.sendMessage(ChatColor.AQUA + "The shop could only afford " + ChatColor.WHITE + amount);
                    if (!pData.payPlayer(shop.getOwner(), player.getName(), totalCost)) {
                        player.sendMessage(ChatColor.AQUA + "Unexpected money problem: could not complete sale.");
                        return false;
                    }
                }
            }
        }

        if (!shop.isUnlimitedStock()) {
            shop.addStock(item.name, amount);
        }

        if (isShopController(shop)) {
            player.sendMessage(ChatColor.AQUA + "You added " + ChatColor.WHITE + amount + " " + item.name + ChatColor.AQUA + " to the shop");
        } else {
            player.sendMessage(ChatColor.AQUA + "You sold " + ChatColor.WHITE + amount + " " + item.name + ChatColor.AQUA + " and gained " + ChatColor.WHITE + totalCost + " " + plugin.shopData.currencyName);
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
        log.info("shopSell");
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
            if (!canUseCommand(CommandTypes.SELL_ITEM)) {
                sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "You don't have permission to use this command");
                return false;
            }
            
            // sell all (player only command)
            Pattern pattern = Pattern.compile("(?i)sell\\s+all");
            Matcher matcher = pattern.matcher(command);
            if (matcher.find()) {
                ItemStack itemStack = player.getItemInHand();
                if (itemStack == null) {
                    return false;
                }
                int amount = countItemsInInventory(player.getInventory(), itemStack);
                ItemInfo item = Search.itemById(itemStack.getTypeId(), itemStack.getDurability());
                if(item == null) {
                    sender.sendMessage("Could not find an item.");
                    return false;
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
                    return false;
                }
                int amount = itemStack.getAmount();
                ItemInfo item = Search.itemById(itemStack.getTypeId(), itemStack.getDurability());
                if(item == null) {
                    sender.sendMessage("Could not find an item.");
                    return false;
                }
                return shopSell(shop, item, amount);
            }            
        } else {
            sender.sendMessage("Console is not implemented yet.");
            return false;
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
                return false;
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
                return false;
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
                return false;
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
                return false;
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
                return false;
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
                return false;
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
                return false;
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
                return false;
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
                return false;
            }
            return shopSell(shop, item, 1);
        }
        
        // Show sell help
        sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " sell [itemname] [number] " + ChatColor.AQUA + "- Sell this item.");
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
                sender.sendMessage(ChatColor.AQUA + "You only have " + ChatColor.WHITE + playerItemCount + ChatColor.AQUA + " in your inventory that can be added.");
                // amount = playerItemCount;
                return false;
            }

            // If ALL (amount == -1), set amount to the count the player has
            if (amount == -1) {
                amount = playerItemCount;
            }
        }

        // Check Shop Contents, add if necessary
        if (amount == 0 & shop.containsItem(item)) {
            // nicely message user
            sender.sendMessage(String.format("This shop already carries %s!", item.name));
            return true;
        }

        // Add item to shop if needed
        if (!shop.containsItem(item)) {
            shop.addItem(item.typeId, item.subTypeId, 0, 1, 0, 1, 0, 0);
        }

        // Check stock settings, add stock if necessary
        if (shop.isUnlimitedStock()) {
            sender.sendMessage(ChatColor.AQUA + "Succesfully added " + ChatColor.WHITE + item.name + ChatColor.AQUA + " to the shop.");
        } else {
            shop.addStock(item.name, amount);
            sender.sendMessage(ChatColor.AQUA + "Succesfully added " + ChatColor.WHITE + item.name + ChatColor.AQUA + " to the shop. Stock is now " + ChatColor.WHITE + shop.getItem(item.name).getStock());
        }

        // log the transaction
        if (player != null) {
            int itemInv = shop.getItem(item.name).getStock();
            int startInv = itemInv - amount;
            if (startInv < 0) {
                startInv = 0;
            }
            plugin.shopData.logItems(player.getName(), shop.getName(), "add-item", item.name, amount, startInv, itemInv);

            // take items from player
            removeItemsFromInventory(player.getInventory(), item.toStack(), amount);
        }
        plugin.shopData.saveShop(shop);
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
        log.info("shopAdd");
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
            if (!canUseCommand(CommandTypes.ADD_ITEM)) {
                sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "You don't have permission to use this command");
                return false;
            }            

            // Check if Player can Modify
            if (!isShopController(shop)) {
                player.sendMessage(ChatColor.AQUA + "You must be the shop owner or a manager to set this.");
                player.sendMessage(ChatColor.AQUA + "The current shop owner is " + ChatColor.WHITE + shop.getOwner());
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
                int amount = itemStack.getAmount();
                ItemInfo item = Search.itemById(itemStack.getTypeId(), itemStack.getDurability());
                if(item == null) {
                    sender.sendMessage("Could not find an item.");
                    return false;
                }
                return shopAdd(shop, item, amount);
            }
            
        } else {
            sender.sendMessage("Console is not implemented yet.");
            return false;
        }

        // Command matching     
        
        // add int
        Pattern pattern = Pattern.compile("(?i)add\\s+(\\d+)");
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
        pattern = Pattern.compile("(?i)add\\s+(\\d+)\\s+(\\d+)");
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
        pattern = Pattern.compile("(?i)add\\s+(\\d+):(\\d+)");
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
        pattern = Pattern.compile("(?i)add\\s+(\\d+):(\\d+)\\s+(\\d+)");
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
        pattern = Pattern.compile("(?i)add\\s+(.*)\\s+(\\d+)");
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
        pattern = Pattern.compile("(?i)add\\s+(.*)");
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
        sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " buy [itemname] [number] " + ChatColor.AQUA + "- Buy this item.");
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
            player.sendMessage(ChatColor.AQUA + "Sorry, " + ChatColor.WHITE + shop.getName() + ChatColor.AQUA + " is not selling " + ChatColor.WHITE + item.name + ChatColor.AQUA + " right now.");
            return false;
        } else if(invItem.getStock() == 0) {
            player.sendMessage(ChatColor.AQUA + "Sorry, " + ChatColor.WHITE + shop.getName() + ChatColor.AQUA + " is sold out of " + ChatColor.WHITE + item.name + ChatColor.AQUA + " right now.");
            return false;
        }
        
        // check if the item has a price, or if this is a shop owner
        if (invItem.getBuyPrice() == 0 && !isShopController(shop)) {
            player.sendMessage(ChatColor.AQUA + "Sorry, " + ChatColor.WHITE + shop.getName() + ChatColor.AQUA + " is not selling " + ChatColor.WHITE + item.name + ChatColor.AQUA + " right now.");
            return false;
        }        

        // if amount = 0, assume single stack size
        if(amount == 0) {
            amount = invItem.getBuySize();
        }
        
        // Check that shop has enough
        if(amount >= invItem.getStock()) {
            // enough
        } else {
            // not enough
        }

        int totalAmount;
        totalAmount = invItem.getStock();

        if (totalAmount == 0 && !shop.isUnlimitedStock()) {
            player.sendMessage(ChatColor.AQUA + "The shop has " + ChatColor.WHITE + totalAmount + " " + item.name);
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
                player.sendMessage(ChatColor.AQUA + "The shop has " + ChatColor.WHITE + totalAmount + " " + item.name);
            }
        } else if(amount % invItem.getBuySize() != 0){
            amount = amount - (amount % invItem.getBuySize());
            player.sendMessage(ChatColor.AQUA + "The bundle size is  " + ChatColor.WHITE + invItem.getBuySize() + ChatColor.AQUA + " order reduced to " + ChatColor.WHITE + amount);
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
            player.sendMessage(ChatColor.AQUA + "You only have room for " + ChatColor.WHITE + amount);
        }

        // calculate cost
        int bundles = amount / invItem.getBuySize();
        int itemPrice = invItem.getBuyPrice();
        // recalculate # of items since may not fit cleanly into bundles
        amount = bundles * invItem.getBuySize();
        int totalCost = bundles * itemPrice;

        // try to pay the shop owner
        if (!isShopController(shop)) {
            if (!pData.payPlayer(player.getName(), shop.getOwner(), totalCost)) {
                // player doesn't have enough money
                // get player's balance and calculate how many it can buy
                long playerBalance = pData.getBalance(player.getName());
                int bundlesCanAford = (int) Math.floor(playerBalance / itemPrice);
                totalCost = bundlesCanAford * itemPrice;
                amount = bundlesCanAford * invItem.getSellSize();
                player.sendMessage(ChatColor.AQUA + "You could only afford " + ChatColor.WHITE + amount);

                if (!pData.payPlayer(player.getName(), shop.getOwner(), totalCost)) {
                    player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "Unexpected money problem: could not complete sale.");
                    return false;
                }
            }
        }

        if (!shop.isUnlimitedStock()) {
            shop.removeStock(item.name, amount);
        }
        if (isShopController(shop)) {
            player.sendMessage(ChatColor.AQUA + "You removed " + ChatColor.WHITE + amount + " " + item.name + ChatColor.AQUA + " from the shop");
        } else {
            player.sendMessage(ChatColor.AQUA + "You purchased " + ChatColor.WHITE + amount + " " + item.name + ChatColor.AQUA + " for " + ChatColor.WHITE + totalCost + " " + plugin.shopData.currencyName);
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
        log.info("shopBuy");
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
            if (!canUseCommand(CommandTypes.SELL_ITEM)) {
                sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "You don't have permission to use this command");
                return false;
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
                    return false;
                }
                return shopBuy(shop, item, 0);
            }            
        } else {
            sender.sendMessage("Console is not implemented yet.");
            return false;
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
                return false;
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
                return false;
            }
            return shopBuy(shop, item, count);
        }
        
        // buy int all
        matcher.reset();
        pattern = Pattern.compile("(?i)buy\\s+(\\d+)\\s+all");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            int count = Integer.parseInt(matcher.group(2));
            ItemInfo item = Search.itemById(id);
            if(item == null) {
                sender.sendMessage("Could not find an item.");
                return false;
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
                return false;
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
                return false;
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
            int count = Integer.parseInt(matcher.group(3));
            if(item == null) {
                sender.sendMessage("Could not find an item.");
                return false;
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
                return false;
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
            int count = Integer.parseInt(matcher.group(2));
            if(item == null) {
                sender.sendMessage("Could not find an item.");
                return false;
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
                return false;
            }
            return shopBuy(shop, item, 0);
        }
        
        // Show sell help
        sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " buy [itemname] [number] " + ChatColor.AQUA + "- Buy an item.");
        return true;
    }

    public boolean shopSet() {
        // Check Permissions
        if (!canUseCommand(CommandTypes.SET)) {
            sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "You don't have permission to use this command");
            return false;
        }

        // Check minimum variable length
        if (args.length < 2) {
            return shopSetHelp();
        }

        log.info(String.format("[%s] Command issued: %s", plugin.pdfFile.getName(), command));

        // Parse Arguments
        if (command.matches("(?i)set\\s+buy.*")) {
            return shopSetBuy();
        } else if (command.matches("(?i)set\\s+sell.*")) {
            return shopSetSell();
        } else if (command.matches("(?i)set\\s+max.*")) {
            return shopSetMax();
        } else if (command.matches("(?i)set\\s+unlimited.*")) {
            return shopSetUnlimited();
        } else if (command.matches("(?i)set\\s+manager.*")) {
            return shopSetManager();
        } else if (command.matches("(?i)set\\s+owner.*")) {
            return shopSetOwner();
        } else if (command.matches("(?i)set\\s+name.*")) {
            return shopSetName();
        } else {
            return shopSetHelp();
        }
    }

    private boolean shopSetBuy(Shop shop, ItemInfo item, int price, int size) {
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
        sender.sendMessage(ChatColor.AQUA + "The buy information for " + ChatColor.WHITE + item.name + ChatColor.AQUA + " has been updated.");
        sender.sendMessage("   " + ChatColor.WHITE + item.name + ChatColor.AQUA + " [" + ChatColor.WHITE + price + " " + plugin.shopData.currencyName + ChatColor.AQUA + "] [" + ChatColor.WHITE + "Bundle: " + size + ChatColor.AQUA + "]");

        return true;
    }

    private boolean shopSetBuy(Shop shop, ItemInfo item, int price) {
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
        sender.sendMessage(ChatColor.AQUA + "The buy information for " + ChatColor.WHITE + item.name + ChatColor.AQUA + " has been updated.");
        sender.sendMessage("   " + ChatColor.WHITE + item.name + ChatColor.AQUA + " [" + ChatColor.WHITE + price + " " + plugin.shopData.currencyName + ChatColor.AQUA + "]");

        return true;
    }

    private boolean shopSetBuy() {
        log.info("shopSetBuy");
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

            // Check if Player can Modify
            if (!isShopController(shop)) {
                player.sendMessage(ChatColor.AQUA + "You must be the shop owner or a manager to set this.");
                player.sendMessage(ChatColor.AQUA + "The current shop owner is " + ChatColor.WHITE + shop.getOwner());
                return true;
            }
        } else {
            sender.sendMessage("Console is not implemented yet.");
            return false;
        }

        // Command matching

        // set buy int int int
        Pattern pattern = Pattern.compile("(?i)set\\s+buy\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)");
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            ItemInfo item = Search.itemById(id);
            int price = Integer.parseInt(matcher.group(2));
            int size = Integer.parseInt(matcher.group(3));
            return shopSetBuy(shop, item, price, size);
        }

        // set buy int:int int int
        matcher.reset();
        pattern = Pattern.compile("(?i)set\\s+buy\\s+(\\d+):(\\d+)\\s+(\\d+)\\s+(\\d+)");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            short type = Short.parseShort(matcher.group(2));
            ItemInfo item = Search.itemById(id, type);
            int price = Integer.parseInt(matcher.group(3));
            int size = Integer.parseInt(matcher.group(4));
            return shopSetBuy(shop, item, price, size);
        }

        // set buy int int
        matcher.reset();
        pattern = Pattern.compile("(?i)set\\s+buy\\s+(\\d+)\\s+(\\d+)");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            ItemInfo item = Search.itemById(id);
            int price = Integer.parseInt(matcher.group(2));
            return shopSetBuy(shop, item, price);
        }

        // set buy int:int int
        matcher.reset();
        pattern = Pattern.compile("(?i)set\\s+buy\\s+(\\d+):(\\d+)\\s+(\\d+)");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            short type = Short.parseShort(matcher.group(2));
            ItemInfo item = Search.itemById(id, type);
            int price = Integer.parseInt(matcher.group(3));
            return shopSetBuy(shop, item, price);
        }

        // set buy (chars) int int
        matcher.reset();
        pattern = Pattern.compile("(?i)set\\s+buy\\s+(.*)\\s+(\\d+)\\s+(\\d+)");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            String name = matcher.group(1);
            ItemInfo item = Search.itemByName(name);
            int price = Integer.parseInt(matcher.group(2));
            int size = Integer.parseInt(matcher.group(3));
            return shopSetBuy(shop, item, price, size);
        }

        // set buy (chars) int
        matcher.reset();
        pattern = Pattern.compile("(?i)set\\s+buy\\s+(.*)\\s+(\\d+)");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            String name = matcher.group(1);
            ItemInfo item = Search.itemByName(name);
            int price = Integer.parseInt(matcher.group(2));
            return shopSetBuy(shop, item, price);
        }

        // show set buy usage
        sender.sendMessage("   " + "/" + commandLabel + " set buy [item name] [price] <bundle size>");
        return true;
    }

    private boolean shopSetSell(Shop shop, ItemInfo item, int price, int size) {
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
        sender.sendMessage(ChatColor.AQUA + "The sell information for " + ChatColor.WHITE + item.name + ChatColor.AQUA + " has been updated.");
        sender.sendMessage("   " + ChatColor.WHITE + item.name + ChatColor.AQUA + " [" + ChatColor.WHITE + price + " " + plugin.shopData.currencyName + ChatColor.AQUA + "] [" + ChatColor.WHITE + "Bundle: " + size + ChatColor.AQUA + "]");

        return true;
    }

    private boolean shopSetSell(Shop shop, ItemInfo item, int price) {
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
        sender.sendMessage(ChatColor.AQUA + "The sell information for " + ChatColor.WHITE + item.name + ChatColor.AQUA + " has been updated.");
        sender.sendMessage("   " + ChatColor.WHITE + item.name + ChatColor.AQUA + " [" + ChatColor.WHITE + price + " " + plugin.shopData.currencyName + ChatColor.AQUA + "]");

        return true;
    }

    private boolean shopSetSell() {
        log.info("shopSetSell");
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

            // Check if Player can Modify
            if (!isShopController(shop)) {
                player.sendMessage(ChatColor.AQUA + "You must be the shop owner or a manager to set this.");
                player.sendMessage(ChatColor.AQUA + "The current shop owner is " + ChatColor.WHITE + shop.getOwner());
                return true;
            }
        } else {
            sender.sendMessage("Console is not implemented yet.");
            return false;
        }

        // Command matching

        // set sell int int int
        Pattern pattern = Pattern.compile("(?i)set\\s+sell\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)");
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            ItemInfo item = Search.itemById(id);
            int price = Integer.parseInt(matcher.group(2));
            int size = Integer.parseInt(matcher.group(3));
            return shopSetSell(shop, item, price, size);
        }

        // set sell int:int int int
        matcher.reset();
        pattern = Pattern.compile("(?i)set\\s+sell\\s+(\\d+):(\\d+)\\s+(\\d+)\\s+(\\d+)");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            short type = Short.parseShort(matcher.group(2));
            ItemInfo item = Search.itemById(id, type);
            int price = Integer.parseInt(matcher.group(3));
            int size = Integer.parseInt(matcher.group(4));
            return shopSetSell(shop, item, price, size);
        }

        // set sell int int
        matcher.reset();
        pattern = Pattern.compile("(?i)set\\s+sell\\s+(\\d+)\\s+(\\d+)");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            ItemInfo item = Search.itemById(id);
            int price = Integer.parseInt(matcher.group(2));
            return shopSetSell(shop, item, price);
        }

        // set sell int:int int
        matcher.reset();
        pattern = Pattern.compile("(?i)set\\s+sell\\s+(\\d+):(\\d+)\\s+(\\d+)");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            short type = Short.parseShort(matcher.group(2));
            ItemInfo item = Search.itemById(id, type);
            int price = Integer.parseInt(matcher.group(3));
            return shopSetSell(shop, item, price);
        }

        // set sell (chars) int int
        matcher.reset();
        pattern = Pattern.compile("(?i)set\\s+sell\\s+(.*)\\s+(\\d+)\\s+(\\d+)");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            String name = matcher.group(1);
            ItemInfo item = Search.itemByName(name);
            int price = Integer.parseInt(matcher.group(2));
            int size = Integer.parseInt(matcher.group(3));
            return shopSetSell(shop, item, price, size);
        }

        // set sell (chars) int
        matcher.reset();
        pattern = Pattern.compile("(?i)set\\s+sell\\s+(.*)\\s+(\\d+)");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            String name = matcher.group(1);
            ItemInfo item = Search.itemByName(name);
            int price = Integer.parseInt(matcher.group(2));
            return shopSetSell(shop, item, price);
        }

        // show set sell usage
        sender.sendMessage("   " + "/" + commandLabel + " set sell [item name] [price] <bundle size>");
        return true;
    }

    private boolean shopSetHelp() {
        log.info("shopSetHelp");
        // Display list of set commands & return
        sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "The following set commands are available: ");
        sender.sendMessage("   " + "/" + commandLabel + " set buy [item name] [price] <bundle size>");
        sender.sendMessage("   " + "/" + commandLabel + " set sell [item name] [price] <bundle size>");
        sender.sendMessage("   " + "/" + commandLabel + " set max [item name] [max number]");
        sender.sendMessage("   " + "/" + commandLabel + " set manager +[playername] -[playername2]");
        sender.sendMessage("   " + "/" + commandLabel + " set owner [player name]");
        if (canUseCommand(CommandTypes.ADMIN)) {
            sender.sendMessage("   " + "/" + commandLabel + " set unlimited money");
            sender.sendMessage("   " + "/" + commandLabel + " set unlimited stock");
        }
        return true;
    }
    
    private boolean shopSetName() {
        log.info("shopSetName");
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
                return false;                
            }
            
            // Check if Player can Modify  
            if(!canModifyShop(shop)) {
              sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "You must be the shop owner to set this.");
                sender.sendMessage(ChatColor.AQUA + "  The current shop owner is " + ChatColor.WHITE + shop.getOwner());                
                return true;
            }
        } else {
            sender.sendMessage("Console is not implemented yet.");
            return false;            
        }
        
        Pattern pattern = Pattern.compile("(?i)set\\s+name\\s+(.*)");
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            String name = matcher.group(1).trim();
            shop.setName(name);
            plugin.shopData.saveShop(shop);
            notifyPlayers(shop, new String[] { LocalShops.CHAT_PREFIX + ChatColor.AQUA + "Shop name is now " + ChatColor.WHITE + shop.getName() } );
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
                p.removePlayerFromShop(thisPlayer, shop.getUuid());
                for(String message : messages) {
                    thisPlayer.sendMessage(message);
                }
            }
        }
        return true;
    }

    private boolean shopSetOwner() {
        log.info("shopSetOwner");
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

            // Check if Player can Modify     
            if (!canUseCommand(CommandTypes.ADMIN) && !shop.getOwner().equalsIgnoreCase(player.getName())) {
                sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "You must be the shop owner to set this.");
                sender.sendMessage(ChatColor.AQUA + "  The current shop owner is " + ChatColor.WHITE + shop.getOwner());
                return true;
            }
            
            if (!canUseCommand(CommandTypes.SET_OWNER) && !canUseCommand(CommandTypes.ADMIN)) {
                sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "You don't have permission to use this command");
                return false;
            }
        } else {
            sender.sendMessage("Console is not implemented yet.");
            return false;
        }

        if (args.length == 3) {

            if (!canUseCommand(CommandTypes.SET_OWNER)) {
                sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "You do not have permission to do this.");
                return false;
            } else {
                shop.setOwner(args[2]);

                // Save Shop
                plugin.shopData.saveShop(shop);

                notifyPlayers(shop, new String[] { LocalShops.CHAT_PREFIX + ChatColor.AQUA + shop.getName() +" is now under new management!  The new owner is " + ChatColor.WHITE + shop.getOwner() } );
                return true;
            }
        } else {
            sender.sendMessage("   " + "/" + commandLabel + " set owner [player name]");
            return true;
        }
    }

    private boolean shopSetManager() {
        log.info("shopSetOwner");
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

            // Check if Player can Modify
            if (!shop.getOwner().equalsIgnoreCase(player.getName())) {
                player.sendMessage(ChatColor.AQUA + "You must be the shop owner to set this.");
                player.sendMessage(ChatColor.AQUA + "The current shop owner is " + ChatColor.WHITE + shop.getOwner());
                return false;
            }
        } else {
            sender.sendMessage("Console is not implemented yet.");
            return false;
        }

        if (args.length >= 3) {
            // shop set manager +managername -managername
            for (int i = 2; i < args.length; i++) {
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

            notifyPlayers(shop, new String[] { ChatColor.AQUA + "The shop managers have been updated. The current managers are:", Search.join(shop.getManagers(), ", ") } );
            return true;
        }
        
        // show set manager usage
        sender.sendMessage("   " + "/" + commandLabel + " set manager +[playername] -[playername2]");
        return true;
    }

    private boolean shopSetUnlimited() {
        log.info("shopSetUnlimited");
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
            if (!canUseCommand(CommandTypes.ADMIN)) {
                player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "You must be a shop admin to do this.");
                return false;
            }            
        } else {
            sender.sendMessage("Console is not implemented yet.");
            return false;
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
            sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "Unlimited money was set to " + ChatColor.WHITE + shop.isUnlimitedMoney());
            plugin.shopData.saveShop(shop);
            return true;
        }

        // shop set unlimited stock
        matcher.reset();
        pattern = Pattern.compile("(?i)set\\s+unlimited\\s+stock");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            shop.setUnlimitedStock(!shop.isUnlimitedStock());
            sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "Unlimited stock was set to " + ChatColor.WHITE + shop.isUnlimitedStock());
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
        log.info("shopSetMax");
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

            // Check if Player can Modify
            if (!isShopController(shop)) {
                player.sendMessage(ChatColor.AQUA + "You must be the shop owner or a manager to set this.");
                player.sendMessage(ChatColor.AQUA + "The current shop owner is " + ChatColor.WHITE + shop.getOwner());
                return true;
            }
        } else {
            sender.sendMessage("Console is not implemented yet.");
            return false;
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

    /**
     * Processes set command.
     * 
     * @param sender
     * @param args
     * @return true - if command succeeds false otherwise
     */
    public boolean shopSetItemOld() {
        if (!(sender instanceof Player) || !canUseCommand(CommandTypes.SET)) {
            sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "You don't have permission to use this command");
            return false;
        }

        /*
         * Available formats: /lshop set buy itemName price stackSize /lshop set
         * sell itemName price stackSize /lshop set max itemName amount /lshop
         * set manager +managerName +managerName -managerName /lshop set owner
         * ownerName
         */

        Player player = (Player) sender;
        String playerName = player.getName();

        // get the shop the player is currently in
        if (plugin.playerData.get(playerName).shopList.size() == 1) {
            UUID shoopUuid = plugin.playerData.get(playerName).shopList.get(0);
            Shop shop = plugin.shopData.getShop(shoopUuid);

            if (!isShopController(shop)) {
                player.sendMessage(ChatColor.AQUA + "You must be the shop owner or a manager to set this.");
                player.sendMessage(ChatColor.AQUA + "The current shop owner is " + ChatColor.WHITE + shop.getOwner());
                return false;
            }

            if (args.length == 1) {
                String[] temp = args.clone();
                args = new String[2];
                args[0] = temp[0];
                args[1] = "empty";
            }

            if (args[1].equalsIgnoreCase("buy")) {
                // /lshop set buy ItemName Price <bundle size>

                ItemStack item = null;
                String itemName = null;

                if (args.length == 4 || args.length == 5) {
                    int price = 0;
                    int bundle = 1;

                    item = LocalShops.itemList.getShopItem(player, shop, args[2]);
                    if (item == null) {
                        player.sendMessage(ChatColor.AQUA + "Could not complete command.");
                        return false;
                    } else {
                        int itemData = item.getDurability();
                        itemName = LocalShops.itemList.getItemName(item.getTypeId(), itemData);
                    }

                    if (!shop.getItems().contains(itemName)) {
                        player.sendMessage(ChatColor.AQUA + "Shop is not yet selling " + ChatColor.WHITE + itemName);
                        player.sendMessage(ChatColor.AQUA + "To add the item use " + ChatColor.WHITE + "/" + commandLabel + " add");
                        return false;
                    }

                    try {
                        if (args.length == 4) {
                            price = Integer.parseInt(args[3]);
                            bundle = shop.getItem(itemName).getBuySize();
                        } else {
                            price = Integer.parseInt(args[3]);
                            bundle = Integer.parseInt(args[4]);
                        }
                    } catch (NumberFormatException ex1) {
                        player.sendMessage(ChatColor.AQUA + "The price and bundle size must be a number.");
                        player.sendMessage(ChatColor.AQUA + "The command format is " + ChatColor.WHITE + "/" + commandLabel + " set buy [item name] [price] <bundle size>");
                        return false;
                    }

                    shop.setItemBuyPrice(itemName, price);
                    shop.setItemBuyAmount(itemName, bundle);

                    player.sendMessage(ChatColor.AQUA + "The buy information for " + ChatColor.WHITE + itemName + ChatColor.AQUA + " has been updated.");
                    player.sendMessage("   " + ChatColor.WHITE + itemName + ChatColor.AQUA + " [" + ChatColor.WHITE + price + " " + plugin.shopData.currencyName + ChatColor.AQUA + "] [" + ChatColor.WHITE + "Bundle: " + bundle + ChatColor.AQUA + "]");

                    plugin.shopData.saveShop(shop);

                } else {
                    player.sendMessage(ChatColor.AQUA + "The command format is " + ChatColor.WHITE + "/" + commandLabel + " set buy [item name] [price] <bundle size>");
                    return true;
                }

            } else if (args[1].equalsIgnoreCase("sell")) {

                // /lshop set sell ItemName Price <bundle size>

                ItemStack item = null;
                String itemName = null;

                if (args.length == 4 || args.length == 5) {
                    int price = 0;
                    int bundle = 1;

                    item = LocalShops.itemList.getShopItem(player, shop, args[2]);
                    if (item == null) {
                        player.sendMessage(ChatColor.AQUA + "Could not complete command.");
                        return false;
                    } else {
                        int itemData = item.getDurability();
                        itemName = LocalShops.itemList.getItemName(item.getTypeId(), itemData);
                    }

                    if (!shop.getItems().contains(itemName)) {
                        player.sendMessage(ChatColor.AQUA + "Shop is not yet buying " + ChatColor.WHITE + itemName);
                        player.sendMessage(ChatColor.AQUA + "To add the item use " + ChatColor.WHITE + "/" + commandLabel + " add");
                        return false;
                    }

                    try {
                        if (args.length == 4) {
                            price = Integer.parseInt(args[3]);
                            bundle = shop.getItem(itemName).getSellSize();
                        } else {
                            price = Integer.parseInt(args[3]);
                            bundle = Integer.parseInt(args[4]);
                        }
                    } catch (NumberFormatException ex1) {
                        player.sendMessage(ChatColor.AQUA + "The price and bundle size must be a number.");
                        player.sendMessage(ChatColor.AQUA + "The command format is " + ChatColor.WHITE + "/" + commandLabel + " set sell [item name] [price] <bundle size>");
                        return false;
                    }

                    shop.getItem(itemName).setSellPrice(price);
                    shop.getItem(itemName).setSellSize(bundle);

                    plugin.shopData.saveShop(shop);

                    player.sendMessage(ChatColor.AQUA + "The sell information for " + ChatColor.WHITE + itemName + ChatColor.AQUA + " has been updated.");
                    player.sendMessage("   " + ChatColor.WHITE + itemName + ChatColor.AQUA + " [" + ChatColor.WHITE + price + " " + plugin.shopData.currencyName + ChatColor.AQUA + "] [" + ChatColor.WHITE + "Bundle: " + bundle + ChatColor.AQUA + "]");

                } else {
                    player.sendMessage(ChatColor.AQUA + "The command format is " + ChatColor.WHITE + "/" + commandLabel + " set sell [item name] [price] <bundle size>");
                    return true;
                }

            } else if (args[1].equalsIgnoreCase("max")) {

                // /lshop set max ItemName amount

                ItemStack item = null;
                String itemName = null;

                int maxStock = 0;

                if (args.length == 4) {

                    item = LocalShops.itemList.getShopItem(player, shop, args[2]);
                    if (item == null) {
                        player.sendMessage(ChatColor.AQUA + "Could not complete command.");
                        return false;
                    } else {
                        int itemData = item.getDurability();
                        itemName = LocalShops.itemList.getItemName(item.getTypeId(), itemData);
                    }

                    if (!shop.getItems().contains(itemName)) {
                        player.sendMessage(ChatColor.AQUA + "Shop is not yet buying " + ChatColor.WHITE + itemName);
                        player.sendMessage(ChatColor.AQUA + "To add the item use " + ChatColor.WHITE + "/" + commandLabel + " add");
                        return false;
                    }

                    try {
                        maxStock = Integer.parseInt(args[3]);
                    } catch (NumberFormatException ex1) {
                        player.sendMessage(ChatColor.AQUA + "The price and bundle size must be a number.");
                        player.sendMessage(ChatColor.AQUA + "The command format is " + ChatColor.WHITE + "/" + commandLabel + " set sell [item name] [price] <bundle size>");
                        return false;
                    }

                    shop.setItemMaxStock(itemName, maxStock);

                    plugin.shopData.saveShop(shop);

                    player.sendMessage(ChatColor.AQUA + "The max stock level for " + ChatColor.WHITE + itemName
                            + ChatColor.AQUA + " has been changed to " + maxStock + ".");

                } else {
                    player.sendMessage(ChatColor.AQUA + "The command format is " + ChatColor.WHITE + "/" + commandLabel + " set max [item name] [amount]");
                    return true;
                }

            } else if (args[1].equalsIgnoreCase("manager")) {

            } else if (args[1].equalsIgnoreCase("owner")) {

            } else if (args[1].equalsIgnoreCase("unlimited")) {
                if (!canUseCommand(CommandTypes.ADMIN)) {
                    player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "You must be a shop admin to do this.");
                    return false;
                } else {
                    if (args.length == 3) {
                        if (args[2].equalsIgnoreCase("money")) {
                            boolean current = shop.isUnlimitedMoney();
                            shop.setUnlimitedMoney(!current);
                            player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "Unlimited money was set to " + ChatColor.WHITE + shop.isUnlimitedMoney());
                            plugin.shopData.saveShop(shop);
                            return true;
                        } else if (args[2].equalsIgnoreCase("stock")) {
                            boolean current = shop.isUnlimitedStock();
                            shop.setUnlimitedStock(!current);
                            player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "Unlimited stock was set to " + ChatColor.WHITE + shop.isUnlimitedStock());
                            plugin.shopData.saveShop(shop);
                            return true;
                        }
                    }
                    player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "The following set commands are available: ");
                    player.sendMessage("   " + "/" + commandLabel + " set unlimited money");
                    player.sendMessage("   " + "/" + commandLabel + " set unlimited stock");
                    return true;
                }
            } else {
                player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "The following set commands are available: ");
                player.sendMessage("   " + "/" + commandLabel + " set buy [item name] [price] <bundle size>");
                player.sendMessage("   " + "/" + commandLabel + " set sell [item name] [price] <bundle size>");
                player.sendMessage("   " + "/" + commandLabel + " set max [item name] [max number]");
                player.sendMessage("   " + "/" + commandLabel + " set manager +[playername] -[playername2]");
                player.sendMessage("   " + "/" + commandLabel + " set owner [player name]");
                if (canUseCommand(CommandTypes.ADMIN)) {
                    player.sendMessage("   " + "/" + commandLabel + " set unlimited money");
                    player.sendMessage("   " + "/" + commandLabel + " set unlimited stock");
                }
            }

            plugin.shopData.saveShop(shop);

        } else {
            player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "You must be inside a shop to use /" + commandLabel + " " + args[0]);
            return false;
        }

        return true;
    }
    
    private boolean shopRemove(Shop shop, ItemInfo item) {
        if (item == null) {
            sender.sendMessage(ChatColor.AQUA + "Item not found.");
            return false;
        }

        if(!shop.containsItem(item)) {
            sender.sendMessage(ChatColor.AQUA + "The shop is not selling " + ChatColor.WHITE + item.name);
            return true;
        }
        
        sender.sendMessage(ChatColor.WHITE + item.name + ChatColor.AQUA + " removed from the shop. ");
        if (!shop.isUnlimitedStock()) {
            int amount = shop.getItem(item.name).getStock();

            if (sender instanceof Player) {
                Player player = (Player) sender;
                // log the transaction
                plugin.shopData.logItems(player.getName(), shop.getName(), "remove-item", item.name, amount, amount, 0);

                givePlayerItem(item.toStack(), amount);
                player.sendMessage("" + ChatColor.WHITE + amount + ChatColor.AQUA + " have been returned to your inventory");
            }
        }

        shop.removeItem(item.name);
        plugin.shopData.saveShop(shop);        
        
        return false;
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
        log.info("shopAdd");
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
            if (!canUseCommand(CommandTypes.REMOVE_ITEM)) {
                sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "You don't have permission to use this command");
                return false;
            }            

            // Check if Player can Modify
            if (!isShopController(shop)) {
                player.sendMessage(ChatColor.AQUA + "You must be the shop owner or a manager to set this.");
                player.sendMessage(ChatColor.AQUA + "The current shop owner is " + ChatColor.WHITE + shop.getOwner());
                return true;
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
        sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " remove [itemname]" + ChatColor.AQUA + " - Stop selling item in shop.");
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
        log.info("shopDestory");
        if (!(sender instanceof Player) || !canUseCommand(CommandTypes.ADMIN)) {
            sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "You don't have permission to use this command");
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
                player.sendMessage(ChatColor.AQUA + "You must be the shop owner to destroy it.");
                return false;
            }

            Iterator<PlayerData> it = plugin.playerData.values().iterator();
            while(it.hasNext()) {
                PlayerData p = it.next();
                if(p.shopList.contains(shop.getUuid())) {
                    Player thisPlayer = plugin.getServer().getPlayer(p.playerName);
                    p.removePlayerFromShop(thisPlayer, shop.getUuid());
                    thisPlayer.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.WHITE + shop.getName() + ChatColor.AQUA + " has been destroyed");
                }
            }
            
            plugin.shopData.deleteShop(shop);

        } else {
            player.sendMessage(ChatColor.AQUA + "You must be inside a shop to use /" + commandLabel + " " + args[0]);
        }

        return true;
    }

    public int countItemsInInventory(PlayerInventory inventory, ItemStack item) {
        int totalAmount = 0;
        boolean isDurable = LocalShops.itemList.isDurable(item);

        for (Integer i : inventory.all(item.getType()).keySet()) {
            ItemStack thisStack = inventory.getItem(i);
            if (isDurable) {
                int damage = calcDurabilityPercentage(thisStack);
                if (damage > plugin.shopData.maxDamage && plugin.shopData.maxDamage != 0)
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
                if (damage > plugin.shopData.maxDamage && plugin.shopData.maxDamage != 0)
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
}
