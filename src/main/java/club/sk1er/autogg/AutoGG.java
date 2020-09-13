/*
 * AutoGG - Automatically say a selectable phrase at the end of a game on supported servers.
 * Copyright (C) 2020  Sk1er LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package club.sk1er.autogg;

import club.sk1er.autogg.command.AutoGGCommand;
import club.sk1er.autogg.config.AutoGGConfig;
import club.sk1er.autogg.listener.AutoGGListener;
import club.sk1er.modcore.ModCoreInstaller;
import club.sk1er.mods.core.universal.ChatColor;
import club.sk1er.mods.core.util.MinecraftUtils;
import club.sk1er.mods.core.util.Multithreading;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Mod(modid = "autogg", name = "AutoGG", version = AutoGG.VERSION)
public class AutoGG {
    public static final String VERSION = "4.0.1";
    private static final String[] ACCEPTED_CONFIG_VERSIONS = {"2"};
    public static boolean validConfigVersion, triggerFetchSuccess = true; // independent of config
    private final Logger logger = LogManager.getLogger("AutoGG");
    private final String prefix = ChatColor.BLUE + "[AutoGG] " + ChatColor.RESET;
    private static JsonObject triggerJson;
    public static Map<String, String> triggerMeta;

    public static final Map<String, List<Pattern>> ggRegexes = new HashMap<>();
    public static final Map<String, Pattern> otherRegexes = new HashMap<>();
    public static final Map<String, String> other = new HashMap<>();

    private AutoGGConfig autoGGConfig;
    private boolean running;

    @Mod.Instance("autogg")
    public static AutoGG instance;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ModCoreInstaller.initializeModCore(Minecraft.getMinecraft().mcDataDir);

        autoGGConfig = new AutoGGConfig();
        autoGGConfig.preload();

        ClientCommandHandler.instance.registerCommand(new AutoGGCommand());
        MinecraftForge.EVENT_BUS.register(new AutoGGListener());

        downloadTriggers(false);
    }

    public static void downloadTriggers(boolean sendChatMsg) {
        Multithreading.runAsync(() -> {
            try {
                validConfigVersion = triggerFetchSuccess = true;

                triggerJson = new JsonParser().parse(fetchString(
                    "http://static.sk1er.club/autogg/regex_triggers_new.json")
                ).getAsJsonObject();

                assert Arrays.asList(ACCEPTED_CONFIG_VERSIONS).contains(triggerJson.get("triggers_format").toString());

                // black magic; https://stackoverflow.com/a/21720953
                triggerMeta = new Gson().fromJson(triggerJson.get("meta"), new TypeToken<HashMap<String, String>>() {
                }.getType());

            } catch (IOException e) {
                if (sendChatMsg) {
                    MinecraftUtils.sendMessage(AutoGG.instance.prefix, ChatColor.RED +
                        "Unable to fetch triggers! Do you have an internet connection?");
                }

                AutoGG.instance.logger.error("Failed to fetch triggers.", e);
                triggerFetchSuccess = false;
                return;
            } catch (JsonSyntaxException e) {
                if (sendChatMsg) {
                    MinecraftUtils.sendMessage(AutoGG.instance.prefix, ChatColor.RED +
                        ChatColor.BOLD.toString() +
                        "JSON Syntax Error! Contact the mod authors if you see this message!");
                }

                AutoGG.instance.logger.error(
                    "JSON Syntax Error! Contact us in the support channel at https://discord.gg/sk1er.", e);
                triggerFetchSuccess = false;
                return;
            } catch (AssertionError | NullPointerException e) {
                if (sendChatMsg) {
                    MinecraftUtils.sendMessage(AutoGG.instance.prefix, ChatColor.RED +
                        "Unsupported triggers version! Please update AutoGG!");
                }

                AutoGG.instance.logger.error("Unsupported triggers version! Please update AutoGG!");
                validConfigVersion = false;
                return;
            }

            if (sendChatMsg) {
                MinecraftUtils.sendMessage(AutoGG.instance.prefix, ChatColor.GREEN +
                    "Successfully fetched triggers!");
            }
        });
    }

    public void getDataFromDownloadedTriggers() {
        final Set<String> ggOptions = keySet(triggerJson.get("servers").getAsJsonObject().get("^(?:.+\\.)?hypixel\\.(?:net|io)$").getAsJsonObject().get("gg_triggers").getAsJsonObject());
        final Set<String> otherPatternOptions = keySet(triggerJson.get("servers").getAsJsonObject().get("^(?:.+\\.)?hypixel\\.(?:net|io)$").getAsJsonObject().get("other_patterns").getAsJsonObject());
        final Set<String> otherOptions = keySet(triggerJson.get("servers").getAsJsonObject().get("^(?:.+\\.)?hypixel\\.(?:net|io)$").getAsJsonObject().get("other").getAsJsonObject());
        ServerData serverData = Minecraft.getMinecraft().getCurrentServerData();
        String ip;

        ggRegexes.clear();
        otherRegexes.clear();
        other.clear();

        for (String s : ggOptions) {
            ggRegexes.put(s, new ArrayList<>());
        }

        if ((ip = serverData == null ? null : serverData.serverIP) == null) {
            setDefaultTriggerData();
            return;
        }

        Set<String> keySet;

        try {
            keySet = keySet(triggerJson.get("servers").getAsJsonObject());
        } catch (NullPointerException e) { // if download silently failed
            AutoGG.instance.getLogger().error("Trigger download silently failed.");
            return;
        }

        boolean foundServer = false;

        for (String a : keySet) { // could be made more efficient by pre-building list and compiling on download but
            if (Pattern.compile(a).matcher(ip).matches()) { // there is not an easy way to do it i don't think
                JsonObject data = triggerJson.get("servers").getAsJsonObject().get(a).getAsJsonObject();
                for (String s : ggOptions) {
                    for (JsonElement j : data.get("gg_triggers")
                        .getAsJsonObject()
                        .get(s)
                        .getAsJsonArray()) {
                        ggRegexes.get(s).add(Pattern.compile(j.toString().substring(1, j.toString().length() - 1)
                            .replaceAll("\\\\{2}", "\\\\")));
                        // for some reason, using \\<character> in json turns into \\<character> rather than
                        // \<character> when compiled, i don't know why must be a quirk of json
                    }
                }
                for (String s : otherPatternOptions) {
                    String p = data.get("other_patterns").getAsJsonObject().get(s).toString();
                    otherRegexes.put(s, Pattern.compile(p.substring(1, p.length() - 1)
                        .replaceAll("\\\\{2}", "\\\\")
                        // for some reason, using \\<character> in json turns into \\<character> rather than
                        // \<character> when compiled, i don't know why must be a quirk of json
                        .replaceAll("(?<!\\\\)\\$\\{antigg_strings}",
                            String.join("|", getAntiGGStrings()))
                    ));
                }
                for (String s : otherOptions) {
                    String p = data.get("other").getAsJsonObject().get(s).toString();
                    other.put(s, p.substring(1, p.length() - 1));
                }

                foundServer = true;
                break;
            }
        }

        if (!foundServer) {
            setDefaultTriggerData();
        }

    }

    private static void setDefaultTriggerData() {
        Pattern unmatch = Pattern.compile("$^");
        otherRegexes.put("antigg", unmatch);
        otherRegexes.put("anti_karma", unmatch);
        other.put("msg", "");
    }

    // The following function includes code from org.apache.commons.lang.ArrayUtils
    //
    // They are used within the terms of the Apache License v2.0, which can be viewed at
    // https://apache.org/licenses/LICENSE-2.0.txt
    // Copyright (C) Apache Foundation 2020
    //
    // Modifications: strip out everything that isn't the actual copying part and make it work on internal variables
    private static String[] getAntiGGStrings() {
        String[] primaryStrings = AutoGGListener.getPrimaryStrings();
        String[] secondaryStrings = AutoGGListener.getSecondaryStrings();
        String[] joinedArray = (String[]) Array.newInstance(primaryStrings.getClass().getComponentType(), primaryStrings.length + secondaryStrings.length);
        System.arraycopy(primaryStrings, 0, joinedArray, 0, primaryStrings.length);
        System.arraycopy(secondaryStrings, 0, joinedArray, primaryStrings.length, secondaryStrings.length);
        return joinedArray;
    }

    public static Set<String> keySet(JsonObject json) throws NullPointerException {
        try { // some people don't have this function for some reason
            return json.keySet();
        } catch (NoSuchMethodError e) {
            Set<String> keySet = new HashSet<>();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                keySet.add(entry.getKey());
            }
            return keySet;
        }
    }

    public boolean works() {
        return validConfigVersion && triggerFetchSuccess;
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
        String s;

        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.addRequestProperty("User-Agent", "Mozilla/4.76 (Sk1er AutoGG)");
            connection.setReadTimeout(15000);
            connection.setConnectTimeout(15000);
            connection.setDoOutput(true);

            try (InputStream setup = connection.getInputStream()) {
                s = IOUtils.toString(setup, Charset.defaultCharset());
            }
        } catch (Exception e) {
            AutoGG.instance.logger.error("Failed to fetch string.", e);
            throw new IOException("Failed to fetch triggers!");
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
