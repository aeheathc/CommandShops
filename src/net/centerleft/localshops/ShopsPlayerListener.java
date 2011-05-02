package net.centerleft.localshops;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

import cuboidLocale.BookmarkedResult;
import cuboidLocale.PrimitiveCuboid;

/**
 * Handle events for all Player related events
 * 
 * @author Jonbas
 */
public class ShopsPlayerListener extends PlayerListener {
    private LocalShops plugin;

    // Logging
    private static final Logger log = Logger.getLogger("Minecraft");    
    
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

        // If our user is select & is not holding an item, selection time
        if (plugin.playerData.get(playerName).isSelecting && player.getItemInHand().getType() == Material.AIR) {
            long x, y, z;
            Location loc = event.getClickedBlock().getLocation();
            x = (long) loc.getBlockX();
            y = (long) loc.getBlockY();
            z = (long) loc.getBlockZ();
            
            PlayerData pData = plugin.playerData.get(playerName);
            
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                long[] xyz = { x, y, z };
                pData.setPositionA(xyz);
                if(pData.checkSize()) {
                    player.sendMessage(ChatColor.AQUA + "First Position " + ChatColor.LIGHT_PURPLE + x + " " + y + " " + z + ChatColor.AQUA + " size " + ChatColor.LIGHT_PURPLE + plugin.playerData.get(playerName).getSizeString());
                } else {
                    player.sendMessage(ChatColor.AQUA + "First Position " + ChatColor.LIGHT_PURPLE + x + " " + y + " " + z);
                }
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                long[] xyz = { x, y, z };
                pData.setPositionB(xyz);
                if(pData.checkSize()) {
                    player.sendMessage(ChatColor.AQUA + "Second Position " + ChatColor.LIGHT_PURPLE + x + " " + y + " " + z + ChatColor.AQUA + " size " + ChatColor.LIGHT_PURPLE + plugin.playerData.get(playerName).getSizeString());
                } else {
                    player.sendMessage(ChatColor.AQUA + "Second Position " + ChatColor.LIGHT_PURPLE + x + " " + y + " " + z);
                }
            }
        }

    }
    
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        
        if (!plugin.playerData.containsKey(playerName)) {
            plugin.playerData.put(playerName, new PlayerData(plugin, playerName));
        }

        long x, y, z;
        Location xyz = player.getLocation();
        x = (long) xyz.getBlockX();
        y = (long) xyz.getBlockY();
        z = (long) xyz.getBlockZ();

        checkPlayerPosition(player, x, y, z);        
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
        x = (long) xyz.getBlockX();
        y = (long) xyz.getBlockY();
        z = (long) xyz.getBlockZ();

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
        if (xyz.length == 3) {
            checkPlayerPosition(player, xyz[0], xyz[1], xyz[2]);
        } else {
            log.info(String.format("[%s] Bad Position", plugin.pdfFile.getName()));
        }

    }

    public void checkPlayerPosition(Player player, long x, long y, long z) {
        PlayerData pData = plugin.playerData.get(player.getName());
        BookmarkedResult res = pData.bookmark;
        res = LocalShops.cuboidTree.relatedSearch(res.bookmark, x, y, z);

        // check to see if we've entered any shops
        ArrayList<PrimitiveCuboid> cuboids = (ArrayList<PrimitiveCuboid>) res.results.clone();
        for (PrimitiveCuboid cuboid : cuboids) {

            // for each shop that you find, check to see if we're already in it

            if (cuboid.uuid == null)
                continue;
            if (!cuboid.world.equalsIgnoreCase(player.getWorld().getName()))
                continue;

            Shop shop = plugin.shopData.getShop(cuboid.uuid);
            if(shop == null) {
                // shop no longer exists...remove from cuboid
                res.results.remove(cuboid);
            }
            if (!pData.playerIsInShop(shop)) {
                if (pData.addPlayerToShop(shop)) {
                    notifyPlayerEnterShop(player, shop.getUuid());
                }
            }
        }

        // check to see if we've left any shops
        Iterator<UUID> itr = pData.shopList.iterator();
        while (itr.hasNext()) {
            UUID checkShopUuid = itr.next();
            // check the tree search results to see player is no longer in a
            // shop.
            boolean removeShop = true;
            for (PrimitiveCuboid shop : res.results) {
                if (shop.uuid == checkShopUuid) {
                    removeShop = false;
                    break;
                }
            }
            if (removeShop) {
                itr.remove();
                notifyPlayerLeftShop(player, checkShopUuid);
            }

        }

    }

    private void notifyPlayerLeftShop(Player player, UUID shopUuid) {
        // TODO Add formatting
        Shop shop = plugin.shopData.getShop(shopUuid);
        player.sendMessage(ChatColor.AQUA + "[" + ChatColor.WHITE + "Shop" + ChatColor.AQUA + "] You have left the shop " + ChatColor.WHITE + shop.getName());
    }

    private void notifyPlayerEnterShop(Player player, UUID shopUuid) {
        // TODO Add formatting
        Shop shop = plugin.shopData.getShop(shopUuid);
        player.sendMessage(ChatColor.AQUA + "[" + ChatColor.WHITE + "Shop" + ChatColor.AQUA
                + "] You have entered the shop " + ChatColor.WHITE + shop.getName());

    }

}
