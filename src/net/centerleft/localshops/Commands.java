package net.centerleft.localshops;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Logger;

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

    // Command Types Enum
    private static enum CommandTypes {
        ADMIN(0, new String[] { "localshops.admin" }),
        ADD_ITEM(1, new String[] { "localshops.manage" }),
        BUY_ITEM(2, new String[] { "localshops.buysell" }),
        CREATE_SHOP(3, new String[] { "localshops.create" }),
        CREATE_SHOP_FREE(4, new String[] { "localshops.create.free" }),
        DESTROY_SHOP(5, new String[] { "localshops.destroy" }),
        HELP(6, new String[] {}),
        LIST(7, new String[] { "localshops.buysell" }),
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
    }

    public boolean shopDebug() {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Shop shop = null;

            // Get current shop
            String currShop = plugin.playerData.get(player.getName()).getCurrentShop();
            if (currShop != null) {
                shop = plugin.shopData.getShop(currShop);
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
        // TODO Change this so that non players can create shops as long as they
        // send x, y, z coords
        if (canUseCommand(CommandTypes.CREATE_SHOP) && args.length == 2 && (sender instanceof Player)) {
            // command format /lshop create ShopName
            Player player = (Player) sender;
            Location location = player.getLocation();
            String shopName = args[1];

            // check to see if that shop name is already used
            Collection<Shop> shops = plugin.shopData.getAllShops();
            for (Shop shop : shops) {
                if (shop.getName().equalsIgnoreCase(shopName)) {
                    player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "Could not create shop.  " + ChatColor.WHITE + shop.getName() + ChatColor.AQUA + " already exists.");
                    return false;
                }
            }

            long x = (long) location.getX();
            long y = (long) location.getY();
            long z = (long) location.getZ();

            Shop thisShop = new Shop(UUID.randomUUID());

            thisShop.setCreator(player.getName());
            thisShop.setOwner(player.getName());
            thisShop.setName(shopName);
            thisShop.setWorld(player.getWorld().getName());

            // setup the cuboid for the tree
            long[] xyzA = new long[3];
            long[] xyzB = new long[3];

            if (plugin.playerData.containsKey(player.getName()) && plugin.playerData.get(player.getName()).isSelecting) {
                // Player has been selecting a shop -- use coords from
                // PlayerData
                if (!plugin.playerData.get(player.getName()).checkSize()) {
                    String size = plugin.shopData.maxWidth + "x" + plugin.shopData.maxHeight + "x" + plugin.shopData.maxWidth;
                    player.sendMessage(ChatColor.AQUA + "Problem with selection. Max size is " + ChatColor.WHITE + size);
                    return false;
                }
                // if a custom size had been set, use that
                PlayerData data = plugin.playerData.get(player.getName());
                xyzA = data.getPositionA();
                xyzB = data.getPositionB();

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

            thisShop.setLocations(new ShopLocation(xyzA), new ShopLocation(xyzB));

            // need to check to see if the shop overlaps another shop
            if (shopPositionOk(player, xyzA, xyzB)) {

                if (plugin.shopData.chargeForShop) {
                    if (!canUseCommand(CommandTypes.CREATE_SHOP_FREE)) {
                        if (!plugin.playerData.get(player.getName()).chargePlayer(player.getName(), plugin.shopData.shopCost)) {
                            player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "You need " + plugin.shopData.shopCost + " " + plugin.shopData.currencyName + " to create a shop.");
                            return false;
                        }
                    }
                }

                // insert the shop into the world
                LocalShops.cuboidTree.insert(thisShop.getCuboid());
                log.info(String.format("[%s] Created: %s", plugin.pdfFile.getName(), thisShop.toString()));
                plugin.shopData.addShop(thisShop);

                plugin.playerData.put(player.getName(), new PlayerData(plugin, player.getName()));

                // write the file
                if (plugin.shopData.saveShop(thisShop)) {
                    player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.WHITE + shopName + ChatColor.AQUA + " was created successfully.");
                    return true;
                } else {
                    player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "There was an error, could not create shop.");
                    return false;
                }

            }
        }
        if (args.length != 2) {
            sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "The command format is " + ChatColor.WHITE + "/"+commandLabel+" create [ShopName]");
        }
        if (!canUseCommand(CommandTypes.CREATE_SHOP)) {
            sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "You don't have permission to use this command");
        }
        return false;
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
                if (shopLocation.name == null)
                    continue;
                if (!shopLocation.world.equalsIgnoreCase(thisShop.getWorld()))
                    continue;

                LocalShops.cuboidTree.delete(shopLocation);
            }

            // need to check to see if the shop overlaps another shop
            if (shopPositionOk(player, xyzA, xyzB)) {

                PrimitiveCuboid tempShopCuboid = new PrimitiveCuboid(xyzA, xyzB);
                tempShopCuboid.name = shopName;
                tempShopCuboid.world = player.getWorld().getName();

                if (plugin.shopData.chargeForMove) {
                    if (!canUseCommand(CommandTypes.MOVE_SHOP_FREE)) {
                        if (!plugin.playerData.get(player.getName()).chargePlayer(player.getName(), plugin.shopData.shopCost)) {
                            // insert the old cuboid back into the world
                            tempShopCuboid = new PrimitiveCuboid(xyzAold, xyzBold);
                            tempShopCuboid.name = shopName;
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
                tempShopCuboid.name = shopName;
                tempShopCuboid.world = thisShop.getWorld();
                LocalShops.cuboidTree.insert(tempShopCuboid);
            }
        }
        if (args.length != 2) {
            sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "The command format is " + ChatColor.WHITE + "/"+commandLabel+" move [ShopName]");
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
            sender.sendMessage(ChatColor.WHITE + "   /"+commandLabel+" add" + ChatColor.AQUA + " - Add the item that you are holding to the shop.");
        }
        if (canUseCommand(CommandTypes.BUY_ITEM)) {
            sender.sendMessage(ChatColor.WHITE + "   /"+commandLabel+" buy [itemname] [number] " + ChatColor.AQUA + "- Buy this item.");
        }
        if (canUseCommand(CommandTypes.CREATE_SHOP)) {
            sender.sendMessage(ChatColor.WHITE + "   /"+commandLabel+" create [ShopName]" + ChatColor.AQUA + " - Create a shop at your location.");
        }
        if (canUseCommand(CommandTypes.DESTROY_SHOP)) {
            sender.sendMessage(ChatColor.WHITE + "   /"+commandLabel+" destroy" + ChatColor.AQUA + " - Destroy the shop you're in.");
        }        
        if (canUseCommand(CommandTypes.LIST)) {
            sender.sendMessage(ChatColor.WHITE + "   /"+commandLabel+" list <buy|sell> " + ChatColor.AQUA + "- List the shop's inventory.");
        }
        if (canUseCommand(CommandTypes.MOVE_SHOP)) {
            sender.sendMessage(ChatColor.WHITE + "   /"+commandLabel+" move [ShopName]" + ChatColor.AQUA + " - Move a shop to your location.");
        }
        sender.sendMessage(ChatColor.WHITE + "   /"+commandLabel+" search" + ChatColor.AQUA + " - Search for an item name.");        
        if (canUseCommand(CommandTypes.SELECT_CUBOID)) {
            sender.sendMessage(ChatColor.WHITE + "   /"+commandLabel+" select" + ChatColor.AQUA + " - Select two corners for custom shop size.");
        }        
        if (canUseCommand(CommandTypes.SELL_ITEM)) {
            sender.sendMessage(ChatColor.WHITE + "   /"+commandLabel+" sell <#|all>" + ChatColor.AQUA + " - Sell the item in your hand.");
            sender.sendMessage(ChatColor.WHITE + "   /"+commandLabel+" sell [itemname] [number]");
        }
        if (canUseCommand(CommandTypes.SET)) {
            sender.sendMessage(ChatColor.WHITE + "   /"+commandLabel+" set" + ChatColor.AQUA + " - Display list of set commands");
        }        
        if (canUseCommand(CommandTypes.REMOVE_ITEM)) {
            sender.sendMessage(ChatColor.WHITE + "   /"+commandLabel+" remove [itemname]" + ChatColor.AQUA + " - Stop selling item in shop.");
        }
        return true;
    }

    private static boolean shopPositionOk(Player player, long[] xyzA, long[] xyzB) {
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
                    if (shopOverlaps(player, res))
                        return false;
                }
            }
        }
        return true;
    }

    private static boolean shopOverlaps(Player player, BookmarkedResult res) {
        if (res.results.size() != 0) {
            for (PrimitiveCuboid shop : res.results) {
                if (shop.name != null) {
                    if (shop.world.equalsIgnoreCase(player.getWorld().getName())) {
                        player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "Could not create shop, it overlaps with " + ChatColor.WHITE
                                + shop.name);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean shopList() {
        if (canUseCommand(CommandTypes.LIST) && (sender instanceof Player)) {
            Player player = (Player) sender;
            String playerName = player.getName();
            String inShopName;

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
                inShopName = plugin.playerData.get(playerName).shopList.get(0);
                Shop shop = plugin.shopData.getShop(inShopName);

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
                player.sendMessage(ChatColor.AQUA + "You must be inside a shop to use /"+commandLabel+" list");
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
                    ChatColor.WHITE + "/"+commandLabel+" " + buySell + " ItemName [amount]";
            player.sendMessage(message);
        } else {
            player.sendMessage(ChatColor.AQUA + "Type " + ChatColor.WHITE + "/"+commandLabel+" list buy"
                    + ChatColor.AQUA + " or " + ChatColor.WHITE + "/"+commandLabel+" list sell");
            player.sendMessage(ChatColor.AQUA + "to see details about price and quantity.");
        }
    }

    /**
     * Processes sell command.
     * 
     * @param sender
     * @param args
     * @return true - if command succeeds false otherwise
     */
    public boolean shopSellItem() {
        if (!(sender instanceof Player) || !canUseCommand(CommandTypes.SELL_ITEM)) {
            sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "You don't have permission to use this command");
            return false;
        }
        if (!plugin.pluginListener.useiConomy) {
            sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "Can not complete. Can not find iConomy.");
            return false;
        }

        /*
         * Available formats: /lshop sell /lshop sell # /lshop sell all /lshop
         * sell item # /lshop sell item all
         */
        String shopName;
        Shop shop;

        Player player = (Player) sender;
        String playerName = player.getName();

        ItemStack item = player.getItemInHand();
        String itemName = null;
        int amount = item.getAmount();

        // get the shop the player is currently in
        if (plugin.playerData.get(playerName).shopList.size() == 1) {
            shopName = plugin.playerData.get(playerName).shopList.get(0);
            shop = plugin.shopData.getShop(shopName);
        } else {
            player.sendMessage(ChatColor.AQUA + "You must be inside a shop to use /"+commandLabel+" " + args[0]);
            return false;
        }

        if (args.length == 1) {
            // /lshop sell
        } else if (args.length == 2) {
            /*
             * /lshop sell # /lshop sell all /lshop sell item
             */
            if (!args[1].equalsIgnoreCase("all")) {
                try {
                    amount = Integer.parseInt(args[1]);
                } catch (NumberFormatException ex1) {
                    item = LocalShops.itemList.getShopItem(player, shop, args[1]);
                    itemName = null;
                }
            }
        } else if (args.length == 3) {
            /*
             * /lshop sell item # /lshop sell item all
             */
            item = LocalShops.itemList.getShopItem(player, shop, args[1]);
            itemName = null;
            if (!args[2].equalsIgnoreCase("all")) {
                try {
                    amount = Integer.parseInt(args[2]);
                } catch (NumberFormatException ex1) {
                    itemName = null;
                }
            }
        } else {
            item = null;
            itemName = null;
        }

        if (item == null && itemName == null) {
            player.sendMessage(ChatColor.AQUA + "Input problem. The format is " + ChatColor.WHITE + "/"+commandLabel+" sell <itemName> <# to sell>");
            return false;
        }

        if (item == null && itemName != null) {
            item = LocalShops.itemList.getItem(player, args[1]);
            if (item == null) {
                player.sendMessage(ChatColor.AQUA + "Could not add the item to shop.");
                return false;
            }
        } else if (item != null && itemName == null) {
            itemName = LocalShops.itemList.getItemName(item.getType().getId(), (int) item.getDurability());
            if (itemName == null) {
                sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "Item " + ChatColor.WHITE + item.getType().toString() + ChatColor.AQUA + " can not be added to the shop.");
                System.out.println("LocalShops: " + player.getName() + " tried to add " + item.getType().toString() + " but it's not in the item list.");
                return false;
            }
        }

        // check if the shop is buying that item
        if (!shop.getItems().contains(itemName) || shop.getItem(itemName).getSellPrice() == 0) {
            player.sendMessage(ChatColor.AQUA + "Sorry, " + ChatColor.WHITE + shopName + ChatColor.AQUA + " is not buying " + ChatColor.WHITE + itemName + ChatColor.AQUA + " right now.");
            return false;
        }

        // check how many items the player has
        int playerInventory = countItemsinInventory(player.getInventory(), item);
        if (amount < 0)
            amount = 0;
        if (args.length == 2) {
            if (args[1].equalsIgnoreCase("all")) {
                amount = playerInventory;
            }
        } else if (args.length == 3) {
            if (args[2].equalsIgnoreCase("all")) {
                amount = playerInventory;
            }
        }

        // check if the amount to add is okay
        if (amount > playerInventory) {
            player.sendMessage(ChatColor.AQUA + "You only have " + ChatColor.WHITE + playerInventory
                    + ChatColor.AQUA + " in your inventory that can be added.");
            amount = playerInventory;
        }

        // check if the shop has a max stock level set
        if (shop.itemMaxStock(itemName) != 0 && !shop.isUnlimitedStock()) {
            if (shop.getItem(itemName).getStock() >= shop.itemMaxStock(itemName)) {
                player.sendMessage(ChatColor.AQUA + "Sorry, " + ChatColor.WHITE + shopName + ChatColor.AQUA + " is not buying any more " + ChatColor.WHITE + itemName + ChatColor.AQUA + " right now.");
                return false;
            }

            if (amount > (shop.itemMaxStock(itemName) - shop.getItem(itemName).getStock())) {
                amount = shop.itemMaxStock(itemName) - shop.getItem(itemName).getStock();
            }
        }

        // calculate cost
        int bundles = amount / shop.getItem(itemName).getSellSize();

        if (bundles == 0 && amount > 0) {
            player.sendMessage(ChatColor.AQUA + "The minimum number to sell is  " + ChatColor.WHITE + shop.getItem(itemName).getSellSize());
            return false;
        }

        int itemPrice = shop.getItem(itemName).getSellPrice();
        // recalculate # of items since may not fit cleanly into bundles
        // notify player if there is a change
        if (amount % shop.getItem(itemName).getSellSize() != 0) {
            player.sendMessage(ChatColor.AQUA + "The bundle size is  " + ChatColor.WHITE + shop.getItem(itemName).getBuySize() + ChatColor.AQUA + " order reduced to " + ChatColor.WHITE + bundles * shop.getItem(itemName).getSellSize());
        }
        amount = bundles * shop.getItem(itemName).getSellSize();
        int totalCost = bundles * itemPrice;

        // try to pay the player for order
        if (shop.isUnlimitedMoney()) {
            plugin.playerData.get(playerName).payPlayer(playerName, totalCost);
        } else {
            if (!isShopController(player, shop)) {
                if (!plugin.playerData.get(playerName).payPlayer(shop.getOwner(), playerName, totalCost)) {
                    // lshop owner doesn't have enough money
                    // get shop owner's balance and calculate how many it can
                    // buy
                    long shopBalance = plugin.playerData.get(playerName).getBalance(shop.getOwner());
                    int bundlesCanAford = (int) shopBalance / itemPrice;
                    totalCost = bundlesCanAford * itemPrice;
                    amount = bundlesCanAford * shop.getItem(itemName).getSellSize();
                    player.sendMessage(ChatColor.AQUA + "The shop could only afford " + ChatColor.WHITE + amount);
                    if (!plugin.playerData.get(playerName).payPlayer(shop.getOwner(), playerName, totalCost)) {
                        player.sendMessage(ChatColor.AQUA + "Unexpected money problem: could not complete sale.");
                        return false;
                    }
                }
            }
        }

        if (!shop.isUnlimitedStock()) {
            shop.addStock(itemName, amount);
        }

        if (isShopController(player, shop)) {
            player.sendMessage(ChatColor.AQUA + "You added " + ChatColor.WHITE + amount + " " + itemName + ChatColor.AQUA + " to the shop");
        } else {
            player.sendMessage(ChatColor.AQUA + "You sold " + ChatColor.WHITE + amount + " " + itemName + ChatColor.AQUA + " and gained " + ChatColor.WHITE + totalCost + " " + plugin.shopData.currencyName);
        }

        // log the transaction
        int itemInv = shop.getItem(itemName).getStock();
        int startInv = itemInv - amount;
        if (startInv < 0)
            startInv = 0;
        plugin.shopData.logItems(playerName, shopName, "sell-item", itemName, amount, startInv, itemInv);

        removeItemsFromInventory(player.getInventory(), item, amount);
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
    public boolean shopAddItem() {
        if (sender instanceof Player) {
            // Player has sent command
            Player player = (Player) sender;

            // Check Permissions
            if (!canUseCommand(CommandTypes.ADD_ITEM)) {
                sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "You don't have permission to use this command");
                return false;
            }

            // Variables
            ItemInfo item = null;
            int amount = 0;
            PlayerData pData = plugin.playerData.get(player.getName());
            Shop shop = null;

            // Parse Arguments
            if (args.length == 1) {
                // shop add
                ItemStack itemStack = player.getItemInHand();
                if (itemStack == null) {
                    return false;
                }
                amount = itemStack.getAmount();
                item = Search.itemById(itemStack.getTypeId(), itemStack.getDurability());
            } else if (args.length > 1) {
                if (args[1].matches("(?i)all") && args.length == 2) {
                    // shop add all
                } else if (args[1].matches("\\d+")) {
                    // shop add id [count]
                    if (args.length == 3) {
                        // shop add id count
                        if (args[2].matches("(?i)all")) {
                            amount = -1;
                        } else {
                            amount = Integer.parseInt(args[2]);
                        }
                        int itemId = Integer.parseInt(args[1]);
                        item = Search.itemById(itemId);
                    } else {
                        // shop add id
                        int itemId = Integer.parseInt(args[1]);
                        item = Search.itemById(itemId);
                    }
                } else if (args[1].matches("\\d+:\\d+")) {
                    // shop add id:type [count]
                    if (args.length == 3) {
                        // shop add id:type count
                        if (args[2].matches("(?i)all")) {
                            amount = -1;
                        } else {
                            amount = Integer.parseInt(args[2]);
                        }
                        int itemId = Integer.parseInt(args[1].split(":")[0]);
                        short typeId = Short.parseShort(args[1].split(":")[1]);
                        item = Search.itemById(itemId, typeId);
                    } else {
                        // shop add id:type
                        int itemId = Integer.parseInt(args[1].split(":")[0]);
                        short typeId = Short.parseShort(args[1].split(":")[1]);
                        item = Search.itemById(itemId, typeId);
                    }
                } else {
                    // shop add name, ... [count]
                    if (args[args.length - 1].matches("\\d+")) {
                        // Last element is an integer
                        if (args[args.length - 1].matches("(?i)all")) {
                            amount = -1;
                        } else {
                            amount = Integer.parseInt(args[args.length - 1]);
                        }
                        ArrayList<String> itemName = new ArrayList<String>();
                        for (int i = 1; i < args.length - 1; i++) {
                            itemName.add(args[i]);
                        }
                        item = Search.itemByName(itemName);
                    } else {
                        // All string data
                        ArrayList<String> itemName = new ArrayList<String>();
                        for (int i = 1; i < args.length; i++) {
                            itemName.add(args[i]);
                        }
                        item = Search.itemByName(itemName);
                    }
                }
            }

            if (amount < -1 || item == null) {
                return false;
            }

            // Get current shop
            String currShop = pData.getCurrentShop();
            if (currShop != null) {
                shop = plugin.shopData.getShop(currShop);
            }
            if (shop == null) {
                return false;
            }

            // Calculate number of items player has
            int playerItemCount = countItemsinInventory(player.getInventory(), item.toStack());

            // Validate Count
            if (playerItemCount >= amount) {
                // Perform add
                log.info(String.format("Add %d of %s to %s", amount, item, shop));
            } else {
                // Nag player
                player.sendMessage(ChatColor.AQUA + "You only have " + ChatColor.WHITE + playerItemCount + ChatColor.AQUA + " in your inventory that can be added.");
                // amount = playerItemCount;
                return false;
            }

            // If ALL (amount == -1), set ammount to the count the player has
            if (amount == -1) {
                amount = playerItemCount;
            }

            // Check Shop Contents, add if necessary
            if(amount == 0 & shop.containsItem(item)) {
                // nicely message user
                player.sendMessage(String.format("This shop already carries %s!", item.name));
                return true;
            }
            
            // Add item to shop if needed
            if (!shop.containsItem(item)) {
                shop.addItem(item.typeId, item.subTypeId, 0, 1, 0, 1, 0, 0);
            }

            // Check stock settings, add stock if necessary
            if (shop.isUnlimitedStock()) {
                player.sendMessage(ChatColor.AQUA + "Succesfully added " + ChatColor.WHITE + item.name + ChatColor.AQUA + " to the shop.");
            } else {
                shop.addStock(item.name, amount);
                player.sendMessage(ChatColor.AQUA + "Succesfully added " + ChatColor.WHITE + item.name + ChatColor.AQUA + " to the shop. Stock is now " + ChatColor.WHITE + shop.getItem(item.name).getStock());
            }

            // log the transaction
            int itemInv = shop.getItem(item.name).getStock();
            int startInv = itemInv - amount;
            if (startInv < 0) {
                startInv = 0;
            }
            plugin.shopData.logItems(player.getName(), shop.getName(), "add-item", item.name, amount, startInv, itemInv);

            // take items from player
            removeItemsFromInventory(player.getInventory(), item.toStack(), amount);
            plugin.shopData.saveShop(shop);
        } else {
            // Console or other has sent command
            // TODO: Determine command syntax!
        }
        return false;
    }

    /**
     * Returns true if the player is in the shop manager list or is the shop
     * owner
     * 
     * @param player
     * @param shop
     * @return
     */
    private boolean isShopController(Player player, Shop shop) {
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
    }

    /**
     * Processes buy command.
     * 
     * @param sender
     * @param args
     * @return true - if command succeeds false otherwise
     */
    public boolean shopBuyItem() {
        if (!(sender instanceof Player) || !canUseCommand(CommandTypes.BUY_ITEM)) {
            sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "You don't have permission to use this command");
            return false;
        }
        if (!plugin.pluginListener.useiConomy) {
            sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "Can not complete. Can not find iConomy.");
            return false;
        }

        /*
         * Available formats: /lshop buy item #
         */

        Player player = (Player) sender;
        String playerName = player.getName();

        // get the shop the player is currently in
        if (plugin.playerData.get(playerName).shopList.size() == 1) {
            String shopName = plugin.playerData.get(playerName).shopList.get(0);
            Shop shop = plugin.shopData.getShop(shopName);

            ItemStack item = null;
            String itemName = null;
            int amount = 0;

            if (args.length == 3) {
                item = LocalShops.itemList.getShopItem(player, shop, args[1]);
                if (item == null) {
                    player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "Could not complete the purchase.");
                    return false;
                } else {
                    int itemData = item.getDurability();
                    itemName = LocalShops.itemList.getItemName(item.getTypeId(), itemData);
                }

                // check if the shop is selling that item
                if (!shop.getItems().contains(itemName)) {
                    player.sendMessage(ChatColor.AQUA + "Sorry, " + ChatColor.WHITE + shopName
                            + ChatColor.AQUA + " is not selling " + ChatColor.WHITE + itemName
                            + ChatColor.AQUA + " right now.");
                    return false;
                }

                // check if the item has a price, or if this is a shop owner
                if (shop.getItem(itemName).getBuyPrice() == 0 && !isShopController(player, shop)) {
                    player.sendMessage(ChatColor.AQUA + "Sorry, " + ChatColor.WHITE + shopName + ChatColor.AQUA + " is not selling " + ChatColor.WHITE + itemName + ChatColor.AQUA + " right now.");
                    return false;
                }

                int totalAmount;
                totalAmount = shop.getItem(itemName).getStock();

                if (totalAmount == 0 && !shop.isUnlimitedStock()) {
                    player.sendMessage(ChatColor.AQUA + "The shop has " + ChatColor.WHITE + totalAmount + " " + itemName);
                    return true;
                }

                try {
                    int numberToRemove = Integer.parseInt(args[2]);
                    if (numberToRemove < 0)
                        numberToRemove = 0;
                    if (shop.isUnlimitedStock()) {
                        totalAmount = numberToRemove;
                    }
                    if (numberToRemove > totalAmount) {
                        amount = totalAmount - (totalAmount % shop.getItem(itemName).getBuySize());
                        if (!shop.isUnlimitedStock()) {
                            player.sendMessage(ChatColor.AQUA + "The shop has " + ChatColor.WHITE + totalAmount + " " + itemName);
                        }
                    } else {
                        amount = numberToRemove - (numberToRemove % shop.getItem(itemName).getBuySize());
                    }

                    if (amount % shop.getItem(itemName).getBuySize() != 0) {
                        player.sendMessage(ChatColor.AQUA + "The bundle size is  " + ChatColor.WHITE + shop.getItem(itemName).getBuySize() + ChatColor.AQUA + " order reduced to " + ChatColor.WHITE + (int) (amount / shop.getItem(itemName).getBuySize()));
                    }
                } catch (NumberFormatException ex2) {
                    if (args[2].equalsIgnoreCase("all")) {
                        amount = totalAmount;
                    } else {
                        player.sendMessage(ChatColor.AQUA + "Input problem. The format is " + ChatColor.WHITE + "/"+commandLabel+" buy <itemName> <# to buy>");
                        return false;
                    }
                }

            } else {
                player.sendMessage(ChatColor.AQUA + "Input problem. The format is " + ChatColor.WHITE + "/"+commandLabel+" buy <itemName> <# to buy>");
                return false;
            }

            // check how many items the user has room for
            int freeSpots = 0;
            for (ItemStack thisSlot : player.getInventory().getContents()) {
                if (thisSlot == null || thisSlot.getType() == Material.AIR) {
                    freeSpots += 64;
                    continue;
                }
                if (thisSlot.getTypeId() == item.getTypeId() && thisSlot.getDurability() == item.getDurability()) {
                    freeSpots += 64 - thisSlot.getAmount();
                }
            }

            if (amount > freeSpots) {
                player.sendMessage(ChatColor.AQUA + "You only have room for " + ChatColor.WHITE + freeSpots);
                amount = freeSpots;
            }

            // calculate cost
            int bundles = amount / shop.getItem(itemName).getBuySize();
            int itemPrice = shop.getItem(itemName).getBuyPrice();
            // recalculate # of items since may not fit cleanly into bundles
            amount = bundles * shop.getItem(itemName).getBuySize();
            int totalCost = bundles * itemPrice;

            // try to pay the shop owner
            if (!isShopController(player, shop)) {
                if (!plugin.playerData.get(playerName).payPlayer(playerName, shop.getOwner(), totalCost)) {
                    // player doesn't have enough money
                    // get player's balance and calculate how many it can buy
                    long playerBalance = plugin.playerData.get(playerName).getBalance(playerName);
                    int bundlesCanAford = (int) Math.floor(playerBalance / itemPrice);
                    totalCost = bundlesCanAford * itemPrice;
                    amount = bundlesCanAford * shop.getItem(itemName).getSellSize();
                    player.sendMessage(ChatColor.AQUA + "You could only afford " + ChatColor.WHITE + amount);

                    if (!plugin.playerData.get(playerName).payPlayer(playerName, shop.getOwner(), totalCost)) {
                        player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "Unexpected money problem: could not complete sale.");
                        return false;
                    }
                }
            }

            if (!shop.isUnlimitedStock()) {
                shop.removeStock(itemName, amount);
            }
            if (isShopController(player, shop)) {
                player.sendMessage(ChatColor.AQUA + "You removed " + ChatColor.WHITE + amount + " " + itemName + ChatColor.AQUA + " from the shop");
            } else {
                player.sendMessage(ChatColor.AQUA + "You purchased " + ChatColor.WHITE + amount + " " + itemName + ChatColor.AQUA + " for " + ChatColor.WHITE + totalCost + " " + plugin.shopData.currencyName);
            }

            // log the transaction
            int itemInv = shop.getItem(itemName).getStock();
            int startInv = itemInv + amount;
            if (shop.isUnlimitedStock())
                startInv = 0;
            plugin.shopData.logItems(playerName, shopName, "buy-item", itemName, amount, startInv, itemInv);

            givePlayerItem(player, item, amount);
            plugin.shopData.saveShop(shop);

        } else {
            player.sendMessage(ChatColor.AQUA + "You must be inside a shop to use /"+commandLabel+" " + args[0]);
        }

        return true;
    }

    public boolean shopSetItem() {
        if (sender instanceof Player) {
            // Player has sent command
            Player player = (Player) sender;

            // Check Permissions
            if (!canUseCommand(CommandTypes.SET)) {
                sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "You don't have permission to use this command");
                return false;
            }

            // Variables

            // Check minimum variable length
            if (args.length < 2) {
                // Display list of set commands & return
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
                return true;
            }

            // Get current shop
            Shop shop = null;
            PlayerData pData = plugin.playerData.get(player.getName());
            String currShop = pData.getCurrentShop();
            if (currShop != null) {
                shop = plugin.shopData.getShop(currShop);
            }
            if (shop == null) {
                return false;
            }

            // Parse Arguments
            if (args[1].matches("(?i)buy")) {
                // shop set buy itemName price stacksize
                if (args[2].matches("\\d+")) {
                    if (args.length == 4) {
                        // shop set buy itemid price
                        int id = Integer.parseInt(args[2]);
                        int price = Integer.parseInt(args[3]);

                        // Search for Item
                        ItemInfo item = Search.itemById(id);
                        if (item == null) {
                            player.sendMessage("Item was not found.");
                            return true;
                        }
                        
                        // Warn about negative items
                        if(price < 0) {
                            player.sendMessage("[WARNING] This shop will loose money with negative values!");
                        }

                        // Set new values
                        shop.setItemBuyPrice(item.name, price);
                        
                        // Save Shop
                        plugin.shopData.saveShop(shop);
                        
                        return true;
                    } else if (args.length == 5) {
                        // shop set buy itemid price stacksize
                        int id = Integer.parseInt(args[2]);
                        int price = Integer.parseInt(args[3]);
                        int size = Integer.parseInt(args[4]);

                        // Search for Item
                        ItemInfo item = Search.itemById(id);
                        if (item == null) {
                            player.sendMessage("Item was not found.");
                            return true;
                        }
                        
                        // Warn about negative items
                        if(price < 0) {
                            player.sendMessage("[WARNING] This shop will loose money with negative values!");
                        }
                        if(size < 0) {
                            player.sendMessage("[ERROR] Stacks cannot be negative!");
                            return true;
                        }

                        // Set new values
                        shop.setItemBuyAmount(item.name, size);
                        shop.setItemBuyPrice(item.name, price);
                        
                        // Save Shop
                        plugin.shopData.saveShop(shop);
                        
                        return true;
                    } else {
                        return false;
                    }
                } else if (args[2].matches("\\d+:\\d+")) {
                    // shop set buy id:type price stacksize
                    if (args.length == 4) {
                        // shop set buy id:type price
                        int id = Integer.parseInt(args[2].split(":")[0]);
                        short type = Short.parseShort(args[2].split(":")[1]);
                        int price = Integer.parseInt(args[3]);

                        // Search for Item
                        ItemInfo item = Search.itemById(id, type);
                        if (item == null) {
                            player.sendMessage("Item was not found.");
                            return true;
                        }
                        
                        // Warn about negative items
                        if(price < 0) {
                            player.sendMessage("[WARNING] This shop will loose money with negative values!");
                        }
                        
                        // Set new values
                        shop.setItemBuyPrice(item.name, price);
                        
                        // Save Shop
                        plugin.shopData.saveShop(shop);
                        
                        return true;
                    } else if (args.length == 5) {
                        // shop set buy id:type price stacksize
                        int id = Integer.parseInt(args[2].split(":")[0]);
                        short type = Short.parseShort(args[2].split(":")[1]);
                        int price = Integer.parseInt(args[3]);
                        int size = Integer.parseInt(args[4]);

                        // Search for Item
                        ItemInfo item = Search.itemById(id, type);
                        if (item == null) {
                            player.sendMessage("Item was not found.");
                            return true;
                        }
                        
                        // Warn about negative items
                        if(price < 0) {
                            player.sendMessage("[WARNING] This shop will loose money with negative values!");
                        }
                        if(size < 0) {
                            player.sendMessage("[ERROR] Stacks cannot be negative!");
                            return true;
                        }

                        // Set new values
                        shop.setItemBuyPrice(item.name, price);
                        shop.setItemBuyAmount(item.name, size);
                        
                        // Save Shop
                        plugin.shopData.saveShop(shop);
                        
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    // shop set words... price stacksize
                    if (args[args.length - 1].matches("\\d+") && args[args.length - 2].matches("\\d+")) {
                        // shop set buy name... price stacksize
                        int price = Integer.parseInt(args[args.length - 2]);
                        int size = Integer.parseInt(args[args.length - 1]);

                        // Search for Item
                        ArrayList<String> itemName = new ArrayList<String>();
                        for (int i = 1; i < args.length - 2; i++) {
                            itemName.add(args[i]);
                        }
                        ItemInfo item = Search.itemByName(itemName);
                        if (item == null) {
                            player.sendMessage("Item was not found.");
                            return true;
                        }
                        
                        // Warn about negative items
                        if(price < 0) {
                            player.sendMessage("[WARNING] This shop will loose money with negative values!");
                        }
                        if(size < 0) {
                            player.sendMessage("[ERROR] Stacks cannot be negative!");
                            return true;
                        }

                        // Set new values
                        shop.setItemBuyPrice(item.name, price);
                        shop.setItemBuyAmount(item.name, size);
                        
                        // Save Shop
                        plugin.shopData.saveShop(shop);
                        
                        return true;
                    } else if (args[args.length - 1].matches("\\d+")) {
                        // shop set buy name... price
                        int price = Integer.parseInt(args[args.length - 1]);

                        // Search for Item
                        ArrayList<String> itemName = new ArrayList<String>();
                        for (int i = 1; i < args.length - 1; i++) {
                            itemName.add(args[i]);
                        }
                        ItemInfo item = Search.itemByName(itemName);
                        if (item == null) {
                            player.sendMessage("Item was not found.");
                            return true;
                        }
                        
                        // Warn about negative items
                        if(price < 0) {
                            player.sendMessage("[WARNING] This shop will loose money with negative values!");
                        }

                        // Set new values
                        shop.setItemBuyPrice(item.name, price);
                        
                        // Save Shop
                        plugin.shopData.saveShop(shop);
                        
                        return true;
                    } else {
                        return false;
                    }
                }
            } else if (args[1].matches("(?i)sell")) {
                // shop set sell itemname price stacksize
                if (args[2].matches("\\d+")) {
                    if (args.length == 4) {
                        // shop set sell itemid price
                        int id = Integer.parseInt(args[2]);
                        int price = Integer.parseInt(args[3]);

                        // Search for Item
                        ItemInfo item = Search.itemById(id);
                        if (item == null) {
                            player.sendMessage("Item was not found.");
                            return true;
                        }
                        
                        // Warn about negative items
                        if(price < 0) {
                            player.sendMessage("[WARNING] This shop will loose money with negative values!");
                        }

                        // Set new values
                        shop.setItemSellPrice(item.name, price);
                        
                        // Save Shop
                        plugin.shopData.saveShop(shop);
                        
                        return true;
                    } else if (args.length == 5) {
                        // shop set sell itemid price stacksize
                        int id = Integer.parseInt(args[2]);
                        int price = Integer.parseInt(args[3]);
                        int size = Integer.parseInt(args[4]);

                        // Search for Item
                        ItemInfo item = Search.itemById(id);
                        if (item == null) {
                            player.sendMessage("Item was not found.");
                            return true;
                        }
                        
                        // Warn about negative items
                        if(price < 0) {
                            player.sendMessage("[WARNING] This shop will loose money with negative values!");
                        }
                        if(size < 0) {
                            player.sendMessage("[ERROR] Stacks cannot be negative!");
                            return true;
                        }

                        // Set new values
                        shop.setItemSellAmount(item.name, size);
                        shop.setItemSellPrice(item.name, price);
                        
                        // Save Shop
                        plugin.shopData.saveShop(shop);
                        
                        return true;
                    } else {
                        return false;
                    }
                } else if (args[2].matches("\\d+:\\d+")) {
                    // shop set sell id:type price stacksize
                    if (args.length == 4) {
                        // shop set sell id:type price
                        int id = Integer.parseInt(args[2].split(":")[0]);
                        short type = Short.parseShort(args[2].split(":")[1]);
                        int price = Integer.parseInt(args[3]);

                        // Search for Item
                        ItemInfo item = Search.itemById(id, type);
                        if (item == null) {
                            player.sendMessage("Item was not found.");
                            return true;
                        }
                        
                        // Warn about negative items
                        if(price < 0) {
                            player.sendMessage("[WARNING] This shop will loose money with negative values!");
                        }

                        // Set new values
                        shop.setItemSellPrice(item.name, price);
                        
                        // Save Shop
                        plugin.shopData.saveShop(shop);
                        
                        return true;
                    } else if (args.length == 5) {
                        // shop set sell id:type price stacksize
                        int id = Integer.parseInt(args[2].split(":")[0]);
                        short type = Short.parseShort(args[2].split(":")[1]);
                        int price = Integer.parseInt(args[3]);
                        int size = Integer.parseInt(args[4]);

                        // Search for Item
                        ItemInfo item = Search.itemById(id, type);
                        if (item == null) {
                            player.sendMessage("Item was not found.");
                            return true;
                        }
                        
                        // Warn about negative items
                        if(price < 0) {
                            player.sendMessage("[WARNING] This shop will loose money with negative values!");
                        }
                        if(size < 0) {
                            player.sendMessage("[ERROR] Stacks cannot be negative!");
                            return true;
                        }

                        // Set new values
                        shop.setItemSellPrice(item.name, price);
                        shop.setItemSellAmount(item.name, size);
                        
                        // Save Shop
                        plugin.shopData.saveShop(shop);
                        
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    // shop set sell words... price stacksize
                    if (args[args.length - 1].matches("\\d+") && args[args.length - 2].matches("\\d+")) {
                        // shop set sell name... price stacksize
                        int price = Integer.parseInt(args[args.length - 2]);
                        int size = Integer.parseInt(args[args.length - 1]);

                        // Search for Item
                        ArrayList<String> itemName = new ArrayList<String>();
                        for (int i = 1; i < args.length - 2; i++) {
                            itemName.add(args[i]);
                        }
                        ItemInfo item = Search.itemByName(itemName);
                        if (item == null) {
                            player.sendMessage("Item was not found.");
                            return true;
                        }
                        
                        // Warn about negative items
                        if(price < 0) {
                            player.sendMessage("[WARNING] This shop will loose money with negative values!");
                        }
                        if(size < 0) {
                            player.sendMessage("[ERROR] Stacks cannot be negative!");
                            return true;
                        }

                        // Set new values
                        shop.setItemSellPrice(item.name, price);
                        shop.setItemSellAmount(item.name, size);
                        
                        // Save Shop
                        plugin.shopData.saveShop(shop);
                        
                        return true;
                    } else if (args[args.length - 1].matches("\\d+")) {
                        // shop set sell name... price
                        int price = Integer.parseInt(args[args.length - 1]);

                        // Search for Item
                        ArrayList<String> itemName = new ArrayList<String>();
                        for (int i = 1; i < args.length - 1; i++) {
                            itemName.add(args[i]);
                        }
                        ItemInfo item = Search.itemByName(itemName);
                        if (item == null) {
                            player.sendMessage("Item was not found.");
                            return true;
                        }
                        
                        // Warn about negative items
                        if(price < 0) {
                            player.sendMessage("[WARNING] This shop will loose money with negative values!");
                        }

                        // Set new values
                        shop.setItemSellPrice(item.name, price);
                        
                        // Save Shop
                        plugin.shopData.saveShop(shop);
                        
                        return true;
                    } else {
                        return false;
                    }
                }
            } else if (args[1].matches("(?i)max")) {
                // shop set max itemname [amount|all]
                if (args[args.length - 1].matches("\\d+")) {
                    // shop set max itemname integer
                    int amount = Integer.parseInt(args[args.length-1]);

                    // Search for Item
                    ArrayList<String> itemName = new ArrayList<String>();
                    for (int i = 1; i < args.length - 1; i++) {
                        itemName.add(args[i]);
                    }
                    ItemInfo item = Search.itemByName(itemName);
                    if (item == null) {
                        player.sendMessage("Item was not found.");
                        return true;
                    }
                    
                    // Check negative values
                    if(amount < 0) {
                        player.sendMessage("Only positive values allowed");
                        return true;
                    }
                    
                    // Set new values
                    shop.setItemMaxStock(item.name, amount);
                    
                    // Save Shop
                    plugin.shopData.saveShop(shop);
                    
                    // Send Message
                    player.sendMessage(item.name + " maximum stock is now " + amount);
                    
                    return true;
                } else if(args[args.length -1].matches("(?i)all")) {
                    // shop set max itemname all
                    
                    // Search for Item
                    ArrayList<String> itemName = new ArrayList<String>();
                    for (int i = 1; i < args.length - 1; i++) {
                        itemName.add(args[i]);
                    }
                    ItemInfo item = Search.itemByName(itemName);
                    if (item == null) {
                        player.sendMessage("Item was not found.");
                        return true;
                    }
                    
                    // Get ammount of item
                    int amount = countItemsinInventory(player.getInventory(), item.toStack());
                    
                    // Check negative values
                    if(amount < 0) {
                        player.sendMessage("How do you have negative amounts of an item!?");
                        return true;
                    }
                    
                    // Set new values
                    shop.setItemMaxStock(item.name, amount);
                    
                    // Save Shop
                    plugin.shopData.saveShop(shop);
                    
                    // Send Message
                    player.sendMessage(item.name + " maximum stock is now " + amount);
                    
                    return true;
                } else {
                    // syntax error
                    player.sendMessage("Bad formatting");
                    return false;
                }
            } else if (args[1].matches("(?i)unlimited")) {
                if (!canUseCommand(CommandTypes.ADMIN)) {
                    player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "You must be a shop admin to do this.");
                    return false;
                }
                
                if (args.length == 3) {
                    if (args[2].matches("(?i)money")) {
                        shop.setUnlimitedMoney(!shop.isUnlimitedMoney());
                        player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "Unlimited money was set to " + ChatColor.WHITE + shop.isUnlimitedMoney());
                        plugin.shopData.saveShop(shop);
                        return true;
                    } else if (args[2].matches("(?i)stock")) {
                        shop.setUnlimitedStock(!shop.isUnlimitedStock());
                        player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "Unlimited stock was set to " + ChatColor.WHITE + shop.isUnlimitedStock());
                        plugin.shopData.saveShop(shop);
                        return true;
                    }
                }
                
                player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "The following set commands are available: ");
                player.sendMessage("   " + "/" + commandLabel + " set unlimited money");
                player.sendMessage("   " + "/" + commandLabel + " set unlimited stock");
                return true;
            } else if (args[1].matches("(?i)manager")) {
                // shop set manager +managername -managername
                // Check if Owner
                if (!shop.getOwner().equalsIgnoreCase(player.getName())) {
                    player.sendMessage(ChatColor.AQUA + "You must be the shop owner to set this.");
                    player.sendMessage(ChatColor.AQUA + "The current shop owner is " + ChatColor.WHITE + shop.getOwner());
                    return false;
                }
                
                String[] managers = shop.getManagers();
                
                for(int i=2; i < args.length; i++) {
                    String arg = args[i];
                    if(arg.matches("^\\+")) {
                        // add manager
                        shop.addManager(arg.replaceFirst("\\+", ""));
                    } else if(arg.matches("^\\-")) {
                        // remove manager
                        shop.removeManager(arg.replaceFirst("\\-", ""));
                    }
                }
                
                // Save Shop
                plugin.shopData.saveShop(shop);

                player.sendMessage(ChatColor.AQUA + "The shop managers have been updated. The current managers are:");
                player.sendMessage("   " + Arrays.toString(shop.getManagers()));
                
                return true;
                
            } else if (args[1].matches("(?i)owner")) {
                // shop set owner ownername

                // Check Permissions
                if (!canUseCommand(CommandTypes.SET_OWNER)) {
                    sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "You don't have permission to use this command");
                    return false;
                }

                if (args.length == 3) {
                    if (!shop.getOwner().equalsIgnoreCase(player.getName())) {
                        player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "You must be the shop owner to set this.");
                        player.sendMessage(ChatColor.AQUA + "  The current shop owner is " + ChatColor.WHITE + shop.getOwner());
                        return false;
                    } else if (!canUseCommand(CommandTypes.SET_OWNER)) {
                        player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "You do not have permission to do this.");
                        return false;
                    } else {
                        shop.setOwner(args[2]);
                        
                        // Save Shop
                        plugin.shopData.saveShop(shop);
                        
                        player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "Shop owner changed to " + ChatColor.WHITE + args[2]);
                        return true;
                    }
                }
            }

            return true;

        } else {
            // Console or other has sent command
            // TODO: Determine command syntax!
        }

        return false;
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
            String shopName = plugin.playerData.get(playerName).shopList.get(0);
            Shop shop = plugin.shopData.getShop(shopName);

            if (!isShopController(player, shop)) {
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
                        player.sendMessage(ChatColor.AQUA + "To add the item use " + ChatColor.WHITE + "/"+commandLabel+" add");
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
                        player.sendMessage(ChatColor.AQUA + "The command format is " + ChatColor.WHITE + "/"+commandLabel+" set buy [item name] [price] <bundle size>");
                        return false;
                    }

                    shop.setItemBuyPrice(itemName, price);
                    shop.setItemBuyAmount(itemName, bundle);

                    player.sendMessage(ChatColor.AQUA + "The buy information for " + ChatColor.WHITE + itemName + ChatColor.AQUA + " has been updated.");
                    player.sendMessage("   " + ChatColor.WHITE + itemName + ChatColor.AQUA + " [" + ChatColor.WHITE + price + " " + plugin.shopData.currencyName + ChatColor.AQUA + "] [" + ChatColor.WHITE + "Bundle: " + bundle + ChatColor.AQUA + "]");

                    plugin.shopData.saveShop(shop);

                } else {
                    player.sendMessage(ChatColor.AQUA + "The command format is " + ChatColor.WHITE + "/"+commandLabel+" set buy [item name] [price] <bundle size>");
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
                        player.sendMessage(ChatColor.AQUA + "To add the item use " + ChatColor.WHITE + "/"+commandLabel+" add");
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
                        player.sendMessage(ChatColor.AQUA + "The command format is " + ChatColor.WHITE + "/"+commandLabel+" set sell [item name] [price] <bundle size>");
                        return false;
                    }

                    shop.getItem(itemName).setSellPrice(price);
                    shop.getItem(itemName).setSellSize(bundle);

                    plugin.shopData.saveShop(shop);

                    player.sendMessage(ChatColor.AQUA + "The sell information for " + ChatColor.WHITE + itemName + ChatColor.AQUA + " has been updated.");
                    player.sendMessage("   " + ChatColor.WHITE + itemName + ChatColor.AQUA + " [" + ChatColor.WHITE + price + " " + plugin.shopData.currencyName + ChatColor.AQUA + "] [" + ChatColor.WHITE + "Bundle: " + bundle + ChatColor.AQUA + "]");

                } else {
                    player.sendMessage(ChatColor.AQUA + "The command format is " + ChatColor.WHITE + "/"+commandLabel+" set sell [item name] [price] <bundle size>");
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
                        player.sendMessage(ChatColor.AQUA + "To add the item use " + ChatColor.WHITE + "/"+commandLabel+" add");
                        return false;
                    }

                    try {
                        maxStock = Integer.parseInt(args[3]);
                    } catch (NumberFormatException ex1) {
                        player.sendMessage(ChatColor.AQUA + "The price and bundle size must be a number.");
                        player.sendMessage(ChatColor.AQUA + "The command format is " + ChatColor.WHITE + "/"+commandLabel+" set sell [item name] [price] <bundle size>");
                        return false;
                    }

                    shop.setItemMaxStock(itemName, maxStock);

                    plugin.shopData.saveShop(shop);

                    player.sendMessage(ChatColor.AQUA + "The max stock level for " + ChatColor.WHITE + itemName
                            + ChatColor.AQUA + " has been changed to " + maxStock + ".");

                } else {
                    player.sendMessage(ChatColor.AQUA + "The command format is " + ChatColor.WHITE + "/"+commandLabel+" set max [item name] [amount]");
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
                    player.sendMessage("   " + "/"+commandLabel+" set unlimited money");
                    player.sendMessage("   " + "/"+commandLabel+" set unlimited stock");
                    return true;
                }
            } else {
                player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "The following set commands are available: ");
                player.sendMessage("   " + "/"+commandLabel+" set buy [item name] [price] <bundle size>");
                player.sendMessage("   " + "/"+commandLabel+" set sell [item name] [price] <bundle size>");
                player.sendMessage("   " + "/"+commandLabel+" set max [item name] [max number]");
                player.sendMessage("   " + "/"+commandLabel+" set manager +[playername] -[playername2]");
                player.sendMessage("   " + "/"+commandLabel+" set owner [player name]");
                if (canUseCommand(CommandTypes.ADMIN)) {
                    player.sendMessage("   " + "/"+commandLabel+" set unlimited money");
                    player.sendMessage("   " + "/"+commandLabel+" set unlimited stock");
                }
            }

            plugin.shopData.saveShop(shop);

        } else {
            player.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "You must be inside a shop to use /"+commandLabel+" " + args[0]);
            return false;
        }

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
    public boolean shopRemoveItem() {
        if (!(sender instanceof Player) || !canUseCommand(CommandTypes.REMOVE_ITEM)) {
            sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.AQUA + "You don't have permission to use this command");
            return false;
        }

        /*
         * Available formats: /lshop remove itemName
         */

        Player player = (Player) sender;
        String playerName = player.getName();

        // get the shop the player is currently in
        if (plugin.playerData.get(playerName).shopList.size() == 1) {
            String shopName = plugin.playerData.get(playerName).shopList.get(0);
            Shop shop = plugin.shopData.getShop(shopName);

            if (!isShopController(player, shop)) {
                player.sendMessage(ChatColor.AQUA + "You must be the shop owner or a manager to remove an item.");
                player.sendMessage(ChatColor.AQUA + "The current shop owner is " + ChatColor.WHITE + shop.getOwner());
                return false;
            }

            if (args.length != 2) {
                player.sendMessage(ChatColor.AQUA + "The command format is " + ChatColor.WHITE + "/"+commandLabel+" remove [item name]");
                return false;
            }

            ItemStack item = LocalShops.itemList.getShopItem(player, shop, args[1]);
            String itemName;

            if (item == null) {
                player.sendMessage(ChatColor.AQUA + "Could not complete command.");
                return false;
            } else {
                int itemData = item.getDurability();
                itemName = LocalShops.itemList.getItemName(item.getTypeId(), itemData);
            }

            if (!shop.getItems().contains(itemName)) {
                player.sendMessage(ChatColor.AQUA + "The shop is not selling " + ChatColor.WHITE + itemName);
                return true;
            }

            player.sendMessage(ChatColor.WHITE + itemName + ChatColor.AQUA + " removed from the shop. ");
            if (!shop.isUnlimitedStock()) {
                int amount = shop.getItem(itemName).getStock();

                // log the transaction
                plugin.shopData.logItems(playerName, shopName, "remove-item", itemName, amount, amount, 0);

                givePlayerItem(player, item, amount);
                player.sendMessage("" + ChatColor.WHITE + amount + ChatColor.AQUA + " have been returned to your inventory");
            }

            shop.removeItem(itemName);
            plugin.shopData.saveShop(shop);

        } else {
            player.sendMessage(ChatColor.AQUA + "You must be inside a shop to use /"+commandLabel+" " + args[0]);
        }

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
            String shopName = plugin.playerData.get(playerName).shopList.get(0);
            Shop shop = plugin.shopData.getShop(shopName);

            if (!shop.getOwner().equalsIgnoreCase(player.getName()) && !canUseCommand(CommandTypes.ADMIN)) {
                player.sendMessage(ChatColor.AQUA + "You must be the shop owner to destroy it.");
                return false;
            }

            sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.WHITE + shop.getName() + ChatColor.AQUA + " has been destroyed");
            plugin.shopData.deleteShop(shop);

        } else {
            player.sendMessage(ChatColor.AQUA + "You must be inside a shop to use /"+commandLabel+" " + args[0]);
        }

        return true;
    }

    public int countItemsinInventory(PlayerInventory inventory, ItemStack item) {
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

    private static void givePlayerItem(Player player, ItemStack item, int amount) {
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
}
