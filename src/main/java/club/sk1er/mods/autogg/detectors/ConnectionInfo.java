package club.sk1er.mods.autogg.detectors;

import org.jetbrains.annotations.Nullable;

/**
 * What the detectors look at, read from the game on the client thread when the player joins a server.
 *
 * @param brand   the server's brand, e.g. "Hypixel BungeeCord (2025.10.1) <- vanilla"
 * @param address the address from the server list, e.g. "mc.hypixel.net"
 */
public record ConnectionInfo(@Nullable String brand, @Nullable String address) {
}
