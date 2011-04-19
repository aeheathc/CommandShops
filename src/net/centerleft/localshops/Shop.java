package net.centerleft.localshops;

import java.util.Collection;
import java.util.HashMap;

public class Shop {
    // Attributes
    private String world = null;
    private String name = null;
    private ShopLocation locationA = null;
    private ShopLocation locationB = null;
    private String owner = null;
    private String creator = null;
    private String[] managers = null;
    private boolean unlimitedMoney = false;
    private boolean unlimitedStock = false;
    private HashMap<String, Item> inventory = new HashMap<String, Item>();

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

    public void setShopManagers(String[] names) {
	if (names != null) {
	    managers = names.clone();
	} else {
	    managers = null;
	}
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

    public void addItem(int itemNumber, int itemData, int buyPrice,
			int buyStackSize, int sellPrice, int sellStackSize, int stock, int maxStock) {
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

    public String[] getManagers() {
	return managers;
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

    public String toString() {
	return String.format("Shop \"%s\" is at [%s], [%s] and has %d items", this.name, locationA.toString(), locationB.toString(), inventory.size());
    }
}