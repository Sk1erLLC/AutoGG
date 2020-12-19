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
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.modcore.api.ModCoreAPI;
import net.modcore.api.utils.GuiUtil;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class AutoGGCommand extends CommandBase {

    private final String prefix = AutoGG.instance.getPrefix();
    private static final SimpleDateFormat ISO_8601 = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat LOCALE_FORMAT = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());

    private static Date parseDate(String date) {
        try {
            return ISO_8601.parse(date);
        } catch (ParseException e) {
            return new Date(0);
        }
    }

    @Override
    public String getCommandName() {
        return "autogg";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + getCommandName() + " [refresh|triggers|info|credits|help]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            GuiUtil.open(AutoGG.instance.getAutoGGConfig().gui());
        } else {
            switch (args[0]) {
                case "refresh": {
                    ModCoreAPI.getMinecraftUtil().sendMessage(prefix, ChatColor.YELLOW + "Fetching triggers...");
                    AutoGG.downloadTriggers(true);
                    AutoGGListener.switchTriggerset();
                    break;
                }
                case "triggers": {
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

                    break;
                }
                case "info": {
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

                    break;
                }
                case "credits": {
                    ModCoreAPI.getMinecraftUtil().sendMessage(prefix, ChatColor.GREEN +
                        "AutoGG Originally created by 2Pi, continued by Sk1er LLC. " +
                        "Regex update & multi-server support by SirNapkin1334.");
                    ModCoreAPI.getMinecraftUtil().sendMessage(prefix, ChatColor.GREEN +
                        "Additional special thanks to: LlamaLad7, FalseHonesty, DJTheRedstoner, " +
                        "Pluggs and Unextracted!");
                    break; // Lots of general help x3, General help, Getting antigg strings x2
                }
                case "toggle": {
                    ModCoreAPI.getMinecraftUtil().sendMessage(prefix, (AutoGG.instance.getAutoGGConfig().toggle() ? "En" : "Dis") + "abled AutoGG.");
                    break;
                }
                default: { // thank you asbyth!
                    ChatComponentText supportDiscordLink = new ChatComponentText(prefix + ChatColor.GREEN +
                        "For support with AutoGG, go to https://sk1er.club/support-discord.");
                    supportDiscordLink.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                        "https://sk1er.club/support-discord"));
                    supportDiscordLink.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ChatComponentText("Click to join our support Discord.")));

                    ChatComponentText discordLink = new ChatComponentText(prefix + ChatColor.GREEN +
                            "For the community server for all Sk1er mods, go to https://discord.gg/sk1er.");
                    discordLink.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                            "https://discord.gg/sk1er"));
                    discordLink.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ChatComponentText("Click to join our community Discord.")));


                    ChatComponentText autoGGConfig = new ChatComponentText(prefix + ChatColor.GREEN +
                        "To configure AutoGG, run /autogg.");
                    autoGGConfig.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/autogg"));
                    autoGGConfig.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ChatComponentText("Click to run /autogg.")));

                    Minecraft.getMinecraft().thePlayer.addChatComponentMessage(supportDiscordLink);
                    Minecraft.getMinecraft().thePlayer.addChatComponentMessage(discordLink);
                    Minecraft.getMinecraft().thePlayer.addChatComponentMessage(autoGGConfig);
                    ModCoreAPI.getMinecraftUtil().sendMessage(prefix, ChatColor.GREEN +
                        "AutoGG Commands: refresh, info, credits, help");
                    // help doesn't actually exist but that's our secret
                    break;
                }
            }
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args,
            "refresh", "info", "credits", "help") : null;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return -1;
    }
}
