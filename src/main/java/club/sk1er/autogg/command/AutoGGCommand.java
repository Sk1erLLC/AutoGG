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
        return "/" + getCommandName();
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
            if (args[0].equals("refresh")) {
                AutoGG.fetchTriggers();
                MinecraftUtils.sendMessage(ChatColor.GREEN + "Fetched triggers!");
            } else {
                // thank you asbyth!
                ChatComponentText discordLink = new ChatComponentText(ChatColor.GREEN + "For support with AutoGG, go to discord.gg/sk1er.");
                discordLink.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/sk1er"));
                discordLink.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Click to join our Discord.")));

                ChatComponentText autoGGRefresh = new ChatComponentText(ChatColor.GREEN + "To refresh triggers, run /autogg refresh.");
                autoGGRefresh.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/autogg refresh"));
                autoGGRefresh.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Click to run /autogg refresh.")));

                ChatComponentText autoGGConfig = new ChatComponentText(ChatColor.GREEN + "To configure AutoGG, run /autogg.");
                autoGGConfig.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/autogg"));
                autoGGConfig.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Click to run /autogg.")));

                Minecraft.getMinecraft().thePlayer.addChatComponentMessage(discordLink);
                Minecraft.getMinecraft().thePlayer.addChatComponentMessage(autoGGRefresh);
                Minecraft.getMinecraft().thePlayer.addChatComponentMessage(autoGGConfig);
            }
        }
    }

    @Override
    public int getRequiredPermissionLevel() { return -1; }
}
