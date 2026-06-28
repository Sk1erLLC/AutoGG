package club.sk1er.mods.autogg.command;

import club.sk1er.mods.autogg.AutoGG;
import club.sk1er.mods.autogg.tasks.RetrieveTriggersTask;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class AutoGGCommand {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(
                literal("autogg")
                    .executes(ctx -> {
                        sendMessage(ctx.getSource(),
                            ChatFormatting.GOLD + "[AutoGG] " + ChatFormatting.GRAY +
                            "Edit §econfig/autogg.json §7to change settings. " +
                            "Use §e/autogg refresh §7to reload triggers.");
                        return 1;
                    })
                    .then(literal("refresh")
                        .executes(ctx -> {
                            AutoGG.POOL.submit(new RetrieveTriggersTask());
                            sendMessage(ctx.getSource(),
                                ChatFormatting.GOLD + "[AutoGG] " + ChatFormatting.GREEN + "Refreshed triggers!");
                            return 1;
                        }))
            )
        );
    }

    private static void sendMessage(FabricClientCommandSource source, String text) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.sendSystemMessage(Component.literal(text));
        }
    }
}
