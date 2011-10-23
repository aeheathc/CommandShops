package com.aehdev.commandshops.modules.economy;

import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.aehdev.commandshops.RegisterListener;
import com.nijikokun.register.payment.Method;
import com.nijikokun.register.payment.Methods;
import com.nijikokun.register.payment.Method.MethodAccount;

// TODO: Auto-generated Javadoc
/**
 * The Class EconomyManager.
 */
public class EconomyManager
{

	/** The plugin. */
	private Plugin plugin = null;

	/** The plugin manager. */
	private PluginManager pluginManager = null;

	/** The register listener. */
	private RegisterListener registerListener = null;

	/**
	 * Instantiates a new economy manager.
	 * @param plugin
	 * the plugin
	 */
	public EconomyManager(Plugin plugin)
	{
		this.plugin = plugin;
		this.pluginManager = this.plugin.getServer().getPluginManager();
		registerListener = new RegisterListener(plugin);
		this.pluginManager.registerEvent(Type.PLUGIN_ENABLE, registerListener,
				Priority.Monitor, plugin);
		this.pluginManager.registerEvent(Type.PLUGIN_DISABLE, registerListener,
				Priority.Monitor, plugin);
	}

	/**
	 * Gets the name.
	 * @return the name
	 */
	public String getName()
	{
		return Methods.hasMethod() ? Methods.getMethod().getName() : "None";
	}

	/**
	 * Format.
	 * @param amount
	 * the amount
	 * @return the string
	 */
	public String format(double amount)
	{
		return Methods.getMethod().format(amount);
	}

	/**
	 * Gets the balance.
	 * @param playerName
	 * the player name
	 * @return the balance
	 */
	public EconomyResponse getBalance(String playerName)
	{
		double balance = 0;
		EconomyResponse.ResponseType type = EconomyResponse.ResponseType.FAILURE;
		String errorMessage = null;
		if(!Methods.hasMethod())
		{
			errorMessage = "Register has no economy";
		}else{
			Method method = Methods.getMethod();
			if(!method.hasAccount(playerName))
			{
				errorMessage = "Register couldn't find player";
			}else{
				MethodAccount account = method.getAccount(playerName);
				balance = account.balance();
				type = EconomyResponse.ResponseType.SUCCESS;
			}
		}

		return new EconomyResponse(balance, balance, type, errorMessage);
	}

	/**
	 * Withdraw player.
	 * @param playerName
	 * the player name
	 * @param amount
	 * the amount
	 * @return the economy response
	 */
	public EconomyResponse withdrawPlayer(String playerName, double amount)
	{
		double balance = 0;
		EconomyResponse.ResponseType type = EconomyResponse.ResponseType.FAILURE;
		String errorMessage = null;
		if(!Methods.hasMethod())
		{
			errorMessage = "Register has no economy";
		}else{
			Method method = Methods.getMethod();
			if(!method.hasAccount(playerName))
			{
				errorMessage = "Register couldn't find player";
			}else{
				MethodAccount account = method.getAccount(playerName);
				if(!account.hasEnough(amount))
				{
					balance = account.balance();
					errorMessage = "Insufficient funds";
				}else{
					boolean worked = account.subtract(amount);
					balance = account.balance();
					if(worked)
					{
						type = EconomyResponse.ResponseType.SUCCESS;
					}else{
						errorMessage = "Register couldn't subtract amount.";
					}
				}
			}
		}

		return new EconomyResponse(balance, balance, type, errorMessage);
	}

	/**
	 * Deposit player.
	 * @param playerName
	 * the player name
	 * @param amount
	 * the amount
	 * @return the economy response
	 */
	public EconomyResponse depositPlayer(String playerName, double amount)
	{
		double balance = 0;
		EconomyResponse.ResponseType type = EconomyResponse.ResponseType.FAILURE;
		String errorMessage = null;
		if(!Methods.hasMethod())
		{
			errorMessage = "Register has no economy";
		}else{
			Method method = Methods.getMethod();
			if(!method.hasAccount(playerName))
			{
				errorMessage = "Register couldn't find player";
			}else{
				MethodAccount account = method.getAccount(playerName);
				boolean worked = account.add(amount);
				balance = account.balance();
				if(worked)
				{
					type = EconomyResponse.ResponseType.SUCCESS;
				}else{
					errorMessage = "Register couldn't add amount.";
				}
			}
		}

		return new EconomyResponse(balance, balance, type, errorMessage);
	}
}
