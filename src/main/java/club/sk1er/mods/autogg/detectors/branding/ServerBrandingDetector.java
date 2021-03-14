package club.sk1er.mods.autogg.detectors.branding;

import club.sk1er.mods.autogg.detectors.IDetector;
import club.sk1er.mods.autogg.handlers.patterns.PatternHandler;
import net.minecraft.client.Minecraft;

public class ServerBrandingDetector implements IDetector {
    @Override
    public boolean detect(String detector) {
        return PatternHandler.INSTANCE.getPattern(detector).matcher(Minecraft.getMinecraft().thePlayer.getClientBrand()).matches();
    }
}
