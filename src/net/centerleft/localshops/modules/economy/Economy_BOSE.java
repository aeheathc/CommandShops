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

import cosine.boseconomy.BOSEconomy;

public class Economy_BOSE implements Economy {
    private String name = "BOSEconomy";
    private LocalShops plugin = null;
    private PluginManager pluginManager = null;
    private BOSEconomy economy = null;
    private EconomyServerListener economyServerListener = null;

    public Economy_BOSE(LocalShops plugin) {
        this.plugin = plugin;
        pluginManager = this.plugin.getServer().getPluginManager();

        economyServerListener = new EconomyServerListener(this);

        this.pluginManager.registerEvent(Type.PLUGIN_ENABLE, economyServerListener, Priority.Monitor, plugin);
        this.pluginManager.registerEvent(Type.PLUGIN_DISABLE, economyServerListener, Priority.Monitor, plugin);

        // Load Plugin in case it was loaded before
        if (economy == null) {
            Plugin bose = plugin.getServer().getPluginManager().getPlugin("BOSEconomy");
            if (bose != null) {
                if (bose.isEnabled()) {
                    economy = (BOSEconomy) bose;
                    log.info(String.format("[%s] %s hooked.", plugin.getDescription().getName(), name));
                }
            }
        }
    }

    @Override
    public String getName() {
        return name;
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
    public double getBalance(String playerName) {
        return (double) economy.getPlayerMoney(playerName);
    }

    @Override
    public boolean withdrawPlayer(String playerName, double amount) {
        double balance = getBalance(playerName);
        return economy.setPlayerMoney(playerName, (int) (balance - amount), false);
    }

    @Override
    public boolean depositPlayer(String playerName, double amount) {
        double balance = getBalance(playerName);
        return economy.setPlayerMoney(playerName, (int) (balance + amount), false);
    }

    @Override
    public boolean depositShop(Shop shop, double amount) {
        // Currently not supported
        return false;
    }

    @Override
    public boolean withdrawShop(Shop shop, double amount) {
        // Currently not supported
        return false;
    }

    private String getMoneyNamePlural() {
        return economy.getMoneyNamePlural();
    }

    private String getMoneyNameSingular() {
        return economy.getMoneyName();
    }
    
    private class EconomyServerListener extends ServerListener {
        Economy_BOSE economy = null;
        
        public EconomyServerListener(Economy_BOSE economy) {
            this.economy = economy;
        }
        
        public void onPluginEnable(PluginEnableEvent event) {
            if (economy.economy == null) {
                Plugin bose = plugin.getServer().getPluginManager().getPlugin("BOSEconomy");

                if (bose != null) {
                    if (bose.isEnabled()) {
                        economy.economy = (BOSEconomy) bose;
                        log.info(String.format("[%s] %s hooked.", plugin.getDescription().getName(), economy.name));
                    }
                }
            }
        }
        
        public void onPluginDisable(PluginDisableEvent event) {
            if (economy.economy != null) {
                if (event.getPlugin().getDescription().getName().equals("Essentials")) {
                    economy.economy = null;
                    log.info(String.format("[%s] %s un-hooked.", plugin.getDescription().getName(), economy.name));
                }
            }
        }
    }

    @Override
    public String format(double amount) {
        if (amount == 1) {
            return String.format("%f %s", amount, getMoneyNameSingular());
        } else {
            return String.format("%f %s", amount, getMoneyNamePlural());
        }
    }
}
