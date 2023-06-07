package com.github.gavvydizzle.chestshops.commands.admin;

import com.github.gavvydizzle.chestshops.commands.AdminCommandManager;
import com.github.gavvydizzle.chestshops.shops.ChestShop;
import com.github.gavvydizzle.chestshops.shops.ShopManager;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class FixHologramCommand extends SubCommand {

    private final ShopManager shopManager;

    public FixHologramCommand(AdminCommandManager adminCommandManager, ShopManager shopManager) {
        this.shopManager = shopManager;

        setName("fixHologram");
        setDescription("Regenerates the hologram for shop you are looking at");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " fixHologram");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(adminCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;

        ChestShop shop = shopManager.getShopFromContainer(player.getTargetBlock(null, 10).getLocation());
        if (shop == null) {
            sender.sendMessage(ChatColor.RED + "You must be looking at a valid (and active) shop container");
            return;
        }

        shop.removeShopHologram();
        shop.placeShopHologram();
        sender.sendMessage(ChatColor.GREEN + "The hologram for this shop should now appear above the shop");
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender commandSender, String[] strings) {
        return new ArrayList<>();
    }
}