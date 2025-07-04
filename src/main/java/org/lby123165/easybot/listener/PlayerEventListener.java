·package org.lby123165.easybot.listener;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.lby123165.easybot.EasyBotFabric;
import org.lby123165.easybot.bridge.BridgeClient;
import org.lby123165.easybot.bridge.model.PlayerInfo;
import org.lby123165.easybot.bridge.model.PlayerInfoWithRaw;
import org.lby123165.easybot.bridge.packet.PlayerLoginResultPacket;

public class PlayerEventListener {

    public static void register() {
        // 玩家加入事件
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            BridgeClient client = EasyBotFabric.getBridgeClient();
            if (client == null || !client.isOpen()) return;

            EasyBotFabric.LOGGER.info("Player {} joined, reporting to EasyBot...", player.getName().getString());

            // 1. 构建玩家信息
            PlayerInfo playerInfo = new PlayerInfo(
                    player.getName().getString(),
                    player.getUuid().toString(),
                    player.getDisplayName().getString(),
                    player.networkHandler.getLatency(),
                    player.getWorld().getRegistryKey().getValue().toString(),
                    player.getX(), player.getY(), player.getZ()
            );

            // 2. 发送登录请求并处理结果
            client.login(playerInfo).thenAccept(result -> {
                if (result.kick) {
                    // 必须在服务器主线程中执行踢出操作
                    server.execute(() -> player.networkHandler.disconnect(Text.of(result.kickMessage)));
                    EasyBotFabric.LOGGER.info("Kicking player {} due to EasyBot login check: {}", player.getName().getString(), result.kickMessage);
                } else {
                    // 登录成功后，再发送进服通知
                    PlayerInfoWithRaw infoWithRaw = new PlayerInfoWithRaw(playerInfo, player.getName().getString());
                    client.syncEnterExit(infoWithRaw, true);
                }
            }).exceptionally(ex -> {
                EasyBotFabric.LOGGER.error("Error during player login check for {}", player.getName().getString(), ex);
                return null;
            });
        });

        // 玩家退出事件
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            BridgeClient client = EasyBotFabric.getBridgeClient();
            if (client == null || !client.isOpen()) return;

            EasyBotFabric.LOGGER.info("Player {} left, reporting to EasyBot...", player.getName().getString());

            PlayerInfo playerInfo = new PlayerInfo(
                    player.getName().getString(),
                    player.getUuid().toString(),
                    player.getDisplayName().getString(),
                    0, // 退出时延迟不可用
                    player.getWorld().getRegistryKey().getValue().toString(),
                    player.getX(), player.getY(), player.getZ()
            );
            PlayerInfoWithRaw infoWithRaw = new PlayerInfoWithRaw(playerInfo, player.getName().getString());
            client.syncEnterExit(infoWithRaw, false);
        });

        // 玩家聊天事件
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, typeKey) -> {
            BridgeClient client = EasyBotFabric.getBridgeClient();
            if (client == null || !client.isOpen()) return;

            PlayerInfo playerInfo = new PlayerInfo(
                    sender.getName().getString(),
                    sender.getUuid().toString(),
                    sender.getDisplayName().getString(),
                    sender.networkHandler.getLatency(),
                    sender.getWorld().getRegistryKey().getValue().toString(),
                    sender.getX(), sender.getY(), sender.getZ()
            );


            PlayerInfoWithRaw infoWithRaw = new PlayerInfoWithRaw(playerInfo, sender.getName().getString());
            // useCommand=false 表示这是普通聊天消息
            client.syncMessage(infoWithRaw, message.getContent().getString(), false);
        });
    }
}