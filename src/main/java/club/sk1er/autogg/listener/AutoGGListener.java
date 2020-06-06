package club.sk1er.autogg.listener;

import club.sk1er.autogg.AutoGG;
import club.sk1er.autogg.config.AutoGGConfig;
import club.sk1er.mods.core.universal.ChatColor;
import club.sk1er.mods.core.util.MinecraftUtils;
import club.sk1er.mods.core.util.Multithreading;
import club.sk1er.vigilance.data.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.regex.Pattern;

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
        String unformattedText = EnumChatFormatting.getTextWithoutFormattingCodes(event.message.getUnformattedText());
        if (AutoGG.instance.getAutoGGConfig().isAntiGGEnabled() && invoked) {
            for (String primaryString : getPrimaryStrings()) {
                if (unformattedText.contains(primaryString.toLowerCase(Locale.ENGLISH)))
                    event.setCanceled(true);
            }

            for (String primaryString : getSecondaryStrings()) {
                if (unformattedText.contains(primaryString.toLowerCase(Locale.ENGLISH)))
                    event.setCanceled(true);
            }
        }
        if (AutoGG.instance.getAutoGGConfig().isAntiKarmaEnabled() && MinecraftUtils.isHypixel() &&
                Pattern.compile("^\\+(?<karma>\\d)+ Karma!$").matcher(unformattedText).matches()) {
            event.setCanceled(true);
        }

        if (!MinecraftUtils.isHypixel() || !AutoGG.instance.getAutoGGConfig().isAutoGGEnabled() || AutoGG.instance.isRunning() || AutoGG.instance.getTriggers().isEmpty()) {
            return;
        }



        for (String trigger : AutoGG.instance.getTriggers()) {
            if (unformattedText.contains(trigger) && unformattedText.startsWith(" ")) {
                AutoGG.instance.setRunning(true);
                invoked = true;
                Multithreading.schedule(() -> {
                    try {
                        Minecraft.getMinecraft().thePlayer.sendChatMessage(
                            "/achat " + (getPrimaryString())
                        );
                        if (AutoGG.instance.getAutoGGConfig().isSecondaryEnabled()) {
                            Multithreading.schedule(() -> {
                                Minecraft.getMinecraft().thePlayer.sendChatMessage(
                                    "/achat " + (getSecondString())
                                );
                                end();
                            }, AutoGG.instance.getAutoGGConfig().getSecondaryDelay(), TimeUnit.MILLISECONDS);
                            return;
                        }
                        end();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, AutoGG.instance.getAutoGGConfig().getAutoGGDelay(), TimeUnit.MILLISECONDS);
            }
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
        int autoGGPhrase = AutoGG.instance.getAutoGGConfig().getAutoGGPhrase();
        String[] primaryStrings = getPrimaryStrings();
        if (autoGGPhrase > 0 && autoGGPhrase < primaryStrings.length) {
            return primaryStrings[autoGGPhrase];
        }

        return "gg";
    }

    private String[] getPrimaryStrings() {
        try {
            Property autoGGPhrase = AutoGGConfig.class.getDeclaredField("autoGGPhrase").getAnnotation(Property.class);
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
        int autoGGPhrase = AutoGG.instance.getAutoGGConfig().getAutoGGPhrase2();
        String[] primaryStrings = getSecondaryStrings();
        if (autoGGPhrase > 0 && autoGGPhrase < primaryStrings.length) {
            return primaryStrings[autoGGPhrase];
        }

        return "gg";
    }
}
