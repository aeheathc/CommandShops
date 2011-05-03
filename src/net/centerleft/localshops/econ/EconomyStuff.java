package net.centerleft.localshops.econ;

import java.util.logging.Logger;

import net.centerleft.localshops.LocalShops;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.iConomy.iConomy;

import cosine.boseconomy.BOSEconomy;

public class EconomyStuff {
    private LocalShops plugin = null;
    
    // Economy Type
    public static enum EconomyType {
        BOSE(1),
        ICONOMY(2),
        ESSENTIALS(3);
        
        int id;
        EconomyType(int type) {
            this.id = type;
        }
        
        public int getType() {
            return id;
        }
    }
    
    // Selected Economy Plugin
    private EconomyType economyType = null;
    
    // Economy Objects
    private BOSEconomy bose = null;
    private iConomy iconomy = null;
    private Object essentials = null;
    
    // Logger
    private static final Logger log = Logger.getLogger("Minecraft");
    
    public EconomyStuff(LocalShops plugin) {
        this.plugin = plugin;
    }
    
    protected void findEconomyPlugins() {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        
        // BOSEconomy
        bose = null;
        Plugin temp = pluginManager.getPlugin("BOSEconomy");
        if(temp != null) {
            bose = (BOSEconomy)temp;
        }
        
        // iConomy
        iconomy = null;
        Plugin ic = pluginManager.getPlugin("iConomy");
        if (ic != null) {
            if (ic.isEnabled()) {
                iconomy = (iConomy) ic;
                log.info(String.format("[%s] %s", plugin.getDescription().getName(), "iConomy found."));
            } else {
                log.info(String.format("[%s] %s", plugin.getDescription().getName(), "Waiting for iConomy to start."));
            }
        }
    }
    
    protected boolean depositPlayer(String playerName, int amount) {
        switch (economyType) {
        case BOSE:
            return boseDepositPlayer(playerName, amount);
        case ICONOMY:
            return iconomyDepositPlayer(playerName, amount);
        case ESSENTIALS:
            return essentialsDepositPlayer(playerName, amount);
        default:
            return false;
        }
    }
    
    protected boolean withdrawPlayer(String playerName, int amount) {
        switch (economyType) {
        case BOSE:
            return boseWithdrawPlayer(playerName, amount);
        case ICONOMY:
            return iconomyWithdrawPlayer(playerName, amount);
        case ESSENTIALS:
            return essentialsWithdrawPlayer(playerName, amount);
        default:
            return false;
        }
    }
    
    protected int getBalance(String playerName, int amount) {
        switch (economyType) {
        case BOSE:
            return boseGetBalance(playerName);
        case ICONOMY:
            return iconomyGetBalance(playerName);
        case ESSENTIALS:
            return essentialsGetBalance(playerName);
        default:
            return 0;
        }
    }
    
    private boolean boseDepositPlayer(String playerName, int amount) {
        return false;
    }
    
    private boolean boseWithdrawPlayer(String playerName, int amount) {
        return false;
    }
    
    private int boseGetBalance(String playerName) {
        return 0;
    }
    
    private boolean iconomyDepositPlayer(String playerName, int amount) {
        return false;
    }
    
    private boolean iconomyWithdrawPlayer(String playerName, int amount) {
        return false;
    }
    
    private int iconomyGetBalance(String playerName) {
        return 0;
    }
    
    private boolean essentialsDepositPlayer(String playerName, int amount) {
        return false;
    }
    
    private boolean essentialsWithdrawPlayer(String playerName, int amount) {
        return false;
    }
    
    private int essentialsGetBalance(String playerName) {
        return 0;
    }
}