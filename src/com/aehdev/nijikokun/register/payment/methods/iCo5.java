package com.aehdev.nijikokun.register.payment.methods;

import com.aehdev.nijikokun.register.payment.Method;
import com.iConomy.iConomy;
import com.iConomy.system.Account;
import com.iConomy.system.BankAccount;
import com.iConomy.system.Holdings;
import com.iConomy.util.Constants;

import org.bukkit.plugin.Plugin;

// TODO: Auto-generated Javadoc
/**
 * iConomy 5 Implementation of Method.
 * @author Nijikokun <nijikokun@shortmail.com> (@nijikokun)
 * @copyright (c) 2011
 * @license AOL license <http://aol.nexua.org>
 */
public class iCo5 implements Method
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
		return "5";
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
		return com.iConomy.iConomy.format(amount);
	}

	/* (non-Javadoc)
	 * @see com.aehdev.nijikokun.register.payment.Method#hasBanks() */
	public boolean hasBanks()
	{
		return Constants.Banking;
	}

	/* (non-Javadoc)
	 * @see
	 * com.aehdev.nijikokun.register.payment.Method#hasBank(java.lang.String) */
	public boolean hasBank(String bank)
	{
		return (hasBanks()) && com.iConomy.iConomy.Banks.exists(bank);
	}

	/* (non-Javadoc)
	 * @see
	 * com.aehdev.nijikokun.register.payment.Method#hasAccount(java.lang.String) */
	public boolean hasAccount(String name)
	{
		return com.iConomy.iConomy.hasAccount(name);
	}

	/* (non-Javadoc)
	 * @see
	 * com.aehdev.nijikokun.register.payment.Method#hasBankAccount(java.lang
	 * .String, java.lang.String) */
	public boolean hasBankAccount(String bank, String name)
	{
		return (hasBank(bank))
				&& com.iConomy.iConomy.getBank(bank).hasAccount(name);
	}

	/* (non-Javadoc)
	 * @see
	 * com.aehdev.nijikokun.register.payment.Method#getAccount(java.lang.String) */
	public MethodAccount getAccount(String name)
	{
		return new iCoAccount(com.iConomy.iConomy.getAccount(name));
	}

	/* (non-Javadoc)
	 * @see
	 * com.aehdev.nijikokun.register.payment.Method#getBankAccount(java.lang
	 * .String, java.lang.String) */
	public MethodBankAccount getBankAccount(String bank, String name)
	{
		return new iCoBankAccount(com.iConomy.iConomy.getBank(bank).getAccount(
				name));
	}

	/* (non-Javadoc)
	 * @see
	 * com.aehdev.nijikokun.register.payment.Method#isCompatible(org.bukkit
	 * .plugin.Plugin) */
	public boolean isCompatible(Plugin plugin)
	{
		return plugin.getDescription().getName().equalsIgnoreCase("iconomy")
				&& plugin.getClass().getName().equals("com.iConomy.iConomy")
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
			return this.holdings.balance();
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#set(double
		 * ) */
		public boolean set(double amount)
		{
			if(this.holdings == null) return false;
			this.holdings.set(amount);
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

	/**
	 * The Class iCoBankAccount.
	 */
	public class iCoBankAccount implements MethodBankAccount
	{

		/** The account. */
		private BankAccount account;

		/** The holdings. */
		private Holdings holdings;

		/**
		 * Instantiates a new i co bank account.
		 * @param account
		 * the account
		 */
		public iCoBankAccount(BankAccount account)
		{
			this.account = account;
			this.holdings = account.getHoldings();
		}

		/**
		 * Gets the i co bank account.
		 * @return the i co bank account
		 */
		public BankAccount getiCoBankAccount()
		{
			return account;
		}

		/* (non-Javadoc)
		 * @see com.aehdev.nijikokun.register.payment.Method.MethodBankAccount#
		 * getBankName() */
		public String getBankName()
		{
			return this.account.getBankName();
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodBankAccount#getBankId
		 * () */
		public int getBankId()
		{
			return this.account.getBankId();
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodBankAccount#balance
		 * () */
		public double balance()
		{
			return this.holdings.balance();
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodBankAccount#set
		 * (double) */
		public boolean set(double amount)
		{
			if(this.holdings == null) return false;
			this.holdings.set(amount);
			return true;
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodBankAccount#add
		 * (double) */
		public boolean add(double amount)
		{
			if(this.holdings == null) return false;
			this.holdings.add(amount);
			return true;
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodBankAccount#subtract
		 * (double) */
		public boolean subtract(double amount)
		{
			if(this.holdings == null) return false;
			this.holdings.subtract(amount);
			return true;
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodBankAccount#multiply
		 * (double) */
		public boolean multiply(double amount)
		{
			if(this.holdings == null) return false;
			this.holdings.multiply(amount);
			return true;
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodBankAccount#divide
		 * (double) */
		public boolean divide(double amount)
		{
			if(this.holdings == null) return false;
			this.holdings.divide(amount);
			return true;
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodBankAccount#hasEnough
		 * (double) */
		public boolean hasEnough(double amount)
		{
			return this.holdings.hasEnough(amount);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodBankAccount#hasOver
		 * (double) */
		public boolean hasOver(double amount)
		{
			return this.holdings.hasOver(amount);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodBankAccount#hasUnder
		 * (double) */
		public boolean hasUnder(double amount)
		{
			return this.holdings.hasUnder(amount);
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodBankAccount#isNegative
		 * () */
		public boolean isNegative()
		{
			return this.holdings.isNegative();
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodBankAccount#remove
		 * () */
		public boolean remove()
		{
			if(this.account == null) return false;
			this.account.remove();
			return true;
		}
	}
}
