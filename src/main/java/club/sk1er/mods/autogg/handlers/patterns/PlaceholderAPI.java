package club.sk1er.mods.autogg.handlers.patterns;

import java.util.HashMap;
import java.util.Map;

public class PlaceholderAPI {
    public static PlaceholderAPI INSTANCE = new PlaceholderAPI();
    private static final String PLACEHOLDER = "${%s}";

    private final Map<String, String> placeholders = new HashMap<>();

    public void registerPlaceHolder(String key, String value) {
        placeholders.put(key, value);
    }

    public String process(String string) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String placeholder = String.format(PLACEHOLDER, entry.getKey());
            if (string.contains(placeholder)) {
                string = string.replace(placeholder, entry.getValue());
            }
        }

        return string;
    }
}
