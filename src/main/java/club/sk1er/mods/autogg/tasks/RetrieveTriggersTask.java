package club.sk1er.mods.autogg.tasks;

import club.sk1er.mods.autogg.AutoGG;
import club.sk1er.mods.autogg.handlers.patterns.PatternHandler;
import club.sk1er.mods.autogg.tasks.data.Server;
import club.sk1er.mods.autogg.tasks.data.TriggersSchema;
import com.google.gson.Gson;
import net.minecraft.util.HttpUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Runnable class to fetch the AutoGG triggers on startup.
 *
 * @author ChachyDev
 */

public class RetrieveTriggersTask implements Runnable {

    private final Logger LOGGER = LogManager.getLogger(this);

    private final Gson gson = new Gson();

    /**
     * Runs a task which fetches the triggers JSON from the internet.
     *
     * @author ChachyDev
     */

    @Override
    public void run() {
        try {
            String AUTOGG_TRIGGERS_URL = "https://gist.githubusercontent.com/ChachyDev/645f67de6cad549a9d3c26af0779d53b/raw/effa62b07066dbd609a14072e28993899851fcc7/new_new_triggers.json";
            AutoGG.INSTANCE.setTriggers(gson.fromJson(HttpUtil.get(new URL(AUTOGG_TRIGGERS_URL)), TriggersSchema.class));
        } catch (IOException e) {
            // To stop maniac in the event of the triggers being failed to reach we just create an empty TriggerSchema.
            LOGGER.error("Failed to fetch the AutoGG triggers! This isn't good...", e);
            AutoGG.INSTANCE.setTriggers(new TriggersSchema(new Server[0]));
            e.printStackTrace();
        }

        LOGGER.info("Registering patterns...");

        for (Server server : AutoGG.INSTANCE.getTriggers().getServers()) {
            for (String trigger : server.getTriggers()) {
                PatternHandler.INSTANCE.registerPattern(trigger);
            }

            for (String trigger : server.getCasualTriggers()) {
                PatternHandler.INSTANCE.registerPattern(trigger);
            }

            if (server.getAntiGGTrigger() != null) {
                List<String> joined = new ArrayList<>();
                joined.addAll(Arrays.asList(AutoGG.INSTANCE.getPrimaryGGStrings()));
                joined.addAll(Arrays.asList(AutoGG.INSTANCE.getSecondaryGGStrings()));
                PatternHandler.INSTANCE.registerPattern(server.getAntiGGTrigger().replace("${antigg_strings}", String.join("|", joined)));
            }

            if (server.getAntiKarmaTrigger() != null) {
                PatternHandler.INSTANCE.registerPattern(server.getAntiKarmaTrigger());
            }
        }
    }
}
