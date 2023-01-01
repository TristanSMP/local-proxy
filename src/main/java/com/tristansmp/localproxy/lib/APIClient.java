package com.tristansmp.localproxy.lib;

import com.google.gson.Gson;

import java.net.HttpURLConnection;
import java.net.URL;

public class APIClient {
    public final String API_URL = "https://tristansmp.com/api/services/lp";
    private final Gson gson = new Gson();

    public Meta getMeta() {
        try {
            URL url = new URL(API_URL + "/meta");

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");

            int status = con.getResponseCode();
            if (status == 200) {
                return gson.fromJson(new String(con.getInputStream().readAllBytes()), Meta.class);
            } else {
                throw new Exception("Failed to get meta");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
