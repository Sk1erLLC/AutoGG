package club.sk1er.mods.autogg.handlers.patterns;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The phrases AutoGG can send, and the ${antigg_strings} placeholder the triggers file uses to hide them.
 */
public final class GGPhrases {
    public static final String[] PRIMARY = {"gg", "GG", "gf", "Good Game", "Good Fight", "Good Round! :D"};
    public static final String[] SECONDARY = {"Have a good day!", "<3", "AutoGG By Sk1er!", "gf", "Good Fight", "Good Round", ":D", "Well played!", "wp"};

    private GGPhrases() {
    }

    public static String antiGGStrings() {
        Set<String> joined = new LinkedHashSet<>();
        joined.addAll(Arrays.asList(PRIMARY));
        joined.addAll(Arrays.asList(SECONDARY));
        return String.join("|", joined);
    }

    public static void registerPlaceholders() {
        PlaceholderAPI.INSTANCE.registerPlaceHolder("antigg_strings", antiGGStrings());
    }
}
