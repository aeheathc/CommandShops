package net.centerleft.localshops;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
    private HashMap<UUID, Shop> shops = new HashMap<UUID, Shop>();

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
    
    static int MIN_UNIQUE_ID_LENGTH = 1;
    ArrayList<String> uniqueIds = new ArrayList<String>();

    public ShopData(LocalShops plugin) {
        this.plugin = plugin;
    }

    public Shop getShop(UUID uuid) {
        return shops.get(uuid);
    }
    
    public Shop getShop(String partialUuid) {       
        Iterator<Shop> it = shops.values().iterator();
        while (it.hasNext()) {
            Shop cShop = it.next();
            if(cShop.getUuid().toString().matches(".*"+partialUuid.toLowerCase()+"$")) {
                return cShop;
            }
        }
        
        return null;
    }

    public void addShop(Shop shop) {
        String uuid = shop.getUuid().toString();
        while (true) {
            if (uniqueIds.contains(uuid.substring(uuid.length() - MIN_UNIQUE_ID_LENGTH))) {
                calcShortUuidSize();
            } else {
                uniqueIds.add(uuid.substring(uuid.length() - MIN_UNIQUE_ID_LENGTH));
                break;
            }
        }
        shops.put(shop.getUuid(), shop);
    }
    
    private void calcShortUuidSize() {
            uniqueIds.clear();
            Iterator<Shop> it = shops.values().iterator();
            while (it.hasNext()) {
                Shop cShop = it.next();
                String cUuid = cShop.getUuid().toString();
                String sUuid = cUuid.substring(cUuid.length() - MIN_UNIQUE_ID_LENGTH);
                if(uniqueIds.contains(sUuid)) {
                    calcShortUuidSize();
                } else {
                    uniqueIds.add(sUuid);
                }
            }
    }

    public Collection<Shop> getAllShops() {
        return shops.values();
    }

    public int getNumShops() {
        return shops.size();
    }

    public void loadShops(File shopsDir) {
        log.info(String.format("[%s] %s.%s", plugin.pdfFile.getName(), "ShopData", "loadShops(File shopsDir)"));

        LocalShops.cuboidTree = new QuadTree();

        File[] shopsList = shopsDir.listFiles();
        for (File file : shopsList) {
            
            log.info(String.format("[%s] Loading Shop file \"%s\".", plugin.pdfFile.getName(), file.toString()));
            Shop shop = null;
            
            // Determine if filename is a UUID or not
            if(file.getName().matches("^(\\{{0,1}([0-9a-fA-F]){8}-([0-9a-fA-F]){4}-([0-9a-fA-F]){4}-([0-9a-fA-F]){4}-([0-9a-fA-F]){12}\\}{0,1})\\.shop$")) {
                shop = loadShop(file);
            } else {
                // Convert old format & delete the file...immediately save using the new format (will generate a new UUID for this shop)
                shop = convertShopOldFormat(file);                
            }
            
            // Check if not null, and add to world
            if (shop != null) {
                log.info(String.format("[%s] Loaded Shop %s", plugin.pdfFile.getName(), shop.toString()));
                LocalShops.cuboidTree.insert(shop.getCuboid());
                plugin.shopData.addShop(shop);
            }
        }

    }

    public Shop convertShopOldFormat(File file) {
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
            
            if(file.delete()) {
                saveShop(shop);
                return shop;
            } else {
                return null;
            }

        } catch (IOException e) {
            log.warning(String.format("[%s] Could not load Shop File \"%s\": %s", plugin.pdfFile.getName(), file.toString(), e.getMessage()));
            return null;
        }
    }
    
    public static long[] convertStringArraytoLongArray(String[] sarray) {
        if (sarray != null) {
            long longArray[] = new long[sarray.length];
            for (int i = 0; i < sarray.length; i++) {
                longArray[i] = Long.parseLong(sarray[i]);
            }
            return longArray;
        }
        return null;
    }    

    public Shop loadShop(File file) {
        log.info(String.format("[%s] %s.%s", plugin.pdfFile.getName(), "ShopData", "loadShop(File file)"));

        SortedProperties props = new SortedProperties();
        try {
            props.load(new FileInputStream(file));
        } catch(IOException e) {
            log.warning(String.format("[%s] %s", plugin.pdfFile.getName(), "IOException: " + e.getMessage()));
            return null;
        }

        // Shop attributes
        UUID uuid = UUID.fromString(props.getProperty("uuid"));
        String name = props.getProperty("name");
        boolean unlimitedMoney = Boolean.parseBoolean(props.getProperty("unlimited-money"));
        boolean unlimitedStock = Boolean.parseBoolean(props.getProperty("unlimited-stock"));

        // Location - locationB=-88, 50, -127
        long[] locationA = convertStringArraytoLongArray(props.getProperty("locationA").split(", "));
        long[] locationB = convertStringArraytoLongArray(props.getProperty("locationB").split(", "));
        String world = props.getProperty("world");

        // People
        String owner = props.getProperty("owner");
        String[] managers = props.getProperty("managers").replaceAll("[\\[\\]]", "").split(", ");
        String creator = props.getProperty("creator");
        
        Shop shop = new Shop(uuid);
        shop.setName(name);
        shop.setUnlimitedMoney(unlimitedMoney);
        shop.setUnlimitedStock(unlimitedStock);
        shop.setLocationA(new ShopLocation(locationA));
        shop.setLocationB(new ShopLocation(locationB));
        shop.setWorld(world);
        shop.setOwner(owner);
        shop.setManagers(managers);
        shop.setCreator(creator);
        
        // Iterate through all keys, find items & parse
        // props.setProperty(String.format("%d:%d", info.typeId, info.subTypeId), String.format("%d:%d,%d:%d,%d:%d", buyPrice, buySize, sellPrice, sellSize, stock, maxStock));
        Iterator<Object> it = props.keySet().iterator();
        while(it.hasNext()) {
            String key = (String) it.next();
            if(key.matches("\\d+:\\d+")) {
                String[] k = key.split(":");
                int id = Integer.parseInt(k[0]);
                short type = Short.parseShort(k[1]);
                
                String value = props.getProperty(key);
                String[] v = value.split(",");

                String[] buy = v[0].split(":");
                int buyPrice = Integer.parseInt(buy[0]);
                int buyStackSize = Integer.parseInt(buy[1]);
                
                String[] sell = v[1].split(":");
                int sellPrice = Integer.parseInt(sell[0]);
                int sellStackSize = Integer.parseInt(sell[1]);
                
                String[] stock = v[2].split(":");
                int currStock = Integer.parseInt(stock[0]);
                int maxStock = Integer.parseInt(stock[1]);
                
                shop.addItem(id, type, buyPrice, buyStackSize, sellPrice, sellStackSize, currStock, maxStock);
            }
        }
                
        return shop;
    }
    
    public boolean saveAllShops() {
        log.info(String.format("[%s] %s", plugin.pdfFile.getName(), "Saving All Shops"));
        Iterator<Shop> it = shops.values().iterator();
        while(it.hasNext()) {
            Shop shop = it.next();
            saveShop(shop);
        }
        return true;
    }

    public boolean saveShop(Shop shop) {
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
        Date date = new Date();
        SortedProperties props = new SortedProperties();

        // Config attributes
        props.setProperty("config-version", "2.0");

        // Shop attributes
        props.setProperty("uuid", shop.getUuid().toString());
        props.setProperty("name", shop.getName());
        props.setProperty("unlimited-money", String.valueOf(shop.isUnlimitedMoney()));
        props.setProperty("unlimited-stock", String.valueOf(shop.isUnlimitedStock()));

        // Location
        props.setProperty("locationA", shop.getLocationA().toString());
        props.setProperty("locationB", shop.getLocationB().toString());
        props.setProperty("world", shop.getWorld());

        // People
        props.setProperty("owner", shop.getOwner());
        props.setProperty("managers", Search.join(shop.getManagers(), ", "));
        props.setProperty("creator", shop.getCreator());

        // Inventory
        for (InventoryItem item : shop.getItems()) {
            ItemInfo info = item.getInfo();
            int buyPrice = item.getBuyPrice();
            int buySize = item.getBuySize();
            int sellPrice = item.getSellPrice();
            int sellSize = item.getSellSize();
            int stock = item.getStock();
            int maxStock = item.getMaxStock();

            props.setProperty(String.format("%d:%d", info.typeId, info.subTypeId), String.format("%d:%d,%d:%d,%d:%d", buyPrice, buySize, sellPrice, sellSize, stock, maxStock));
        }

        String fileName = LocalShops.folderPath + LocalShops.shopsPath + shop.getUuid().toString() + ".shop";
        try {
            props.store(new FileOutputStream(fileName), "LocalShops Config Version 2.0");
        } catch (IOException e) {
            log.warning("IOException: " + e.getMessage());
        }

        return true;
    }

    public boolean deleteShop(Shop shop) {
        String shortUuid = shop.getShortUuidString();
        long[] xyzA = shop.getLocation();
        BookmarkedResult res = new BookmarkedResult();

        res = LocalShops.cuboidTree.relatedSearch(res.bookmark, xyzA[0],
                xyzA[1], xyzA[2]);

        // get the shop's tree node and delete it
        for (PrimitiveCuboid shopLocation : res.results) {

            // for each shop that you find, check to see if we're already in it
            // this should only find one shop node
            if (shopLocation.uuid == null) {
                continue;
            }
            if (!shopLocation.world.equalsIgnoreCase(shop.getWorld())) {
                continue;
            }
            LocalShops.cuboidTree.delete(shopLocation);

        }
        
        // remove string from uuid short list
        uniqueIds.remove(shortUuid);

        // delete the file from the directory
        String filePath = LocalShops.folderPath + LocalShops.shopsPath + shop.getUuid() + ".shop";
        File shopFile = new File(filePath);
        shopFile.delete();

        // remove shop from data structure
        shops.remove(shop.getUuid());

        return true;
    }

    private boolean shopPositionOk(Shop shop, long[] xyzA, long[] xyzB) {
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
                    if (shopOverlaps(shop, res))
                        return false;
                }
            }
        }
        return true;
    }

    private boolean shopOverlaps(Shop shop, BookmarkedResult res) {
        if (res.results.size() != 0) {
            for (PrimitiveCuboid foundShop : res.results) {
                if (foundShop.uuid != null) {
                    if (foundShop.world.equalsIgnoreCase(shop.getWorld())) {
                        Shop fShop = getShop(foundShop.uuid);
                        System.out.println("Could not create shop, it overlaps with " + fShop.getName());
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
