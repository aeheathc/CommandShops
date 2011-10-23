package com.aehdev.commandshops.commands;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.aehdev.commandshops.CommandShops;
import com.aehdev.commandshops.InventoryItem;
import com.aehdev.commandshops.PlayerData;
import com.aehdev.commandshops.Shop;

// TODO: Auto-generated Javadoc
/**
 * The Class CommandShopDestroy.
 */
public class CommandShopDestroy extends Command
{

	/**
	 * Instantiates a new command shop destroy.
	 * @param plugin
	 * the plugin
	 * @param commandLabel
	 * the command label
	 * @param sender
	 * the sender
	 * @param command
	 * the command
	 */
	public CommandShopDestroy(CommandShops plugin, String commandLabel,
			CommandSender sender, String command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/**
	 * Instantiates a new command shop destroy.
	 * @param plugin
	 * the plugin
	 * @param commandLabel
	 * the command label
	 * @param sender
	 * the sender
	 * @param command
	 * the command
	 */
	public CommandShopDestroy(CommandShops plugin, String commandLabel,
			CommandSender sender, String[] command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/* (non-Javadoc)
	 * @see com.aehdev.commandshops.commands.Command#process() */
	public boolean process()
	{
		if(!(sender instanceof Player) || !canUseCommand(CommandTypes.DESTROY))
		{
			sender.sendMessage(CommandShops.CHAT_PREFIX + ChatColor.DARK_AQUA
					+ "You don't have permission to use this command");
			return false;
		}

		/* Available formats: /lshop destroy */

		Player player = (Player)sender;
		String playerName = player.getName();

		// get the shop the player is currently in
		if(plugin.getPlayerData().get(playerName).shopList.size() == 1)
		{
			UUID shopUuid = plugin.getPlayerData().get(playerName).shopList
					.get(0);
			Shop shop = plugin.getShopData().getShop(shopUuid);

			if(!shop.getOwner().equalsIgnoreCase(player.getName())
					&& !canUseCommand(CommandTypes.ADMIN))
			{
				player.sendMessage(ChatColor.DARK_AQUA
						+ "You must be the shop owner to destroy it.");
				return false;
			}

			Iterator<PlayerData> it = plugin.getPlayerData().values()
					.iterator();
			while(it.hasNext())
			{
				PlayerData p = it.next();
				if(p.shopList.contains(shop.getUuid()))
				{
					Player thisPlayer = plugin.getServer().getPlayer(
							p.playerName);
					p.removePlayerFromShop(thisPlayer, shop.getUuid());
					thisPlayer.sendMessage(CommandShops.CHAT_PREFIX
							+ ChatColor.WHITE + shop.getName()
							+ ChatColor.DARK_AQUA + " has been destroyed");
				}
			}

			Collection<InventoryItem> shopItems = shop.getItems();

			if(plugin.getShopData().deleteShop(shop))
			{
				// return items to player (if a player)
				if(sender instanceof Player)
				{
					for(InventoryItem item: shopItems)
					{
						givePlayerItem(item.getInfo().toStack(),
								item.getStock());
					}
				}
			}else
			{
				// error message :(
				sender.sendMessage("Could not return shop inventory!");
			}

		}else
		{
			player.sendMessage(ChatColor.DARK_AQUA
					+ "You must be inside a shop to use /" + commandLabel
					+ " destroy");
		}

		return true;
	}

}
