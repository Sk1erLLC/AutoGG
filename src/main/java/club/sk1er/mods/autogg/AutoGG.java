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
import club.sk1er.mods.autogg.handlers.patterns.GGPhrases;
import club.sk1er.mods.autogg.handlers.web.WebHandler;
import club.sk1er.mods.autogg.tasks.RetrieveTriggersTask;
import club.sk1er.mods.autogg.tasks.data.TriggersSchema;
import club.sk1er.mods.autogg.util.LanguageCheck;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.Minecraft;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Contains the main class for AutoGG which handles trigger schema setting/getting and the main initialization code.
 *
 * @author ChachyDev
 */
public class AutoGG implements ClientModInitializer {

    public static AutoGG INSTANCE;

    private volatile TriggersSchema triggers;
    private AutoGGConfig autoGGConfig;
    private AutoGGHandler handler;

    // Assume English until the language check says otherwise, so the warning isn't shown before it has answered
    public volatile boolean usingEnglish = true;

    // Daemon threads, so a pending task can't keep the game running after it quits
    public static final ScheduledExecutorService POOL = Executors.newScheduledThreadPool(5, new ThreadFactory() {
        private final AtomicInteger count = new AtomicInteger();

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "AutoGG-" + count.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        }
    });

    @Override
    public void onInitializeClient() {
        INSTANCE = this;

        // The session is only guaranteed to be set up once the client has started
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> POOL.submit(this::checkUserLanguage));

        autoGGConfig = AutoGGConfig.load();

        GGPhrases.registerPlaceholders();

        handler = new AutoGGHandler();
        handler.register();
        AutoGGCommand.register();
        POOL.submit(new RetrieveTriggersTask());

        autoGGConfig.migrateDelays();
        autoGGConfig.save();
    }

    private void checkUserLanguage() {
        final String username = Minecraft.getInstance().getUser().getName();
        this.usingEnglish = LanguageCheck.isEnglish(WebHandler.fetchString("https://api.sk1er.club/language/" + username));
    }

    public TriggersSchema getTriggers() {
        return triggers;
    }

    public void setTriggers(TriggersSchema triggers) {
        this.triggers = triggers;
    }

    public AutoGGHandler getHandler() {
        return handler;
    }

    public AutoGGConfig getAutoGGConfig() {
        return autoGGConfig;
    }

    public String[] getPrimaryGGStrings() {
        return GGPhrases.PRIMARY;
    }

    public String[] getSecondaryGGStrings() {
        return GGPhrases.SECONDARY;
    }
}
