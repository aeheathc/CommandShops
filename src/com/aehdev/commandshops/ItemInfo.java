package com.aehdev.commandshops;

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

	/** Serialize to YAML
	 * @return YAML fragment representing the ItemInfo
	 * */
	public String toString()
	{
		StringBuffer out = new StringBuffer(String.format((Locale)null,
							"  item,%d,%d:\n    name: \"%s\"\n    maxstack: \"%d\"\n    wordforms:\n",
							typeId, subTypeId, name, maxStackSize));
		for(String[] form : search)
		{
			out.append("      - ");
			out.append(Search.join(form, " "));
			out.append("\n");
		}
		return out.toString();
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
