package club.sk1er.mods.autogg.handlers.gg;

import club.sk1er.mods.autogg.detectors.ConnectionInfo;
import club.sk1er.mods.autogg.tasks.data.Server;
import org.jetbrains.annotations.Nullable;

/**
 * The connection the player is on and the server detected for it. Detection runs on the pool, so a result is only
 * kept if it's for the connection that is still current: a slow detection for an old connection can't overwrite a
 * newer one.
 */
public final class ServerTracker {
    private ConnectionInfo connection;
    private Server server;

    public synchronized void connect(ConnectionInfo connection) {
        this.connection = connection;
        this.server = null;
    }

    public synchronized void disconnect() {
        this.connection = null;
        this.server = null;
    }

    @Nullable
    public synchronized ConnectionInfo connection() {
        return connection;
    }

    /**
     * @return true when the result was kept, false when the player has since left or moved to another connection
     */
    public synchronized boolean publish(ConnectionInfo detectedFor, @Nullable Server found) {
        if (detectedFor == null || detectedFor != connection) return false;
        server = found;
        return true;
    }

    @Nullable
    public synchronized Server server() {
        return server;
    }
}
