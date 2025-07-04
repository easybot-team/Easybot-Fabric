package org.lby123165.easybot.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.lby123165.easybot.EasyBotFabric;

public class CommandHandler {

    private final EasyBotFabric mod;

    public CommandHandler(EasyBotFabric mod) {
        this.mod = mod;
    }

    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("easybot")
                .requires(source -> source.hasPermissionLevel(2)) // 需要 OP 权限等级 2
                .then(CommandManager.literal("reload")
                        .executes(this::executeReload)
                )
        );
    }

    private int executeReload(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(() -> Text.literal("§eReloading EasyBot configuration..."), true);
        try {
            mod.reload();
            context.getSource().sendFeedback(() -> Text.literal("§aEasyBot reloaded successfully!"), true);
        } catch (Exception e) {
            EasyBotFabric.LOGGER.error("Failed to reload EasyBot", e);
            context.getSource().sendError(Text.literal("§cFailed to reload EasyBot. Check the server console for errors."));
        }
        return 1; // 代表成功
    }
}