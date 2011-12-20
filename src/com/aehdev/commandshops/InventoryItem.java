package com.aehdev.commandshops;

// TODO: Auto-generated Javadoc
/**
 * The Class InventoryItem.
 */
public class InventoryItem
{

	/** The info. */
	private ItemInfo info;

	/** The buy price. */
	private double buyPrice = 0;

	/** The sell price. */
	private double sellPrice = 0;

	/** The stock. */
	private int stock;

	/** The max stock. */
	public int maxStock;

	/**
	 * Instantiates a new inventory item.
	 */
	public InventoryItem()
	{
		info = null;
		buyPrice = 0;
		sellPrice = 0;
		stock = 0;
		maxStock = 0;
	}

	/**
	 * Instantiates a new inventory item.
	 * @param info
	 * the info
	 */
	public InventoryItem(ItemInfo info)
	{
		this.info = info;
		buyPrice = 0;
		sellPrice = 0;
		stock = 0;
	}

	/**
	 * Instantiates a new inventory item.
	 * @param info
	 * the info
	 * @param buySize
	 * the buy size
	 * @param buyPrice
	 * the buy price
	 * @param sellSize
	 * the sell size
	 * @param sellPrice
	 * the sell price
	 * @param stock
	 * the stock
	 * @param maxStock
	 * the max stock
	 */
	public InventoryItem(ItemInfo info, double buyPrice,
			double sellPrice, int stock, int maxStock)
	{
		this.info = info;
		this.buyPrice = buyPrice;
		this.sellPrice = sellPrice;
		this.stock = stock;
		this.maxStock = maxStock;
	}

	/**
	 * Gets the info.
	 * @return the info
	 */
	public ItemInfo getInfo()
	{
		return info;
	}

	/**
	 * Sets the sell.
	 * @param sellPrice
	 * the sell price
	 * @param sellSize
	 * the sell size
	 */
	public void setSell(double sellPrice)
	{
		this.sellPrice = sellPrice;
	}

	/**
	 * Sets the buy.
	 * @param buyPrice
	 * the buy price
	 * @param buySize
	 * the buy size
	 */
	public void setBuy(double buyPrice)
	{
		this.buyPrice = buyPrice;
	}

	/**
	 * Gets the max stock.
	 * @return the max stock
	 */
	public int getMaxStock()
	{
		return maxStock;
	}

	/**
	 * Sets the stock.
	 * @param stock
	 * the new stock
	 */
	public void setStock(int stock)
	{
		this.stock = stock;
	}

	/**
	 * Adds the stock.
	 * @param stock
	 * the stock
	 */
	public void addStock(int stock)
	{
		this.stock += stock;
	}

	/**
	 * Removes the stock.
	 * @param stock
	 * the stock
	 */
	public void removeStock(int stock)
	{
		this.stock -= stock;
		if(this.stock < 0)
		{
			this.stock = 0;
		}
	}

	/**
	 * Gets the stock.
	 * @return the stock
	 */
	public int getStock()
	{
		return stock;
	}

	/**
	 * Sets the sell price.
	 * @param price
	 * the new sell price
	 */
	public void setSellPrice(double price)
	{
		sellPrice = price;
	}

	/**
	 * Gets the sell price.
	 * @return the sell price
	 */
	public double getSellPrice()
	{
		return sellPrice;
	}
	
	/**
	 * Sets the buy price.
	 * @param price
	 * the new buy price
	 */
	public void setBuyPrice(double price)
	{
		buyPrice = price;
	}

	/**
	 * Gets the buy price.
	 * @return the buy price
	 */
	public double getBuyPrice()
	{
		return buyPrice;
	}
}
