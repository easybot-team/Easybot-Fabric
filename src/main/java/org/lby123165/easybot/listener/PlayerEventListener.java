package org.lby123165.easybot.listener;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.lby123165.easybot.EasyBotFabric;
import org.lby123165.easybot.bridge.BridgeClient;
import org.lby123165.easybot.bridge.model.PlayerInfo;
import org.lby123165.easybot.bridge.model.PlayerInfoWithRaw;
import org.lby123165.easybot.duck.ILatencyProvider;

public class PlayerEventListener {

    public static void register() {
        // Player Join Event
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            BridgeClient client = EasyBotFabric.getBridgeClient();
            if (client == null || !client.isOpen()) return;

            EasyBotFabric.LOGGER.info("玩家 {} 加入游戏，正在上报EasyBot...", player.getName().getString());

            PlayerInfo playerInfo = createPlayerInfo(player);

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

        // Player Quit Event
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            BridgeClient client = EasyBotFabric.getBridgeClient();
            if (client == null || !client.isOpen()) return;

            EasyBotFabric.LOGGER.info("玩家 {} 退出游戏，正在上报EasyBot...", player.getName().getString());

            PlayerInfo playerInfo = createPlayerInfo(player);
            PlayerInfoWithRaw infoWithRaw = new PlayerInfoWithRaw(playerInfo, player.getName().getString());
            client.syncEnterExit(infoWithRaw, false);
        });

        // Player Chat Event
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, typeKey) -> {
            BridgeClient client = EasyBotFabric.getBridgeClient();
            if (client == null || !client.isOpen()) return;

            PlayerInfo playerInfo = createPlayerInfo(sender);
            PlayerInfoWithRaw infoWithRaw = new PlayerInfoWithRaw(playerInfo, sender.getName().getString());
            client.syncMessage(infoWithRaw, message.getContent().getString(), false);
        });

        // Player Death Event (Robust Implementation)
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayerEntity player)) {
                return; // Only care about players
            }

            BridgeClient client = EasyBotFabric.getBridgeClient();
            if (client == null || !client.isOpen()) return;

            String deathMessage = damageSource.getDeathMessage(player).getString();
            String killerName = getKillerName(damageSource);

            PlayerInfo playerInfo = createPlayerInfo(player);
            PlayerInfoWithRaw infoWithRaw = new PlayerInfoWithRaw(playerInfo, player.getName().getString());
            client.syncDeathMessage(infoWithRaw, deathMessage, killerName);
        });
    }

    /**
     * Helper method to create a PlayerInfo object from a ServerPlayerEntity.
     * Reduces code duplication.
     */
    private static PlayerInfo createPlayerInfo(ServerPlayerEntity player) {
        // For disconnected players, latency is not available.
        int latency = player.networkHandler != null ? ((ILatencyProvider) player.networkHandler).getLatency() : 0;
        return new PlayerInfo(
                player.getName().getString(),
                player.getUuid().toString(),
                player.getDisplayName().getString(),
                latency,
                player.getWorld().getRegistryKey().getValue().toString(),
                player.getX(), player.getY(), player.getZ()
        );
    }

    /**
     * Determines the killer's name in a robust, language-independent way.
     * @param damageSource The source of the damage.
     * @return A string representing the killer or cause of death.
     */
    private static String getKillerName(DamageSource damageSource) {
        // 1. Prioritize getting the attacker entity directly.
        Entity attacker = damageSource.getAttacker();
        if (attacker instanceof LivingEntity) {
            return attacker.getName().getString();
        }

        // 2. If no direct attacker, check the damage type. This is language-independent.
        RegistryKey<net.minecraft.entity.damage.DamageType> typeKey = damageSource.getTypeRegistryEntry().getKey()
                .orElse(null);

        if (typeKey != null) {
            if (typeKey.equals(DamageTypes.FALL) || typeKey.equals(DamageTypes.FLY_INTO_WALL)) {
                return "摔落伤害";
            } else if (typeKey.equals(DamageTypes.DROWN)) {
                return "drown";
            } else if (typeKey.equals(DamageTypes.IN_FIRE) || typeKey.equals(DamageTypes.ON_FIRE) || typeKey.equals(DamageTypes.LAVA)) {
                return "火焰";
            } else if (typeKey.equals(DamageTypes.EXPLOSION) || typeKey.equals(DamageTypes.PLAYER_EXPLOSION)) {
                return "explosion";
            } else if (typeKey.equals(DamageTypes.CACTUS) || typeKey.equals(DamageTypes.SWEET_BERRY_BUSH)) {
                return "扎人的东西";
            } else if (typeKey.equals(DamageTypes.STARVE)) {
                return "starvation";
            } else if (typeKey.equals(DamageTypes.IN_WALL)) {
                return "suffocation";
            } else if (typeKey.equals(DamageTypes.WITHER)) {
                return "wither";
            } else if (typeKey.equals(DamageTypes.FREEZE)) {
                return "细雪";
            }
        }

        // 3. Fallback for other environmental damage
        return "environment";
    }
}