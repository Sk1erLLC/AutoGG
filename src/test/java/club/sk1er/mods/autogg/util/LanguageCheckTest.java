package club.sk1er.mods.autogg.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LanguageCheckTest {
    @Test
    void english() {
        assertTrue(LanguageCheck.isEnglish("{\"language\": \"ENGLISH\"}"));
    }

    @Test
    void otherLanguages() {
        assertFalse(LanguageCheck.isEnglish("{\"language\": \"GERMAN\"}"));
    }

    @Test
    void failuresKeepEnglish() {
        assertTrue(LanguageCheck.isEnglish("Failed to fetch"));
        assertTrue(LanguageCheck.isEnglish("{}"));
        assertTrue(LanguageCheck.isEnglish(""));
        assertTrue(LanguageCheck.isEnglish(null));
        assertTrue(LanguageCheck.isEnglish("[1, 2]"));
    }
}
