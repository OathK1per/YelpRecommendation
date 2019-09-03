package com.zyp.yelp.utils;

import com.zyp.yelp.bean.Item;
import com.zyp.yelp.bean.Item.ItemBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * created by Yuanping Zhang on 09/07/2018
 */
public class DataPurify {

    public static List<Item> getItemList(JSONArray restaurants) throws JSONException {
        List<Item> list = new ArrayList<>();

        for (int i = 0; i < restaurants.length(); ++i) {
            JSONObject restaurant = restaurants.getJSONObject(i);
            ItemBuilder builder = new ItemBuilder();

            if (!restaurant.isNull("id")) {
                builder.setItemId(restaurant.getString("id"));
            }
            if (!restaurant.isNull("name")) {
                builder.setName(restaurant.getString("name"));
            }
            if (!restaurant.isNull("url")) {
                builder.setUrl(restaurant.getString("url"));
            }
            if (!restaurant.isNull("image_url")) {
                builder.setImageUrl(restaurant.getString("image_url"));
            }
            if (!restaurant.isNull("rating")) {
                builder.setRating(restaurant.getDouble("rating"));
            }
            if (!restaurant.isNull("distance")) {
                builder.setDistance(restaurant.getDouble("distance"));
            }

            builder.setAddress(getAddress(restaurant));
            builder.setCategories(getCategories(restaurant));

            list.add(builder.build());
        }

        return list;
    }

    public static Set<String> getCategories(JSONObject restaurant) throws JSONException {
        Set<String> categories = new HashSet<>();

        if (!restaurant.isNull("categories")) {
            JSONArray array = restaurant.getJSONArray("categories");
            for (int i = 0; i < array.length(); ++i) {
                JSONObject category = array.getJSONObject(i);
                if (!category.isNull("alias")) {
                    categories.add(category.getString("alias"));
                }
            }
        }

        return categories;
    }

    public static String getAddress(JSONObject restaurant) throws JSONException {
        String address = "";

        if (!restaurant.isNull("location")) {
            JSONObject location = restaurant.getJSONObject("location");
            if (!location.isNull("display_address")) {
                JSONArray displayAddress = location.getJSONArray("display_address");
                address = displayAddress.join(",");
            }
        }

        return address;
    }

}
