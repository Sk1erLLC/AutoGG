package club.sk1er.mods.autogg.handlers.patterns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Helper for compiling Regular Expressions on startup to prevent them being compiled on each chat message.
 *
 * @author ChachyDev
 */
public class PatternHandler {
    public static PatternHandler INSTANCE = new PatternHandler();

    private final Map<String, Pattern> patternCache = new HashMap<>();

    public Pattern getOrRegisterPattern(String pattern) {
        String processedPattern = PlaceholderAPI.INSTANCE.process(pattern);

        Pattern p = patternCache.get(processedPattern);
        if (p == null) {
            p = patternCache.put(processedPattern, Pattern.compile(processedPattern));
        }

        return p;
    }

    public void clearPatterns() {
        patternCache.clear();
    }
}
