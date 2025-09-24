package club.sk1er.mods.autogg.handlers.web;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class WebHandler {
    private static final Gson gson = new Gson();

    public static JsonObject fetchJson(String url) {
        String res = fetchString(url);
        return gson.fromJson(res, JsonObject.class);
    }

    public static String fetchString(String url) {
        try {
            return fetchString(new URL(url));
        } catch (Exception e) {
            return "malformed url";
        }
    }

    public static String fetchString(URL url) {
        try {
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", "AutoGG");

            try (InputStream in = connection.getInputStream()) {
                return IOUtils.toString(in, "UTF-8");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return "Failed to fetch";
        }
    }
}
