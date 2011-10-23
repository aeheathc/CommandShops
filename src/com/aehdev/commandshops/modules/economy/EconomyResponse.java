package com.aehdev.commandshops.modules.economy;

// TODO: Auto-generated Javadoc
/**
 * The Class EconomyResponse.
 */
public class EconomyResponse
{

	/**
	 * The Enum ResponseType.
	 */
	public static enum ResponseType
	{

		/** The SUCCESS. */
		SUCCESS(1),

		/** The FAILURE. */
		FAILURE(2),

		/** The NO t_ implemented. */
		NOT_IMPLEMENTED(3);

		/** The id. */
		private int id;

		/**
		 * Instantiates a new response type.
		 * @param id
		 * the id
		 */
		ResponseType(int id)
		{
			this.id = id;
		}

		/**
		 * Gets the id.
		 * @return the id
		 */
		int getId()
		{
			return id;
		}
	}

	/** The amount. */
	public final double amount;

	/** The balance. */
	public final double balance;

	/** The type. */
	public final ResponseType type;

	/** The error message. */
	public final String errorMessage;

	/**
	 * Instantiates a new economy response.
	 * @param amount
	 * the amount
	 * @param balance
	 * the balance
	 * @param type
	 * the type
	 * @param errorMessage
	 * the error message
	 */
	public EconomyResponse(double amount, double balance, ResponseType type,
			String errorMessage)
	{
		this.amount = amount;
		this.balance = balance;
		this.type = type;
		this.errorMessage = errorMessage;
	}

	/**
	 * Transaction success.
	 * @return true, if successful
	 */
	public boolean transactionSuccess()
	{
		switch(type)
		{
			case SUCCESS:
			return true;
			default:
			return false;
		}
	}
}
