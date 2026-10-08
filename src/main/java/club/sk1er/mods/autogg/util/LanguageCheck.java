package club.sk1er.mods.autogg.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Reads the language service's answer, e.g. {"language": "ENGLISH"}.
 */
public final class LanguageCheck {
    private static final Gson GSON = new Gson();

    private LanguageCheck() {
    }

    /**
     * @return false only when the service says the player's Hypixel language is something other than English;
     * an error or an answer it can't read keeps AutoGG's default of English
     */
    public static boolean isEnglish(String response) {
        try {
            JsonObject json = GSON.fromJson(response, JsonObject.class);
            if (json == null) return true;
            return "ENGLISH".equals(JsonUtil.getOrDefaultString(json, "language", "ENGLISH"));
        } catch (RuntimeException e) {
            return true;
        }
    }
}
