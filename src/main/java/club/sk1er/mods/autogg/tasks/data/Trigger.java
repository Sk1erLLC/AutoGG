package club.sk1er.mods.autogg.tasks.data;

/**
 * Data holder for a trigger in the server triggers JsonArray
 *
 * Trigger types:
 * 0 = Normal
 * 1 = Casual
 * 2 = AntiGG
 * 3 = AntiKarma
 *
 * @author ChachyDev
 */

public class Trigger {
    private final int type;

    private final String pattern;

    public Trigger(int type, String pattern) {
        this.type = type;
        this.pattern = pattern;
    }

    public int getType() {
        return type;
    }

    public String getPattern() {
        return pattern;
    }
}
