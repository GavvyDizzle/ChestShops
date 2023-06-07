package com.github.gavvydizzle.chestshops.shops;

import com.github.gavvydizzle.chestshops.ChestShops;
import com.github.gavvydizzle.rentableregions.events.PlayerExitShopEvent;
import com.github.gavvydizzle.rentableregions.events.ShopUnclaimEvent;
import com.github.gavvydizzle.rentableregions.shop.Shop;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Handles removing chest shops when a shop unclaims
 */
public class RemoveOnShopUnclaim implements Listener {

    private final ShopManager shopManager;

    public RemoveOnShopUnclaim(ShopManager shopManager) {
        this.shopManager = shopManager;
    }

    @EventHandler
    public void onUnclaim(ShopUnclaimEvent e) {
        removeAllChestShops(e.getShop(), e.getWorld());
    }

    @EventHandler
    public void onExit(PlayerExitShopEvent e) {
        removePlayerChestShops(e.getWhoLeft(), e.getShop(), e.getWorld());
    }

    private void removeAllChestShops(Shop shop, World world) {
        Bukkit.getScheduler().runTaskAsynchronously(ChestShops.getInstance(), () -> {
            HashSet<ChestShop> shopsToDelete = new HashSet<>();

            for (ChestShop chestShop : shopManager.getAllShops()) {
                // Make sure the world is the same
                if (!(chestShop.getContainerLocation().getWorld() != null && chestShop.getContainerLocation().getWorld().getUID().equals(world.getUID()))) continue;

                Location loc = chestShop.getContainerLocation();
                for (ProtectedRegion region : shop.getRegions()) {
                    if (region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
                        shopsToDelete.add(chestShop);
                        break;
                    }
                }
            }

            if (shopsToDelete.size() > 0) Bukkit.getScheduler().runTask(ChestShops.getInstance(), () -> shopManager.deleteShops(shopsToDelete));
        });
    }

    private void removePlayerChestShops(OfflinePlayer offlinePlayer, Shop shop, World world) {
        Bukkit.getScheduler().runTaskAsynchronously(ChestShops.getInstance(), () -> {
            HashSet<ChestShop> shopsToDelete = new HashSet<>();

            for (ChestShop chestShop : shopManager.getAllShops()) {
                // Make sure the world is the same
                if (!(chestShop.getContainerLocation().getWorld() != null && chestShop.getContainerLocation().getWorld().getUID().equals(world.getUID()))) continue;

                // Make sure this player is the owner of the shop
                if (!chestShop.getUuid().equals(offlinePlayer.getUniqueId())) continue;

                Location loc = chestShop.getContainerLocation();
                for (ProtectedRegion region : shop.getRegions()) {
                    if (region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
                        shopsToDelete.add(chestShop);
                        break;
                    }
                }
            }

            if (shopsToDelete.size() > 0) Bukkit.getScheduler().runTask(ChestShops.getInstance(), () -> shopManager.deleteShops(shopsToDelete));
        });
    }
}
