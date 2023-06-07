package com.github.gavvydizzle.chestshops;

import com.github.gavvydizzle.chestshops.shops.RemoveOnShopUnclaim;
import com.github.gavvydizzle.chestshops.commands.AdminCommandManager;
import com.github.gavvydizzle.chestshops.commands.PlayerCommandManager;
import com.github.gavvydizzle.chestshops.database.Database;
import com.github.gavvydizzle.chestshops.database.SQLite;
import com.github.gavvydizzle.chestshops.shops.ShopManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class ChestShops extends JavaPlugin {
    private static ChestShops instance;
    private static Economy economy;
    private Database database;

    private ShopManager shopManager;

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            Bukkit.getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getConfig().options().copyDefaults();
        saveDefaultConfig();

        instance = this;

        database = new SQLite(instance);
        database.load();

        shopManager = new ShopManager();

        getServer().getPluginManager().registerEvents(shopManager, this);
        getServer().getPluginManager().registerEvents(new RemoveOnShopUnclaim(shopManager), this);

        new AdminCommandManager(getCommand("chestshopsadmin"), shopManager);
        new PlayerCommandManager(getCommand("chestshops"), shopManager);
    }

    @Override
    public void onDisable() {
        if (shopManager != null) {
            shopManager.onServerShutdown();
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return true;
    }

    public static ChestShops getInstance() {
        return instance;
    }

    public static Economy getEconomy() {
        return economy;
    }

    public Database getDatabase() {
        return database;
    }

    // Needed because I'm too lazy to make a proper API
    public ShopManager getShopManager() {
        return shopManager;
    }
}
