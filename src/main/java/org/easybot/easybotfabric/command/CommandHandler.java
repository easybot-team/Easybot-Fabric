package org.easybot.easybotfabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.easybot.easybotfabric.bridge.model.PlayerInfo;
import org.easybot.easybotfabric.bridge.model.PlayerInfoWithRaw;
import org.easybot.easybotfabric.EasyBotFabric;
import org.easybot.easybotfabric.bridge.BridgeClient;
import org.easybot.easybotfabric.config.EasyBotConfig;
import org.easybot.easybotfabric.util.BotFilterUtil;

import java.util.concurrent.CompletableFuture;

public class CommandHandler {
    private final EasyBotFabric mod;

    public CommandHandler(EasyBotFabric mod) {
        this.mod = mod;
    }

    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        // 主命令 /ez
        dispatcher.register(CommandManager.literal("ez")
                .then(CommandManager.literal("help")
                        .executes(this::showHelp))
                .then(CommandManager.literal("reload")
                        .requires(source -> source.hasPermissionLevel(3))
                        .executes(this::reload))
                .then(CommandManager.literal("bind")
                        .executes(this::bind))
                .then(CommandManager.literal("say")
                        .then(CommandManager.argument("message", StringArgumentType.greedyString())
                                .executes(this::say)))
                .then(CommandManager.literal("ssay")
                        .then(CommandManager.argument("message", StringArgumentType.greedyString())
                                .executes(this::crossServerSay)))
                .then(CommandManager.literal("bot")
                        .requires(source -> source.hasPermissionLevel(3))
                        .then(CommandManager.literal("toggle")
                                .executes(this::toggleBotFilter))
                        .then(CommandManager.literal("add")
                                .then(CommandManager.argument("prefix", StringArgumentType.string())
                                        .executes(this::addBotPrefix)))
                        .then(CommandManager.literal("remove")
                                .then(CommandManager.argument("prefix", StringArgumentType.string())
                                        .executes(this::removeBotPrefix)))
                        .then(CommandManager.literal("list")
                                .executes(this::listBotPrefixes))
                        .then(CommandManager.literal("carpet")
                                .then(CommandManager.literal("toggle")
                                        .executes(this::toggleCarpetBotFilter))
                                .then(CommandManager.literal("add")
                                        .then(CommandManager.argument("prefix", StringArgumentType.string())
                                                .executes(this::addCarpetBotPrefix)))
                                .then(CommandManager.literal("remove")
                                        .then(CommandManager.argument("prefix", StringArgumentType.string())
                                                .executes(this::removeCarpetBotPrefix)))
                                .then(CommandManager.literal("list")
                                        .executes(this::listCarpetBotPrefixes))))
                .executes(this::showHelp));

        // 简化命令
        dispatcher.register(CommandManager.literal("bind")
                .executes(this::bind));

        dispatcher.register(CommandManager.literal("say")
                .then(CommandManager.argument("message", StringArgumentType.greedyString())
                        .executes(this::say)));

        dispatcher.register(CommandManager.literal("esay")
                .then(CommandManager.argument("message", StringArgumentType.greedyString())
                        .executes(this::say)));

        dispatcher.register(CommandManager.literal("ssay")
                .then(CommandManager.argument("message", StringArgumentType.greedyString())
                        .executes(this::crossServerSay)));

        dispatcher.register(CommandManager.literal("essay")
                .then(CommandManager.argument("message", StringArgumentType.greedyString())
                        .executes(this::crossServerSay)));
    }

    private int showHelp(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        String helpMsg = """
                §6--------§a EasyBot Fabric V1.0.0§r--------
                §b/ez help §f- §c显示帮助菜单
                §b/ez reload §f- §c重载配置文件 (需要OP权限)
                
                §c绑定类
                §b/ez bind §f- §c触发绑定
                §b/bind §f- §c同上
                
                §c消息发送类
                §b/ez say <message> §f- §c发送消息
                §b/esay <message> §f- §c同上
                §b/say <message> §f- §c同上
                
                §c跨服聊天
                §b/ez ssay <message> §f- §c发送跨服消息
                §b/essay <message> §f- §c同上
                §b/ssay <message> §f- §c同上
                
                §c假人过滤设置(需要OP权限)
                §b/ez bot toggle §f- §c开启/关闭假人过滤
                §b/ez bot add <prefix> §f- §c添加假人过滤前缀
                §b/ez bot remove <prefix> §f- §c移除假人过滤前缀
                §b/ez bot list §f- §c显示假人过滤前缀列表
                
                §cCarpet假人过滤设置(需要OP权限)
                §b/ez bot carpet toggle §f- §c开启/关闭Carpet假人过滤
                §b/ez bot carpet add <prefix> §f- §c添加Carpet假人过滤前缀
                §b/ez bot carpet remove <prefix> §f- §c移除Carpet假人过滤前缀
                §b/ez bot carpet list §f- §c显示Carpet假人过滤前缀列表
                §6---------------------------------------------
                """;

        for (String line : helpMsg.split("\n")) {
            if (!line.trim().isEmpty()) {
                source.sendFeedback(() -> Text.literal(line), false);
            }
        }
        return 1;
    }

    private int reload(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        try {
            mod.reload();
            source.sendFeedback(() -> Text.literal("§a插件重载成功!"), false);
        } catch (Exception e) {
            source.sendFeedback(() -> Text.literal("§c插件重载失败: " + e.getMessage()), false);
            EasyBotFabric.LOGGER.error("重载配置时出错", e);
        }
        return 1;
    }

    private int bind(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendFeedback(() -> Text.literal("§c这个命令不能在控制台使用!"), false);
            return 0;
        }

        ServerPlayerEntity player = source.getPlayer();
        String playerName = player.getName().getString();
        BridgeClient client = EasyBotFabric.getBridgeClient();
        
        if (client == null || !client.isOpen()) {
            source.sendFeedback(() -> Text.literal("§c与EasyBot服务器的连接未建立!"), false);
            return 0;
        }

        // 异步处理绑定逻辑
        CompletableFuture.runAsync(() -> {
            try {
                // 先检查玩家是否已经绑定
                client.getSocialAccount(playerName).thenAccept(bindData -> {
                    String uuid = (String) bindData.get("uuid");
                    
                    if (uuid != null && !uuid.isEmpty()) {
                        // 已绑定
                        String name = (String) bindData.get("name");
                        String platform = (String) bindData.get("platform");
                        String time = (String) bindData.get("time");
                        
                        player.sendMessage(Text.literal(
                            String.format("§c你已经绑定了账号, (%s/%s/时间:%s/%s)",
                                name, uuid, time, platform)
                        ), false);
                    } else {
                        // 未绑定，开始绑定流程
                        client.startBind(playerName).thenAccept(codeData -> {
                            String code = (String) codeData.get("code");
                            String time = (String) codeData.get("time");
                            
                            String message = EasyBotFabric.getConfig().message.bindStart
                                    .replace("#code", code)
                                    .replace("#time", time);
                            
                            player.sendMessage(Text.literal(message), false);
                            EasyBotFabric.LOGGER.info("玩家 {} 开始绑定，绑定码: {}", playerName, code);
                        }).exceptionally(e -> {
                            player.sendMessage(Text.literal("§c获取绑定码失败: " + e.getMessage()), false);
                            EasyBotFabric.LOGGER.error("获取绑定码时出错", e);
                            return null;
                        });
                    }
                }).exceptionally(e -> {
                    player.sendMessage(Text.literal("§c检查绑定状态失败: " + e.getMessage()), false);
                    EasyBotFabric.LOGGER.error("检查绑定状态时出错", e);
                    return null;
                });
            } catch (Exception e) {
                player.sendMessage(Text.literal("§c绑定请求失败: " + e.getMessage()), false);
                EasyBotFabric.LOGGER.error("处理绑定请求时出错", e);
            }
        });

        return 1;
    }

    private int say(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String message = StringArgumentType.getString(context, "message");
        
        String playerName = "CONSOLE";
        ServerPlayerEntity player = null;
        if (source.isExecutedByPlayer()) {
            player = source.getPlayer();
            playerName = player.getName().getString();
        }

        BridgeClient client = EasyBotFabric.getBridgeClient();
        if (client == null || !client.isOpen()) {
            source.sendFeedback(() -> Text.literal("§c与EasyBot服务器的连接未建立!"), false);
            return 0;
        }

        // 异步发送消息
        final String finalPlayerName = playerName;
        final ServerPlayerEntity finalPlayer = player;
        CompletableFuture.runAsync(() -> {
            try {
                if (finalPlayer != null) {
                    // 创建玩家信息
                    PlayerInfo playerInfo = new PlayerInfo(
                        finalPlayerName,
                        finalPlayer.getUuid().toString(),
                        finalPlayer.getDisplayName().getString(),
                        0, // 延迟
                        finalPlayer.getWorld().getRegistryKey().getValue().toString(),
                        finalPlayer.getX(), finalPlayer.getY(), finalPlayer.getZ()
                    );
                    
                    PlayerInfoWithRaw infoWithRaw =
                        new PlayerInfoWithRaw(playerInfo, finalPlayerName);
                    
                    // 发送消息
                    client.syncMessage(infoWithRaw, message, true);
                } else {
                    // 控制台发送消息的逻辑
                    // 这里可能需要特殊处理，暂时记录日志
                    EasyBotFabric.LOGGER.info("控制台发送消息: {}", message);
                }
                
                source.sendFeedback(() -> Text.literal("§a消息已发送: §f" + message), false);
            } catch (Exception e) {
                source.sendFeedback(() -> Text.literal("§c消息发送失败: " + e.getMessage()), false);
                EasyBotFabric.LOGGER.error("发送消息时出错", e);
            }
        });

        return 1;
    }

    private int crossServerSay(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String message = StringArgumentType.getString(context, "message");
        
        if (!source.isExecutedByPlayer()) {
            source.sendFeedback(() -> Text.literal("§c这个命令只能由玩家使用!"), false);
            return 0;
        }

        ServerPlayerEntity player = source.getPlayer();
        String playerName = player.getName().getString();
        BridgeClient client = EasyBotFabric.getBridgeClient();
        
        if (client == null || !client.isOpen()) {
            source.sendFeedback(() -> Text.literal("§c与EasyBot服务器的连接未建立!"), false);
            return 0;
        }

        // 异步发送跨服消息
        CompletableFuture.runAsync(() -> {
            try {
                // 创建玩家信息
                PlayerInfo playerInfo = new PlayerInfo(
                    playerName,
                    player.getUuid().toString(),
                    player.getDisplayName().getString(),
                    0, // 延迟
                    player.getWorld().getRegistryKey().getValue().toString(),
                    player.getX(), player.getY(), player.getZ()
                );
                
                PlayerInfoWithRaw infoWithRaw =
                    new PlayerInfoWithRaw(playerInfo, playerName);
                
                // 发送跨服消息
                client.syncCrossServerMessage(infoWithRaw, message);
                
                EasyBotFabric.LOGGER.info("玩家 {} 发送跨服消息: {}", playerName, message);
                player.sendMessage(Text.literal("§a你的消息已发送到其他服务器."), false);
            } catch (Exception e) {
                player.sendMessage(Text.literal("§c跨服消息发送失败: " + e.getMessage()), false);
                EasyBotFabric.LOGGER.error("发送跨服消息时出错", e);
            }
        });

        return 1;
    }

    private int toggleBotFilter(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        EasyBotConfig config = EasyBotFabric.getConfig();
        
        config.botFilter.enabled = !config.botFilter.enabled;
        config.save();
        
        String state = config.botFilter.enabled ? "启用" : "禁用";
        source.sendFeedback(() -> Text.literal("§a假人过滤已" + state), false);
        
        return 1;
    }

    private int addBotPrefix(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String prefix = StringArgumentType.getString(context, "prefix");
        EasyBotConfig config = EasyBotFabric.getConfig();
        
        if (!config.botFilter.prefixes.contains(prefix)) {
            config.botFilter.prefixes.add(prefix);
            config.save();
            source.sendFeedback(() -> Text.literal("§a已添加假人前缀: " + prefix), false);
        } else {
            source.sendFeedback(() -> Text.literal("§c前缀 " + prefix + " 已存在!"), false);
        }
        
        return 1;
    }

    private int removeBotPrefix(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String prefix = StringArgumentType.getString(context, "prefix");
        EasyBotConfig config = EasyBotFabric.getConfig();
        
        if (config.botFilter.prefixes.remove(prefix)) {
            config.save();
            source.sendFeedback(() -> Text.literal("§a已移除假人前缀: " + prefix), false);
        } else {
            source.sendFeedback(() -> Text.literal("§c前缀 " + prefix + " 不存在!"), false);
        }
        
        return 1;
    }

    private int listBotPrefixes(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        EasyBotConfig config = EasyBotFabric.getConfig();
        
        String state = config.botFilter.enabled ? "启用" : "禁用";
        source.sendFeedback(() -> Text.literal("§a假人过滤状态: " + state), false);
        
        if (config.botFilter.prefixes.isEmpty()) {
            source.sendFeedback(() -> Text.literal("§c无前缀"), false);
        } else {
            String prefixes = String.join(", ", config.botFilter.prefixes);
            source.sendFeedback(() -> Text.literal("§a假人前缀列表: " + prefixes), false);
        }
        
        // 显示Carpet假人过滤状态
        if (BotFilterUtil.isCarpetModLoaded()) {
            String carpetState = config.botFilter.filterCarpetBots ? "启用" : "禁用";
            source.sendFeedback(() -> Text.literal("§aCarpet假人过滤状态: " + carpetState), false);
        }
        
        return 1;
    }
    
    private int toggleCarpetBotFilter(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        EasyBotConfig config = EasyBotFabric.getConfig();
        
        if (!BotFilterUtil.isCarpetModLoaded()) {
            source.sendFeedback(() -> Text.literal("§c未检测到Carpet模组，无法切换Carpet假人过滤状态"), false);
            return 0;
        }
        
        config.botFilter.filterCarpetBots = !config.botFilter.filterCarpetBots;
        config.save();
        
        String state = config.botFilter.filterCarpetBots ? "启用" : "禁用";
        source.sendFeedback(() -> Text.literal("§aCarpet假人过滤已" + state), false);
        
        return 1;
    }
    
    private int addCarpetBotPrefix(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String prefix = StringArgumentType.getString(context, "prefix");
        EasyBotConfig config = EasyBotFabric.getConfig();
        
        if (!BotFilterUtil.isCarpetModLoaded()) {
            source.sendFeedback(() -> Text.literal("§c未检测到Carpet模组，无法添加Carpet假人前缀"), false);
            return 0;
        }
        
        if (!config.botFilter.carpetPrefixes.contains(prefix)) {
            config.botFilter.carpetPrefixes.add(prefix);
            config.save();
            source.sendFeedback(() -> Text.literal("§a已添加Carpet假人前缀: " + prefix), false);
        } else {
            source.sendFeedback(() -> Text.literal("§cCarpet假人前缀 " + prefix + " 已存在!"), false);
        }
        
        return 1;
    }
    
    private int removeCarpetBotPrefix(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String prefix = StringArgumentType.getString(context, "prefix");
        EasyBotConfig config = EasyBotFabric.getConfig();
        
        if (!BotFilterUtil.isCarpetModLoaded()) {
            source.sendFeedback(() -> Text.literal("§c未检测到Carpet模组，无法移除Carpet假人前缀"), false);
            return 0;
        }
        
        if (config.botFilter.carpetPrefixes.remove(prefix)) {
            config.save();
            source.sendFeedback(() -> Text.literal("§a已移除Carpet假人前缀: " + prefix), false);
        } else {
            source.sendFeedback(() -> Text.literal("§cCarpet假人前缀 " + prefix + " 不存在!"), false);
        }
        
        return 1;
    }
    
    private int listCarpetBotPrefixes(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        EasyBotConfig config = EasyBotFabric.getConfig();
        
        if (!BotFilterUtil.isCarpetModLoaded()) {
            source.sendFeedback(() -> Text.literal("§c未检测到Carpet模组，无法列出Carpet假人前缀"), false);
            return 0;
        }
        
        String state = config.botFilter.filterCarpetBots ? "启用" : "禁用";
        source.sendFeedback(() -> Text.literal("§aCarpet假人过滤状态: " + state), false);
        
        if (config.botFilter.carpetPrefixes.isEmpty()) {
            source.sendFeedback(() -> Text.literal("§c无Carpet假人前缀"), false);
        } else {
            String prefixes = String.join(", ", config.botFilter.carpetPrefixes);
            source.sendFeedback(() -> Text.literal("§aCarpet假人前缀列表: " + prefixes), false);
        }
        
        return 1;
    }
}