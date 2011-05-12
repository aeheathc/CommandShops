package net.centerleft.localshops.modules.economy;

import net.centerleft.localshops.LocalShops;
import net.centerleft.localshops.Shop;

import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.iConomy.iConomy;
import com.iConomy.system.Account;
import com.iConomy.system.BankAccount;
import com.iConomy.system.Holdings;

public class Economy_iConomy5 implements Economy {
    private String name = "iConomy 5";
    private LocalShops plugin = null;
    private PluginManager pluginManager = null;
    protected iConomy economy = null;
    private EconomyServerListener economyServerListener = null;
    
    public Economy_iConomy5(LocalShops plugin) {
        this.plugin = plugin;
        this.pluginManager = this.plugin.getServer().getPluginManager();

        economyServerListener = new EconomyServerListener(this);
        
        this.pluginManager.registerEvent(Type.PLUGIN_ENABLE, economyServerListener, Priority.Monitor, plugin);
        this.pluginManager.registerEvent(Type.PLUGIN_DISABLE, economyServerListener, Priority.Monitor, plugin);
        
        // Load Plugin in case it was loaded before
        if(economy == null) {
            Plugin ec = plugin.getServer().getPluginManager().getPlugin("iConomy");
            if (ec != null && ec.isEnabled() && ec.getClass().getName().equals("com.iConomy.iConomy")) {
                economy = (iConomy) ec;
                log.info(String.format("[%s] %s hooked.", plugin.getDescription().getName(), name));
            }
        }
    }
    
    @Override
    public boolean isEnabled() {
        if(economy == null) {
            return false;
        } else {
            return economy.isEnabled();
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public double getBalance(String playerName) {
        return iConomy.getAccount(playerName).getHoldings().balance();
    }

    @Override
    public double withdrawPlayer(String playerName, double amount) {
        Account account = iConomy.getAccount(playerName);
        Holdings holdings = account.getHoldings();
        if (holdings.hasEnough(amount)) {
            holdings.subtract(amount);
            return amount;
        } else {
            return -1;
        }
    }

    @Override
    public double depositPlayer(String playerName, double amount) {
        Account account = iConomy.getAccount(playerName);
        Holdings holdings = account.getHoldings();
        holdings.add(amount);
        return amount;
    }

    @Override
    public double withdrawShop(Shop shop, double amount) {
        if(amount < 0) {
            return -1;
        }
        amount = Math.round(amount);
        // Currently not supported
        return -1;
    }

    @Override
    public double depositShop(Shop shop, double amount) {
        if(amount < 0) {
            return -1;
        }
        amount = Math.round(amount);
        // Currently not supported
        return -1;
    }
    
    private class EconomyServerListener extends ServerListener {
        Economy_iConomy5 economy = null;
        
        public EconomyServerListener(Economy_iConomy5 economy) {
            this.economy = economy;
        }
        
        public void onPluginEnable(PluginEnableEvent event) {
            if (economy.economy == null) {
                Plugin ec = plugin.getServer().getPluginManager().getPlugin("iConomy");

                if (ec != null && ec.isEnabled() && ec.getClass().getName().equals("com.iConomy.iConomy")) {
                    economy.economy = (iConomy) ec;
                    log.info(String.format("[%s] %s hooked.", plugin.getDescription().getName(), economy.name));
                }
            }
        }
        
        public void onPluginDisable(PluginDisableEvent event) {
            if (economy.economy != null) {
                if (event.getPlugin().getDescription().getName().equals("iConomy")) {
                    economy.economy = null;
                    log.info(String.format("[%s] %s un-hooked.", plugin.getDescription().getName(), economy.name));
                }
            }
        }
    }

    @Override
    public String format(double amount) {
        return iConomy.format(amount);
    }
}