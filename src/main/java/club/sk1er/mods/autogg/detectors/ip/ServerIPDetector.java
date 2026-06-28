package club.sk1er.mods.autogg.detectors.ip;

import club.sk1er.mods.autogg.detectors.IDetector;
import club.sk1er.mods.autogg.handlers.patterns.PatternHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

public class ServerIPDetector implements IDetector {
    @Override
    public boolean detect(String data) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        ServerData serverData = mc.getCurrentServer();
        if (serverData == null) return false;
        return PatternHandler.INSTANCE.getOrRegisterPattern(data).matcher(serverData.ip).matches();
    }
}
