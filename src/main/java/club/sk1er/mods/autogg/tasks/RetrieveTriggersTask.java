package club.sk1er.mods.autogg.tasks;

import club.sk1er.mods.autogg.AutoGG;
import club.sk1er.mods.autogg.handlers.patterns.PatternHandler;
import club.sk1er.mods.autogg.handlers.web.WebHandler;
import club.sk1er.mods.autogg.tasks.data.Server;
import club.sk1er.mods.autogg.tasks.data.Trigger;
import club.sk1er.mods.autogg.tasks.data.TriggersSchema;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Runnable class to fetch the AutoGG triggers on startup.
 *
 * @author ChachyDev
 */
public class RetrieveTriggersTask implements Runnable {

    private final Logger LOGGER = LogManager.getLogger(this);
    private final Gson gson = new Gson();
    private static final String TRIGGERS_URL = "https://static.sk1er.club/autogg/regex_triggers_3.json";

    /**
     * Runs a task which fetches the triggers JSON from the internet.
     *
     * @author ChachyDev
     */
    @Override
    public void run() {
        try {
            AutoGG.INSTANCE.setTriggers(gson.fromJson(WebHandler.fetchString(TRIGGERS_URL), TriggersSchema.class));
        } catch (Exception e) {
            // Prevent the game from melting when the triggers are not available
            LOGGER.error("Failed to fetch the AutoGG triggers! This isn't good...", e);
            AutoGG.INSTANCE.setTriggers(new TriggersSchema(new Server[0]));
            e.printStackTrace();
        }

        LOGGER.info("Registering patterns...");
        for (Server server : AutoGG.INSTANCE.getTriggers().getServers()) {
            for (Trigger trigger : server.getTriggers()) {
                String pattern = trigger.getPattern();
                PatternHandler.INSTANCE.getOrRegisterPattern(pattern);
            }
        }
    }
}
