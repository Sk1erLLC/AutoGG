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
import club.sk1er.mods.autogg.tasks.RetrieveTriggersTask;
import club.sk1er.mods.autogg.tasks.data.TriggersSchema;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.modcore.api.utils.Multithreading;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Contains the main class for AutoGG which handles trigger schema setting/getting and the main initialization code.
 *
 * @author ChachyDev
 */

@Mod(modid = "autogg", name = "AutoGG", version = AutoGG.VERSION)
public class AutoGG {
    public static final String VERSION = "4.2";

    @Mod.Instance
    public static AutoGG INSTANCE;

    private final Logger LOGGER = LogManager.getLogger(this);
    private final String[] primaryGGStrings = {"gg", "GG", "gf", "Good Game", "Good Fight", "Good Round! :D"};
    private final String[] secondaryGGStrings = {"Have a good day!", "<3", "AutoGG By Sk1er!"};
    private TriggersSchema triggers;
    private AutoGGConfig autoGGConfig;

    @Mod.EventHandler
    public void onFMLInitialization(FMLInitializationEvent event) {
        LOGGER.info("Starting AutoGG " + VERSION);

        LOGGER.info("Initializing AutoGG Config...");
        autoGGConfig = new AutoGGConfig();

        LOGGER.info("Fetching triggers...");
        Multithreading.runAsync(new RetrieveTriggersTask());

        LOGGER.info("Registering chat handler...");
        MinecraftForge.EVENT_BUS.register(new AutoGGHandler());

        LOGGER.info("Registering command...");
        new AutoGGCommand().register();
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
