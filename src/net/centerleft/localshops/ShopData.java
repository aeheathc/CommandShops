package net.centerleft.localshops;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import java.util.logging.Logger;

import cuboidLocale.BookmarkedResult;
import cuboidLocale.PrimitiveCuboid;
import cuboidLocale.QuadTree;

public class ShopData {
    private LocalShops plugin = null;
    private HashMap<String, Shop> shops;

    // Logging
    private final Logger log = Logger.getLogger("Minecraft");

    long shopSize = 10;
    long shopHeight = 3;
    String currencyName = "Coin";

    long shopCost = 4000;
    long moveCost = 1000;
    boolean chargeForShop = false;
    boolean chargeForMove = false;
    boolean logTransactions = true;

    int maxDamage = 35;

    long maxWidth = 30;
    long maxHeight = 10;

    public ShopData(LocalShops plugin) {
        this.plugin = plugin;
    }

    public Shop getShop(String name) {
        return shops.get(name);
    }

    public void addShop(Shop shop) {
        shops.put(shop.getName(), shop);
    }

    public Collection<Shop> getAllShops() {
        return shops.values();
    }

    public int getNumShops() {
        return shops.size();
    }

    public void loadShops(File shopsDir) {
        log.info(String.format("[%s] %s.%s", plugin.pdfFile.getName(), "ShopData", "loadShops(File shopsDir)"));
        // initialize and setup the hash of shops
        shops = new HashMap<String, Shop>();

        LocalShops.cuboidTree = new QuadTree();

        File[] shopsList = shopsDir.listFiles();
        for (File file : shopsList) {
            log.info(String.format("[%s] Loading Shop file \"%s\".", plugin.pdfFile.getName(), file.toString()));
            // TODO: Regex on filename to determine new or old format

            Shop shop = loadShopOldFormat(file);
            // Check if not null, and add to world
            if (shop != null) {
                log.info(String.format("[%s] Loaded Shop %s", plugin.pdfFile.getName(), shop.toString()));
                LocalShops.cuboidTree.insert(shop.getCuboid());
                plugin.shopData.addShop(shop);
            }
        }

    }

    public Shop loadShopOldFormat(File file) {
        log.info(String.format("[%s] %s.%s", plugin.pdfFile.getName(), "ShopData", "loadShopOldFormat(File file)"));

        try {
            // Create new empty shop (this format has no UUID, so generate one)
            Shop shop = new Shop(UUID.randomUUID());

            // Retrieve Shop Name (from filename)
            shop.setName(file.getName().split("\\.")[0]);

            // Open file & iterate over lines
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            while (line != null) {
                log.info(String.format("[%s] %s", plugin.pdfFile.getName(), line));

                // Skip comment lines / metadata
                if (line.startsWith("#")) {
                    line = br.readLine();
                    continue;
                }

                // Data is separated by =
                String[] cols = line.split("=");

                // Check if there are enough columns (needs key and value)
                if (cols.length < 2) {
                    line = br.readLine();
                    continue;
                }

                if (cols[0].equalsIgnoreCase("world")) { // World
                    shop.setWorld(cols[1]);
                } else if (cols[0].equalsIgnoreCase("owner")) { // Owner
                    shop.setOwner(cols[1]);
                } else if (cols[0].equalsIgnoreCase("managers")) { // Managers
                    String[] managers = cols[1].split(",");
                    shop.setManagers(managers);
                } else if (cols[0].equalsIgnoreCase("creator")) { // Creator
                    shop.setCreator(cols[1]);
                } else if (cols[0].equalsIgnoreCase("position1")) { // Position
                                                                    // A
                    String[] xyzStr = cols[1].split(",");
                    try {
                        long x = Long.parseLong(xyzStr[0].trim());
                        long y = Long.parseLong(xyzStr[1].trim());
                        long z = Long.parseLong(xyzStr[2].trim());

                        ShopLocation loc = new ShopLocation(x, y, z);
                        shop.setLocationA(loc);
                    } catch (NumberFormatException e) {
                        log.warning(String.format("[%s] Shop File \"%s\" has bad Location Data, could not load.", plugin.pdfFile.getName(), file.toString()));
                        return null;
                    }
                } else if (cols[0].equalsIgnoreCase("position2")) { // Position
                                                                    // B
                    String[] xyzStr = cols[1].split(",");
                    try {
                        long x = Long.parseLong(xyzStr[0].trim());
                        long y = Long.parseLong(xyzStr[1].trim());
                        long z = Long.parseLong(xyzStr[2].trim());

                        ShopLocation loc = new ShopLocation(x, y, z);
                        shop.setLocationB(loc);
                    } catch (NumberFormatException e) {
                        log.warning(String.format("[%s] Shop File \"%s\" has bad Location Data, could not load.", plugin.pdfFile.getName(), file.toString()));
                        return null;
                    }
                } else if (cols[0].equalsIgnoreCase("unlimited-money")) { // Unlimited
                                                                          // Money
                    shop.setUnlimitedMoney(Boolean.parseBoolean(cols[1]));
                } else if (cols[0].equalsIgnoreCase("unlimited-stock")) { // Unlimited
                                                                          // Stock
                    shop.setUnlimitedStock(Boolean.parseBoolean(cols[1]));
                } else if (cols[0].matches("\\d+:\\d+")) { // Items
                    String[] itemInfo = cols[0].split(":");
                    if (itemInfo.length < 2) {
                        log.warning(String.format("[%s] Shop File \"%s\" has bad Item Data, could not load.", plugin.pdfFile.getName(), file.toString()));
                        return null;
                    }
                    int itemId = Integer.parseInt(itemInfo[0]);
                    short damageMod = Short.parseShort(itemInfo[1]);

                    String[] dataCols = cols[1].split(",");
                    if (dataCols.length < 3) {
                        log.warning(String.format("[%s] Shop File \"%s\" has bad Item Data, could not load.", plugin.pdfFile.getName(), file.toString()));
                        return null;
                    }

                    String[] buyInfo = dataCols[0].split(":");
                    if (buyInfo.length < 2) {
                        log.warning(String.format("[%s] Shop File \"%s\" has bad Item Data, could not load.", plugin.pdfFile.getName(), file.toString()));
                        return null;
                    }
                    int buyPrice = Integer.parseInt(buyInfo[0]);
                    int buySize = Integer.parseInt(buyInfo[1]);

                    String[] sellInfo = dataCols[1].split(":");
                    if (sellInfo.length < 2) {
                        log.warning(String.format("[%s] Shop File \"%s\" has bad Item Data, could not load.", plugin.pdfFile.getName(), file.toString()));
                        return null;
                    }
                    int sellPrice = Integer.parseInt(sellInfo[0]);
                    int sellSize = Integer.parseInt(sellInfo[1]);

                    String[] stockInfo = dataCols[2].split(":");
                    if (stockInfo.length < 2) {
                        log.warning(String.format("[%s] Shop File \"%s\" has bad Item Data, could not load.", plugin.pdfFile.getName(), file.toString()));
                        return null;
                    }
                    int stock = Integer.parseInt(stockInfo[0]);
                    int maxStock = Integer.parseInt(stockInfo[1]);

                    shop.addItem(itemId, damageMod, buyPrice, buySize, sellPrice, sellSize, stock, maxStock);
                } else { // Not defined
                    log.info(String.format("[%s] Shop File \"%s\" has undefined data, ignoring.", plugin.pdfFile.getName(), file.toString()));
                }
                line = br.readLine();
            }

            br.close();
            return shop;

        } catch (IOException e) {
            log.warning(String.format("[%s] Could not load Shop File \"%s\": %s", plugin.pdfFile.getName(), file.toString(), e.getMessage()));
            return null;
        }
    }

    public Shop loadShop(File file) {
        return null;
    }
    
    public boolean saveAllShops() {
        Iterator<Shop> it = shops.values().iterator();
        while(it.hasNext()) {
            Shop shop = it.next();
            saveShop(shop);
        }
        return true;
    }

    public boolean saveShop(Shop shop) {
        String filePath = LocalShops.folderPath + LocalShops.shopsPath
                + shop.getName() + ".shop";

        File shopFile = new File(filePath);
        try {

            shopFile.createNewFile();

            ArrayList<String> fileOutput = new ArrayList<String>();

            fileOutput.add("#" + shop.getName() + " shop file\n");

            DateFormat dateFormat = new SimpleDateFormat(
                    "EEE MMM dd HH:mm:ss z yyyy");
            Date date = new Date();
            fileOutput.add("#" + dateFormat.format(date) + "\n");

            fileOutput.add("world=" + shop.getWorld() + "\n");
            fileOutput.add("owner=" + shop.getOwner() + "\n");

            String outString = "";
            if (shop.getManagers() != null) {
                for (String manager : shop.getManagers()) {
                    outString = outString + manager + ",";
                }
            }
            if (outString.equalsIgnoreCase("null"))
                outString = "";

            fileOutput.add(String.format("managers=%s\n", outString));
            fileOutput.add(String.format("creator=%s\n", shop.getCreator()));
            fileOutput.add(String.format("position1=%s\n", shop.getLocationA().toString()));
            fileOutput.add(String.format("position2=%s\n", shop.getLocationB().toString()));
            fileOutput.add(String.format("unlimited-money=%s\n", String.valueOf(shop.isUnlimitedMoney())));
            fileOutput.add("unlimited-stock=" + String.valueOf(shop.isUnlimitedStock()) + "\n");

            for (InventoryItem item : shop.getItems()) {
                int buyPrice = item.getBuyPrice();
                int buySize = item.getBuySize();
                int sellPrice = item.getSellPrice();
                int sellSize = item.getSellSize();
                int stock = item.getStock();
                int maxStock = item.getMaxStock();
                int[] itemInfo = LocalShops.itemList.getItemInfo(null, item.getInfo().name);
                if (itemInfo == null)
                    continue;
                // itemId=dataValue,buyPrice:buyStackSize,sellPrice:sellStackSize,stock
                fileOutput.add(itemInfo[0] + ":" + itemInfo[1] + "=" + buyPrice
                        + ":" + buySize + "," + sellPrice + ":" + sellSize
                        + "," + stock + ":" + maxStock + "\n");
            }

            FileOutputStream shopFileOut = new FileOutputStream(filePath);

            for (String line : fileOutput) {
                shopFileOut.write(line.getBytes());
            }

            shopFileOut.close();

        } catch (IOException e1) {
            System.out.println(plugin.pdfFile.getName() + ": Error - Could not create file " + shopFile.getName());
            return false;
        }
        return true;
    }

    public boolean deleteShop(Shop shop) {
        long[] xyzA = shop.getLocation();
        BookmarkedResult res = new BookmarkedResult();

        res = LocalShops.cuboidTree.relatedSearch(res.bookmark, xyzA[0],
                xyzA[1], xyzA[2]);

        // get the shop's tree node and delete it
        for (PrimitiveCuboid shopLocation : res.results) {

            // for each shop that you find, check to see if we're already in it
            // this should only find one shop node
            if (shopLocation.name == null)
                continue;
            if (!shopLocation.world.equalsIgnoreCase(shop.getWorld()))
                continue;
            LocalShops.cuboidTree.delete(shopLocation);

        }

        // delete the file from the directory
        String filePath = LocalShops.folderPath + LocalShops.shopsPath
                + shop.getName() + ".shop";
        File shopFile = new File(filePath);
        shopFile.delete();

        // remove shop from data structure
        String name = shop.getName();
        shops.remove(name);

        return true;
    }

    private static boolean shopPositionOk(Shop shop, long[] xyzA, long[] xyzB) {
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
                    res = LocalShops.cuboidTree.relatedSearch(res.bookmark, x,
                            y, z);
                    if (shopOverlaps(shop, res))
                        return false;
                }
            }
        }
        return true;
    }

    private static boolean shopOverlaps(Shop shop, BookmarkedResult res) {
        if (res.results.size() != 0) {
            for (PrimitiveCuboid foundShop : res.results) {
                if (foundShop.name != null) {
                    if (foundShop.world.equalsIgnoreCase(shop.getWorld())) {
                        System.out
                                .println("Could not create shop, it overlaps with "
                                        + foundShop.name);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean logItems(String playerName, String shopName, String action, String itemName, int numberOfItems, int startNumberOfItems, int endNumberOfItems) {

        return logTransaciton(playerName, shopName, action, itemName, numberOfItems, startNumberOfItems, endNumberOfItems, 0, 0, 0);

    }

    public boolean logPayment(String playerName, String action, double moneyTransfered, double startingbalance, double endingbalance) {

        return logTransaciton(playerName, null, action, null, 0, 0, 0, moneyTransfered, startingbalance, endingbalance);
    }

    public boolean logTransaciton(String playerName, String shopName, String action, String itemName, int numberOfItems, int startNumberOfItems, int endNumberOfItems, double moneyTransfered, double startingbalance, double endingbalance) {
        if (!logTransactions)
            return false;

        String filePath = LocalShops.folderPath + "transactions.log";

        File logFile = new File(filePath);
        try {

            logFile.createNewFile();

            String fileOutput = "";

            DateFormat dateFormat = new SimpleDateFormat(
                    "yyyy/MM/dd HH:mm:ss z");
            Date date = new Date();
            fileOutput += dateFormat.format(date) + ": ";
            fileOutput += "Action: ";
            if (action != null)
                fileOutput += action;
            fileOutput += ": ";
            fileOutput += "Player: ";
            if (playerName != null)
                fileOutput += playerName;
            fileOutput += ": ";
            fileOutput += "Shop: ";
            if (shopName != null)
                fileOutput += shopName;
            fileOutput += ": ";
            fileOutput += "Item Type: ";
            if (itemName != null)
                fileOutput += itemName;
            fileOutput += ": ";
            fileOutput += "Number Transfered: ";
            fileOutput += numberOfItems;
            fileOutput += ": ";
            fileOutput += "Stating Stock: ";
            fileOutput += startNumberOfItems;
            fileOutput += ": ";
            fileOutput += "Ending Stock: ";
            fileOutput += endNumberOfItems;
            fileOutput += ": ";
            fileOutput += "Money Transfered: ";
            fileOutput += moneyTransfered;
            fileOutput += ": ";
            fileOutput += "Starting balance: ";
            fileOutput += startingbalance;
            fileOutput += ": ";
            fileOutput += "Ending balance: ";
            fileOutput += endingbalance;
            fileOutput += ": ";
            fileOutput += "\n";

            FileOutputStream logFileOut = new FileOutputStream(logFile, true);
            logFileOut.write(fileOutput.getBytes());
            logFileOut.close();

        } catch (IOException e1) {
            System.out.println(plugin.pdfFile.getName() + ": Error - Could not write to file " + logFile.getName());
            return false;
        }

        return true;
    }
}
