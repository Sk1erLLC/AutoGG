package club.sk1er.mods.autogg.command;

import club.sk1er.mods.autogg.config.AutoGGConfigScreen;
import club.sk1er.mods.autogg.handlers.gg.AutoGGHandler;
import club.sk1er.mods.autogg.tasks.RetrieveTriggersTask;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import static club.sk1er.mods.autogg.AutoGG.POOL;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class AutoGGCommand {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                literal("autogg")
                        .executes(context -> {
                            AutoGGHandler.displayScreen = new AutoGGConfigScreen(null);
                            return 1;
                        })
                        .then(literal("refresh").executes(context -> {
                            POOL.submit(new RetrieveTriggersTask());
                            context.getSource().sendFeedback(Component.literal("Refreshed triggers!").withStyle(ChatFormatting.GREEN));
                            return 1;
                        }))
        ));
    }
}
