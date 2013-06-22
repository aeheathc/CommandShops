package com.aehdev.commandshops;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Logger;

import com.aehdev.lib.multiDB.Database;

import cuboidLocale.QuadTree;

/**
 * The Class ShopData.
 */
public class ShopData
{
	/** The shops. */
	private HashMap<UUID,Shop> shops = new HashMap<UUID,Shop>();

	// Logging
	/** The log. */
	private final Logger log = Logger.getLogger("Minecraft");

	/**
	 * Adds the shop.
	 * @param shop
	 * the shop
	 */
	public void addShop(Shop shop)
	{
		if(Config.DEBUG)
		{
			log.info(String.format((Locale)null,"[%s] Adding %s", CommandShops.pdfFile.getName(),
					shop.toString()));
		}
		shops.put(shop.getUuid(), shop);
	}

	/**
	 * Load shops.
	 * @param shopsDir
	 * the shops dir
	 */
	public void loadShops(File shopsDir)
	{
		if(Config.DEBUG)
		{
			log.info(String.format((Locale)null,"[%s] %s.%s", CommandShops.pdfFile.getName(),
					"ShopData", "loadShops(File shopsDir)"));
		}
		Database db = CommandShops.db;

		CommandShops.setCuboidTree(new QuadTree());

		File[] shopsList = shopsDir.listFiles();
		for(File file: shopsList)
		{
			String loaderror = null;

			if(Config.DEBUG)
			{
				log.info(String.format((Locale)null,"[%s] Loading Shop file \"%s\".",
						CommandShops.pdfFile.getName(), file.toString()));
			}
			Shop shop = null;

			// Determine if filename is a UUID or not
			if(file.getName().matches("^(\\{{0,1}([0-9a-fA-F]){8}-([0-9a-fA-F]){4}-([0-9a-fA-F]){4}-([0-9a-fA-F]){4}-([0-9a-fA-F]){12}\\}{0,1})\\.shop$"))
			{
				try
				{
					shop = loadShop(file);
				}catch(Exception e){
					// log error
					log.info(String.format((Locale)null,
							"[%s] Error loading Shop file \"%s\", ignored.",
							CommandShops.pdfFile.getName(), file.toString()));
				}
			}else{
				// Convert old format & delete the file...immediately save using
				// the new format (will generate a new UUID for this shop)
				shop = convertShopOldFormat(file);
			}

			// Check if not null, and add to world
			if(shop != null)
			{
				//Insert the shop data from LS3 format into the database
				ShopLocation sla = shop.getLocationA(), slb=shop.getLocationB();
				long x,y,z,x2,y2,z2;
				if(sla.getX()<=slb.getX()){ x=sla.getX();x2=slb.getX(); }else{ x=slb.getX();x2=sla.getX(); }
				if(sla.getY()<=slb.getY()){ y=sla.getY();y2=slb.getY(); }else{ y=slb.getY();y2=sla.getY(); }
				if(sla.getZ()<=slb.getZ()){ z=sla.getZ();z2=slb.getZ(); }else{ z=slb.getZ();z2=sla.getZ(); }
				
				String shopquery = String.format((Locale)null,"INSERT INTO `shops`"
				+ "(`name`,						`owner`,					`creator`,						`x`,`y`,`z`,`x2`,`y2`,`z2`,`world`,						`minbalance`,			`unlimitedMoney`,				`unlimitedStock`,				`notify`,						`service_repair`,	`service_disenchant`) VALUES"
				+ "('%s',						'%s',						'%s',							%d, %d, %d, %d,  %d,  %d,  '%s',						%f,						%d,								%d,								%d,								1,					1)"
				, 	db.escape(shop.getName()),	db.escape(shop.getOwner()),	db.escape(shop.getCreator()),	x,  y,  z,  x2,  y2,  z2,  db.escape(shop.getWorld()),	shop.getMinBalance(),	(shop.isUnlimitedMoney()?1:0),	(shop.isUnlimitedStock()?1:0),	(shop.getNotification()?1:0));
				
				try{
					ResultSet findExisting = CommandShops.db.query("SELECT id FROM shops WHERE `name`='"
							+ db.escape(shop.getName()) + "' AND `creator`='" + db.escape(shop.getCreator()) + "' LIMIT 1");
					if(!findExisting.next())
					{
						db.query(shopquery);
					
						ResultSet resIns = db.query("SELECT MAX(id) FROM shops WHERE `name`='"+db.escape(shop.getName())+"'");
						resIns.next();
						long insId = resIns.getLong(1);
						resIns.close();
						
						for(InventoryItem item : shop.getItems())
						{
							ItemInfo ii = item.getInfo();
							String itemquery = String.format((Locale)null,"INSERT INTO `shop_items`"
							+ "(`shop`,	`itemid`,	`itemdamage`,	`stock`,		`maxstock`,			`buy`,				`sell`) VALUES"
							+ "(%d,		%d,			%d,				%d,				%d,					%f,					%f)"
							,   insId,	ii.typeId,	ii.subTypeId,	item.getStock(),item.getMaxStock(),	item.getSellPrice(),item.getBuyPrice());
							CommandShops.db.query(itemquery);
						}
						for(String man : shop.getManagers())
						{
							CommandShops.db.query("INSERT INTO managers(`shop`,`manager`) VALUES(" + insId + ",'" + db.escape(man) + "')");
						}
						
						if(Config.DEBUG)
						{
							log.info(String.format((Locale)null,"[%s] Loaded %s",
									CommandShops.pdfFile.getName(), shop.toString()));
						}
					}else{
						if(Config.DEBUG)
						{
							log.info(String.format((Locale)null,"[%s] Not loading shop already in database: %s",
									CommandShops.pdfFile.getName(), shop.toString()));
						}
					}
					findExisting.close();
				}catch(Exception e){
					loaderror = e.toString();
				}

			}else{
				loaderror = "File appears broken.";
			}
			if(loaderror != null)
			{
				log.warning(String.format((Locale)null,
						"[%s] Failed to load Shop file \"%s\": %s",
						CommandShops.pdfFile.getName(), file.getName(), loaderror));
			}
		}

	}

	/**
	 * Convert shop old format.
	 * @param file
	 * the file
	 * @return the shop
	 */
	public Shop convertShopOldFormat(File file)
	{
		if(Config.DEBUG)
		{
			log.info(String.format((Locale)null,"[%s] %s.%s", CommandShops.pdfFile.getName(),
					"ShopData", "loadShopOldFormat(File file)"));
		}

		try
		{
			// Create new empty shop (this format has no UUID, so generate one)
			Shop shop = new Shop(UUID.randomUUID());

			// Retrieve Shop Name (from filename)
			shop.setName(file.getName().split("\\.")[0]);

			// Open file & iterate over lines
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			while(line != null)
			{
				if(Config.DEBUG)
				{
					log.info(String.format((Locale)null,"[%s] %s", CommandShops.pdfFile.getName(),
							line));
				}

				// Skip comment lines / metadata
				if(line.startsWith("#"))
				{
					line = br.readLine();
					continue;
				}

				// Data is separated by =
				String[] cols = line.split("=");

				// Check if there are enough columns (needs key and value)
				if(cols.length < 2)
				{
					line = br.readLine();
					continue;
				}

				if(cols[0].equalsIgnoreCase("world"))
				{ // World
					shop.setWorld(cols[1]);
				}else if(cols[0].equalsIgnoreCase("owner")){ // Owner
					shop.setOwner(cols[1]);
				}else if(cols[0].equalsIgnoreCase("managers")){ // Managers
					String[] managers = cols[1].split(",");
					shop.setManagers(managers);
				}else if(cols[0].equalsIgnoreCase("creator")){ // Creator
					shop.setCreator(cols[1]);
				}else if(cols[0].equalsIgnoreCase("position1")){ // Position
					// A
					String[] xyzStr = cols[1].split(",");
					try
					{
						long x = Long.parseLong(xyzStr[0].trim());
						long y = Long.parseLong(xyzStr[1].trim());
						long z = Long.parseLong(xyzStr[2].trim());

						ShopLocation loc = new ShopLocation(x, y, z);
						shop.setLocationA(loc);
					}catch(NumberFormatException e){
						if(isolateBrokenShopFile(file))
						{
							log.warning(String
									.format("[%s] Shop File \"%s\" has bad Location Data, Moving to \"plugins/CommandShops/broken-shops/\"",
											CommandShops.pdfFile.getName(),
											file.toString()));
						}else{
							log.warning(String
									.format("[%s] Shop File \"%s\" has bad Location Data, Error moving to \"plugins/CommandShops/broken-shops/\"",
											CommandShops.pdfFile.getName(),
											file.toString()));
						}
						return null;
					}
				}else if(cols[0].equalsIgnoreCase("position2")){
					// Position
					// B
					String[] xyzStr = cols[1].split(",");
					try
					{
						long x = Long.parseLong(xyzStr[0].trim());
						long y = Long.parseLong(xyzStr[1].trim());
						long z = Long.parseLong(xyzStr[2].trim());

						ShopLocation loc = new ShopLocation(x, y, z);
						shop.setLocationB(loc);
					}catch(NumberFormatException e){
						if(isolateBrokenShopFile(file))
						{
							log.warning(String
									.format("[%s] Shop File \"%s\" has bad Location Data, Moving to \"plugins/CommandShops/broken-shops/\"",
											CommandShops.pdfFile.getName(),
											file.toString()));
						}else{
							log.warning(String
									.format("[%s] Shop File \"%s\" has bad Location Data, Error moving to \"plugins/CommandShops/broken-shops/\"",
											CommandShops.pdfFile.getName(),
											file.toString()));
						}
						return null;
					}
				}else if(cols[0].equalsIgnoreCase("unlimited-money")){
					// Unlimited
					// Money
					shop.setUnlimitedMoney(Boolean.parseBoolean(cols[1]));
				}else if(cols[0].equalsIgnoreCase("unlimited-stock")){
					// Unlimited
					// Stock
					shop.setUnlimitedStock(Boolean.parseBoolean(cols[1]));
				}else if(cols[0].matches("\\d+:\\d+")){
					// Items
					String[] itemInfo = cols[0].split(":");
					if(itemInfo.length < 2)
					{
						if(isolateBrokenShopFile(file))
						{
							log.warning(String
									.format("[%s] Shop File \"%s\" has bad Item Data, Moving to \"plugins/CommandShops/broken-shops/\"",
											CommandShops.pdfFile.getName(),
											file.toString()));
						}else{
							log.warning(String
									.format("[%s] Shop File \"%s\" has bad Item Data, Error moving to \"plugins/CommandShops/broken-shops/\"",
											CommandShops.pdfFile.getName(),
											file.toString()));
						}
						return null;
					}
					int itemId = Integer.parseInt(itemInfo[0]);
					short damageMod = Short.parseShort(itemInfo[1]);

					String[] dataCols = cols[1].split(",");
					if(dataCols.length < 3)
					{
						if(isolateBrokenShopFile(file))
						{
							log.warning(String
									.format("[%s] Shop File \"%s\" has bad Item Data, Moving to \"plugins/CommandShops/broken-shops/\"",
											CommandShops.pdfFile.getName(),
											file.toString()));
						}else{
							log.warning(String
									.format("[%s] Shop File \"%s\" has bad Item Data, Error moving to \"plugins/CommandShops/broken-shops/\"",
											CommandShops.pdfFile.getName(),
											file.toString()));
						}
						return null;
					}

					String[] buyInfo = dataCols[0].split(":");
					if(buyInfo.length < 2)
					{
						if(isolateBrokenShopFile(file))
						{
							log.warning(String
									.format("[%s] Shop File \"%s\" has bad Item Data, Moving to \"plugins/CommandShops/broken-shops/\"",
											CommandShops.pdfFile.getName(),
											file.toString()));
						}else{
							log.warning(String
									.format("[%s] Shop File \"%s\" has bad Item Data, Error moving to \"plugins/CommandShops/broken-shops/\"",
											CommandShops.pdfFile.getName(),
											file.toString()));
						}
						return null;
					}
					int buyPrice = Integer.parseInt(buyInfo[0]);
					int buySize = Integer.parseInt(buyInfo[1]);

					String[] sellInfo = dataCols[1].split(":");
					if(sellInfo.length < 2)
					{
						if(isolateBrokenShopFile(file))
						{
							log.warning(String
									.format("[%s] Shop File \"%s\" has bad Item Data, Moving to \"plugins/CommandShops/broken-shops/\"",
											CommandShops.pdfFile.getName(),
											file.toString()));
						}else{
							log.warning(String
									.format("[%s] Shop File \"%s\" has bad Item Data, Error moving to \"plugins/CommandShops/broken-shops/\"",
											CommandShops.pdfFile.getName(),
											file.toString()));
						}
						return null;
					}
					int sellPrice = Integer.parseInt(sellInfo[0]);
					int sellSize = Integer.parseInt(sellInfo[1]);

					String[] stockInfo = dataCols[2].split(":");
					if(stockInfo.length < 2)
					{
						if(isolateBrokenShopFile(file))
						{
							log.warning(String
									.format("[%s] Shop File \"%s\" has bad Item Data, Moving to \"plugins/CommandShops/broken-shops/\"",
											CommandShops.pdfFile.getName(),
											file.toString()));
						}else{
							log.warning(String
									.format("[%s] Shop File \"%s\" has bad Item Data, Error moving to \"plugins/CommandShops/broken-shops/\"",
											CommandShops.pdfFile.getName(),
											file.toString()));
						}
						return null;
					}
					int stock = Integer.parseInt(stockInfo[0]);
					int maxStock = Integer.parseInt(stockInfo[1]);

					double buyPricePerItem = Math.max(.01,((double)buyPrice)/((double)buySize));
					double sellPricePerItem = Math.max(.01,((double)sellPrice)/((double)sellSize));
					if(!shop.addItem(itemId, damageMod, buyPricePerItem,
							sellPricePerItem, stock, maxStock))
					{
						if(isolateBrokenShopFile(file))
						{
							log.warning(String
									.format("[%s] Shop File \"%s\" has bad Item Data (%d:%d), Moving to \"plugins/CommandShops/broken-shops/\"",
											CommandShops.pdfFile.getName(),
											file.toString(), itemId, damageMod));
						}else{
							log.warning(String
									.format("[%s] Shop File \"%s\" has bad Item Data (%d:%d), Error moving to \"plugins/CommandShops/broken-shops/\"",
											CommandShops.pdfFile.getName(),
											file.toString(), itemId, damageMod));
						}
						return null;
					}
				}else{ // Not defined
					log.info(String
							.format("[%s] Shop File \"%s\" has undefined data, ignoring.",
									CommandShops.pdfFile.getName(), file.toString()));
				}
				line = br.readLine();
			}

			br.close();

			File dir = new File("plugins/CommandShops/shops-converted/");
			dir.mkdir();
			if(file.renameTo(new File(dir, file.getName())))
			{
				file.delete();
				return shop;
			}else{
				return null;
			}

		}catch(IOException e){
			if(isolateBrokenShopFile(file))
			{
				log.warning(String
						.format("[%s] Shop File \"%s\" Exception: %s, Moving to \"plugins/CommandShops/broken-shops/\"",
								CommandShops.pdfFile.getName(), file.toString(),
								e.toString()));
			}else{
				log.warning(String
						.format("[%s] Shop File \"%s\" Exception: %s, Error moving to \"plugins/CommandShops/broken-shops/\"",
								CommandShops.pdfFile.getName(), file.toString(),
								e.toString()));
			}
			return null;
		}
	}

	/**
	 * Convert string arrayto double array.
	 * @param sarray
	 * the sarray
	 * @return the double[]
	 */
	public static double[] convertStringArraytoDoubleArray(String[] sarray)
	{
		if(sarray != null)
		{
			double longArray[] = new double[sarray.length];
			for(int i = 0; i < sarray.length; i++)
			{
				longArray[i] = Long.parseLong(sarray[i]);
			}
			return longArray;
		}
		return null;
	}

	/**
	 * Load shop.
	 * @param file
	 * the file
	 * @return the shop
	 * @throws Exception
	 * the exception
	 */
	public Shop loadShop(File file) throws Exception
	{
		SortedProperties props = new SortedProperties();
		try
		{
			props.load(new FileInputStream(file));
		}catch(IOException e){
			log.warning(String.format((Locale)null,"[%s] %s", CommandShops.pdfFile.getName(),
					"IOException: " + e.getMessage()));
			return null;
		}

		// Shop attributes
		UUID uuid = UUID.fromString(props.getProperty("uuid",
				"00000000-0000-0000-0000-000000000000"));
		String name = props.getProperty("name", "Nameless Shop");
		boolean unlimitedMoney = Boolean.parseBoolean(props.getProperty(
				"unlimited-money", "false"));
		boolean unlimitedStock = Boolean.parseBoolean(props.getProperty(
				"unlimited-stock", "false"));
		double minBalance = Double.parseDouble((props.getProperty(
				"min-balance", "0.0")));
		boolean notification = Boolean.parseBoolean(props.getProperty(
				"notification", "true"));

		// Location - locationB=-88, 50, -127
		double[] locationA;
		double[] locationB;
		String world;
		try
		{
			locationA = convertStringArraytoDoubleArray(props.getProperty(
					"locationA").split(", "));
			locationB = convertStringArraytoDoubleArray(props.getProperty(
					"locationB").split(", "));
			world = props.getProperty("world", "world1");
		}catch(Exception e){
			if(isolateBrokenShopFile(file))
			{
				log.warning(String
						.format("[%s] Shop File \"%s\" has bad Location Data, Moving to \"plugins/CommandShops/broken-shops/\"",
								CommandShops.pdfFile.getName(), file.toString()));
			}else{
				log.warning(String
						.format("[%s] Shop File \"%s\" has bad Location Data, Error moving to \"plugins/CommandShops/broken-shops/\"",
								CommandShops.pdfFile.getName(), file.toString()));
			}
			return null;
		}

		// People
		String owner = props.getProperty("owner", "");
		String[] managers = props.getProperty("managers", "")
				.replaceAll("[\\[\\]]", "").split(", ");
		String creator = props.getProperty("creator", "CommandShops");

		Shop shop = new Shop(uuid);
		shop.setName(name);
		shop.setUnlimitedMoney(unlimitedMoney);
		shop.setUnlimitedStock(unlimitedStock);
		shop.setLocationA(new ShopLocation(locationA));
		shop.setLocationB(new ShopLocation(locationB));
		shop.setWorld(world);
		shop.setOwner(owner);
		shop.setManagers(managers);
		shop.setCreator(creator);
		shop.setNotification(notification);

		// Make sure minimum balance isn't negative
		if(minBalance < 0)
		{
			shop.setMinBalance(0);
		}else{
			shop.setMinBalance(minBalance);
		}

		// Iterate through all keys, find items & parse
		// props.setProperty(String.format((Locale)null,"%d:%d", info.typeId,
		// info.subTypeId), String.format((Locale)null,"%d:%d,%d:%d,%d:%d", buyPrice,
		// buySize, sellPrice, sellSize, stock, maxStock));
		Iterator<Object> it = props.keySet().iterator();
		while(it.hasNext())
		{
			String key = (String)it.next();
			if(key.matches("\\d+:\\d+"))
			{
				String[] k = key.split(":");
				int id = Integer.parseInt(k[0]);
				short type = Short.parseShort(k[1]);

				String value = props.getProperty(key);
				String[] v = value.split(",");

				String[] buy = v[0].split(":");
				double buyPrice = Double.parseDouble(buy[0]);
				int buyStackSize = Integer.parseInt(buy[1]);

				String[] sell = v[1].split(":");
				double sellPrice = Double.parseDouble(sell[0]);
				int sellStackSize = Integer.parseInt(sell[1]);

				String[] stock = v[2].split(":");
				int currStock = Integer.parseInt(stock[0]);
				int maxStock = Integer.parseInt(stock[1]);

				if(!shop.addItem(id, type, Math.max(.01,buyPrice/buyStackSize),
						Math.max(.01,sellPrice/sellStackSize), currStock, maxStock))
				{
					if(isolateBrokenShopFile(file))
					{
						log.warning(String
								.format("[%s] Shop File \"%s\" has bad Item Data (%d:%d), Moving to \"plugins/CommandShops/broken-shops/\"",
										CommandShops.pdfFile.getName(),
										file.toString(), id, type));
					}else{
						log.warning(String
								.format("[%s] Shop File \"%s\" has bad Item Data (%d:%d), Error moving to \"plugins/CommandShops/broken-shops/\"",
										CommandShops.pdfFile.getName(),
										file.toString(), id, type));
					}
					return null;
				}
			}
		}

		// Sanity Checks
		// Check that filename == UUID from file
		if(!file.getName().equalsIgnoreCase(
				String.format((Locale)null,"%s.shop", shop.getUuid().toString())))
		{
			shop = null;

			if(isolateBrokenShopFile(file))
			{
				log.warning(String
						.format("[%s] Shop file %s has bad data!  Moving to \"plugins/CommandShops/broken-shops/\"",
								CommandShops.pdfFile.getName(), file.getName()));
			}else{
				log.warning(String
						.format("[%s] Shop file %s has bad data!  Error moving to \"plugins/CommandShops/broken-shops/\"",
								CommandShops.pdfFile.getName(), file.getName()));
			}
		}

		return shop;
	}

	/**
	 * Checks if is olate broken shop file.
	 * @param file
	 * the file
	 * @return true, if is olate broken shop file
	 */
	public boolean isolateBrokenShopFile(File file)
	{
		File dir = new File("plugins/CommandShops/shops-broken/");
		dir.mkdir();
		if(file.renameTo(new File(dir, file.getName())))
		{
			file.delete();
			return true;
		}else{
			return false;
		}
	}
}
