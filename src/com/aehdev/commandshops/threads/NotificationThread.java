package com.aehdev.commandshops.threads;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.aehdev.commandshops.CommandShops;
import com.aehdev.commandshops.Config;
import com.aehdev.commandshops.Shop;
import com.aehdev.commandshops.Transaction;

// TODO: Auto-generated Javadoc
/**
 * The Class NotificationThread.
 */
public class NotificationThread extends Thread
{

	/** The plugin. */
	private CommandShops plugin;

	/** The run. */
	private boolean run = true;

	/** The log. */
	protected final Logger log = Logger.getLogger("Minecraft");

	/**
	 * Instantiates a new notification thread.
	 * @param plugin
	 * the plugin
	 */
	public NotificationThread(CommandShops plugin)
	{
		this.plugin = plugin;
	}

	/**
	 * Sets the run.
	 * @param run
	 * the new run
	 */
	public void setRun(boolean run)
	{
		this.run = run;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run() */
	public void run()
	{
		log.info(String.format(
				"[%s] Starting NotificationThread with Timer of %d seconds",
				plugin.getDescription().getName(),
				Config.NOTIFY_INTERVAL));

		while(true)
		{
			List<Shop> shops = plugin.getShopData().getAllShops();
			for(final Shop shop: shops)
			{
				if(!shop.getNotification())
				{
					shop.clearTransactions();
					continue;
				}

				Queue<Transaction> transactions = shop.getTransactions();
				if(transactions.size() == 0)
				{
					continue;
				}

				final Player player = plugin.getServer().getPlayer(
						shop.getOwner());
				if(player == null || !player.isOnline())
				{
					continue;
				}

				final ArrayList<String> messages = new ArrayList<String>();

				if(transactions.size() <= 4)
				{
					// List the last 4 transactions...
					messages.add(String.format(ChatColor.WHITE + "%d "
							+ ChatColor.DARK_AQUA + "transactions for "
							+ ChatColor.WHITE + "%s", transactions.size(),
							shop.getName()));
					for(Transaction trans: transactions)
					{
						switch(trans.type)
						{
							case Buy:
							messages.add(String.format(ChatColor.WHITE
									+ "   %s " + ChatColor.GOLD + "sold "
									+ ChatColor.WHITE + "%d %s"
									+ ChatColor.DARK_AQUA + " for "
									+ ChatColor.WHITE + "%s", trans.playerName,
									trans.quantity, trans.itemName, plugin
											.getEconManager()
											.format(trans.cost)));
							break;
							case Sell:
							messages.add(String.format(ChatColor.WHITE
									+ "   %s " + ChatColor.GREEN + "purchased "
									+ ChatColor.WHITE + "%d %s"
									+ ChatColor.DARK_AQUA + " for "
									+ ChatColor.WHITE + " %s",
									trans.playerName, trans.quantity,
									trans.itemName, plugin.getEconManager()
											.format(trans.cost)));
							break;

							default:
							// ruh roh lets ignore it
						}
					}
				}else
				{
					// Summarize the transactions
					double buyCostTotal = 0;
					HashMap<String,Double> itemBuyCost = new HashMap<String,Double>();
					HashMap<String,Integer> itemBuyQuantity = new HashMap<String,Integer>();

					double sellCostTotal = 0;
					HashMap<String,Double> itemSellCost = new HashMap<String,Double>();
					HashMap<String,Integer> itemSellQuantity = new HashMap<String,Integer>();

					ArrayList<String> players = new ArrayList<String>();

					for(Transaction trans: transactions)
					{
						if(trans.type == Transaction.Type.Sell)
						{
							if(itemSellCost.containsKey(trans.itemName))
							{
								itemSellCost.put(trans.itemName,
										itemSellCost.get(trans.itemName)
												+ trans.cost);
							}else
							{
								itemSellCost.put(trans.itemName, trans.cost);
							}

							if(itemSellQuantity.containsKey(trans.itemName))
							{
								itemSellQuantity.put(trans.itemName,
										itemSellQuantity.get(trans.itemName)
												+ trans.quantity);
							}else
							{
								itemSellQuantity.put(trans.itemName,
										trans.quantity);
							}

							if(!players.contains(trans.playerName))
							{
								players.add(trans.playerName);
							}

							buyCostTotal += trans.cost;

						}else if(trans.type == Transaction.Type.Buy)
						{
							if(itemBuyCost.containsKey(trans.itemName))
							{
								itemBuyCost.put(trans.itemName,
										itemBuyCost.get(trans.itemName)
												+ trans.cost);
							}else
							{
								itemBuyCost.put(trans.itemName, trans.cost);
							}

							if(itemBuyQuantity.containsKey(trans.itemName))
							{
								itemBuyQuantity.put(trans.itemName,
										itemBuyQuantity.get(trans.itemName)
												+ trans.quantity);
							}else
							{
								itemBuyQuantity.put(trans.itemName,
										trans.quantity);
							}

							if(!players.contains(trans.playerName))
							{
								players.add(trans.playerName);
							}

							sellCostTotal += trans.cost;
						}
					}

					// Create messages :D
					messages.add(String.format(ChatColor.WHITE + "%d "
							+ ChatColor.DARK_AQUA + "transactions for "
							+ ChatColor.WHITE + "%s", transactions.size(),
							shop.getName()));
					messages.add(String.format(ChatColor.WHITE + "Totals: "
							+ ChatColor.GREEN + "Gained %s, " + ChatColor.GOLD
							+ "Lost %s",
							plugin.getEconManager().format(buyCostTotal),
							plugin.getEconManager().format(sellCostTotal)));
					String g = "";
					for(String item: itemSellCost.keySet())
					{
						if(g.equals(""))
						{
							g += ChatColor.GREEN + item;
						}else
						{
							g += " " + item;
						}
					}
					String l = "";
					for(String item: itemBuyCost.keySet())
					{
						if(l.equals(""))
						{
							l += ChatColor.GOLD + item;
						}else
						{
							l += " " + item;
						}
					}

					messages.add(String.format(ChatColor.WHITE + "   Sold: %s",
							g));
					messages.add(String.format(ChatColor.WHITE
							+ "   Bought: %s", l));
				}

				// Register task to send messages ;)
				plugin.getServer().getScheduler()
						.scheduleAsyncDelayedTask(plugin, new Runnable()
						{
							public void run()
							{
								for(String message: messages)
								{
									player.sendMessage(message);
								}

								shop.clearTransactions();
							}
						});
			}

			try
			{
				for(int i = 0; i < Config.NOTIFY_INTERVAL; i++)
				{
					if(!run)
					{
						break;
					}
					Thread.sleep(1000);
				}
			}catch(InterruptedException e)
			{
				// Ignore it...really its just not important
				return;
			}

			if(!run)
			{
				break;
			}
		}
	}
}
