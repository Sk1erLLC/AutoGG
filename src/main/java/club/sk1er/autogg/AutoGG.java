package club.sk1er.autogg;

import club.sk1er.autogg.command.AutoGGCommand;
import club.sk1er.autogg.config.AutoGGConfig;
import club.sk1er.autogg.listener.AutoGGListener;
import club.sk1er.modcore.ModCoreInstaller;
import club.sk1er.mods.core.util.Multithreading;
import club.sk1er.mods.core.util.WebUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Mod(modid = "autogg", name = "AutoGG", version = AutoGG.VERSION)
public class AutoGG {
    public static final String VERSION = "3.3";

    private final static List<Pattern> triggers = new ArrayList<>();
    private final static List<Pattern> casualTriggers = new ArrayList<>();
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

        fetchTriggers();
    }

    public static void fetchTriggers() {
        triggers.clear();
        casualTriggers.clear();
        Multithreading.runAsync(() -> { // change the url once you have the json on static.sk1er.club \/ \/ \/
            JsonArray downloadedTriggers = new JsonParser().parse(WebUtil.fetchString("https://raw.githubusercontent.com/SirNapkin1334/sirnapkin1334.github.io/master/file/regex_triggers.json")).getAsJsonArray();
            triggerData = downloadedTriggers.get(0).getAsJsonObject();
            for (JsonElement element : downloadedTriggers.get(1).getAsJsonArray()) {
                triggers.add(Pattern.compile(element.getAsString()));
            }
            for (JsonElement element : downloadedTriggers.get(2).getAsJsonArray()) {
                casualTriggers.add(Pattern.compile(element.getAsString()));
            }
        });
    }

    public List<Pattern> getTriggers() { return triggers; }

    public List<Pattern> getCasualTriggers() { return casualTriggers; }

    public JsonObject getTriggerData() { return triggerData; }

    public boolean isRunning() { return running; }

    public void setRunning(boolean running) { this.running = running; }

    public AutoGGConfig getAutoGGConfig() { return autoGGConfig; }
}
