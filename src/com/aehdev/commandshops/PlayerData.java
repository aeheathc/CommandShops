package com.aehdev.commandshops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.entity.Player;

import com.aehdev.commandshops.modules.economy.EconomyResponse;

import cuboidLocale.BookmarkedResult;

// TODO: Auto-generated Javadoc
/**
 * The Class PlayerData.
 */
public class PlayerData
{
	// Objects
	/** The plugin. */
	private CommandShops plugin = null;

	// Attributes
	/** The shop list. */
	public List<UUID> shopList = Collections
			.synchronizedList(new ArrayList<UUID>());

	/** The bookmark. */
	protected BookmarkedResult bookmark = new BookmarkedResult();

	/** The player name. */
	public String playerName = null;

	/** The is selecting. */
	private boolean isSelecting = false;

	/** The xyz a. */
	private double xyzA[] = null;

	/** The xyz b. */
	private double xyzB[] = null;

	/** The size. */
	protected String size = "";

	// Logging
	/** The Constant log. */
	private static final Logger log = Logger.getLogger("Minecraft");

	// Constructor
	/**
	 * Instantiates a new player data.
	 * @param plugin
	 * the plugin
	 * @param playerName
	 * the player name
	 */
	public PlayerData(CommandShops plugin, String playerName)
	{
		this.plugin = plugin;
		this.playerName = playerName;
	}

	/**
	 * Gets the position a.
	 * @return the position a
	 */
	public double[] getPositionA()
	{
		return xyzA;
	}

	/**
	 * Gets the position b.
	 * @return the position b
	 */
	public double[] getPositionB()
	{
		return xyzB;
	}

	/**
	 * Sets the position a.
	 * @param xyz
	 * the new position a
	 */
	public void setPositionA(double[] xyz)
	{
		xyzA = xyz.clone();
	}

	/**
	 * Sets the position b.
	 * @param xyz
	 * the new position b
	 */
	public void setPositionB(double[] xyz)
	{
		xyzB = xyz.clone();
	}

	/**
	 * Gets the size string.
	 * @return the size string
	 */
	public String getSizeString()
	{
		return size;
	}

	/**
	 * Check size.
	 * @return true, if successful
	 */
	public boolean checkSize()
	{
		if(xyzA == null || xyzB == null){ return false; }

		double width1 = Math.abs(xyzA[0] - xyzB[0]) + 1;
		double height = Math.abs(xyzA[1] - xyzB[1]) + 1;
		double width2 = Math.abs(xyzA[2] - xyzB[2]) + 1;

		size = "" + width1 + "x" + height + "x" + width2;

		if(width1 > Config.MAX_WIDTH
				|| width2 > Config.MAX_WIDTH
				|| height > Config.MAX_HEIGHT)
		{
			return false;
		}else
		{
			return true;
		}
	}

	/**
	 * Adds the player to shop.
	 * @param shop
	 * the shop
	 * @return true, if successful
	 */
	public boolean addPlayerToShop(Shop shop)
	{
		String playerWorld = plugin.getServer().getPlayer(playerName)
				.getWorld().getName();

		if(!playerIsInShop(shop)
				&& shop.getWorld().equalsIgnoreCase(playerWorld))
		{
			shopList.add(shop.getUuid());
			return true;
		}else
		{
			return false;
		}
	}

	/**
	 * Player is in shop.
	 * @param shop
	 * the shop
	 * @return true, if successful
	 */
	public boolean playerIsInShop(Shop shop)
	{
		String playerWorld = plugin.getServer().getPlayer(playerName)
				.getWorld().getName();

		if(shopList.contains(shop.getUuid()))
		{
			if(shop.getWorld().equalsIgnoreCase(playerWorld)){ return true; }
		}
		return false;
	}

	/**
	 * Removes the player from shop.
	 * @param player
	 * the player
	 * @param uuid
	 * the uuid
	 */
	public void removePlayerFromShop(Player player, UUID uuid)
	{
		shopList.remove(uuid);
	}

	/**
	 * Player shops list.
	 * @param playerName
	 * the player name
	 * @return the list
	 */
	public List<UUID> playerShopsList(String playerName)
	{
		return shopList;
	}

	/**
	 * Pay player.
	 * @param playerName
	 * the player name
	 * @param cost
	 * the cost
	 * @return true, if successful
	 */
	public boolean payPlayer(String playerName, double cost)
	{
		EconomyResponse depositResp = plugin.getEconManager().depositPlayer(
				playerName, cost);
		if(depositResp.transactionSuccess())
		{
			return true;
		}else
		{
			return false;
		}
	}

	/**
	 * Pay player.
	 * @param playerFrom
	 * the player from
	 * @param playerTo
	 * the player to
	 * @param cost
	 * the cost
	 * @return true, if successful
	 */
	public boolean payPlayer(String playerFrom, String playerTo, double cost)
	{
		EconomyResponse balanceFromResp = plugin.getEconManager().getBalance(
				playerFrom);
		EconomyResponse balanceToResp = plugin.getEconManager().getBalance(
				playerTo);

		log.info("PlayerFrom: " + playerFrom + " balanceFrom: "
				+ balanceFromResp.amount + " PlayerTo: " + playerTo
				+ " balanceTo: " + balanceToResp.amount + " Cost: " + cost);

		EconomyResponse withdrawResp = plugin.getEconManager().withdrawPlayer(
				playerFrom, cost);
		if(!withdrawResp.transactionSuccess())
		{
			log.info("Failed to withdraw");
			return false;
		}

		EconomyResponse depositResp = plugin.getEconManager().depositPlayer(
				playerTo, cost);
		if(!depositResp.transactionSuccess())
		{
			log.info("Failed to deposit");
			// Return money to shop owner
			EconomyResponse returnResp = plugin.getEconManager().depositPlayer(
					playerFrom, cost);
			if(!returnResp.transactionSuccess())
			{
				log.warning(String
						.format("[%s] ERROR:  Payment failed and could not return funds to original state!  %s may need %s!",
								plugin.pdfFile.getName(), playerName, plugin
										.getEconManager().format(cost)));
			}
			return false;
		}

		if(withdrawResp.transactionSuccess()
				&& depositResp.transactionSuccess())
		{
			plugin.getShopData().logPayment(playerFrom, "payment",
					withdrawResp.amount, balanceFromResp.amount,
					withdrawResp.balance);
			plugin.getShopData().logPayment(playerTo, "payment",
					depositResp.amount, balanceToResp.amount,
					depositResp.balance);
			return true;
		}else
		{
			return false;
		}
	}

	/**
	 * Gets the balance.
	 * @param playerName
	 * the player name
	 * @return the balance
	 */
	public double getBalance(String playerName)
	{
		EconomyResponse balanceResp = plugin.getEconManager().getBalance(
				playerName);
		return balanceResp.amount;
	}

	/**
	 * Charge player.
	 * @param playerName
	 * the player name
	 * @param chargeAmount
	 * the charge amount
	 * @return true, if successful
	 */
	public boolean chargePlayer(String playerName, double chargeAmount)
	{
		EconomyResponse balanceResp = plugin.getEconManager().getBalance(
				playerName);
		if(!balanceResp.transactionSuccess()){ return false; }

		EconomyResponse withdrawResp = plugin.getEconManager().withdrawPlayer(
				playerName, chargeAmount);
		if(withdrawResp.transactionSuccess())
		{
			plugin.getShopData().logPayment(playerName, "payment",
					withdrawResp.amount, balanceResp.balance,
					withdrawResp.balance);
			return true;
		}else
		{
			return false;
		}
	}

	/**
	 * Gets the current shop.
	 * @return the current shop
	 */
	public UUID getCurrentShop()
	{
		if(shopList.size() == 1)
		{
			return shopList.get(0);
		}else
		{
			return null;
		}
	}

	/**
	 * Sets the selecting.
	 * @param isSelecting
	 * the new selecting
	 */
	public void setSelecting(boolean isSelecting)
	{
		this.isSelecting = isSelecting;
	}

	/**
	 * Checks if is selecting.
	 * @return true, if is selecting
	 */
	public boolean isSelecting()
	{
		return isSelecting;
	}

}
