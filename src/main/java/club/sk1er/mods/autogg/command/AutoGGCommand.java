package club.sk1er.mods.autogg.command;

import club.sk1er.mods.autogg.AutoGG;
import club.sk1er.mods.autogg.tasks.RetrieveTriggersTask;
import club.sk1er.mods.core.universal.ChatColor;
import club.sk1er.mods.core.universal.wrappers.message.UTextComponent;
import net.modcore.api.ModCoreAPI;
import net.modcore.api.commands.Command;
import net.modcore.api.commands.DefaultHandler;
import net.modcore.api.commands.SubCommand;
import net.modcore.api.utils.GuiUtil;
import net.modcore.api.utils.Multithreading;

import java.util.Objects;

public class AutoGGCommand extends Command {
    public AutoGGCommand() {
        super("autogg");
    }

    @DefaultHandler
    public void handle() {
        GuiUtil.open(Objects.requireNonNull(AutoGG.INSTANCE.getAutoGGConfig().gui()));
    }

    @SubCommand(value = "refresh", description = "Refreshes your loaded triggers.")
    public void refresh() {
        Multithreading.runAsync(new RetrieveTriggersTask());
        ModCoreAPI.getMinecraftUtil().sendMessage(new UTextComponent(ChatColor.GREEN + "Refreshed triggers!"));
    }
}
