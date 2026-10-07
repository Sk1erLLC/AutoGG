package club.sk1er.mods.autogg.handlers.patterns;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Helper for compiling Regular Expressions on startup to prevent them being compiled on each chat message.
 *
 * @author ChachyDev
 */
public class PatternHandler {
    public static PatternHandler INSTANCE = new PatternHandler();

    private final Map<String, Pattern> patternCache = new ConcurrentHashMap<>();

    public Pattern getOrRegisterPattern(String pattern) {
        String processedPattern = PlaceholderAPI.INSTANCE.process(pattern);

        // Triggers are matched on the client thread and on the pool at the same time
        return patternCache.computeIfAbsent(processedPattern, Pattern::compile);
    }

    public void clearPatterns() {
        patternCache.clear();
    }
}
