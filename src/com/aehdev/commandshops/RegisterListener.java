package com.aehdev.commandshops;

// Imports for MyPlugin
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;
import com.aehdev.nijikokun.register.payment.Methods;

public class RegisterListener extends ServerListener {
    private Plugin plugin;
    private Methods methods = null;

    public RegisterListener(Plugin plugin) {
        this.plugin = plugin;
        methods = new Methods();
    }

    @Override
    public void onPluginDisable(PluginDisableEvent event) {
        // Check to see if the plugin thats being disabled is the one we are using
        if (methods != null && Methods.hasMethod()) {
            Boolean check = Methods.checkDisabled(event.getPlugin());

            if(check) {
                Methods.reset();
                System.out.println("[CommandShops][Economy] Payment method was disabled. No longer accepting payments.");
            }
        }
    }

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        // Check to see if we need a payment method
        if (!Methods.hasMethod()) {
            if(Methods.setMethod(plugin.getServer().getPluginManager())) {
                if(Methods.hasMethod())
                    System.out.println("[CommandShops][Economy] Payment method found (" + Methods.getMethod().getName() + " version: " + Methods.getMethod().getVersion() + ")");
            }
        }
    }
}