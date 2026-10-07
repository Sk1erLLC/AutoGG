package club.sk1er.mods.autogg.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutoGGConfigTest {
    @TempDir
    Path dir;

    @Test
    void missingFileGivesTheDefaults() {
        AutoGGConfig config = AutoGGConfig.load(dir.resolve("autogg.json"));
        assertTrue(config.isModEnabled());
        assertFalse(config.isCasualAutoGGEnabled());
        assertFalse(config.isAntiGGEnabled());
        assertFalse(config.isAntiKarmaEnabled());
        assertFalse(config.isSecondaryEnabled());
        assertEquals(1, config.getAutoGGDelay());
        assertEquals(1, config.getSecondaryDelay());
        assertEquals(0, config.getAutoGGPhrase());
        assertEquals(0, config.getAutoGGPhrase2());
    }

    @Test
    void partialFileKeepsTheOtherDefaults() throws IOException {
        Path path = dir.resolve("autogg.json");
        Files.writeString(path, "{ \"antiGGEnabled\": true, \"antiKarmaEnabled\": true }");
        AutoGGConfig config = AutoGGConfig.load(path);
        assertTrue(config.isAntiGGEnabled());
        assertTrue(config.isAntiKarmaEnabled());
        assertTrue(config.isModEnabled());
        assertEquals(1, config.getAutoGGDelay());
    }

    @Test
    void savedSettingsLoadBack() {
        Path path = dir.resolve("sub/autogg.json");
        AutoGGConfig config = AutoGGConfig.load(path);
        config.setAutoGGPhrase(3);
        config.setSecondaryEnabled(true);
        config.setAutoGGPhrase2(2);
        config.setSecondaryDelay(4);
        config.save();

        AutoGGConfig loaded = AutoGGConfig.load(path);
        assertEquals(3, loaded.getAutoGGPhrase());
        assertTrue(loaded.isSecondaryEnabled());
        assertEquals(2, loaded.getAutoGGPhrase2());
        assertEquals(4, loaded.getSecondaryDelay());
    }

    @Test
    void everyToggleLoadsBack() {
        Path path = dir.resolve("autogg.json");
        AutoGGConfig config = AutoGGConfig.load(path);
        config.setModEnabled(false);
        config.setCasualAutoGGEnabled(true);
        config.setAntiGGEnabled(true);
        config.setAntiKarmaEnabled(true);
        config.setAutoGGDelay(3);
        config.save();

        AutoGGConfig loaded = AutoGGConfig.load(path);
        assertFalse(loaded.isModEnabled());
        assertTrue(loaded.isCasualAutoGGEnabled());
        assertTrue(loaded.isAntiGGEnabled());
        assertTrue(loaded.isAntiKarmaEnabled());
        assertEquals(3, loaded.getAutoGGDelay());
    }

    @Test
    void aFailedSaveKeepsTheSettingsInMemory() throws IOException {
        Path blocker = dir.resolve("not-a-directory");
        Files.writeString(blocker, "");
        AutoGGConfig config = AutoGGConfig.load(blocker.resolve("autogg.json"));
        config.setAntiGGEnabled(true);
        config.save();
        assertTrue(config.isAntiGGEnabled());
        assertFalse(Files.exists(blocker.resolve("autogg.json")));
    }

    @Test
    void delaySlidersStepInWholeSeconds() {
        assertEquals(0, AutoGGConfig.sliderToSeconds(0));
        assertEquals(1, AutoGGConfig.sliderToSeconds(0.2));
        assertEquals(1, AutoGGConfig.sliderToSeconds(0.25));
        assertEquals(3, AutoGGConfig.sliderToSeconds(0.55));
        assertEquals(5, AutoGGConfig.sliderToSeconds(1));
        assertEquals(0.2, AutoGGConfig.secondsToSlider(1));
        assertEquals(1.0, AutoGGConfig.secondsToSlider(5000));
        for (int seconds = 0; seconds <= AutoGGConfig.MAX_DELAY; seconds++) {
            assertEquals(seconds, AutoGGConfig.sliderToSeconds(AutoGGConfig.secondsToSlider(seconds)));
        }
    }

    @Test
    void brokenFileGivesTheDefaults() throws IOException {
        Path path = dir.resolve("autogg.json");
        Files.writeString(path, "not json {");
        assertTrue(AutoGGConfig.load(path).isModEnabled());
    }

    @Test
    void oldMillisecondDelaysAreReset() throws IOException {
        Path path = dir.resolve("autogg.json");
        Files.writeString(path, "{ \"autoGGDelay\": 5000, \"secondaryDelay\": 4 }");
        AutoGGConfig config = AutoGGConfig.load(path);
        config.migrateDelays();
        assertEquals(1, config.getAutoGGDelay());
        assertEquals(4, config.getSecondaryDelay());
    }

    @Test
    void outOfRangeValuesAreClamped() throws IOException {
        Path path = dir.resolve("autogg.json");
        Files.writeString(path, "{ \"autoGGDelay\": 5000, \"secondaryDelay\": -2 }");
        AutoGGConfig config = AutoGGConfig.load(path);
        assertEquals(5, config.getClampedAutoGGDelay());
        assertEquals(0, config.getClampedSecondaryDelay());
        assertEquals(5, AutoGGConfig.clampPhrase(17, 6));
        assertEquals(0, AutoGGConfig.clampPhrase(-1, 6));
    }
}
