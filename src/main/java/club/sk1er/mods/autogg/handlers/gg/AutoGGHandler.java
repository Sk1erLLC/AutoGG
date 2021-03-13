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
    private Server server;

    @SubscribeEvent
    public void onWorldLoad(EntityJoinWorldEvent event) {
        if (event.entity instanceof EntityPlayerSP) {
            if (AutoGG.INSTANCE.getAutoGGConfig().isModEnabled()) {
                Multithreading.schedule(() -> {
                    for (Server server : AutoGG.INSTANCE.getTriggers().getServers()) {
                        if (server.getHandler().getDetector().detect(server.getDetector(), (EntityPlayerSP) event.entity)) {
                            this.server = server;
                            break;
                        }
                    }
                }, 3, TimeUnit.SECONDS);
            }
        }
    }

    @SubscribeEvent
    public void onClientChatReceived(ClientChatReceivedEvent event) {
        if (AutoGG.INSTANCE.getAutoGGConfig().isModEnabled()) {
            Multithreading.runAsync(() -> {
                if (server != null) {
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
                    boolean isCasualSent = false;
                    // Give casual triggers priority if enabled
                    // Casual GG feature
                    if (AutoGG.INSTANCE.getAutoGGConfig().isCasualAutoGGEnabled()) {
                        for (String trigger : server.getCasualTriggers()) {
                            if (PatternHandler.INSTANCE.getPattern(trigger).matcher(chatMessage).matches()) {
                                isCasualSent = true;
                                invokeGG();
                                break;
                            }
                        }
                    }

                    if (!isCasualSent) {
                        // Normal AutoGG feature
                        for (String trigger : server.getTriggers()) {
                            if (PatternHandler.INSTANCE.getPattern(trigger).matcher(chatMessage).matches()) {
                                invokeGG();
                                break;
                            }
                        }
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
            try {
                Thread.sleep(AutoGG.INSTANCE.getAutoGGConfig().getAutoGGDelay());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Minecraft.getMinecraft().thePlayer.sendChatMessage(prefix + " " + ggMessage);

            if (AutoGG.INSTANCE.getAutoGGConfig().isSecondaryEnabled()) {
                String secondGGMessage = AutoGG.INSTANCE.getSecondaryGGStrings()[AutoGG.INSTANCE.getAutoGGConfig().getAutoGGPhrase()];
                try {
                    Thread.sleep(AutoGG.INSTANCE.getAutoGGConfig().getSecondaryDelay());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Minecraft.getMinecraft().thePlayer.sendChatMessage(prefix + " " + secondGGMessage);
            }
        }
    }
}
