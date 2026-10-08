package club.sk1er.mods.autogg.handlers.gg;

import club.sk1er.mods.autogg.handlers.patterns.PatternHandler;
import club.sk1er.mods.autogg.tasks.data.Server;
import club.sk1er.mods.autogg.tasks.data.Trigger;

/**
 * Decides what a chat line (formatting already stripped) means on a supported server.
 */
public final class TriggerMatcher {
    private TriggerMatcher() {
    }

    /**
     * @return true when Anti GG or Anti Karma should remove the line from chat
     */
    public static boolean shouldHide(Server server, String stripped, boolean antiGG, boolean antiKarma) {
        for (Trigger trigger : server.getTriggers()) {
            switch (trigger.getType()) {
                case ANTI_GG:
                    if (antiGG && matches(trigger, stripped)) return true;
                    break;
                case ANTI_KARMA:
                    if (antiKarma && matches(trigger, stripped)) return true;
                    break;
                default:
                    break;
            }
        }
        return false;
    }

    /**
     * @return true when the line ends a game, so AutoGG should say GG (casual triggers only when Casual AutoGG is on)
     */
    public static boolean shouldSayGG(Server server, String stripped, boolean casual) {
        for (Trigger trigger : server.getTriggers()) {
            switch (trigger.getType()) {
                case NORMAL:
                    if (matches(trigger, stripped)) return true;
                    break;
                case CASUAL:
                    if (casual && matches(trigger, stripped)) return true;
                    break;
                default:
                    break;
            }
        }
        return false;
    }

    private static boolean matches(Trigger trigger, String stripped) {
        return PatternHandler.INSTANCE.getOrRegisterPattern(trigger.getPattern()).matcher(stripped).matches();
    }
}
