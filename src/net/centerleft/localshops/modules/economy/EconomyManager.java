package net.centerleft.localshops.modules.economy;

import java.util.Iterator;
import java.util.TreeMap;
import java.util.logging.Logger;

import net.centerleft.localshops.LocalShops;
import net.centerleft.localshops.Shop;

public class EconomyManager {
    
    private LocalShops plugin = null;
    private TreeMap<Integer,Economy> econs = new TreeMap<Integer,Economy>();
    private Economy activeEconomy = null;
    private static final Logger log = Logger.getLogger("Minecraft");

    public EconomyManager(LocalShops plugin) {
        this.plugin = plugin;
    }
    
    public void loadEconomies() {
        // Try to load BOSEconomy
        if (packageExists(new String[] { "com.earth2me.essentials.api.Economy", "com.earth2me.essentials.api.NoLoanPermittedException", "com.earth2me.essentials.api.UserDoesNotExistException" })) {
            Economy bose = new Economy_BOSE(plugin);
            econs.put(10, bose);
            log.info(String.format("[%s] Loading BOSEconomy: %s", plugin.getDescription().getName(), bose.isEnabled() ? "Yes" : "No"));
        } else {
            log.info(String.format("[%s] BOSEconomy: Library not loaded.", plugin.getDescription().getName()));
        }

        // Try to load Essentials Economy
        if (packageExists(new String[] { "com.earth2me.essentials.api.Economy", "com.earth2me.essentials.api.NoLoanPermittedException", "com.earth2me.essentials.api.UserDoesNotExistException" })) {
            Economy essentials = new Economy_Essentials(plugin);
            econs.put(9, essentials);
            log.info(String.format("[%s] Essentials Economy: %s", plugin.getDescription().getName(), essentials.isEnabled() ? "Yes" : "No"));
        } else {
            log.info(String.format("[%s] Essentials Economy: Library not loaded.", plugin.getDescription().getName()));
        }

        // Try to load iConomy 4
        if (packageExists(new String[] { "com.nijiko.coelho.iConomy.iConomy", "com.nijiko.coelho.iConomy.system.Account" })) {
            Economy icon4 = new Economy_iConomy4(plugin);
            econs.put(8, icon4);
            log.info(String.format("[%s] Loading iConomy 4: %s", plugin.getDescription().getName(), icon4.isEnabled() ? "Yes" : "No"));
        } else {
            log.info(String.format("[%s] iConomy 4: Library not loaded.", plugin.getDescription().getName()));
        }

        // Try to load iConomy 5
        if (packageExists(new String[] { "com.iConomy.iConomy", "com.iConomy.system.Account", "com.iConomy.system.Holdings" })) {
        Economy icon5 = new Economy_iConomy5(plugin);
        econs.put(7, icon5);
        log.info(String.format("[%s] Loading iConomy 5: %s", plugin.getDescription().getName(), icon5.isEnabled() ? "Yes" : "No"));
        } else {
            log.info(String.format("[%s] iConomy 5: Library not loaded.", plugin.getDescription().getName()));
        }
        
        return;
    }
    
    private boolean packageExists(String[] packages) {
        try {
            for (String pkg : packages) {
                Class.forName(pkg);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private Economy getEconomy() {
        if(activeEconomy == null) {
            Iterator<Economy> it = econs.values().iterator();
            while(it.hasNext()) {
                Economy e = it.next();
                if(e.isEnabled()) {
                    return e;
                }
            }
            return null;
        } else {
            return activeEconomy;
        }
    }
    
    public String getName() {
        return getEconomy().getName();
    }
    
    public String format(double amount) {
        return getEconomy().format(amount);
    }
    
    public EconomyResponse getBalance(String playerName) {
        return getEconomy().getBalance(playerName);
    }
    
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        return getEconomy().withdrawPlayer(playerName, amount);
    }
    
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return getEconomy().depositPlayer(playerName, amount);
    }
    
    public EconomyResponse withdrawShop(Shop shop, double amount) {
        return getEconomy().withdrawShop(shop, amount);
    }
    
    public EconomyResponse depositShop(Shop shop, double amount) {
        return getEconomy().depositShop(shop, amount);
    }
}