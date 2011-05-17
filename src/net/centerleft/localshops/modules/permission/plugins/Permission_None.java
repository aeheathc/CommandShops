package net.centerleft.localshops.modules.permission.plugins;

import net.centerleft.localshops.modules.permission.Permission;

import org.bukkit.plugin.Plugin;

public class Permission_None implements Permission {
    private String name = "Local Fallback Permissions";
    private Plugin plugin = null;
    
    public Permission_None(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isEnabled() {
        // This method is essentially static, it is always enabled if LS is!
        return true;
    }

    @Override
    public boolean hasPermission(String playerName, String permission) {
        // Allow OPs to everything
        if(plugin.getServer().getPlayer(playerName).isOp()) {
            return true;
        }
        
        // Allow everyone user & manager
        if(permission.startsWith("localshops.user") || permission.startsWith("localshops.manager")) {
            return true;
        }
        
        return false;
    }

    @Override
    public String getName() {
        return name;
    }

}
