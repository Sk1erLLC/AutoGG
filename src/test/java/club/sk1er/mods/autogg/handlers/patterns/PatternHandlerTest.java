package club.sk1er.mods.autogg.handlers.patterns;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PatternHandlerTest {
    @Test
    void firstLookupReturnsThePattern() {
        Pattern pattern = new PatternHandler().getOrRegisterPattern("^ +Game over!$");
        assertNotNull(pattern);
        assertTrue(pattern.matcher("    Game over!").matches());
    }

    @Test
    void cachesCompiledPatterns() {
        PatternHandler handler = new PatternHandler();
        assertSame(handler.getOrRegisterPattern("^a+$"), handler.getOrRegisterPattern("^a+$"));
    }

    @Test
    void placeholdersAreReplacedBeforeCompiling() {
        PlaceholderAPI.INSTANCE.registerPlaceHolder("antigg_strings", "gg|GG|Good Game");
        Pattern pattern = new PatternHandler().getOrRegisterPattern("^(?:\\[.+] )?\\w{1,16}: (?:${antigg_strings})$");
        assertTrue(pattern.matcher("[MVP+] Bot_One: gg").matches());
        assertTrue(pattern.matcher("Bot_Two: Good Game").matches());
        assertFalse(pattern.matcher("[MVP+] Bot_One: nice try").matches());
    }
}
