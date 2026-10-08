package club.sk1er.mods.autogg.handlers.patterns;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GGPhrasesTest {
    @Test
    void antiGGStringsListEveryPhraseOnce() {
        List<String> parts = Arrays.asList(GGPhrases.antiGGStrings().split("\\|"));
        for (String phrase : GGPhrases.PRIMARY) assertTrue(parts.contains(phrase), phrase);
        for (String phrase : GGPhrases.SECONDARY) assertTrue(parts.contains(phrase), phrase);
        assertEquals(parts.size(), parts.stream().distinct().count());
    }
}
