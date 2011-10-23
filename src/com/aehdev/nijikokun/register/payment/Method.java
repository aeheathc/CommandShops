package com.aehdev.nijikokun.register.payment;

import org.bukkit.plugin.Plugin;

// TODO: Auto-generated Javadoc
/**
 * Interface to be implemented by a payment method.
 * @author Nijikokun <nijikokun@shortmail.com> (@nijikokun)
 * @copyright Copyright (C) 2011
 * @license AOL license <http://aol.nexua.org>
 */
public interface Method
{

	/**
	 * Encodes the Plugin into an Object disguised as the Plugin. If you want
	 * the original Plugin Class you must cast it to the correct Plugin, to do
	 * so you have to verify the name and or version then cast.
	 * 
	 * <pre>
	 * if(method.getName().equalsIgnoreCase("iConomy"))
	 * iConomy plugin = ((iConomy)method.getPlugin());
	 * </pre>
	 * @return
	 * @see #getName()
	 * @see #getVersion()
	 */
	public Object getPlugin();

	/**
	 * Returns the actual name of this method.
	 * @return Plugin name.
	 */
	public String getName();

	/**
	 * Returns the actual version of this method.
	 * @return Plugin version.
	 */
	public String getVersion();

	/**
	 * Returns the amount of decimal places that get stored NOTE: it will return
	 * -1 if there is no rounding.
	 * @return for each decimal place
	 */
	public int fractionalDigits();

	/**
	 * Formats amounts into this payment methods style of currency display.
	 * @param amount
	 * Double
	 * @return - Formatted Currency Display.
	 */
	public String format(double amount);

	/**
	 * Allows the verification of bank API existence in this payment method.
	 * @return
	 */
	public boolean hasBanks();

	/**
	 * Determines the existence of a bank via name.
	 * @param bank
	 * Bank name
	 * @return
	 * @see #hasBanks
	 */
	public boolean hasBank(String bank);

	/**
	 * Determines the existence of an account via name.
	 * @param name
	 * Account name
	 * @return
	 */
	public boolean hasAccount(String name);

	/**
	 * Check to see if an account <code>name</code> is tied to a
	 * <code>bank</code>.
	 * @param bank
	 * Bank name
	 * @param name
	 * Account name
	 * @return
	 */
	public boolean hasBankAccount(String bank, String name);

	/**
	 * Returns a <code>MethodAccount</code> class for an account
	 * <code>name</code>.
	 * @param name
	 * Account name
	 * @return
	 */
	public MethodAccount getAccount(String name);

	/**
	 * Returns a <code>MethodBankAccount</code> class for an account
	 * <code>name</code>.
	 * @param bank
	 * Bank name
	 * @param name
	 * Account name
	 * @return
	 */
	public MethodBankAccount getBankAccount(String bank, String name);

	/**
	 * Checks to verify the compatibility between this Method and a plugin.
	 * Internal usage only, for the most part.
	 * @param plugin
	 * Plugin
	 * @return
	 */
	public boolean isCompatible(Plugin plugin);

	/**
	 * Set Plugin data.
	 * @param plugin
	 * Plugin
	 */
	public void setPlugin(Plugin plugin);

	/**
	 * Contains Calculator and Balance functions for Accounts.
	 */
	public interface MethodAccount
	{

		/**
		 * Balance.
		 * @return the double
		 */
		public double balance();

		/**
		 * Sets the.
		 * @param amount
		 * the amount
		 * @return true, if successful
		 */
		public boolean set(double amount);

		/**
		 * Adds the.
		 * @param amount
		 * the amount
		 * @return true, if successful
		 */
		public boolean add(double amount);

		/**
		 * Subtract.
		 * @param amount
		 * the amount
		 * @return true, if successful
		 */
		public boolean subtract(double amount);

		/**
		 * Multiply.
		 * @param amount
		 * the amount
		 * @return true, if successful
		 */
		public boolean multiply(double amount);

		/**
		 * Divide.
		 * @param amount
		 * the amount
		 * @return true, if successful
		 */
		public boolean divide(double amount);

		/**
		 * Checks for enough.
		 * @param amount
		 * the amount
		 * @return true, if successful
		 */
		public boolean hasEnough(double amount);

		/**
		 * Checks for over.
		 * @param amount
		 * the amount
		 * @return true, if successful
		 */
		public boolean hasOver(double amount);

		/**
		 * Checks for under.
		 * @param amount
		 * the amount
		 * @return true, if successful
		 */
		public boolean hasUnder(double amount);

		/**
		 * Checks if is negative.
		 * @return true, if is negative
		 */
		public boolean isNegative();

		/**
		 * Removes the.
		 * @return true, if successful
		 */
		public boolean remove();

		/**
		 * To string.
		 * @return the string
		 */
		@Override
		public String toString();
	}

	/**
	 * Contains Calculator and Balance functions for Bank Accounts.
	 */
	public interface MethodBankAccount
	{

		/**
		 * Balance.
		 * @return the double
		 */
		public double balance();

		/**
		 * Gets the bank name.
		 * @return the bank name
		 */
		public String getBankName();

		/**
		 * Gets the bank id.
		 * @return the bank id
		 */
		public int getBankId();

		/**
		 * Sets the.
		 * @param amount
		 * the amount
		 * @return true, if successful
		 */
		public boolean set(double amount);

		/**
		 * Adds the.
		 * @param amount
		 * the amount
		 * @return true, if successful
		 */
		public boolean add(double amount);

		/**
		 * Subtract.
		 * @param amount
		 * the amount
		 * @return true, if successful
		 */
		public boolean subtract(double amount);

		/**
		 * Multiply.
		 * @param amount
		 * the amount
		 * @return true, if successful
		 */
		public boolean multiply(double amount);

		/**
		 * Divide.
		 * @param amount
		 * the amount
		 * @return true, if successful
		 */
		public boolean divide(double amount);

		/**
		 * Checks for enough.
		 * @param amount
		 * the amount
		 * @return true, if successful
		 */
		public boolean hasEnough(double amount);

		/**
		 * Checks for over.
		 * @param amount
		 * the amount
		 * @return true, if successful
		 */
		public boolean hasOver(double amount);

		/**
		 * Checks for under.
		 * @param amount
		 * the amount
		 * @return true, if successful
		 */
		public boolean hasUnder(double amount);

		/**
		 * Checks if is negative.
		 * @return true, if is negative
		 */
		public boolean isNegative();

		/**
		 * Removes the.
		 * @return true, if successful
		 */
		public boolean remove();

		/**
		 * To string.
		 * @return the string
		 */
		@Override
		public String toString();
	}
}
