package com.aehdev.nijikokun.register.payment.methods;

import com.aehdev.nijikokun.register.payment.Method;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;

import org.bukkit.plugin.Plugin;

// TODO: Auto-generated Javadoc
/**
 * Essentials 17 Implementation of Method.
 * @author Nijikokun <nijikokun@shortmail.com> (@nijikokun)
 * @author Snowleo
 * @author Acrobot
 * @author KHobbits
 * @copyright (c) 2011
 * @license AOL license <http://aol.nexua.org>
 */
public class EE17 implements Method
{

	/** The Essentials. */
	private Essentials Essentials;

	/* (non-Javadoc)
	 * @see com.aehdev.nijikokun.register.payment.Method#getPlugin() */
	public Essentials getPlugin()
	{
		return this.Essentials;
	}

	/* (non-Javadoc)
	 * @see com.aehdev.nijikokun.register.payment.Method#getName() */
	public String getName()
	{
		return "Essentials";
	}

	/* (non-Javadoc)
	 * @see com.aehdev.nijikokun.register.payment.Method#getVersion() */
	public String getVersion()
	{
		return "2.2";
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
		return Economy.format(amount);
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
		return Economy.playerExists(name);
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
		if(!hasAccount(name)) return null;

		return new EEcoAccount(name);
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
		try
		{
			Class.forName("com.earth2me.essentials.api.Economy");
		}catch(Exception e)
		{
			return false;
		}

		return plugin.getDescription().getName().equalsIgnoreCase("essentials")
				&& plugin instanceof Essentials;
	}

	/* (non-Javadoc)
	 * @see
	 * com.aehdev.nijikokun.register.payment.Method#setPlugin(org.bukkit.plugin
	 * .Plugin) */
	public void setPlugin(Plugin plugin)
	{
		Essentials = (Essentials)plugin;
	}

	/**
	 * The Class EEcoAccount.
	 */
	public class EEcoAccount implements MethodAccount
	{

		/** The name. */
		private String name;

		/**
		 * Instantiates a new e eco account.
		 * @param name
		 * the name
		 */
		public EEcoAccount(String name)
		{
			this.name = name;
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#balance() */
		public double balance()
		{
			Double balance = 0.0;

			try
			{
				balance = Economy.getMoney(this.name);
			}catch(UserDoesNotExistException ex)
			{
				System.out
						.println("[REGISTER] Failed to grab balance in Essentials Economy: "
								+ ex.getMessage());
			}

			return balance;
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#set(double
		 * ) */
		public boolean set(double amount)
		{
			try
			{
				Economy.setMoney(name, amount);
			}catch(UserDoesNotExistException ex)
			{
				System.out
						.println("[REGISTER] User does not exist in Essentials Economy: "
								+ ex.getMessage());
				return false;
			}catch(NoLoanPermittedException ex)
			{
				System.out
						.println("[REGISTER] No loan permitted in Essentials Economy: "
								+ ex.getMessage());
				return false;
			}

			return true;
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#add(double
		 * ) */
		public boolean add(double amount)
		{
			try
			{
				Economy.add(name, amount);
			}catch(UserDoesNotExistException ex)
			{
				System.out
						.println("[REGISTER] User does not exist in Essentials Economy: "
								+ ex.getMessage());
				return false;
			}catch(NoLoanPermittedException ex)
			{
				System.out
						.println("[REGISTER] No loan permitted in Essentials Economy: "
								+ ex.getMessage());
				return false;
			}

			return true;
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#subtract
		 * (double) */
		public boolean subtract(double amount)
		{
			try
			{
				Economy.subtract(name, amount);
			}catch(UserDoesNotExistException ex)
			{
				System.out
						.println("[REGISTER] User does not exist in Essentials Economy: "
								+ ex.getMessage());
				return false;
			}catch(NoLoanPermittedException ex)
			{
				System.out
						.println("[REGISTER] No loan permitted in Essentials Economy: "
								+ ex.getMessage());
				return false;
			}

			return true;
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#multiply
		 * (double) */
		public boolean multiply(double amount)
		{
			try
			{
				Economy.multiply(name, amount);
			}catch(UserDoesNotExistException ex)
			{
				System.out
						.println("[REGISTER] User does not exist in Essentials Economy: "
								+ ex.getMessage());
				return false;
			}catch(NoLoanPermittedException ex)
			{
				System.out
						.println("[REGISTER] No loan permitted in Essentials Economy: "
								+ ex.getMessage());
				return false;
			}

			return true;
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#divide
		 * (double) */
		public boolean divide(double amount)
		{
			try
			{
				Economy.divide(name, amount);
			}catch(UserDoesNotExistException ex)
			{
				System.out
						.println("[REGISTER] User does not exist in Essentials Economy: "
								+ ex.getMessage());
				return false;
			}catch(NoLoanPermittedException ex)
			{
				System.out
						.println("[REGISTER] No loan permitted in Essentials Economy: "
								+ ex.getMessage());
				return false;
			}

			return true;
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#hasEnough
		 * (double) */
		public boolean hasEnough(double amount)
		{
			try
			{
				return Economy.hasEnough(name, amount);
			}catch(UserDoesNotExistException ex)
			{
				System.out
						.println("[REGISTER] User does not exist in Essentials Economy: "
								+ ex.getMessage());
			}

			return false;
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#hasOver
		 * (double) */
		public boolean hasOver(double amount)
		{
			try
			{
				return Economy.hasMore(name, amount);
			}catch(UserDoesNotExistException ex)
			{
				System.out
						.println("[REGISTER] User does not exist in Essentials Economy: "
								+ ex.getMessage());
			}

			return false;
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#hasUnder
		 * (double) */
		public boolean hasUnder(double amount)
		{
			try
			{
				return Economy.hasLess(name, amount);
			}catch(UserDoesNotExistException ex)
			{
				System.out
						.println("[REGISTER] User does not exist in Essentials Economy: "
								+ ex.getMessage());
			}

			return false;
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#isNegative
		 * () */
		public boolean isNegative()
		{
			try
			{
				return Economy.isNegative(name);
			}catch(UserDoesNotExistException ex)
			{
				System.out
						.println("[REGISTER] User does not exist in Essentials Economy: "
								+ ex.getMessage());
			}

			return false;
		}

		/* (non-Javadoc)
		 * @see
		 * com.aehdev.nijikokun.register.payment.Method.MethodAccount#remove() */
		public boolean remove()
		{
			return false;
		}
	}
}
