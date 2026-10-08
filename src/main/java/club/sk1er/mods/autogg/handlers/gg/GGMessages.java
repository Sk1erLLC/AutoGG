package club.sk1er.mods.autogg.handlers.gg;

import club.sk1er.mods.autogg.config.AutoGGConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * The messages AutoGG sends at the end of a game, and when.
 */
public final class GGMessages {
    private GGMessages() {
    }

    public record Message(String text, int delaySeconds) {
        /**
         * A message starting with "/" (a prefix such as "/ac") is run as a command, as typing it in chat would.
         */
        public boolean isCommand() {
            return text.startsWith("/");
        }
    }

    public static List<Message> plan(String prefix, AutoGGConfig config, String[] primary, String[] secondary) {
        List<Message> messages = new ArrayList<>(2);
        int delay = config.getClampedAutoGGDelay();
        messages.add(new Message(withPrefix(prefix, primary[AutoGGConfig.clampPhrase(config.getAutoGGPhrase(), primary.length)]), delay));

        if (config.isSecondaryEnabled()) {
            String secondGGMessage = secondary[AutoGGConfig.clampPhrase(config.getAutoGGPhrase2(), secondary.length)];
            messages.add(new Message(withPrefix(prefix, secondGGMessage), delay + config.getClampedSecondaryDelay()));
        }
        return messages;
    }

    public static String withPrefix(String prefix, String message) {
        return prefix == null || prefix.isEmpty() ? message : prefix + " " + message;
    }
}
