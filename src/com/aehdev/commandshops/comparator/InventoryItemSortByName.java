package com.aehdev.commandshops.comparator;

import java.util.Comparator;

import com.aehdev.commandshops.InventoryItem;

// TODO: Auto-generated Javadoc
/**
 * The Class InventoryItemSortByName.
 */
public class InventoryItemSortByName implements Comparator<InventoryItem>
{

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object) */
	@Override
	public int compare(InventoryItem o1, InventoryItem o2)
	{
		return o1.getInfo().name.compareTo(o2.getInfo().name);
	}

}
