package com.zyp.yelp.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * created by Yuanping Zhang on 09/05/2018
 */
public class RpcHelper {

    public static void writeJsonObject(HttpServletResponse response, JSONObject object) throws IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.addHeader("Access-Control-Allow-Origin", "*");
        out.print(object);
        out.close();
    }

    public static void writeJsonArray(HttpServletResponse response, JSONArray array) throws IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.addHeader("Access-Control-Allow-Origin", "*");
        out.print(array);
        out.close();
    }

    public static JSONObject readJSONObject(HttpServletRequest request) {
        StringBuilder sBuilder = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                sBuilder.append(line);
            }
            return new JSONObject(sBuilder.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new JSONObject();
    }

}
