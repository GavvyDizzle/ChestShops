package com.github.gavvydizzle.chestshops.commands.admin;

import com.github.gavvydizzle.chestshops.commands.AdminCommandManager;
import com.github.gavvydizzle.chestshops.shops.ShopManager;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class RemoveInvalidShopsCommand extends SubCommand {

    private final ShopManager shopManager;

    public RemoveInvalidShopsCommand(AdminCommandManager adminCommandManager, ShopManager shopManager) {
        this.shopManager = shopManager;

        setName("removeInvalidShops");
        setDescription("Removes any shops that exist without a sign attached");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " removeInvalidShops");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(adminCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        int invalidCount = shopManager.validateShops();

        if (invalidCount > 0) {
            sender.sendMessage(ChatColor.RED + "[ChestShops] " + invalidCount + " invalid shops were found and deleted! Check the console the location of these shops");
        }
        else {
            sender.sendMessage(ChatColor.GREEN + "[ChestShops] All loaded shops are valid");
        }
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}