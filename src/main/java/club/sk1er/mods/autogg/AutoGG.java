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

package club.sk1er.mods.autogg;

import club.sk1er.mods.autogg.command.AutoGGCommand;
import club.sk1er.mods.autogg.config.AutoGGConfig;
import club.sk1er.mods.autogg.handlers.gg.AutoGGHandler;
import club.sk1er.mods.autogg.handlers.patterns.PlaceholderAPI;
import club.sk1er.mods.autogg.handlers.web.WebHandler;
import club.sk1er.mods.autogg.tasks.RetrieveTriggersTask;
import club.sk1er.mods.autogg.tasks.data.TriggersSchema;
import club.sk1er.mods.autogg.util.JsonUtil;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Contains the main class for AutoGG which handles trigger schema setting/getting and the main initialization code.
 *
 * @author ChachyDev
 */
@Mod(modid = "autogg", name = "AutoGG", version = "4.1.3")
public class AutoGG {

    @Mod.Instance
    public static AutoGG INSTANCE;

    private final String[] primaryGGStrings = {"gg", "GG", "gf", "Good Game", "Good Fight", "Good Round! :D"};
    private final String[] secondaryGGStrings = {"Have a good day!", "<3", "AutoGG By Sk1er!", "gf", "Good Fight", "Good Round", ":D", "Well played!", "wp"};
    private TriggersSchema triggers;
    private AutoGGConfig autoGGConfig;

    public boolean usingEnglish;

    public static final ScheduledExecutorService POOL = Executors.newScheduledThreadPool(5);

    @Mod.EventHandler
    public void onFMLPreInitialization(FMLPreInitializationEvent event) {
        POOL.submit(this::checkUserLanguage);
    }

    @Mod.EventHandler
    public void onFMLInitialization(FMLInitializationEvent event) {
        autoGGConfig = new AutoGGConfig();
        autoGGConfig.preload();

        Set<String> joined = new HashSet<>();
        joined.addAll(Arrays.asList(primaryGGStrings));
        joined.addAll(Arrays.asList(secondaryGGStrings));

        PlaceholderAPI.INSTANCE.registerPlaceHolder("antigg_strings", String.join("|", joined));

        POOL.submit(new RetrieveTriggersTask());
        MinecraftForge.EVENT_BUS.register(new AutoGGHandler());
        ClientCommandHandler.instance.registerCommand(new AutoGGCommand());

        // fix settings that were moved to seconds instead of ms
        // so users aren't waiting 5000 seconds to send GG
        if (autoGGConfig.getAutoGGDelay() > 5) autoGGConfig.setAutoGGDelay(1);
        if (autoGGConfig.getSecondaryDelay() > 5) autoGGConfig.setSecondaryDelay(1);
    }

    private void checkUserLanguage() {
        final String username = Minecraft.getMinecraft().getSession().getUsername();
        final JsonObject json = WebHandler.fetchJson("https://api.sk1er.club/player/" + username);
        final String language = JsonUtil.getOrDefaultString(JsonUtil.getPossibleJsonObject(json, "player"),"userLanguage", "ENGLISH");
        this.usingEnglish = "ENGLISH".equals(language);
    }

    public TriggersSchema getTriggers() {
        return triggers;
    }

    public void setTriggers(TriggersSchema triggers) {
        this.triggers = triggers;
    }

    public AutoGGConfig getAutoGGConfig() {
        return autoGGConfig;
    }

    public String[] getPrimaryGGStrings() {
        return primaryGGStrings;
    }

    public String[] getSecondaryGGStrings() {
        return secondaryGGStrings;
    }
}
