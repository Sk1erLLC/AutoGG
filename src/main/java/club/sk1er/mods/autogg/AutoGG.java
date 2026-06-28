package club.sk1er.mods.autogg;

import club.sk1er.mods.autogg.command.AutoGGCommand;
import club.sk1er.mods.autogg.config.AutoGGConfig;
import club.sk1er.mods.autogg.handlers.gg.AutoGGHandler;
import club.sk1er.mods.autogg.handlers.patterns.PlaceholderAPI;
import club.sk1er.mods.autogg.handlers.web.WebHandler;
import club.sk1er.mods.autogg.tasks.RetrieveTriggersTask;
import club.sk1er.mods.autogg.tasks.data.Server;
import club.sk1er.mods.autogg.tasks.data.TriggersSchema;
import club.sk1er.mods.autogg.util.JsonUtil;
import com.google.gson.JsonObject;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class AutoGG implements ClientModInitializer {
    public static final String MODID = "autogg";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    public static AutoGG INSTANCE;

    private final String[] primaryGGStrings = {"gg", "GG", "gf", "Good Game", "Good Fight", "Good Round! :D"};
    private final String[] secondaryGGStrings = {"Have a good day!", "<3", "AutoGG By Sk1er!", "gf", "Good Fight", "Good Round", ":D", "Well played!", "wp"};
    private volatile TriggersSchema triggers;

    public volatile boolean usingEnglish = true;
    public static final ScheduledExecutorService POOL = Executors.newScheduledThreadPool(5);

    @Override
    public void onInitializeClient() {
        INSTANCE = this;

        AutoGGConfig.load();

        Set<String> joined = new HashSet<>();
        joined.addAll(Arrays.asList(primaryGGStrings));
        joined.addAll(Arrays.asList(secondaryGGStrings));
        PlaceholderAPI.INSTANCE.registerPlaceHolder("antigg_strings", String.join("|", joined));

        new AutoGGHandler().register();
        AutoGGCommand.register();

        POOL.submit(new RetrieveTriggersTask());
        POOL.submit(this::checkUserLanguage);
    }

    private void checkUserLanguage() {
        try {
            Thread.sleep(5000);
            Minecraft mc = Minecraft.getInstance();
            if (mc.getUser() == null) return;
            final String username = mc.getUser().getName();
            final JsonObject json = WebHandler.fetchJson("https://api.sk1er.club/language/" + username);
            final String language = JsonUtil.getOrDefaultString(json, "language", "ENGLISH");
            this.usingEnglish = "ENGLISH".equals(language);
        } catch (Exception e) {
            LOGGER.error("Failed to check user language", e);
        }
    }

    public TriggersSchema getTriggers() {
        return triggers != null ? triggers : new TriggersSchema(new Server[0]);
    }

    public void setTriggers(TriggersSchema triggers) {
        this.triggers = triggers;
    }

    public String[] getPrimaryGGStrings() {
        return primaryGGStrings;
    }

    public String[] getSecondaryGGStrings() {
        return secondaryGGStrings;
    }
}
