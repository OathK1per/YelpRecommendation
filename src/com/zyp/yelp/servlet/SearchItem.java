package com.zyp.yelp.servlet;

import com.zyp.yelp.bean.Item;
import com.zyp.yelp.utils.MysqlUtil;
import com.zyp.yelp.utils.RpcHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * 搜索Yelp上附近的餐馆供选择
 *
 * created by Yuanping Zhang on 09/03/2018
 */
@WebServlet("/search")
public class SearchItem extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        double lat = Double.parseDouble(req.getParameter("lat"));
        double lon = Double.parseDouble(req.getParameter("lon"));
        String userId = req.getParameter("user_id");
        String term = req.getParameter("term");
        MysqlUtil connection = new MysqlUtil();
        connection.getConnection();
        try {
            List<Item> items = connection.searchItems(lat, lon, term);
            Set<String> favoriteItemIds = connection.getFavoriteItemIds(userId);

            JSONArray array = new JSONArray();
            for (Item item : items) {
                JSONObject obj = item.toJSONObject();
                obj.put("favorite", favoriteItemIds.contains(item.getItemId()));
                array.put(obj);
            }
            RpcHelper.writeJsonArray(resp, array);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }
}
