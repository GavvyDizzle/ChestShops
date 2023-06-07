package com.github.gavvydizzle.chestshops.commands.admin;

import com.github.gavvydizzle.chestshops.ChestShops;
import com.github.gavvydizzle.chestshops.commands.AdminCommandManager;
import com.github.gavvydizzle.chestshops.shops.ShopManager;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ReloadCommand extends SubCommand {

    private final ShopManager shopManager;

    public ReloadCommand(AdminCommandManager adminCommandManager, ShopManager shopManager) {
        this.shopManager = shopManager;

        setName("reload");
        setDescription("Reloads this plugin");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " reload");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(adminCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        try {
            ChestShops.getInstance().reloadConfig();
            shopManager.reload();
            sender.sendMessage(ChatColor.GREEN + "[ChestShops] Reloaded");
        }
        catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to reload ChestShops. Check the console for errors");
            ChestShops.getInstance().getLogger().severe("Failed when reloading ChestShops");
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
