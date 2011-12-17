package com.aehdev.commandshops.commands;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.aehdev.commandshops.CommandShops;
import com.aehdev.commandshops.Config;
import com.aehdev.commandshops.Shop;
import com.aehdev.commandshops.comparator.ShopSortByName;

// TODO: Auto-generated Javadoc
/**
 * The Class CommandShopList.
 */
public class CommandShopList extends Command
{

	/**
	 * Instantiates a new command shop list.
	 * @param plugin
	 * the plugin
	 * @param commandLabel
	 * the command label
	 * @param sender
	 * the sender
	 * @param command
	 * the command
	 */
	public CommandShopList(CommandShops plugin, String commandLabel,
			CommandSender sender, String command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/**
	 * Instantiates a new command shop list.
	 * @param plugin
	 * the plugin
	 * @param commandLabel
	 * the command label
	 * @param sender
	 * the sender
	 * @param command
	 * the command
	 */
	public CommandShopList(CommandShops plugin, String commandLabel,
			CommandSender sender, String[] command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/* (non-Javadoc)
	 * @see com.aehdev.commandshops.commands.Command#process() */
	public boolean process()
	{
		int idWidth = Config.UUID_MIN_LENGTH + 1;
		if(idWidth < 4)
		{
			idWidth = 4;
		}

		boolean showAll = false;
		boolean isPlayer = false;

		// list all
		Pattern pattern = Pattern.compile("(?i)list\\s+all$");
		Matcher matcher = pattern.matcher(command);
		if(matcher.find())
		{
			showAll = true;
		}

		if(sender instanceof Player)
		{
			isPlayer = true;
		}

		if(isPlayer)
		{
			sender.sendMessage(String.format("%-" + idWidth + "s  %s", "Id",
					"Name"));
		}else{
			sender.sendMessage(String.format("%-" + idWidth + "s  %-25s %s",
					"Id", "Name", "Owner"));
		}

		List<Shop> shops = plugin.getShopData().getAllShops();
		Collections.sort(shops, new ShopSortByName());

		Iterator<Shop> it = shops.iterator();
		while(it.hasNext())
		{
			Shop shop = it.next();
			if(!showAll && isPlayer && !isShopController(shop))
			{
				continue;
			}

			if(isPlayer)
			{
				sender.sendMessage(String.format("%-" + idWidth + "s  %s",
						shop.getShortUuidString(), shop.getName()));
			}else
			{
				sender.sendMessage(String.format(
						"%-" + idWidth + "s  %-25s %s",
						shop.getShortUuidString(), shop.getName(),
						shop.getOwner()));
			}
		}
		return true;
	}
}
