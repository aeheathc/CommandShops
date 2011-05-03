package net.centerleft.localshops.econ;

import java.util.logging.Logger;

import net.centerleft.localshops.Shop;

public interface Economy {
    
    public static final Logger log = Logger.getLogger("Minecraft");

    public boolean isEnabled();
    public String getName();
    public String getMoneyNamePlural();
    public String getMoneyNameSingular();
    public double getBalance(String playerName);
    public boolean withdrawPlayer(String playerName, double amount);
    public boolean depositPlayer(String playerName, double amount);
    public boolean withdrawShop(Shop shop, double amount);
    public boolean depositShop(Shop shop, double amount);
}