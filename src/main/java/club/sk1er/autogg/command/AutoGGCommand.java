package club.sk1er.autogg.command;

import club.sk1er.autogg.AutoGG;
import club.sk1er.mods.core.ModCore;
import club.sk1er.mods.core.universal.ChatColor;
import club.sk1er.mods.core.util.MinecraftUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;

import java.util.regex.Pattern;

public class AutoGGCommand extends CommandBase {
    /**
     * Gets the name of the command
     */
    @Override
    public String getCommandName() {
        return "autogg";
    }

    /**
     * Gets the usage string for the command.
     *
     * @param sender user the command is being sent from
     */
    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + getCommandName() + " [refresh|triggers|info|help]";
    }

    /**
     * Callback when the command is invoked
     *
     * @param sender user the command is being sent from
     * @param args   arguments provided via command
     */
    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            ModCore.getInstance().getGuiHandler().open(AutoGG.instance.getAutoGGConfig().gui());
        } else {
            switch (args[0]) {
                case "refresh":
                    MinecraftUtils.sendMessage(AutoGG.instance.getPrefix(), ChatColor.YELLOW + "Fetching triggers...");
                    AutoGG.fetchTriggers(true);
                    break;
                case "triggers": // print triggers
                    MinecraftUtils.sendMessage(AutoGG.instance.getPrefix(), "\n" + ChatColor.AQUA + "TRIGGERS:\n\n");

                    for (Pattern pattern : AutoGG.instance.getTriggers()) {
                        MinecraftUtils.sendMessage("", pattern.toString());
                    }

                    MinecraftUtils.sendMessage("", "\n" + ChatColor.AQUA + "CASUAL TRIGGERS:\n\n");

                    for (Pattern pattern : AutoGG.instance.getCasualTriggers()) {
                        MinecraftUtils.sendMessage("", pattern.toString());
                    }

                    break;
                case "info":
                    MinecraftUtils.sendMessage(AutoGG.instance.getPrefix(), ChatColor.GREEN + "Mod Version: " + AutoGG.VERSION);
                    MinecraftUtils.sendMessage(AutoGG.instance.getPrefix(), ChatColor.GREEN + "Triggers Version: " + AutoGG.instance.getTriggerData().get("version").toString().replaceAll("\"", ""));
                    MinecraftUtils.sendMessage(AutoGG.instance.getPrefix(), ChatColor.GREEN + "Triggers last updated on " + AutoGG.instance.getTriggerData().get("date").toString().replaceAll("\"", ""));
                    MinecraftUtils.sendMessage(AutoGG.instance.getPrefix(), ChatColor.GREEN + "Triggers info message: " + AutoGG.instance.getTriggerData().get("note").toString().replaceAll("\"", ""));
                    MinecraftUtils.sendMessage(AutoGG.instance.getPrefix(), ChatColor.GREEN + Integer.toString(AutoGG.instance.getTriggers().size()) + " Triggers, " + AutoGG.instance.getCasualTriggers().size() + " Casual Triggers");
                    break;

                default: // thank you asbyth!
                    ChatComponentText discordLink = new ChatComponentText(AutoGG.instance.getPrefix() + ChatColor.GREEN + "For support with AutoGG, go to discord.gg/sk1er.");
                    discordLink.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/sk1er"));
                    discordLink.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Click to join our Discord.")));

                    ChatComponentText autoGGConfig = new ChatComponentText(AutoGG.instance.getPrefix() + ChatColor.GREEN + "To configure AutoGG, run /autogg.");
                    autoGGConfig.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/autogg"));
                    autoGGConfig.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Click to run /autogg.")));

                    Minecraft.getMinecraft().thePlayer.addChatComponentMessage(discordLink);
                    Minecraft.getMinecraft().thePlayer.addChatComponentMessage(autoGGConfig);
                    MinecraftUtils.sendMessage(AutoGG.instance.getPrefix(), ChatColor.GREEN + "AutoGG Commands: refresh, triggers, info, help"); // help doesn't actually exist but that's our secret
                    break;
            }
        }
    }
    
    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, "refresh", "triggers", "info", "help") : null;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return -1;
    }
}
