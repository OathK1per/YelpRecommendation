package com.zyp.yelp.dao;

import com.zyp.yelp.bean.Item;
import com.zyp.yelp.utils.YelpInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * created by Yuanping Zhang on 09/13/2018
 */
public class SearchItemRepository {
    private Connection conn;

    public SearchItemRepository(Connection conn) {
        this.conn = conn;
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
}
