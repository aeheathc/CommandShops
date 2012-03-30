package com.aehdev.commandshops;

import java.util.Arrays;
import java.util.Locale;

import org.bukkit.inventory.ItemStack;

// TODO: Auto-generated Javadoc
/**
 * The Class ItemInfo.
 */
public class ItemInfo
{

	/** The name. */
	public String name = null;

	/** The search. */
	public String[][] search = null;

	/** The type id. */
	public int typeId = -1;

	/** The sub type id. */
	public short subTypeId = 0;

	/** The max stack size. */
	public int maxStackSize = 64;

	/**
	 * Instantiates a new item info.
	 * @param name
	 * the name
	 * @param search
	 * the search
	 * @param typeId
	 * the type id
	 * @param subTypeId
	 * the sub type id
	 * @param maxStackSize
	 * the max stack size
	 */
	public ItemInfo(String name, String[][] search, int typeId,
			short subTypeId, int maxStackSize)
	{
		this.name = name;
		this.search = search;
		this.typeId = typeId;
		this.subTypeId = subTypeId;
		this.maxStackSize = maxStackSize;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString() */
	public String toString()
	{
		return String.format((Locale)null,"%s, %s, %d:%d", name,
				Arrays.deepToString(search), typeId, subTypeId);
	}

	/**
	 * To stack.
	 * @return the item stack
	 */
	public ItemStack toStack()
	{
		return new ItemStack(typeId, 1, subTypeId);
	}
}
