package club.sk1er.autogg.command;

import club.sk1er.autogg.AutoGG;
import club.sk1er.mods.core.ModCore;
import club.sk1er.mods.core.util.MinecraftUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;

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
            MinecraftUtils.sendMessage(EnumChatFormatting.RED + "AutoGG has no arguments. Usage: /autogg");
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return -1;
    }
}
