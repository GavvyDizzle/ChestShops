package com.github.gavvydizzle.chestshops.commands.admin;

import com.github.gavvydizzle.chestshops.commands.AdminCommandManager;
import com.github.gavvydizzle.chestshops.shops.ShopManager;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class UpdateAllSignsCommand extends SubCommand {

    private final ShopManager shopManager;

    public UpdateAllSignsCommand(AdminCommandManager adminCommandManager, ShopManager shopManager) {
        this.shopManager = shopManager;

        setName("updateSigns");
        setDescription("Updates the text on all sign shops");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " updateSigns");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(adminCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        int invalidCount = shopManager.reloadAllShopSigns();

        if (invalidCount > 0) {
            sender.sendMessage(ChatColor.RED + "[ChestShops] " + invalidCount + " shops were deleted due to invalid signs! Check the console the location of these shops");
        }
        else {
            sender.sendMessage(ChatColor.GREEN + "[ChestShops] All signs reloaded successfully");
        }
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
