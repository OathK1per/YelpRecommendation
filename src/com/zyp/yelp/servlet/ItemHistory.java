package com.zyp.yelp.servlet;

import com.zyp.yelp.bean.Item;
import com.zyp.yelp.utils.RpcHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 根据个人点赞记录添加到个人喜好当中
 * created by Yuanping Zhang on 09/05/2018
 */
@WebServlet("/history")
public class ItemHistory extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        MySQLConnection connection = new MySQLConnection();
        try {
            JSONObject input = RpcHelper.readJSONObject(request);
            String userId = input.getString("user_id");
            JSONArray array = input.getJSONArray("favorite");
            List<String> itemIds = new ArrayList<>();
            for (int i = 0; i < array.length(); ++i) {
                itemIds.add(array.getString(i));
            }
            connection.setFavoriteItems(userId, itemIds);
            RpcHelper.writeJsonObject(response, new JSONObject().put("result", "SUCCESS"));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        MySQLConnection connection = new MySQLConnection();
        try {
            String userId = request.getParameter("user_id");
            Set<Item> favoritedItems = connection.getFavoriteItems(userId);

            JSONArray array = new JSONArray();
            for (Item item : favoritedItems) {
                JSONObject obj = item.toJSONObject();
                obj.put("favorite", true);
                array.put(obj);
            }
            RpcHelper.writeJsonArray(response, array);;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        MySQLConnection connection = new MySQLConnection();
        try {
            JSONObject input = RpcHelper.readJSONObject(request);
            String userId = input.getString("user_id");
            JSONArray array = input.getJSONArray("favorite");
            List<String> itemIds = new ArrayList<>();
            for (int i = 0; i < array.length(); ++i) {
                itemIds.add(array.getString(i));
            }
            connection.unsetFavoriteItems(userId, itemIds);
            RpcHelper.writeJsonObject(response, new JSONObject().put("result", "SUCCESS"));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
    }
}
