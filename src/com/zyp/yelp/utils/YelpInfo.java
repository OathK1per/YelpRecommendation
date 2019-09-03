package com.zyp.yelp.utils;

import com.zyp.yelp.bean.Item;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * created by Yuanping Zhang on 09/05/2018
 */
public class YelpInfo {
    private static final String HOST = "https://api.yelp.com";
    private static final String ENDPOINT = "/v3/businesses/search";
    private static final String DEFAULT_TERM = "";
    private static final int SEARCH_LIMIT = 20;

    private static final String TOKEN_TYPE = "Bearer";
    private static final String API_KEY = "_KYyNQB5_ItahxQXUmwJRJv1VeO_BISqbdGcKC3OyJgMd6o7bvMkK4Z4K23fkey2PXe5LHHh0IsBA-Tg8efYn9MPYsP1KPJuwA9La9PxJDNrNtqojdG5wIHX9x9uXXYx";

    public List<Item> search(double lat, double lon, String term) {
        if (term == null || term.isEmpty()) {
            term = DEFAULT_TERM;
        }

        try {
            term = URLEncoder.encode(term, "UTF-8"); // Rick Sun => Rick20%Sun
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String query = String.format("term=%s&latitude=%s&longitude=%s&limit=%s", term, lat, lon, SEARCH_LIMIT);
        String url = HOST + ENDPOINT + "?" + query;

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", TOKEN_TYPE + " " + API_KEY);

            int responseCode = connection.getResponseCode();
            System.out.println("Sending request to URL: " + url);
            System.out.println("Response code: " + responseCode);

            if (responseCode != 200) {
                return new ArrayList<>();
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine = "";
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject obj = new JSONObject(response.toString());
            if (!obj.isNull("businesses")) {
                return DataPurify.getItemList(obj.getJSONArray("businesses"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    private void queryAPI(double lat, double lon) {
        List<Item> itemList = search(lat, lon, null);
        for (Item item : itemList) {
            JSONObject jsonObject = item.toJSONObject();
            System.out.println(jsonObject);
        }
    }

    public static void main(String[] args) {
        YelpInfo tmApi = new YelpInfo();
        tmApi.queryAPI(32.88, -117.23);
    }

}
