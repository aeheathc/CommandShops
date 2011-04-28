package net.centerleft.localshops;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Search {
    private static final Logger log = Logger.getLogger("Minecraft");
    private static final String ITEMS_FILE = "plugins/LocalShops/items.txt"; 
    
    private static ArrayList<ItemInfo> items = new ArrayList<ItemInfo>();
    
    public static void loadItems() {
        File itemFile = new File(ITEMS_FILE);
        if(!itemFile.exists()) {
            // retrieve the file somehow
        }
                
        try {
            BufferedReader br = new BufferedReader(new FileReader(itemFile));
            String line = br.readLine();
            while(line != null) {
                log.info(line);
                String[] cols = line.split("\\|");
                String name = cols[0];
                String[] attrsArray = cols[1].split("\\]\\[");
                int typeId = Integer.parseInt(cols[2]);
                short subTypeId = Short.parseShort(cols[3]);
                
                ArrayList<String[]> attributes = new ArrayList<String[]>();
                for(String attrs : attrsArray) {
                    attrs = attrs.replaceAll("[\\[\\]]", "");
                    attributes.add(attrs.split(","));
                }
                
                items.add(new ItemInfo(name, attributes, typeId, subTypeId));
                
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        for(ItemInfo item : items) {
            log.info(item.toString());
        }
    }

    public static String join(List<String> shopList, String glue) {
        String joined = null;
        for (String element : shopList) {
            if (joined == null) {
                joined = element;
            } else {
                joined += glue + element;
            }
        }
        
        if(joined == null) {
            return "";
        } else {
            return joined;
        }
    }
    
    public static ItemInfo itemById(int type) {
        return itemById(type, (short) 0);
    }
    
    public static ItemInfo itemById(int type, short subType) {
        for(ItemInfo item : items) {
            if(item.typeId == type && item.subTypeId == subType) {
                return item;
            }
        }
        return null;
    }
    
    public static ItemInfo itemByName(ArrayList<String> search) {
        String searchString = join(search, " ");
        return itemByName(searchString);
    }

    public static ItemInfo itemByName(String searchString) {
        ItemInfo matchedItem = null;
        int matchedItemStrength = 0;

        if (searchString.matches("\\d+:\\d+")) {
            // Match on integer:short to get typeId and subTypeId
            
            // Retrieve/parse data
            String[] params = searchString.split(":");
            int typeId = Integer.parseInt(params[0]);
            short subTypeId = Short.parseShort(params[1]);
            
            // Iterate through Items
            for (ItemInfo item : items) {
                // Test for match
                if (item.typeId == typeId && item.subTypeId == subTypeId) {
                    matchedItem = item;
                    break;
                }
            }
        } else if (searchString.matches("\\d+")) {
            // Match an integer only, assume subTypeId = 0
            
            // Retrieve/parse data
            int typeId = Integer.parseInt(searchString);
            short subTypeId = 0;
            
            // Iterate through Items
            for (ItemInfo item : items) {
                // Test for match
                if (item.typeId == typeId && item.subTypeId == subTypeId) {
                    matchedItem = item;
                    break;
                }
            }
        } else {
            // Else this must be a string that we need to identify
            
            // Iterate through Items
            for (ItemInfo item : items) {
                // Look through each possible match criteria
                for (String[] attributes : item.search) {
                    boolean match = false;
                    // Loop through entire criteria strings
                    for (String attribute : attributes) {
                        if (searchString.contains(attribute)) {
                            match = true;
                        } else {
                            match = false;
                            break;
                        }
                    }
                    
                    // THIS was a match
                    if (match) {
                        if (matchedItem == null || attributes.length > matchedItemStrength) {
                            matchedItem = item;
                            matchedItemStrength = attributes.length;
                        }
                        
                        // This criteria was a match, lets break out of this item...no point testing alternate criteria's
                        break;
                    }                    
                }
            }
        }

        return matchedItem;
    }
}
