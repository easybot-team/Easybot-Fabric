package org.lby123165.easybot.listener;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.lby123165.easybot.EasyBotFabric;
import org.lby123165.easybot.bridge.BridgeClient;
import org.lby123165.easybot.bridge.model.PlayerInfo;
import org.lby123165.easybot.bridge.model.PlayerInfoWithRaw;

public class PlayerEventListener {

    public static void register() {
        // 玩家加入事件
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            BridgeClient client = EasyBotFabric.getBridgeClient();
            if (client == null || !client.isOpen()) return;

            EasyBotFabric.LOGGER.info("Player {} joined, reporting to EasyBot...", player.getName().getString());

            PlayerInfo playerInfo = new PlayerInfo(
                    player.getName().getString(),
                    player.getUuid().toString(),
                    player.getDisplayName().getString(),
                    player.networkHandler.getLatency(),
                    player.getWorld().getRegistryKey().getValue().toString(),
                    player.getX(), player.getY(), player.getZ()
            );

            client.login(playerInfo).thenAccept(result -> {
                if (result.kick) {
                    server.execute(() -> player.networkHandler.disconnect(Text.of(result.kickMessage)));
                    EasyBotFabric.LOGGER.info("Kicking player {} due to EasyBot login check: {}", player.getName().getString(), result.kickMessage);
                } else {
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
            client.syncMessage(infoWithRaw, message.getContent().getString(), false);
        });

        // 新增：玩家死亡事件
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayerEntity player)) {
                return; // 只关心玩家死亡
            }

            BridgeClient client = EasyBotFabric.getBridgeClient();
            if (client == null || !client.isOpen()) return;

            // 获取死亡信息
            String deathMessage = player.getDamageTracker().getDeathMessage().getString();
            String killerName = "null";
            if (damageSource.getAttacker() != null) {
                killerName = damageSource.getAttacker().getName().getString();
            }

            PlayerInfo playerInfo = new PlayerInfo(
                    player.getName().getString(),
                    player.getUuid().toString(),
                    player.getDisplayName().getString(),
                    player.networkHandler.getLatency(),
                    player.getWorld().getRegistryKey().getValue().toString(),
                    // FIX: Removed the stray "ax" typo here
                    player.getX(), player.getY(), player.getZ()
            );

            PlayerInfoWithRaw infoWithRaw = new PlayerInfoWithRaw(playerInfo, player.getName().getString());
            client.syncDeathMessage(infoWithRaw, deathMessage, killerName);
        });
    }
}