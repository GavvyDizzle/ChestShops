package com.github.gavvydizzle.chestshops.shops;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.UUID;

public class ChestShop {

    private ItemStack item;
    private final int amount;
    private final double price;
    private final UUID uuid;
    private Location containerLocation;
    private final Location signLocation;
    private final ShopType type;
    private boolean isQueueShop;

    public ChestShop(int amount, double price, ShopType shopType, UUID uuid, Location signLocation) {
        this.amount = amount;
        this.price = price;
        this.type = shopType;
        this.uuid = uuid;
        this.signLocation = signLocation.clone();
    }

    public void setItem(ItemStack itemStack) {
        this.item = itemStack.clone();
        this.item.setAmount(amount);
    }

    public void setContainerLocation(Location location) {
        this.containerLocation = location.clone();
    }

    public void placeShopHologram() {
        if (isQueueShop) return;
        Location location = containerLocation.clone();
        location.add(0.5, 1.5, 0.5);

        try {
            Hologram hologram = DHAPI.createHologram(Objects.requireNonNull(containerLocation.getWorld()).getName() + "_" + containerLocation.getBlockX() + "_" + containerLocation.getBlockY() + "_" + containerLocation.getBlockZ(), location, false);
            DHAPI.addHologramLine(hologram, item);
            hologram.setDisplayRange(10);
        } catch (Exception ignored) {}
    }

    public void removeShopHologram() {
        if (isQueueShop) return;
        DHAPI.removeHologram(Objects.requireNonNull(containerLocation.getWorld()).getName() + "_" + containerLocation.getBlockX() + "_" + containerLocation.getBlockY() + "_" + containerLocation.getBlockZ());
    }

    public void removeSign() {
        if (signLocation.getBlock().getType().toString().contains("WALL_SIGN")) {
            signLocation.getBlock().setType(Material.AIR);
        }
    }

    public void dropSign() {
        getSignLocation().getBlock().breakNaturally();
    }

    public int getAmount() {
        return amount;
    }

    public double getPrice() {
        return price;
    }

    public Location getContainerLocation() {
        return containerLocation;
    }

    public ItemStack getItem() {
        return item;
    }

    public UUID getUuid() {
        return uuid;
    }

    public ShopType getType() {
        return type;
    }

    public Location getSignLocation() {
        return signLocation;
    }

    public boolean isQueueShop() {
        return isQueueShop;
    }

    public void setQueueShop(boolean queueShop) {
        isQueueShop = queueShop;
    }
}
