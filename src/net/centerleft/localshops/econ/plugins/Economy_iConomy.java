package net.centerleft.localshops.econ.plugins;

import net.centerleft.localshops.LocalShops;
import net.centerleft.localshops.Shop;
import net.centerleft.localshops.econ.Economy;

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

public class Economy_iConomy implements Economy {
    private String name = "iConomy";
    private LocalShops plugin = null;
    private PluginManager pluginManager = null;
    protected iConomy economy = null;
    private EconomyServerListener economyServerListener = null;
    
    public Economy_iConomy(LocalShops plugin) {
        this.plugin = plugin;
        this.pluginManager = this.plugin.getServer().getPluginManager();

        economyServerListener = new EconomyServerListener(this);
        
        this.pluginManager.registerEvent(Type.PLUGIN_ENABLE, economyServerListener, Priority.Monitor, plugin);
        this.pluginManager.registerEvent(Type.PLUGIN_DISABLE, economyServerListener, Priority.Monitor, plugin);
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
        Account account = iConomy.getAccount(playerName);
        BankAccount bankAccount = account.getMainBankAccount();
        return bankAccount.getHoldings().balance();
    }

    @Override
    public boolean withdrawPlayer(String playerName, double amount) {
        Account account = iConomy.getAccount(playerName);
        Holdings holdings = account.getHoldings();
        if (holdings.hasEnough(amount)) {
            holdings.subtract(amount);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean depositPlayer(String playerName, double amount) {
        Account account = iConomy.getAccount(playerName);
        Holdings holdings = account.getHoldings();
        holdings.add(amount);
        return true;
    }

    @Override
    public boolean withdrawShop(Shop shop, double amount) {
        // Currently not supported
        return false;
    }

    @Override
    public boolean depositShop(Shop shop, double amount) {
        // Currently not supported
        return false;
    }
    
    private class EconomyServerListener extends ServerListener {
        Economy_iConomy economy = null;
        
        public EconomyServerListener(Economy_iConomy economy) {
            this.economy = economy;
        }
        
        public void onPluginEnable(PluginEnableEvent event) {
            if (economy.economy == null) {
                Plugin iConomy = plugin.getServer().getPluginManager().getPlugin("iConomy");

                if (iConomy != null) {
                    if (iConomy.isEnabled()) {
                        economy.economy = (iConomy) iConomy;
                        log.info(String.format("[%s] %s hooked.", plugin.getDescription().getName(), economy.name));
                    }
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