package com.aehdev.commandshops.commands;

import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.aehdev.commandshops.CommandShops;

/**
 * Command listing all shops the sender can control (own/manage)
 */
public class CommandShopList extends Command
{

	/**
	 * Create a list order.
	 * @param plugin
	 * reference to the main CommandShops plugin object
	 * @param commandLabel
	 * command name/alias
	 * @param sender
	 * who sent the command
	 * @param command
	 * command string with arguments
	 */
	public CommandShopList(CommandShops plugin, String commandLabel,
			CommandSender sender, String command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/**
	 * Run the command, show the list of shops the sender controls
	 */
	public boolean process()
	{
		boolean showAll = true;
		String playerName = "";
		Pattern pattern = Pattern.compile("(?i)list\\s+all$");
		Matcher matcher = pattern.matcher(command);
		if(!matcher.find())
		{
			showAll = false;
		}
		
		if(sender instanceof Player)
		{
			playerName = ((Player)sender).getName();
		}

		String listQuery = "SELECT id,owner,`name` FROM shops";
		if(!showAll)
		{
			listQuery += " WHERE owner='" + db.escape(((Player)sender).getName())
					+ "' OR id IN(SELECT shop FROM managers WHERE manager='"
					+ db.escape(((Player)sender).getName()) + "')";
		}
		listQuery += " ORDER BY owner,id";
		LinkedList<String> msg = new LinkedList<String>();
		msg.add(ChatColor.DARK_AQUA + "ID: Name");
		try{
			ResultSet resList = CommandShops.db.query(listQuery);
			while(resList.next())
			{
				StringBuffer output = new StringBuffer();
				output.append(ChatColor.WHITE);
				output.append(resList.getLong("id"));
				output.append(ChatColor.DARK_AQUA);
				output.append(": ");
				String owner = resList.getString("owner");
				if(owner.equals(playerName))
					output.append(ChatColor.GREEN);
				else
					output.append(ChatColor.WHITE);
				output.append(resList.getString("name"));
				msg.add(output.toString());
			}
			resList.close();
		}catch(Exception e){
			sender.sendMessage("List cancelled due to DB error.");
			log.warning(String.format("[%s] Couldn't get shop list: %s", CommandShops.pdfFile.getName(), e));
			return false;
		}
		String[] msgOut = new String[1];
		msgOut[0] = "";
		sender.sendMessage(msg.toArray(msgOut));
		return true;
	}
}
