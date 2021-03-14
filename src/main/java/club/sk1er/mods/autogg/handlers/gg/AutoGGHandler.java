package club.sk1er.mods.autogg.handlers.gg;

import club.sk1er.mods.autogg.AutoGG;
import club.sk1er.mods.autogg.handlers.patterns.PatternHandler;
import club.sk1er.mods.autogg.tasks.data.Server;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.modcore.api.utils.Multithreading;

/**
 * Where the magic happens...
 * We handle which server's triggers should be used
 * and how to detect which server the player is currently
 * on.
 *
 * @author ChachyDev
 */

public class AutoGGHandler {
    private Server server;

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.entity instanceof EntityPlayerSP) {
            if (Minecraft.getMinecraft().thePlayer != null) {
                if (AutoGG.INSTANCE.getAutoGGConfig().isModEnabled()) {
                    Multithreading.runAsync(() -> {
                        for (Server s : AutoGG.INSTANCE.getTriggers().getServers()) {
                            if (s.getHandler().getDetector().detect(s.getDetector())) {
                                server = s;
                                return;
                            }
                        }

                        // In case if it's not null and we couldn't find the triggers for the current server.
                        server = null;
                    });
                }
            }
        }
    }

    @SubscribeEvent
    public void onClientChatReceived(ClientChatReceivedEvent event) {
        if (AutoGG.INSTANCE.getAutoGGConfig().isModEnabled()) {
            if (server != null) {
                Multithreading.runAsync(() -> {
                    // Anti-GG feature
                    if (AutoGG.INSTANCE.getAutoGGConfig().isAntiGGEnabled() && server.getAntiGGTrigger() != null) {
                        if (PatternHandler.INSTANCE.getPattern(server.getAntiGGTrigger()).matcher(event.message.getUnformattedText()).matches()) {
                            event.setCanceled(true);
                        }
                    }

                    // Anti Karma feature
                    if (AutoGG.INSTANCE.getAutoGGConfig().isAntiKarmaEnabled() && server.getAntiKarmaTrigger() != null) {
                        if (PatternHandler.INSTANCE.getPattern(server.getAntiKarmaTrigger()).matcher(event.message.getUnformattedText()).matches()) {
                            event.setCanceled(true);
                        }
                    }

                    String chatMessage = event.message.getUnformattedText();
                    // Casual GG feature
                    if (AutoGG.INSTANCE.getAutoGGConfig().isCasualAutoGGEnabled()) {
                        for (String trigger : server.getCasualTriggers()) {
                            if (PatternHandler.INSTANCE.getPattern(trigger).matcher(chatMessage).matches()) {
                                invokeGG();
                                return;
                            }
                        }
                    }

                    // Normal AutoGG feature
                    for (String trigger : server.getTriggers()) {
                        if (PatternHandler.INSTANCE.getPattern(trigger).matcher(chatMessage).matches()) {
                            invokeGG();
                            return;
                        }
                    }
                });
            }
        }
    }

    private void invokeGG() {
        // Better safe than sorry
        if (server != null) {
            String prefix = server.getMessagePrefix();
            String ggMessage = AutoGG.INSTANCE.getPrimaryGGStrings()[AutoGG.INSTANCE.getAutoGGConfig().getAutoGGPhrase()];
            int delay = AutoGG.INSTANCE.getAutoGGConfig().getAutoGGDelay();
            if (delay > 0) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Minecraft.getMinecraft().thePlayer.sendChatMessage(prefix.isEmpty() ? ggMessage : prefix + " " + ggMessage);

            if (AutoGG.INSTANCE.getAutoGGConfig().isSecondaryEnabled()) {
                String secondGGMessage = AutoGG.INSTANCE.getSecondaryGGStrings()[AutoGG.INSTANCE.getAutoGGConfig().getAutoGGPhrase()];
                int secondaryDelay = AutoGG.INSTANCE.getAutoGGConfig().getSecondaryDelay();
                if (secondaryDelay > 0) {
                    try {
                        Thread.sleep(secondaryDelay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Minecraft.getMinecraft().thePlayer.sendChatMessage(prefix.isEmpty() ? ggMessage : prefix + " " + secondGGMessage);
            }
        }
    }
}
