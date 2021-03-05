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

package club.sk1er.mods.autogg.command;

import club.sk1er.mods.autogg.AutoGG;
import club.sk1er.mods.autogg.listener.AutoGGListener;
import club.sk1er.mods.core.universal.ChatColor;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.modcore.api.ModCoreAPI;
import net.modcore.api.commands.Command;
import net.modcore.api.commands.DefaultHandler;
import net.modcore.api.commands.SubCommand;
import net.modcore.api.utils.GuiUtil;
import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public class AutoGGCommand extends Command {

    private final String prefix = AutoGG.instance.getPrefix();
    private static final SimpleDateFormat ISO_8601 = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat LOCALE_FORMAT = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());

    public AutoGGCommand() {
        super("autogg");
    }

    @DefaultHandler
    public void handle() {
        GuiUtil.open(Objects.requireNonNull(AutoGG.instance.getAutoGGConfig().gui()));
    }

    @SubCommand(value = "refresh", description = "Refresh the triggers list.")
    public void refresh() {
        ModCoreAPI.getMinecraftUtil().sendMessage(prefix, ChatColor.YELLOW + "Fetching triggers...");
        AutoGG.downloadTriggers(true);
        AutoGGListener.switchTriggerset();
    }

    @SubCommand(value = "triggers", description = "Dump the currently fetched triggers for debugging.")
    public void triggers() {
        for (String key : AutoGG.ggRegexes.keySet()) {
            if (!AutoGG.ggRegexes.get(key).isEmpty()) {
                ModCoreAPI.getMinecraftUtil().sendMessage(prefix, ChatColor.AQUA +
                    key.replaceAll("_", " ").toUpperCase(Locale.ENGLISH) + ":");
                for (Pattern pattern : AutoGG.ggRegexes.get(key)) {
                    ModCoreAPI.getMinecraftUtil().sendMessage("  ", pattern.toString());
                }
            }
        }

        for (String key : AutoGG.otherRegexes.keySet()) {
            if (!"$^".equals(AutoGG.otherRegexes.get(key).toString())) {
                ModCoreAPI.getMinecraftUtil().sendMessage(prefix, ChatColor.AQUA +
                    key.replaceAll("_", " ").toUpperCase(Locale.ENGLISH) + ": " + ChatColor.RESET +
                    AutoGG.otherRegexes.get(key));
            }
        }
    }

    @SubCommand(value = "info", description = "Display information about AutoGG.")
    public void info() {
        ModCoreAPI.getMinecraftUtil().sendMessage(prefix, ChatColor.GREEN + "Mod Version: " + AutoGG.VERSION);
        try {
            int triggersSize = AutoGG.ggRegexes.get("triggers").size();
            int casualTriggersSize = AutoGG.ggRegexes.get("casual_triggers").size();
            ModCoreAPI.getMinecraftUtil().sendMessage(prefix, ChatColor.GREEN +
                "Triggers Version: " +
                AutoGG.triggerMeta.get("version").replaceAll("\"", ""));
            ModCoreAPI.getMinecraftUtil().sendMessage(prefix, ChatColor.GREEN +
                "Triggers last updated on " +
                LOCALE_FORMAT.format(parseDate(AutoGG.triggerMeta.get("upload_date")
                    .replaceAll("\"", ""))));
            ModCoreAPI.getMinecraftUtil().sendMessage(prefix, ChatColor.GREEN +
                "Triggers info message: " +
                AutoGG.triggerMeta.get("note").replaceAll("\"", ""));
            ModCoreAPI.getMinecraftUtil().sendMessage(prefix, ChatColor.GREEN +
                Integer.toString(triggersSize) + " Trigger" + (triggersSize == 1 ? "" : "s") + ", " + casualTriggersSize + " Casual Trigger" +
                (casualTriggersSize == 1 ? "" : "s"));
        } catch (NullPointerException e) {
            ModCoreAPI.getMinecraftUtil().sendMessage(prefix, ChatColor.RED +
                "Could not get Trigger Meta! Were the triggers downloaded properly?");
            AutoGG.instance.getLogger().error("Could not get trigger meta.", e);
        }
    }

    @SubCommand(value = "toggle", description = "Toggle the status of AutoGG.")
    public void toggle() {
        ModCoreAPI.getMinecraftUtil().sendMessage(prefix, (AutoGG.instance.getAutoGGConfig().toggle() ? "En" : "Dis") + "abled AutoGG.");
    }

    private static Date parseDate(String date) {
        try {
            return ISO_8601.parse(date);
        } catch (ParseException e) {
            return new Date(0);
        }
    }
}
