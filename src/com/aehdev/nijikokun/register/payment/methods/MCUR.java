package com.aehdev.nijikokun.register.payment.methods;

import com.aehdev.nijikokun.register.payment.Method;

import me.ashtheking.currency.Currency;
import me.ashtheking.currency.CurrencyList;

import org.bukkit.plugin.Plugin;

// TODO: Auto-generated Javadoc
/**
 * MultiCurrency Method implementation.
 * @author Acrobot
 * @copyright (c) 2011
 * @license AOL license <http://aol.nexua.org>
 */
public class MCUR implements Method
{

	/** The currency list. */
	private Currency currencyList;

	/* (non-Javadoc)
	 * @see com.aehdev.nijikokun.register.payment.Method#getPlugin() */
	public Object getPlugin()
	{
		return this.currencyList;
	}

	/* (non-Javadoc)
	 * @see com.aehdev.nijikokun.register.payment.Method#getName() */
	public String getName()
	{
		return "MultiCurrency";
	}

	/* (non-Javadoc)
	 * @see com.aehdev.nijikokun.register.payment.Method#getVersion() */
	public String getVersion()
	{
		return "0.09";
	}

	/* (non-Javadoc)
	 * @see com.aehdev.nijikokun.register.payment.Method#fractionalDigits() */
	public int fractionalDigits()
	{
		return -1;
	}

	/* (non-Javadoc)
	 * @see com.aehdev.nijikokun.register.payment.Method#format(double) */
	public String format(double amount)
	{
		return amount + " Currency";
	}

	/* (non-Javadoc)
	 * @see com.aehdev.nijikokun.register.payment.Method#hasBanks() */
	public boolean hasBanks()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see
	 * com.aehdev.nijikokun.register.payment.Method#hasBank(java.lang.String) */
	public boolean hasBank(String bank)
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see
	 * com.aehdev.nijikokun.register.payment.Method#hasAccount(java.lang.String) */
	public boolean hasAccount(String name)
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see
	 * com.aehdev.nijikokun.register.payment.Method#hasBankAccount(java.lang
	 * .String, java.lang.String) */
	public boolean hasBankAccount(String bank, String name)
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see
	 * com.aehdev.nijikokun.register.payment.Method#getAccount(java.lang.String) */
	public MethodAccount getAccount(String name)
	{
		return new MCurrencyAccount(name);
	}

	/* (non-Javadoc)
	 * @see
	 * com.aehdev.nijikokun.register.payment.Method#getBankAccount(java.lang
	 * .String, java.lang.String) */
	public MethodBankAccount getBankAccount(String bank, String name)
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see
	 * com.aehdev.nijikokun.register.payment.Method#isCompatible(org.bukkit
	 * .plugin.Plugin) */
	public boolean isCompatible(Plugin plugin)
	{
		return (plugin.getDescription().getName().equalsIgnoreCase("Currency") || plugin
				.getDescription().getName().equalsIgnoreCase("MultiCurrency"))
				&& plugin instanceof Currency;
	}

	/* (non-Javadoc)
	 * @see
	 * com.aehdev.nijikokun.register.payment.Method#setPlugin(org.bukkit.plugin
	 * .Plugin) */
	public void setPlugin(Plugin plugin)
	{
		currencyList = (Currency)plugin;
	}

	/**
	 * The Class MCurrencyAccount.
	 */
	public class MCurrencyAccount implements MethodAccount
	{

		/** The name. */
		private String name;

		/**
		 * Instantiates a new m currency account.
		 * @param name
		 * the name
		 */
		public MCurrencyAccount(String name)
		{
			this.name = name;
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#balance() */
		public double balance()
		{
			return CurrencyList.getValue(
					(String)CurrencyList.maxCurrency(name)[0], name);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#set(double
		 * ) */
		public boolean set(double amount)
		{
			CurrencyList.setValue((String)CurrencyList.maxCurrency(name)[0],
					name, amount);
			return true;
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#add(double
		 * ) */
		public boolean add(double amount)
		{
			return CurrencyList.add(name, amount);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#subtract
		 * (double) */
		public boolean subtract(double amount)
		{
			return CurrencyList.subtract(name, amount);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#multiply
		 * (double) */
		public boolean multiply(double amount)
		{
			return CurrencyList.multiply(name, amount);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#divide
		 * (double) */
		public boolean divide(double amount)
		{
			return CurrencyList.divide(name, amount);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#hasEnough
		 * (double) */
		public boolean hasEnough(double amount)
		{
			return CurrencyList.hasEnough(name, amount);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#hasOver
		 * (double) */
		public boolean hasOver(double amount)
		{
			return CurrencyList.hasOver(name, amount);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#hasUnder
		 * (double) */
		public boolean hasUnder(double amount)
		{
			return CurrencyList.hasUnder(name, amount);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#isNegative
		 * () */
		public boolean isNegative()
		{
			return CurrencyList.isNegative(name);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#remove() */
		public boolean remove()
		{
			return CurrencyList.remove(name);
		}
	}
}
