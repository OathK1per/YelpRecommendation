package com.zyp.yelp.servlet;

import com.zyp.yelp.bean.Item;
import com.zyp.yelp.dao.ItemHistoryRepository;
import com.zyp.yelp.dao.SearchItemRepository;
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
import java.sql.Connection;
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
        double lat = 32.88;
        double lon = -117.23;
        if (req.getParameter("lat") != null){
            lat = Double.parseDouble(req.getParameter("lat"));
        }
        if (req.getParameter("lon") != null) {
            lon = Double.parseDouble(req.getParameter("lon"));
        }
        String userId = req.getParameter("user_id");
        String term = req.getParameter("term");
        Connection conn = MysqlUtil.getConnection();
        SearchItemRepository searchItem = new SearchItemRepository(conn);
        ItemHistoryRepository itemHistory = new ItemHistoryRepository(conn);
        try {
            List<Item> items = searchItem.searchItems(lat, lon, term);
            Set<String> favoriteItemIds = itemHistory.getFavoriteItemIds(userId);

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
            MysqlUtil.close(conn);
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }
}
