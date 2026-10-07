package club.sk1er.mods.autogg.handlers.gg;

import club.sk1er.mods.autogg.AutoGG;
import club.sk1er.mods.autogg.config.AutoGGConfig;
import club.sk1er.mods.autogg.detectors.ConnectionInfo;
import club.sk1er.mods.autogg.tasks.data.Server;
import club.sk1er.mods.autogg.tasks.data.TriggersSchema;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;

import java.util.concurrent.TimeUnit;

import static club.sk1er.mods.autogg.AutoGG.POOL;

/**
 * Where the magic happens...
 * We handle which server's triggers should be used
 * and how to detect which server the player is currently
 * on.
 */
public class AutoGGHandler {
    private volatile Server server;
    private volatile ConnectionInfo connection;
    private final Cooldown ggCooldown = new Cooldown(10_000, System::currentTimeMillis);

    public void register() {
        ClientPlayConnectionEvents.JOIN.register((listener, sender, client) -> onJoinServer(client, listener));
        ClientPlayConnectionEvents.DISCONNECT.register((listener, client) -> {
            connection = null;
            server = null;
        });

        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> overlay || onChatReceived(message));
        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> onChatReceived(message));

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onJoinServer(Minecraft client, ClientPacketListener listener) {
        ServerData serverData = client.getCurrentServer();
        connection = new ConnectionInfo(listener.serverBrand(), serverData == null ? null : serverData.ip);

        if (AutoGG.INSTANCE.getAutoGGConfig().isModEnabled()) {
            detectServer();
            if (!AutoGG.INSTANCE.usingEnglish && client.player != null) {
                client.player.sendSystemMessage(Component.literal("AutoGG"));
                client.player.sendSystemMessage(Component.literal("We've detected your Hypixel language isn't set to English! AutoGG will not work on other languages.\n" +
                        "If this is a mistake, feel free to ignore it."));
            }
        }
    }

    /**
     * Finds the triggers for the server the player is on. Runs again when the triggers are (re)loaded,
     * so joining before they arrive doesn't leave AutoGG off for the whole session.
     */
    public void detectServer() {
        POOL.submit(() -> {
            ConnectionInfo connection = this.connection;
            TriggersSchema triggers = AutoGG.INSTANCE.getTriggers();
            // null when it's not a supported server, or we're not on one
            server = connection == null || triggers == null ? null : triggers.findServer(connection);
        });
    }

    /**
     * @return false to hide the message
     */
    private boolean onChatReceived(Component message) {
        Server server = this.server;
        AutoGGConfig config = AutoGG.INSTANCE.getAutoGGConfig();
        if (!config.isModEnabled() || server == null) return true;

        String stripped = ChatFormatting.stripFormatting(message.getString());

        if (TriggerMatcher.shouldHide(server, stripped, config.isAntiGGEnabled(), config.isAntiKarmaEnabled())) {
            return false;
        }

        POOL.submit(() -> {
            // Casual GG feature
            if (TriggerMatcher.shouldSayGG(server, stripped, config.isCasualAutoGGEnabled())) {
                invokeGG(server);
            }
        });

        return true;
    }

    private void invokeGG(Server server) {
        if (!ggCooldown.tryAcquire()) return;

        for (GGMessages.Message message : GGMessages.plan(server.getMessagePrefix(), AutoGG.INSTANCE.getAutoGGConfig(),
                AutoGG.INSTANCE.getPrimaryGGStrings(), AutoGG.INSTANCE.getSecondaryGGStrings())) {
            POOL.schedule(() -> sendMessage(message), message.delaySeconds(), TimeUnit.SECONDS);
        }
    }

    /**
     * Sends the message as the player, on the client thread.
     */
    private static void sendMessage(GGMessages.Message message) {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            ClientPacketListener connection = mc.getConnection();
            if (mc.player == null || connection == null) return;
            if (message.isCommand()) {
                connection.sendCommand(message.text().substring(1));
            } else {
                connection.sendChat(message.text());
            }
        });
    }

    /**
     * Gui Handling
     * The command runs while the chat screen is open, which closes after it, so the screen is opened on the next tick.
     */
    public static Screen displayScreen = null;

    private void onTick(Minecraft client) {
        if (displayScreen == null) return;
        //? if >= 26.2 {
        client.gui.setScreen(displayScreen);
        //?} else {
        // client.setScreen(displayScreen);
        //?}
        displayScreen = null;
    }
}
