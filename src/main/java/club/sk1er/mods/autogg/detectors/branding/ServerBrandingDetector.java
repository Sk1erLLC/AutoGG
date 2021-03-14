package club.sk1er.mods.autogg.detectors.branding;

import club.sk1er.mods.autogg.detectors.IDetector;
import club.sk1er.mods.autogg.handlers.patterns.PatternHandler;
import net.minecraft.client.entity.EntityPlayerSP;

public class ServerBrandingDetector implements IDetector {
    @Override
    public boolean detect(String detector, EntityPlayerSP player) {
        return PatternHandler.INSTANCE.getPattern(detector).matcher(player.getClientBrand()).matches();
    }
}
