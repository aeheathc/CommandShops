package net.centerleft.localshops.modules.economy;

import java.util.logging.Logger;

import net.centerleft.localshops.Shop;

public interface Economy {
    
    public static final Logger log = Logger.getLogger("Minecraft");

    public boolean isEnabled();
    public String getName();
    public String format(double amount);
    public double getBalance(String playerName);
    public double withdrawPlayer(String playerName, double amount);
    public double depositPlayer(String playerName, double amount);
    public double withdrawShop(Shop shop, double amount);
    public double depositShop(Shop shop, double amount);
}