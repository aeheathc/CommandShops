package com.aehdev.commandshops;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a search engine for items.
 */
public class Search
{

	/** Table of information about all items we can deal in. */
	private static ArrayList<ItemInfo> items = new ArrayList<ItemInfo>(500);
	static
	{
		//name,										search,									   					typeId, subTypeId, maxStackSize    	
		items.add(new ItemInfo("Stone",				new String[][] {{"stone"}},										1,	(short) 0, 64 ));
		items.add(new ItemInfo("Grass",				new String[][] {{"gras"}},										2,	(short) 0, 64 ));
		items.add(new ItemInfo("Dirt",				new String[][] {{"dirt"}},										3,	(short) 0, 64 ));
		items.add(new ItemInfo("Cobblestone",		new String[][] {{"cobb","sto"},{"cobb"}},						4,	(short) 0, 64 ));
		items.add(new ItemInfo("Wooden Planks",		new String[][] {{"plank"}},										5,	(short) 0, 64 ));
		items.add(new ItemInfo("Sapling",			new String[][] {{"sapling"}},									6,	(short) 0, 64 ));
		items.add(new ItemInfo("Redwood Sapling",	new String[][] {{"sapling","red"}},								6,	(short) 1, 64 ));
		items.add(new ItemInfo("Birch Sapling",		new String[][] {{"sapling","birch"}},							6,	(short) 2, 64 ));
		items.add(new ItemInfo("Bedrock",			new String[][] {{"rock"}},										7,	(short) 0, 64 ));
		items.add(new ItemInfo("Water",				new String[][] {{"water"}},										9,	(short) 0, 64 ));
		items.add(new ItemInfo("Lava",				new String[][] {{"lava"}},										11, (short) 0, 64 ));
		items.add(new ItemInfo("Sand",				new String[][] {{"sand"}},										12, (short) 0, 64 ));
		items.add(new ItemInfo("Gravel",			new String[][] {{"gravel"}},									13, (short) 0, 64 ));
		items.add(new ItemInfo("Gold Ore",			new String[][] {{"ore","gold"}},								14, (short) 0, 64 ));
		items.add(new ItemInfo("Iron Ore",			new String[][] {{"ore","iron"}},								15, (short) 0, 64 ));
		items.add(new ItemInfo("Coal Ore",			new String[][] {{"ore","coal"}},								16, (short) 0, 64 ));
		items.add(new ItemInfo("Log",				new String[][] {{"log"}},										17, (short) 0, 64 ));
		items.add(new ItemInfo("Redwood Log",		new String[][] {{"log","red"},{"red","wood"}},					17, (short) 1, 64 ));
		items.add(new ItemInfo("Birch Log",			new String[][] {{"birch"},{"log","birch"}},						17, (short) 2, 64 ));
		items.add(new ItemInfo("Leaves",			new String[][] {{"leaf"},{"leaves"}},							18, (short) 0, 64 ));
		items.add(new ItemInfo("Redwood Leaves",	new String[][] {{"lea","red"}},									18, (short) 1, 64 ));
		items.add(new ItemInfo("Birch Leaves",		new String[][] {{"lea","birch"}},								18, (short) 2, 64 ));
		items.add(new ItemInfo("Sponge",			new String[][] {{"sponge"}},									19, (short) 0, 64 ));
		items.add(new ItemInfo("Glass",				new String[][] {{"glas"}},										20, (short) 0, 64 ));
		items.add(new ItemInfo("Lapis Lazuli Ore",	new String[][] {{"lapis","ore"}},								21, (short) 0, 64 ));
		items.add(new ItemInfo("Lapis Lazuli Block",new String[][] {{"lapis","bl"}},								22, (short) 0, 64 ));
		items.add(new ItemInfo("Dispenser",			new String[][] {{"dispen"},{"dis","pen"}},						23, (short) 0, 64 ));
		items.add(new ItemInfo("Sandstone",			new String[][] {{"sand","st"}},									24, (short) 0, 64 ));
		items.add(new ItemInfo("Note Block",		new String[][] {{"note"}},										25, (short) 0, 64 ));
		items.add(new ItemInfo("Powered Rail",		new String[][] {{"rail","pow"},{"trac","pow"},{"boost"}},		27, (short) 0, 64 ));
		items.add(new ItemInfo("Detector Rail",		new String[][] {{"rail","det"},{"trac","det"},{"detec"}},		28, (short) 0, 64 ));
		items.add(new ItemInfo("Sticky Piston",		new String[][] {{"sticky"},{"sticky","pist"}},					29, (short) 7, 64 ));
		items.add(new ItemInfo("Cobweb",			new String[][] {{"web"},{"cobweb"}},							30, (short) 0, 64 ));
		items.add(new ItemInfo("Tall Grass",		new String[][] {{"tall","gras"}},								31, (short) 0, 64 ));
		items.add(new ItemInfo("Dead Bush",			new String[][] {{"dead"},{"dead","bush"},{"dead","shrub"}},		32, (short) 0, 64 ));
		items.add(new ItemInfo("Piston",			new String[][] {{"pist"}},										33, (short) 7, 64 ));
		items.add(new ItemInfo("White Wool",		new String[][] {{"wool","whit"},{"wool"}},						35, (short) 0, 64 ));
		items.add(new ItemInfo("Orange Wool",		new String[][] {{"wool","ora"}},								35, (short) 1, 64 ));
		items.add(new ItemInfo("Magenta Wool",		new String[][] {{"wool","mag"}},								35, (short) 2, 64 ));
		items.add(new ItemInfo("Light Blue Wool",	new String[][] {{"wool","lig","blue"}},							35, (short) 3, 64 ));
		items.add(new ItemInfo("Yellow Wool",		new String[][] {{"wool","yell"}},								35, (short) 4, 64 ));
		items.add(new ItemInfo("Light Green Wool",	new String[][] {{"wool","lig","gree"},{"wool","gree"}},			35, (short) 5, 64 ));
		items.add(new ItemInfo("Pink Wool",			new String[][] {{"wool","pink"}},								35, (short) 6, 64 ));
		items.add(new ItemInfo("Gray Wool",			new String[][] {{"wool","gray"},{"wool","grey"}},				35, (short) 7, 64 ));
		items.add(new ItemInfo("Light Gray Wool",	new String[][] {{"lig","wool","gra"},{"lig","wool","gre"}},		35, (short) 8, 64 ));
		items.add(new ItemInfo("Cyan Wool",			new String[][] {{"wool","cya"}},								35, (short) 9, 64 ));
		items.add(new ItemInfo("Purple Wool",		new String[][] {{"wool","pur"}},								35, (short) 10, 64 ));
		items.add(new ItemInfo("Blue Wool",			new String[][] {{"wool","blue"}},								35, (short) 11, 64 ));
		items.add(new ItemInfo("Brown Wool",		new String[][] {{"wool","brow"}},								35, (short) 12, 64 ));
		items.add(new ItemInfo("Dark Green Wool",	new String[][] {{"wool","dar","gree"},{"wool","gree"}},			35, (short) 13, 64 ));
		items.add(new ItemInfo("Red Wool",			new String[][] {{"wool","red"}},								35, (short) 14, 64 ));
		items.add(new ItemInfo("Black Wool",		new String[][] {{"wool","bla"}},								35, (short) 15, 64 ));
		items.add(new ItemInfo("Dandelion",			new String[][] {{"flow","yell"},{"dande"}},						37, (short) 0, 64 ));
		items.add(new ItemInfo("Rose",				new String[][] {{"flow","red"},{"rose"}},						38, (short) 0, 64 ));
		items.add(new ItemInfo("Brown Mushroom",	new String[][] {{"mush","bro"}},								39, (short) 0, 64 ));
		items.add(new ItemInfo("Red Mushroom",		new String[][] {{"mush","red"}},								40, (short) 0, 64 ));
		items.add(new ItemInfo("Gold Block",		new String[][] {{"gold","bl"}},									41, (short) 0, 64 ));
		items.add(new ItemInfo("Iron Block",		new String[][] {{"iron","bl"}},									42, (short) 0, 64 ));
	items.add(new ItemInfo("Double Stone Slab",		new String[][] {{"dou","slab"},{"dou","slab","sto"}},			43, (short) 0, 64 ));
	items.add(new ItemInfo("Double Sandstone Slab",	new String[][] {{"dou","slab","sand","sto"}},					43, (short) 1, 64 ));
	items.add(new ItemInfo("Double Wooden Slab",	new String[][] {{"dou","slab","woo"}},							43, (short) 2, 64 ));
	items.add(new ItemInfo("Double Cobblestone Slab",new String[][]{{"dou","slab","cob","sto"},{"dou","slab","cob"}},43,(short) 3, 64 ));
	items.add(new ItemInfo("Double Brick Slab",		new String[][] {{"dou","slab","bric"}},							43, (short) 4, 64 ));
	items.add(new ItemInfo("Double Stone Brick Slab",new String[][]{{"dou","slab","bric","sto"}},					43, (short) 5, 64 ));
		items.add(new ItemInfo("Stone Slab",		new String[][] {{"slab"},{"slab","sto"}},						44, (short) 0, 64 ));
		items.add(new ItemInfo("Sandstone Slab",	new String[][] {{"slab","sand","sto"}},							44, (short) 1, 64 ));
		items.add(new ItemInfo("Wooden Slab",		new String[][] {{"slab","woo"}},								44, (short) 2, 64 ));
		items.add(new ItemInfo("Cobblestone Slab",	new String[][] {{"slab","cob","sto"},{"slab","cob"}},			44, (short) 3, 64 ));
		items.add(new ItemInfo("Brick Slab",		new String[][] {{"slab","bric"}},								44, (short) 4, 64 ));
		items.add(new ItemInfo("Stone Brick Slab",	new String[][] {{"slab","bric","sto"}},							44, (short) 5, 64 ));
		items.add(new ItemInfo("Bricks",			new String[][] {{"bric"}},										45, (short) 0, 64 ));
		items.add(new ItemInfo("TNT",				new String[][] {{"tnt"}},										46, (short) 0, 64 ));
		items.add(new ItemInfo("Bookshelf",			new String[][] {{"bookshe"},{"book","she"}},					47, (short) 0, 64 ));
		items.add(new ItemInfo("Moss Stone",		new String[][] {{"moss","sto"},{"moss","cob"},{"moss"}},		48, (short) 0, 64 ));
		items.add(new ItemInfo("Obsidian",			new String[][] {{"obsi"}},										49, (short) 0, 64 ));
		items.add(new ItemInfo("Torch",				new String[][] {{"torc"}},										50, (short) 0, 64 ));
		items.add(new ItemInfo("Fire",				new String[][] {{"fire"}},										51, (short) 0, 64 ));
		items.add(new ItemInfo("Monster Spawner",	new String[][] {{"spawn"}},										52, (short) 0, 64 ));
		items.add(new ItemInfo("Wooden Stairs",		new String[][] {{"stair","wood"}},								53, (short) 0, 64 ));
		items.add(new ItemInfo("Chest",				new String[][] {{"chest"}},										54, (short) 0, 64 ));
		items.add(new ItemInfo("Diamond Ore",		new String[][] {{"ore","diam"}},								56, (short) 0, 64 ));
		items.add(new ItemInfo("Diamond Block",		new String[][] {{"diam","bl"}},									57, (short) 0, 64 ));
		items.add(new ItemInfo("Crafting Table",	new String[][] {{"benc"},{"craft"}},							58, (short) 0, 64 ));
		items.add(new ItemInfo("Farmland",			new String[][] {{"farm"}},										60, (short) 0, 64 ));
		items.add(new ItemInfo("Furnace",			new String[][] {{"furna"}},										61, (short) 0, 64 ));
		items.add(new ItemInfo("Ladder",			new String[][] {{"ladd"}},										65, (short) 0, 64 ));
		items.add(new ItemInfo("Rails",				new String[][] {{"rail"},{"trac"}},								66, (short) 0, 64 ));
		items.add(new ItemInfo("Cobblestone Stairs",new String[][] {{"stair","cob","sto"},{"stair","cob"}},			67, (short) 0, 64 ));
		items.add(new ItemInfo("Lever",				new String[][] {{"lever"},{"switc"}},							69, (short) 0, 64 ));
		items.add(new ItemInfo("Stone Pressure Plate",new String[][]{{"pres","plat","ston"}},						70, (short) 0, 64 ));
		items.add(new ItemInfo("Wooden Pressure Plate",new String[][]{{"pres","plat","wood"}},						72, (short) 0, 64 ));
		items.add(new ItemInfo("Redstone Ore",		new String[][] {{"ore","red"}},									73, (short) 0, 64 ));
		items.add(new ItemInfo("Redstone Torch",	new String[][] {{"torc","red"},{"torc","rs"}},					76, (short) 0, 64 ));
		items.add(new ItemInfo("Stone Button",		new String[][] {{"stone","button"},{"button"}},					77, (short) 0, 64 ));
		items.add(new ItemInfo("Snow",				new String[][] {{"snow"}},										78, (short) 0, 64 ));
		items.add(new ItemInfo("Ice",				new String[][] {{"ice"}},										79, (short) 0, 64 ));
		items.add(new ItemInfo("Snow Block",		new String[][] {{"snow","blo"}},								80, (short) 0, 64 ));
		items.add(new ItemInfo("Cactus",			new String[][] {{"cact"}},										81, (short) 0, 64 ));
		items.add(new ItemInfo("Clay Block",		new String[][] {{"clay","blo"}},								82, (short) 0, 64 ));
		items.add(new ItemInfo("Jukebox",			new String[][] {{"jukeb"}},										84, (short) 0, 64 ));
		items.add(new ItemInfo("Fence",				new String[][] {{"fence"}},										85, (short) 0, 64 ));
		items.add(new ItemInfo("Pumpkin",			new String[][] {{"pump"}},										86, (short) 0, 64 ));
		items.add(new ItemInfo("Netherrack",		new String[][] {{"netherr"}},									87, (short) 0, 64 ));
		items.add(new ItemInfo("Soul Sand",			new String[][] {{"soul","sand"},{"soul"}},						88, (short) 0, 64 ));
		items.add(new ItemInfo("Glowstone Block",	new String[][] {{"glow","stone"},{"glow","block"}},				89, (short) 0, 64 ));
		items.add(new ItemInfo("Jack-O-Lantern",	new String[][] {{"jack"},{"lante"}},							91, (short) 0, 64 ));
		items.add(new ItemInfo("Locked Chest",		new String[][] {{"lock","chest"}},								95, (short) 0, 64 ));
		items.add(new ItemInfo("Trapdoor",			new String[][] {{"trap"},{"trap","door"},{"hatch"}},			96, (short) 0, 64 ));
items.add(new ItemInfo("Hidden Silverfish Stone",	new String[][] {{"silver"},{"hidden","silver"},{"silver","ston"},{"hidden","silver","ston"}},								97, (short) 0, 64 ));
items.add(new ItemInfo("Hidden Silverfish Cobblestone",new String[][]{{"silver","cob"},{"hidden","silver","cob"},{"silver","cob","ston"},{"hidden","silver","cob","ston"}},		97, (short) 1, 64 ));
items.add(new ItemInfo("Hidden Silverfish Stone Brick",new String[][]{{"silver","bric"},{"hidden","silver","bric"},{"silver","ston","bric"},{"hidden","silver","ston","bric"}},	97, (short) 2, 64 ));
		items.add(new ItemInfo("Stone Brick",		new String[][] {{"ston","bric"}},								98, (short) 0, 64 ));
		items.add(new ItemInfo("Mossy Stone Brick",	new String[][] {{"ston","bric","moss"}},						98, (short) 1, 64 ));
		items.add(new ItemInfo("Cracked Stone Brick",new String[][]{{"ston","bric","cra"}},							98, (short) 2, 64 ));
		items.add(new ItemInfo("Huge Brown Mushroom",new String[][]{{"huge","bro","mush"}},							99, (short) 0, 64 ));
		items.add(new ItemInfo("Huge Red Mushroom",	new String[][] {{"huge","red","mush"}},							100,(short) 0, 64 ));
		items.add(new ItemInfo("Iron Bars",			new String[][] {{"iron","bar"}},								101,(short) 0, 64 ));
		items.add(new ItemInfo("Glass Pane",		new String[][] {{"glas","pan"}},								102,(short) 0, 64 ));
		items.add(new ItemInfo("Melon",				new String[][] {{"melo"}},										103,(short) 0, 64 ));
		items.add(new ItemInfo("Pumpkin Stem",		new String[][] {{"pump","stem"}},								104,(short) 0, 64 ));
		items.add(new ItemInfo("Melon Stem",		new String[][] {{"melo","stem"}},								105,(short) 0, 64 ));
		items.add(new ItemInfo("Vines",				new String[][] {{"vine"}},										106,(short) 0, 64 ));
		items.add(new ItemInfo("Fence Gate",		new String[][] {{"fence","gate"},{"gate"}},						107,(short) 0, 64 ));
		items.add(new ItemInfo("Brick Stairs",		new String[][] {{"bric","stair"}},								108,(short) 0, 64 ));
		items.add(new ItemInfo("Stone Brick Stairs",new String[][] {{"ston","bric","stair"}},						109,(short) 0, 64 ));
		items.add(new ItemInfo("Mycelium",			new String[][] {{"myce"}},										110,(short) 0, 64 ));
		items.add(new ItemInfo("Lily Pad",			new String[][] {{"lily"}},										111,(short) 0, 64 ));
		items.add(new ItemInfo("Nether Brick",		new String[][] {{"bric","nether"}},								112,(short) 0, 64 ));
		items.add(new ItemInfo("Nether Brick Fence",new String[][] {{"fen","bric","nether"}},						113,(short) 0, 64 ));
		items.add(new ItemInfo("Nether Brick Stairs",new String[][]{{"stair","bric","nether"}},						114,(short) 0, 64 ));
		items.add(new ItemInfo("Enchantment Table", new String[][] {{"encha"}},										116,(short) 0, 64 ));
		items.add(new ItemInfo("End Portal Frame",	new String[][] {{"end","fra"}},									120,(short) 0, 64 ));
		items.add(new ItemInfo("End Stone", 		new String[][] {{"end","sto"}},									121,(short) 0, 64 ));
		items.add(new ItemInfo("Dragon Egg", 		new String[][] {{"drag"}},										122,(short) 0, 64 ));
		
		items.add(new ItemInfo("Iron Shovel",		new String[][] {{"shov","ir"}},									256, (short) 0, 1 ));
		items.add(new ItemInfo("Iron Pickaxe",		new String[][] {{"pick","ir"},{"pick","axe","ir"}},				257, (short) 0, 1 ));
		items.add(new ItemInfo("Iron Axe",			new String[][] {{"axe","ir"}},									258, (short) 0, 1 ));
		items.add(new ItemInfo("Flint and Steel",	new String[][] {{"flin","ste"}},								259, (short) 0, 1 ));
		items.add(new ItemInfo("Red Apple",			new String[][] {{"appl"},{"red","appl"}},						260, (short) 0, 1 ));
		items.add(new ItemInfo("Bow",				new String[][] {{"bow"}},										261, (short) 0, 1 ));
		items.add(new ItemInfo("Arrow",				new String[][] {{"arro"}},										262, (short) 0, 64 ));
		items.add(new ItemInfo("Coal",				new String[][] {{"coal"}},										263, (short) 0, 64 ));
		items.add(new ItemInfo("Charcoal",			new String[][] {{"char","coal"},{"char"}},						263, (short) 1, 64 ));
		items.add(new ItemInfo("Diamond",			new String[][] {{"diamo"}},										264, (short) 0, 64 ));
		items.add(new ItemInfo("Iron Ingot",		new String[][] {{"ingo","ir"},{"iron"}},						265, (short) 0, 64 ));
		items.add(new ItemInfo("Gold Ingot",		new String[][] {{"ingo","go"},{"gold"}},						266, (short) 0, 64 ));
		items.add(new ItemInfo("Iron Sword",		new String[][] {{"swor","ir"}},									267, (short) 0, 1 ));
		items.add(new ItemInfo("Wooden Sword",		new String[][] {{"swor","woo"}},								268, (short) 0, 1 ));
		items.add(new ItemInfo("Wooden Shovel",		new String[][] {{"shov","wo"}},									269, (short) 0, 1 ));
		items.add(new ItemInfo("Wooden Pickaxe",	new String[][] {{"pick","woo"},{"pick","axe","woo"}},			270, (short) 0, 1 ));
		items.add(new ItemInfo("Wooden Axe",		new String[][] {{"axe","woo"}},									271, (short) 0, 1 ));
		items.add(new ItemInfo("Stone Sword",		new String[][] {{"swor","sto"}},								272, (short) 0, 1 ));
		items.add(new ItemInfo("Stone Shovel",		new String[][] {{"shov","sto"}},								273, (short) 0, 1 ));
		items.add(new ItemInfo("Stone Pickaxe",		new String[][] {{"pick","sto"},{"pick","axe","sto"}},			274, (short) 0, 1 ));
		items.add(new ItemInfo("Stone Axe",			new String[][] {{"axe","sto"}},									275, (short) 0, 1 ));
		items.add(new ItemInfo("Diamond Sword",		new String[][] {{"swor","dia"}},								276, (short) 0, 1 ));
		items.add(new ItemInfo("Diamond Shovel",	new String[][] {{"shov","dia"}},								277, (short) 0, 1 ));
		items.add(new ItemInfo("Diamond Pickaxe",	new String[][] {{"pick","dia"},{"pick","axe","dia"}},			278, (short) 0, 1 ));
		items.add(new ItemInfo("Diamond Axe",		new String[][] {{"axe","dia"}},									279, (short) 0, 1 ));
		items.add(new ItemInfo("Stick",				new String[][] {{"stic"}},										280, (short) 0, 64 ));
		items.add(new ItemInfo("Bowl",				new String[][] {{"bowl","bo","wl"}},							281, (short) 0, 64 ));
		items.add(new ItemInfo("Mushroom Soup",		new String[][] {{"soup"}},										282, (short) 0, 1 ));
		items.add(new ItemInfo("Gold Sword",		new String[][] {{"swor","gol"}},								283, (short) 0, 1 ));
		items.add(new ItemInfo("Gold Shovel",		new String[][] {{"shov","gol"}},								284, (short) 0, 1 ));
		items.add(new ItemInfo("Gold Pickaxe",		new String[][] {{"pick","gol"},{"pick","axe","gol"}},			285, (short) 0, 1 ));
		items.add(new ItemInfo("Gold Axe",			new String[][] {{"axe","gol"}},									286, (short) 0, 1 ));
		items.add(new ItemInfo("String",			new String[][] {{"stri"}},										287, (short) 0, 64 ));
		items.add(new ItemInfo("Feather",			new String[][] {{"feat"}},										288, (short) 0, 64 ));
		items.add(new ItemInfo("Gunpowder",			new String[][] {{"gun"},{"sulph"}},								289, (short) 0, 64 ));
		items.add(new ItemInfo("Wooden Hoe",		new String[][] {{"hoe","wo"}},									290, (short) 0, 1 ));
		items.add(new ItemInfo("Stone Hoe",			new String[][] {{"hoe","sto"}},									291, (short) 0, 1 ));
		items.add(new ItemInfo("Iron Hoe",			new String[][] {{"hoe","iro"}},									292, (short) 0, 1 ));
		items.add(new ItemInfo("Diamond Hoe",		new String[][] {{"hoe","dia"}},									293, (short) 0, 1 ));
		items.add(new ItemInfo("Gold Hoe",			new String[][] {{"hoe","go"}},									294, (short) 0, 1 ));
		items.add(new ItemInfo("Seeds",				new String[][] {{"seed"}},										295, (short) 0, 64 ));
		items.add(new ItemInfo("Wheat",				new String[][] {{"whea"}},										296, (short) 0, 64 ));
		items.add(new ItemInfo("Bread",				new String[][] {{"brea"}},										297, (short) 0, 64 ));
		items.add(new ItemInfo("Leather Cap",		new String[][] {{"cap","lea"},{"helm","lea"}},					298, (short) 0, 1 ));
		items.add(new ItemInfo("Leather Tunic",		new String[][] {{"tun","lea"},{"ches","lea"}},					299, (short) 0, 1 ));
		items.add(new ItemInfo("Leather Pants",		new String[][] {{"pan","lea"},{"leg","lea"}},					300, (short) 0, 1 ));
		items.add(new ItemInfo("Leather Boots",		new String[][] {{"boo","lea"}},									301, (short) 0, 1 ));
		items.add(new ItemInfo("Chain Helmet",		new String[][] {{"cap","cha"},{"helm","cha"}},					302, (short) 0, 1 ));
		items.add(new ItemInfo("Chain Chestplate",	new String[][] {{"tun","cha"},{"ches","cha"}},					303, (short) 0, 1 ));
		items.add(new ItemInfo("Chain Leggings",	new String[][] {{"pan","cha"},{"leg","cha"}},					304, (short) 0, 1 ));
		items.add(new ItemInfo("Chain Boots",		new String[][] {{"boo","cha"}},									305, (short) 0, 1 ));
		items.add(new ItemInfo("Iron Helmet",		new String[][] {{"cap","ir"},{"helm","ir"}},					306, (short) 0, 1 ));
		items.add(new ItemInfo("Iron Chestplate",	new String[][] {{"tun","ir"},{"ches","ir"}},					307, (short) 0, 1 ));
		items.add(new ItemInfo("Iron Leggings",		new String[][] {{"pan","ir"},{"leg","ir"}},						308, (short) 0, 1 ));
		items.add(new ItemInfo("Iron Boots",		new String[][] {{"boo","ir"}},									309, (short) 0, 1 ));
		items.add(new ItemInfo("Diamond Helmet",	new String[][] {{"cap","dia"},{"helm","dia"}},					310, (short) 0, 1 ));
		items.add(new ItemInfo("Diamond Chestplate",new String[][] {{"tun","dia"},{"ches","dia"}},					311, (short) 0, 1 ));
		items.add(new ItemInfo("Diamond Leggings",	new String[][] {{"pan","dia"},{"leg","dia"}},					312, (short) 0, 1 ));
		items.add(new ItemInfo("Diamond Boots",		new String[][] {{"boo","dia"}},									313, (short) 0, 1 ));
		items.add(new ItemInfo("Gold Helmet",		new String[][] {{"cap","go"},{"helm","go"}},					314, (short) 0, 1 ));
		items.add(new ItemInfo("Gold Chestplate",	new String[][] {{"tun","go"},{"ches","go"}},					315, (short) 0, 1 ));
		items.add(new ItemInfo("Gold Leggings",		new String[][] {{"pan","go"},{"leg","go"}},						316, (short) 0, 1 ));
		items.add(new ItemInfo("Gold Boots",		new String[][] {{"boo","go"}},									317, (short) 0, 1 ));
		items.add(new ItemInfo("Flint",				new String[][] {{"flin"}},										318, (short) 0, 64 ));
		items.add(new ItemInfo("Raw Porkchop",		new String[][] {{"raw","pork"}},								319, (short) 0, 1 ));
		items.add(new ItemInfo("Cooked Porkchop",	new String[][] {{"cook","pork"}},								320, (short) 0, 1 ));
		items.add(new ItemInfo("Paintings",			new String[][] {{"painting"}},									321, (short) 0, 64 ));
		items.add(new ItemInfo("Golden Apple",		new String[][] {{"appl","go"}},									322, (short) 0, 64 ));
		items.add(new ItemInfo("Sign",				new String[][] {{"sign"}},										323, (short) 0, 1 ));
		items.add(new ItemInfo("Wooden Door",		new String[][] {{"door","wood"},{"door"}},						324, (short) 0, 1 ));
		items.add(new ItemInfo("Bucket",			new String[][] {{"buck"}},										325, (short) 0, 1 ));
		items.add(new ItemInfo("Water Bucket",		new String[][] {{"water","buck"}},								326, (short) 0, 1 ));
		items.add(new ItemInfo("Lava Bucket",		new String[][] {{"lava","buck"}},								327, (short) 0, 1 ));
		items.add(new ItemInfo("Minecart",			new String[][] {{"cart"}},										328, (short) 0, 1 ));
		items.add(new ItemInfo("Saddle",			new String[][] {{"sadd"}},										329, (short) 0, 1 ));
		items.add(new ItemInfo("Iron Door",			new String[][] {{"door","iron"}},								330, (short) 0, 1 ));
		items.add(new ItemInfo("Redstone Dust",		new String[][] {{"red","ston"}},								331, (short) 0, 64));
		items.add(new ItemInfo("Snowball",			new String[][] {{"snow","ball"}},								332, (short) 0, 16 ));
		items.add(new ItemInfo("Boat",				new String[][] {{"boat"}},										333, (short) 0, 1 ));
		items.add(new ItemInfo("Leather",			new String[][] {{"lea"}},										334, (short) 0, 64 ));
		items.add(new ItemInfo("Milk Bucket",		new String[][] {{"milk"}},										335, (short) 0, 1 ));
		items.add(new ItemInfo("Clay Brick",		new String[][] {{"bric","cla"}},								336, (short) 0, 64 ));
		items.add(new ItemInfo("Clay",				new String[][] {{"cla"}},										337, (short) 0, 64 ));
		items.add(new ItemInfo("Sugar Cane",		new String[][] {{"cane"}},										338, (short) 0, 64 ));
		items.add(new ItemInfo("Paper",				new String[][] {{"pape"}},										339, (short) 0, 64 ));
		items.add(new ItemInfo("Book",				new String[][] {{"book"}},										340, (short) 0, 64 ));
		items.add(new ItemInfo("Slimeball",			new String[][] {{"slime"}},										341, (short) 0, 64 ));
		items.add(new ItemInfo("Minecart with Chest",new String[][]{{"cart","sto"},{"cart","che"}},					342, (short) 0, 1 ));
		items.add(new ItemInfo("Minecart with Furnace",new String[][]{{"cart","pow"},{"cart","furn"}},				343, (short) 0, 1 ));
		items.add(new ItemInfo("Egg",				new String[][] {{"egg"}},										344, (short) 0, 16 ));
		items.add(new ItemInfo("Compass",			new String[][] {{"comp"}},										345, (short) 0, 64 ));
		items.add(new ItemInfo("Fishing Rod",		new String[][] {{"fish","rod"},{"fish","pole"}},				346, (short) 0, 1 ));
		items.add(new ItemInfo("Clock",				new String[][] {{"cloc"},{"watc"}},								347, (short) 0, 64 ));
		items.add(new ItemInfo("Glowstone Dust",	new String[][] {{"glow","sto","dus"},{"glow","dus"}},			348, (short) 0, 64 ));
		items.add(new ItemInfo("Raw Fish",			new String[][] {{"fish"},{"raw","fish"}},						349, (short) 0, 64 ));
		items.add(new ItemInfo("Cooked Fish",		new String[][] {{"fish","coo"}},								350, (short) 0, 64 ));
		items.add(new ItemInfo("Ink Sac",			new String[][] {{"dye","bla"},	{"ink"}},						351, (short) 0, 64 ));
		items.add(new ItemInfo("Rose Red",			new String[][] {{"dye","red"},	{"rose","red"}},				351, (short) 1, 64 ));
		items.add(new ItemInfo("Cactus Green",		new String[][] {{"dye","gree"},	{"cact","gree"}},				351, (short) 2, 64 ));
		items.add(new ItemInfo("Cocoa Beans",		new String[][] {{"dye","bro"},	{"bean"},{"choco"},{"cocoa"}},	351, (short) 3, 64 ));
		items.add(new ItemInfo("Lapis Lazuli",		new String[][] {{"dye","blu"},	{"lapis"}},						351, (short) 4, 64 ));
		items.add(new ItemInfo("Purple Dye",		new String[][] {{"dye","pur"}},									351, (short) 5, 64 ));
		items.add(new ItemInfo("Cyan Dye",			new String[][] {{"dye","cya"}},									351, (short) 6, 64 ));
		items.add(new ItemInfo("Light Gray Dye",	new String[][] {{"dye","lig","gra"},{"dye","lig","grey"}},		351, (short) 7, 64 ));
		items.add(new ItemInfo("Gray Dye",			new String[][] {{"dye","gra"},{"dye","grey"}},					351, (short) 8, 64 ));
		items.add(new ItemInfo("Pink Dye",			new String[][] {{"dye","pin"}},									351, (short) 9, 64 ));
		items.add(new ItemInfo("Lime Dye",			new String[][] {{"dye","lim"},{"dye","lig","gree"}},			351, (short) 10, 64 ));
		items.add(new ItemInfo("Dandelion Yellow",	new String[][] {{"dye","yel"},	{"dand","yel"}},				351, (short) 11, 64 ));
		items.add(new ItemInfo("Light Blue Dye",	new String[][] {{"dye","lig","blu"}},							351, (short) 12, 64 ));
		items.add(new ItemInfo("Magenta Dye",		new String[][] {{"dye","mag"}},									351, (short) 13, 64 ));
		items.add(new ItemInfo("Orange Dye",		new String[][] {{"dye","ora"}},									351, (short) 14, 64 ));
		items.add(new ItemInfo("Bone Meal",			new String[][] {{"dye","whi"},	{"bonem"},{"bone","me"}},		351, (short) 15, 64 ));
		items.add(new ItemInfo("Bone",				new String[][] {{"bone"}},										352, (short) 0, 64 ));
		items.add(new ItemInfo("Sugar",				new String[][] {{"suga"}},										353, (short) 0, 64 ));
		items.add(new ItemInfo("Cake",				new String[][] {{"cake"}},										354, (short) 0, 1 ));
		items.add(new ItemInfo("Bed",				new String[][] {{"bed"}},										355, (short) 0, 1 ));
		items.add(new ItemInfo("Redstone Repeater",	new String[][] {{"rep"},{"rep","red"},{"rep","ston","red"}},	356, (short) 0, 64 ));
		items.add(new ItemInfo("Cookie",			new String[][] {{"cooki"}},										357, (short) 0, 64 ));
		items.add(new ItemInfo("Map",				new String[][] {{"map"}},										358, (short) 0, 1 ));
		items.add(new ItemInfo("Shears",			new String[][] {{"shear"}},										359, (short) 0, 1 ));
		items.add(new ItemInfo("Melon Slice",		new String[][] {{"melo","sli"}},								360, (short) 0, 64 ));
		items.add(new ItemInfo("Pumpkin Seeds",		new String[][] {{"pump","seed"}},								361, (short) 0, 64 ));
		items.add(new ItemInfo("Melon Seeds",		new String[][] {{"melo","seed"}},								362, (short) 0, 64 ));
		items.add(new ItemInfo("Raw Beef",			new String[][] {{"beef","raw"},{"beef"}},						363, (short) 0, 64 ));
		items.add(new ItemInfo("Steak",				new String[][] {{"beef","cook"},{"steak"}},						364, (short) 0, 64 ));
		items.add(new ItemInfo("Raw Chicken",		new String[][] {{"chicken","raw"},{"chicken"}},					365, (short) 0, 64 ));
		items.add(new ItemInfo("Cooked Chicken",	new String[][] {{"chicken","cook"}},							366, (short) 0, 64 ));
		items.add(new ItemInfo("Rotten Flesh",		new String[][] {{"rot"},{"flesh"}},								367, (short) 0, 64 ));
		items.add(new ItemInfo("Ender Pearl",		new String[][] {{"pearl"},{"ender"},{"ender","pearl"}},			368, (short) 0, 64 ));
		items.add(new ItemInfo("Blaze Rod",			new String[][] {{"blaz"},{"blaz","rod"}},						369, (short) 0, 64 ));
		items.add(new ItemInfo("Ghast Tear",		new String[][] {{"ghast"},{"tear"}},							370, (short) 0, 64 ));
		items.add(new ItemInfo("Gold Nugget",		new String[][] {{"nug"},{"gold","nug"}},						371, (short) 0, 64 ));
		items.add(new ItemInfo("Nether Wart",		new String[][] {{"wart"},{"wart","nether"}},					372, (short) 0, 64 ));
items.add(new ItemInfo("Water Bottle",					new String[][] {{"water","bot"}},							373, (short) 0, 64 ));
items.add(new ItemInfo("Bootleg Potion of Regeneration",new String[][] {{"pot","boot","regen"}},					373, (short) 1, 64 ));
items.add(new ItemInfo("Bootleg Potion of Swiftness",	new String[][] {{"pot","boot","swif"}},						373, (short) 2, 64 ));
items.add(new ItemInfo("Bootleg Potion of Fire Resistance",new String[][] {{"pot","boot","fire","res"}},			373, (short) 3, 64 ));
items.add(new ItemInfo("Bootleg Potion of Poison",		new String[][] {{"pot","boot","poi"}},						373, (short) 4, 64 ));
items.add(new ItemInfo("Bootleg Potion of Healing",		new String[][] {{"pot","boot","heal"}},						373, (short) 5, 64 ));
items.add(new ItemInfo("Bootleg Clear Potion",			new String[][] {{"pot","boot","clear"}},					373, (short) 6, 64 ));
items.add(new ItemInfo("Bootleg Clear Potion 2",		new String[][] {{"pot","boot","clear","2"}},				373, (short) 7, 64 ));
items.add(new ItemInfo("Bootleg Potion of Weakness",	new String[][] {{"pot","boot","weak"}},						373, (short) 8, 64 ));
items.add(new ItemInfo("Bootleg Potion of Strength",	new String[][] {{"pot","boot","stre"}},						373, (short) 9, 64 ));
items.add(new ItemInfo("Bootleg Potion of Slowness",	new String[][] {{"pot","boot","slow"}},						373, (short) 10,64 ));
items.add(new ItemInfo("Bootleg Diffuse Potion",		new String[][] {{"pot","boot","diff"}},						373, (short) 11,64 ));
items.add(new ItemInfo("Bootleg Potion of Harming",		new String[][] {{"pot","boot","har"}},						373, (short) 12,64 ));
items.add(new ItemInfo("Bootleg Artless Potion",		new String[][] {{"pot","boot","artl"}},						373, (short) 13,64 ));
items.add(new ItemInfo("Bootleg Thin Potion",			new String[][] {{"pot","boot","thin"}},						373, (short) 14,64 ));
items.add(new ItemInfo("Bootleg Thin Potion 2",			new String[][] {{"pot","boot","thin","2"}},					373, (short) 15,64 ));
items.add(new ItemInfo("Awkward Potion",				new String[][] {{"pot","awk"}},								373, (short) 16,64 ));
items.add(new ItemInfo("Thick Potion",					new String[][] {{"pot","thi"}},								373, (short) 32,64 ));
items.add(new ItemInfo("Mundane Potion (extended)",		new String[][] {{"pot","mun","ext"}},						373, (short) 64,64 ));
items.add(new ItemInfo("Mundane Potion",				new String[][] {{"pot","mun"}},								373, (short)8192,64));
items.add(new ItemInfo("Potion of Regeneration",		new String[][] {{"pot","regen"}},							373, (short)8193,64));
items.add(new ItemInfo("Potion of Regeneration (extended)",new String[][]{{"pot","regen","ext"}},					373, (short)8257,64));
items.add(new ItemInfo("Potion of Regeneration 2",		new String[][] {{"pot","regen","2"}},						373, (short)8225,64));
items.add(new ItemInfo("Potion of Swiftness",			new String[][] {{"pot","swif"}},							373, (short)8194,64));
items.add(new ItemInfo("Potion of Swiftness (extended)",new String[][] {{"pot","swif","ext"}},						373, (short)8258,64));
items.add(new ItemInfo("Potion of Swiftness 2",			new String[][] {{"pot","swif","2"}},						373, (short)8226,64));
items.add(new ItemInfo("Potion of Fire Resistance",		new String[][] {{"pot","fire","res"}},						373, (short)8195,64));
items.add(new ItemInfo("Potion of Fire Resistance (extended)",new String[][]{{"pot","fire","res","ext"}},			373, (short)8259,64));
items.add(new ItemInfo("Potion of Healing",				new String[][] {{"pot","heal"}},							373, (short)8197,64));
items.add(new ItemInfo("Potion of Healing 2",			new String[][] {{"pot","heal","2"}},						373, (short)8229,64));
items.add(new ItemInfo("Potion of Strength",			new String[][] {{"pot","stre"}},							373, (short)8201,64));
items.add(new ItemInfo("Potion of Strength (extended)",	new String[][] {{"pot","stre","ext"}},						373, (short)8265,64));
items.add(new ItemInfo("Potion of Strength 2",			new String[][] {{"pot","stre","2"}},						373, (short)8233,64));
items.add(new ItemInfo("Potion of Poison",				new String[][] {{"pot","poi"}},								373, (short)8196,64));
items.add(new ItemInfo("Potion of Poison (extended)",	new String[][] {{"pot","poi","ext"}},						373, (short)8260,64));
items.add(new ItemInfo("Potion of Poison 2",			new String[][] {{"pot","poi","2"}},							373, (short)8228,64));
items.add(new ItemInfo("Potion of Weakness",			new String[][] {{"pot","weak"}},							373, (short)8200,64));
items.add(new ItemInfo("Potion of Weakness (extended)",	new String[][] {{"pot","weak","ext"}},						373, (short)8264,64));
items.add(new ItemInfo("Potion of Slowness",			new String[][] {{"pot","slow"}},							373, (short)8202,64));
items.add(new ItemInfo("Potion of Slowness (extended)",	new String[][] {{"pot","slow","ext"}},						373, (short)8266,64));
items.add(new ItemInfo("Potion of Harming",				new String[][] {{"pot","har"}},								373, (short)8204,64));
items.add(new ItemInfo("Potion of Harming 2",			new String[][] {{"pot","har","2"}},							373, (short)8236,64));
items.add(new ItemInfo("Splash Mundane Potion",			new String[][] {{"pot","spl","mun"}},						373,(short)16384,64));
items.add(new ItemInfo("Splash Potion of Regeneration",	new String[][] {{"pot","spl","regen"}},						373,(short)16385,64));
items.add(new ItemInfo("Splash Potion of Regeneration (extended)",new String[][]{{"pot","spl","regen","ext"}},		373,(short)16449,64));
items.add(new ItemInfo("Splash Potion of Regeneration 2",new String[][]{{"pot","spl","reg","2"}},					373,(short)16417,64));
items.add(new ItemInfo("Splash Potion of Swiftness",	new String[][] {{"pot","spl","swif"}},						373,(short)16386,64));
items.add(new ItemInfo("Splash Potion of Swiftness (extended)",new String[][]{{"pot","spl","swif","ext"}},			373,(short)16450,64));
items.add(new ItemInfo("Splash Potion of Swiftness 2",	new String[][] {{"pot","spl","swif","2"}},					373,(short)16418,64));
items.add(new ItemInfo("Splash Potion of Fire Resistance",new String[][]{{"pot","spl","fire"}},						373,(short)16387,64));
items.add(new ItemInfo("Splash Potion of Fire Resistance (extended)",new String[][]{{"pot","spl","fire","ext"}},	373,(short)16451,64));
items.add(new ItemInfo("Splash Potion of Healing",		new String[][] {{"pot","spl","heal"}},						373,(short)16389,64));
items.add(new ItemInfo("Splash Potion of Healing 2",	new String[][] {{"pot","spl","heal","2"}},					373,(short)16421,64));
items.add(new ItemInfo("Splash Potion of Strength",		new String[][] {{"pot","spl","stre"}},						373,(short)16393,64));
items.add(new ItemInfo("Splash Potion of Strength (extended)",new String[][]{{"pot","spl","stre","ext"}},			373,(short)16457,64));
items.add(new ItemInfo("Splash Potion of Strength 2",	new String[][] {{"pot","spl","stre","2"}},					373,(short)16425,64));
items.add(new ItemInfo("Splash Potion of Poison",		new String[][] {{"pot","spl","poi"}},						373,(short)16388,64));
items.add(new ItemInfo("Splash Potion of Poison (extended)",new String[][]{{"pot","spl","poi","ext"}},				373,(short)16452,64));
items.add(new ItemInfo("Splash Potion of Poison 2",		new String[][] {{"pot","spl","poi","2"}},					373,(short)16420,64));
items.add(new ItemInfo("Splash Potion of Weakness",		new String[][] {{"pot","spl","weak"}},						373,(short)16392,64));
items.add(new ItemInfo("Splash Potion of Weakness (extended)",new String[][]{{"pot","spl","weak","ext"}},			373,(short)16456,64));
items.add(new ItemInfo("Splash Potion of Slowness",		new String[][] {{"pot","spl","slow"}},						373,(short)16394,64));
items.add(new ItemInfo("Splash Potion of Slowness (extended)",new String[][]{{"pot","spl","slow","ext"}},			373,(short)16458,64));
items.add(new ItemInfo("Splash Potion of Harming",		new String[][] {{"pot","spl","har"}},						373,(short)16396,64));
items.add(new ItemInfo("Splash Potion of Harming 2",	new String[][] {{"pot","spl","har","2"}},					373,(short)16428,64));
		items.add(new ItemInfo("Glass Bottle",		new String[][] {{"glas","bottl"}},								374, (short) 0, 64 ));
		items.add(new ItemInfo("Spider Eye",		new String[][] {{"spid"}},										375, (short) 0, 64 ));
		items.add(new ItemInfo("Fermented Spider Eye",new String[][]{{"ferm"},{"ferm","eye"},{"ferm","spid"}},		376, (short) 0, 64 ));
		items.add(new ItemInfo("Blaze Powder",		new String[][] {{"blaz","pow"}},								377, (short) 0, 64 ));
		items.add(new ItemInfo("Magma Cream",		new String[][] {{"mag","cream"}},								378, (short) 0, 64 ));
		items.add(new ItemInfo("Brewing Stand",		new String[][] {{"brew"}},										379, (short) 0, 64 ));
		items.add(new ItemInfo("Cauldron",			new String[][] {{"caul"}},										380, (short) 0, 64 ));
		items.add(new ItemInfo("Eye of Ender",		new String[][] {{"eye","end"}},									381, (short) 0, 64 ));
		items.add(new ItemInfo("Glistering Melon",	new String[][] {{"glist"},{"glist","melo"}},					382, (short) 0, 64 ));
		items.add(new ItemInfo("Spawner Egg",		new String[][] {{"spawn","egg"}},								383, (short) 0, 64 ));
		
		
		items.add(new ItemInfo("13 Disc",			new String[][] {{"dis","13"},{"dis","gol"}},					2256,(short) 0, 64 ));
		items.add(new ItemInfo("Cat Disc",			new String[][] {{"dis","cat"},{"dis","gre"}},					2257,(short) 0, 64 ));
		items.add(new ItemInfo("Blocks Disc",		new String[][] {{"dis","blo"},{"dis","ora"}},					2258,(short) 0, 64 ));
		items.add(new ItemInfo("Chirp Disc",		new String[][] {{"dis","chi"},{"dis","red"}},					2259,(short) 0, 64 ));
		items.add(new ItemInfo("Far Disc",			new String[][] {{"dis","far"},{"dis","lim"}},					2260,(short) 0, 64 ));
		items.add(new ItemInfo("Mall Disc",			new String[][] {{"dis","mal"},{"dis","pur"}},					2261,(short) 0, 64 ));
		items.add(new ItemInfo("Mellohi Disc",		new String[][] {{"dis","mel"},{"dis","vio"}},					2262,(short) 0, 64 ));
		items.add(new ItemInfo("Stal Disc",			new String[][] {{"dis","sta"},{"dis","bla"}},					2263,(short) 0, 64 ));
		items.add(new ItemInfo("Strad Disc",		new String[][] {{"dis","str"},{"dis","whi"}},					2264,(short) 0, 64 ));
		items.add(new ItemInfo("Ward Disc",			new String[][] {{"dis","war"},{"dis","sea","gre"}},				2265,(short) 0, 64 ));
		items.add(new ItemInfo("11 Disc",			new String[][] {{"dis","11"},{"dis","bro"}},					2266,(short) 0, 64 ));

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
		}
		return joined.toString();
	}

	/**
	 * Get an (@link ItemInfo} by the ItemId. Intended for durable items as it assumes
	 * damage=0. 
	 * @param type
	 * the type or "item id"
	 * @return the ItemInfo for this item
	 */
	public static ItemInfo itemById(int type)
	{
		return itemById(type, (short)0);
	}

	/**
	 * Get an (@link ItemInfo} by the ItemId. Suitable for subtyped items (dyes etc) as
	 * it includes the subtype
	 * @param type
	 * the type or "item id"
	 * @param subType
	 * the sub type or "damage value"
	 * @return the ItemInfo for this item
	 */
	public static ItemInfo itemById(int type, short subType)
	{
		for(ItemInfo item: items)
		{
			if(item.typeId == type && item.subTypeId == subType){ return item; }
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
		}else if(searchString.matches("\\d+"))
		{
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
		}else
		{
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
						//lets us searech everything to find the BEST match
					}
				}
			}
		}

		return matchedItem;
	}
}
