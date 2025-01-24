package club.sk1er.mods.autogg.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public class JsonUtil {
    @NotNull
    public static JsonObject getPossibleJsonObject(JsonObject object, String member) {
        JsonElement element = object.get(member);
        if (element == null) {
            return new JsonObject();
        } else {
            return element.getAsJsonObject();
        }
    }

    @NotNull
    public static String getOrDefaultString(JsonObject object, String member, String defaultString) {
        JsonElement element = object.get(member);
        if (element == null) {
            return defaultString;
        } else {
            return element.getAsString();
        }
    }
}
