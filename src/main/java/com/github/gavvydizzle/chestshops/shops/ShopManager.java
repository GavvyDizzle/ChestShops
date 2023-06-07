package com.github.gavvydizzle.chestshops.shops;

import com.github.gavvydizzle.rentableregions.api.RentableRegionsAPI;
import com.github.gavvydizzle.rentableregions.shop.Shop;
import com.github.mittenmc.serverutils.Colors;
import com.github.mittenmc.serverutils.Numbers;
import com.github.mittenmc.serverutils.PlayerNameCache;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.libs.net.kyori.adventure.text.Component;
import com.loohp.interactivechat.libs.net.kyori.adventure.text.format.NamedTextColor;
import com.github.gavvydizzle.chestshops.ChestShops;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.*;

public class ShopManager implements Listener {

    private final RentableRegionsAPI rentableRegionsAPI;
    private List<String> shopWorlds, tutorialMessages;
    private final HashMap<Location, ChestShop> chestShopHashMap; // Container Location, ChestShop
    private final HashSet<UUID> playersPreviewingShops, playersBypassingShopMessages;

    DecimalFormat decimalFormat = new DecimalFormat("0.#####");
    private long maxShopPrice;
    private String boughtItemMessage, shopBoughtItemMessage, soldItemMessage, shopSoldItemMessage;
    private final String[] queuedShopLines, buyShopLines, sellShopLines;

    public ShopManager() {
        rentableRegionsAPI = RentableRegionsAPI.getInstance();

        shopWorlds = new ArrayList<>();
        tutorialMessages = new ArrayList<>();
        chestShopHashMap = new HashMap<>();
        playersPreviewingShops = new HashSet<>();
        playersBypassingShopMessages = new HashSet<>();

        queuedShopLines = new String[4];
        buyShopLines = new String[4];
        sellShopLines = new String[4];

        reload();
        loadShops();
    }

    public void reload() {
        FileConfiguration config = ChestShops.getInstance().getConfig();
        config.options().copyDefaults(true);
        config.addDefault("enabledWorlds", new ArrayList<>());
        config.addDefault("maxShopPrice", 5000000000L);
        config.addDefault("messages.boughtItem", "&aYou bought &c{amount}x {item_name} &a for ${price} from {owner_name}");
        config.addDefault("messages.shopBoughtItem", "&9{customer_name} &abought &c{amount}x {item_name} from you for ${price}");
        config.addDefault("messages.soldItem", "&aYou sold &c{amount}x {item_name} &a for ${price} to {owner_name}");
        config.addDefault("messages.shopSoldItem", "&9{customer_name} &asold &c{amount}x {item_name} to you for ${price}");
        config.addDefault("tutorial", new ArrayList<>());
        config.addDefault("shopSign.queuedLines.1", "");
        config.addDefault("shopSign.queuedLines.2", "");
        config.addDefault("shopSign.queuedLines.3", "");
        config.addDefault("shopSign.queuedLines.4", "");
        config.addDefault("shopSign.buyShopLines.1", "");
        config.addDefault("shopSign.buyShopLines.2", "");
        config.addDefault("shopSign.buyShopLines.3", "");
        config.addDefault("shopSign.buyShopLines.4", "");
        config.addDefault("shopSign.sellShopLines.1", "");
        config.addDefault("shopSign.sellShopLines.2", "");
        config.addDefault("shopSign.sellShopLines.3", "");
        config.addDefault("shopSign.sellShopLines.4", "");
        ChestShops.getInstance().saveConfig();

        shopWorlds = config.getStringList("enabledWorlds");
        maxShopPrice = config.getLong("maxShopPrice");
        boughtItemMessage = Colors.conv(config.getString("messages.boughtItem"));
        shopBoughtItemMessage = Colors.conv(config.getString("messages.shopBoughtItem"));
        soldItemMessage = Colors.conv(config.getString("messages.soldItem"));
        shopSoldItemMessage = Colors.conv(config.getString("messages.shopSoldItem"));

        tutorialMessages = Colors.conv(config.getStringList("tutorial"));

        queuedShopLines[0] = Colors.conv(config.getString("shopSign.queuedLines.1"));
        queuedShopLines[1] = Colors.conv(config.getString("shopSign.queuedLines.2"));
        queuedShopLines[2] = Colors.conv(config.getString("shopSign.queuedLines.3"));
        queuedShopLines[3] = Colors.conv(config.getString("shopSign.queuedLines.4"));
        buyShopLines[0] = Colors.conv(config.getString("shopSign.buyShopLines.1"));
        buyShopLines[1] = Colors.conv(config.getString("shopSign.buyShopLines.2"));
        buyShopLines[2] = Colors.conv(config.getString("shopSign.buyShopLines.3"));
        buyShopLines[3] = Colors.conv(config.getString("shopSign.buyShopLines.4"));
        sellShopLines[0] = Colors.conv(config.getString("shopSign.sellShopLines.1"));
        sellShopLines[1] = Colors.conv(config.getString("shopSign.sellShopLines.2"));
        sellShopLines[2] = Colors.conv(config.getString("shopSign.sellShopLines.3"));
        sellShopLines[3] = Colors.conv(config.getString("shopSign.sellShopLines.4"));
    }

    public void loadShops() {
        //Call this async because database updates here don't need to be on the main thread
        Bukkit.getScheduler().runTaskAsynchronously(ChestShops.getInstance(), () -> {
            for (ChestShop shop : ChestShops.getInstance().getDatabase().loadShops()) {
                chestShopHashMap.put(shop.getContainerLocation(), shop);

                if (shop.getContainerLocation().getBlock().getRelative(BlockFace.UP).getType() == Material.AIR) {
                    shop.placeShopHologram();
                }
            }
        });
    }

    public void saveShop(ChestShop shop) {
        chestShopHashMap.put(shop.getContainerLocation(), shop);

        if (!shop.isQueueShop()) {
            Bukkit.getScheduler().runTaskAsynchronously(ChestShops.getInstance(), () -> ChestShops.getInstance().getDatabase().saveShop(shop));
        }
    }

    public void deleteShop(ChestShop shop) {
        shop.removeShopHologram();
        shop.removeSign();
        chestShopHashMap.remove(shop.getContainerLocation());

        if (!shop.isQueueShop()) {
            Bukkit.getScheduler().runTaskAsynchronously(ChestShops.getInstance(), () -> ChestShops.getInstance().getDatabase().deleteShop(shop));
        }
    }

    public void deleteShops(Collection<ChestShop> shops) {
        if (shops.isEmpty()) return;

        ArrayList<ChestShop> newShopList = new ArrayList<>();
        for (ChestShop shop : shops) {
            shop.removeSign();
            chestShopHashMap.remove(shop.getContainerLocation());

            if (!shop.isQueueShop()) {
                shop.removeShopHologram();
                newShopList.add(shop);
            }
        }

        if (!newShopList.isEmpty()) {
            Bukkit.getScheduler().runTaskAsynchronously(ChestShops.getInstance(), () -> ChestShops.getInstance().getDatabase().deleteShops(newShopList));
        }
    }


    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null || e.getPlayer().isSneaking()) return;
        Player player = e.getPlayer();

        if (e.getClickedBlock() == null || !(e.getClickedBlock().getType().name().contains("WALL_SIGN")) || e.getPlayer().isSneaking()) {
            return;
        }

        if (e.getClickedBlock().getType().name().contains("WALL_SIGN")) {
            ChestShop shop = getShopFromWallSign(e.getClickedBlock());
            if (shop == null) return;

            if (shop.isQueueShop() && shop.getUuid() == e.getPlayer().getUniqueId()) {
                if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    onQueuedShopClick(e.getPlayer(), shop, e.getClickedBlock());
                }
            }
            // Preview shop contents
            else if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                onShopPreview(player, shop);
            }
            // Using the shop to buy/sell items
            else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                onShopUse(player, shop, e.getAction());
            }
        }
    }

    private void onShopPreview(Player player, ChestShop shop) {
        if (shop.isQueueShop()) return;

        if (isInvalidContainer(shop.getContainerLocation().getBlock())) {
            player.sendMessage(ChatColor.RED + "This shop has an invalid container. Please alert an admin!");
            ChestShops.getInstance().getLogger().warning("The shop at " + shop.getContainerLocation() + " has an invalid container!");
            return;
        }

        playersPreviewingShops.add(player.getUniqueId());

        Container chest = (Container) shop.getContainerLocation().getBlock().getState();
        Inventory chestInv = chest.getInventory();
        Inventory inventory = Bukkit.createInventory(player, chestInv.getSize(), Colors.conv("Chest Shop"));
        inventory.setContents(chestInv.getContents());

        player.openInventory(inventory);

        try {
            Component component = Component.text("Shop Item: ")
                    .color(NamedTextColor.GREEN)
                    .append(InteractiveChatAPI.createItemDisplayComponent(player, shop.getItem()));
            InteractiveChatAPI.sendMessage(player, component);
        } catch (Exception ignored) {}
    }

    private void onQueuedShopClick(Player player, ChestShop shop, Block sign) {
        if (!sign.getLocation().equals(shop.getSignLocation())) {
            player.sendMessage(ChatColor.RED + "This shop does not belong to you");
            return;
        }

        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You must be holding an item to make a shop");
            return;
        }

        if (shop.getAmount() > heldItem.getMaxStackSize()) {
            player.sendMessage(Colors.conv("&cSelling amount cannot be more than maximum stack size!"));
            sign.breakNaturally();
            chestShopHashMap.remove(shop.getContainerLocation());
            return;
        }

        Sign blockSign = (Sign) sign.getState();
        String[] arr = shop.getType() == ShopType.BUY ? buyShopLines : sellShopLines;

        blockSign.setLine(0, fillShopSignPlaceholders(arr[0], player, shop));
        blockSign.setLine(1, fillShopSignPlaceholders(arr[1], player, shop));
        blockSign.setLine(2, fillShopSignPlaceholders(arr[2], player, shop));
        blockSign.setLine(3, fillShopSignPlaceholders(arr[3], player, shop));
        blockSign.update();

        shop.setItem(heldItem);
        shop.setQueueShop(false);
        if (shop.getContainerLocation().getBlock().getRelative(BlockFace.UP).getType() == Material.AIR) {
            shop.placeShopHologram();
        }
        saveShop(shop);
    }

    private void onShopUse(Player player, ChestShop shop, Action action) {
        if (shop.isQueueShop()) return;

        if (shop.getUuid().equals(player.getUniqueId())) {
            player.sendMessage(Colors.conv("&cYou cannot use your own shop!"));
            return;
        }

        if (isInvalidContainer(shop.getContainerLocation().getBlock())) {
            player.sendMessage(ChatColor.RED + "This shop has an invalid container. Please alert an admin!");
            ChestShops.getInstance().getLogger().warning("The shop at " + shop.getContainerLocation() + " has an invalid container!");
            return;
        }

        Container chest = (Container) shop.getContainerLocation().getBlock().getState();
        Inventory chestInventory = chest.getInventory();

        if (action == Action.RIGHT_CLICK_BLOCK) {
            if (shop.getType() == ShopType.SELL) { // The shop is looking to buy something
                if (ChestShops.getEconomy().getBalance(Bukkit.getOfflinePlayer(shop.getUuid())) < shop.getPrice()) {
                    player.sendMessage(Colors.conv("&cPlayer does not have enough funds!"));
                    return;
                }

                if (!chestHasSpace(chestInventory, shop.getItem(), shop.getAmount())) {
                    player.sendMessage(Colors.conv("&cPlayer's shop is full!"));
                    return;
                }

                int amount = shop.getAmount();
                ItemStack shopItem = shop.getItem().clone();
                shopItem.setAmount(amount);
                if (!player.getInventory().containsAtLeast(shop.getItem(), amount)) {
                    player.sendMessage(Colors.conv("&cYou don't have enough of that item to sell!"));
                    return;
                }

                int amountItem = 0;
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item == null) {
                        continue;
                    }
                    if (item.isSimilar(shopItem)) {
                        amountItem += item.getAmount();
                    }
                }
                player.getInventory().removeItem(shopItem);
                int amt = 0;
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item == null) {
                        continue;
                    }
                    if (item.isSimilar(shopItem)) {
                        amt += item.getAmount();
                    }
                }

                ItemStack stack = shop.getItem().clone();
                stack.setAmount(amountItem - amt);
                chest.getInventory().addItem(stack);

                double sellAmount = shop.getPrice();

                OfflinePlayer seller = Bukkit.getOfflinePlayer(shop.getUuid());
                player.sendMessage(fillMessagePlaceholders(soldItemMessage, seller, player, shop.getAmount(), shop.getPrice(), shop.getItem()));
                if (seller.getPlayer() != null && !playersBypassingShopMessages.contains(seller.getUniqueId())) {
                    seller.getPlayer().sendMessage(fillMessagePlaceholders(shopSoldItemMessage, seller, player, shop.getAmount(), shop.getPrice(), shop.getItem()));
                }

                ChestShops.getEconomy().withdrawPlayer(seller, sellAmount);
                ChestShops.getEconomy().depositPlayer(player, sellAmount);

            } else if (shop.getType() == ShopType.BUY) { // The shop is looking to sell something
                if (ChestShops.getEconomy().getBalance(player) < shop.getPrice()) {
                    player.sendMessage(Colors.conv("&cYou do not have enough money!"));
                    return;
                }
                for (int slot = 0; slot < chestInventory.getSize(); slot++) {
                    ItemStack item = chestInventory.getItem(slot);
                    if (item == null) {
                        continue;
                    }
                    if (item.isSimilar(shop.getItem()) && item.getAmount() >= shop.getAmount()) {
                        if (hasSpace(player.getInventory(), shop.getItem(), shop.getAmount())) {

                            OfflinePlayer seller = Bukkit.getOfflinePlayer(shop.getUuid());
                            player.sendMessage(fillMessagePlaceholders(boughtItemMessage, seller, player, shop.getAmount(), shop.getPrice(), shop.getItem()));
                            if (seller.getPlayer() != null && !playersBypassingShopMessages.contains(seller.getUniqueId())) {
                                seller.getPlayer().sendMessage(fillMessagePlaceholders(shopBoughtItemMessage, seller, player, shop.getAmount(), shop.getPrice(), shop.getItem()));
                            }

                            ChestShops.getEconomy().withdrawPlayer(player, shop.getPrice());
                            ChestShops.getEconomy().depositPlayer(seller, shop.getPrice());

                            ItemStack newPlayerItem = item.clone();
                            newPlayerItem.setAmount(shop.getAmount());
                            player.getInventory().addItem(newPlayerItem);
                            item.setAmount(item.getAmount() - shop.getAmount());
                        } else {
                            player.sendMessage(Colors.conv("&cYour inventory does not have enough space!"));
                        }
                        return;
                    }
                }

                player.sendMessage(Colors.conv("&cShop out of stock!"));
            }
        }
    }

    @EventHandler
    public void onPlace(SignChangeEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();

        // Not a sign or in an invalid world
        if (!block.getType().toString().contains("WALL_SIGN")) return;
        if (!shopWorlds.contains(block.getWorld().getName().toLowerCase())) return;

        // Shop not connected to valid container
        Block containerBlock = getAttachedBlock(e.getBlock());
        if (containerBlock == null) return;
        if (isInvalidContainer(containerBlock)) return;

        Bukkit.getScheduler().scheduleSyncDelayedTask(ChestShops.getInstance(), () -> {
            Sign blockSign = (Sign) block.getState();

            ShopType shopType = getShopTypeFromString(blockSign.getLine(0));
            if (shopType == null) {
                e.getPlayer().sendMessage(ChatColor.YELLOW + "Note: The first line must have \"buy\" or \"sell\" in it for a shop to be made!");
                return;
            }

            int sellAmount;
            try {
                sellAmount = Integer.parseInt(blockSign.getLine(1));
            } catch (Exception ignored) {
                e.getPlayer().sendMessage(ChatColor.RED + "(Line 2) Your sell amount of '" + blockSign.getLine(1) + "' is not a valid amount. Please provide a number");
                blockSign.getBlock().breakNaturally();
                return;
            }
            if (sellAmount <= 0 || sellAmount > 64) {
                e.getPlayer().sendMessage(ChatColor.RED + "(Line 2) Your sell amount of '" + blockSign.getLine(1) + "' is not a valid amount. Please provide a number 1 to 64");
                blockSign.getBlock().breakNaturally();
                return;
            }

            double sellPrice;
            try {
                sellPrice = Double.parseDouble(blockSign.getLine(2));
            } catch (Exception ignored) {
                e.getPlayer().sendMessage(ChatColor.RED + "(Line 3) Your sell price of '" + blockSign.getLine(2) + "' is not a valid amount. Please provide a number");
                blockSign.getBlock().breakNaturally();
                return;
            }
            if (sellPrice <= 0 || sellPrice - 0.001 > maxShopPrice) {
                e.getPlayer().sendMessage(ChatColor.RED + "(Line 3) Your sell amount of '" + blockSign.getLine(2) + "' is invalid. The range is $0.01 - $" + Numbers.withSuffix(maxShopPrice));
                blockSign.getBlock().breakNaturally();
                return;
            }

            // Shop already exists //TODO - Might need outside task
            if (chestShopHashMap.get(containerBlock.getLocation()) != null) {
                e.getBlock().breakNaturally();
                player.sendMessage(Colors.conv("&cThere's already a shop here!"));
                return;
            }

            /*
            // Player has another shop queued
            if (queuedShops.containsKey(player.getUniqueId())) {
                e.getBlock().breakNaturally();
                player.sendMessage(Colors.conv("&cYou must finish making your last shop before making a new one!"));
                return;
            }
             */

            ChestShop shop = new ChestShop(sellAmount, sellPrice, shopType, player.getUniqueId(), blockSign.getLocation());
            shop.setContainerLocation(Objects.requireNonNull(getAttachedBlock(shop.getSignLocation().getBlock())).getLocation());
            shop.setQueueShop(true);
            chestShopHashMap.put(shop.getContainerLocation(), shop);

            blockSign.setLine(0, queuedShopLines[0]);
            blockSign.setLine(1, queuedShopLines[1]);
            blockSign.setLine(2, queuedShopLines[2]);
            blockSign.setLine(3, queuedShopLines[3]);
            blockSign.update();
        }, 0);
    }

    // Break the shop when its sign or container was broken
    // If the player has admin perms, or is the shop owner, or is the cell owner the shop is in, let them break it
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (!shopWorlds.contains(e.getPlayer().getWorld().getName().toLowerCase())) return;

        ChestShop chestShop;
        if (e.getBlock().getType().toString().contains("WALL_SIGN")) {
            chestShop = getShopFromWallSign(e.getBlock());
        }
        else {
            chestShop = getShopFromContainer(e.getBlock().getLocation());
        }

        // See if the block below this is a chest shop to hide the hologram
        if (chestShop == null) {
            chestShop = getShopFromContainer(e.getBlock().getRelative(BlockFace.DOWN).getLocation());
            if (chestShop == null) return;

            chestShop.placeShopHologram();
        }
        else {
            // Delete the shop if the breaker is an admin, shop owner, or cell owner
            Shop shop = rentableRegionsAPI.getShop(e.getBlock());
            if (e.getPlayer().hasPermission("chestshops.admin") || e.getPlayer().getUniqueId().equals(chestShop.getUuid()) || (shop != null && shop.isOwner(e.getPlayer().getUniqueId()))) {
                chestShop.dropSign();
                deleteShop(chestShop);
                return;
            }

            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        ChestShop chestShop = getShopFromContainer(e.getBlock().getRelative(BlockFace.DOWN).getLocation());
        if (chestShop == null) return;

        chestShop.removeShopHologram();
    }

    @EventHandler
    public void onChestInventoryInteract(InventoryClickEvent e) {
        if (playersPreviewingShops.contains(e.getWhoClicked().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        playersPreviewingShops.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        playersBypassingShopMessages.remove(e.getPlayer().getUniqueId());

        Set<ChestShop> shops = new HashSet<>(chestShopHashMap.values());
        for (ChestShop shop : shops) {
            if (shop.isQueueShop() && shop.getUuid() == e.getPlayer().getUniqueId()) {
                chestShopHashMap.remove(shop.getContainerLocation());
                shop.removeSign();
            }
        }
    }

    /**
     * Checks to see if all loaded shops are valid
     * @return The number of invalid shops
     */
    public int validateShops() {
        int count = 0;

        Set<ChestShop> shops = new HashSet<>(chestShopHashMap.values());
        for (ChestShop shop : shops) {
            if (!isShopValid(shop)) count++;
        }

        return count;
    }

    /**
     * Determines if this shop is valid by checking multiple things about the shop. If the shop is invalid, it is deleted.
     * @return True if the shop is valid
     */
    public boolean isShopValid(ChestShop shop) {
        if (isInvalidContainer(shop.getContainerLocation().getBlock())) {
            ChestShops.getInstance().getLogger().warning("The shop located at " + shop.getContainerLocation() + " does not have a valid container! It is being deleted");
            deleteShop(shop);
            return false;
        }
        else if (!shop.getSignLocation().getBlock().getType().toString().contains("WALL_SIGN")) {
            ChestShops.getInstance().getLogger().warning("The shop located at " + shop.getContainerLocation() + " does not have a valid shop sign! It is being deleted");
            deleteShop(shop);
            return false;
        }

        return true;
    }

    /**
     * Reloads the signs of every loaded sign shop.
     * @return The number of sign shops that were deleted (if any didn't have a valid sign)
     */
    public int reloadAllShopSigns() {
        int failedCount = 0;

        Set<ChestShop> shops = new HashSet<>(chestShopHashMap.values());
        for (ChestShop shop : shops) {

            Block signBlock = shop.getSignLocation().getBlock();
            if (!(signBlock.getBlockData() instanceof WallSign)) {
                ChestShops.getInstance().getLogger().warning("The shop located at " + shop.getContainerLocation() + " does not have a valid shop sign! It is being deleted");
                deleteShop(shop);
                failedCount++;
            }

            Sign blockSign = (Sign) signBlock.getState();
            String[] arr = shop.getType() == ShopType.BUY ? buyShopLines : sellShopLines;
            OfflinePlayer player = Bukkit.getOfflinePlayer(shop.getUuid());

            blockSign.setLine(0, fillShopSignPlaceholders(arr[0], player, shop));
            blockSign.setLine(1, fillShopSignPlaceholders(arr[1], player, shop));
            blockSign.setLine(2, fillShopSignPlaceholders(arr[2], player, shop));
            blockSign.setLine(3, fillShopSignPlaceholders(arr[3], player, shop));
            blockSign.update();
        }

        return failedCount;
    }

    public void onServerShutdown() {
        for (ChestShop shop : chestShopHashMap.values()) {
            if (shop.isQueueShop()) {
                shop.removeSign();
            }
        }
    }

    @Nullable
    public ChestShop getShopFromContainer(Location containerLocation) {
        return chestShopHashMap.get(containerLocation);
    }

    /**
     * Gets the shop this wall sign is attached to
     * @param sign The sign block
     * @return The shop the sign is attached to or null if none exists for this sign
     */
    @Nullable
    public ChestShop getShopFromWallSign(Block sign) {
        if (!sign.getType().toString().contains("WALL_SIGN")) return null;

        Block containerBlock = getAttachedBlock(sign);
        if (containerBlock == null) return null;

        ChestShop shop = chestShopHashMap.get(containerBlock.getLocation());
        if (shop == null) return null;

        if (shop.getSignLocation().getBlock().equals(sign)) return shop;
        return null;
    }

    /**
     * Gets the block the wall sign is attacked to
     * @param sign The block of the sign
     * @return The block this sign is attached to or null if none exists
     */
    @Nullable
    public Block getAttachedBlock(Block sign) {
        if (!sign.getType().toString().contains("WALL_SIGN")) return null;

        if (sign.getBlockData() instanceof Directional) {
            Directional d = (Directional) sign.getBlockData();
            return sign.getRelative(d.getFacing().getOppositeFace());
        }
        return null;
    }

    public boolean isInvalidContainer(Block block) {
        if (block == null) return true;
        return !(block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST || block.getType() == Material.BARREL);
    }

    /**
     * Gets the type of shop
     * @param str The line of text to parse
     * @return BUY/SELL if the line contains the text buy/sell or null if neither exists.
     */
    private ShopType getShopTypeFromString(String str) {
        if (str.toLowerCase().contains("buy")) return ShopType.BUY;
        else if (str.toLowerCase().contains("sell")) return ShopType.SELL;
        return null;
    }

    private boolean chestHasSpace(Inventory inventory, ItemStack itemStack, int amount) {
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);

            if (item == null) {
                return true;
            }

            if (item.isSimilar(itemStack) && item.getAmount() + amount <= itemStack.getMaxStackSize()) {
                return true;
            }
        }

        return false;
    }

    private boolean hasSpace(Inventory inventory, ItemStack itemStack, int amount) {
        for (int slot = 0; slot < 36; slot++) {
            ItemStack item = inventory.getItem(slot);

            if (item == null) {
                return true;
            }

            if (item.isSimilar(itemStack) && item.getAmount() + amount <= itemStack.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    private String fillMessagePlaceholders(String message, OfflinePlayer owner, Player customer, int amountSold, double price, ItemStack shopItem) {
        String ownerName = PlayerNameCache.get(owner);
        String itemName = (shopItem.getItemMeta() != null && shopItem.getItemMeta().hasDisplayName()) ? shopItem.getItemMeta().getDisplayName() : shopItem.getType().toString();

        return message
                .replace("{owner_name}", ownerName)
                .replace("{customer_name}", customer.getName())
                .replace("{amount}", String.valueOf(amountSold))
                .replace("{price}", decimalFormat.format(price))
                .replace("{item_name}", itemName);
    }

    private String fillShopSignPlaceholders(String message, OfflinePlayer owner, ChestShop shop) {
        String ownerName = PlayerNameCache.get(owner);

        return message
                .replace("{owner}", ownerName)
                .replace("{amount}", String.valueOf(shop.getAmount()))
                .replace("{price}", decimalFormat.format(shop.getPrice()));
    }

    protected Collection<ChestShop> getAllShops() {
        return chestShopHashMap.values();
    }

    /**
     * Toggles shop messages for the player
     * @param player The player
     * @return True if the player is now bypassing shop messages, false otherwise
     */
    public boolean toggleShopMessages(Player player) {
        if (!playersBypassingShopMessages.remove(player.getUniqueId())) {
            playersBypassingShopMessages.add(player.getUniqueId());
            return true;
        }
        return false;
    }

    public List<String> getTutorialMessages() {
        return tutorialMessages;
    }
}
