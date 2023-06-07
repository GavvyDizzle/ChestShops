package com.github.gavvydizzle.chestshops.commands.player;

import com.github.gavvydizzle.chestshops.commands.PlayerCommandManager;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class PlayerHelpCommand extends SubCommand {

    private final PlayerCommandManager commandManager;

    public PlayerHelpCommand(PlayerCommandManager commandManager) {
        this.commandManager = commandManager;

        setName("help");
        setDescription("Opens this help menu");
        setSyntax("/" + commandManager.getCommandDisplayName() + " help");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(commandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        sender.sendMessage(ChatColor.GOLD +  "-----(Shop Commands)-----");
        ArrayList<SubCommand> subCommands = commandManager.getSubcommands();
        for (SubCommand subCommand : subCommands) {
            sender.sendMessage(ChatColor.GOLD + subCommand.getSyntax() + " - " + ChatColor.YELLOW + subCommand.getDescription());
        }
        sender.sendMessage(ChatColor.GOLD +  "-----(Shop Commands)-----");
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
