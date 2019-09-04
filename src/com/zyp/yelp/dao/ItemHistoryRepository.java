package com.zyp.yelp.dao;

import com.zyp.yelp.bean.Item;
import com.zyp.yelp.bean.Item.ItemBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * created by Yuanping Zhang on 09/17/2018
 */
public class ItemHistoryRepository {
    private Connection conn;

    public ItemHistoryRepository(Connection conn) {
        this.conn = conn;
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
                    builder.setCategories(new SearchItemRepository(conn).getCategories(itemId));

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
}
