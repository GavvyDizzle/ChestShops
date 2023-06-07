package com.github.gavvydizzle.chestshops.commands;

import com.github.gavvydizzle.chestshops.shops.ShopManager;
import com.github.mittenmc.serverutils.CommandManager;
import com.github.mittenmc.serverutils.SubCommand;
import com.github.gavvydizzle.chestshops.commands.admin.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class AdminCommandManager extends CommandManager {

    public AdminCommandManager(PluginCommand pluginCommand, ShopManager shopManager) {
        super(pluginCommand);

        registerCommand(new FixHologramCommand(this, shopManager));
        registerCommand(new AdminHelpCommand(this));
        registerCommand(new ReloadCommand(this, shopManager));
        registerCommand(new RemoveInvalidShopsCommand(this, shopManager));
        registerCommand(new UpdateAllSignsCommand(this, shopManager));
        sortCommands();
    }
}