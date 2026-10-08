package club.sk1er.mods.autogg.tasks.data;

import club.sk1er.mods.autogg.detectors.ConnectionInfo;
import org.jetbrains.annotations.Nullable;

public class TriggersSchema {
    private final Server[] servers;

    public TriggersSchema(Server[] servers) {
        this.servers = servers;
    }

    public Server[] getServers() {
        return servers;
    }

    /**
     * @return the first server whose detector matches the connection, or null when AutoGG doesn't support it
     */
    @Nullable
    public Server findServer(ConnectionInfo connection) {
        if (servers == null) return null;
        for (Server s : servers) {
            try {
                if (s.getDetectionHandler().getDetector().detect(s.getData(), connection)) {
                    return s;
                }
            } catch (Throwable e) {
                // Stop log spam
            }
        }
        return null;
    }
}
