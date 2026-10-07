package club.sk1er.mods.autogg.handlers.gg;

import club.sk1er.mods.autogg.config.AutoGGConfig;
import club.sk1er.mods.autogg.handlers.patterns.GGPhrases;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GGMessagesTest {
    @TempDir
    Path dir;

    private AutoGGConfig config() {
        return AutoGGConfig.load(dir.resolve("autogg.json"));
    }

    private static List<GGMessages.Message> plan(String prefix, AutoGGConfig config) {
        return GGMessages.plan(prefix, config, GGPhrases.PRIMARY, GGPhrases.SECONDARY);
    }

    @Test
    void defaultsSayGGAfterOneSecond() {
        List<GGMessages.Message> messages = plan("", config());
        assertEquals(List.of(new GGMessages.Message("gg", 1)), messages);
        assertFalse(messages.get(0).isCommand());
    }

    @Test
    void aSlashPrefixMakesACommand() {
        GGMessages.Message message = plan("/ac", config()).get(0);
        assertEquals("/ac gg", message.text());
        assertTrue(message.isCommand());
    }

    @Test
    void secondMessageUsesTheSecondPhraseAfterBothDelays() {
        AutoGGConfig config = config();
        config.setAutoGGPhrase(3);
        config.setSecondaryEnabled(true);
        config.setAutoGGPhrase2(2);
        config.setAutoGGDelay(2);
        config.setSecondaryDelay(3);
        assertEquals(List.of(new GGMessages.Message("Good Game", 2), new GGMessages.Message("AutoGG By Sk1er!", 5)), plan("", config));
        assertEquals("/ac AutoGG By Sk1er!", plan("/ac", config).get(1).text());
    }

    @Test
    void outOfRangeSettingsAreClamped() {
        AutoGGConfig config = config();
        config.setAutoGGPhrase(99);
        config.setAutoGGDelay(-4);
        config.setSecondaryEnabled(true);
        config.setAutoGGPhrase2(-1);
        config.setSecondaryDelay(60);
        assertEquals(List.of(new GGMessages.Message("Good Round! :D", 0), new GGMessages.Message("Have a good day!", 5)), plan(null, config));
    }
}
