package club.sk1er.mods.autogg.handlers.patterns;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class PatternHandler {
    public static PatternHandler INSTANCE = new PatternHandler();

    private final Map<String, Pattern> patternCache = new HashMap<>();

    public Pattern getOrRegisterPattern(String pattern) {
        String processed = PlaceholderAPI.INSTANCE.process(pattern);
        return patternCache.computeIfAbsent(processed, Pattern::compile);
    }

    public void clearPatterns() {
        patternCache.clear();
    }
}
