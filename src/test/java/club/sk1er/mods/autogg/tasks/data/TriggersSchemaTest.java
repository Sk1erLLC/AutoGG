package club.sk1er.mods.autogg.tasks.data;

import club.sk1er.mods.autogg.TriggersFixture;
import club.sk1er.mods.autogg.detectors.ConnectionInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TriggersSchemaTest {
    private final TriggersSchema triggers = TriggersFixture.load();

    @Test
    void hypixelIsDetectedByItsBrand() {
        Server server = triggers.findServer(new ConnectionInfo("Hypixel BungeeCord (2025.10.1) <- vanilla", "mc.hypixel.net"));
        assertEquals("Hypixel Server", server.getName());
        assertEquals("/ac", server.getMessagePrefix());
    }

    @Test
    void hypixelNeedsItsBrandNotItsAddress() {
        assertNull(triggers.findServer(new ConnectionInfo("vanilla", "mc.hypixel.net")));
    }

    @Test
    void addressServersAreDetectedByAddress() {
        assertEquals("Mineplex Server", triggers.findServer(new ConnectionInfo(null, "us.mineplex.com")).getName());
        assertEquals("Mineplex Server", triggers.findServer(new ConnectionInfo("vanilla", "mineplex.com")).getName());
        assertEquals("SuperAwesome Server", triggers.findServer(new ConnectionInfo(null, "superawesome.dk")).getName());
    }

    @Test
    void otherServersAreNotSupported() {
        assertNull(triggers.findServer(new ConnectionInfo("vanilla", "play.example.net")));
        assertNull(triggers.findServer(new ConnectionInfo(null, null)));
    }

    @Test
    void emptySchemaSupportsNothing() {
        assertNull(new TriggersSchema(new Server[0]).findServer(new ConnectionInfo("Hypixel BungeeCord (1) <- x", null)));
        assertNull(new TriggersSchema(null).findServer(new ConnectionInfo(null, "mineplex.com")));
    }

    @Test
    void unknownDetectorKindsAreSkipped() {
        Server broken = new Server("Broken", "NO_SUCH_KIND", ".*", "", new Trigger[0], new String[0], null, null);
        Server ip = new Server("Ip", "SERVER_IP", "^example\\.net$", "", new Trigger[0], new String[0], null, null);
        assertEquals("Ip", new TriggersSchema(new Server[]{broken, ip}).findServer(new ConnectionInfo(null, "example.net")).getName());
    }
}
