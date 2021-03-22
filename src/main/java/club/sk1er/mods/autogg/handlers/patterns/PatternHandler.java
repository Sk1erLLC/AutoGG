package club.sk1er.mods.autogg.handlers.patterns;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Helper for compiling Regular Expressions on startup to prevent them being compiled on each chat message.
 *
 * @author ChachyDev
 */
public class PatternHandler {
    public static PatternHandler INSTANCE = new PatternHandler();
    private final List<Pattern> patterns = new ArrayList<>();

    public Pattern registerPattern(String pattern) {
        Pattern p = Pattern.compile(pattern);
        if (!patterns.contains(p)) {
            patterns.add(p);
        }

        return p;
    }

    public Pattern getPattern(String pattern) {
        for (Pattern pattern1 : patterns) {
            if (pattern1.pattern().equals(pattern)) {
                return pattern1;
            }
        }

        return registerPattern(pattern);
    }

    public void clearPatterns() {
        patterns.clear();
    }
}
