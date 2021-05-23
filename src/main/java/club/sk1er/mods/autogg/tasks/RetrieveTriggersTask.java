package club.sk1er.mods.autogg.tasks;

import club.sk1er.mods.autogg.AutoGG;
import club.sk1er.mods.autogg.handlers.patterns.PatternHandler;
import club.sk1er.mods.autogg.tasks.data.Server;
import club.sk1er.mods.autogg.tasks.data.Trigger;
import club.sk1er.mods.autogg.tasks.data.TriggerType;
import club.sk1er.mods.autogg.tasks.data.TriggersSchema;
import com.google.gson.Gson;
import gg.essential.api.utils.WebUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private static final String TRIGGERS_URL = "https://static.sk1er.club/autogg/regex_triggers_3.json";

    /**
     * Runs a task which fetches the triggers JSON from the internet.
     *
     * @author ChachyDev
     */
    @Override
    public void run() {
        try {
            AutoGG.INSTANCE.setTriggers(gson.fromJson(WebUtil.fetchString(TRIGGERS_URL), TriggersSchema.class));
        } catch (Exception e) {
            // To stop maniac in the event of the triggers being failed to reach we just create an empty TriggerSchema.
            LOGGER.error("Failed to fetch the AutoGG triggers! This isn't good...", e);
            AutoGG.INSTANCE.setTriggers(new TriggersSchema(new Server[0]));
            e.printStackTrace();
        }

        LOGGER.info("Registering patterns...");
        List<String> joined = new ArrayList<>();
        joined.addAll(Arrays.asList(AutoGG.INSTANCE.getPrimaryGGStrings()));
        joined.addAll(Arrays.asList(AutoGG.INSTANCE.getSecondaryGGStrings()));

        for (Server server : AutoGG.INSTANCE.getTriggers().getServers()) {
            for (Trigger trigger : server.getTriggers()) {
                String pattern = trigger.getPattern();
                if (trigger.getType() == TriggerType.ANTI_GG) {
                    pattern = pattern.replace("${antigg_strings}", String.join("|", joined));
                }
                PatternHandler.INSTANCE.registerPattern(pattern);
            }
        }
    }
}
