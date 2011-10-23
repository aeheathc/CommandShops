package com.aehdev.nijikokun.register.payment.methods;

import com.aehdev.nijikokun.register.payment.Method;
import com.iCo6.iConomy;
import com.iCo6.system.Account;
import com.iCo6.system.Accounts;
import com.iCo6.system.Holdings;

import org.bukkit.plugin.Plugin;

// TODO: Auto-generated Javadoc
/**
 * iConomy 6 Implementation of Method.
 * @author Nijikokun <nijikokun@shortmail.com> (@nijikokun)
 * @copyright (c) 2011
 * @license AOL license <http://aol.nexua.org>
 */
public class iCo6 implements Method
{

	/** The i conomy. */
	private iConomy iConomy;

	/* (non-Javadoc)
	 * @see com.aehdev.nijikokun.register.payment.Method#getPlugin() */
	public iConomy getPlugin()
	{
		return this.iConomy;
	}

	/* (non-Javadoc)
	 * @see com.aehdev.nijikokun.register.payment.Method#getName() */
	public String getName()
	{
		return "iConomy";
	}

	/* (non-Javadoc)
	 * @see com.aehdev.nijikokun.register.payment.Method#getVersion() */
	public String getVersion()
	{
		return "6";
	}

	/* (non-Javadoc)
	 * @see com.aehdev.nijikokun.register.payment.Method#fractionalDigits() */
	public int fractionalDigits()
	{
		return 2;
	}

	/* (non-Javadoc)
	 * @see com.aehdev.nijikokun.register.payment.Method#format(double) */
	public String format(double amount)
	{
		return com.iCo6.iConomy.format(amount);
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
		return (new Accounts()).exists(name);
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
		return new iCoAccount((new Accounts()).get(name));
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
		return plugin.getDescription().getName().equalsIgnoreCase("iconomy")
				&& plugin.getClass().getName().equals("com.iCo6.iConomy")
				&& plugin instanceof iConomy;
	}

	/* (non-Javadoc)
	 * @see
	 * com.aehdev.nijikokun.register.payment.Method#setPlugin(org.bukkit.plugin
	 * .Plugin) */
	public void setPlugin(Plugin plugin)
	{
		iConomy = (iConomy)plugin;
	}

	/**
	 * The Class iCoAccount.
	 */
	public class iCoAccount implements MethodAccount
	{

		/** The account. */
		private Account account;

		/** The holdings. */
		private Holdings holdings;

		/**
		 * Instantiates a new i co account.
		 * @param account
		 * the account
		 */
		public iCoAccount(Account account)
		{
			this.account = account;
			this.holdings = account.getHoldings();
		}

		/**
		 * Gets the i co account.
		 * @return the i co account
		 */
		public Account getiCoAccount()
		{
			return account;
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#balance() */
		public double balance()
		{
			return this.holdings.getBalance();
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#set(double
		 * ) */
		public boolean set(double amount)
		{
			if(this.holdings == null) return false;
			this.holdings.setBalance(amount);
			return true;
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#add(double
		 * ) */
		public boolean add(double amount)
		{
			if(this.holdings == null) return false;
			this.holdings.add(amount);
			return true;
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#subtract
		 * (double) */
		public boolean subtract(double amount)
		{
			if(this.holdings == null) return false;
			this.holdings.subtract(amount);
			return true;
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#multiply
		 * (double) */
		public boolean multiply(double amount)
		{
			if(this.holdings == null) return false;
			this.holdings.multiply(amount);
			return true;
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#divide
		 * (double) */
		public boolean divide(double amount)
		{
			if(this.holdings == null) return false;
			this.holdings.divide(amount);
			return true;
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#hasEnough
		 * (double) */
		public boolean hasEnough(double amount)
		{
			return this.holdings.hasEnough(amount);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#hasOver
		 * (double) */
		public boolean hasOver(double amount)
		{
			return this.holdings.hasOver(amount);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#hasUnder
		 * (double) */
		public boolean hasUnder(double amount)
		{
			return this.holdings.hasUnder(amount);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#isNegative
		 * () */
		public boolean isNegative()
		{
			return this.holdings.isNegative();
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#remove() */
		public boolean remove()
		{
			if(this.account == null) return false;
			this.account.remove();
			return true;
		}
	}
}
