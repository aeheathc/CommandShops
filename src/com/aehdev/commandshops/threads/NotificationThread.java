package com.aehdev.commandshops.threads;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.aehdev.commandshops.CommandShops;
import com.aehdev.commandshops.Config;
import com.aehdev.commandshops.ItemInfo;
import com.aehdev.commandshops.Search;

/**
 * This thread periodically shows a transaction digest to players who need to know.
 */
public class NotificationThread extends Thread
{
	/** Reference back to the main plugin object. */
	private CommandShops plugin;

	/** Current state. */
	private boolean run = true;

	/** Master logger. */
	protected final Logger log = Logger.getLogger("Minecraft");
	
	/** Stores the last time each player was updated about shops. */
	private HashMap<String,String> updates = new HashMap<String,String>();
	
	/** date formatter object this thread will use a lot */
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	/** Keep track of when the thread began so we don't show old messages */
	private final String startDate = sdf.format(new Date());

	/**
	 * Creates the thread.
	 * @param plugin
	 * Reference back to the main plugin object
	 */
	public NotificationThread(CommandShops plugin)
	{
		this.plugin = plugin;
	}

	/**
	 * Sets the current state and forces the thread to recognize it.
	 * @param run
	 * whether or not the new state should be running
	 */
	public void setRun(boolean run)
	{
		this.run = run;
		synchronized(this){notify();}
	}

	/**
	 * Starts the thread. 
	 * @see java.lang.Thread#run()
	 */
	public void run()
	{
		log.info(String.format((Locale)null,
				"[%s] Starting NotificationThread with Timer of %d seconds",
				plugin.getDescription().getName(),
				Config.NOTIFY_INTERVAL));

		while(true)
		{
			long start = System.currentTimeMillis();
			Player[] online = Bukkit.getOnlinePlayers();
			for(Player player : online)
			{
				String name = player.getName();
				String lastUpdate = updates.get(name);
				if(lastUpdate == null)
				{
					lastUpdate = new String(startDate);
					updates.put(name, lastUpdate);
				}
				try{
					String logQuery = "SELECT name,action,amount,itemid,itemdamage,user,total FROM log LEFT JOIN shops ON log.shop=shops.id WHERE owner='"
									+ name + "' AND (action='buy' OR action='sell') AND datetime>'"
									+ lastUpdate + "' AND notify=1";
					ResultSet resLog = CommandShops.db.query(logQuery);
					LinkedList<String> msg = new LinkedList<String>();
					boolean any = false;
					while(resLog.next())
					{
						any = true;
						StringBuffer output = new StringBuffer(60);
						output.append(ChatColor.WHITE);
						output.append(resLog.getString("name"));
						output.append(ChatColor.DARK_AQUA);
						output.append(": ");
						if(resLog.getString("action").equals("sell"))
						{
							output.append(ChatColor.GREEN);
							output.append("bought ");
						}else{
							output.append(ChatColor.GOLD);
							output.append("sold ");
						}
						output.append(ChatColor.WHITE);
						output.append(resLog.getInt("amount"));
						output.append(" ");
						ItemInfo item = Search.itemById(resLog.getInt("itemid"), (short)resLog.getInt("itemdamage"));
						String itemName = "items";
						if(item != null) itemName = item.name;
						output.append(itemName);
						output.append(ChatColor.DARK_AQUA);
						output.append(" via ");
						output.append(ChatColor.WHITE);
						output.append(resLog.getString("user"));
						output.append(ChatColor.DARK_AQUA);
						output.append(" @");
						output.append(ChatColor.WHITE);
						output.append(plugin.econ.format(resLog.getDouble("total")));
						msg.add(output.toString());
					}
					resLog.close();
					String[] example = new String[1];
					if(any) player.sendMessage(msg.toArray(example));
				}catch(Exception e){
					log.warning(String.format((Locale)null,"[%s] Couldn't get transaction log: %s",
							CommandShops.pdfFile.getName(), e));
					break;
				}
				lastUpdate = sdf.format(new Date());
				updates.put(name, lastUpdate);
			}
			
			// wait the configured amount of time before updates, but stop waiting if the thread is told to stop
			if(!run) break;
			while((System.currentTimeMillis()-start)/1000 < Config.NOTIFY_INTERVAL)
			{
				long millisToWait = (Config.NOTIFY_INTERVAL * 1000) - (System.currentTimeMillis()-start); 
				try{synchronized(this){wait(millisToWait);}}catch(InterruptedException e){}
				if(!run) break;
			}
		}
	}
}
