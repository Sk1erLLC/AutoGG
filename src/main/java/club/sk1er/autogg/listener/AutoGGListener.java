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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class AutoGGListener {

	private static final Pattern karmaPattern = Pattern.compile("^\\+(?<karma>\\d)+ Karma!$");
	private static final Pattern antiGGPrimaryPattern = Pattern.compile("^(?:.* )?(?:\\[.+] )?\\w{1,16}(?: .+)?: (?:" +
			String.join("|", getPrimaryStrings(true)) + ")$");
	private static final Pattern antiGGSecondaryPattern = Pattern.compile("^(?:.* )?(?:\\[.+] )?\\w{1,16}(?: .+)?: (?:" +
			String.join("|", getSecondaryStrings(true)) + ")$");
	// special thanks to Pluggs and Unextracted for helping me out where other people ignored me

	private boolean invoked;

	private boolean holeInTheBlock = false;
	private boolean useDelay;

	@SubscribeEvent
	public void worldSwap(WorldEvent.Unload event) {
		invoked = false;
	}

	// forge fires this event three times but luckily that doesn't affect what we're doing
	@SubscribeEvent
	public void holeInTheBlockThing(WorldEvent.Load event) {
		Multithreading.schedule(() -> {
			System.out.println(antiGGPrimaryPattern);
			System.out.println(antiGGSecondaryPattern);
			String scoreboardTitle;
			try {
				scoreboardTitle = EnumChatFormatting.getTextWithoutFormattingCodes(event.world.getScoreboard()
						.getObjectiveInDisplaySlot(1).getDisplayName());
			} catch (Exception e) {
				end();
				return;
			}

			holeInTheBlock = "HOLE IN THE WALL".equals(scoreboardTitle);
			useDelay = "PIXEL PAINTERS".equals(scoreboardTitle) || "SPEED UHC".equals(scoreboardTitle);

			end();
		}, 300, TimeUnit.MILLISECONDS);
	}

	@SubscribeEvent
	public void onChat(ClientChatReceivedEvent event) {
		String unformattedText = EnumChatFormatting.getTextWithoutFormattingCodes(event.message.getUnformattedText());
		if ((AutoGG.instance.getAutoGGConfig().isAntiGGEnabled() && invoked) &&
				(antiGGPrimaryPattern.matcher(unformattedText.toLowerCase()).matches() ||
				antiGGSecondaryPattern.matcher(unformattedText.toLowerCase()).matches())) {
			event.setCanceled(true);
			return;
		}

		if (AutoGG.instance.getAutoGGConfig().isAntiKarmaEnabled() && karmaPattern.matcher(unformattedText).matches()) {
			event.setCanceled(true);
			return;
		}

		if (!AutoGG.instance.isRunning()) {
			if (AutoGG.instance.getAutoGGConfig().isCasualAutoGGEnabled()) {
				for (Pattern trigger : AutoGG.instance.getCasualTriggers()) {
					if (trigger.matcher(unformattedText).matches()) {
						AutoGG.instance.setRunning(true);
						invoked = true;
						sayGG(false, 0);
						AutoGG.instance.setRunning(false);
						Multithreading.schedule(() -> {
							invoked = false;
							end();
						}, 60, TimeUnit.SECONDS);
						return;
					}
				}
			}

			if (AutoGG.instance.getAutoGGConfig().isAutoGGEnabled()) {
				for (Pattern trigger : AutoGG.instance.getTriggers()) {
					if (trigger.matcher(unformattedText).matches()) {
						if (holeInTheBlock) {
							holeInTheBlock = false;
							return;
						}
						AutoGG.instance.setRunning(true);
						invoked = true;
						sayGG(true, useDelay ? 240 : 0);
					}
				}
			}
		}
	}

	private void sayGG(boolean doSecond, int addedTime) {
		Multithreading.schedule(() -> {
			try {
				Minecraft.getMinecraft().thePlayer.sendChatMessage(
						"/achat " + (getPrimaryString())
				);
				if (AutoGG.instance.getAutoGGConfig().isSecondaryEnabled() && doSecond) {
					Multithreading.schedule(() -> {
						try {
							Minecraft.getMinecraft().thePlayer.sendChatMessage(
									"/achat " + (getSecondString())
							);
						} catch (RuntimeException e) {
							MinecraftUtils.sendMessage(AutoGG.instance.getPrefix(),ChatColor.RED +
									"An error occurred getting second string.");
							AutoGG.instance.getLogger().error("Failed to get second string.", e);
						} finally {
							end();
						}
					}, AutoGG.instance.getAutoGGConfig().getSecondaryDelay() + 10, TimeUnit.MILLISECONDS);
				}
			} catch (Exception e) {
				AutoGG.instance.getLogger().error("Failed to send AutoGG messages.", e);
			} finally {
				end();
			}
		}, AutoGG.instance.getAutoGGConfig().getAutoGGDelay() + addedTime, TimeUnit.MILLISECONDS);
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
		String[] primaryStrings = getPrimaryStrings(false);
		if (autoGGPhrase > 0 && autoGGPhrase < primaryStrings.length) {
			return primaryStrings[autoGGPhrase];
		}

		return "gg";
	}

	private static String[] getPrimaryStrings(boolean lower) { // can be static because the Strings never change, only selected one
		try {
			Property autoGGPhrase = AutoGGConfig.class.getDeclaredField("autoGGPhrase").getAnnotation(Property.class);
			List<String> options = new ArrayList<>();
			for (String option : autoGGPhrase.options()) {
				options.add(lower ? option.toLowerCase() : option);
			}
			return options.toArray(new String[0]);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}

		return new String[0];
	}

	private static String[] getSecondaryStrings(boolean lower) { // see above
		try {
			Property autoGGPhrase = AutoGGConfig.class.getDeclaredField("autoGGPhrase2").getAnnotation(Property.class);
			List<String> options = new ArrayList<>();
			for (String option : autoGGPhrase.options()) {
				options.add(lower ? option.toLowerCase() : option);
			}
			return options.toArray(new String[0]);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}

		return new String[0];
	}

	private String getSecondString() throws RuntimeException {
		int autoGGPhrase = AutoGG.instance.getAutoGGConfig().getAutoGGPhrase2();
		String[] primaryStrings = getSecondaryStrings(false);
		if (autoGGPhrase >= 0 && autoGGPhrase < primaryStrings.length) {
			return primaryStrings[autoGGPhrase];
		}

		throw new RuntimeException("An unknown error occurred parsing config. Try deleting " +
				".minecraft/config/autogg.toml or contacting the mod authors.");
	}
}
