package net.centerleft.localshops;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Shop {

    private String world;
    private String name;
    private Location location;
    private String owner;
    private String creator;
    private String[] managers;
    private boolean unlimitedMoney;
    private boolean unlimitedStock;

    private HashMap<String, Item> shopInventory;

    public Shop() {
	world = "";
	name = null;
	shopInventory = new HashMap<String, Item>();
	shopInventory.clear();
	long[] xyz = { 0, 0, 0 };
	location = new Location(xyz, xyz);
	owner = "";
	creator = "";
	managers = null;
	unlimitedStock = false;
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

    public void setLocation(long[] position1, long[] position2) {
	location.setLocation(position1, position2);
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
	return shopInventory.get(item);
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

	if (shopInventory.containsKey(itemName)) {
	    shopInventory.remove(itemName);
	}

	shopInventory.put(itemName, thisItem);

    }

    public String getShopPosition1String() {
	String returnString = "";
	for (long coord : location.getLocation1()) {
	    returnString += coord + ",";
	}
	return returnString;
    }

    public String getShopPosition2String() {
	String returnString = "";
	for (long coord : location.getLocation2()) {
	    returnString += coord + ",";
	}
	return returnString;
    }

    public String[] getManagers() {
	return managers;
    }

    public Collection<Item> getItems() {
	return shopInventory.values();
    }

    public boolean isUnlimitedStock() {
	return unlimitedStock;
    }

    public boolean isUnlimitedMoney() {
	return unlimitedMoney;
    }

    public boolean addStock(String itemName, int amount) {
	if (!shopInventory.containsKey(itemName)) {
	    return false;
	}
	shopInventory.get(itemName).addStock(amount);
	return true;
    }

    public boolean removeStock(String itemName, int amount) {
	if (!shopInventory.containsKey(itemName))
	    return false;
	shopInventory.get(itemName).removeStock(amount);
	return true;
    }

    public void setItemBuyPrice(String itemName, int price) {
	int buySize = shopInventory.get(itemName).getBuySize();
	shopInventory.get(itemName).setBuy(price, buySize);

    }

    public void setItemBuyAmount(String itemName, int buySize) {
	int price = shopInventory.get(itemName).getBuyPrice();
	shopInventory.get(itemName).setBuy(price, buySize);
    }

    public void setItemSellPrice(String itemName, int price) {
	int sellSize = shopInventory.get(itemName).getSellPrice();
	shopInventory.get(itemName).setSell(price, sellSize);

    }

    public void removeItem(String itemName) {
	shopInventory.remove(itemName);

    }

    public long[] getLocation1() {
	return location.getLocation1();
    }

    public long[] getLocation2() {
	return location.getLocation2();
    }

    public long[] getLocation() {
	long[] xyz = new long[3];
	long[] xyzA = location.getLocation1();
	long[] xyzB = location.getLocation2();

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
	return shopInventory.get(itemName).maxStock;
    }

    public void setItemMaxStock(String itemName, int maxStock) {
	shopInventory.get(itemName).maxStock = maxStock;
    }

}