package club.sk1er.autogg;

import club.sk1er.autogg.command.AutoGGCommand;
import club.sk1er.autogg.config.AutoGGConfig;
import club.sk1er.autogg.listener.AutoGGListener;
import club.sk1er.modcore.ModCoreInstaller;
import club.sk1er.mods.core.util.Multithreading;
import club.sk1er.mods.core.util.WebUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import java.util.ArrayList;
import java.util.List;

@Mod(modid = "autogg", name = "AutoGG", version = "3.0")
public class AutoGG {

    private List<String> triggers = new ArrayList<>();
    private AutoGGConfig autoGGConfig;
    private boolean running;

    @Mod.Instance("autogg")
    public static AutoGG instance;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ModCoreInstaller.initializeModCore(Minecraft.getMinecraft().mcDataDir);

        autoGGConfig = new AutoGGConfig();
        autoGGConfig.preload();

        running = false;

        ClientCommandHandler.instance.registerCommand(new AutoGGCommand());
        Multithreading.runAsync(() -> {
            try {
                for (JsonElement element : new JsonParser().parse(WebUtil.fetchString("http://static.sk1er.club/autogg/triggers.json")).getAsJsonArray()) {
                    triggers.add(element.getAsString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        MinecraftForge.EVENT_BUS.register(new AutoGGListener());
    }

    public List<String> getTriggers() {
        return triggers;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public AutoGGConfig getAutoGGConfig() {
        return autoGGConfig;
    }
}
