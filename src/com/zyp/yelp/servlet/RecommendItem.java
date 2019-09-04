package com.zyp.yelp.servlet;

import com.zyp.yelp.bean.Item;
import com.zyp.yelp.service.Recommendation;
import com.zyp.yelp.utils.RpcHelper;
import org.json.JSONArray;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 根据位置和喜好做出推荐
 * created by Yuanping Zhang on 09/03/2018
 */
@WebServlet("/recommend")
public class RecommendItem extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        double lat = Double.parseDouble(req.getParameter("lat"));
        double lon = Double.parseDouble(req.getParameter("lon"));
        String userId = req.getParameter("user_id");

        Recommendation recommendation = new Recommendation();
        List<Item> items = recommendation.recommendItems(userId, lat, lon);

        try {
            JSONArray array = new JSONArray();
            for (Item item : items) {
                array.put(item.toJSONObject());
            }
            RpcHelper.writeJsonArray(resp, array);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
