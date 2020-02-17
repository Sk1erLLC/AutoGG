package club.sk1er.autogg.listener;

import club.sk1er.autogg.AutoGG;
import club.sk1er.autogg.config.AutoGGConfig;
import club.sk1er.mods.core.util.MinecraftUtils;
import club.sk1er.mods.core.util.Multithreading;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Locale;

public class AutoGGListener {

    private boolean invoked;

    @SubscribeEvent
    public void worldSwap(WorldEvent.Unload event) {
        invoked = false;
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (AutoGGConfig.antiGGEnabled && invoked
                && (event.message.getUnformattedText().toLowerCase(Locale.ENGLISH).endsWith("gg")
                || event.message.getUnformattedText().toLowerCase(Locale.ENGLISH).endsWith("good game"))) {
            event.setCanceled(true);
        }

        String unformattedText = EnumChatFormatting.getTextWithoutFormattingCodes(event.message.getUnformattedText());

        if (!MinecraftUtils.isHypixel() || !AutoGGConfig.autoGGEnabled || AutoGG.instance.isRunning() || AutoGG.instance.getTriggers().isEmpty()) {
            return;
        }

        if (AutoGG.instance.getTriggers().stream().anyMatch(unformattedText::contains) && unformattedText.startsWith(" ")) {
            AutoGG.instance.setRunning(true);
            invoked = true;
            Multithreading.runAsync(() -> {
                try {
                    Thread.sleep(AutoGGConfig.autoGGDelay * 1000);
                    Minecraft.getMinecraft().thePlayer.sendChatMessage(
                            "/achat " + (AutoGGConfig.goodGameEnabled
                                    ? (AutoGGConfig.lowercaseEnabled ? "good game" : "Good Game")
                                    : (AutoGGConfig.lowercaseEnabled ? "gg" : "GG"))
                    );

                    Thread.sleep(2000L);
                    AutoGG.instance.setRunning(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        System.out.println("should've done by now");
    }
}
