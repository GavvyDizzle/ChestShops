package com.github.gavvydizzle.chestshops.commands;

import com.github.gavvydizzle.chestshops.shops.ShopManager;
import com.github.mittenmc.serverutils.CommandManager;
import com.github.mittenmc.serverutils.SubCommand;
import com.github.gavvydizzle.chestshops.commands.player.PlayerHelpCommand;
import com.github.gavvydizzle.chestshops.commands.player.ShopTutorialCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class PlayerCommandManager extends CommandManager {

    public PlayerCommandManager(PluginCommand command, ShopManager shopManager) {
        super(command);

        registerCommand(new PlayerHelpCommand(this));
        registerCommand(new ShopTutorialCommand(this, shopManager));
        sortCommands();
    }
}