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

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;

public class Economy_iConomy4 implements Economy {
    private String name = "iConomy 4";
    private LocalShops plugin = null;
    private PluginManager pluginManager = null;
    protected iConomy economy = null;
    private EconomyServerListener economyServerListener = null;
    
    public Economy_iConomy4(LocalShops plugin) {
        this.plugin = plugin;
        this.pluginManager = this.plugin.getServer().getPluginManager();

        economyServerListener = new EconomyServerListener(this);
        
        this.pluginManager.registerEvent(Type.PLUGIN_ENABLE, economyServerListener, Priority.Monitor, plugin);
        this.pluginManager.registerEvent(Type.PLUGIN_DISABLE, economyServerListener, Priority.Monitor, plugin);
        
        // Load Plugin in case it was loaded before
        if(economy == null) {
            Plugin ec = plugin.getServer().getPluginManager().getPlugin("iConomy");
            if (ec != null && ec.isEnabled() && ec.getClass().getName().equals("com.nijiko.coelho.iConomy.iConomy.class")) {
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
    public String format(double amount) {
        return iConomy.getBank().format(amount);
    }

    @Override
    public double getBalance(String playerName) {
        Account account = iConomy.getBank().getAccount(playerName);
        if (account == null) {
            iConomy.getBank().addAccount(playerName);
            account = iConomy.getBank().getAccount(playerName);
        }
        return account.getBalance();
    }

    @Override
    public boolean withdrawPlayer(String playerName, double amount) {
        amount = Math.abs(amount);
        double balance = getBalance(playerName);
        if(balance >= amount) {
            Account account = iConomy.getBank().getAccount(playerName);
            if(account == null) {
                return false;
            }
            account.subtract(amount);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean depositPlayer(String playerName, double amount) {
        amount = Math.abs(amount);
        Account account = iConomy.getBank().getAccount(playerName);
        if(account == null) {
            iConomy.getBank().addAccount(playerName);
            account = iConomy.getBank().getAccount(playerName);
        }
        account.add(amount);
        return true;
    }

    @Override
    public boolean withdrawShop(Shop shop, double amount) {
        amount = Math.abs(amount);
        // Currently not supported
        return false;
    }

    @Override
    public boolean depositShop(Shop shop, double amount) {
        amount = Math.abs(amount);
        // Currently not supported
        return false;
    }
    
    private class EconomyServerListener extends ServerListener {
        Economy_iConomy4 economy = null;
        
        public EconomyServerListener(Economy_iConomy4 economy) {
            this.economy = economy;
        }
        
        public void onPluginEnable(PluginEnableEvent event) {
            if (economy.economy == null) {
                Plugin iConomy = plugin.getServer().getPluginManager().getPlugin("iConomy");

                if (iConomy != null && iConomy.isEnabled() && iConomy.getClass().getName().equals("com.nijiko.coelho.iConomy.iConomy")) {
                    economy.economy = (iConomy) iConomy;
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
}