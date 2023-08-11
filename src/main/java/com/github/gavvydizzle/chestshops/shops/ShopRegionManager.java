package com.github.gavvydizzle.chestshops.shops;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.block.Block;

public class ShopRegionManager {

    private static StateFlag CHEST_SHOPS;
    private RegionQuery query;

    public static void initFlags() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            // create a flag defaulting to true
            StateFlag flag = new StateFlag("create-chest-shops", false);
            registry.register(flag);
            CHEST_SHOPS = flag; // only set our field if there was no error
        } catch (FlagConflictException e) {
            // some other plugin registered a flag by the same name already.
            // you can use the existing flag, but this may cause conflicts - be sure to check type
            Flag<?> existing = registry.get("create-chest-shops");
            if (existing instanceof StateFlag) {
                CHEST_SHOPS = (StateFlag) existing;
            }
            // types don't match - this is bad news! some other plugin conflicts with you
            // hopefully this never actually happens
        }
    }

    private void initQuery() {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        query = container.createQuery();
    }

    public boolean isNotInValidRegion(Block block) {
        if (query == null) {
            initQuery();
        }

        return !query.testState(BukkitAdapter.adapt(block.getLocation()), null, CHEST_SHOPS);
    }

}