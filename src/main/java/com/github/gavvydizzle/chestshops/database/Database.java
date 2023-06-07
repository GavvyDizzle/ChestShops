package com.github.gavvydizzle.chestshops.database;

import com.github.mittenmc.serverutils.ItemStackSerializer;
import com.github.gavvydizzle.chestshops.ChestShops;
import com.github.gavvydizzle.chestshops.shops.ChestShop;
import com.github.gavvydizzle.chestshops.shops.ShopType;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

public abstract class Database {

    private final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS shop_data(" +
            "id INTEGER PRIMARY KEY," +
            "item BLOB NOT NULL," +
            "amount INT NOT NULL," +
            "price DOUBLE NOT NULL," +
            "uuid VARCHAR(32) NOT NULL," +
            "shoptype VARCHAR(4) NOT NULL," +
            "blocklocation VARCHAR NOT NULL," +
            "signlocation VARCHAR NOT NULL," +
            "world VARCHAR NOT NULL" +
            ");";

    protected ChestShops plugin;
    Connection connection;
    public String table = "shop_data";

    public Database(ChestShops instance) {
        plugin = instance;
    }

    public abstract Connection getSQLConnection();

    /**
     * If the table for this plugin does not exist one will be created
     */
    public void load() {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(CREATE_TABLE);
            ps.execute();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }

    /**
     * Loads all shops from the database
     * @return A list of all loaded shops
     */
    public ArrayList<ChestShop> loadShops() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;

        ArrayList<ChestShop> shops = new ArrayList<>();

        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + ";");

            rs = ps.executeQuery();

            while(rs.next()){
                String[] signLoc = rs.getString("signlocation").split(",");
                Location signLocation = new Location(Bukkit.getWorld(rs.getString("world")), Double.parseDouble(signLoc[0]), Double.parseDouble(signLoc[1]), Double.parseDouble(signLoc[2]));
                ChestShop shop = new ChestShop(
                        rs.getInt("amount"),
                        rs.getDouble("price"),
                        ShopType.valueOf(rs.getString("shoptype")),
                        UUID.fromString(rs.getString("uuid")),
                        signLocation
                );
                String[] blockLoc = rs.getString("blocklocation").split(",");
                Location blockLocation = new Location(Bukkit.getWorld(rs.getString("world")), Double.parseDouble(blockLoc[0]), Double.parseDouble(blockLoc[1]), Double.parseDouble(blockLoc[2]));
                shop.setContainerLocation(blockLocation);
                shop.setItem(ItemStackSerializer.deserializeItemStack(rs.getBytes("item")));
                shops.add(shop);
            }
            return shops;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return null;
    }

    public void saveShop(ChestShop shop) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("INSERT INTO " + table + " (item, amount, price, uuid, shoptype, blocklocation, signlocation, world) VALUES(?,?,?,?,?,?,?,?)");
            ps.setBytes(1, ItemStackSerializer.serializeItemStack(shop.getItem()));
            ps.setInt(2, shop.getAmount());
            ps.setDouble(3, shop.getPrice());
            ps.setString(4, shop.getUuid().toString());
            ps.setString(5, shop.getType().toString());
            Location blockLocation = shop.getContainerLocation();
            ps.setString(6, blockLocation.getX()+","+blockLocation.getY()+","+blockLocation.getZ());
            Location signLocation = shop.getSignLocation();
            ps.setString(7, signLocation.getX()+","+signLocation.getY()+","+signLocation.getZ());
            ps.setString(8, blockLocation.getWorld().getName());

            ps.execute();

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }

    public void deleteShop(ChestShop shop) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("DELETE FROM " + table + " WHERE blocklocation = ? AND world = ?;");
            Location blockLocation = shop.getContainerLocation();
            ps.setString(1, blockLocation.getX()+","+blockLocation.getY()+","+blockLocation.getZ());
            ps.setString(2, blockLocation.getWorld().getName());
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }

    public void deleteShops(ArrayList<ChestShop> shops) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("DELETE FROM " + table + " WHERE blocklocation = ? AND world = ?;");
            for (ChestShop shop : shops) {
                Location blockLocation = shop.getContainerLocation();
                ps.setString(1, blockLocation.getX()+","+blockLocation.getY()+","+blockLocation.getZ());
                ps.setString(2, blockLocation.getWorld().getName());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }
}
