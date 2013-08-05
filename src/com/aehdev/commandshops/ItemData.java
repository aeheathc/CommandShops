package com.aehdev.commandshops;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

// TODO: Auto-generated Javadoc
/**
 * The Class ItemData.
 */
public class ItemData
{

	/** The item name. */
	private ArrayList<String> itemName;

	/** The item number. */
	private ArrayList<Integer> itemNumber;

	/** The item data. */
	private ArrayList<itemDataType> itemData;

	/**
	 * Instantiates a new item data.
	 */
	public ItemData()
	{
		itemName = new ArrayList<String>();
		itemNumber = new ArrayList<Integer>();
		itemData = new ArrayList<itemDataType>();
	}

	/**
	 * Adds the item.
	 * @param name
	 * the name
	 * @param blockNumber
	 * the block number
	 */
	public void addItem(String name, int blockNumber)
	{
		if(!itemName.contains(name))
		{
			itemName.add(name);
			itemNumber.add(blockNumber);
			itemDataType tmp = new itemDataType();
			itemData.add(tmp);
		}

	}

	/**
	 * Adds the item.
	 * @param name
	 * the name
	 * @param blockNumber
	 * the block number
	 * @param dataValue
	 * the data value
	 */
	public void addItem(String name, int blockNumber, int dataValue)
	{
		if(!itemName.contains(name))
		{
			itemName.add(name);
			itemNumber.add(blockNumber);

			itemDataType tmp = new itemDataType(dataValue);
			itemData.add(tmp);
		}
	};

	/**
	 * Tries to match item name passed in string. If sender is passed to
	 * function, will return message to sender if no matches are found or print
	 * list of matches if multiple are found.
	 * @param sender
	 * the sender
	 * @param name
	 * the name
	 * @return Will return null if no matches are found.
	 */
	public int[] getItemInfo(CommandSender sender, String name)
	{

		int index = itemName.indexOf(name);
		if(index == -1)
		{
			Pattern myPattern = Pattern.compile(name, Pattern.CASE_INSENSITIVE);
			Matcher myMatcher = myPattern.matcher("tmp");

			ArrayList<String> foundMatches = new ArrayList<String>();
			foundMatches.clear();

			Iterator<String> itr = itemName.iterator();
			while(itr.hasNext())
			{
				String thisItem = itr.next();
				myMatcher.reset(thisItem);
				if(myMatcher.find()) foundMatches.add(thisItem);
			}

			if(foundMatches.size() == 1)
			{
				index = itemName.indexOf(foundMatches.get(0));
			}else
			{
				if(sender != null)
				{
					if(foundMatches.size() > 1)
					{
						sender.sendMessage(name + ChatColor.DARK_AQUA
								+ " matched multiple items:");
						for(String foundName: foundMatches)
						{
							sender.sendMessage("  " + foundName);
						}
					}else
					{
						sender.sendMessage(name + ChatColor.DARK_AQUA
								+ " did not match any items.");
					}
				}
				return null;
			}
		}
		int[] data = {itemNumber.get(index), itemData.get(index).dataValue};
		return data;
	}

	/**
	 * Returns list of all itemNames that match the itemId supplied.
	 * @param itemId
	 * the item id
	 * @return Will return list of all found matches.
	 */
	public ArrayList<String> getItemName(int itemId)
	{
		ArrayList<String> foundNames = new ArrayList<String>();

		for(int i = 0; i < this.itemNumber.size(); i++)
		{
			if(itemNumber.get(i) == itemId)
			{
				foundNames.add(this.itemName.get(i));
			}
		}

		return foundNames;

	}

	/**
	 * Gets the item name.
	 * @param itemNumber
	 * the item number
	 * @param itemData
	 * the item data
	 * @return the item name
	 */
	public String getItemName(int itemNumber, int itemData)
	{

		// check if type and data match, if they do, return that one
		for(int i = 0; i < this.itemNumber.size(); i++)
		{
			if(this.itemNumber.get(i) == itemNumber
					&& this.itemData.get(i).dataValue == itemData){ return this.itemName
					.get(i); }
		}

		// check if this is armor or an item
		ArrayList<String> itemList = getItemName(itemNumber);
		if(itemList.size() == 1){ return itemList.get(0); }

		return null;
	}

	/**
	 * The Class itemDataType.
	 */
	private class itemDataType
	{
		/** The data value. */
		public int dataValue = 0;

		/**
		 * Instantiates a new item data type.
		 * @param dataValue
		 * the data value
		 */
		public itemDataType(int dataValue)
		{
			this.dataValue = dataValue;
		}

		/**
		 * Instantiates a new item data type.
		 */
		public itemDataType()
		{
			this.dataValue = 0;
		}
	}

	/**
	 * Checks if is durable.
	 * @param item
	 * the item
	 * @return true, if is durable
	 */
	public boolean isDurable(ItemStack item)
	{
		Material itemType = item.getType();
		if(itemType == Material.CHAINMAIL_BOOTS
		|| itemType == Material.CHAINMAIL_CHESTPLATE
		|| itemType == Material.CHAINMAIL_HELMET
		|| itemType == Material.CHAINMAIL_LEGGINGS
		|| itemType == Material.WOOD_AXE
		|| itemType == Material.WOOD_HOE
		|| itemType == Material.WOOD_PICKAXE
		|| itemType == Material.WOOD_SPADE
		|| itemType == Material.WOOD_SWORD
		|| itemType == Material.LEATHER_HELMET
		|| itemType == Material.LEATHER_CHESTPLATE
		|| itemType == Material.LEATHER_LEGGINGS
		|| itemType == Material.LEATHER_BOOTS
		|| itemType == Material.STONE_AXE
		|| itemType == Material.STONE_HOE
		|| itemType == Material.STONE_PICKAXE
		|| itemType == Material.STONE_SPADE
		|| itemType == Material.STONE_SWORD
		|| itemType == Material.IRON_AXE
		|| itemType == Material.IRON_BOOTS
		|| itemType == Material.IRON_CHESTPLATE
		|| itemType == Material.IRON_HELMET
		|| itemType == Material.IRON_HOE
		|| itemType == Material.IRON_LEGGINGS
		|| itemType == Material.IRON_PICKAXE
		|| itemType == Material.IRON_SPADE
		|| itemType == Material.IRON_SWORD
		|| itemType == Material.GOLD_AXE
		|| itemType == Material.GOLD_BOOTS
		|| itemType == Material.GOLD_CHESTPLATE
		|| itemType == Material.GOLD_HELMET
		|| itemType == Material.GOLD_HOE
		|| itemType == Material.GOLD_LEGGINGS
		|| itemType == Material.GOLD_PICKAXE
		|| itemType == Material.GOLD_SPADE
		|| itemType == Material.GOLD_SWORD
		|| itemType == Material.DIAMOND_AXE
		|| itemType == Material.DIAMOND_BOOTS
		|| itemType == Material.DIAMOND_CHESTPLATE
		|| itemType == Material.DIAMOND_HELMET
		|| itemType == Material.DIAMOND_HOE
		|| itemType == Material.DIAMOND_LEGGINGS
		|| itemType == Material.DIAMOND_PICKAXE
		|| itemType == Material.DIAMOND_SPADE
		|| itemType == Material.DIAMOND_SWORD
		|| itemType == Material.SHEARS
		|| itemType == Material.FLINT_AND_STEEL
		|| itemType == Material.FISHING_ROD
		|| itemType == Material.BOW){ return true; }
		return false;
	}
}
