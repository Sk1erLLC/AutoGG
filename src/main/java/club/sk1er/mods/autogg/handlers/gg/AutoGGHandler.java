package club.sk1er.mods.autogg.handlers.gg;

import club.sk1er.mods.autogg.AutoGG;
import club.sk1er.mods.autogg.handlers.patterns.PatternHandler;
import club.sk1er.mods.autogg.tasks.data.Server;
import club.sk1er.mods.autogg.tasks.data.Trigger;
import gg.essential.api.utils.Multithreading;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.concurrent.TimeUnit;

/**
 * Where the magic happens...
 * We handle which server's triggers should be used
 * and how to detect which server the player is currently
 * on.
 *
 * @author ChachyDev
 */
public class AutoGGHandler {
    private volatile Server server;

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.entity == Minecraft.getMinecraft().thePlayer && AutoGG.INSTANCE.getAutoGGConfig().isModEnabled()) {
            Multithreading.runAsync(() -> {
                for (Server s : AutoGG.INSTANCE.getTriggers().getServers()) {
                    try {
                        if (s.getDetectionHandler().getDetector().detect(s.getData())) {
                            server = s;
                            return;
                        }
                    } catch (Throwable e) {
                        // Stop log spam
                    }
                }

                // In case if it's not null and we couldn't find the triggers for the current server.
                server = null;
            });
        }
    }

    @SubscribeEvent
    public void onClientChatReceived(ClientChatReceivedEvent event) {
        String stripped = EnumChatFormatting.getTextWithoutFormattingCodes(event.message.getUnformattedText());

        if (AutoGG.INSTANCE.getAutoGGConfig().isModEnabled() && server != null) {
            for (Trigger trigger : server.getTriggers()) {
                switch (trigger.getType()) {
                    case ANTI_GG:
                        if (AutoGG.INSTANCE.getAutoGGConfig().isAntiGGEnabled()) {
                            if (PatternHandler.INSTANCE.getOrRegisterPattern(trigger.getPattern()).matcher(stripped).matches()) {
                                event.setCanceled(true);
                                return;
                            }
                        }
                        break;
                    case ANTI_KARMA:
                        if (AutoGG.INSTANCE.getAutoGGConfig().isAntiKarmaEnabled()) {
                            if (PatternHandler.INSTANCE.getOrRegisterPattern(trigger.getPattern()).matcher(stripped).matches()) {
                                event.setCanceled(true);
                                return;
                            }
                        }
                        break;
                }
            }

            Multithreading.runAsync(() -> {
                // Casual GG feature
                for (Trigger trigger : server.getTriggers()) {
                    switch (trigger.getType()) {
                        case NORMAL:
                            if (PatternHandler.INSTANCE.getOrRegisterPattern(trigger.getPattern()).matcher(stripped).matches()) {
                                invokeGG();
                                return;
                            }
                            break;

                        case CASUAL:
                            if (AutoGG.INSTANCE.getAutoGGConfig().isCasualAutoGGEnabled()) {
                                if (PatternHandler.INSTANCE.getOrRegisterPattern(trigger.getPattern()).matcher(stripped).matches()) {
                                    invokeGG();
                                    return;
                                }
                            }
                            break;
                    }
                }
            });
        }
    }

    private void invokeGG() {
        // Better safe than sorry
        if (server != null) {
            String prefix = server.getMessagePrefix();

            String ggMessage = AutoGG.INSTANCE.getPrimaryGGStrings()[AutoGG.INSTANCE.getAutoGGConfig().getAutoGGPhrase()];
            int delay = AutoGG.INSTANCE.getAutoGGConfig().getAutoGGDelay();

            Multithreading.schedule(() -> Minecraft.getMinecraft().thePlayer.sendChatMessage(prefix.isEmpty() ? ggMessage : prefix + " " + ggMessage), delay, TimeUnit.SECONDS);

            if (AutoGG.INSTANCE.getAutoGGConfig().isSecondaryEnabled()) {
                String secondGGMessage = AutoGG.INSTANCE.getSecondaryGGStrings()[AutoGG.INSTANCE.getAutoGGConfig().getAutoGGPhrase2()];
                int secondaryDelay = AutoGG.INSTANCE.getAutoGGConfig().getSecondaryDelay();

                Multithreading.schedule(() -> Minecraft.getMinecraft().thePlayer.sendChatMessage(prefix.isEmpty() ? ggMessage : prefix + " " + secondGGMessage), secondaryDelay, TimeUnit.SECONDS);
            }
        }
    }
}
