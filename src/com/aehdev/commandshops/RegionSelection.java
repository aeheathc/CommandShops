package com.aehdev.commandshops;

import org.bukkit.Bukkit;
import org.bukkit.World;

/**
 * Object representing a selected region.
 */
public class RegionSelection
{
	/** Indicates which points have been selected so far in this selection. */
	public String region = null;
	
	/** World containing the selection. */
	public World world = Bukkit.getWorld("world");
	
	/**
	 * Check if the Selection has been fully made and the
	 * size of this Selection is within acceptable limits.
	 * @return true if the size is OK
	 */
	public boolean exists()
	{
		return CommandShops.worldguard.get(world).getRegion(region) != null;
	}
	
	public RegionSelection(String region, World world)
	{
		this.region = region;
		this.world = world;
	}
}