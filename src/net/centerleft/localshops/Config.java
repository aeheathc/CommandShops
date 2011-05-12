package net.centerleft.localshops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Config {

    // Shop Size settings
    protected static long SHOP_SIZE_DEF_WIDTH = 5;
    protected static long SHOP_SIZE_DEF_HEIGHT = 3;
    protected static long SHOP_SIZE_MAX_WIDTH = 30;
    protected static long SHOP_SIZE_MAX_HEIGHT = 10;
    
    // Shop Charge settings
    protected static double SHOP_CHARGE_CREATE_COST = 4000;
    protected static double SHOP_CHARGE_MOVE_COST = 1000;
    protected static boolean SHOP_CHARGE_CREATE = false;
    protected static boolean SHOP_CHARGE_MOVE = false;
    
    // Server Settings
    protected static boolean SRV_LOG_TRANSACTIONS = true;
    protected static boolean SRV_DEBUG = false;
    protected static UUID SRV_UUID = null;
    protected static boolean SRV_REPORT = false;
    
    // Player Settings
    protected static int PLAYER_MAX_SHOPS = -1;        // Anything < 0 = unlimited player shops.
    
    // Item Settings
    protected static int ITEM_MAX_DAMAGE = 35;
    
    // UUID settings
    protected static int UUID_MIN_LENGTH = 1;
    protected static List<String> UUID_LIST = Collections.synchronizedList(new ArrayList<String>());
}
