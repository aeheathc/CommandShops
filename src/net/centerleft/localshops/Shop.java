package net.centerleft.localshops;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import cuboidLocale.PrimitiveCuboid;

public class Shop {
    // Attributes
    private UUID uuid = null;
    private String world = null;
    private String name = null;
    private ShopLocation locationA = null;
    private ShopLocation locationB = null;
    private String owner = null;
    private String creator = null;
    private ArrayList<String> managers = new ArrayList<String>();
    private boolean unlimitedMoney = false;
    private boolean unlimitedStock = false;
    private HashMap<String, InventoryItem> inventory = new HashMap<String, InventoryItem>();
    private PrimitiveCuboid cuboid = null;
    
    // Logging
    private static final Logger log = Logger.getLogger("Minecraft");    

    public Shop(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setWorld(String name) {
        world = name;
    }

    public String getWorld() {
        return world;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setLocations(ShopLocation locationA, ShopLocation locationB) {
        this.locationA = locationA;
        this.locationB = locationB;
    }

    public void setLocationA(ShopLocation locationA) {
        this.locationA = locationA;
    }

    public void setLocationA(long x, long y, long z) {
        locationA = new ShopLocation(x, y, z);
    }

    public void setLocationB(ShopLocation locationB) {
        this.locationB = locationB;
    }

    public void setLocationB(long x, long y, long z) {
        locationB = new ShopLocation(x, y, z);
    }

    public ShopLocation[] getLocations() {
        return new ShopLocation[] { locationA, locationB };
    }

    public ShopLocation getLocationA() {
        return locationA;
    }

    public ShopLocation getLocationB() {
        return locationB;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getOwner() {
        return owner;
    }

    public String getCreator() {
        return creator;
    }

    public void setUnlimitedStock(boolean b) {
        unlimitedStock = b;
    }

    public void setUnlimitedMoney(boolean b) {
        unlimitedMoney = b;
    }

    public InventoryItem getItem(String item) {
        return inventory.get(item);
    }
    
    public boolean containsItem(ItemInfo item) {
        Iterator<InventoryItem> it = inventory.values().iterator();
        while(it.hasNext()) {
            InventoryItem invItem = it.next();
            ItemInfo invItemInfo = invItem.getInfo();
            if(invItemInfo.typeId == item.typeId && invItemInfo.subTypeId == item.subTypeId) {
                return true;
            }
        }
        return false;
    }

    public void addItem(int itemNumber, short itemData, int buyPrice, int buyStackSize, int sellPrice, int sellStackSize, int stock, int maxStock) {
        // TODO add maxStock to item object
        ItemInfo item = Search.itemById(itemNumber, itemData);
        InventoryItem thisItem = new InventoryItem(item);

        thisItem.setBuy(buyPrice, buyStackSize);
        thisItem.setSell(sellPrice, sellStackSize);

        thisItem.setStock(stock);

        thisItem.maxStock = maxStock;

        if (inventory.containsKey(item.name)) {
            inventory.remove(item.name);
        }

        inventory.put(item.name, thisItem);
    }

    public void setManagers(String[] managers) {
        this.managers = new ArrayList<String>();

        for (String manager : managers) {
            if (!manager.equals("")) {
                this.managers.add(manager);
            }
        }
    }

    public void addManager(String manager) {
        managers.add(manager);
    }
    
    public void removeManager(String manager) {
        managers.remove(manager);
    }

    public List<String> getManagers() {
        return managers;
    }

    public Collection<InventoryItem> getItems() {
        return inventory.values();
    }

    public boolean isUnlimitedStock() {
        return unlimitedStock;
    }

    public boolean isUnlimitedMoney() {
        return unlimitedMoney;
    }

    public boolean addStock(String itemName, int amount) {
        if (!inventory.containsKey(itemName)) {
            return false;
        }
        inventory.get(itemName).addStock(amount);
        return true;
    }

    public boolean removeStock(String itemName, int amount) {
        if (!inventory.containsKey(itemName))
            return false;
        inventory.get(itemName).removeStock(amount);
        return true;
    }

    public void setItemBuyPrice(String itemName, int price) {
        int buySize = inventory.get(itemName).getBuySize();
        inventory.get(itemName).setBuy(price, buySize);
    }

    public void setItemBuyAmount(String itemName, int buySize) {
        int price = inventory.get(itemName).getBuyPrice();
        inventory.get(itemName).setBuy(price, buySize);
    }

    public void setItemSellPrice(String itemName, int price) {
        int sellSize = inventory.get(itemName).getSellPrice();
        inventory.get(itemName).setSell(price, sellSize);
    }
    
    public void setItemSellAmount(String itemName, int sellSize) {
        int price = inventory.get(itemName).getSellPrice();
        inventory.get(itemName).setBuy(price, sellSize);
    }    

    public void removeItem(String itemName) {
        inventory.remove(itemName);
    }

    // Why are we trying to find the center of the shop???
    public long[] getLocation() {
        long[] xyz = new long[3];
        long[] xyzA = locationA.toArray();
        long[] xyzB = locationA.toArray();

        for (int i = 0; i < 3; i++) {
            if (xyzA[i] < xyzB[i]) {
                xyz[i] = xyzA[i] + (Math.abs(xyzA[i] - xyzB[i])) / 2;
            } else {
                xyz[i] = xyzA[i] - (Math.abs(xyzA[i] - xyzB[i])) / 2;
            }
        }

        return xyz;
    }

    public int itemMaxStock(String itemName) {
        return inventory.get(itemName).maxStock;
    }

    public void setItemMaxStock(String itemName, int maxStock) {
        inventory.get(itemName).maxStock = maxStock;
    }

    public PrimitiveCuboid getCuboid() {
        // If no cuboid, create it
        if (cuboid == null) {
            // Check if either locaiton is null and return appropriately
            if (locationA == null || locationB == null) {
                return null;
            }
            cuboid = new PrimitiveCuboid(getLocationA().toArray(), getLocationB().toArray());
            cuboid.name = name;
            cuboid.world = world;
        }

        return cuboid;
    }

    public String toString() {
        return String.format("Shop \"%s\" at [%s], [%s] %d items", this.name, locationA.toString(), locationB.toString(), inventory.size());
    }
    
    public void log() {
        // Details
        log.info("Shop Information");
        log.info(String.format("   %-16s %s", "UUID:", uuid.toString()));
        log.info(String.format("   %-16s %s", "Name:", name));
        log.info(String.format("   %-16s %s", "Creator:", creator));
        log.info(String.format("   %-16s %s", "Owner:", owner));
        log.info(String.format("   %-16s %s", "Managers:", Search.join(managers, ",")));
        log.info(String.format("   %-16s %s", "Unlimited Money:", unlimitedMoney ? "Yes" : "No"));
        log.info(String.format("   %-16s %s", "Unlimited Stock:", unlimitedStock ? "Yes" : "No"));        
        log.info(String.format("   %-16s %s", "Location A:", locationA.toString()));
        log.info(String.format("   %-16s %s", "Location B:", locationB.toString()));
        log.info(String.format("   %-16s %s", "World:", world));
        
        // Items
        log.info("Shop Inventory");
        log.info("   BP=Buy Price, BS=Buy Size, SP=Sell Price, SS=Sell Size, ST=Stock, MX=Max Stock");
        log.info(String.format("   %-6s %-3s %-3s %-3s %-3s %-3s %-3s", "Id", "BP", "BS", "SP", "SS", "ST", "MX"));        
        Iterator<InventoryItem> it = inventory.values().iterator();
        while(it.hasNext()) {
            InventoryItem item = it.next();
            ItemInfo info = item.getInfo();
            log.info(String.format("   %3d:%-2d %-3d %-3d %-3d %-3d %-3d %-3d", info.typeId, info.subTypeId, item.getBuyPrice(), item.getBuySize(), item.getSellPrice(), item.getSellSize(), item.getStock(), item.getMaxStock()));
        }
    }
}