package club.sk1er.mods.autogg;

import club.sk1er.mods.autogg.handlers.patterns.GGPhrases;
import club.sk1er.mods.autogg.tasks.data.Server;
import club.sk1er.mods.autogg.tasks.data.TriggersSchema;
import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * The triggers file as https://static.sk1er.club/autogg/regex_triggers_3.json served it on 2026-10-07.
 */
public final class TriggersFixture {
    private TriggersFixture() {
    }

    public static TriggersSchema load() {
        GGPhrases.registerPlaceholders();
        try (Reader reader = new InputStreamReader(TriggersFixture.class.getResourceAsStream("/regex_triggers_3.json"), StandardCharsets.UTF_8)) {
            return new Gson().fromJson(reader, TriggersSchema.class);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static Server server(String name) {
        return Arrays.stream(load().getServers()).filter(s -> s.getName().equals(name)).findFirst().orElseThrow();
    }
}
