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

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

public class Economy_Essentials implements Economy {
    private String name = "Essentials Economy";
    private LocalShops plugin = null;
    private PluginManager pluginManager = null;
    private Essentials economy = null;
    private EconomyServerListener economyServerListener = null;
    
    public Economy_Essentials(LocalShops plugin) {
        this.plugin = plugin;
        pluginManager = this.plugin.getServer().getPluginManager();

        economyServerListener = new EconomyServerListener(this);
        
        this.pluginManager.registerEvent(Type.PLUGIN_ENABLE, economyServerListener, Priority.Monitor, plugin);
        this.pluginManager.registerEvent(Type.PLUGIN_DISABLE, economyServerListener, Priority.Monitor, plugin);
        
        // Load Plugin in case it was loaded before
        if (economy == null) {
            Plugin essentials = plugin.getServer().getPluginManager().getPlugin("Essentials");
            if (essentials != null && essentials.isEnabled()) {
                economy = (Essentials) essentials;
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
        User u = User.get(playerName);
        return u.getMoney();
    }

    @Override
    public boolean withdrawPlayer(String playerName, double amount) {
        amount = Math.abs(amount);
        User u = User.get(playerName);
        if(u.canAfford(amount)) {
            double money = u.getMoney();
            u.setMoney(money - amount);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean depositPlayer(String playerName, double amount) {
        amount = Math.abs(amount);
        User u = User.get(playerName);
        double money = u.getMoney();
        u.setMoney(money + amount);
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

    private String getMoneyNamePlural() {
        return "samolians";
    }

    private String getMoneyNameSingular() {
        return "samolian";
    }
    
    private class EconomyServerListener extends ServerListener {
        Economy_Essentials economy = null;
        
        public EconomyServerListener(Economy_Essentials economy) {
            this.economy = economy;
        }
        
        public void onPluginEnable(PluginEnableEvent event) {
            if (economy.economy == null) {
                Plugin essentials = plugin.getServer().getPluginManager().getPlugin("Essentials");

                if (essentials != null && essentials.isEnabled()) {
                    economy.economy = (Essentials) essentials;
                    log.info(String.format("[%s] %s hooked.", plugin.getDescription().getName(), economy.name));
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