package club.sk1er.autogg.command;

import club.sk1er.autogg.AutoGG;
import club.sk1er.autogg.listener.AutoGGListener;
import club.sk1er.mods.core.ModCore;
import club.sk1er.mods.core.universal.ChatColor;
import club.sk1er.mods.core.util.MinecraftUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
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
            ModCore.getInstance().getGuiHandler().open(AutoGG.instance.getAutoGGConfig().gui());
        } else {
            switch (args[0]) {
                case "refresh": {
                    MinecraftUtils.sendMessage(prefix, ChatColor.YELLOW + "Fetching triggers...");
                    AutoGG.downloadTriggers(true);
                    AutoGGListener.switchTriggerset();
                    break;
                }
                case "triggers": {
                    for (String key : AutoGG.ggRegexes.keySet()) {
                        if (!AutoGG.ggRegexes.get(key).isEmpty()) {
                            MinecraftUtils.sendMessage(prefix, ChatColor.AQUA +
                                    key.replaceAll("_", " ").toUpperCase() + ":\n");
                            for (Pattern pattern : AutoGG.ggRegexes.get(key)) {
                                MinecraftUtils.sendMessage("  ", pattern.toString());
                            }
                        }
                    }

                    for (String key : AutoGG.otherRegexes.keySet()) {
                        MinecraftUtils.sendMessage(prefix, ChatColor.AQUA +
                                key.replaceAll("_", " ").toUpperCase() + ": " +
                                AutoGG.otherRegexes.get(key));
                    }

                    break;
                }
                case "info": {
                    MinecraftUtils.sendMessage(prefix, ChatColor.GREEN + "Mod Version: " + AutoGG.VERSION);
                    int g = AutoGG.ggRegexes.get("triggers").size();
                    int c = AutoGG.ggRegexes.get("casual_triggers").size();
                    try {
                        MinecraftUtils.sendMessage(prefix, ChatColor.GREEN +
                                "Triggers Version: " +
                                AutoGG.triggerMeta.get("version").replaceAll("\"", ""));
                        MinecraftUtils.sendMessage(prefix, ChatColor.GREEN +
                                "Triggers last updated on " +
                                LOCALE_FORMAT.format(parseDate(AutoGG.triggerMeta.get("upload_date")
                                        .replaceAll("\"", ""))));
                        MinecraftUtils.sendMessage(prefix, ChatColor.GREEN +
                                "Triggers info message: " +
                                AutoGG.triggerMeta.get("note").replaceAll("\"", ""));
                        MinecraftUtils.sendMessage(prefix, ChatColor.GREEN +
                                Integer.toString(g) + " Trigger" + (g == 1 ? "" : "s") + ", " + c + " Casual Trigger" +
                                (g == 1 ? "" : "s"));
                    } catch (NullPointerException e) {
                        MinecraftUtils.sendMessage(prefix, ChatColor.RED +
                                "Could not get Trigger Meta! Were the triggers downloaded properly?");
                        AutoGG.instance.getLogger().error("Could not get trigger meta.", e);
                    }

                    break;
                }
                case "credits": {
                    MinecraftUtils.sendMessage(prefix, ChatColor.GREEN +
                            "AutoGG Originally created by 2Pi, continued by Sk1er. " +
                            "Regex update & multi-server support by SirNapkin1334.");
                    MinecraftUtils.sendMessage(prefix, ChatColor.GREEN +
                            "Additional special thanks to: Asbyth, LlamaLad7, FalseHonesty, DJTheRedstoner, " +
                            "Pluggs and Unextracted!");
                    break; // Lots of general help x3, General help, Getting antigg strings x2
                }
                default: { // thank you asbyth!
                    ChatComponentText discordLink = new ChatComponentText(prefix + ChatColor.GREEN +
                            "For support with AutoGG, go to discord.gg/sk1er.");
                    discordLink.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                            "https://discord.gg/sk1er"));
                    discordLink.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ChatComponentText("Click to join our Discord.")));

                    ChatComponentText autoGGConfig = new ChatComponentText(prefix + ChatColor.GREEN +
                            "To configure AutoGG, run /autogg.");
                    autoGGConfig.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/autogg"));
                    autoGGConfig.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ChatComponentText("Click to run /autogg.")));

                    Minecraft.getMinecraft().thePlayer.addChatComponentMessage(discordLink);
                    Minecraft.getMinecraft().thePlayer.addChatComponentMessage(autoGGConfig);
                    MinecraftUtils.sendMessage(prefix, ChatColor.GREEN +
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
