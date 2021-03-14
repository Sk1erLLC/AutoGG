package club.sk1er.mods.autogg.detectors.ip;

import club.sk1er.mods.autogg.detectors.IDetector;
import club.sk1er.mods.autogg.handlers.patterns.PatternHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

public class ServerIPDetector implements IDetector {
    @Override
    public boolean detect(String detector) {
        return PatternHandler.INSTANCE.getPattern(detector).matcher(Minecraft.getMinecraft().getCurrentServerData().serverIP).matches();
    }
}
