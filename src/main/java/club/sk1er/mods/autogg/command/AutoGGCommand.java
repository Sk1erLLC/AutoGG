package club.sk1er.mods.autogg.command;

import club.sk1er.mods.autogg.AutoGG;
import club.sk1er.mods.autogg.tasks.RetrieveTriggersTask;
import club.sk1er.mods.core.universal.ChatColor;
import net.minecraft.client.gui.GuiScreen;
import net.modcore.api.ModCoreAPI;
import net.modcore.api.commands.Command;
import net.modcore.api.commands.DefaultHandler;
import net.modcore.api.utils.GuiUtil;
import net.modcore.api.utils.Multithreading;

public class AutoGGCommand extends Command {
    public AutoGGCommand() {
        super("autogg");
    }

    @DefaultHandler
    public void handle() {
        GuiScreen gui = AutoGG.INSTANCE.getAutoGGConfig().gui();
        if (gui != null) {
            GuiUtil.open(gui);
        }
    }

    public void refresh() {
        Multithreading.runAsync(new RetrieveTriggersTask());
        ModCoreAPI.getMinecraftUtil().sendMessage(ChatColor.GREEN + "Refreshed triggers!");
    }
}
