package com.aehdev.commandshops;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import com.google.common.collect.HashBiMap;

/**
 * Provides a search engine for items.
 */
public class Search
{
	/** Table of information about all items we can deal in. */
	private static ArrayList<ItemInfo> items = new ArrayList<ItemInfo>(600);
	
	/** Map of Materials known by Bukkit, indexed by their ID.
	 * This is built by using reflection to get the real id of each material despite it being set private.
	 * We need this because it will be the only way to convert between numeric ID and Material once bukkit removes the normal methods to do this (morons).
	 * Note that Materials do not correlate to items, only to IDs. Different items with the same id but different sub-id have the same Material.*/
	public static final HashBiMap<Integer,Material> materials = HashBiMap.create();
	
	static{
		Field enumId = null;
		try{
			enumId = Material.class.getDeclaredField("id");
			enumId.setAccessible(true);
			for(Material mat : Material.values()) 
			{
				int id = (int)enumId.get(mat);
				materials.put(id, mat);
			}
		}catch(NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e){
			materials.clear();
			Logger log = Logger.getLogger("Minecraft");
			log.severe(String.format((Locale)null, "[%s] Error using reflection to get material IDs. CS will not be able to even start up. Exception: %s", CommandShops.pdfFile.getName(), e.toString()));
		}
	}
	
	public static void reload(CommandShops plugin)
	{
		Logger log = Logger.getLogger("Minecraft");
		if(!items.isEmpty()) items.clear();
		File itemsyml = new File(plugin.getDataFolder(), "items.yml");
		if(itemsyml.exists() && Config.CUSTOM_ITEMS)
		{
			FileConfiguration custom = YamlConfiguration.loadConfiguration(itemsyml);
			ConfigurationSection data = custom.getConfigurationSection("items");
			if(data == null)
			{
				log.info(String.format((Locale)null,
						"[%s] Custom items enabled, but nothing found in items.yml",
						CommandShops.pdfFile.getName()));
			}else{
				for(String key : data.getKeys(false))
				{
					String[] locText = key.split(",");
					int id =  Integer.parseInt(locText[1]);
					short subtype =  Short.parseShort(locText[2]);
					String name = data.getString(key + ".name");
					ArrayList<String> wordforms = new ArrayList<String>(); 
					for(Object o : data.getList(key + ".wordforms"))
					{
						wordforms.add(o.toString());
					}
					String[][] dictionary = new String[wordforms.size()][];
					int index = -1;
					for(String form : wordforms) dictionary[++index] = form.split(" ");
					items.add(new ItemInfo(name,dictionary, id, subtype));
				}
				log.info(String.format((Locale)null,
						"[%s] Loaded custom item set from items.yml",
						CommandShops.pdfFile.getName()));
				return;
				
			}
		}
		
		
		
		//name,										search,									   					typeId,  subTypeId    	
		items.add(new ItemInfo("Stone",				new String[][] {{"stone"}},										1,	 (short) 0  ));
		items.add(new ItemInfo("Grass",				new String[][] {{"gras"}},										2,	 (short) 0  ));
		items.add(new ItemInfo("Dirt",				new String[][] {{"dirt"}},										3,	 (short) 0  ));
		items.add(new ItemInfo("Grassless Dirt",	new String[][] {{"grassless"}},									3,	 (short) 1  ));
		items.add(new ItemInfo("Podzol",			new String[][] {{"podz"}},										3,	 (short) 2  ));
		items.add(new ItemInfo("Cobblestone",		new String[][] {{"cobb","sto"},{"cobb"}},						4,	 (short) 0  ));
		items.add(new ItemInfo("Oak Planks",		new String[][] {{"plank"},{"plank","oak"}},						5,	 (short) 0  ));
		items.add(new ItemInfo("Spruce Planks",		new String[][] {{"plank","spr"}},								5,	 (short) 1  ));
		items.add(new ItemInfo("Birch Planks",		new String[][] {{"plank","birch"}},								5,	 (short) 2  ));
		items.add(new ItemInfo("Jungle Planks",		new String[][] {{"plank","jung"}},								5,	 (short) 3  ));
		items.add(new ItemInfo("Oak Sapling",		new String[][] {{"sapling"},{"sapling","oak"}},					6,	 (short) 0  ));
		items.add(new ItemInfo("Spruce Sapling",	new String[][] {{"sapling","spr"}},								6,	 (short) 1  ));
		items.add(new ItemInfo("Birch Sapling",		new String[][] {{"sapling","birch"}},							6,	 (short) 2  ));
		items.add(new ItemInfo("Jungle Sapling",	new String[][] {{"sapling","jung"}},							6,	 (short) 3  ));
		items.add(new ItemInfo("Bedrock",			new String[][] {{"rock"}},										7,	 (short) 0  ));
		items.add(new ItemInfo("Water Block",		new String[][] {{"water","blo"}},								9,	 (short) 0  ));
		items.add(new ItemInfo("Lava Block",		new String[][] {{"lava","blo"}},								11,  (short) 0  ));
		items.add(new ItemInfo("Sand",				new String[][] {{"sand"}},										12,  (short) 0  ));
		items.add(new ItemInfo("Gravel",			new String[][] {{"gravel"}},									13,  (short) 0  ));
		items.add(new ItemInfo("Gold Ore",			new String[][] {{"ore","gold"}},								14,  (short) 0  ));
		items.add(new ItemInfo("Iron Ore",			new String[][] {{"ore","iron"}},								15,  (short) 0  ));
		items.add(new ItemInfo("Coal Ore",			new String[][] {{"ore","coal"}},								16,  (short) 0  ));
		items.add(new ItemInfo("Oak Log",			new String[][] {{"log"},{"log","oak"}},							17,  (short) 0  ));
		items.add(new ItemInfo("Spruce Log",		new String[][] {{"log","spr"}},									17,  (short) 1  ));
		items.add(new ItemInfo("Birch Log",			new String[][] {{"log","birch"}},								17,  (short) 2  ));
		items.add(new ItemInfo("Jungle Log",		new String[][] {{"log","jung"}},								17,  (short) 3  ));
		items.add(new ItemInfo("Oak Leaves Block",	new String[][] {{"blo","lea"},{"blo","leaves","oak"}},			18,  (short) 0  ));
		items.add(new ItemInfo("Spruce Leaves Block",new String[][]{{"blo","lea","spr"}},							18,  (short) 1  ));
		items.add(new ItemInfo("Birch Leaves Block",new String[][] {{"blo","lea","birch"}},							18,  (short) 2  ));
		items.add(new ItemInfo("Jungle Leaves Block",new String[][]{{"blo","lea","jung"}},							18,  (short) 3  ));
		items.add(new ItemInfo("Oak Leaves",		new String[][] {{"lea"},{"lea","oak"}},							18,  (short) 4  ));
		items.add(new ItemInfo("Spruce Leaves",		new String[][] {{"lea","spr"}},									18,  (short) 5  ));
		items.add(new ItemInfo("Birch Leaves",		new String[][] {{"lea","birch"}},								18,  (short) 6  ));
		items.add(new ItemInfo("Jungle Leaves",		new String[][] {{"lea","jung"}},								18,  (short) 7  ));
		items.add(new ItemInfo("Sponge",			new String[][] {{"sponge"}},									19,  (short) 0  ));
		items.add(new ItemInfo("Glass",				new String[][] {{"glas"}},										20,  (short) 0  ));
		items.add(new ItemInfo("Lapis Lazuli Ore",	new String[][] {{"lapis","ore"}},								21,  (short) 0  ));
		items.add(new ItemInfo("Lapis Lazuli Block",new String[][] {{"lapis","bl"}},								22,  (short) 0  ));
		items.add(new ItemInfo("Dispenser",			new String[][] {{"dispen"},{"dis","pen"}},						23,  (short) 0  ));
		items.add(new ItemInfo("Sandstone",			new String[][] {{"sand","st"}},									24,  (short) 0  ));
		items.add(new ItemInfo("Chiseled Sandstone",new String[][] {{"sand","st","chi"}},							24,  (short) 1  ));
		items.add(new ItemInfo("Smooth Sandstone",	new String[][] {{"sand","st","smo"}},							24,  (short) 2  ));
		items.add(new ItemInfo("Note Block",		new String[][] {{"note"}},										25,  (short) 0  ));
		items.add(new ItemInfo("Powered Rail",		new String[][] {{"rail","pow"},{"trac","pow"},{"boost"}},		27,  (short) 0  ));
		items.add(new ItemInfo("Detector Rail",		new String[][] {{"rail","det"},{"trac","det"},{"detec"}},		28,  (short) 0  ));
		items.add(new ItemInfo("Sticky Piston Block",new String[][]{{"blo","sticky"},{"blo","sticky","pist"}},		29,  (short) 0  ));
		items.add(new ItemInfo("Sticky Piston",		new String[][] {{"sticky"},{"sticky","pist"}},					29,  (short) 7  ));
		items.add(new ItemInfo("Cobweb",			new String[][] {{"web"},{"cobweb"}},							30,  (short) 0  ));
		items.add(new ItemInfo("Dead Shrub",		new String[][] {{"dead","shrub"}},								31,  (short) 0  ));
		items.add(new ItemInfo("Tall Grass",		new String[][] {{"tall","gras"}},								31,  (short) 1  ));
		items.add(new ItemInfo("Fern",				new String[][] {{"fern"}},										31,  (short) 2  ));
		items.add(new ItemInfo("Dead Bush",			new String[][] {{"dead","bush"}},								32,  (short) 0  ));
		items.add(new ItemInfo("Piston Block",		new String[][] {{"blo","pist"}},								33,  (short) 0  ));
		items.add(new ItemInfo("Piston",			new String[][] {{"pist"}},										33,  (short) 7  ));
		items.add(new ItemInfo("White Wool",		new String[][] {{"wool","whit"},{"wool"}},						35,  (short) 0  ));
		items.add(new ItemInfo("Orange Wool",		new String[][] {{"wool","ora"}},								35,  (short) 1  ));
		items.add(new ItemInfo("Magenta Wool",		new String[][] {{"wool","mag"}},								35,  (short) 2  ));
		items.add(new ItemInfo("Light Blue Wool",	new String[][] {{"wool","lig","blue"}},							35,  (short) 3  ));
		items.add(new ItemInfo("Yellow Wool",		new String[][] {{"wool","yell"}},								35,  (short) 4  ));
		items.add(new ItemInfo("Lime Wool",			new String[][] {{"wool","lime"}},								35,  (short) 5  ));
		items.add(new ItemInfo("Pink Wool",			new String[][] {{"wool","pink"}},								35,  (short) 6  ));
		items.add(new ItemInfo("Gray Wool",			new String[][] {{"wool","gray"},{"wool","grey"}},				35,  (short) 7  ));
		items.add(new ItemInfo("Light Gray Wool",	new String[][] {{"lig","wool","gra"},{"lig","wool","gre"}},		35,  (short) 8  ));
		items.add(new ItemInfo("Cyan Wool",			new String[][] {{"wool","cya"}},								35,  (short) 9  ));
		items.add(new ItemInfo("Purple Wool",		new String[][] {{"wool","pur"}},								35,  (short) 10 ));
		items.add(new ItemInfo("Blue Wool",			new String[][] {{"wool","blue"}},								35,  (short) 11 ));
		items.add(new ItemInfo("Brown Wool",		new String[][] {{"wool","brow"}},								35,  (short) 12 ));
		items.add(new ItemInfo("Green Wool",		new String[][] {{"wool","gree"}},								35,  (short) 13 ));
		items.add(new ItemInfo("Red Wool",			new String[][] {{"wool","red"}},								35,  (short) 14 ));
		items.add(new ItemInfo("Black Wool",		new String[][] {{"wool","bla"}},								35,  (short) 15 ));
		items.add(new ItemInfo("Dandelion",			new String[][] {{"flow","yell"},{"dande"}},						37,  (short) 0  ));
		items.add(new ItemInfo("Poppy",				new String[][] {{"rose"},{"poppy"}},							38,  (short) 0  ));
		items.add(new ItemInfo("Blue Orchid",		new String[][] {{"orchid"}},									38,  (short) 1  ));
		items.add(new ItemInfo("Allium",			new String[][] {{"alli"}},										38,  (short) 2  ));
		items.add(new ItemInfo("Azure Bluet",		new String[][] {{"bluet"}},										38,  (short) 3  ));
		items.add(new ItemInfo("Red Tulip",			new String[][] {{"tulip","red"}},								38,  (short) 4  ));
		items.add(new ItemInfo("Orange Tulip",		new String[][] {{"tulip","ora"}},								38,  (short) 5  ));
		items.add(new ItemInfo("White Tulip",		new String[][] {{"tulip","whi"}},								38,  (short) 6  ));
		items.add(new ItemInfo("Pink Tulip",		new String[][] {{"tulip","pin"}},								38,  (short) 7  ));
		items.add(new ItemInfo("Oxeye Daisy",		new String[][] {{"daisy"}},										38,  (short) 8  ));
		items.add(new ItemInfo("Brown Mushroom",	new String[][] {{"mush","bro"}},								39,  (short) 0  ));
		items.add(new ItemInfo("Red Mushroom",		new String[][] {{"mush","red"}},								40,  (short) 0  ));
		items.add(new ItemInfo("Gold Block",		new String[][] {{"gold","bl"}},									41,  (short) 0  ));
		items.add(new ItemInfo("Iron Block",		new String[][] {{"iron","bl"}},									42,  (short) 0  ));
	items.add(new ItemInfo("Double Stone Slab",		new String[][] {{"dou","slab"},{"dou","slab","sto"}},			43,  (short) 0  ));
	items.add(new ItemInfo("Double Sandstone Slab",	new String[][] {{"dou","slab","sand","sto"}},					43,  (short) 1  ));
	items.add(new ItemInfo("Double Wooden Stone Slab",new String[][]{{"dou","slab","sto","wood"}},					43,  (short) 2  ));
	items.add(new ItemInfo("Double Cobblestone Slab",new String[][]{{"dou","slab","cob","sto"},{"dou","slab","cob"}},43, (short) 3  ));
	items.add(new ItemInfo("Double Clay Brick Slab",new String[][] {{"dou","slab","bric","clay"}},					43,  (short) 4  ));
	items.add(new ItemInfo("Double Stone Brick Slab",new String[][]{{"dou","slab","bric","sto"}},					43,  (short) 5  ));
	items.add(new ItemInfo("Double Nether Brick Slab",new String[][]{{"dou","slab","bric","neth"}},					43,  (short) 6  ));
	items.add(new ItemInfo("Double Quartz Slab",	new String[][]{{"dou","slab","quar"}},							43,  (short) 7  ));
	items.add(new ItemInfo("Double Smooth Stone Slab",new String[][]{{"dou","slab","smo","sto"}},					43,  (short) 8  ));
	items.add(new ItemInfo("Double Smooth Sandstone Slab",new String[][]{{"dou","slab","sand","smo"}},				43,  (short) 9  ));
	items.add(new ItemInfo("Double Quartz Tile Slab",new String[][]{{"dou","slab","quar","til"}},					43,  (short) 10 ));
		items.add(new ItemInfo("Stone Slab",		new String[][] {{"slab","sto"}},								44,  (short) 0  ));
		items.add(new ItemInfo("Sandstone Slab",	new String[][] {{"slab","sand","sto"}},							44,  (short) 1  ));
		items.add(new ItemInfo("Wooden Stone Slab",	new String[][] {{"slab","wood"},{"slab","sto","wood"}},			44,  (short) 2  ));
		items.add(new ItemInfo("Cobblestone Slab",	new String[][] {{"slab","cob","sto"},{"slab","cob"}},			44,  (short) 3  ));
		items.add(new ItemInfo("Clay Brick Slab",	new String[][] {{"slab","bric","clay"}},						44,  (short) 4  ));
		items.add(new ItemInfo("Stone Brick Slab",	new String[][] {{"slab","bric","sto"}},							44,  (short) 5  ));
		items.add(new ItemInfo("Nether Brick Slab",	new String[][] {{"slab","bric","neth"}},						44,  (short) 6  ));
		items.add(new ItemInfo("Quartz Slab",		new String[][] {{"slab","quar"}},								44,  (short) 7  ));
		items.add(new ItemInfo("Smooth Stone Slab",	new String[][] {{"slab","smo","sto"}},							44,  (short) 8  ));
		items.add(new ItemInfo("Smooth Sandstone Slab",new String[][]{{"slab","sand","smo"}},						44,  (short) 9  ));
		items.add(new ItemInfo("Quartz Tile Slab",	new String[][] {{"slab","quar","til"}},							44,  (short) 10 ));
		items.add(new ItemInfo("Clay Brick Block",	new String[][] {{"clay","bric","bloc"}},						45,  (short) 0  ));
		items.add(new ItemInfo("TNT",				new String[][] {{"tnt"}},										46,  (short) 0  ));
		items.add(new ItemInfo("Bookshelf",			new String[][] {{"bookshe"},{"book","she"}},					47,  (short) 0  ));
		items.add(new ItemInfo("Moss Stone",		new String[][] {{"moss","sto"},{"moss","cob"},{"moss"}},		48,  (short) 0  ));
		items.add(new ItemInfo("Obsidian",			new String[][] {{"obsi"}},										49,  (short) 0  ));
		items.add(new ItemInfo("Torch",				new String[][] {{"torc"}},										50,  (short) 0  ));
		items.add(new ItemInfo("Fire",				new String[][] {{"fire"}},										51,  (short) 0  ));
		items.add(new ItemInfo("Monster Spawner",	new String[][] {{"spawn"}},										52,  (short) 0  ));
		items.add(new ItemInfo("Oak Stairs",		new String[][] {{"stair","wood"},{"stair","oak"}},				53,  (short) 0  ));
		items.add(new ItemInfo("Chest",				new String[][] {{"chest"}},										54,  (short) 0  ));
		items.add(new ItemInfo("Diamond Ore",		new String[][] {{"ore","diam"}},								56,  (short) 0  ));
		items.add(new ItemInfo("Diamond Block",		new String[][] {{"diam","bl"}},									57,  (short) 0  ));
		items.add(new ItemInfo("Crafting Table",	new String[][] {{"benc"},{"craft"}},							58,  (short) 0  ));
		items.add(new ItemInfo("Farmland",			new String[][] {{"farm"}},										60,  (short) 0  ));
		items.add(new ItemInfo("Furnace",			new String[][] {{"furna"}},										61,  (short) 0  ));
		items.add(new ItemInfo("Ladder",			new String[][] {{"ladd"}},										65,  (short) 0  ));
		items.add(new ItemInfo("Rail",				new String[][] {{"rail"}},										66,  (short) 0  ));
		items.add(new ItemInfo("Cobblestone Stairs",new String[][] {{"stair","cob","sto"},{"stair","cob"}},			67,  (short) 0  ));
		items.add(new ItemInfo("Lever",				new String[][] {{"lever"},{"switc"}},							69,  (short) 0  ));
		items.add(new ItemInfo("Stone Pressure Plate",new String[][]{{"pres","plat","ston"}},						70,  (short) 0  ));
		items.add(new ItemInfo("Wooden Pressure Plate",new String[][]{{"pres","plat","wood"}},						72,  (short) 0  ));
		items.add(new ItemInfo("Redstone Ore",		new String[][] {{"ore","red"}},									73,  (short) 0  ));
		items.add(new ItemInfo("Redstone Torch",	new String[][] {{"torc","red"},{"torc","rs"}},					76,  (short) 0  ));
		items.add(new ItemInfo("Stone Button",		new String[][] {{"stone","button"},{"button"}},					77,  (short) 0  ));
		items.add(new ItemInfo("Snow",				new String[][] {{"snow"}},										78,  (short) 0  ));
		items.add(new ItemInfo("Ice",				new String[][] {{"ice"}},										79,  (short) 0  ));
		items.add(new ItemInfo("Snow Block",		new String[][] {{"snow","blo"}},								80,  (short) 0  ));
		items.add(new ItemInfo("Cactus",			new String[][] {{"cact"}},										81,  (short) 0  ));
		items.add(new ItemInfo("Clay Block",		new String[][] {{"clay","blo"}},								82,  (short) 0  ));
		items.add(new ItemInfo("Jukebox",			new String[][] {{"jukeb"}},										84,  (short) 0  ));
		items.add(new ItemInfo("Fence",				new String[][] {{"fence"}},										85,  (short) 0  ));
		items.add(new ItemInfo("Pumpkin",			new String[][] {{"pump"}},										86,  (short) 0  ));
		items.add(new ItemInfo("Netherrack",		new String[][] {{"netherr"}},									87,  (short) 0  ));
		items.add(new ItemInfo("Soul Sand",			new String[][] {{"soul","sand"},{"soul"}},						88,  (short) 0  ));
		items.add(new ItemInfo("Glowstone Block",	new String[][] {{"glow","stone"},{"glow","block"}},				89,  (short) 0  ));
		items.add(new ItemInfo("Jack-O-Lantern",	new String[][] {{"jack"},{"lante"}},							91,  (short) 0  ));
		items.add(new ItemInfo("Locked Chest",		new String[][] {{"lock","chest"}},								95,  (short) 0  ));
		items.add(new ItemInfo("Trapdoor",			new String[][] {{"trap"},{"trap","door"},{"hatch"}},			96,  (short) 0  ));
		items.add(new ItemInfo("Silverfish Stone",	new String[][] {{"silver","ston"}},								97,  (short) 0  ));
		items.add(new ItemInfo("Silverfish Cobblestone",new String[][]{{"silver","cob"},{"silver","cob","ston"}},	97,  (short) 1  ));
		items.add(new ItemInfo("Silverfish Stone Brick",new String[][]{{"silver","bric"},{"silver","ston","bric"}},	97,  (short) 2  ));
		items.add(new ItemInfo("Stone Brick Block",	new String[][] {{"ston","bric","bloc"}},						98,  (short) 0  ));
		items.add(new ItemInfo("Mossy Stone Brick Block",new String[][]{{"ston","bric","moss","bloc"}},				98,  (short) 1  ));
		items.add(new ItemInfo("Cracked Stone Brick Block",new String[][]{{"ston","bric","cra"}},					98,  (short) 2  ));
		items.add(new ItemInfo("Chiseled Stone Brick Block",new String[][] {{"ston","bric","chi"}},					98,  (short) 3  ));
		items.add(new ItemInfo("Huge Brown Mushroom",new String[][]{{"huge","bro","mush"}},							99,  (short) 0  ));
		items.add(new ItemInfo("Huge Red Mushroom",	new String[][] {{"huge","red","mush"}},							100, (short) 0  ));
		items.add(new ItemInfo("Iron Bars",			new String[][] {{"iron","bar"}},								101, (short) 0  ));
		items.add(new ItemInfo("Glass Pane",		new String[][] {{"glas","pan"}},								102, (short) 0  ));
		items.add(new ItemInfo("Melon",				new String[][] {{"melo"}},										103, (short) 0  ));
		items.add(new ItemInfo("Pumpkin Stem",		new String[][] {{"pump","stem"}},								104, (short) 0  ));
		items.add(new ItemInfo("Melon Stem",		new String[][] {{"melo","stem"}},								105, (short) 0  ));
		items.add(new ItemInfo("Vines",				new String[][] {{"vine"}},										106, (short) 0  ));
		items.add(new ItemInfo("Fence Gate",		new String[][] {{"fence","gate"},{"gate"}},						107, (short) 0  ));
		items.add(new ItemInfo("Clay Brick Stairs",	new String[][] {{"clay","bric","stair"}},						108, (short) 0  ));
		items.add(new ItemInfo("Stone Brick Stairs",new String[][] {{"ston","bric","stair"}},						109, (short) 0  ));
		items.add(new ItemInfo("Mycelium",			new String[][] {{"myce"}},										110, (short) 0  ));
		items.add(new ItemInfo("Lily Pad",			new String[][] {{"lily"}},										111, (short) 0  ));
		items.add(new ItemInfo("Nether Brick Block",new String[][] {{"bric","nether","bloc"}},						112, (short) 0  ));
		items.add(new ItemInfo("Nether Brick Fence",new String[][] {{"fen","bric","nether"}},						113, (short) 0  ));
		items.add(new ItemInfo("Nether Brick Stairs",new String[][]{{"stair","bric","nether"}},						114, (short) 0  ));
		items.add(new ItemInfo("Enchantment Table", new String[][] {{"encha"}},										116, (short) 0  ));
		items.add(new ItemInfo("End Portal Frame",	new String[][] {{"end","fra"}},									120, (short) 0  ));
		items.add(new ItemInfo("End Stone", 		new String[][] {{"end","sto"}},									121, (short) 0  ));
		items.add(new ItemInfo("Dragon Egg", 		new String[][] {{"drag"}},										122, (short) 0  ));
		items.add(new ItemInfo("Redstone Lamp",		new String[][] {{"lamp"},{"red","lamp"}},						123, (short) 0  ));
		items.add(new ItemInfo("Oak Double Slab",	new String[][] {{"oak","dou","slab"},{"wood","dou","slab"}},	125, (short) 0  ));
		items.add(new ItemInfo("Spruce Double Slab",new String[][] {{"spr","dou","slab"}},							125, (short) 1  ));
		items.add(new ItemInfo("Birch Double Slab",	new String[][] {{"birch","dou","slab"}},						125, (short) 2  ));
		items.add(new ItemInfo("Jungle Double Slab",new String[][] {{"jung","dou","slab"}},							125, (short) 3  ));
		items.add(new ItemInfo("Oak Slab",			new String[][] {{"oak","slab"}},								126, (short) 0  ));
		items.add(new ItemInfo("Spruce Slab",		new String[][] {{"spr","dou","slab"}},							126, (short) 1  ));
		items.add(new ItemInfo("Birch Slab",		new String[][] {{"birch","dou","slab"}},						126, (short) 2  ));
		items.add(new ItemInfo("Jungle Slab",		new String[][] {{"jung","dou","slab"}},							126, (short) 3  ));
		items.add(new ItemInfo("Cocoa Plant",		new String[][] {{"coco","pla"}},								127, (short) 0  ));
		items.add(new ItemInfo("Sandstone Stairs",	new String[][] {{"sand","stair"}},								128, (short) 0  ));
		items.add(new ItemInfo("Emerald Ore",		new String[][] {{"emer","ore"}},								129, (short) 0  ));
		items.add(new ItemInfo("Ender Chest",		new String[][] {{"end","chest"}},								130, (short) 0  ));
		items.add(new ItemInfo("Tripwire Hook",		new String[][] {{"trip","hook"}},								131, (short) 0  ));
		items.add(new ItemInfo("Emerald Block",		new String[][] {{"emer","blo"}},								133, (short) 0  ));
		items.add(new ItemInfo("Spruce Stairs",		new String[][] {{"spr","stair"}},								134, (short) 0  ));
		items.add(new ItemInfo("Birch Stairs",		new String[][] {{"birch","stair"}},								135, (short) 0  ));
		items.add(new ItemInfo("Jungle Stairs",		new String[][] {{"jung","stair"}},								136, (short) 0  ));
		items.add(new ItemInfo("Command Block",		new String[][] {{"comma","blo"}},								137, (short) 0  ));
		items.add(new ItemInfo("Beacon",			new String[][] {{"beaco"}},										138, (short) 0  ));
		items.add(new ItemInfo("Cobblestone Wall",	new String[][] {{"cob","fence"},{"cob","wall"}},				139, (short) 0  ));
		items.add(new ItemInfo("Moss Stone Wall",	new String[][] {{"moss","fence"},{"moss","wall"}},				139, (short) 1  ));
		items.add(new ItemInfo("Wooden Button",		new String[][] {{"wood","button"}},								143, (short) 0  ));
		items.add(new ItemInfo("Anvil",				new String[][] {{"anvi"}},										145, (short) 0  ));
		items.add(new ItemInfo("Trapped Chest",		new String[][] {{"trap","ches"}},								146, (short) 0  ));
		items.add(new ItemInfo("Light Weighted Pressure Plate",new String[][]{{"light","plate"}},					147, (short) 0  ));
		items.add(new ItemInfo("Heavy Weighted Pressure Plate",new String[][]{{"heavy","plate"}},					148, (short) 0  ));
		items.add(new ItemInfo("Daylight Sensor",	new String[][] {{"ligh","sens"}},								151, (short) 0  ));
		items.add(new ItemInfo("Redstone Block",	new String[][] {{"reds","bloc"}},								152, (short) 0  ));
		items.add(new ItemInfo("Nether Quartz Ore",	new String[][] {{"quar","ore"}},								153, (short) 0  ));
		items.add(new ItemInfo("Hopper",			new String[][] {{"hopp"}},										154, (short) 0  ));
		items.add(new ItemInfo("Quartz Block",		new String[][] {{"quart","bloc"}},								155, (short) 0  ));
		items.add(new ItemInfo("Quartz Stairs",		new String[][] {{"quart","stai"}},								156, (short) 0  ));
		items.add(new ItemInfo("Activator Rail",	new String[][] {{"acti","rail"}},								157, (short) 0  ));
		items.add(new ItemInfo("Dropper",			new String[][] {{"dropp"}},										158, (short) 0  ));
		items.add(new ItemInfo("White Stained Clay",new String[][] {{"clay","whit"}},								159, (short) 0  ));
		items.add(new ItemInfo("Orange Stained Clay",new String[][]{{"clay","ora"}},								159, (short) 1  ));
		items.add(new ItemInfo("Magenta Stained Clay",new String[][]{{"clay","mag"}},								159, (short) 2  ));
		items.add(new ItemInfo("Light Blue Stained Clay",new String[][]{{"clay","lig","blue"}},						159, (short) 3  ));
		items.add(new ItemInfo("Yellow Stained Clay",new String[][]{{"clay","yell"}},								159, (short) 4  ));
		items.add(new ItemInfo("Lime Stained Clay",	new String[][] {{"clay","lime"}},								159, (short) 5  ));
		items.add(new ItemInfo("Pink Stained Clay",	new String[][] {{"clay","pink"}},								159, (short) 6  ));
		items.add(new ItemInfo("Gray Stained Clay",	new String[][] {{"clay","gray"},{"clay","grey"}},				159, (short) 7  ));
		items.add(new ItemInfo("Light Gray Stained Clay",new String[][]{{"clay","gray","lig"},{"clay","grey","lig"}},159,(short) 8  ));
		items.add(new ItemInfo("Cyan Stained Clay",	new String[][] {{"clay","cya"}},								159, (short) 9  ));
		items.add(new ItemInfo("Purple Stained Clay",new String[][]{{"clay","pur"}},								159, (short) 10 ));
		items.add(new ItemInfo("Blue Stained Clay",	new String[][] {{"clay","blue"}},								159, (short) 11 ));
		items.add(new ItemInfo("Brown Stained Clay",new String[][] {{"clay","brow"}},								159, (short) 12 ));
		items.add(new ItemInfo("Green Stained Clay",new String[][] {{"clay","gree"}},								159, (short) 13 ));
		items.add(new ItemInfo("Red Stained Clay",	new String[][] {{"clay","red"}},								159, (short) 14 ));
		items.add(new ItemInfo("Black Stained Clay",new String[][] {{"clay","bla"}},								159, (short) 15 ));
		items.add(new ItemInfo("Hay Block",			new String[][] {{"hay"}},										170, (short) 0  ));
		items.add(new ItemInfo("White Carpet",		new String[][] {{"carp","whit"}},								171, (short) 0  ));
		items.add(new ItemInfo("Orange Carpet",		new String[][] {{"carp","ora"}},								171, (short) 1  ));
		items.add(new ItemInfo("Magenta Carpet",	new String[][] {{"carp","mag"}},								171, (short) 2  ));
		items.add(new ItemInfo("Light Blue Carpet",	new String[][] {{"carp","lig","blue"}},							171, (short) 3  ));
		items.add(new ItemInfo("Yellow Carpet",		new String[][] {{"carp","yell"}},								171, (short) 4  ));
		items.add(new ItemInfo("Lime Carpet",		new String[][] {{"carp","lime"}},								171, (short) 5  ));
		items.add(new ItemInfo("Pink Carpet",		new String[][] {{"carp","pink"}},								171, (short) 6  ));
		items.add(new ItemInfo("Gray Carpet",		new String[][] {{"carp","gray"},{"carp","grey"}},				171, (short) 7  ));
		items.add(new ItemInfo("Light Gray Carpet",	new String[][] {{"carp","gray","lig"},{"carp","grey","lig"}},	171, (short) 8  ));
		items.add(new ItemInfo("Cyan Carpet",		new String[][] {{"carp","cya"}},								171, (short) 9  ));
		items.add(new ItemInfo("Purple Carpet",		new String[][] {{"carp","pur"}},								171, (short) 10 ));
		items.add(new ItemInfo("Blue Carpet",		new String[][] {{"carp","blue"}},								171, (short) 11 ));
		items.add(new ItemInfo("Brown Carpet",		new String[][] {{"carp","brow"}},								171, (short) 12 ));
		items.add(new ItemInfo("Green Carpet",		new String[][] {{"carp","gree"}},								171, (short) 13 ));
		items.add(new ItemInfo("Red Carpet",		new String[][] {{"carp","red"}},								171, (short) 14 ));
		items.add(new ItemInfo("Black Carpet",		new String[][] {{"carp","bla"}},								171, (short) 15 ));
		items.add(new ItemInfo("Hardened Clay",		new String[][] {{"hard","clay"}},								172, (short) 0  ));
		items.add(new ItemInfo("Coal Block",		new String[][] {{"coal","bl"}},									173, (short) 0  ));
		
		items.add(new ItemInfo("Iron Shovel",		new String[][] {{"shov","ir"}},									256, (short) 0  ));
		items.add(new ItemInfo("Iron Pickaxe",		new String[][] {{"pick","ir"},{"pick","axe","ir"}},				257, (short) 0  ));
		items.add(new ItemInfo("Iron Axe",			new String[][] {{"axe","ir"}},									258, (short) 0  ));
		items.add(new ItemInfo("Flint and Steel",	new String[][] {{"flin","ste"}},								259, (short) 0  ));
		items.add(new ItemInfo("Red Apple",			new String[][] {{"appl"},{"red","appl"}},						260, (short) 0  ));
		items.add(new ItemInfo("Bow",				new String[][] {{"bow"}},										261, (short) 0  ));
		items.add(new ItemInfo("Arrow",				new String[][] {{"arrow"}},										262, (short) 0  ));
		items.add(new ItemInfo("Coal",				new String[][] {{"coal"}},										263, (short) 0  ));
		items.add(new ItemInfo("Charcoal",			new String[][] {{"char","coal"},{"char"}},						263, (short) 1  ));
		items.add(new ItemInfo("Diamond",			new String[][] {{"diamo"}},										264, (short) 0  ));
		items.add(new ItemInfo("Iron Ingot",		new String[][] {{"ingo","ir"},{"iron"}},						265, (short) 0  ));
		items.add(new ItemInfo("Gold Ingot",		new String[][] {{"ingo","go"},{"gold"}},						266, (short) 0  ));
		items.add(new ItemInfo("Iron Sword",		new String[][] {{"swor","ir"}},									267, (short) 0  ));
		items.add(new ItemInfo("Wooden Sword",		new String[][] {{"swor","woo"}},								268, (short) 0  ));
		items.add(new ItemInfo("Wooden Shovel",		new String[][] {{"shov","wo"}},									269, (short) 0  ));
		items.add(new ItemInfo("Wooden Pickaxe",	new String[][] {{"pick","woo"},{"pick","axe","woo"}},			270, (short) 0  ));
		items.add(new ItemInfo("Wooden Axe",		new String[][] {{"axe","woo"}},									271, (short) 0  ));
		items.add(new ItemInfo("Stone Sword",		new String[][] {{"swor","sto"}},								272, (short) 0  ));
		items.add(new ItemInfo("Stone Shovel",		new String[][] {{"shov","sto"}},								273, (short) 0  ));
		items.add(new ItemInfo("Stone Pickaxe",		new String[][] {{"pick","sto"},{"pick","axe","sto"}},			274, (short) 0  ));
		items.add(new ItemInfo("Stone Axe",			new String[][] {{"axe","sto"}},									275, (short) 0  ));
		items.add(new ItemInfo("Diamond Sword",		new String[][] {{"swor","dia"}},								276, (short) 0  ));
		items.add(new ItemInfo("Diamond Shovel",	new String[][] {{"shov","dia"}},								277, (short) 0  ));
		items.add(new ItemInfo("Diamond Pickaxe",	new String[][] {{"pick","dia"},{"pick","axe","dia"}},			278, (short) 0  ));
		items.add(new ItemInfo("Diamond Axe",		new String[][] {{"axe","dia"}},									279, (short) 0  ));
		items.add(new ItemInfo("Stick",				new String[][] {{"stic"}},										280, (short) 0  ));
		items.add(new ItemInfo("Bowl",				new String[][] {{"bowl","bo","wl"}},							281, (short) 0  ));
		items.add(new ItemInfo("Mushroom Stew",		new String[][] {{"stew"}},										282, (short) 0  ));
		items.add(new ItemInfo("Gold Sword",		new String[][] {{"swor","gol"}},								283, (short) 0  ));
		items.add(new ItemInfo("Gold Shovel",		new String[][] {{"shov","gol"}},								284, (short) 0  ));
		items.add(new ItemInfo("Gold Pickaxe",		new String[][] {{"pick","gol"},{"pick","axe","gol"}},			285, (short) 0  ));
		items.add(new ItemInfo("Gold Axe",			new String[][] {{"axe","gol"}},									286, (short) 0  ));
		items.add(new ItemInfo("String",			new String[][] {{"stri"}},										287, (short) 0  ));
		items.add(new ItemInfo("Feather",			new String[][] {{"feat"}},										288, (short) 0  ));
		items.add(new ItemInfo("Gunpowder",			new String[][] {{"gun"},{"sulph"}},								289, (short) 0  ));
		items.add(new ItemInfo("Wooden Hoe",		new String[][] {{"hoe","wo"}},									290, (short) 0  ));
		items.add(new ItemInfo("Stone Hoe",			new String[][] {{"hoe","sto"}},									291, (short) 0  ));
		items.add(new ItemInfo("Iron Hoe",			new String[][] {{"hoe","iro"}},									292, (short) 0  ));
		items.add(new ItemInfo("Diamond Hoe",		new String[][] {{"hoe","dia"}},									293, (short) 0  ));
		items.add(new ItemInfo("Gold Hoe",			new String[][] {{"hoe","go"}},									294, (short) 0  ));
		items.add(new ItemInfo("Seeds",				new String[][] {{"seed"}},										295, (short) 0  ));
		items.add(new ItemInfo("Wheat",				new String[][] {{"whea"}},										296, (short) 0  ));
		items.add(new ItemInfo("Bread",				new String[][] {{"brea"}},										297, (short) 0  ));
		items.add(new ItemInfo("Leather Cap",		new String[][] {{"cap","lea"},{"helm","lea"}},					298, (short) 0  ));
		items.add(new ItemInfo("Leather Tunic",		new String[][] {{"tun","lea"},{"ches","lea"}},					299, (short) 0  ));
		items.add(new ItemInfo("Leather Pants",		new String[][] {{"pan","lea"},{"leg","lea"}},					300, (short) 0  ));
		items.add(new ItemInfo("Leather Boots",		new String[][] {{"boo","lea"}},									301, (short) 0  ));
		items.add(new ItemInfo("Chain Helmet",		new String[][] {{"cap","cha"},{"helm","cha"}},					302, (short) 0  ));
		items.add(new ItemInfo("Chain Chestplate",	new String[][] {{"tun","cha"},{"ches","cha"}},					303, (short) 0  ));
		items.add(new ItemInfo("Chain Leggings",	new String[][] {{"pan","cha"},{"leg","cha"}},					304, (short) 0  ));
		items.add(new ItemInfo("Chain Boots",		new String[][] {{"boo","cha"}},									305, (short) 0  ));
		items.add(new ItemInfo("Iron Helmet",		new String[][] {{"cap","ir"},{"helm","ir"}},					306, (short) 0  ));
		items.add(new ItemInfo("Iron Chestplate",	new String[][] {{"tun","ir"},{"ches","ir"}},					307, (short) 0  ));
		items.add(new ItemInfo("Iron Leggings",		new String[][] {{"pan","ir"},{"leg","ir"}},						308, (short) 0  ));
		items.add(new ItemInfo("Iron Boots",		new String[][] {{"boo","ir"}},									309, (short) 0  ));
		items.add(new ItemInfo("Diamond Helmet",	new String[][] {{"cap","dia"},{"helm","dia"}},					310, (short) 0  ));
		items.add(new ItemInfo("Diamond Chestplate",new String[][] {{"tun","dia"},{"ches","dia"}},					311, (short) 0  ));
		items.add(new ItemInfo("Diamond Leggings",	new String[][] {{"pan","dia"},{"leg","dia"}},					312, (short) 0  ));
		items.add(new ItemInfo("Diamond Boots",		new String[][] {{"boo","dia"}},									313, (short) 0  ));
		items.add(new ItemInfo("Gold Helmet",		new String[][] {{"cap","go"},{"helm","go"}},					314, (short) 0  ));
		items.add(new ItemInfo("Gold Chestplate",	new String[][] {{"tun","go"},{"ches","go"}},					315, (short) 0  ));
		items.add(new ItemInfo("Gold Leggings",		new String[][] {{"pan","go"},{"leg","go"}},						316, (short) 0  ));
		items.add(new ItemInfo("Gold Boots",		new String[][] {{"boo","go"}},									317, (short) 0  ));
		items.add(new ItemInfo("Flint",				new String[][] {{"flin"}},										318, (short) 0  ));
		items.add(new ItemInfo("Raw Porkchop",		new String[][] {{"raw","pork"}},								319, (short) 0  ));
		items.add(new ItemInfo("Cooked Porkchop",	new String[][] {{"cook","pork"}},								320, (short) 0  ));
		items.add(new ItemInfo("Painting",			new String[][] {{"painting"}},									321, (short) 0  ));
		items.add(new ItemInfo("Golden Apple",		new String[][] {{"appl","go"}},									322, (short) 0  ));
		items.add(new ItemInfo("Enchanted Golden Apple",new String[][]{{"appl","go","ench"}},						322, (short) 1  ));
		items.add(new ItemInfo("Sign",				new String[][] {{"sign"}},										323, (short) 0  ));
		items.add(new ItemInfo("Wooden Door",		new String[][] {{"door","wood"},{"door"}},						324, (short) 0  ));
		items.add(new ItemInfo("Bucket",			new String[][] {{"buck"}},										325, (short) 0  ));
		items.add(new ItemInfo("Water Bucket",		new String[][] {{"water","buck"}},								326, (short) 0  ));
		items.add(new ItemInfo("Lava Bucket",		new String[][] {{"lava","buck"}},								327, (short) 0  ));
		items.add(new ItemInfo("Minecart",			new String[][] {{"cart"}},										328, (short) 0  ));
		items.add(new ItemInfo("Saddle",			new String[][] {{"sadd"}},										329, (short) 0  ));
		items.add(new ItemInfo("Iron Door",			new String[][] {{"door","iron"}},								330, (short) 0  ));
		items.add(new ItemInfo("Redstone Dust",		new String[][] {{"red","ston"}},								331, (short) 0  ));
		items.add(new ItemInfo("Snowball",			new String[][] {{"snow","ball"}},								332, (short) 0  ));
		items.add(new ItemInfo("Boat",				new String[][] {{"boat"}},										333, (short) 0  ));
		items.add(new ItemInfo("Leather",			new String[][] {{"lea","the"}},									334, (short) 0  ));
		items.add(new ItemInfo("Milk Bucket",		new String[][] {{"milk"}},										335, (short) 0  ));
		items.add(new ItemInfo("Clay Brick",		new String[][] {{"bric","cla"}},								336, (short) 0  ));
		items.add(new ItemInfo("Clay",				new String[][] {{"cla"}},										337, (short) 0  ));
		items.add(new ItemInfo("Sugar Cane",		new String[][] {{"cane"}},										338, (short) 0  ));
		items.add(new ItemInfo("Paper",				new String[][] {{"pape"}},										339, (short) 0  ));
		items.add(new ItemInfo("Book",				new String[][] {{"book"}},										340, (short) 0  ));
		items.add(new ItemInfo("Slimeball",			new String[][] {{"slime"}},										341, (short) 0  ));
		items.add(new ItemInfo("Minecart with Chest",new String[][]{{"cart","sto"},{"cart","che"}},					342, (short) 0  ));
		items.add(new ItemInfo("Minecart with Furnace",new String[][]{{"cart","pow"},{"cart","furn"}},				343, (short) 0  ));
		items.add(new ItemInfo("Egg",				new String[][] {{"egg"}},										344, (short) 0  ));
		items.add(new ItemInfo("Compass",			new String[][] {{"comp"}},										345, (short) 0  ));
		items.add(new ItemInfo("Fishing Rod",		new String[][] {{"fish","rod"},{"fish","pole"}},				346, (short) 0  ));
		items.add(new ItemInfo("Clock",				new String[][] {{"cloc"},{"watc"}},								347, (short) 0  ));
		items.add(new ItemInfo("Glowstone Dust",	new String[][] {{"glow","sto","dus"},{"glow","dus"}},			348, (short) 0  ));
		items.add(new ItemInfo("Raw Fish",			new String[][] {{"fish"},{"raw","fish"}},						349, (short) 0  ));
		items.add(new ItemInfo("Cooked Fish",		new String[][] {{"fish","coo"}},								350, (short) 0  ));
		items.add(new ItemInfo("Ink Sac",			new String[][] {{"dye","bla"},	{"ink"}},						351, (short) 0  ));
		items.add(new ItemInfo("Rose Red",			new String[][] {{"dye","red"},	{"rose","red"}},				351, (short) 1  ));
		items.add(new ItemInfo("Cactus Green",		new String[][] {{"dye","gree"},	{"cact","gree"}},				351, (short) 2  ));
		items.add(new ItemInfo("Cocoa Beans",		new String[][] {{"dye","bro"},	{"bean"},{"choco"},{"coco"}},	351, (short) 3  ));
		items.add(new ItemInfo("Lapis Lazuli",		new String[][] {{"dye","blu"},	{"lapis"}},						351, (short) 4  ));
		items.add(new ItemInfo("Purple Dye",		new String[][] {{"dye","pur"}},									351, (short) 5  ));
		items.add(new ItemInfo("Cyan Dye",			new String[][] {{"dye","cya"}},									351, (short) 6  ));
		items.add(new ItemInfo("Light Gray Dye",	new String[][] {{"dye","lig","gra"},{"dye","lig","grey"}},		351, (short) 7  ));
		items.add(new ItemInfo("Gray Dye",			new String[][] {{"dye","gra"},{"dye","grey"}},					351, (short) 8  ));
		items.add(new ItemInfo("Pink Dye",			new String[][] {{"dye","pin"}},									351, (short) 9  ));
		items.add(new ItemInfo("Lime Dye",			new String[][] {{"dye","lim"},{"dye","lig","gree"}},			351, (short) 10 ));
		items.add(new ItemInfo("Dandelion Yellow",	new String[][] {{"dye","yel"},	{"dand","yel"}},				351, (short) 11 ));
		items.add(new ItemInfo("Light Blue Dye",	new String[][] {{"dye","lig","blu"}},							351, (short) 12 ));
		items.add(new ItemInfo("Magenta Dye",		new String[][] {{"dye","mag"}},									351, (short) 13 ));
		items.add(new ItemInfo("Orange Dye",		new String[][] {{"dye","ora"}},									351, (short) 14 ));
		items.add(new ItemInfo("Bone Meal",			new String[][] {{"dye","whi"},	{"bonem"},{"bone","me"}},		351, (short) 15 ));
		items.add(new ItemInfo("Bone",				new String[][] {{"bone"}},										352, (short) 0  ));
		items.add(new ItemInfo("Sugar",				new String[][] {{"suga"}},										353, (short) 0  ));
		items.add(new ItemInfo("Cake",				new String[][] {{"cake"}},										354, (short) 0  ));
		items.add(new ItemInfo("Bed",				new String[][] {{"bed"}},										355, (short) 0  ));
		items.add(new ItemInfo("Redstone Repeater",	new String[][] {{"rep"},{"rep","red"},{"rep","ston","red"}},	356, (short) 0  ));
		items.add(new ItemInfo("Cookie",			new String[][] {{"cooki"}},										357, (short) 0  ));
		items.add(new ItemInfo("Map",				new String[][] {{"map"}},										358, (short) 0  ));
		items.add(new ItemInfo("Shears",			new String[][] {{"shear"}},										359, (short) 0  ));
		items.add(new ItemInfo("Melon Slice",		new String[][] {{"melo","sli"}},								360, (short) 0  ));
		items.add(new ItemInfo("Pumpkin Seeds",		new String[][] {{"pump","seed"}},								361, (short) 0  ));
		items.add(new ItemInfo("Melon Seeds",		new String[][] {{"melo","seed"}},								362, (short) 0  ));
		items.add(new ItemInfo("Raw Beef",			new String[][] {{"beef","raw"},{"beef"}},						363, (short) 0  ));
		items.add(new ItemInfo("Steak",				new String[][] {{"beef","cook"},{"steak"}},						364, (short) 0  ));
		items.add(new ItemInfo("Raw Chicken",		new String[][] {{"chicken","raw"},{"chicken"}},					365, (short) 0  ));
		items.add(new ItemInfo("Cooked Chicken",	new String[][] {{"chicken","cook"}},							366, (short) 0  ));
		items.add(new ItemInfo("Rotten Flesh",		new String[][] {{"rot","flesh"}},								367, (short) 0  ));
		items.add(new ItemInfo("Ender Pearl",		new String[][] {{"pearl"},{"ender"},{"ender","pearl"}},			368, (short) 0  ));
		items.add(new ItemInfo("Blaze Rod",			new String[][] {{"blaz"},{"blaz","rod"}},						369, (short) 0  ));
		items.add(new ItemInfo("Ghast Tear",		new String[][] {{"ghast"},{"tear"}},							370, (short) 0  ));
		items.add(new ItemInfo("Gold Nugget",		new String[][] {{"nug"},{"gold","nug"}},						371, (short) 0  ));
		items.add(new ItemInfo("Nether Wart",		new String[][] {{"wart"},{"wart","nether"}},					372, (short) 0  ));
items.add(new ItemInfo("Water Bottle",					new String[][] {{"water","bot"}},							373, (short) 0  ));
items.add(new ItemInfo("Bootleg Potion of Regeneration",new String[][] {{"poti","boot","regen"}},					373, (short) 1  ));
items.add(new ItemInfo("Bootleg Potion of Swiftness",	new String[][] {{"poti","boot","swif"}},					373, (short) 2  ));
items.add(new ItemInfo("Bootleg Potion of Fire Resistance",new String[][]{{"poti","boot","fire","res"}},			373, (short) 3  ));
items.add(new ItemInfo("Bootleg Potion of Poison",		new String[][] {{"poti","boot","poi"}},						373, (short) 4  ));
items.add(new ItemInfo("Bootleg Potion of Healing",		new String[][] {{"poti","boot","heal"}},					373, (short) 5  ));
items.add(new ItemInfo("Bootleg Potion of Night Vision",new String[][] {{"poti","boot","nigh"}},					373, (short) 6  ));
items.add(new ItemInfo("Bootleg Clear Potion",			new String[][] {{"poti","boot","clear"}},					373, (short) 7  ));
items.add(new ItemInfo("Bootleg Potion of Weakness",	new String[][] {{"poti","boot","weak"}},					373, (short) 8  ));
items.add(new ItemInfo("Bootleg Potion of Strength",	new String[][] {{"poti","boot","stre"}},					373, (short) 9  ));
items.add(new ItemInfo("Bootleg Potion of Slowness",	new String[][] {{"poti","boot","slow"}},					373, (short) 10 ));
items.add(new ItemInfo("Bootleg Diffuse Potion",		new String[][] {{"poti","boot","diff"}},					373, (short) 11 ));
items.add(new ItemInfo("Bootleg Potion of Harming",		new String[][] {{"poti","boot","har"}},						373, (short) 12 ));
items.add(new ItemInfo("Bootleg Artless Potion",		new String[][] {{"poti","boot","artl"}},					373, (short) 13 ));
items.add(new ItemInfo("Bootleg Potion of Invisibility",new String[][] {{"poti","boot","invi"}},					373, (short) 14 ));
items.add(new ItemInfo("Bootleg Thin Potion",			new String[][] {{"poti","boot","thin"}},					373, (short) 15 ));
items.add(new ItemInfo("Awkward Potion",				new String[][] {{"poti","awk"}},							373, (short) 16 ));
items.add(new ItemInfo("Bootleg Potion of Regeneration 2",new String[][] {{"poti","boot","regen","2"}},				373, (short) 17 ));
items.add(new ItemInfo("Bootleg Potion of Swiftness 2",	new String[][] {{"poti","boot","swif","2"}},				373, (short) 18 ));
items.add(new ItemInfo("Bootleg Potion of Fire Resistance 2",new String[][] {{"poti","boot","fire","res","2"}},		373, (short) 19 ));
items.add(new ItemInfo("Bootleg Potion of Poison 2",	new String[][] {{"poti","boot","poi","2"}},					373, (short) 20 ));
items.add(new ItemInfo("Bootleg Potion of Healing 2",	new String[][] {{"poti","boot","heal","2"}},				373, (short) 21 ));
items.add(new ItemInfo("Bootleg Potion of Night Vision 2",new String[][] {{"poti","boot","clear","2"}},				373, (short) 22 ));
items.add(new ItemInfo("Bungling Potion",				new String[][] {{"poti","bung"}},							373, (short) 23 ));
items.add(new ItemInfo("Bootleg Potion of Weakness 2",	new String[][] {{"poti","boot","weak","2"}},				373, (short) 24 ));
items.add(new ItemInfo("Bootleg Potion of Strength 2",	new String[][] {{"poti","boot","stre","2"}},				373, (short) 25 ));
items.add(new ItemInfo("Bootleg Potion of Slowness 2",	new String[][] {{"poti","boot","slow","2"}},				373, (short) 26 ));
items.add(new ItemInfo("Smooth Potion",					new String[][] {{"poti","smoo"}},							373, (short) 27 ));
items.add(new ItemInfo("Bootleg Potion of Harming 2",	new String[][] {{"poti","boot","har","2"}},					373, (short) 28 ));
items.add(new ItemInfo("Suave Potion",					new String[][] {{"poti","suav"}},							373, (short) 29 ));
items.add(new ItemInfo("Bootleg Potion of Invisibility 2",new String[][] {{"poti","boot","invi","2"}},				373, (short) 30 ));
items.add(new ItemInfo("Debonair Potion",				new String[][] {{"poti","debo"}},							373, (short) 31 ));
items.add(new ItemInfo("Thick Potion",					new String[][] {{"poti","thick"}},							373, (short) 32 ));
items.add(new ItemInfo("Bootleg Potion of Regeneration 3",new String[][] {{"poti","boot","regen","3"}},				373, (short) 33 ));
items.add(new ItemInfo("Bootleg Potion of Swiftness 3",	new String[][] {{"poti","boot","swif","3"}},				373, (short) 34 ));
items.add(new ItemInfo("Bootleg Potion of Fire Resistance 3",new String[][] {{"poti","boot","fire","res","3"}},		373, (short) 35 ));
items.add(new ItemInfo("Bootleg Potion of Poison 3",	new String[][] {{"poti","boot","poi","3"}},					373, (short) 36 ));
items.add(new ItemInfo("Bootleg Potion of Healing 3",	new String[][] {{"poti","boot","heal","3"}},				373, (short) 37 ));
items.add(new ItemInfo("Bootleg Potion of Night Vision 3",new String[][] {{"poti","boot","clear","3"}},				373, (short) 38 ));
items.add(new ItemInfo("Charming Potion",				new String[][] {{"poti","char"}},							373, (short) 39 ));
items.add(new ItemInfo("Bootleg Potion of Weakness 3",	new String[][] {{"poti","boot","weak","3"}},				373, (short) 40 ));
items.add(new ItemInfo("Bootleg Potion of Strength 3",	new String[][] {{"poti","boot","stre","3"}},				373, (short) 41 ));
items.add(new ItemInfo("Bootleg Potion of Slowness 3",	new String[][] {{"poti","boot","slow","3"}},				373, (short) 42 ));
items.add(new ItemInfo("Refined Potion",				new String[][] {{"poti","refi"}},							373, (short) 43 ));
items.add(new ItemInfo("Bootleg Potion of Harming 3",	new String[][] {{"poti","boot","har","3"}},					373, (short) 44 ));
items.add(new ItemInfo("Cordial Potion",				new String[][] {{"poti","cord"}},							373, (short) 45 ));
items.add(new ItemInfo("Bootleg Potion of Invisibility 3",new String[][] {{"poti","boot","invi","3"}},				373, (short) 46 ));
items.add(new ItemInfo("Sparkling Potion",				new String[][] {{"poti","spar"}},							373, (short) 47 ));
items.add(new ItemInfo("Potent Potion",					new String[][] {{"poti","cord"}},							373, (short) 48 ));
items.add(new ItemInfo("Bootleg Potion of Regeneration 4",new String[][] {{"poti","boot","regen","4"}},				373, (short) 49 ));
items.add(new ItemInfo("Bootleg Potion of Swiftness 4",	new String[][] {{"poti","boot","swif","4"}},				373, (short) 50 ));
items.add(new ItemInfo("Bootleg Potion of Fire Resistance 4",new String[][] {{"poti","boot","fire","res","4"}},		373, (short) 51 ));
items.add(new ItemInfo("Bootleg Potion of Poison 4",	new String[][] {{"poti","boot","poi","4"}},					373, (short) 52 ));
items.add(new ItemInfo("Bootleg Potion of Healing 4",	new String[][] {{"poti","boot","heal","4"}},				373, (short) 53 ));
items.add(new ItemInfo("Bootleg Potion of Night Vision 4",new String[][] {{"poti","boot","clear","4"}},				373, (short) 54 ));
items.add(new ItemInfo("Rank Potion",					new String[][] {{"poti","rank"}},							373, (short) 55 ));
items.add(new ItemInfo("Bootleg Potion of Weakness 4",	new String[][] {{"poti","boot","weak","4"}},				373, (short) 56 ));
items.add(new ItemInfo("Bootleg Potion of Strength 4",	new String[][] {{"poti","boot","stre","4"}},				373, (short) 57 ));
items.add(new ItemInfo("Bootleg Potion of Slowness 4",	new String[][] {{"poti","boot","slow","4"}},				373, (short) 58 ));
items.add(new ItemInfo("Acrid Potion",					new String[][] {{"poti","acri"}},							373, (short) 59 ));
items.add(new ItemInfo("Bootleg Potion of Harming 4",	new String[][] {{"poti","boot","har","4"}},					373, (short) 60 ));
items.add(new ItemInfo("Gross Potion",					new String[][] {{"poti","gros"}},							373, (short) 61 ));
items.add(new ItemInfo("Bootleg Potion of Invisibility 4",new String[][] {{"poti","boot","invi","4"}},				373, (short) 62 ));
items.add(new ItemInfo("Stinky Potion",					new String[][] {{"poti","stin"}},							373, (short) 63 ));
items.add(new ItemInfo("Mundane Potion",				new String[][] {{"poti","mun"}},							373, (short)8192));
items.add(new ItemInfo("Potion of Regeneration",		new String[][] {{"poti","regen"}},							373, (short)8193));
items.add(new ItemInfo("Potion of Regeneration (extended)",new String[][]{{"poti","regen","ext"}},					373, (short)8257));
items.add(new ItemInfo("Potion of Regeneration 2",		new String[][] {{"poti","regen","2"}},						373, (short)8225));
items.add(new ItemInfo("Potion of Swiftness",			new String[][] {{"poti","swif"}},							373, (short)8194));
items.add(new ItemInfo("Potion of Swiftness (extended)",new String[][] {{"poti","swif","ext"}},						373, (short)8258));
items.add(new ItemInfo("Potion of Swiftness 2",			new String[][] {{"poti","swif","2"}},						373, (short)8226));
items.add(new ItemInfo("Potion of Fire Resistance",		new String[][] {{"poti","fire","res"}},						373, (short)8195));
items.add(new ItemInfo("Potion of Fire Resistance (extended)",new String[][]{{"poti","fire","res","ext"}},			373, (short)8259));
items.add(new ItemInfo("Potion of Healing",				new String[][] {{"poti","heal"}},							373, (short)8197));
items.add(new ItemInfo("Potion of Healing 2",			new String[][] {{"poti","heal","2"}},						373, (short)8229));
items.add(new ItemInfo("Potion of Strength",			new String[][] {{"poti","stre"}},							373, (short)8201));
items.add(new ItemInfo("Potion of Strength (extended)",	new String[][] {{"poti","stre","ext"}},						373, (short)8265));
items.add(new ItemInfo("Potion of Strength 2",			new String[][] {{"poti","stre","2"}},						373, (short)8233));
items.add(new ItemInfo("Potion of Poison",				new String[][] {{"poti","poi"}},							373, (short)8196));
items.add(new ItemInfo("Potion of Poison (extended)",	new String[][] {{"poti","poi","ext"}},						373, (short)8260));
items.add(new ItemInfo("Potion of Poison 2",			new String[][] {{"poti","poi","2"}},						373, (short)8228));
items.add(new ItemInfo("Potion of Weakness",			new String[][] {{"poti","weak"}},							373, (short)8200));
items.add(new ItemInfo("Potion of Weakness (extended)",	new String[][] {{"poti","weak","ext"}},						373, (short)8264));
items.add(new ItemInfo("Potion of Slowness",			new String[][] {{"poti","slow"}},							373, (short)8202));
items.add(new ItemInfo("Potion of Slowness (extended)",	new String[][] {{"poti","slow","ext"}},						373, (short)8266));
items.add(new ItemInfo("Potion of Harming",				new String[][] {{"poti","har"}},							373, (short)8204));
items.add(new ItemInfo("Potion of Harming 2",			new String[][] {{"poti","har","2"}},						373, (short)8236));
items.add(new ItemInfo("Splash Mundane Potion",			new String[][] {{"poti","spl","mun"}},						373, (short)16384));
items.add(new ItemInfo("Splash Potion of Regeneration",	new String[][] {{"poti","spl","regen"}},					373, (short)16385));
items.add(new ItemInfo("Splash Potion of Regeneration (extended)",new String[][]{{"poti","spl","regen","ext"}},		373, (short)16449));
items.add(new ItemInfo("Splash Potion of Regeneration 2", new String[][]{{"poti","spl","reg","2"}},					373, (short)16417));
items.add(new ItemInfo("Splash Potion of Swiftness",	new String[][] {{"poti","spl","swif"}},						373, (short)16386));
items.add(new ItemInfo("Splash Potion of Swiftness (extended)",new String[][]{{"poti","spl","swif","ext"}},			373, (short)16450));
items.add(new ItemInfo("Splash Potion of Swiftness 2",	new String[][] {{"poti","spl","swif","2"}},					373, (short)16418));
items.add(new ItemInfo("Splash Potion of Fire Resistance",new String[][]{{"poti","spl","fire","res"}},				373, (short)16387));
items.add(new ItemInfo("Splash Potion of Fire Resistance (extended)",new String[][]{{"poti","spl","fire","res","ext"}},373,(short)16451));
items.add(new ItemInfo("Splash Potion of Healing",		new String[][] {{"poti","spl","heal"}},						373, (short)16389));
items.add(new ItemInfo("Splash Potion of Healing 2",	new String[][] {{"poti","spl","heal","2"}},					373, (short)16421));
items.add(new ItemInfo("Splash Potion of Strength",		new String[][] {{"poti","spl","stre"}},						373, (short)16393));
items.add(new ItemInfo("Splash Potion of Strength (extended)",new String[][]{{"poti","spl","stre","ext"}},			373, (short)16457));
items.add(new ItemInfo("Splash Potion of Strength 2",	new String[][] {{"poti","spl","stre","2"}},					373, (short)16425));
items.add(new ItemInfo("Splash Potion of Poison",		new String[][] {{"poti","spl","poi"}},						373, (short)16388));
items.add(new ItemInfo("Splash Potion of Poison (extended)",new String[][]{{"poti","spl","poi","ext"}},				373, (short)16452));
items.add(new ItemInfo("Splash Potion of Poison 2",		new String[][] {{"poti","spl","poi","2"}},					373, (short)16420));
items.add(new ItemInfo("Splash Potion of Weakness",		new String[][] {{"poti","spl","weak"}},						373, (short)16392));
items.add(new ItemInfo("Splash Potion of Weakness (extended)",new String[][]{{"poti","spl","weak","ext"}},			373, (short)16456));
items.add(new ItemInfo("Splash Potion of Slowness",		new String[][] {{"poti","spl","slow"}},						373, (short)16394));
items.add(new ItemInfo("Splash Potion of Slowness (extended)",new String[][]{{"poti","spl","slow","ext"}},			373, (short)16458));
items.add(new ItemInfo("Splash Potion of Harming",		new String[][] {{"poti","spl","har"}},						373, (short)16396));
items.add(new ItemInfo("Splash Potion of Harming 2",	new String[][] {{"poti","spl","har","2"}},					373, (short)16428));
		items.add(new ItemInfo("Glass Bottle",		new String[][] {{"glas","bottl"}},								374, (short) 0  ));
		items.add(new ItemInfo("Spider Eye",		new String[][] {{"spid"}},										375, (short) 0  ));
		items.add(new ItemInfo("Fermented Spider Eye",new String[][]{{"ferm"},{"ferm","eye"},{"ferm","spid"}},		376, (short) 0  ));
		items.add(new ItemInfo("Blaze Powder",		new String[][] {{"blaz","pow"}},								377, (short) 0  ));
		items.add(new ItemInfo("Magma Cream",		new String[][] {{"mag","cream"}},								378, (short) 0  ));
		items.add(new ItemInfo("Brewing Stand",		new String[][] {{"brew"}},										379, (short) 0  ));
		items.add(new ItemInfo("Cauldron",			new String[][] {{"caul"}},										380, (short) 0  ));
		items.add(new ItemInfo("Eye of Ender",		new String[][] {{"eye","end"}},									381, (short) 0  ));
		items.add(new ItemInfo("Glistering Melon",	new String[][] {{"glist"},{"glist","melo"}},					382, (short) 0  ));
		items.add(new ItemInfo("XP Egg",			new String[][] {{"egg","xp"}},									383, (short) 2  ));
		items.add(new ItemInfo("Painting Egg",		new String[][] {{"egg","paint"}},								383, (short) 9  ));
		items.add(new ItemInfo("Item Frame Egg",	new String[][] {{"egg","frame"}},								383, (short) 18 ));
		items.add(new ItemInfo("Primed TNT Egg",	new String[][] {{"egg","tnt"}},									383, (short) 20 ));
		items.add(new ItemInfo("Boat Egg",			new String[][] {{"egg","boat"}},								383, (short) 41 ));
		items.add(new ItemInfo("Minecart Egg",		new String[][] {{"egg","cart"}},								383, (short) 42 ));
		items.add(new ItemInfo("Chest Minecart Egg",new String[][] {{"egg","chest","cart"}},						383, (short) 43 ));
		items.add(new ItemInfo("Furnace Minecart Egg",new String[][]{{"egg","furn","cart"}},						383, (short) 44 ));
		items.add(new ItemInfo("TNT Minecart Egg",	new String[][] {{"egg","tnt","cart"}},							383, (short) 45 ));
		items.add(new ItemInfo("Hopper Minecart Egg",new String[][]{{"egg","hopp","cart"}},							383, (short) 46 ));
		items.add(new ItemInfo("Spawner Minecart Egg",new String[][]{{"egg","cart","egg"}},							383, (short) 47 ));
		items.add(new ItemInfo("Creeper Egg",		new String[][] {{"egg","cree"}},								383, (short) 50 ));
		items.add(new ItemInfo("Skeleton Egg",		new String[][] {{"egg","skel"}},								383, (short) 51 ));
		items.add(new ItemInfo("Spider Egg",		new String[][] {{"egg","spid"}},								383, (short) 52 ));
		items.add(new ItemInfo("Giant Egg",			new String[][] {{"egg","gian"}},								383, (short) 53 ));
		items.add(new ItemInfo("Zombie Egg",		new String[][] {{"egg","zom"}},									383, (short) 54 ));
		items.add(new ItemInfo("Slime Egg",			new String[][] {{"egg","slim"}},								383, (short) 55 ));
		items.add(new ItemInfo("Ghast Egg",			new String[][] {{"egg","ghas"}},								383, (short) 56 ));
		items.add(new ItemInfo("Zombie Pigman Egg",	new String[][]{{"egg","pigman"}},								383, (short) 57 ));
		items.add(new ItemInfo("Enderman Egg",		new String[][] {{"egg","end"}},									383, (short) 58 ));
		items.add(new ItemInfo("Cave Spider Egg",	new String[][]{{"egg","cav"}},									383, (short) 59 ));
		items.add(new ItemInfo("Silverfish Egg",	new String[][]{{"egg","silv"}},									383, (short) 60 ));
		items.add(new ItemInfo("Blaze Egg",			new String[][] {{"egg","blaz"}},								383, (short) 61 ));
		items.add(new ItemInfo("Magma Cube Egg",	new String[][]{{"egg","mag"}},									383, (short) 62 ));
		items.add(new ItemInfo("Ender Dragon Egg",	new String[][]{{"egg","dragon"}},								383, (short) 63 ));
		items.add(new ItemInfo("Wither Egg",		new String[][] {{"egg","with"}},								383, (short) 64 ));
		items.add(new ItemInfo("Bat Egg",			new String[][] {{"egg","bat"}},									383, (short) 65 ));
		items.add(new ItemInfo("Witch Egg",			new String[][] {{"egg","witc"}},								383, (short) 66 ));
		items.add(new ItemInfo("Pig Egg",			new String[][] {{"egg","pig"}},									383, (short) 90 ));
		items.add(new ItemInfo("Sheep Egg",			new String[][] {{"egg","she"}},									383, (short) 91 ));
		items.add(new ItemInfo("Cow Egg",			new String[][] {{"egg","cow"}},									383, (short) 92 ));
		items.add(new ItemInfo("Chicken Egg",		new String[][] {{"egg","chi"}},									383, (short) 93 ));
		items.add(new ItemInfo("Squid Egg",			new String[][] {{"egg","squ"}},									383, (short) 94 ));
		items.add(new ItemInfo("Wolf Egg",			new String[][] {{"egg","wolf"}},								383, (short) 95 ));
		items.add(new ItemInfo("Mooshroom Egg",		new String[][]{{"egg","moo"}},									383, (short) 96 ));
		items.add(new ItemInfo("Snow Golem Egg",	new String[][]{{"egg","snow"}},									383, (short) 97 ));
		items.add(new ItemInfo("Ocelot Egg",		new String[][] {{"egg","ocel"}},								383, (short) 98 ));
		items.add(new ItemInfo("Iron Golem Egg",	new String[][]{{"egg","iron"}},									383, (short) 99 ));
		items.add(new ItemInfo("Horse Egg",			new String[][] {{"egg","hors"}},								383, (short) 100));
		items.add(new ItemInfo("Villager Egg",		new String[][] {{"egg","vil"}},									383, (short) 120));
		items.add(new ItemInfo("Ender Crystal Egg",	new String[][]{{"egg","cry"}},									383, (short) 200));
		items.add(new ItemInfo("Bottle o' Enchanting",new String[][]{{"bott","ench"}},								384, (short) 0  ));
		items.add(new ItemInfo("Fire Charge",		new String[][] {{"bott","ench"}},								385, (short) 0  ));
		items.add(new ItemInfo("Book and Quill",	new String[][] {{"book","quil"}},								386, (short) 0  ));
		items.add(new ItemInfo("Emerald",			new String[][] {{"emer"}},										388, (short) 0  ));
		items.add(new ItemInfo("Item Frame",		new String[][] {{"item","frame"},{"frame"}},					389, (short) 0  ));
		items.add(new ItemInfo("Flower Pot",		new String[][] {{"flow","pot"}},								390, (short) 0  ));
		items.add(new ItemInfo("Carrot",			new String[][] {{"carrot"}},									391, (short) 0  ));
		items.add(new ItemInfo("Potato",			new String[][] {{"potat"}},										392, (short) 0  ));
		items.add(new ItemInfo("Baked Potato",		new String[][] {{"bak","potat"}},								393, (short) 0  ));
		items.add(new ItemInfo("Poisonous Potato",	new String[][] {{"poi","potat"}},								394, (short) 0  ));
		items.add(new ItemInfo("Empty Map",			new String[][] {{"map","blank"},{"map","empty"}},				395, (short) 0  ));
		items.add(new ItemInfo("Golden Carrot",		new String[][] {{"gold","carrot"}},								396, (short) 0  ));
		items.add(new ItemInfo("Skeleton Head",		new String[][] {{"head","skel"}},								397, (short) 0  ));
		items.add(new ItemInfo("Wither Skeleton Head",new String[][]{{"head","skel","with"}},						397, (short) 1  ));
		items.add(new ItemInfo("Zombie Head",		new String[][] {{"head","zom"}},								397, (short) 2  ));
		items.add(new ItemInfo("Human Head",		new String[][] {{"head","hum"}},								397, (short) 3  ));
		items.add(new ItemInfo("Creeper Head",		new String[][] {{"head","cre"}},								397, (short) 4  ));
		items.add(new ItemInfo("Carrot on a Stick",	new String[][] {{"stic","carrot"}},								398, (short) 0  ));
		items.add(new ItemInfo("Nether Star",		new String[][] {{"neth","star"}},								399, (short) 0  ));
		items.add(new ItemInfo("Pumpkin Pie",		new String[][] {{"pump","pie"}},								400, (short) 0  ));
		items.add(new ItemInfo("Firework Rocket",	new String[][] {{"rocke"}},										401, (short) 0  ));
		items.add(new ItemInfo("Firework Star",		new String[][] {{"firew","star"}},								402, (short) 0  ));
		items.add(new ItemInfo("Redstone Comparator",new String[][]{{"reds","comp"}},								404, (short) 0  ));
		items.add(new ItemInfo("Nether Brick",		new String[][] {{"neth","bric"}},								405, (short) 0  ));
		items.add(new ItemInfo("Nether Quartz",		new String[][] {{"neth","quar"}},								406, (short) 0  ));
		items.add(new ItemInfo("Minecart with TNT",	new String[][] {{"cart","tnt"}},								407, (short) 0  ));
		items.add(new ItemInfo("Minecart with Hopper",new String[][]{{"cart","hop"}},								408, (short) 0  ));
		items.add(new ItemInfo("Iron Horse Armor",	new String[][] {{"iron","hors"}},								417, (short) 0  ));
		items.add(new ItemInfo("Gold Horse Armor",	new String[][] {{"gol","hors"}},								418, (short) 0  ));
		items.add(new ItemInfo("Diamond Horse Armor",new String[][]{{"dia","hors"}},								419, (short) 0  ));
		items.add(new ItemInfo("Lead",				new String[][] {{"lead","leash"}},								420, (short) 0  ));
		items.add(new ItemInfo("Name Tag",			new String[][] {{"lead","leash"}},								421, (short) 0  ));
		
		items.add(new ItemInfo("13 Disc",			new String[][] {{"dis","13"}},									2256,(short) 0  ));
		items.add(new ItemInfo("Cat Disc",			new String[][] {{"dis","cat"}},									2257,(short) 0  ));
		items.add(new ItemInfo("Blocks Disc",		new String[][] {{"dis","blo"}},									2258,(short) 0  ));
		items.add(new ItemInfo("Chirp Disc",		new String[][] {{"dis","chi"}},									2259,(short) 0  ));
		items.add(new ItemInfo("Far Disc",			new String[][] {{"dis","far"}},									2260,(short) 0  ));
		items.add(new ItemInfo("Mall Disc",			new String[][] {{"dis","mal"}},									2261,(short) 0  ));
		items.add(new ItemInfo("Mellohi Disc",		new String[][] {{"dis","mel"}},									2262,(short) 0  ));
		items.add(new ItemInfo("Stal Disc",			new String[][] {{"dis","sta"}},									2263,(short) 0  ));
		items.add(new ItemInfo("Strad Disc",		new String[][] {{"dis","str"}},									2264,(short) 0  ));
		items.add(new ItemInfo("Ward Disc",			new String[][] {{"dis","war"}},									2265,(short) 0  ));
		items.add(new ItemInfo("11 Disc",			new String[][] {{"dis","11"}},									2266,(short) 0  ));
		items.add(new ItemInfo("Wait Disc",			new String[][] {{"dis","wai"}},									2267,(short) 0  ));
		
		if(!itemsyml.exists())
		{
			try{
				FileWriter out = new FileWriter(itemsyml);
				out.write("items:\n");
				for(ItemInfo itm : items) out.write(itm.toString());
				out.close();
			}catch(IOException e){
				log.warning(String.format((Locale)null,"[%s] Couldn't write fresh items.yml: %s", CommandShops.pdfFile.getName(), e));
			}
			log.info(String.format((Locale)null,
					"[%s] Wrote standard item set out to items.yml",
					CommandShops.pdfFile.getName()));
		}
	}

	/**
	 * Concatenate elements of an array into a glue-separated string, similar to PHP's implode()
	 * @param array
	 * the elements to join
	 * @param glue
	 * the separator
	 * @return the joined string
	 */
	public static String join(String[] array, String glue)
	{
		StringBuilder joined = new StringBuilder();
		if(array.length>0) joined.append(array[0]);
		for(int i=1; i<array.length; i++)
		{
			joined.append(glue);
			joined.append(array[i]);
		}
		return joined.toString();
	}

	/**
	 * Concatenate elements of a list into a glue-separated string, similar to PHP's implode()
	 * @param list
	 * the elements to join
	 * @param glue
	 * the separator
	 * @return the joined string
	 */
	public static String join(List<String> list, String glue)
	{
		StringBuilder joined = new StringBuilder();
		for(String element: list)
		{
			joined.append(element);
			joined.append(glue);
		}
		joined.delete(joined.lastIndexOf(glue), joined.length());
		return joined.toString();
	}

	/**
	 * Get an (@link ItemInfo} by the ItemId. Intended for durable items as it assumes damage=0. 
	 * @param type the type or "item id"
	 * @return the ItemInfo for this item
	 */
	public static ItemInfo itemById(int type)
	{
		return itemById(type, (short)0);
	}

	/**
	 * Get an (@link ItemInfo} by the ItemId and subtype stored in an {@link ItemStack}.
	 * @param stack the ItemStack having the item id and subtype you want to search for.
	 * @return the ItemInfo for this item
	 */
	public static ItemInfo itemById(ItemStack stack)
	{
		return itemById(materials.inverse().get(stack.getType()), stack.getDurability());
	}
	
	
	/**
	 * Get an (@link ItemInfo} by both IDs -- type and subtype
	 * @param type the type or "item id"
	 * @param subType the subtype or "damage value"
	 * @return the ItemInfo for this item
	 */
	public static ItemInfo itemById(int type, short subType)
	{
		for(ItemInfo item: items)
		{
			if(item.isDurable())
			{
				if(item.typeId == type)
				{
					return item;
				}
			}else{
				if(item.typeId == type && item.subTypeId == subType)
				{
					return item;
				}
			}
		}
		return null;
	}

	/**
	 * Get an {@link ItemInfo} by its name.
	 * @param search
	 * the player-input item name to search for
	 * @return the ItemInfo
	 */
	public static ItemInfo itemByName(ArrayList<String> search)
	{
		String searchString = join(search, " ");
		return itemByName(searchString);
	}

	/**
	 * Get an {@link ItemInfo} by its name.
	 * @param searchString
	 * the player-input item name to search for
	 * @return the ItemInfo
	 */
	public static ItemInfo itemByName(String searchString)
	{
		ItemInfo matchedItem = null;
		int matchedItemStrength = 0;

		/* Match on integer:short to get typeId and subTypeId
		 * This way we can avoid invoking the search algorithm and
		 * look up the item directly by these IDs.
		 */
		if(searchString.matches("\\d+:\\d+"))
		{
			// Retrieve/parse data
			String[] params = searchString.split(":");
			int typeId = Integer.parseInt(params[0]);
			short subTypeId = Short.parseShort(params[1]);

			// Iterate through Items
			for(ItemInfo item: items)
			{
				// Test for match
				if(item.typeId == typeId && item.subTypeId == subTypeId)
				{
					matchedItem = item;
					break;
				}
			}
		}else if(searchString.matches("\\d+")){
			// Match an integer only, assume subTypeId = 0

			// Retrieve/parse data
			int typeId = Integer.parseInt(searchString);
			short subTypeId = 0;

			// Iterate through Items
			for(ItemInfo item: items)
			{
				// Test for match
				if(item.typeId == typeId && item.subTypeId == subTypeId)
				{
					matchedItem = item;
					break;
				}
			}
		}else{
			// Else this must be a string name that we need to search for

			// Iterate through Items
			for(ItemInfo item: items)
			{
				// Look through each possible match criteria
				for(String[] attributes: item.search)
				{
					boolean match = false;
					// Loop through entire criteria strings
					for(String attribute: attributes)
					{
						if(searchString.toLowerCase().contains(attribute))
						{
							match = true;
						}else
						{
							match = false;
							break;
						}
					}

					// THIS was a match
					if(match)
					{
						if(matchedItem == null
								|| attributes.length > matchedItemStrength)
						{
							matchedItem = item;
							matchedItemStrength = attributes.length;
						}

						//we used to break here upon finding a match, but not breaking
						//lets us search everything to find the BEST match
					}
				}
			}
		}

		return matchedItem;
	}
}
