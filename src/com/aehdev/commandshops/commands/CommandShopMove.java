package com.aehdev.commandshops.commands;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.aehdev.commandshops.CommandShops;
import com.aehdev.commandshops.Config;
import com.aehdev.commandshops.Selection;
import com.aehdev.commandshops.ShopLocation;
import com.aehdev.commandshops.ShopsPlayerListener;
import cuboidLocale.BookmarkedResult;
import cuboidLocale.PrimitiveCuboid;

/**
 * Command that moves a shop.
 */
public class CommandShopMove extends Command
{

	/**
	 * Create a Move order.
	 * @param plugin
	 * reference to the main CommandShops plugin object
	 * @param commandLabel
	 * command name/alias
	 * @param sender
	 * who sent the command
	 * @param command
	 * command string with arguments
	 */
	public CommandShopMove(CommandShops plugin, String commandLabel,
			CommandSender sender, String command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/**
	 * Move the shop.
	 */
	public boolean process()
	{
		if(!(sender instanceof Player))
		{
			sender.sendMessage("You need to have a location or a selection to move a shop.");
			return false;
		}
		
		if(!canUseCommand(CommandTypes.MOVE))
		{
			sender.sendMessage(CommandShops.CHAT_PREFIX + ChatColor.DARK_AQUA
					+ "You don't have permission to use this command");
			return false;
		}

		Pattern pattern = Pattern.compile("(?i)move\\s+(\\d+)");
		Matcher matcher = pattern.matcher(command);
		if(!matcher.find())
		{
			sender.sendMessage(CommandShops.CHAT_PREFIX + ChatColor.DARK_AQUA
					+ "The command format is " + ChatColor.WHITE + "/"
					+ commandLabel + " move [id]");
			sender.sendMessage(CommandShops.CHAT_PREFIX + ChatColor.DARK_AQUA
					+ "Use " + ChatColor.WHITE + "/" + commandLabel + " info"
					+ ChatColor.DARK_AQUA + " to obtain the id.");
			return false;
		}
		long shop = -1;
		try{
			shop = Long.parseLong(matcher.group(1));
		}catch(NumberFormatException e){
			sender.sendMessage("Invalid ID.");
			return false;
		}
		
		if(!isShopController(shop) && !canUseCommand(CommandTypes.ADMIN))
		{
			sender.sendMessage("You don't have access to modify this shop.");
			return false;
		}
		
		Player player = (Player)sender;
		String playerName = player.getName();
		String shopWorld = "";
		String targetWorld = "";
		String shopName = "";
		double oldx,oldy,oldz,oldx2,oldy2,oldz2;
		
		try{
			String checkQuery = "SELECT x,y,z,x2,y2,z2,`world`,`name` FROM shops WHERE id=" + shop + " LIMIT 1";
			ResultSet resCheck = CommandShops.db.query(checkQuery);
			if(!resCheck.next())
			{
				sender.sendMessage("Shop ID not found.");
				resCheck.close();
				return false;
			}
			oldx = resCheck.getInt("x");
			oldy = resCheck.getInt("y");
			oldz = resCheck.getInt("z");
			oldx2 = resCheck.getInt("x2");
			oldy2 = resCheck.getInt("y2");
			oldz2 = resCheck.getInt("z2");
			shopWorld = resCheck.getString("world");
			shopName = resCheck.getString("name");
			resCheck.close();
		}catch(Exception e){
			sender.sendMessage("Move cancelled due to DB error.");
			log.warning(String.format("[%s] Couldn't move shop: %s", CommandShops.pdfFile.getName(), e));
			return false;
		}
		
		double[] xyzA = new double[3];
		double[] xyzB = new double[3];

		// If player is select, use their selection
		Selection sel = ShopsPlayerListener.selectingPlayers.get(playerName);

		if(sel != null)
		{
			if(!sel.checkSize())
			{
				String size = Config.MAX_WIDTH + "x"
						+ Config.MAX_HEIGHT + "x"
						+ Config.MAX_WIDTH;
				player.sendMessage(ChatColor.DARK_AQUA
						+ "Problem with selection. Max size is "
						+ ChatColor.WHITE + size);
				return false;
			}
			if(!sel.a || !sel.b)
			{
				player.sendMessage(ChatColor.DARK_AQUA
						+ "Problem with selection. Only one point selected");
				return false;
			}
			xyzA[0] = sel.x;
			xyzA[1] = sel.y;
			xyzA[2] = sel.z;
			xyzB[0] = sel.x2;
			xyzB[1] = sel.y2;
			xyzB[2] = sel.z2;
			targetWorld = sel.world;
		}else{
			player.sendMessage(ChatColor.DARK_AQUA + "You need to select an area first. Use "
					+ ChatColor.WHITE + "/shop select.");
			return false;
		}
		
		/* remove the old shop from the cuboid so the current position isn't
		 * found as overlapping with the target position
		 */
		ShopLocation xyz = new ShopLocation((oldx+oldx2)/2,(oldy+oldy2)/2,(oldz+oldz2)/2);
		BookmarkedResult res = new BookmarkedResult();
		res = CommandShops.getCuboidTree().relatedSearch(res.bookmark,
				xyz.getX(), xyz.getY(), xyz.getZ());
		for(PrimitiveCuboid shopLocation: res.results)
		{
			if(shopLocation.id == -1) continue;
			if(!shopLocation.world.equalsIgnoreCase(shopWorld)) continue;
			CommandShops.getCuboidTree().delete(shopLocation);
		}
		// make backup cuboid of old shop to be restored if anything goes wrong
		PrimitiveCuboid restoreShop = new PrimitiveCuboid(oldx, oldy, oldz, oldx2, oldy2, oldz2);
		restoreShop.id = shop;
		restoreShop.world = shopWorld;
		
		if(!shopPositionOk(xyzA, xyzB, targetWorld))
		{
			sender.sendMessage("A shop already exists here!");
			CommandShops.getCuboidTree().insert(restoreShop);
			return false;
		}

		if(!canUseCommand(CommandTypes.MOVE_FREE) && plugin.econ.getBalance(playerName) < Config.MOVE_COST)
		{
			sender.sendMessage("You need " + plugin.econ.format(Config.MOVE_COST) + " to move a shop!");
			CommandShops.getCuboidTree().insert(restoreShop);
			return false;
		}
		
		if(!plugin.econ.withdrawPlayer(playerName, Config.MOVE_COST).transactionSuccess())
		{
			sender.sendMessage("Shop move failed due to Vault error");
			log.warning(String.format("[%s] Failed to create shop due to Vault error", CommandShops.pdfFile.getName()));
			CommandShops.getCuboidTree().insert(restoreShop);
			return false;
		}
		
		int x = Math.min((int)xyzA[0], (int)xyzB[0]);
		int y = Math.min((int)xyzA[1], (int)xyzB[1]);
		int z = Math.min((int)xyzA[2], (int)xyzB[2]);
		int x2 = Math.max((int)xyzA[0], (int)xyzB[0]);
		int y2 = Math.max((int)xyzA[1], (int)xyzB[1]);
		int z2 = Math.max((int)xyzA[2], (int)xyzB[2]);
		
		try{
			String moveQuery = String.format("UPDATE shops SET x=%d,y=%d,z=%d,x2=%d,y2=%d,z2=%d,world='%s' WHERE id=%d LIMIT 1"
																,x,   y,   z,    x2,   y2,   z2,       targetWorld, shop);
			CommandShops.db.query(moveQuery);
		}catch(Exception e){
			sender.sendMessage("Shop move failed due to DB error");
			CommandShops.getCuboidTree().insert(restoreShop);
			if(plugin.econ.depositPlayer(playerName, Config.MOVE_COST).transactionSuccess())
			{
				log.warning(String.format("[%s]  Failed to move shop, but charge rollback succeeded (Ending state OK): %s"
						, CommandShops.pdfFile.getName(), e));
			}else{
				log.warning(String.format("[%s]  Failed to move shop and charge rollback failed. %s is likely missing %s: %s"
						, CommandShops.pdfFile.getName(), playerName, plugin.econ.format(Config.MOVE_COST), e));
			}
			return false;
		}
		
		PrimitiveCuboid newShop = new PrimitiveCuboid(x, y, z, x2, y2, z2);
		newShop.id = shop;
		newShop.world = targetWorld;
		CommandShops.getCuboidTree().insert(newShop);
		
		ShopsPlayerListener.selectingPlayers.remove(playerName);
		sender.sendMessage("Shop moved to selection."); 
		//log
		log.info(String.format("[%s] Player %s moved shop %d (%s)"
				, CommandShops.pdfFile.getName(), playerName, shop, shopName));
		try{
			String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			String logQuery = String.format("INSERT INTO log " 
				+"(	`datetime`,	`user`,					`shop`,	`action`,	`itemid`,	`itemdamage`,	`amount`,	`cost`,			`total`,`comment`) VALUES"
				+"(	'%s',		'%s',					%d,		'move',		NULL,		NULL,			NULL,		%f,				NULL,	'%s')"
				,	now,		db.escape(playerName),	shop,														Config.MOVE_COST,		"New Location:"+x+','+y+','+z+'x'+x2+','+y2+','+z2);
			CommandShops.db.query(logQuery);
		}catch(Exception e){
			log.warning(String.format("[%s] Couldn't log shop creation: %s",
					CommandShops.pdfFile.getName(), e));
		}
		
		return true;
	}
}
