package club.sk1er.mods.autogg.handlers.gg;

import club.sk1er.mods.autogg.TriggersFixture;
import club.sk1er.mods.autogg.tasks.data.Server;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TriggerMatcherTest {
    private final Server hypixel = TriggersFixture.server("Hypixel Server");

    @ParameterizedTest
    @ValueSource(strings = {
            "                          1st Killer - Bot_Red - 7 Kills",
            " 1st Killer - [MVP+] Bot_Red - 7 Kills",
            "                          Game over!",
            "  Murderer: Bot_Red (3 Kills)",
            "  You survived 12 rounds!",
    })
    void gameEndLinesSayGG(String line) {
        assertTrue(TriggerMatcher.shouldSayGG(hypixel, line, false));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Game over!", "[MVP+] Bot_One: gg", "Bot_One joined the lobby!", ""})
    void otherLinesDoNot(String line) {
        assertFalse(TriggerMatcher.shouldSayGG(hypixel, line, true));
    }

    @Test
    void casualTriggersNeedCasualAutoGG() {
        String line = "MINOR EVENT! Dragon Egg in The End ended";
        assertTrue(TriggerMatcher.shouldSayGG(hypixel, line, true));
        assertFalse(TriggerMatcher.shouldSayGG(hypixel, line, false));
    }

    @ParameterizedTest
    @ValueSource(strings = {"[MVP+] Bot_One: gg", "[VIP] Bot_Two: GG", "Bot_Three: Good Game", "Bot_Four: Well played!", "Bot_Five: <3"})
    void antiGGHidesOtherPlayersGG(String line) {
        assertTrue(TriggerMatcher.shouldHide(hypixel, line, true, false));
        assertFalse(TriggerMatcher.shouldHide(hypixel, line, false, true));
    }

    @Test
    void antiKarmaHidesKarmaLines() {
        assertTrue(TriggerMatcher.shouldHide(hypixel, "+5 Karma!", false, true));
        assertFalse(TriggerMatcher.shouldHide(hypixel, "+5 Karma!", true, false));
    }

    @ParameterizedTest
    @ValueSource(strings = {"[MVP+] Bot_One: nice try", "[VIP] Bot_Two: see you next game", "--- chat test start ---"})
    void ordinaryLinesAreNeverHidden(String line) {
        assertFalse(TriggerMatcher.shouldHide(hypixel, line, true, true));
    }
}
