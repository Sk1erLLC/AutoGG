package club.sk1er.mods.autogg.handlers.gg;

import club.sk1er.mods.autogg.detectors.ConnectionInfo;
import club.sk1er.mods.autogg.tasks.data.Server;
import club.sk1er.mods.autogg.tasks.data.Trigger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerTrackerTest {
    private static Server server(String name) {
        return new Server(name, "SERVER_IP", ".*", "", new Trigger[0], new String[0], null, null);
    }

    @Test
    void keepsTheResultForTheCurrentConnection() {
        ServerTracker tracker = new ServerTracker();
        ConnectionInfo hypixel = new ConnectionInfo("Hypixel BungeeCord (1) <- x", "mc.hypixel.net");
        tracker.connect(hypixel);
        Server found = server("Hypixel");
        assertTrue(tracker.publish(tracker.connection(), found));
        assertSame(found, tracker.server());
    }

    @Test
    void aSlowResultForAnOldConnectionIsDropped() {
        ServerTracker tracker = new ServerTracker();
        ConnectionInfo first = new ConnectionInfo(null, "mineplex.com");
        tracker.connect(first);
        ConnectionInfo second = new ConnectionInfo(null, "play.example.net");
        tracker.connect(second);

        assertFalse(tracker.publish(first, server("Mineplex")));
        assertNull(tracker.server());
        assertTrue(tracker.publish(second, null));
        assertNull(tracker.server());
    }

    @Test
    void joiningClearsThePreviousServer() {
        ServerTracker tracker = new ServerTracker();
        ConnectionInfo first = new ConnectionInfo(null, "mineplex.com");
        tracker.connect(first);
        tracker.publish(first, server("Mineplex"));
        tracker.connect(new ConnectionInfo(null, "mineplex.com"));
        assertNull(tracker.server());
    }

    @Test
    void nothingIsKeptAfterDisconnecting() {
        ServerTracker tracker = new ServerTracker();
        ConnectionInfo connection = new ConnectionInfo(null, "mineplex.com");
        tracker.connect(connection);
        tracker.publish(connection, server("Mineplex"));
        tracker.disconnect();
        assertNull(tracker.server());
        assertNull(tracker.connection());
        assertFalse(tracker.publish(connection, server("Mineplex")));
        assertFalse(tracker.publish(null, server("Mineplex")));
    }
}
