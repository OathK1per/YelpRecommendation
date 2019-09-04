package com.zyp.yelp.service;

import com.zyp.yelp.bean.Item;
import com.zyp.yelp.dao.ItemHistoryRepository;
import com.zyp.yelp.dao.SearchItemRepository;
import com.zyp.yelp.utils.MysqlUtil;

import java.sql.Connection;
import java.util.Map.Entry;
import java.util.*;

/**
 * created by Yuanping Zhang on 09/17/2018
 */
public class Recommendation {

    public List<Item> recommendItems(String userId, double lat, double lon) {
        List<Item> recommendedItems = new ArrayList<>();

        // Step 1, get all favorited itemids
        Connection conn = MysqlUtil.getConnection();
        ItemHistoryRepository itemHistory = new ItemHistoryRepository(conn);
        SearchItemRepository searchItem = new SearchItemRepository(conn);
        Set<String> favoriteItemIds = itemHistory.getFavoriteItemIds(userId);

        // Step 2, get all categories, sort by count
        // bbq => 5, burger => 3, coffee => 6
        Map<String, Integer> allCategories = new HashMap<>();
        for (String itemId : favoriteItemIds) {
            Set<String> categories = searchItem.getCategories(itemId);
            for (String category : categories) {
                allCategories.put(category, allCategories.getOrDefault(category, 0) + 1);
            }
        }

        List<Entry<String, Integer>> categoryList = new ArrayList<>(allCategories.entrySet());
        Collections.sort(categoryList, (Entry<String, Integer> e1, Entry<String, Integer> e2) -> {
            return Integer.compare(e2.getValue(), e1.getValue());
        });

        // Step 3, search based on category, filter out favorite items
        Set<String> visitedItemIds = new HashSet<>();
        for (Entry<String, Integer> category : categoryList) {
            List<Item> items = searchItem.searchItems(lat, lon, category.getKey());
            for (Item item : items) {
                if (!favoriteItemIds.contains(item.getItemId()) && !visitedItemIds.contains(item.getItemId())) {
                    recommendedItems.add(item);
                    visitedItemIds.add(item.getItemId());
                }
            }
        }

        MysqlUtil.close(conn);

        return recommendedItems;
    }

}
