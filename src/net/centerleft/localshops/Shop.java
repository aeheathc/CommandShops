package net.centerleft.localshops;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

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
    private HashMap<String, Item> inventory = new HashMap<String, Item>();
    private PrimitiveCuboid cuboid = null;

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

    public Item getItem(String item) {
        return inventory.get(item);
    }

    public void addItem(int itemNumber, int itemData, int buyPrice, int buyStackSize, int sellPrice, int sellStackSize, int stock, int maxStock) {
        // TODO add maxStock to item object
        String itemName = LocalShops.itemList.getItemName(itemNumber, itemData);
        Item thisItem = new Item(itemName);

        thisItem.setBuy(buyPrice, buyStackSize);
        thisItem.setSell(sellPrice, sellStackSize);

        thisItem.setStock(stock);

        thisItem.maxStock = maxStock;

        if (inventory.containsKey(itemName)) {
            inventory.remove(itemName);
        }

        inventory.put(itemName, thisItem);

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
        this.managers.add(manager);
    }

    public String[] getManagers() {
        String[] m = new String[this.managers.size()];
        m = managers.toArray(m);
        return m;
    }

    public Collection<Item> getItems() {
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
        return String.format("Shop \"%s\" is at [%s], [%s] and has %d items", this.name, locationA.toString(), locationB.toString(), inventory.size());
    }
}