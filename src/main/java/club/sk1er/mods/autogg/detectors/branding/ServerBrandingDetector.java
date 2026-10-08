package club.sk1er.mods.autogg.detectors.branding;

import club.sk1er.mods.autogg.detectors.ConnectionInfo;
import club.sk1er.mods.autogg.detectors.IDetector;
import club.sk1er.mods.autogg.handlers.patterns.PatternHandler;

public class ServerBrandingDetector implements IDetector {
    @Override
    public boolean detect(String data, ConnectionInfo connection) {
        return connection.brand() != null && PatternHandler.INSTANCE.getOrRegisterPattern(data).matcher(connection.brand()).matches();
    }
}
