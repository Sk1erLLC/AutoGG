package club.sk1er.mods.autogg.command;

import club.sk1er.mods.autogg.AutoGG;
import club.sk1er.mods.autogg.handlers.gg.AutoGGHandler;
import club.sk1er.mods.autogg.tasks.RetrieveTriggersTask;
import gg.essential.universal.ChatColor;
import gg.essential.universal.wrappers.message.UTextComponent;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

import static club.sk1er.mods.autogg.AutoGG.POOL;

public class AutoGGCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "autogg";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "autogg";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 1 && args[0].equalsIgnoreCase("refresh")) {
            POOL.submit(new RetrieveTriggersTask());
            (new UTextComponent(ChatColor.GREEN + "Refreshed triggers!")).chat();
            return;
        }
        AutoGGHandler.displayScreen = AutoGG.INSTANCE.getAutoGGConfig().gui();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
