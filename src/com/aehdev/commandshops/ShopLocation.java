package com.aehdev.commandshops;

import org.bukkit.Location;

// TODO: Auto-generated Javadoc
/**
 * The Class ShopLocation.
 */
public class ShopLocation
{

	/** The x. */
	private double x = 0;

	/** The y. */
	private double y = 0;

	/** The z. */
	private double z = 0;

	/**
	 * Instantiates a new shop location.
	 * @param x
	 * the x
	 * @param y
	 * the y
	 * @param z
	 * the z
	 */
	public ShopLocation(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Instantiates a new shop location.
	 * @param xyz
	 * the xyz
	 */
	public ShopLocation(double[] xyz)
	{
		this.x = xyz[0];
		this.y = xyz[1];
		this.z = xyz[2];
	}

	/**
	 * Instantiates a new shop location.
	 * @param loc
	 * the loc
	 */
	public ShopLocation(Location loc)
	{
		this.x = loc.getX();
		this.y = loc.getY();
		this.z = loc.getZ();
	}

	/**
	 * Gets the x.
	 * @return the x
	 */
	public int getX()
	{
		return (int)x;
	}

	/**
	 * Gets the y.
	 * @return the y
	 */
	public int getY()
	{
		return (int)y;
	}

	/**
	 * Gets the z.
	 * @return the z
	 */
	public int getZ()
	{
		return (int)z;
	}

	/**
	 * To array.
	 * @return the double[]
	 */
	public double[] toArray()
	{
		return new double[]{x, y, z};
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString() */
	public String toString()
	{
		return String.format("%.0f, %.0f, %.0f", x, y, z);
	}
}
