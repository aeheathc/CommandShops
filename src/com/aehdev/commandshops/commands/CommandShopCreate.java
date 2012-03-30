package com.aehdev.commandshops.commands;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.aehdev.commandshops.CommandShops;
import com.aehdev.commandshops.Config;
import com.aehdev.commandshops.Selection;
import com.aehdev.commandshops.ShopsPlayerListener;

import cuboidLocale.PrimitiveCuboid;

/**
 * Command for creating new shops.
 */
public class CommandShopCreate extends Command
{

	/**
	 * Creates a new shop creation order.
	 * @param plugin
	 * reference to the main CommandShops plugin object
	 * @param commandLabel
	 * the actual main command name
	 * @param sender
	 * the sender of the command
	 * @param command
	 * the whole argument string
	 */
	public CommandShopCreate(CommandShops plugin, String commandLabel,
			CommandSender sender, String command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/**
	 * Parse their create command and attempt to make a new shop
	 * @return true on success
	 */
	@Override
	public boolean process()
	{
		if(!(sender instanceof Player))
		{
			sender.sendMessage("You need to be able to select coordinates to create a shop.");
			return false;
		}
		
		double[] xyzA = new double[3];
		double[] xyzB = new double[3];

		Player player = (Player)sender;
		String creator = player.getName();
		String createWorld = "";
		
		// Check permissions
		if(!canCreateShop(creator))
		{
			sender.sendMessage(CommandShops.CHAT_PREFIX
					+ ChatColor.DARK_AQUA
					+ "You already have the maximum number of shops or don't have permission to create them!");
			return false;
		}
		
		// If player is select, use their selection
		Selection sel = ShopsPlayerListener.selectingPlayers.get(creator);
		
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
			createWorld = sel.world;
		}else{
			player.sendMessage(ChatColor.DARK_AQUA + "You need to select an area first. Use "
					+ ChatColor.WHITE + "/shop select.");
			return false;
		}
		if(!shopPositionOk(xyzA, xyzB, createWorld))
		{
			sender.sendMessage("A shop already exists here!");
			return false;
		}

		if(Config.SHOP_COST > 0)
		{
			if(!canUseCommand(CommandTypes.CREATE_FREE))
			{
				if(plugin.econ.getBalance(creator) < Config.SHOP_COST)
				{
					sender.sendMessage(CommandShops.CHAT_PREFIX
							+ ChatColor.DARK_AQUA
							+ "You need "
							+ plugin.econ.format(
									Config.SHOP_COST)
							+ " to create a shop.");
					return false;
				}else{
					if(!plugin.econ.withdrawPlayer(creator, Config.SHOP_COST).transactionSuccess())
					{
						sender.sendMessage(CommandShops.CHAT_PREFIX
								+ ChatColor.DARK_AQUA
								+ "Create canceled due to Vault error.");
						return false;
					}
				}
			}
		}

		// Command matching
		int insId = -1;
		Pattern pattern = Pattern.compile("(?i)create\\s+(.*)");
		Matcher matcher = pattern.matcher(command);
		if(matcher.find())
		{
			String name = matcher.group(1);
			int x = Math.min((int)xyzA[0], (int)xyzB[0]);
			int y = Math.min((int)xyzA[1], (int)xyzB[1]);
			int z = Math.min((int)xyzA[2], (int)xyzB[2]);
			int x2 = Math.max((int)xyzA[0], (int)xyzB[0]);
			int y2 = Math.max((int)xyzA[1], (int)xyzB[1]);
			int z2 = Math.max((int)xyzA[2], (int)xyzB[2]);
			try{
				String insertQuery = String.format((Locale)null,"INSERT INTO shops"
						+ "(	`name`,			`owner`,			`creator`,			`x`,`y`,`z`,`x2`,`y2`,`z2`,	`world`,	`minbalance`,	`unlimitedMoney`,	`unlimitedStock`) VALUES"
						+ "(	'%s',			'%s',				'%s',				%d, %d, %d, %d,  %d,  %d,	'%s',		0,				0,					0)"
						,		db.escape(name),db.escape(creator),	db.escape(creator),	x,	y,	z,  x2,  y2,  z2,	createWorld);
				CommandShops.db.query(insertQuery);
				String idQuery = "SELECT MAX(id) FROM shops WHERE `name`='" + db.escape(name) + "' AND `owner`='" + db.escape(creator) + "'";
				ResultSet resId = CommandShops.db.query(idQuery);
				resId.next();
				insId = resId.getInt(1);
				resId.close();
			}catch(Exception e){
				player.sendMessage(ChatColor.DARK_AQUA + "Unable to create shop: DB error");
				//rollback payment
				if(!plugin.econ.depositPlayer(creator, Config.SHOP_COST).transactionSuccess())
				{
					log.warning(String.format((Locale)null,
							"[%s] Failed to create shop and failed to rollback payment; %s is likely missing %s!: %s",
							CommandShops.pdfFile.getName(), creator, plugin.econ.format(Config.SHOP_COST), e));
				}else{
					log.warning(String.format((Locale)null,
							"[%s] Failed to create shop, but charge rollback succeeded (Ending state OK): %s",
							CommandShops.pdfFile.getName(), e));
				}
				return false;
			}

			// insert the shop into the world
			PrimitiveCuboid cub = new PrimitiveCuboid(xyzA, xyzB);
			cub.world = createWorld;
			cub.id = insId;
			CommandShops.getCuboidTree().insert(cub);

			//disable selecting
			ShopsPlayerListener.selectingPlayers.remove(creator);
			
			//log
			log.info(String.format((Locale)null,"[%s] %s Created shop: %s",
					CommandShops.pdfFile.getName(), creator, name));
			try{
				String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
				String logQuery = String.format((Locale)null,"INSERT INTO log " 
					+"(	`datetime`,	`user`,				`shop`,	`action`,	`itemid`,	`itemdamage`,	`amount`,	`cost`,			`total`,`comment`) VALUES"
					+"(	'%s',		'%s',				%d,		'create',	NULL,		NULL,			NULL,		%f,				NULL,	'%s')"
					,	now,		db.escape(creator),	insId,														Config.SHOP_COST,		"Location:"+x+','+y+','+z+'x'+x2+','+y2+','+z2);
				CommandShops.db.query(logQuery);
			}catch(Exception e){
				log.warning(String.format((Locale)null,"[%s] Couldn't log shop creation: %s",
						CommandShops.pdfFile.getName(), e));
			}
			sender.sendMessage(ChatColor.DARK_AQUA + "Created shop " + ChatColor.WHITE + name);
			return true;
		}

		// Show usage
		sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel
				+ " create [ShopName]" + ChatColor.DARK_AQUA
				+ " - Create a shop at your location.");
		return true;
	}
}
