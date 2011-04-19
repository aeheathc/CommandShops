package net.centerleft.localshops;

import java.util.logging.Logger;

import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class ShopsPluginListener extends ServerListener {
    private final LocalShops plugin;

    // Logging
    private final Logger log = Logger.getLogger("Minecraft");

    // Attributes

    protected iConomy iConomy;
    protected Permissions permissions;
    protected PermissionHandler gmPermissionCheck;
    protected boolean usePermissions = false;
    protected boolean useiConomy = false;

    public ShopsPluginListener(LocalShops instance) {
        plugin = instance;
    }

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        if (event.getPlugin().getDescription().getName().equals("iConomy")) {
            iConomy = (iConomy) event.getPlugin();
            System.out.println("LocalShops: Attached to iConomy.");
            useiConomy = true;
            plugin.shopData.currencyName = iConomy.getBank().getCurrency();
        }

        if (event.getPlugin().getDescription().getName().equals("Permissions")) {
            permissions = (Permissions) event.getPlugin();
            gmPermissionCheck = permissions.getHandler();
            log.info(String.format("[%s] %s", plugin.pdfFile.getName(), "Attached to Permissions"));
            usePermissions = true;

        }
    }

    @Override
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin().getDescription().getName().equals("iConomy")) {
            iConomy = null;
            System.out.println("LocalShops: Lost connection to iConomy.");
            useiConomy = false;
        }
        if (event.getPlugin().getDescription().getName().equals("Permissions")) {
            permissions = (Permissions) event.getPlugin();
            System.out.print("LocalShops: Lost connection to Permissions");
            usePermissions = false;
        }
    }
}
