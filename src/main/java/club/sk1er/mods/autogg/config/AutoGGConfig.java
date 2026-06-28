package club.sk1er.mods.autogg.config;

import club.sk1er.mods.autogg.AutoGG;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class AutoGGConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("autogg.json");

    private static AutoGGConfig instance = new AutoGGConfig();

    // General
    private boolean modEnabled = true;
    private boolean casualAutoGGEnabled = false;
    private int autoGGDelay = 1;
    private int autoGGPhrase = 0;

    // Misc
    private boolean antiGGEnabled = false;
    private boolean antiKarmaEnabled = false;

    // Secondary
    private boolean secondaryEnabled = false;
    private int autoGGPhrase2 = 0;
    private int secondaryDelay = 1;

    public static AutoGGConfig get() {
        return instance;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                AutoGGConfig loaded = GSON.fromJson(reader, AutoGGConfig.class);
                if (loaded != null) instance = loaded;
            } catch (IOException e) {
                AutoGG.LOGGER.error("Failed to load AutoGG config", e);
            }
        }
        save();
    }

    public static void save() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(instance, writer);
        } catch (IOException e) {
            AutoGG.LOGGER.error("Failed to save AutoGG config", e);
        }
    }

    public boolean isModEnabled() { return modEnabled; }
    public boolean isCasualAutoGGEnabled() { return casualAutoGGEnabled; }
    public int getAutoGGDelay() { return Math.max(0, Math.min(5, autoGGDelay)); }
    public int getAutoGGPhrase() { return Math.max(0, Math.min(5, autoGGPhrase)); }
    public boolean isAntiGGEnabled() { return antiGGEnabled; }
    public boolean isAntiKarmaEnabled() { return antiKarmaEnabled; }
    public boolean isSecondaryEnabled() { return secondaryEnabled; }
    public int getAutoGGPhrase2() { return Math.max(0, Math.min(8, autoGGPhrase2)); }
    public int getSecondaryDelay() { return Math.max(0, Math.min(5, secondaryDelay)); }
}
