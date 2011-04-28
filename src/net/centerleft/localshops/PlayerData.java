package net.centerleft.localshops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.entity.Player;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;

import cuboidLocale.BookmarkedResult;

public class PlayerData {
    // Objects
    private LocalShops plugin = null;

    // Attributes
    protected List<String> shopList = Collections.synchronizedList(new ArrayList<String>());
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

    public boolean addPlayerToShop(String shopName) {
        String playerWorld = plugin.getServer().getPlayer(playerName).getWorld().getName();

        if (!playerIsInShop(shopName) && plugin.shopData.getShop(shopName).getWorld().equalsIgnoreCase(playerWorld)) {
            shopList.add(shopName);
            return true;
        } else {
            return false;
        }
    }

    public boolean playerIsInShop(String shopName) {
        String playerWorld = plugin.getServer().getPlayer(playerName).getWorld().getName();

        if (shopList.contains(shopName)) {
            if (plugin.shopData.getShop(shopName).getWorld().equalsIgnoreCase(playerWorld)) {
                return true;
            }
        }
        return false;
    }

    public void removePlayerFromShop(Player player, String shopName) {
        shopList.remove(shopName);
    }

    public List<String> playerShopsList(String playerName) {
        return shopList;
    }

    public boolean payPlayer(String playerName, int cost) {
        if (plugin.pluginListener.useiConomy) {
            //iConomy ic = plugin.pluginListener.iConomy;
            Account account = iConomy.getBank().getAccount(playerName);
            if (account == null) {
                iConomy.getBank().addAccount(playerName);
                account = iConomy.getBank().getAccount(playerName);
            }
            double balance = account.getBalance();
            account.setBalance(balance + (double) cost);
            plugin.shopData.logPayment(playerName, "payment", cost, balance, balance + (double) cost);
            return true;
        }
        return false;
    }

    public boolean payPlayer(String playerFrom, String playerTo, int cost) {
        if (plugin.pluginListener.useiConomy) {
            //iConomy ic = plugin.pluginListener.iConomy;

            Account accountFrom = iConomy.getBank().getAccount(playerFrom);
            if (accountFrom == null) {
                iConomy.getBank().addAccount(playerFrom);
                accountFrom = iConomy.getBank().getAccount(playerFrom);
            }
            double balanceFrom = accountFrom.getBalance();

            Account accountTo = iConomy.getBank().getAccount(playerTo);
            if (accountTo == null) {
                iConomy.getBank().addAccount(playerTo);
                accountTo = iConomy.getBank().getAccount(playerTo);
            }
            double balanceTo = accountTo.getBalance();

            if (balanceFrom < cost)
                return false;

            accountFrom.setBalance(balanceFrom - cost);
            plugin.shopData.logPayment(playerFrom, "payment", cost, balanceFrom, balanceFrom + cost);
            accountTo.setBalance(balanceTo + cost);
            plugin.shopData.logPayment(playerTo, "payment", cost, balanceTo, balanceTo + cost);
            return true;
        }
        return false;
    }

    public long getBalance(String shopOwner) {
        if (plugin.pluginListener.useiConomy) {
            //iConomy ic = plugin.pluginListener.iConomy;

            Account account = iConomy.getBank().getAccount(shopOwner);
            if (account == null) {
                iConomy.getBank().addAccount(shopOwner);
                account = iConomy.getBank().getAccount(shopOwner);
            }
            double balanceFrom = account.getBalance();

            return (long) Math.floor(balanceFrom);
        }
        return 0;
    }

    public boolean chargePlayer(String shopOwner, long chargeAmount) {
        if (plugin.pluginListener.useiConomy) {
            //iConomy ic = plugin.pluginListener.iConomy;
            //if (ic == null)
            //    return false;

            Account account = iConomy.getBank().getAccount(shopOwner);
            if (account == null) {
                iConomy.getBank().addAccount(shopOwner);
                account = iConomy.getBank().getAccount(shopOwner);
            }
            double balanceFrom = account.getBalance();
            double newBalance = balanceFrom - chargeAmount;
            if (balanceFrom >= chargeAmount) {
                account.setBalance(newBalance);
                plugin.shopData.logPayment(shopOwner, "payment", chargeAmount, balanceFrom, newBalance);
                return true;
            } else {
                return false;
            }

        }
        return false;
    }
    
    public String getCurrentShop() {
        if(shopList.size() == 1) {
            return shopList.get(0);
        } else {
            return null;
        }
    }

}
