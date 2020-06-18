package club.sk1er.autogg.listener;

import club.sk1er.autogg.AutoGG;
import club.sk1er.autogg.config.AutoGGConfig;
import club.sk1er.mods.core.util.Multithreading;
import club.sk1er.vigilance.data.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class AutoGGListener {

    private static final Pattern karmaPattern = Pattern.compile("^\\+(?<karma>\\d)+ Karma!$");

    private boolean invoked;

    private boolean holeInTheBlock = false;


    @SubscribeEvent
    public void worldSwap(WorldEvent.Unload event) { invoked = false; }

    @SubscribeEvent
    public void holeInTheBlockThing(WorldEvent.Load event) {
        Multithreading.schedule(() -> {
            Scoreboard scoreboard;
            try { scoreboard = event.world.getScoreboard(); }
            catch (Exception e) { holeInTheBlock = false; end(); return; }
            if (scoreboard != null) {
                holeInTheBlock = EnumChatFormatting.getTextWithoutFormattingCodes(
                        scoreboard.getObjectiveInDisplaySlot(1).getDisplayName()
                ).equals("HOLE IN THE WALL");
            } else holeInTheBlock = false;
            end();
        }, 300, TimeUnit.MILLISECONDS);
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String unformattedText = EnumChatFormatting.getTextWithoutFormattingCodes(event.message.getUnformattedText());
        if (AutoGG.instance.getAutoGGConfig().isAntiGGEnabled() && invoked) {
            for (String primaryString : getPrimaryStrings()) {
                if (unformattedText.toLowerCase(Locale.ENGLISH).contains(primaryString.toLowerCase(Locale.ENGLISH))) {
                    event.setCanceled(true);
                    return; // don't waste time checking more stuff
                }
            }

            for (String primaryString : getSecondaryStrings()) {
                if (unformattedText.toLowerCase(Locale.ENGLISH).contains(primaryString.toLowerCase(Locale.ENGLISH))) {
                    event.setCanceled(true);
                    return;
                }
            }
        }
        if (AutoGG.instance.getAutoGGConfig().isAntiKarmaEnabled() && karmaPattern.matcher(unformattedText).matches()) {
            event.setCanceled(true);
            return;
        }

        if (AutoGG.instance.getAutoGGConfig().isCasualAutoGGEnabled()) {
            for (Pattern trigger : AutoGG.instance.getCasualTriggers()) {
                if (trigger.matcher(unformattedText).matches()) {
                    invoked = true; // invoked for antigg, but not setRunning because we want to be able to say gg again
                    Multithreading.schedule(() -> {
                        try {
                            Minecraft.getMinecraft().thePlayer.sendChatMessage("/achat " + getPrimaryString());
                            end(); // we're not gonna print secondary strings for "casual" things
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        end();
                    }, AutoGG.instance.getAutoGGConfig().getAutoGGDelay(), TimeUnit.MILLISECONDS);
                    return;
                }
            }
        }

        if (AutoGG.instance.getAutoGGConfig().isAutoGGEnabled() && !AutoGG.instance.isRunning()) {
            for (Pattern trigger : AutoGG.instance.getTriggers()) {
                if (trigger.matcher(unformattedText).matches()) {
                    if (holeInTheBlock) {
                        holeInTheBlock = false; // so that it doesn't execute the first time, only the second
                        return; //                 i can't decide if this solution is really good or really bad
                    } else {
                        AutoGG.instance.setRunning(true);
                        invoked = true;
                        Multithreading.schedule(() -> {
                            try {
                                Minecraft.getMinecraft().thePlayer.sendChatMessage(
                                        "/achat " + (getPrimaryString())
                                );
                                if (AutoGG.instance.getAutoGGConfig().isSecondaryEnabled()) {
                                    Multithreading.schedule(() -> {
                                        try {
                                            Minecraft.getMinecraft().thePlayer.sendChatMessage(
                                                    "/achat " + (getSecondString())
                                            );
                                        } catch (RuntimeException ignored) {
                                            Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("An error occurred getting second string."));
                                        } // if invalid config
                                        end();
                                    }, AutoGG.instance.getAutoGGConfig().getSecondaryDelay(), TimeUnit.MILLISECONDS);
                                }
                                end();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }, AutoGG.instance.getAutoGGConfig().getAutoGGDelay(), TimeUnit.MILLISECONDS);
                    }
                }
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

    private String getSecondString() throws RuntimeException {
        int autoGGPhrase = AutoGG.instance.getAutoGGConfig().getAutoGGPhrase2();
        String[] primaryStrings = getSecondaryStrings();
        if (autoGGPhrase >= 0 && autoGGPhrase < primaryStrings.length) {
            return primaryStrings[autoGGPhrase];
        }

        throw new RuntimeException("An unknown error occurred parsing config. Try deleting .minecraft/config/autogg.toml or contacting the mod authors.");
    }
}
