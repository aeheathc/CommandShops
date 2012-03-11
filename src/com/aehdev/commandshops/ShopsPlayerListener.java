package com.aehdev.commandshops;

import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.aehdev.commandshops.Selection;

/**
 * Handle all Player related events.
 * 
 * @see ShopsPlayerEvent
 */
public class ShopsPlayerListener implements Listener
{
	/** List of all players currently making a selection, and what the selection is. */
	public static HashMap<String,Selection> selectingPlayers = new HashMap<String,Selection>();
	
	/**
	 * Processes PlayerInteractEvent for the sole purpose of selecting cuboids.
	 * 
	 * @param event the event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		String playerName = player.getName();

		// If our user is select & is not holding an item, selection time
		if((selectingPlayers.get(playerName)!= null) && player.getItemInHand().getType() == Material.AIR)
		{
			Selection sel = selectingPlayers.get(playerName);
			sel.world = player.getWorld().getName();
			Location loc = event.getClickedBlock().getLocation();
			int x = loc.getBlockX();
			int y = loc.getBlockY();
			int z = loc.getBlockZ();

			if(event.getAction() == Action.LEFT_CLICK_BLOCK)
			{
				sel.x = x;
				sel.y = y;
				sel.z = z;
				sel.a = true;
				player.sendMessage(ChatColor.DARK_AQUA + "First Position "
							+ ChatColor.LIGHT_PURPLE + x + " " + y + " " + z);
				if(sel.a && !sel.b)
				{
					player.sendMessage(ChatColor.DARK_AQUA
							+ "Now, right click to select the far upper corner for the shop.");
				}else if(sel.a && sel.b){
					player.sendMessage(ChatColor.DARK_AQUA
							+ "Use the Create or Move command if you're happy with your selection, otherwise keep selecting!");
				}
			}else if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
				sel.x2 = x;
				sel.y2 = y;
				sel.z2 = z;
				sel.b = true;
				player.sendMessage(ChatColor.DARK_AQUA + "Second Position "
							+ ChatColor.LIGHT_PURPLE + x + " " + y + " " + z);

				if(sel.b && !sel.a)
				{
					player.sendMessage(ChatColor.DARK_AQUA
							+ "Now, left click to select the bottom corner for a shop.");
				}else if(sel.a && sel.b){
					player.sendMessage(ChatColor.DARK_AQUA
							+ "Use the Create or Move command if you're happy with your selection, otherwise keep selecting!");
				}
			}
		}

	}
}
