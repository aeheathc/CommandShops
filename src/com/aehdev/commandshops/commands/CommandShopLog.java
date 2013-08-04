package com.aehdev.commandshops.commands;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.aehdev.commandshops.CommandShops;
import com.aehdev.commandshops.Shop;

/**
 * Command that queries transaction log data.
 */
public class CommandShopLog extends Command
{
	/**
	 * Create a new log retrieval order.
	 * @param plugin
	 * reference to the main plugin object
	 * @param commandLabel
	 * command name/alias
	 * @param sender
	 * who sent the command
	 * @param command
	 * command string with arguments
	 */
	public CommandShopLog(CommandShops plugin, String commandLabel,
			CommandSender sender, String command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/**
	 * Valid fields in the log table that can be used as parameters to the log command.
	 */
	public static final String[] fields = {"shop","user","action","itemid","itemdamage","amount","price","total","datetime"};
	
	/**
	 * Valid operators that can be used to compare fields to a value for log filtering
	 */
	public static final String[] operators = {"<",">","<=",">=","=","!="};
	
	/**
	 * Execute log order -- show the transactions meeting the given criteria.
	 */
	public boolean process()
	{
		String[] args = command.split(" ");
		HashMap<String,LinkedList<String>> conds = new HashMap<String,LinkedList<String>>();
		boolean hasShop = false;
		for(int i=1;i<args.length;++i)
		{
			if(args[i].startsWith("shop"))
			{
				hasShop=true;
				break;
			}
		}
		args[0] = "shop>0";
		if(!hasShop && (sender instanceof Player))
		{
			Player player = (Player)sender;
			long shop = Shop.getCurrentShop(player);
			if(shop != -1)
			{
				args[0] = "shop="+shop;
			}
		}
		if(!hasShop && args[0] == "shop>0")
		{
			sender.sendMessage("You must either be in a shop or provide a shop ID.");
			return false;
		}
		for(int i=0;i<args.length;++i)
		{
			boolean valid=false;
			for(String field : fields)
			{
				if(args[i].startsWith(field))
				{
					String cond = args[i].substring(field.length());
					String operator = "",value="";
					for(int c = 0; c < cond.length(); ++c)
					{
						if(Character.isAlphabetic(cond.charAt(c)) || Character.isDigit(cond.charAt(c)))
						{
							operator = cond.substring(0, c);
							value = cond.substring(c);
							break;
						}
					}
					if(Arrays.asList(operators).contains(operator) && value.length()>0)
					{
						operator = db.escape(operator);
						value = db.escape(value);
						if(field=="datetime") value = value.replace('@', ' ');
						if(field=="shop")
						{
							int id = 0;
							try{id = Integer.parseInt(value);}catch(NumberFormatException e){break;}
							if(!isShopController(id) && (sender instanceof Player) && !canUseCommand(CommandTypes.ADMIN) && id!=0)
							{
								args[i] += " (Can't query shops that aren't yours)";
								break;
							}
						}
						
						if(!conds.containsKey(field)) conds.put(field, new LinkedList<String>());
						conds.get(field).add(operator+"'"+value+"'");
						valid=true;
					}
					break;
				}
			}
			if(!valid)
			{
				sender.sendMessage("Invalid log parameter: " + args[i]);
				return false;
			}
		}
		
		StringBuffer query = new StringBuffer();
		query.append("SELECT datetime,user,action,itemid,itemdamage,amount,cost,total FROM log WHERE true");
		for(String field : fields)
		{
			LinkedList<String> cds = conds.get(field);
			if(cds == null) continue;
			for(String cond : cds)
			{
				query.append(" AND ");
				query.append(field=="price" ? "cost" : field);
				query.append(cond);
			}
		}
		query.append(" ORDER BY datetime DESC");
		
		LinkedList<String> message = new LinkedList<String>();
		try{
			ResultSet resLog = db.query(query.toString());
			message.add("Datetime                       User       Action Item Amount Price Total");
			while(resLog.next())
			{
				message.add(String.format((Locale)null,
						"%s %s %s %d:%d %d %s %s",
						resLog.getString("datetime"),resLog.getString("user"),resLog.getString("action"),resLog.getInt("itemid"),resLog.getInt("itemdamage"),resLog.getInt("amount"),plugin.econ.format(resLog.getDouble("cost")),plugin.econ.format(resLog.getDouble("total"))));
			}
			resLog.close();
		}catch(Exception e){
			sender.sendMessage("Log req cancelled due to DB error.");
			log.warning(String.format((Locale)null,"[%s] Couldn't get log: %s", CommandShops.pdfFile.getName(), e));
			return false;
		}
		if(message.size()<2)
		{
			sender.sendMessage("None found.");
			return true;
		}

		sender.sendMessage(message.toArray(new String[]{}));
		return true;
	}
}
