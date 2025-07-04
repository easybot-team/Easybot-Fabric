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
        context.getSource().sendFeedback(() -> Text.literal("§eREasyBot正在重载配置"), true);
        try {
            mod.reload();
            context.getSource().sendFeedback(() -> Text.literal("§aEasyBot配置重载成功"), true);
        } catch (Exception e) {
            EasyBotFabric.LOGGER.error("EasyBot重载失败", e);
            context.getSource().sendError(Text.literal("§c重新加载 EasyBot 失败。检查服务器控制台是否有错误。"));
        }
        return 1; // 代表成功
    }
}