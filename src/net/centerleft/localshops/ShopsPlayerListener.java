package net.centerleft.localshops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import cuboidLocale.BookmarkedResult;
import cuboidLocale.PrimitiveCuboid;

/**
 * Handle events for all Player related events
 * 
 * @author Jonbas
 */
public class ShopsPlayerListener extends PlayerListener {
    private LocalShops plugin;

    public ShopsPlayerListener(LocalShops plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled())
            return;

        Player player = event.getPlayer();
        String playerName = player.getName();
        if (!plugin.playerData.containsKey(playerName)) {
            plugin.playerData.put(playerName, new PlayerData(plugin, playerName));
        }

        if (plugin.playerData.get(playerName).isSelecting) {
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                long[] xyz = { event.getClickedBlock().getX(), event.getClickedBlock().getY(), event.getClickedBlock().getZ() };
                plugin.playerData.get(playerName).setPositionA(xyz);
                player.sendMessage(ChatColor.AQUA + "First Position " + ChatColor.LIGHT_PURPLE + xyz[0] + " " + xyz[1] + " " + xyz[2] + ChatColor.AQUA + " size " + ChatColor.LIGHT_PURPLE + plugin.playerData.get(playerName).getSizeString());
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                long[] xyz = { event.getClickedBlock().getX(), event.getClickedBlock().getY(), event.getClickedBlock().getZ() };
                plugin.playerData.get(playerName).setPositionB(xyz);
                player.sendMessage(ChatColor.AQUA + "Second Position " + ChatColor.LIGHT_PURPLE + xyz[0] + " " + xyz[1] + " " + xyz[2] + ChatColor.AQUA + " size " + ChatColor.LIGHT_PURPLE + plugin.playerData.get(playerName).getSizeString());
            }
        }

    }

    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();

        if (!plugin.playerData.containsKey(playerName)) {
            plugin.playerData.put(playerName, new PlayerData(plugin, playerName));
        }

        long x, y, z;
        Location xyz = event.getTo();
        x = (long) xyz.getX();
        y = (long) xyz.getY();
        z = (long) xyz.getZ();

        checkPlayerPosition(player, x, y, z);
    }

    public void checkPlayerPosition(Player player) {
        long x, y, z;
        Location xyz = player.getLocation();
        x = (long) xyz.getX();
        y = (long) xyz.getY();
        z = (long) xyz.getZ();

        checkPlayerPosition(player, x, y, z);
    }

    public void checkPlayerPosition(Player player, long[] xyz) {
        if (xyz.length > 3) {
            checkPlayerPosition(player, xyz[0], xyz[1], xyz[2]);
        } else {
            System.out.println("LocalShops: Bad position");
        }

    }

    public void checkPlayerPosition(Player player, long x, long y, long z) {
        PlayerData pData = plugin.playerData.get(player.getName());
        BookmarkedResult res = pData.bookmark;
        res = LocalShops.cuboidTree.relatedSearch(res.bookmark, x, y, z);

        // check to see if we've entered any shops
        for (PrimitiveCuboid shop : res.results) {

            // for each shop that you find, check to see if we're already in it

            if (shop.name == null)
                continue;
            if (!shop.world.equalsIgnoreCase(player.getWorld().getName()))
                continue;

            if (!pData.playerIsInShop(shop.name)) {
                if (pData.addPlayerToShop(shop.name)) {
                    notifyPlayerEnterShop(player, shop.name);
                }
            }
        }

        // check to see if we've left any shops
        Iterator<String> itr = pData.shopList.iterator();
        while (itr.hasNext()) {
            String checkShopName = itr.next();
            // check the tree search results to see player is no longer in a
            // shop.
            boolean removeShop = true;
            for (PrimitiveCuboid shop : res.results) {
                if (shop.name.equalsIgnoreCase(checkShopName)) {
                    removeShop = false;
                    break;
                }
            }
            if (removeShop) {
                itr.remove();
                notifyPlayerLeftShop(player, checkShopName);
            }

        }

    }

    private static void notifyPlayerLeftShop(Player player, String shopName) {
        // TODO Add formatting
        player.sendMessage(ChatColor.AQUA + "[" + ChatColor.WHITE + "Shop" + ChatColor.AQUA
                + "] You have left the shop " + ChatColor.WHITE + shopName);
    }

    private static void notifyPlayerEnterShop(Player player, String shopName) {
        // TODO Add formatting
        player.sendMessage(ChatColor.AQUA + "[" + ChatColor.WHITE + "Shop" + ChatColor.AQUA
                + "] You have entered the shop " + ChatColor.WHITE + shopName);

    }

}
