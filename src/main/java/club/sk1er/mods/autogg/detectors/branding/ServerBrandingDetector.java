package club.sk1er.mods.autogg.detectors.branding;

import club.sk1er.mods.autogg.detectors.IDetector;
import club.sk1er.mods.autogg.handlers.patterns.PatternHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;

public class ServerBrandingDetector implements IDetector {
    @Override
    public boolean detect(String data) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) return false;
        ClientCommonPacketListenerImpl connection = (ClientCommonPacketListenerImpl) mc.getConnection();
        String brand = connection.serverBrand();
        if (brand == null) return false;
        return PatternHandler.INSTANCE.getOrRegisterPattern(data).matcher(brand).matches();
    }
}
