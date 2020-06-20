package club.sk1er.autogg;

import club.sk1er.autogg.command.AutoGGCommand;
import club.sk1er.autogg.config.AutoGGConfig;
import club.sk1er.autogg.listener.AutoGGListener;
import club.sk1er.modcore.ModCoreInstaller;
import club.sk1er.mods.core.universal.ChatColor;
import club.sk1er.mods.core.util.MinecraftUtils;
import club.sk1er.mods.core.util.Multithreading;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Mod(modid = "autogg", name = "AutoGG", version = AutoGG.VERSION)
public class AutoGG {
    public static final String VERSION = "3.3";

    private final static List<Pattern> triggers = new ArrayList<>();
    private final static List<Pattern> casualTriggers = new ArrayList<>();
    private final Logger logger = LogManager.getLogger("AutoGG");
    private final String prefix = ChatColor.BLUE + "[AutoGG] " + ChatColor.RESET;
    private static JsonObject triggerData;
    private AutoGGConfig autoGGConfig;
    private boolean running;

    @Mod.Instance("autogg")
    public static AutoGG instance;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ModCoreInstaller.initializeModCore(Minecraft.getMinecraft().mcDataDir);

        autoGGConfig = new AutoGGConfig();
        autoGGConfig.preload();

        running = false;

        ClientCommandHandler.instance.registerCommand(new AutoGGCommand());
        MinecraftForge.EVENT_BUS.register(new AutoGGListener());

        fetchTriggers(false);
    }

    public static void fetchTriggers(boolean sendChatMsg) {
        triggers.clear();
        casualTriggers.clear();
        Multithreading.runAsync(() -> {
            JsonArray downloadedTriggers;

            try {
                downloadedTriggers = new JsonParser().parse(fetchString("https://raw.githubusercontent.com/SirNapkin1334/sirnapkin1334.github.io/master/file/regex_triggers.json")).getAsJsonArray();
            } catch (IOException e) {
                if (sendChatMsg) {
                    MinecraftUtils.sendMessage(AutoGG.instance.prefix, ChatColor.RED + "Unable to fetch triggers! Do you have an internet connection?");
                }

                AutoGG.instance.logger.error("Failed to fetch triggers.", e);
                return;
            } catch (JsonSyntaxException e) {
                if (sendChatMsg) {
                    MinecraftUtils.sendMessage(AutoGG.instance.prefix,ChatColor.RED + ChatColor.BOLD.toString() + "JSON Syntax Error! Contact the mod authors if you see this message!");
                }

                AutoGG.instance.logger.error("JSON Syntax Error! Contact us in the support channel at https://discord.gg/sk1er.", e);
                return;
            }

            triggerData = downloadedTriggers.get(0).getAsJsonObject();

            for (JsonElement element : downloadedTriggers.get(1).getAsJsonArray()) {
                triggers.add(Pattern.compile(element.getAsString()));
            }

            for (JsonElement element : downloadedTriggers.get(2).getAsJsonArray()) {
                casualTriggers.add(Pattern.compile(element.getAsString()));
            }

            MinecraftUtils.sendMessage(AutoGG.instance.prefix,ChatColor.GREEN + "Successfully fetched triggers!");
        });
    }

    public List<Pattern> getTriggers() {
        return triggers;
    }

    public List<Pattern> getCasualTriggers() {
        return casualTriggers;
    }

    public JsonObject getTriggerData() {
        return triggerData;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public AutoGGConfig getAutoGGConfig() {
        return autoGGConfig;
    }

    public static String fetchString(String url) throws IOException {
        HttpURLConnection connection = null;
        String s = "";

        try {
            connection = (HttpURLConnection) new URL(url.replace(" ", "%20")).openConnection();
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.addRequestProperty("User-Agent", "Mozilla/4.76 (SirNapkin1334/Sk1er AutoGG)");
            connection.setReadTimeout(15000);
            connection.setConnectTimeout(15000);
            connection.setDoOutput(true);

            try (InputStream setup = connection.getInputStream()) {
                s = IOUtils.toString(setup, Charset.defaultCharset());
            }
        } catch (Exception e) {
            AutoGG.instance.logger.error("Failed to fetch string.", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return s;
    }

    public Logger getLogger() {
        return logger;
    }

    public String getPrefix() {
        return prefix;
    }
}
