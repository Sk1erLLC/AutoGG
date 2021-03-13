package club.sk1er.mods.autogg.command;

import club.sk1er.mods.autogg.AutoGG;
import net.minecraft.client.gui.GuiScreen;
import net.modcore.api.commands.Command;
import net.modcore.api.commands.DefaultHandler;
import net.modcore.api.utils.GuiUtil;

public class AutoGGCommand extends Command  {
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
}
