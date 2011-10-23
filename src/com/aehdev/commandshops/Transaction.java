package com.aehdev.commandshops;

// TODO: Auto-generated Javadoc
/**
 * The Class Transaction.
 */
public class Transaction implements Cloneable
{

	// Transactions are in perspective of the shop
	/**
	 * The Enum Type.
	 */
	public static enum Type
	{

		/** The Buy. */
		Buy(1),

		/** The Sell. */
		Sell(2);

		/** The id. */
		int id;

		/**
		 * Instantiates a new type.
		 * @param id
		 * the id
		 */
		Type(int id)
		{
			this.id = id;
		}
	}

	/** The type. */
	public final Type type;

	/** The player name. */
	public final String playerName;

	/** The item name. */
	public final String itemName;

	/** The quantity. */
	public final int quantity;

	/** The cost. */
	public final double cost;

	/**
	 * Instantiates a new transaction.
	 * @param type
	 * the type
	 * @param playerName
	 * the player name
	 * @param itemName
	 * the item name
	 * @param quantity
	 * the quantity
	 * @param cost
	 * the cost
	 */
	public Transaction(Type type, String playerName, String itemName,
			int quantity, double cost)
	{
		this.type = type;
		this.playerName = playerName;
		this.itemName = itemName;
		this.quantity = quantity;
		this.cost = cost;
	}
}
