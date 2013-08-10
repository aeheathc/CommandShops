package com.aehdev.commandshops.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.aehdev.commandshops.CommandShops;
import com.aehdev.commandshops.Config;
import com.aehdev.commandshops.RegionSelection;
import com.aehdev.commandshops.Selection;
import com.aehdev.commandshops.ShopsPlayerListener;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * Command that lets the player make a cuboid selection that will later define shop boundaries
 */
public class CommandShopSelect extends Command
{

	/**
	 * Create a Selection order.
	 * @param plugin
	 * reference to the main CommandShops plugin object
	 * @param commandLabel
	 * command name/alias
	 * @param sender
	 * who sent the command
	 * @param command
	 * command string with arguments
	 */
	public CommandShopSelect(CommandShops plugin, String commandLabel,
			CommandSender sender, String command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/**
	 * Run select command; start selection mode for the player.
	 */
	public boolean process()
	{
		if(!(sender instanceof Player))
		{
			sender.sendMessage(ChatColor.DARK_AQUA + "Only players can select coordinates or regions in their current world.");
			return false;
		}

		if(!canUseCommand(CommandTypes.CREATE) && !canUseCommand(CommandTypes.MOVE))
		{
			sender.sendMessage(ChatColor.DARK_AQUA + "You don't have permission for any feature that would use a selection.");
			return false;
		}
		Player player = (Player)sender;
		String playerName = player.getName();
		World worldobj = player.getWorld();
		String world = worldobj.getName();
		RegionManager wg = CommandShops.worldguard.get(worldobj);
		
		ShopsPlayerListener.playerRegions.remove(playerName);		//cancel region selections
		
		Pattern pattern = Pattern.compile("(?i)select\\s+(.*)");
		Matcher matcher = pattern.matcher(command);
		if(matcher.find())
		{
			//In this case they used a region name
			ShopsPlayerListener.selectingPlayers.remove(playerName);	//cancel manual selections
			if(CommandShops.worldguard == null)
			{
				sender.sendMessage("Region name was specified, but no region plugin is connected. Can't select a region.");
				return false;
			}
			String regionName = matcher.group(1);
			ProtectedRegion region = wg.getRegion(regionName);
			RegionSelection selection = new RegionSelection(regionName, worldobj);
			if(!selection.exists())
			{
				sender.sendMessage("No region by that name in this world.");
				return false;
			}
			if(!region.isOwner(playerName) && !canUseCommand(CommandTypes.ADMIN))
			{
				sender.sendMessage("Can't attach shops to regions you don't own.");
				return false;
			}
			ShopsPlayerListener.playerRegions.put(playerName,selection);
			sender.sendMessage("Selected region "+regionName+" for attachment! Use the Create or Move command to give this region to a shop.");
		}else{
			//in this case they didn't use a region name so we toggle manual selection mode
			if(Config.REQUIRE_OWNER)
			{
				ShopsPlayerListener.selectingPlayers.remove(playerName);
				sender.sendMessage("You must provide a region name with the select command because manual area selection is disabled.");
				return false;
			}
			if(!ShopsPlayerListener.selectingPlayers.containsKey(playerName))
			{
				//start manual selection
				Selection sel = new Selection();
				sel.world = world;
				ShopsPlayerListener.selectingPlayers.put(playerName, sel);
				sender.sendMessage(ChatColor.WHITE + "Shop selection enabled."
						+ ChatColor.DARK_AQUA + " Use " + ChatColor.WHITE
						+ "bare hands " + ChatColor.DARK_AQUA + "to select!");
				sender.sendMessage(ChatColor.DARK_AQUA
						+ "Left click to select the bottom corner for a shop");
				sender.sendMessage(ChatColor.DARK_AQUA
						+ "Right click to select the far upper corner for the shop");
			}else{
				//cancel manual selection
				ShopsPlayerListener.selectingPlayers.remove(playerName);
				sender.sendMessage(ChatColor.DARK_AQUA + "Selection disabled");
			}
		}
		
		return true;
	}
}
