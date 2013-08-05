package com.aehdev.commandshops.commands;

import java.sql.ResultSet;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.aehdev.commandshops.CommandShops;
import com.aehdev.commandshops.Shop;

/**
 * Command that shows detailed information about a single shop.
 */
public class CommandShopInfo extends Command
{

	/**
	 * Create a new info order.
	 * @param plugin
	 * reference to the main plugin object
	 * @param commandLabel
	 * command name/alias
	 * @param sender
	 * who sent the command
	 * @param command
	 * command string with arguments
	 */
	public CommandShopInfo(CommandShops plugin, String commandLabel,
			CommandSender sender, String command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/**
	 * Execute info order -- show the shop's information.
	 */
	public boolean process()
	{
		long shop = -1;
		
		//> /shop info
		// Get info about the shop the player is currently standing in
		Pattern pattern = Pattern.compile("(?i)info$");
		Matcher matcher = pattern.matcher(command);
		if(matcher.find())
		{
			if(sender instanceof Player) shop = Shop.getCurrentShop((Player)sender);
			if(shop == -1)
			{
				sender.sendMessage("You are not in a shop!");
				return false;
			}
		}
		//> /shop info id
		// Get info about a shop with the specified ID (works in console)
		matcher.reset();
		pattern = Pattern.compile("(?i)info\\s+(\\d+)$");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			try{
				shop = Integer.parseInt(matcher.group(1));
			}catch(NumberFormatException e){
				sender.sendMessage("Bad shop ID");
				return false;
			}
		}

		String[] msg = new String[7];
		try{
			String infoQuery = String.format((Locale)null,"SELECT `name`,`owner`,`creator`,x,y,z,x2,y2,z2,`world`,minbalance,unlimitedMoney,unlimitedStock,region FROM shops WHERE id=%d LIMIT 1", shop);
			ResultSet resInfo = db.query(infoQuery);
			if(!resInfo.next())
			{
				resInfo.close();
				sender.sendMessage(ChatColor.DARK_AQUA + "No shop found with ID " + ChatColor.WHITE + shop);
				return false;
			}
			String shopname = resInfo.getString("name");
			String owner = resInfo.getString("owner");
			String creator = resInfo.getString("creator");
			int x = resInfo.getInt("x");
			int y = resInfo.getInt("y");
			int z = resInfo.getInt("z");
			int x2 = resInfo.getInt("x2");
			int y2 = resInfo.getInt("y2");
			int z2 = resInfo.getInt("z2");
			String world = resInfo.getString("world");
			String region = resInfo.getString("region");
			String minbalance = plugin.econ.format(resInfo.getDouble("minbalance"));
			boolean unlimitedMoney = resInfo.getInt("unlimitedMoney") == 1;
			boolean unlimitedStock = resInfo.getInt("unlimitedStock") == 1;
			resInfo.close();
			ResultSet resMan = db.query("SELECT manager FROM managers WHERE shop=" + shop);
			StringBuffer manstr = new StringBuffer();
			int mantotal = 0;
			while(resMan.next())
			{
				++mantotal;
				manstr.append(ChatColor.WHITE);
				manstr.append(resMan.getString("manager"));
				manstr.append(ChatColor.DARK_AQUA);
				manstr.append(',');
			}
			String managers = manstr.toString();
			if(managers.length() > 0) managers = managers.substring(0, managers.length()-1);
			resMan.close();
			ResultSet resBuy = db.query("SELECT COUNT(*) FROM shop_items WHERE buy IS NOT NULL AND shop="+shop);
			resBuy.next();
			int buy = resBuy.getInt(1);
			resBuy.close();
			ResultSet resSell = db.query("SELECT COUNT(*) FROM shop_items WHERE sell IS NOT NULL AND shop="+shop);
			resSell.next();
			int sell = resSell.getInt(1);
			resSell.close();
			StringBuffer output = new StringBuffer();
			output.append(ChatColor.DARK_AQUA);
			output.append("Info for shop ");
			output.append(ChatColor.WHITE);
			output.append(shop);
			output.append(ChatColor.DARK_AQUA);
			output.append(":");
			output.append(ChatColor.WHITE);
			output.append(shopname);
			msg[0] = output.toString();
			output = new StringBuffer();
			output.append(ChatColor.DARK_AQUA);
			output.append("Owner:");
			output.append(ChatColor.WHITE);
			output.append(owner);
			output.append(ChatColor.DARK_AQUA);
			output.append(" Creator:");
			output.append(ChatColor.WHITE);
			output.append(creator);
			msg[1] = output.toString();
			output = new StringBuffer();
			output.append(ChatColor.WHITE);
			output.append(mantotal);
			output.append(ChatColor.DARK_AQUA);
			output.append(" Managers:");
			output.append(ChatColor.WHITE);
			output.append(managers);
			msg[2] = output.toString();
			output = new StringBuffer();
			output.append(ChatColor.DARK_AQUA);
			output.append("World:");
			output.append(ChatColor.WHITE);
			output.append(world);
			output.append(ChatColor.DARK_AQUA);
			output.append(" Region: ");
			output.append(ChatColor.WHITE);
			output.append(region==null ? "<none>" : region);
			msg[3] = output.toString();
			output = new StringBuffer();
			output.append(ChatColor.DARK_AQUA);
			output.append(" Location:");
			output.append(ChatColor.WHITE);
			output.append(x);
			output.append(ChatColor.DARK_AQUA);
			output.append(",");
			output.append(ChatColor.WHITE);
			output.append(y);
			output.append(ChatColor.DARK_AQUA);
			output.append(",");
			output.append(ChatColor.WHITE);
			output.append(z);
			output.append(ChatColor.DARK_AQUA);
			output.append("x");
			output.append(ChatColor.WHITE);
			output.append(x2);
			output.append(ChatColor.DARK_AQUA);
			output.append(",");
			output.append(ChatColor.WHITE);
			output.append(y2);
			output.append(ChatColor.DARK_AQUA);
			output.append(",");
			output.append(ChatColor.WHITE);
			output.append(z2);
			msg[4] = output.toString();
			output = new StringBuffer();
			output.append(ChatColor.DARK_AQUA);
			output.append("MinBalance:");
			output.append(ChatColor.WHITE);
			output.append(minbalance);
			output.append(ChatColor.DARK_AQUA);
			output.append(" UnlimitedStock:");
			output.append(ChatColor.WHITE);
			output.append(unlimitedStock ? "yes": "no");
			output.append(ChatColor.DARK_AQUA);
			output.append(" UnlimitedMoney:");
			output.append(ChatColor.WHITE);
			output.append(unlimitedMoney ? "yes": "no");
			msg[5] = output.toString();
			output = new StringBuffer();
			output.append(ChatColor.DARK_AQUA);
			output.append("ItemsBuying:");
			output.append(ChatColor.WHITE);
			output.append(buy);
			output.append(ChatColor.DARK_AQUA);
			output.append(" ItemsSelling:");
			output.append(ChatColor.WHITE);
			output.append(sell);
			msg[6] = output.toString();
		}catch(Exception e){
			sender.sendMessage("Info req cancelled due to DB error.");
			log.warning(String.format((Locale)null,"[%s] Couldn't get shop info: %s", CommandShops.pdfFile.getName(), e));
			return false;
		}
		sender.sendMessage(msg);
		return true;
	}
}
