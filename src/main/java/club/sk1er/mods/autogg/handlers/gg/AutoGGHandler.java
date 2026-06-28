package club.sk1er.mods.autogg.handlers.gg;

import club.sk1er.mods.autogg.AutoGG;
import club.sk1er.mods.autogg.config.AutoGGConfig;
import club.sk1er.mods.autogg.handlers.patterns.PatternHandler;
import club.sk1er.mods.autogg.tasks.data.Server;
import club.sk1er.mods.autogg.tasks.data.Trigger;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.concurrent.TimeUnit;

import static club.sk1er.mods.autogg.AutoGG.POOL;

public class AutoGGHandler {
    private volatile Server server;
    private long lastGG = 0;

    public void register() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            server = null;
            if (!AutoGGConfig.get().isModEnabled()) return;

            POOL.submit(() -> {
                for (Server s : AutoGG.INSTANCE.getTriggers().getServers()) {
                    try {
                        if (s.getDetectionHandler().getDetector().detect(s.getData())) {
                            server = s;
                            return;
                        }
                    } catch (Throwable ignored) {
                    }
                }
            });

            if (!AutoGG.INSTANCE.usingEnglish && client.player != null) {
                client.player.sendSystemMessage(Component.literal(
                    ChatFormatting.GOLD + "[AutoGG] " + ChatFormatting.RED +
                    "We've detected your Hypixel language isn't set to English! " +
                    "AutoGG will not work on other languages."
                ));
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> server = null);

        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) ->
            handleIncoming(message.getString())
        );

        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if (overlay) return true;
            return handleIncoming(message.getString());
        });
    }

    // Returns false to cancel the message, true to allow it
    private boolean handleIncoming(String rawMessage) {
        if (!AutoGGConfig.get().isModEnabled() || server == null) return true;

        String stripped = ChatFormatting.stripFormatting(rawMessage);

        for (Trigger trigger : server.getTriggers()) {
            switch (trigger.getType()) {
                case ANTI_GG:
                    if (AutoGGConfig.get().isAntiGGEnabled() &&
                        PatternHandler.INSTANCE.getOrRegisterPattern(trigger.getPattern()).matcher(stripped).matches()) {
                        return false;
                    }
                    break;
                case ANTI_KARMA:
                    if (AutoGGConfig.get().isAntiKarmaEnabled() &&
                        PatternHandler.INSTANCE.getOrRegisterPattern(trigger.getPattern()).matcher(stripped).matches()) {
                        return false;
                    }
                    break;
                default:
                    break;
            }
        }

        POOL.submit(() -> {
            for (Trigger trigger : server.getTriggers()) {
                switch (trigger.getType()) {
                    case NORMAL:
                        if (PatternHandler.INSTANCE.getOrRegisterPattern(trigger.getPattern()).matcher(stripped).matches()) {
                            invokeGG();
                            return;
                        }
                        break;
                    case CASUAL:
                        if (AutoGGConfig.get().isCasualAutoGGEnabled() &&
                            PatternHandler.INSTANCE.getOrRegisterPattern(trigger.getPattern()).matcher(stripped).matches()) {
                            invokeGG();
                            return;
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        return true;
    }

    private void invokeGG() {
        if (server == null) return;
        if (System.currentTimeMillis() - lastGG < 10_000) return;
        lastGG = System.currentTimeMillis();

        String prefix = server.getMessagePrefix();
        String ggMessage = AutoGG.INSTANCE.getPrimaryGGStrings()[AutoGGConfig.get().getAutoGGPhrase()];
        int delay = AutoGGConfig.get().getAutoGGDelay();

        String firstMsg = prefix.isEmpty() ? ggMessage : prefix + " " + ggMessage;
        POOL.schedule(() -> sendChat(firstMsg), delay, TimeUnit.SECONDS);

        if (AutoGGConfig.get().isSecondaryEnabled()) {
            String secondMsg = AutoGG.INSTANCE.getSecondaryGGStrings()[AutoGGConfig.get().getAutoGGPhrase2()];
            String secondFull = prefix.isEmpty() ? secondMsg : prefix + " " + secondMsg;
            int secondaryDelay = delay + AutoGGConfig.get().getSecondaryDelay();
            POOL.schedule(() -> sendChat(secondFull), secondaryDelay, TimeUnit.SECONDS);
        }
    }

    private static void sendChat(String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.getConnection() != null) {
            mc.getConnection().sendChat(message);
        }
    }
}
