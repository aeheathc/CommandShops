package com.aehdev.commandshops.comparator;

import java.util.Comparator;

import com.aehdev.commandshops.Shop;

// TODO: Auto-generated Javadoc
/**
 * The Class ShopSortByOwner.
 */
public class ShopSortByOwner implements Comparator<Shop>
{

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object) */
	@Override
	public int compare(Shop o1, Shop o2)
	{
		return o1.getOwner().compareTo(o2.getOwner());
	}

}
