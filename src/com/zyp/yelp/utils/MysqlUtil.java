package com.zyp.yelp.utils;

import com.zyp.yelp.bean.Item;
import com.zyp.yelp.bean.Item.ItemBuilder;

import java.sql.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * created by Yuanping Zhang on 09/10/2018
 */
public class MysqlUtil {
    private static final String HOSTNAME = "localhost";
    private static final String PORT_NUM = "3306";
    public static final String DB_NAME = "yelp";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "PasswordOfRoot";
    public static final String URL = "jdbc:mysql://" + HOSTNAME + ":" + PORT_NUM + "/" + DB_NAME +
            "?useSSL=false&serverTimezone=UTC";

    private Connection conn;

    public void getConnection() {
        try {
            System.out.println("Connecting to " + DB_NAME);
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (Exception e) {
                e.printStackTrace();
        }
    }

    public List<Item> searchItems(double lat, double lon, String term) {
        // Connect to external API
        YelpInfo api = new YelpInfo();
        List<Item> items = api.search(lat, lon, term);
        for (Item item : items) {
            saveItem(item);
        }
        return items;
    }

    public void saveItem(Item item) {
        if (conn == null) {
            System.err.println("DB connection failed");
            return;
        }

        try {
            String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, item.getItemId());
            ps.setString(2, item.getName());
            ps.setDouble(3, item.getRating());
            ps.setString(4, item.getAddress());
            ps.setString(5, item.getUrl());
            ps.setString(6, item.getImageUrl());
            ps.setDouble(7, item.getDistance());
            ps.execute();

            sql = "INSERT IGNORE INTO categories VALUES(?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, item.getItemId());
            for (String category : item.getCategories()) {
                ps.setString(2, category);
                ps.execute();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setFavoriteItems(String userId, List<String> itemIds) {
        if (conn == null) {
            System.err.println("DB connection failed");
            return;
        }

        try {
            String sql = "INSERT IGNORE INTO history(user_id, item_id) VALUES (?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, userId);
            for (String itemId : itemIds) {
                ps.setString(2, itemId);
                ps.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unsetFavoriteItems(String userId, List<String> itemIds) {
        if (conn == null) {
            System.err.println("DB connection failed");
            return;
        }

        try {
            String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, userId);
            for (String itemId : itemIds) {
                ps.setString(2, itemId);
                ps.execute();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Set<Item> getFavoriteItems(String userId) {
        if (conn == null) {
            System.err.println("DB connection failed");
            return new HashSet<>();
        }
        Set<Item> favoriteItems = new HashSet<>();
        Set<String> itemIds = getFavoriteItemIds(userId);

        try {
            String sql = "SELECT * FROM items WHERE item_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);

            for (String itemId : itemIds) {
                ps.setString(1, itemId);
                ResultSet rs = ps.executeQuery();

                ItemBuilder builder = new ItemBuilder();
                while (rs.next()) {
                    builder.setItemId(rs.getString("item_id"));
                    builder.setName(rs.getString("name"));
                    builder.setAddress(rs.getString("address"));
                    builder.setUrl(rs.getString("url"));
                    builder.setImageUrl(rs.getString("image_url"));
                    builder.setRating(rs.getDouble("rating"));
                    builder.setDistance(rs.getDouble("distance"));
                    builder.setCategories(getCategories(itemId));

                    favoriteItems.add(builder.build());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return favoriteItems;
    }

    public Set<String> getFavoriteItemIds(String userId) {
        if (conn == null) {
            System.err.println("DB connection failed");
            return new HashSet<>();
        }
        Set<String> favoriteItemIds = new HashSet<>();

        try {
            String sql = "SELECT item_id FROM history WHERE user_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                favoriteItemIds.add(rs.getString("item_id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return favoriteItemIds;
    }

    public Set<String> getCategories(String itemId) {
        if (conn == null) {
            System.err.println("DB connection failed");
            return null;
        }
        Set<String> categories = new HashSet<>();

        try {
            String sql = "SELECT category FROM categories WHERE item_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, itemId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return categories;
    }

    private static void schema() {
        try {
            // Step 1 Connect to MySQL.
            System.out.println("Connecting to " + URL);
            Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            if (conn == null) {
                return;
            }

            // Step 2 Drop tables in case they exist.
            Statement statement = conn.createStatement();
            String sql = "DROP TABLE IF EXISTS history";
            statement.executeUpdate(sql);

            sql = "DROP TABLE IF EXISTS categories";
            statement.executeUpdate(sql);

            sql = "DROP TABLE IF EXISTS items";
            statement.executeUpdate(sql);

            sql = "DROP TABLE IF EXISTS users";
            statement.executeUpdate(sql);

            // Step 3 Create new tables
            sql = "CREATE TABLE items ("
                    + "item_id VARCHAR(255) NOT NULL,"
                    + "name VARCHAR(255),"
                    + "rating FLOAT,"
                    + "address VARCHAR(255),"
                    + "url VARCHAR(255),"
                    + "image_url VARCHAR(255),"
                    + "distance FLOAT,"
                    + "PRIMARY KEY (item_id)"
                    + ")";
            statement.executeUpdate(sql);

            sql = "CREATE TABLE users ("
                    + "user_id VARCHAR(255) NOT NULL,"
                    + "password VARCHAR(255) NOT NULL,"
                    + "first_name VARCHAR(255),"
                    + "last_name VARCHAR(255),"
                    + "PRIMARY KEY (user_id)"
                    + ")";
            statement.executeUpdate(sql);

            sql = "CREATE TABLE categories ("
                    + "item_id VARCHAR(255) NOT NULL,"
                    + "category VARCHAR(255) NOT NULL,"
                    + "PRIMARY KEY (item_id, category),"
                    + "FOREIGN KEY (item_id) REFERENCES items(item_id)"
                    + ")";
            statement.executeUpdate(sql);

            sql = "CREATE TABLE history ("
                    + "user_id VARCHAR(255) NOT NULL,"
                    + "item_id VARCHAR(255) NOT NULL,"
                    + "last_favor_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "PRIMARY KEY (user_id, item_id),"
                    + "FOREIGN KEY (user_id) REFERENCES users(user_id),"
                    + "FOREIGN KEY (item_id) REFERENCES items(item_id)"
                    + ")";
            statement.executeUpdate(sql);

            // Step 4: insert fake user 1111/3229c1097c00d497a0fd282d586be050
            sql = "INSERT INTO users VALUES ('1111', '3229c1097c00d497a0fd282d586be050', 'John', 'Smith')";
            statement.executeUpdate(sql);

            System.out.println("Import done successfully");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        schema();
    }
}
