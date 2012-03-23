package com.aehdev.commandshops.commands;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.aehdev.commandshops.CommandShops;
import com.aehdev.commandshops.Search;
import com.aehdev.commandshops.Shop;
import com.aehdev.commandshops.ShopLocation;

import cuboidLocale.BookmarkedResult;
import cuboidLocale.PrimitiveCuboid;

/**
 * Command for destroying shops.
 */
public class CommandShopDestroy extends Command
{

	/**
	 * Creates a new Destroy order.
	 * @param plugin
	 * reference to the main CommandShops plugin object
	 * @param commandLabel
	 * the actual main command name
	 * @param sender
	 *  the sender of the command
	 * @param command
	 * the whole argument string
	 */
	public CommandShopDestroy(CommandShops plugin, String commandLabel,
			CommandSender sender, String command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/**
	 * Parse their Destroy command and attempt to delete a shop.
	 */
	public boolean process()
	{
		if(!(sender instanceof Player) || !canUseCommand(CommandTypes.DESTROY))
		{
			sender.sendMessage(CommandShops.CHAT_PREFIX + ChatColor.DARK_AQUA
					+ "You don't have permission to use this command");
			return false;
		}

		Player player = (Player)sender;
		String playerName = player.getName();
		long shop = Shop.getCurrentShop(player);

		if(shop >= 0)
		{
			//Get info about shop and item
			String shopName = "";
			String owner = "";
			try{
				ResultSet resShop = CommandShops.db.query("SELECT name,owner FROM shops WHERE id="+shop+" LIMIT 1");
				resShop.next();
				shopName = resShop.getString("name");
				owner = resShop.getString("owner");
				resShop.close();
				if(!owner.equals(playerName) && !canUseCommand(CommandTypes.ADMIN))
				{
					player.sendMessage(ChatColor.DARK_AQUA + "You must be the shop owner to destroy it.");
					return false;
				}
				String itemQuery = String.format("SELECT itemid,itemdamage,stock FROM shop_items WHERE shop=%d", shop);
				ResultSet resItem = CommandShops.db.query(itemQuery);
				while(resItem.next())
				{
					int itemid = resItem.getInt("itemid");
					short dam = resItem.getShort("itemdamage");
					int stock = (int)Math.floor(resItem.getDouble("stock"));
					givePlayerItem(Search.itemById(itemid, dam).toStack(),stock);
				}
				resItem.close();
				CommandShops.db.query(String.format("DELETE FROM shops WHERE id=%d LIMIT 1",shop));
				//remove cuboid
				ShopLocation xyz = new ShopLocation(player.getLocation().getBlockX(),player.getLocation().getBlockY(),player.getLocation().getBlockZ());
				BookmarkedResult res = new BookmarkedResult();
				res = CommandShops.getCuboidTree().relatedSearch(res.bookmark,
						xyz.getX(), xyz.getY(), xyz.getZ());
				for(PrimitiveCuboid shopLocation: res.results)
				{
					if(shopLocation.id == -1) continue;
					if(!shopLocation.world.equalsIgnoreCase(player.getWorld().getName())) continue;
					CommandShops.getCuboidTree().delete(shopLocation);
				}
				//log
				player.sendMessage(ChatColor.DARK_AQUA + "Destroyed " + ChatColor.WHITE + shopName + ".");
				log.info(String.format("[%s] Shop %d (%s) destroyed by %s",
						CommandShops.pdfFile.getName(), shop, shopName, playerName));
				String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
				String logQuery = String.format("INSERT INTO log " 
					+"(	`datetime`,	`user`,					`shop`,	`action`,	`itemid`,	`itemdamage`,	`amount`,	`cost`,	`total`,`comment`) VALUES"
					+"(	'%s',		'%s',					NULL,	'destroy',	NULL,		NULL,			NULL,		NULL,	NULL,	'%s')"
					,	now,		db.escape(playerName),																				"Shop " + shop + ": "+db.escape(shopName));
				CommandShops.db.query(logQuery);
			}catch(Exception e){
				log.warning(String.format("[%s] Couldn't finish shop destruction: %s", CommandShops.pdfFile.getName(), e));
				sender.sendMessage(ChatColor.DARK_AQUA + "Destroy canceled: DB error.");
				return false;
			}
		}else{
			player.sendMessage(ChatColor.DARK_AQUA + "You must be inside a shop to destroy it.");
			return false;
		}
		return true;
	}

}
