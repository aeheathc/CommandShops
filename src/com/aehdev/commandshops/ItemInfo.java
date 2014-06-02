package com.aehdev.commandshops;

import java.util.Locale;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an item type definition. Unlike Bukkit's Material class, these are unique for every individual item (every combination of type and subtype).
 * Contains searchdata that the search engine uses for good autocomplete functionality.
 */
public class ItemInfo
{

	/** The full item name. There are not "canon", the set of names used by default is fabricated by this plugin.
	 * This is needed in order to ensure unique item names for every combination of type and subtype, a feature not found in vanilla.
	 * It also allows for the use of a user's custom item set to rename vanilla items. */
	public final String name;

	/** The "dictionary", or set of wordforms used in the search engine.
	 * Care must be taken to prevent too much overlap with that of other items in order to avoid items taking each other over in the search.*/
	public final String[][] search;

	/** The main "Item ID" or "type". */
	public final int typeId;

	/** The "subtype" or "damage value" */
	public final short subTypeId;

	/** The Material corresponding to the item ID. */
	public final Material material;

	/**
	 * Create a new item definition.
	 * @param name Item name unique to this combination of type and subtype
	 * @param search Wordform set for the search engine
	 * @param typeId the item id
	 * @param subTypeId the damage value
	 */
	public ItemInfo(String name, String[][] search, int typeId, short subTypeId)
	{
		material = Search.materials.get(typeId);
		this.name = name;
		this.search = search;
		this.typeId = typeId;
		this.subTypeId = subTypeId;
	}

	/** Serialize to YAML
	 * @return YAML fragment representing the ItemInfo
	 * */
	public String toString()
	{
		StringBuffer out = new StringBuffer(String.format((Locale)null, "  item,%d,%d:\n    name: \"%s\"\n    wordforms:\n", typeId, subTypeId, name));
		for(String[] form : search)
		{
			out.append("      - ");
			out.append(Search.join(form, " "));
			out.append("\n");
		}
		return out.toString();
	}
	
	/**
	 * Use the Material to ask bukkit if this type of item is "durable", that is, if it is an item that can take damage,
	 * and therefore the damage value actually stores the remaining durability instead of a subtype.
	 * 
	 * @return true, if durable
	 */
	public boolean isDurable()
	{
		return material.getMaxDurability() > 0;
	}
	
	/**
	 * Use the Material to ask bukkit the maximum stack size for this type of item.
	 * 
	 * @return the max stack size
	 */
	public int getMaxStackSize()
	{
		return material.getMaxStackSize();
	}

	/**
	 * Create an ItemStack of this type of item.
	 * 
	 * @param amount how many items the stack should have
	 * @return the item stack
	 */
	public ItemStack toStack(int amount)
	{
		return new ItemStack(Search.materials.get(typeId), amount, subTypeId);
	}
}
