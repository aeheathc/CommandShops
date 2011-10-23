package com.aehdev.nijikokun.register.listeners;

// Imports for MyPlugin
import com.aehdev.nijikokun.register.Register;
import com.aehdev.nijikokun.register.payment.Methods;

// Bukkit Imports
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;

public class server extends ServerListener {
    private Register plugin;
    private Methods Methods = null;

    public server(Register plugin) {
        this.plugin = plugin;
        this.Methods = new Methods();
    }

    @Override
    public void onPluginDisable(PluginDisableEvent event) {
        // Check to see if the plugin thats being disabled is the one we are using
        if (this.Methods != null && com.aehdev.nijikokun.register.payment.Methods.hasMethod()) {
            Boolean check = com.aehdev.nijikokun.register.payment.Methods.checkDisabled(event.getPlugin());

            if(check) {
                com.aehdev.nijikokun.register.payment.Methods.reset();
                System.out.println("[" + plugin.info.getName() + "] Payment method was disabled. No longer accepting payments.");
            }
        }
    }

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        // Check to see if we need a payment method
        if (!com.aehdev.nijikokun.register.payment.Methods.hasMethod()) {
            if(com.aehdev.nijikokun.register.payment.Methods.setMethod(plugin.getServer().getPluginManager())) {
                if(com.aehdev.nijikokun.register.payment.Methods.hasMethod())
                    System.out.println("[" + plugin.info.getName() + "] Payment method found (" + com.aehdev.nijikokun.register.payment.Methods.getMethod().getName() + " version: " + com.aehdev.nijikokun.register.payment.Methods.getMethod().getVersion() + ")");
            }
        }
    }
}