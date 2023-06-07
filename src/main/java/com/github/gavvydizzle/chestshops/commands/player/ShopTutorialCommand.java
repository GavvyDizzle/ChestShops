package com.github.gavvydizzle.chestshops.commands.player;

import com.github.gavvydizzle.chestshops.commands.PlayerCommandManager;
import com.github.gavvydizzle.chestshops.shops.ShopManager;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ShopTutorialCommand extends SubCommand {

    private final ShopManager shopManager;

    public ShopTutorialCommand(PlayerCommandManager commandManager, ShopManager shopManager) {
        this.shopManager = shopManager;

        setName("tutorial");
        setDescription("Prints out a sign shop tutorial to your chat");
        setSyntax("/" + commandManager.getCommandDisplayName() + " tutorial");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(commandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        for (String str : shopManager.getTutorialMessages()) {
            sender.sendMessage(str);
        }
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}