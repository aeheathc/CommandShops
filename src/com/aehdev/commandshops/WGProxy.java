package com.aehdev.commandshops;

import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * Quarantine code that causes NoClassDefFoundError simply by having its class loaded when WorldGuard isn't available.
 * As long as we don't actually call any method of this class when WorldGuard isn't around, the class doesn't get loaded and everything will work fine. 
 */
public class WGProxy
{
	/**
	 * Sets the GREET_MESSAGE flag of a region.
	 * 
	 * @param regionobj the region object
	 * @param msg the message
	 */
	public static void setGreeting(ProtectedRegion regionobj, String msg)
	{
		regionobj.setFlag(DefaultFlag.GREET_MESSAGE, msg);
	}
	
	/**
	 * Sets the FAREWELL_MESSAGE flag of a region.
	 * 
	 * @param regionobj the region object
	 * @param msg the message
	 */
	public static void setFarewell(ProtectedRegion regionobj, String msg)
	{
		regionobj.setFlag(DefaultFlag.FAREWELL_MESSAGE, msg);
	}
}