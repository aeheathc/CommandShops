package net.centerleft.localshops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import net.centerleft.localshops.modules.economy.Economy;

import org.bukkit.entity.Player;

import com.nijiko.coelho.iConomy.system.Account;

import cuboidLocale.BookmarkedResult;

public class PlayerData {
    // Objects
    private LocalShops plugin = null;

    // Attributes
    protected List<UUID> shopList = Collections.synchronizedList(new ArrayList<UUID>());
    protected BookmarkedResult bookmark = new BookmarkedResult();
    protected String playerName = null;
    protected boolean isSelecting = false;
    private long xyzA[] = null;
    private long xyzB[] = null;
    protected String size = "";
    
    // Logging
    private static final Logger log = Logger.getLogger("Minecraft");    

    // Constructor
    public PlayerData(LocalShops plugin, String playerName) {
        this.plugin = plugin;
        this.playerName = playerName;
    }

    public long[] getPositionA() {
        return xyzA;
    }

    public long[] getPositionB() {
        return xyzB;
    }

    public void setPositionA(long[] xyz) {
        xyzA = xyz.clone();
    }

    public void setPositionB(long[] xyz) {
        xyzB = xyz.clone();
    }

    public String getSizeString() {
        return size;
    }

    public boolean checkSize() {
        if (xyzA == null || xyzB == null) {
            return false;
        }

        long width1 = Math.abs(xyzA[0] - xyzB[0]) + 1;
        long height = Math.abs(xyzA[1] - xyzB[1]) + 1;
        long width2 = Math.abs(xyzA[2] - xyzB[2]) + 1;

        size = "" + width1 + "x" + height + "x" + width2;

        if (width1 > plugin.shopData.maxWidth || width2 > plugin.shopData.maxWidth || height > plugin.shopData.maxHeight) {
            return false;
        } else {
            return true;
        }
    }

    public boolean addPlayerToShop(Shop shop) {
        String playerWorld = plugin.getServer().getPlayer(playerName).getWorld().getName();

        if (!playerIsInShop(shop) && shop.getWorld().equalsIgnoreCase(playerWorld)) {
            shopList.add(shop.getUuid());
            return true;
        } else {
            return false;
        }
    }

    public boolean playerIsInShop(Shop shop) {
        String playerWorld = plugin.getServer().getPlayer(playerName).getWorld().getName();

        if (shopList.contains(shop.getUuid())) {
            if (shop.getWorld().equalsIgnoreCase(playerWorld)) {
                return true;
            }
        }
        return false;
    }

    public void removePlayerFromShop(Player player, UUID uuid) {
        shopList.remove(uuid);
    }

    public List<UUID> playerShopsList(String playerName) {
        return shopList;
    }

    public boolean payPlayer(String playerName, int cost) {
        return plugin.econManager.depositPlayer(playerName, cost);
    }

    public boolean payPlayer(String playerFrom, String playerTo, int cost) {       
        double balanceFrom = plugin.econManager.getBalance(playerFrom);
        double balanceTo = plugin.econManager.getBalance(playerTo);
        
        log.info("PlayerFrom: " + playerFrom + " balanceFrom: " + balanceFrom + " PlayerTo: " + playerTo + " balanceTo: " + balanceTo + " Cost: " + cost);
        
        boolean withdraw = plugin.econManager.withdrawPlayer(playerFrom, cost);
        boolean deposit = plugin.econManager.depositPlayer(playerTo, cost);
        
        if(!withdraw) {
            log.info("Failed to withdraw");
        }
        
        if(!deposit) {
            log.info("Failed to deposit");
        }
        
        if (withdraw && deposit) {
            plugin.shopData.logPayment(playerFrom, "payment", cost, balanceFrom, balanceFrom + cost);
            plugin.shopData.logPayment(playerTo, "payment", cost, balanceTo, balanceTo + cost);
            return true;
        } else {
            return false;
        }
    }

    public double getBalance(String playerName) {
        return plugin.econManager.getBalance(playerName);
    }

    public boolean chargePlayer(String shopOwner, double chargeAmount) {        
        double balanceFrom = plugin.econManager.getBalance(shopOwner);
        
        if(plugin.econManager.withdrawPlayer(shopOwner, chargeAmount)) {
            plugin.shopData.logPayment(shopOwner, "payment", chargeAmount, balanceFrom, balanceFrom - chargeAmount);
            return true;
        } else {
            return false;
        }
    }
    
    public UUID getCurrentShop() {
        if(shopList.size() == 1) {
            return shopList.get(0);
        } else {
            return null;
        }
    }

}
