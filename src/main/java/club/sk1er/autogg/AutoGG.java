package club.sk1er.autogg;

import club.sk1er.autogg.command.AutoGGCommand;
import club.sk1er.autogg.config.AutoGGConfig;
import club.sk1er.autogg.listener.AutoGGListener;
import club.sk1er.modcore.ModCoreInstaller;
import club.sk1er.mods.core.universal.ChatColor;
import club.sk1er.mods.core.util.Multithreading;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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

        fetchTriggers(false);
    }

    public static void fetchTriggers(boolean sendChatMsg) {
        triggers.clear();
        casualTriggers.clear();
        Multithreading.runAsync(() -> { // change the url once you have the json on static.sk1er.club \/ \/ \/
            JsonArray downloadedTriggers;
            try {
                downloadedTriggers = new JsonParser().parse(fetchString("https://raw.githubusercontent.com/SirNapkin1334/sirnapkin1334.github.io/master/file/regex_triggers.json")).getAsJsonArray();
            } catch (java.io.IOException e) {
                if (sendChatMsg) Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(ChatColor.RED + "Unable to fetch triggers! Do you have an internet connection?"));
                e.printStackTrace();
                return;
            } catch (com.google.gson.JsonSyntaxException e) {
                if (sendChatMsg) Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(ChatColor.RED + ChatColor.BOLD.toString() + "JSON Syntax Error! Contact the mod authors if you see this message!"));
                System.err.println("JSON Syntax Error! Contact the mod authors if you see this!");
                e.printStackTrace();
                return;
            }
            triggerData = downloadedTriggers.get(0).getAsJsonObject();
            for (JsonElement element : downloadedTriggers.get(1).getAsJsonArray()) {
                triggers.add(Pattern.compile(element.getAsString()));
            }
            for (JsonElement element : downloadedTriggers.get(2).getAsJsonArray()) {
                casualTriggers.add(Pattern.compile(element.getAsString()));
            }
            Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(ChatColor.GREEN + "Successfully fetched triggers!"));
        });
    }

    public List<Pattern> getTriggers() { return triggers; }

    public List<Pattern> getCasualTriggers() { return casualTriggers; }

    public JsonObject getTriggerData() { return triggerData; }

    public boolean isRunning() { return running; }

    public void setRunning(boolean running) { this.running = running; }

    public AutoGGConfig getAutoGGConfig() { return autoGGConfig; }


    // asbyth told me to write my own, so I did (all i did was copy and changed some stuff lol)
    public static String fetchString(String url) throws java.io.IOException { // handling errors is left as an exercise to the user
        HttpURLConnection connection = (HttpURLConnection) new URL(url.replace(" ", "%20")).openConnection();
        connection.setRequestMethod("GET");
        connection.setUseCaches(false);
        connection.addRequestProperty("User-Agent", "Mozilla/4.76 (SirNapkin1334/Sk1er AutoGG)");
        connection.setReadTimeout(15000);
        connection.setConnectTimeout(15000);
        connection.setDoOutput(true);
        InputStream setup = connection.getInputStream();
		String s = IOUtils.toString(setup, java.nio.charset.Charset.defaultCharset());
		setup.close();
		return s;
    }
}
