package club.sk1er.mods.autogg.detectors.ip;

import club.sk1er.mods.autogg.detectors.ConnectionInfo;
import club.sk1er.mods.autogg.detectors.IDetector;
import club.sk1er.mods.autogg.handlers.patterns.PatternHandler;

public class ServerIPDetector implements IDetector {
    @Override
    public boolean detect(String data, ConnectionInfo connection) {
        return connection.address() != null && PatternHandler.INSTANCE.getOrRegisterPattern(data).matcher(connection.address()).matches();
    }
}
