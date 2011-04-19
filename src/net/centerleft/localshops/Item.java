package net.centerleft.localshops;

public class Item {

    private String name;
    private int buySize;
    private int buyPrice;
    private int sellSize;
    private int sellPrice;
    private int stock;
    public int maxStock;

    public Item() {
	name = null;
	buySize = 1;
	buyPrice = 0;
	sellSize = 1;
	sellPrice = 0;
	stock = 0;
	maxStock = 0;
    }

    public Item(String name) {
	this.name = name;

	buySize = 1;
	buyPrice = 0;
	sellSize = 1;
	sellPrice = 0;
	stock = 0;
    }

    public String getName() {
	return name;
    }

    public void setSell(int sellPrice, int sellSize) {
	this.sellPrice = sellPrice;
	this.sellSize = sellSize;

    }

    public void setBuy(int buyPrice, int buySize) {
	this.buyPrice = buyPrice;
	this.buySize = buySize;
    }

    public int getMaxStock() {
	return maxStock;
    }
    
    public void setStock(int stock) {
	this.stock = stock;
    }
    
    public void addStock(int stock) {
	this.stock += stock;
    }
    
    public void removeStock(int stock) {
	this.stock -= stock;
	if(this.stock < 0) {
	    this.stock = 0;
	}
    }
    
    public int getStock() {
	return stock;
    }
    
    public void setSellPrice(int price) {
	sellPrice = price;
    }
    
    public int getSellPrice() {
	return sellPrice;
    }
    
    public void setSellSize(int size) {
	sellSize = size;
    }
    
    public int getSellSize() {
	return sellSize;
    }
    
    public void setBuySize(int size) {
	buySize = size;
    }
    
    public int getBuySize() {
	return buySize;
    }
    
    public void setBuyPrice(int price) {
	buyPrice = price;
    }
    
    public int getBuyPrice() {
	return buyPrice;
    }
}
