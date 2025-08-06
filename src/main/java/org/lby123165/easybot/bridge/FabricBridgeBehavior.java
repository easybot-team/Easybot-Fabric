package org.lby123165.easybot.bridge;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.lby123165.easybot.EasyBotFabric;
import org.lby123165.easybot.bridge.message.*;
import org.lby123165.easybot.bridge.model.FabricServerInfo;
import org.lby123165.easybot.bridge.model.PlayerInfo;
import org.lby123165.easybot.bridge.packet.*;
import org.lby123165.easybot.duck.ILatencyProvider;
import org.lby123165.easybot.util.TextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class FabricBridgeBehavior extends BridgeBehavior {

    private static final Logger LOGGER = LoggerFactory.getLogger(FabricBridgeBehavior.class);
    private final MinecraftServer server;
    private final BridgeClient client;
    private static final Gson GSON = new Gson();

    public FabricBridgeBehavior(MinecraftServer server, BridgeClient client) {
        this.server = server;
        this.client = client;
    }

    @Override
    public void onMessage(String rawJson) {
        try {
            CommandPacket command = GSON.fromJson(rawJson, CommandPacket.class);

            if (command.execOp == null) {
                LOGGER.warn("收到格式不正确的指令包 (缺少 exec_op): {}", rawJson);
                return;
            }

            switch (command.execOp) {
                case "PLAYER_LIST":
                case "GET_SERVER_INFO":
                case "RUN_COMMAND":
                    if (command.callbackId == null) {
                        LOGGER.warn("收到需要回调但缺少 callback_id 的指令: {}", rawJson);
                        return;
                    }
                    handleRequest(command, rawJson);
                    break;

                case "SEND_TO_CHAT":
                case "UN_BIND_NOTIFY":
                case "BIND_SUCCESS_NOTIFY":
                case "SYNC_SETTINGS_UPDATED":
                    handleNotification(command, rawJson);
                    break;

                default:
                    LOGGER.warn("收到未知的服务器指令: {}", command.execOp);
                    if (command.callbackId != null) {
                        sendCallback(command.callbackId, command.execOp, new JsonObject());
                    }
                    break;
            }
        } catch (Exception e) {
            LOGGER.error("处理服务器指令时出错: {}", rawJson, e);
        }
    }

    private void handleRequest(CommandPacket command, String rawJson) {
        // Using server.execute to ensure all logic runs on the main server thread
        server.execute(() -> {
            switch (command.execOp) {
                case "PLAYER_LIST":
                    handleGetPlayerList(command.callbackId, command.execOp);
                    break;
                case "GET_SERVER_INFO":
                    handleGetServerInfo(command.callbackId, command.execOp);
                    break;
                case "RUN_COMMAND":
                    handleRunCommand(command.callbackId, command.execOp, rawJson);
                    break;
            }
        });
    }

    private void handleNotification(CommandPacket command, String rawJson) {
        // Using server.execute to ensure all logic runs on the main server thread
        server.execute(() -> {
            switch (command.execOp) {
                case "SEND_TO_CHAT":
                    handleSendToChat(rawJson);
                    break;
                case "UN_BIND_NOTIFY":
                    handleUnbindNotify(rawJson);
                    break;
                case "BIND_SUCCESS_NOTIFY":
                    handleBindSuccessNotify(rawJson);
                    break;
                case "SYNC_SETTINGS_UPDATED":
                    handleSyncSettingsUpdated(rawJson);
                    break;
            }
        });
    }


    private void handleRunCommand(String callbackId, String execOp, String rawJson) {
        RunCommandPacket packet = GSON.fromJson(rawJson, RunCommandPacket.class);
        String commandToRun = packet.command;

        // 如果启用了PAPI且Text Placeholder API可用，则替换占位符
        if (packet.enablePapi && FabricClientProfile.isPapiSupported()) {
            String playerName = packet.playerName;
            if (playerName != null && !playerName.isEmpty()) {
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerName);
                if (player != null) {
                    commandToRun = org.lby123165.easybot.util.PapiUtil.parsePlaceholders(commandToRun, player);
                    LOGGER.debug("已使用PAPI替换命令中的占位符: {}", commandToRun);
                } else {
                    LOGGER.warn("无法找到玩家 {} 进行PAPI替换", playerName);
                }
            }
        }

        try {
            final List<Text> capturedMessages = new ArrayList<>();
            CommandOutput commandOutput = new CommandOutput() {
                @Override
                public void sendMessage(Text message) {
                    capturedMessages.add(message);
                }
                @Override
                public boolean shouldReceiveFeedback() { return true; }
                @Override
                public boolean shouldTrackOutput() { return true; }
                @Override
                public boolean shouldBroadcastConsoleToOps() { return false; }
            };

            ServerCommandSource source = new ServerCommandSource(
                    commandOutput,
                    Vec3d.of(server.getOverworld().getSpawnPos()),
                    Vec2f.ZERO,
                    server.getOverworld(),
                    4, // Permission level 4 (console)
                    "EasyBot",
                    Text.literal("EasyBot"),
                    server,
                    null
            );

            this.server.getCommandManager().executeWithPrefix(source, commandToRun);

            StringBuilder markdownOutput = new StringBuilder();
            for (Text message : capturedMessages) {
                markdownOutput.append(TextUtil.toMarkdown(message)).append("\n");
            }

            RunCommandResultPacket result = new RunCommandResultPacket(true, markdownOutput.toString().trim());
            sendCallback(callbackId, execOp, result);

        } catch (Exception e) {
            LOGGER.error("执行指令 '{}' 时出错", commandToRun, e);
            RunCommandResultPacket result = new RunCommandResultPacket(false, e.getMessage());
            sendCallback(callbackId, execOp, result);
        }
    }

    private void handleSendToChat(String rawJson) {
        try {
            // FIX: Use proper debug logging
            if (FabricClientProfile.isDebugMode()) {
                LOGGER.debug("接收到的 SEND_TO_CHAT 有效负载: {}", rawJson);
            }

            JsonObject packetJson = GSON.fromJson(rawJson, JsonObject.class);

            JsonElement extraElement = packetJson.get("extra");
            String text = packetJson.has("text") ? packetJson.get("text").getAsString() : "";

            boolean hasRichContent = extraElement != null && !extraElement.isJsonNull() && extraElement.isJsonArray() && !extraElement.getAsJsonArray().isEmpty();

            if (FabricClientProfile.isDebugMode()) {
                LOGGER.debug("已解析信息。文本： '{}', 富文本内容： {}", text, hasRichContent);
            }

            if (!hasRichContent) {
                if (text != null && !text.isEmpty()) {
                    server.getPlayerManager().broadcast(TextUtil.parseLegacyColor(text), false);
                }
                return;
            }

            List<Segment> segments = StreamSupport.stream(extraElement.getAsJsonArray().spliterator(), false)
                    .map(JsonElement::getAsJsonObject)
                    .map(obj -> {
                        SegmentType type = SegmentType.fromValue(obj.get("type").getAsInt());
                        Class<? extends Segment> segmentClass = Segment.getSegmentClass(type);
                        if (segmentClass != null) {
                            Segment seg = GSON.fromJson(obj, segmentClass);
                            if (FabricClientProfile.isDebugMode()) {
                                LOGGER.debug("已解析段落： {}", GSON.toJson(seg));
                            }
                            return seg;
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            SyncToChatExtra(segments, text);

        } catch (Exception e) {
            LOGGER.error("在 handleSendToChat 中处理消息时出现严重错误。原始负载: {}", rawJson, e);
        }
    }

    @Override
    public void SyncToChatExtra(List<Segment> segments, String fallbackText) {
        if (server == null || server.getPlayerManager() == null) {
            return;
        }

        if (segments == null || segments.isEmpty()) {
            if (fallbackText != null && !fallbackText.isEmpty()){
                server.getPlayerManager().broadcast(TextUtil.parseLegacyColor(fallbackText), false);
            }
            return;
        }

        MutableText finalMessage = Text.empty();
        for (Segment segment : segments) {
            if (segment instanceof TextSegment textSegment) {
                finalMessage.append(Text.literal(textSegment.text));

            } else if (segment instanceof AtSegment atSegment) {
                String atDisplayName = "@" + (atSegment.atPlayerNames.isEmpty()
                        ? atSegment.atUserName
                        : String.join(",", atSegment.atPlayerNames));

                MutableText atText = Text.literal(atDisplayName).formatted(Formatting.GOLD);
                String hoverInfo = String.format("社交账号: %s (%s)", atSegment.atUserName, atSegment.atUserId);

                atText.styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(hoverInfo))));
                finalMessage.append(atText);

            } else if (segment instanceof ImageSegment imageSegment) {
                MutableText imageText = Text.literal("[图片]").formatted(Formatting.GREEN);

                // FIX: Use Text.literal() instead of Text.of() to avoid InstantiationError
                imageText.styled(style -> style
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("点击预览")))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, imageSegment.url))
                );
                finalMessage.append(imageText);
            }
        }

        if (FabricClientProfile.isDebugMode()) {
            LOGGER.debug("建立最终的富文本信息。向玩家广播。");
        }
        server.getPlayerManager().broadcast(finalMessage, false);
    }

    // FIX: Removed duplicated lines and the extra closing brace that was here.

    private void handleUnbindNotify(String rawJson) {
        PlayerUnBindNotifyPacket packet = GSON.fromJson(rawJson, PlayerUnBindNotifyPacket.class);
        
        // 处理白名单
        if (EasyBotFabric.getConfig().enableWhiteList) {
            org.lby123165.easybot.util.WhitelistUtil.handleUnbind(packet.playerName, server);
        }
        
        // 踢出玩家
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(packet.playerName);
        if (player != null) {
            player.networkHandler.disconnect(Text.literal(packet.kickMessage));
            LOGGER.info("已将玩家 {} 踢出服务器，原因: 解绑账号", packet.playerName);
        }
    }

    private void handleBindSuccessNotify(String rawJson) {
        BindSuccessNotifyPacket packet = GSON.fromJson(rawJson, BindSuccessNotifyPacket.class);
        String message = String.format("§a[EasyBot] 玩家 %s 成功绑定账号 %s (%s)", packet.playerName, packet.accountName, packet.accountId);
        server.getPlayerManager().broadcast(TextUtil.parseLegacyColor(message), false);
        
        // 处理白名单
        if (EasyBotFabric.getConfig().enableWhiteList) {
            org.lby123165.easybot.util.WhitelistUtil.handleBindSuccess(packet.playerName, server);
        }
        
        // 执行绑定成功事件配置的命令
        if (EasyBotFabric.getConfig().events.enableSuccessEvent && 
            EasyBotFabric.getConfig().events.bindSuccess != null && 
            !EasyBotFabric.getConfig().events.bindSuccess.isEmpty()) {
            
            for (String cmd : EasyBotFabric.getConfig().events.bindSuccess) {
                String processedCmd = cmd
                    .replace("#player", packet.playerName)
                    .replace("#name", packet.accountName)
                    .replace("#account", packet.accountId);
                
                try {
                    server.getCommandManager().executeWithPrefix(
                        server.getCommandSource(), processedCmd);
                } catch (Exception e) {
                    LOGGER.error("执行绑定成功命令时出错: {}", processedCmd, e);
                }
            }
        }
    }

    private void handleSyncSettingsUpdated(String rawJson) {
        UpdateSyncSettingsPacket packet = GSON.fromJson(rawJson, UpdateSyncSettingsPacket.class);
        FabricClientProfile.setSyncMessageMode(packet.syncMode);
        FabricClientProfile.setSyncMessageMoney(packet.syncMoney == 1);
        LOGGER.info("同步设置已更新: Mode={}, Money={}", packet.syncMode, packet.syncMoney);
    }

    private void handleGetPlayerList(String callbackId, String execOp) {
        List<PlayerInfo> players = getPlayerList();
        PlayerListPacket payload = new PlayerListPacket(players);
        sendCallback(callbackId, execOp, payload);
    }

    @Override
    public FabricServerInfo getInfo() {
        return new FabricServerInfo(this.server);
    }

    private void handleGetServerInfo(String callbackId, String execOp) {
        FabricServerInfo payload = this.getInfo();
        sendCallback(callbackId, execOp, payload);
    }

    private void sendCallback(String callbackId, String execOp, Object data) {
        JsonObject packet = new JsonObject();
        packet.addProperty("op", 5);
        packet.addProperty("callback_id", callbackId);
        packet.addProperty("exec_op", execOp);

        if (data != null) {
            JsonElement dataElement = GSON.toJsonTree(data);
            if (dataElement.isJsonObject()) {
                JsonObject dataJson = dataElement.getAsJsonObject();
                for (String key : dataJson.keySet()) {
                    packet.add(key, dataJson.get(key));
                }
            }
        }
        client.sendMessage(packet.toString());
    }

    @Override
    public List<PlayerInfo> getPlayerList() {
        List<PlayerInfo> players = new ArrayList<>();
        if (server != null && server.getPlayerManager() != null) {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                int latency = ((ILatencyProvider) player.networkHandler).getLatency();

                players.add(new PlayerInfo(
                        player.getName().getString(),
                        player.getUuid().toString(),
                        player.getDisplayName().getString(),
                        latency,
                        player.getWorld().getRegistryKey().getValue().toString(),
                        player.getX(),
                        player.getY(),
                        player.getZ()
                ));
            }
        }
        return players;
    }

    @Override
    public void onEnable() {
        // ...
    }

    @Override
    public void onDisable() {
        // ...
    }
}