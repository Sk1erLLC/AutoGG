package club.sk1er.mods.autogg.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * AutoGG's settings, saved as JSON in {@code config/autogg.json}.
 * The fields, their names and their defaults are the ones the Vigilance config had.
 */
@SuppressWarnings("FieldMayBeFinal")
public class AutoGGConfig {
    private static final Logger LOGGER = LogManager.getLogger("AutoGG");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final int MAX_DELAY = 5;

    // General
    private boolean autoGGEnabled = true;
    private boolean casualAutoGGEnabled;
    private int autoGGDelay = 1;
    private int autoGGPhrase = 0;

    // Miscellaneous
    private boolean antiGGEnabled;
    private boolean antiKarmaEnabled;

    // Secondary Message
    private boolean secondaryEnabled;
    private int autoGGPhrase2 = 0;
    private int secondaryDelay = 1;

    private transient Path path;

    public static AutoGGConfig load() {
        return load(FabricLoader.getInstance().getConfigDir().resolve("autogg.json"));
    }

    public static AutoGGConfig load(Path path) {
        AutoGGConfig config = null;
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                config = GSON.fromJson(reader, AutoGGConfig.class);
            } catch (IOException | JsonParseException e) {
                LOGGER.error("Failed to read the AutoGG config, using the defaults", e);
            }
        }
        if (config == null) config = new AutoGGConfig();
        config.path = path;
        return config;
    }

    public void save() {
        if (path == null) return;
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save the AutoGG config", e);
        }
    }

    /**
     * Fixes settings that were moved to seconds instead of ms, so users aren't waiting 5000 seconds to send GG.
     */
    public void migrateDelays() {
        if (autoGGDelay > MAX_DELAY) autoGGDelay = 1;
        if (secondaryDelay > MAX_DELAY) secondaryDelay = 1;
    }

    private static int clamp(int value, int max) {
        return Math.max(0, Math.min(max, value));
    }

    public boolean isModEnabled() {
        return autoGGEnabled;
    }

    public void setModEnabled(boolean autoGGEnabled) {
        this.autoGGEnabled = autoGGEnabled;
    }

    public boolean isCasualAutoGGEnabled() {
        return casualAutoGGEnabled;
    }

    public void setCasualAutoGGEnabled(boolean casualAutoGGEnabled) {
        this.casualAutoGGEnabled = casualAutoGGEnabled;
    }

    public boolean isAntiGGEnabled() {
        return antiGGEnabled;
    }

    public void setAntiGGEnabled(boolean antiGGEnabled) {
        this.antiGGEnabled = antiGGEnabled;
    }

    public boolean isAntiKarmaEnabled() {
        return antiKarmaEnabled;
    }

    public void setAntiKarmaEnabled(boolean antiKarmaEnabled) {
        this.antiKarmaEnabled = antiKarmaEnabled;
    }

    /**
     * The raw value as saved; delays from before they were measured in seconds can be larger than {@link #MAX_DELAY}.
     */
    public int getAutoGGDelay() {
        return autoGGDelay;
    }

    public void setAutoGGDelay(int autoGGDelay) {
        this.autoGGDelay = autoGGDelay;
    }

    public int getAutoGGPhrase() {
        return autoGGPhrase;
    }

    public void setAutoGGPhrase(int autoGGPhrase) {
        this.autoGGPhrase = autoGGPhrase;
    }

    public boolean isSecondaryEnabled() {
        return secondaryEnabled;
    }

    public void setSecondaryEnabled(boolean secondaryEnabled) {
        this.secondaryEnabled = secondaryEnabled;
    }

    public int getAutoGGPhrase2() {
        return autoGGPhrase2;
    }

    public void setAutoGGPhrase2(int autoGGPhrase2) {
        this.autoGGPhrase2 = autoGGPhrase2;
    }

    public int getSecondaryDelay() {
        return secondaryDelay;
    }

    public void setSecondaryDelay(int secondaryDelay) {
        this.secondaryDelay = secondaryDelay;
    }

    /**
     * The delay in seconds, kept within the slider's range.
     */
    public int getClampedAutoGGDelay() {
        return clamp(autoGGDelay, MAX_DELAY);
    }

    public int getClampedSecondaryDelay() {
        return clamp(secondaryDelay, MAX_DELAY);
    }

    /**
     * The whole seconds a delay slider at {@code value} (0 to 1) stands for.
     */
    public static int sliderToSeconds(double value) {
        return clamp((int) Math.round(value * MAX_DELAY), MAX_DELAY);
    }

    public static double secondsToSlider(int seconds) {
        return clamp(seconds, MAX_DELAY) / (double) MAX_DELAY;
    }

    /**
     * A phrase index kept within the list's bounds, so a hand-edited file can't crash the game.
     */
    public static int clampPhrase(int index, int count) {
        return clamp(index, count - 1);
    }
}
