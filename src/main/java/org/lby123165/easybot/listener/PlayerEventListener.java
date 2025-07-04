package org.lby123165.easybot.listener;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.lby123165.easybot.EasyBotFabric;
import org.lby123165.easybot.bridge.BridgeClient;
import org.lby123165.easybot.bridge.model.PlayerInfo;
import org.lby123165.easybot.bridge.model.PlayerInfoWithRaw;

// 为正则表达式添加必要的导入
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerEventListener {

    public static void register() {
        // 玩家加入事件
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            BridgeClient client = EasyBotFabric.getBridgeClient();
            if (client == null || !client.isOpen()) return;

            EasyBotFabric.LOGGER.info("玩家 {} 加入游戏，正在上报EasyBot...", player.getName().getString());

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
                    EasyBotFabric.LOGGER.info("正在踢出玩家 {}，原因：EasyBot登录检查不通过：{}", player.getName().getString(), result.kickMessage);
                } else {
                    PlayerInfoWithRaw infoWithRaw = new PlayerInfoWithRaw(playerInfo, player.getName().getString());
                    client.syncEnterExit(infoWithRaw, true);
                }
            }).exceptionally(ex -> {
                EasyBotFabric.LOGGER.error("玩家 {} 登录检查时发生错误", player.getName().getString(), ex);
                return null;
            });
        });

        // 玩家退出事件
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            BridgeClient client = EasyBotFabric.getBridgeClient();
            if (client == null || !client.isOpen()) return;

            EasyBotFabric.LOGGER.info("玩家 {} 退出游戏，正在上报EasyBot...", player.getName().getString());

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

        // 玩家死亡事件 (混合模式最终版)
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayerEntity player)) {
                return; // 只关心玩家死亡
            }

            BridgeClient client = EasyBotFabric.getBridgeClient();
            if (client == null || !client.isOpen()) return;

            // 1. 获取完整的死亡消息文本
            String deathMessage = damageSource.getDeathMessage(player).getString();
            String killerName = "null"; // 默认值

            // 2. API优先：尝试直接从 DamageSource 获取攻击者实体
            Entity attacker = damageSource.getAttacker();
            Entity source = damageSource.getSource();
            Entity killerEntity = attacker != null ? attacker : source;

            if (killerEntity instanceof LivingEntity) {
                killerName = killerEntity.getName().getString();
            } else {
                // 3. 文本解析作为后备
                // 3.1 首先尝试解析实体名称 (例如 "... whilst fighting a Skeleton")
                // (?i) 表示不区分大小写
                Pattern entityPattern = Pattern.compile("(?i)(?:by|fighting) ([\\w\\s.-]+)");
                Matcher entityMatcher = entityPattern.matcher(deathMessage);
                if (entityMatcher.find()) {
                    killerName = entityMatcher.group(1).trim();
                } else {
                    // 3.2 如果没有实体，则解析环境死因
                    if (deathMessage.contains("fell") || deathMessage.contains("hit the ground")) {
                        killerName = "摔落伤害";
                    } else if (deathMessage.contains("drowned")) {
                        killerName = "drown";
                    } else if (deathMessage.contains("burned") || deathMessage.contains("went up in flames")) {
                        killerName = "火焰";
                    } else if (deathMessage.contains("blew up") || deathMessage.contains("was blown up")) {
                        killerName = "explosion";
                    } else if (deathMessage.contains("pricked to death") || deathMessage.contains("poked to death")) {
                        killerName = "扎人的东西"; // 例如: 仙人掌, 甜浆果丛
                    } else if (deathMessage.contains("starved to death")) {
                        killerName = "starvation";
                    } else if (deathMessage.contains("suffocated in a wall")) {
                        killerName = "suffocation";
                    } else if (deathMessage.contains("withered away")) {
                        killerName = "wither";
                    } else if (deathMessage.contains("froze to death")) {
                        killerName = "细雪";
                    } else {
                        killerName = "environment"; // 最终的通用环境伤害
                    }
                }
            }

            // 4. 上报最终结果
            PlayerInfo playerInfo = new PlayerInfo(
                    player.getName().getString(),
                    player.getUuid().toString(),
                    player.getDisplayName().getString(),
                    player.networkHandler.getLatency(),
                    player.getWorld().getRegistryKey().getValue().toString(),
                    player.getX(), player.getY(), player.getZ()
            );

            PlayerInfoWithRaw infoWithRaw = new PlayerInfoWithRaw(playerInfo, player.getName().getString());
            client.syncDeathMessage(infoWithRaw, deathMessage, killerName);
        });
    }
}