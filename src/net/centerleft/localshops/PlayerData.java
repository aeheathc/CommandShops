package net.centerleft.localshops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import net.centerleft.localshops.modules.economy.Economy;
import net.centerleft.localshops.modules.economy.EconomyResponse;

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
    private double xyzA[] = null;
    private double xyzB[] = null;
    protected String size = "";
    
    // Logging
    private static final Logger log = Logger.getLogger("Minecraft");    

    // Constructor
    public PlayerData(LocalShops plugin, String playerName) {
        this.plugin = plugin;
        this.playerName = playerName;
    }

    public double[] getPositionA() {
        return xyzA;
    }

    public double[] getPositionB() {
        return xyzB;
    }

    public void setPositionA(double[] xyz) {
        xyzA = xyz.clone();
    }

    public void setPositionB(double[] xyz) {
        xyzB = xyz.clone();
    }

    public String getSizeString() {
        return size;
    }

    public boolean checkSize() {
        if (xyzA == null || xyzB == null) {
            return false;
        }

        double width1 = Math.abs(xyzA[0] - xyzB[0]) + 1;
        double height = Math.abs(xyzA[1] - xyzB[1]) + 1;
        double width2 = Math.abs(xyzA[2] - xyzB[2]) + 1;

        size = "" + width1 + "x" + height + "x" + width2;

        if (width1 > Config.SHOP_SIZE_MAX_WIDTH || width2 > Config.SHOP_SIZE_MAX_WIDTH || height > Config.SHOP_SIZE_MAX_HEIGHT) {
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

    public boolean payPlayer(String playerName, double cost) {
        EconomyResponse depositResp = plugin.econManager.depositPlayer(playerName, cost);
        if(depositResp.transactionSuccess()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean payPlayer(String playerFrom, String playerTo, double cost) {       
        EconomyResponse balanceFromResp = plugin.econManager.getBalance(playerFrom);
        EconomyResponse balanceToResp = plugin.econManager.getBalance(playerTo);
        
        log.info("PlayerFrom: " + playerFrom + " balanceFrom: " + balanceFromResp.amount + " PlayerTo: " + playerTo + " balanceTo: " + balanceToResp.amount + " Cost: " + cost);
        
        EconomyResponse withdrawResp = plugin.econManager.withdrawPlayer(playerFrom, cost);
        if(!withdrawResp.transactionSuccess()) {
            log.info("Failed to withdraw");
            return false;
        }
        
        EconomyResponse depositResp = plugin.econManager.depositPlayer(playerTo, cost);
        if(!depositResp.transactionSuccess()) {
            log.info("Failed to deposit");
            // Return money to shop owner
            EconomyResponse returnResp = plugin.econManager.depositPlayer(playerFrom, cost);
            if(!returnResp.transactionSuccess()) {
                log.warning(String.format("[%s] ERROR:  Payment failed and could not return funds to original state!  %s may need %s!", plugin.pdfFile.getName(), playerName, plugin.econManager.format(cost)));
            }
            return false;
        }
        
        if (withdrawResp.transactionSuccess() && depositResp.transactionSuccess()) {
            plugin.shopData.logPayment(playerFrom, "payment", withdrawResp.amount, balanceFromResp.amount, withdrawResp.balance);
            plugin.shopData.logPayment(playerTo, "payment", depositResp.amount, balanceToResp.amount, depositResp.balance);
            return true;
        } else {
            return false;
        }
    }

    public double getBalance(String playerName) {
        EconomyResponse balanceResp = plugin.econManager.getBalance(playerName);
        return balanceResp.amount;
    }

    public boolean chargePlayer(String playerName, double chargeAmount) {
        EconomyResponse balanceResp = plugin.econManager.getBalance(playerName);
        if(!balanceResp.transactionSuccess()) {
            return false;
        }
        
        EconomyResponse withdrawResp = plugin.econManager.withdrawPlayer(playerName, chargeAmount);
        if(withdrawResp.transactionSuccess()) {
            plugin.shopData.logPayment(playerName, "payment", withdrawResp.amount, balanceResp.balance, withdrawResp.balance);
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
