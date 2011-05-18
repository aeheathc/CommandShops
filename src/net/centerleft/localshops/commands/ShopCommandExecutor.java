package net.centerleft.localshops.commands;

import java.util.logging.Logger;

import net.centerleft.localshops.LocalShops;
import net.centerleft.localshops.Search;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommandExecutor implements CommandExecutor {
    
    private final LocalShops plugin;
    private final Logger log = Logger.getLogger("Minecraft");
    
    public ShopCommandExecutor(LocalShops plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        Commands commands = null;
        String type = null;
        String user = "CONSOLE";
        if (sender instanceof Player) {
            user = ((Player) sender).getName();
        }

        if (commandLabel.equalsIgnoreCase("buy")) {
            commands = new Commands(plugin, commandLabel, sender, "buy " + Search.join(args, " "));
            type = "buy";
        } else if (commandLabel.equalsIgnoreCase("sell")) {
            commands = new Commands(plugin, commandLabel, sender, "sell " + Search.join(args, " "));
            type = "sell";
        } else {
            commands = new Commands(plugin, commandLabel, sender, args);
            if (args.length > 0) {
                type = args[0];
            } else {
                return commands.shopHelp();
            }
        }

        log.info(String.format("[%s] %s issued: %s", plugin.getDescription().getName(), user, commands.getCommand()));

        String commandName = command.getName().toLowerCase();

        if (commandName.equalsIgnoreCase("lshop") || commandLabel.equalsIgnoreCase("buy") || commandLabel.equalsIgnoreCase("sell")) {
            if (type.equalsIgnoreCase("search")) {
                return commands.shopSearch();
            } else if (type.equalsIgnoreCase("debug")) {
                return commands.shopDebug();
            } else if (type.equalsIgnoreCase("create")) {
                commands.shopCreate();
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    plugin.playerListener.checkPlayerPosition(player);
                }
                return true;
            } else if (type.equalsIgnoreCase("destroy")) {
                commands.shopDestroy();
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    plugin.playerListener.checkPlayerPosition(player);
                }
                return true;
            } else if (type.equalsIgnoreCase("move")) {
                commands.shopMove();
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    plugin.playerListener.checkPlayerPosition(player);
                }
                return true;
            } else if (type.equalsIgnoreCase("browse") || type.equalsIgnoreCase("bro")) {
                return commands.shopBrowse();
            } else if (type.equalsIgnoreCase("sell")) {
                return commands.shopSell();
            } else if (type.equalsIgnoreCase("add")) {
                return commands.shopAdd();
            } else if (type.equalsIgnoreCase("remove")) {
                return commands.shopRemove();
            } else if (type.equalsIgnoreCase("buy")) {
                return commands.shopBuy();
            } else if (type.equalsIgnoreCase("set")) {
                return commands.shopSet();
            } else if (type.equalsIgnoreCase("select")) {
                return commands.shopSelect();
            } else if (type.equalsIgnoreCase("list")) {
                return commands.shopList();
            } else if (type.equalsIgnoreCase("info")) {
                return commands.shopInfo();
            } else if (type.equalsIgnoreCase("version")) {
                sender.sendMessage(String.format("LocalShops Version %s", plugin.getDescription().getVersion()));
                return true;
            } else {
                return commands.shopHelp();
            }
        }
        return false;
    }

}
