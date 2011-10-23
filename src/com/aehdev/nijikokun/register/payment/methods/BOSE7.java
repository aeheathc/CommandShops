package com.aehdev.nijikokun.register.payment.methods;

import com.aehdev.nijikokun.register.payment.Method;

import cosine.boseconomy.BOSEconomy;
import org.bukkit.plugin.Plugin;

// TODO: Auto-generated Javadoc
/**
 * BOSEconomy 7 Implementation of Method.
 * @author Acrobot
 * @author Nijikokun <nijikokun@shortmail.com> (@nijikokun)
 * @copyright (c) 2011
 * @license AOL license <http://aol.nexua.org>
 */
public class BOSE7 implements Method
{

	/** The BOS economy. */
	private BOSEconomy BOSEconomy;

	/* (non-Javadoc)
	 * @see com.aehdev.nijikokun.register.payment.Method#getPlugin() */
	public BOSEconomy getPlugin()
	{
		return this.BOSEconomy;
	}

	/* (non-Javadoc)
	 * @see com.aehdev.nijikokun.register.payment.Method#getName() */
	public String getName()
	{
		return "BOSEconomy";
	}

	/* (non-Javadoc)
	 * @see com.aehdev.nijikokun.register.payment.Method#getVersion() */
	public String getVersion()
	{
		return "0.7.0";
	}

	/* (non-Javadoc)
	 * @see com.aehdev.nijikokun.register.payment.Method#fractionalDigits() */
	public int fractionalDigits()
	{
		return this.BOSEconomy.getFractionalDigits();
	}

	/* (non-Javadoc)
	 * @see com.aehdev.nijikokun.register.payment.Method#format(double) */
	public String format(double amount)
	{
		String currency = this.BOSEconomy.getMoneyNamePlural();

		if(amount == 1) currency = this.BOSEconomy.getMoneyName();

		return amount + " " + currency;
	}

	/* (non-Javadoc)
	 * @see com.aehdev.nijikokun.register.payment.Method#hasBanks() */
	public boolean hasBanks()
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see
	 * com.aehdev.nijikokun.register.payment.Method#hasBank(java.lang.String) */
	public boolean hasBank(String bank)
	{
		return this.BOSEconomy.bankExists(bank);
	}

	/* (non-Javadoc)
	 * @see
	 * com.aehdev.nijikokun.register.payment.Method#hasAccount(java.lang.String) */
	public boolean hasAccount(String name)
	{
		return this.BOSEconomy.playerRegistered(name, false);
	}

	/* (non-Javadoc)
	 * @see
	 * com.aehdev.nijikokun.register.payment.Method#hasBankAccount(java.lang
	 * .String, java.lang.String) */
	public boolean hasBankAccount(String bank, String name)
	{
		return this.BOSEconomy.isBankOwner(bank, name)
				|| this.BOSEconomy.isBankMember(bank, name);
	}

	/* (non-Javadoc)
	 * @see
	 * com.aehdev.nijikokun.register.payment.Method#getAccount(java.lang.String) */
	public MethodAccount getAccount(String name)
	{
		if(!hasAccount(name)) return null;

		return new BOSEAccount(name, this.BOSEconomy);
	}

	/* (non-Javadoc)
	 * @see
	 * com.aehdev.nijikokun.register.payment.Method#getBankAccount(java.lang
	 * .String, java.lang.String) */
	public MethodBankAccount getBankAccount(String bank, String name)
	{
		if(!hasBankAccount(bank, name)) return null;

		return new BOSEBankAccount(bank, BOSEconomy);
	}

	/* (non-Javadoc)
	 * @see
	 * com.aehdev.nijikokun.register.payment.Method#isCompatible(org.bukkit
	 * .plugin.Plugin) */
	public boolean isCompatible(Plugin plugin)
	{
		return plugin.getDescription().getName().equalsIgnoreCase("boseconomy")
				&& plugin instanceof BOSEconomy
				&& !plugin.getDescription().getVersion().equals("0.6.2");
	}

	/* (non-Javadoc)
	 * @see
	 * com.aehdev.nijikokun.register.payment.Method#setPlugin(org.bukkit.plugin
	 * .Plugin) */
	public void setPlugin(Plugin plugin)
	{
		BOSEconomy = (BOSEconomy)plugin;
	}

	/**
	 * The Class BOSEAccount.
	 */
	public class BOSEAccount implements MethodAccount
	{

		/** The name. */
		private String name;

		/** The BOS economy. */
		private BOSEconomy BOSEconomy;

		/**
		 * Instantiates a new bOSE account.
		 * @param name
		 * the name
		 * @param bOSEconomy
		 * the b os economy
		 */
		public BOSEAccount(String name, BOSEconomy bOSEconomy)
		{
			this.name = name;
			this.BOSEconomy = bOSEconomy;
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#balance() */
		public double balance()
		{
			return this.BOSEconomy.getPlayerMoneyDouble(this.name);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#set(double
		 * ) */
		public boolean set(double amount)
		{
			return this.BOSEconomy.setPlayerMoney(this.name, amount, false);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#add(double
		 * ) */
		public boolean add(double amount)
		{
			return this.BOSEconomy.addPlayerMoney(this.name, amount, false);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#subtract
		 * (double) */
		public boolean subtract(double amount)
		{
			double balance = this.balance();
			return this.BOSEconomy.setPlayerMoney(this.name,
					(balance - amount), false);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#multiply
		 * (double) */
		public boolean multiply(double amount)
		{
			double balance = this.balance();
			return this.BOSEconomy.setPlayerMoney(this.name,
					(balance * amount), false);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#divide
		 * (double) */
		public boolean divide(double amount)
		{
			double balance = this.balance();
			return this.BOSEconomy.setPlayerMoney(this.name,
					(balance / amount), false);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#hasEnough
		 * (double) */
		public boolean hasEnough(double amount)
		{
			return (this.balance() >= amount);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#hasOver
		 * (double) */
		public boolean hasOver(double amount)
		{
			return (this.balance() > amount);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#hasUnder
		 * (double) */
		public boolean hasUnder(double amount)
		{
			return (this.balance() < amount);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#isNegative
		 * () */
		public boolean isNegative()
		{
			return (this.balance() < 0);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#remove() */
		public boolean remove()
		{
			return false;
		}
	}

	/**
	 * The Class BOSEBankAccount.
	 */
	public class BOSEBankAccount implements MethodBankAccount
	{

		/** The bank. */
		private String bank;

		/** The BOS economy. */
		private BOSEconomy BOSEconomy;

		/**
		 * Instantiates a new bOSE bank account.
		 * @param bank
		 * the bank
		 * @param bOSEconomy
		 * the b os economy
		 */
		public BOSEBankAccount(String bank, BOSEconomy bOSEconomy)
		{
			this.bank = bank;
			this.BOSEconomy = bOSEconomy;
		}

		/* (non-Javadoc)
		 * @see com.aehdev.nijikokun.register.payment.Method.MethodBankAccount#
		 * getBankName() */
		public String getBankName()
		{
			return this.bank;
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodBankAccount#getBankId
		 * () */
		public int getBankId()
		{
			return -1;
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodBankAccount#balance
		 * () */
		public double balance()
		{
			return this.BOSEconomy.getBankMoneyDouble(bank);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodBankAccount#set
		 * (double) */
		public boolean set(double amount)
		{
			return this.BOSEconomy.setBankMoney(bank, amount, true);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodBankAccount#add
		 * (double) */
		public boolean add(double amount)
		{
			double balance = this.balance();
			return this.BOSEconomy
					.setBankMoney(bank, (balance + amount), false);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodBankAccount#subtract
		 * (double) */
		public boolean subtract(double amount)
		{
			double balance = this.balance();
			return this.BOSEconomy
					.setBankMoney(bank, (balance - amount), false);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodBankAccount#multiply
		 * (double) */
		public boolean multiply(double amount)
		{
			double balance = this.balance();
			return this.BOSEconomy
					.setBankMoney(bank, (balance * amount), false);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodBankAccount#divide
		 * (double) */
		public boolean divide(double amount)
		{
			double balance = this.balance();
			return this.BOSEconomy
					.setBankMoney(bank, (balance / amount), false);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodBankAccount#hasEnough
		 * (double) */
		public boolean hasEnough(double amount)
		{
			return (this.balance() >= amount);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodBankAccount#hasOver
		 * (double) */
		public boolean hasOver(double amount)
		{
			return (this.balance() > amount);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodBankAccount#hasUnder
		 * (double) */
		public boolean hasUnder(double amount)
		{
			return (this.balance() < amount);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodBankAccount#isNegative
		 * () */
		public boolean isNegative()
		{
			return (this.balance() < 0);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodBankAccount#remove
		 * () */
		public boolean remove()
		{
			return this.BOSEconomy.removeBank(bank);
		}
	}
}
