package com.aehdev.commandshops;

// Imports for MyPlugin
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;

import com.nijikokun.register.payment.Methods;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving register events. The class that is
 * interested in processing a register event implements this interface, and the
 * object created with that class is registered with a component using the
 * component's <code>addRegisterListener<code> method. When
 * the register event occurs, that object's appropriate
 * method is invoked.
 * @see RegisterEvent
 */
public class RegisterListener extends ServerListener
{

	/** The plugin. */
	private Plugin plugin;

	/** The methods. */
	private Methods methods = null;

	/**
	 * Instantiates a new register listener.
	 * @param plugin
	 * the plugin
	 */
	public RegisterListener(Plugin plugin)
	{
		this.plugin = plugin;
		methods = new Methods();
	}

	/* (non-Javadoc)
	 * @see
	 * org.bukkit.event.server.ServerListener#onPluginDisable(org.bukkit.event
	 * .server.PluginDisableEvent) */
	@Override
	public void onPluginDisable(PluginDisableEvent event)
	{
		// Check to see if the plugin thats being disabled is the one we are
		// using
		if(methods != null && Methods.hasMethod())
		{
			Boolean check = Methods.checkDisabled(event.getPlugin());

			if(check)
			{
				Methods.reset();
				System.out
						.println("[CommandShops][Economy] Payment method was disabled. No longer accepting payments.");
			}
		}
	}

	/* (non-Javadoc)
	 * @see
	 * org.bukkit.event.server.ServerListener#onPluginEnable(org.bukkit.event
	 * .server.PluginEnableEvent) */
	@Override
	public void onPluginEnable(PluginEnableEvent event)
	{
		// Check to see if we need a payment method
		if(!Methods.hasMethod())
		{
			if(Methods.setMethod(plugin.getServer().getPluginManager()))
			{
				if(Methods.hasMethod()) System.out
						.println("[CommandShops][Economy] Payment method found ("
								+ Methods.getMethod().getName()
								+ " version: "
								+ Methods.getMethod().getVersion() + ")");
			}
		}
	}
}
