package club.sk1er.autogg.listener;

import club.sk1er.autogg.AutoGG;
import club.sk1er.autogg.config.AutoGGConfig;
import club.sk1er.mods.core.util.MinecraftUtils;
import club.sk1er.mods.core.util.Multithreading;
import club.sk1er.vigilance.data.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AutoGGListener {

    private boolean invoked;

    @SubscribeEvent
    public void worldSwap(WorldEvent.Unload event) {
        invoked = false;
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String s = event.message.getUnformattedText().toLowerCase(Locale.ENGLISH);
        if (AutoGGConfig.antiGGEnabled && invoked) {
            for (String primaryString : getPrimaryStrings()) {
                if (s.contains(primaryString.toLowerCase(Locale.ENGLISH)))
                    event.setCanceled(true);
            }
            for (String primaryString : getSecondaryStrings()) {
                if (s.contains(primaryString.toLowerCase(Locale.ENGLISH)))
                    event.setCanceled(true);
            }
        }

        String unformattedText = EnumChatFormatting.getTextWithoutFormattingCodes(event.message.getUnformattedText());

        if (!MinecraftUtils.isHypixel() || !AutoGGConfig.autoGGEnabled || AutoGG.instance.isRunning() || AutoGG.instance.getTriggers().isEmpty()) {
            return;
        }

        if (AutoGG.instance.getTriggers().stream().anyMatch(unformattedText::contains) && unformattedText.startsWith(" ")) {
            AutoGG.instance.setRunning(true);
            invoked = true;
            Multithreading.schedule(() -> {
                try {
                    Minecraft.getMinecraft().thePlayer.sendChatMessage(
                        "/achat " + (getPrimaryString())
                    );
                    if (AutoGGConfig.secondaryEnabled) {
                        Multithreading.schedule(() -> {
                            Minecraft.getMinecraft().thePlayer.sendChatMessage(
                                "/achat " + (getSecondString())
                            );
                            end();
                        }, AutoGGConfig.secondaryDelay, TimeUnit.MILLISECONDS);
                        return;
                    }
                    end();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, AutoGGConfig.autoGGDelay, TimeUnit.MILLISECONDS);
        }
    }

    private void end() {
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        AutoGG.instance.setRunning(false);
    }

    private String getPrimaryString() {
        int autoGGPhrase = AutoGGConfig.autoGGPhrase;
        String[] primaryStrings = getPrimaryStrings();
        if (autoGGPhrase > 0 && autoGGPhrase < primaryStrings.length) {
            return primaryStrings[autoGGPhrase];
        }
        return "gg";
    }

    private String[] getPrimaryStrings() {
        try {
            Property autoGGPhrase = AutoGGConfig.class.getDeclaredField("autoZGGPhrase").getAnnotation(Property.class);
            return autoGGPhrase.options();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return new String[0];
    }

    private String[] getSecondaryStrings() {
        try {
            Property autoGGPhrase = AutoGGConfig.class.getDeclaredField("autoGGPhrase2").getAnnotation(Property.class);
            return autoGGPhrase.options();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return new String[0];
    }

    private String getSecondString() {
        int autoGGPhrase = AutoGGConfig.autoGGPhrase2;
        String[] primaryStrings = getSecondaryStrings();
        if (autoGGPhrase > 0 && autoGGPhrase < primaryStrings.length)
            return primaryStrings[autoGGPhrase];
        return "gg";
    }
}
