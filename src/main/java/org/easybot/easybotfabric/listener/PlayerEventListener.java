package org.easybot.easybotfabric.listener;

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
import org.easybot.easybotfabric.EasyBotFabric;
import org.easybot.easybotfabric.bridge.BridgeClient;
import org.easybot.easybotfabric.bridge.model.PlayerInfo;
import org.easybot.easybotfabric.bridge.model.PlayerInfoWithRaw;
import org.easybot.easybotfabric.duck.ILatencyProvider;
import org.easybot.easybotfabric.util.BotFilterUtil;
import org.easybot.easybotfabric.util.UUIDUtil;
import org.easybot.easybotfabric.util.WhitelistUtil;

public class PlayerEventListener {

    public static void register() {
        // Player Join Event
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            String playerName = player.getName().getString();
            
            // 假人过滤检查
            if (BotFilterUtil.isBotPlayer(playerName)) {
                BotFilterUtil.logBotFiltered(playerName, "加入");
                return;
            }
            
            // UUID同步处理
            String uuid = player.getUuid().toString();
            UUIDUtil.updatePlayerUUID(playerName, uuid);
            EasyBotFabric.LOGGER.debug("已更新玩家 {} 的UUID: {}", playerName, uuid);
            
            BridgeClient client = EasyBotFabric.getBridgeClient();
            if (client == null || !client.isOpen()) return;

            EasyBotFabric.LOGGER.info("玩家 {} 加入游戏，正在上报EasyBot...", playerName);

            PlayerInfo playerInfo = createPlayerInfo(player);

            client.login(playerInfo).thenAccept(result -> {
                if (result.kick) {
                    server.execute(() -> player.networkHandler.disconnect(Text.literal(result.kickMessage)));
                    EasyBotFabric.LOGGER.info("正在踢出玩家 {}，原因：EasyBot登录检查不通过：{}", playerName, result.kickMessage);
                } else {
                    PlayerInfoWithRaw infoWithRaw = new PlayerInfoWithRaw(playerInfo, playerName);
                    client.syncEnterExit(infoWithRaw, true);
                    
                    // 白名单处理
                    if (EasyBotFabric.getConfig().enableWhiteList) {
                        // 检查玩家是否已绑定
                        client.getSocialAccount(playerName).thenAccept(bindData -> {
                            String bindUuid = (String) bindData.get("uuid");
                            if (bindUuid != null && !bindUuid.isEmpty()) {
                                // 已绑定，确保在白名单中
                                WhitelistUtil.addToWhitelist(playerName, EasyBotFabric.getMinecraftServer());
                                EasyBotFabric.LOGGER.debug("玩家 {} 已绑定账号，确保在白名单中", playerName);
                            }
                        }).exceptionally(ex -> {
                            EasyBotFabric.LOGGER.error("检查玩家 {} 绑定状态时出错", playerName, ex);
                            return null;
                        });
                    }
                }
            }).exceptionally(ex -> {
                EasyBotFabric.LOGGER.error("玩家 {} 登录检查时发生错误", playerName, ex);
                return null;
            });
        });

        // Player Quit Event
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            String playerName = player.getName().getString();
            
            // 假人过滤检查
            if (BotFilterUtil.isBotPlayer(playerName)) {
                BotFilterUtil.logBotFiltered(playerName, "退出");
                return;
            }
            
            BridgeClient client = EasyBotFabric.getBridgeClient();
            if (client == null || !client.isOpen()) return;

            EasyBotFabric.LOGGER.info("玩家 {} 退出游戏，正在上报EasyBot...", playerName);

            PlayerInfo playerInfo = createPlayerInfo(player);
            PlayerInfoWithRaw infoWithRaw = new PlayerInfoWithRaw(playerInfo, playerName);
            client.syncEnterExit(infoWithRaw, false);
        });

        // Player Chat Event
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, typeKey) -> {
            String playerName = sender.getName().getString();
            
            // 假人过滤检查
            if (BotFilterUtil.isBotPlayer(playerName)) {
                return;
            }
            
            BridgeClient client = EasyBotFabric.getBridgeClient();
            if (client == null || !client.isOpen()) return;

            // 检查是否忽略命令消息
            String content = message.getContent().getString();
            if (content.startsWith("/") && EasyBotFabric.getConfig().messageSync.ignoreMcdrCommand) {
                return;
            }

            PlayerInfo playerInfo = createPlayerInfo(sender);
            PlayerInfoWithRaw infoWithRaw = new PlayerInfoWithRaw(playerInfo, playerName);
            client.syncMessage(infoWithRaw, content, false);
        });

        // Player Death Event (Robust Implementation)
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayerEntity player)) {
                return; // Only care about players
            }
            
            String playerName = player.getName().getString();
            
            // 假人过滤检查
            if (BotFilterUtil.isBotPlayer(playerName)) {
                BotFilterUtil.logBotFiltered(playerName, "死亡");
                return;
            }

            BridgeClient client = EasyBotFabric.getBridgeClient();
            if (client == null || !client.isOpen()) return;

            String originalDeathMessage = damageSource.getDeathMessage(player).getString();
            String killerName = getKillerName(damageSource);

            // 构造自定义死亡消息
            String customDeathMessage = formatDeathMessage(playerName, killerName, player);
            
            // 如果自定义消息为空，则使用原始消息
            String finalDeathMessage = customDeathMessage != null ? customDeathMessage : originalDeathMessage;

            // 移除颜色代码，用于推送到WebSocket端
            String plainDeathMessage = finalDeathMessage.replaceAll("§[0-9a-fk-or]", "");

            PlayerInfo playerInfo = createPlayerInfo(player);
            PlayerInfoWithRaw infoWithRaw = new PlayerInfoWithRaw(playerInfo, playerName);
            
            // 使用不带颜色代码的消息推送到WebSocket端
            client.syncDeathMessage(infoWithRaw, plainDeathMessage, killerName);
            
            // 注意：这里不再显示自定义死亡消息，因为Minecraft会自动显示原生的死亡消息
            // 如果需要完全自定义死亡消息并取消原生消息，需要创建一个Mixin类来拦截死亡消息的广播
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
     * 格式化自定义死亡消息
     * @param playerName 死亡玩家名称
     * @param killerName 击杀者名称或死亡原因
     * @param player 死亡玩家实体
     * @return 格式化后的死亡消息，如果返回null则使用原始消息
     */
    private static String formatDeathMessage(String playerName, String killerName, ServerPlayerEntity player) {
        // 获取死亡坐标
        int x = (int) player.getX();
        int y = (int) player.getY();
        int z = (int) player.getZ();
        String dimension = getDimensionName(player.getWorld().getRegistryKey().getValue().toString());
        String location = String.format("%s [x:%d, y:%d, z:%d]", dimension, x, y, z);
        
        // 检查killerName是否包含详细的死亡原因描述
        if (killerName.contains("摔") && killerName.contains("高处")) {
            return String.format("§c☠ §f玩家 §e%s §f%s！§7(%s)", playerName, killerName, location);
        } else if (killerName.contains("溺死") || killerName.contains("呼吸")) {
            return String.format("§c☠ §f玩家 §e%s §f%s！§7(%s)", playerName, killerName, location);
        } else if (killerName.contains("烧") || killerName.contains("烤肉")) {
            return String.format("§c☠ §f玩家 §e%s §f%s！§7(%s)", playerName, killerName, location);
        } else if (killerName.contains("岩浆")) {
            return String.format("§c☠ §f玩家 §e%s §f%s！§7(%s)", playerName, killerName, location);
        } else if (killerName.contains("炸")) {
            return String.format("§c☠ §f玩家 §e%s §f%s！§7(%s)", playerName, killerName, location);
        } else if (killerName.contains("仙人掌") || killerName.contains("甜浆果")) {
            return String.format("§c☠ §f玩家 §e%s §f%s！§7(%s)", playerName, killerName, location);
        } else if (killerName.contains("饿死")) {
            return String.format("§c☠ §f玩家 §e%s §f%s！§7(%s)", playerName, killerName, location);
        } else if (killerName.contains("窒息")) {
            return String.format("§c☠ §f玩家 §e%s §f%s！§7(%s)", playerName, killerName, location);
        } else if (killerName.contains("凋零")) {
            return String.format("§c☠ §f玩家 §e%s §f%s！§7(%s)", playerName, killerName, location);
        } else if (killerName.contains("冻")) {
            return String.format("§c☠ §f玩家 §e%s §f%s！§7(%s)", playerName, killerName, location);
        } else if (killerName.contains("魔法")) {
            return String.format("§c☠ §f玩家 §e%s §f%s！§7(%s)", playerName, killerName, location);
        } else if (killerName.contains("闪电")) {
            return String.format("§c☠ §f玩家 §e%s §f%s！§7(%s)", playerName, killerName, location);
        } else if (killerName.contains("砸死") || killerName.contains("掉落")) {
            return String.format("§c☠ §f玩家 §e%s §f%s！§7(%s)", playerName, killerName, location);
        } else if (killerName.contains("荆棘")) {
            return String.format("§c☠ §f玩家 §e%s §f%s！§7(%s)", playerName, killerName, location);
        } else if (killerName.contains("龙息")) {
            return String.format("§c☠ §f玩家 §e%s §f%s！§7(%s)", playerName, killerName, location);
        } else if (killerName.contains("挤")) {
            return String.format("§c☠ §f玩家 §e%s §f%s！§7(%s)", playerName, killerName, location);
        } else if (killerName.contains("游戏规则")) {
            return String.format("§c☠ §f玩家 §e%s §f%s！§7(%s)", playerName, killerName, location);
        } else if (killerName.contains("虚空")) {
            return String.format("§c☠ §f玩家 §e%s §f%s！§7(%s)", playerName, killerName, location);
        } else if (killerName.contains("环境")) {
            return String.format("§c☠ §f玩家 §e%s §f%s！§7(%s)", playerName, killerName, location);
        } else {
            // 被其他实体杀死
            return String.format("§c☠ §f玩家 §e%s §f §c%s §f §7(%s)", playerName, killerName, location);
        }
    }
    
    /**
     * 获取维度的友好名称
     * @param dimensionId 维度ID
     * @return 维度的友好名称
     */
    private static String getDimensionName(String dimensionId) {
        if (dimensionId.contains("overworld")) {
            return "主世界";
        } else if (dimensionId.contains("the_nether")) {
            return "下界";
        } else if (dimensionId.contains("the_end")) {
            return "末地";
        } else {
            return dimensionId;
        }
    }

    /**
     * 确定击杀者名称或死亡原因，使用更详细的描述
     * @param damageSource 伤害源
     * @return 击杀者名称或详细的死亡原因描述
     */
    private static String getKillerName(DamageSource damageSource) {
        // 1. 优先获取直接的攻击者实体
        Entity attacker = damageSource.getAttacker();
        if (attacker instanceof LivingEntity) {
            return attacker.getName().getString();
        }

        // 2. 如果没有直接攻击者，检查伤害类型（语言无关）
        RegistryKey<net.minecraft.entity.damage.DamageType> typeKey = damageSource.getTypeRegistryEntry().getKey()
                .orElse(null);

        if (typeKey != null) {
            if (typeKey.equals(DamageTypes.FALL) || typeKey.equals(DamageTypes.FLY_INTO_WALL)) {
                return "从高处摔了下来，摔得粉身碎骨";
            } else if (typeKey.equals(DamageTypes.DROWN)) {
                return "忘记了如何呼吸，溺死了";
            } else if (typeKey.equals(DamageTypes.IN_FIRE) || typeKey.equals(DamageTypes.ON_FIRE)) {
                return "被烧成了烤肉";
            } else if (typeKey.equals(DamageTypes.LAVA)) {
                return "试图在岩浆中游泳";
            } else if (typeKey.equals(DamageTypes.EXPLOSION) || typeKey.equals(DamageTypes.PLAYER_EXPLOSION)) {
                return "被炸得粉身碎骨";
            } else if (typeKey.equals(DamageTypes.CACTUS)) {
                return "被仙人掌刺死了";
            } else if (typeKey.equals(DamageTypes.SWEET_BERRY_BUSH)) {
                return "被甜浆果丛刺死了";
            } else if (typeKey.equals(DamageTypes.STARVE)) {
                return "饿死了，记得按时吃饭哦";
            } else if (typeKey.equals(DamageTypes.IN_WALL)) {
                return "窒息而亡，请勿把头塞进方块里";
            } else if (typeKey.equals(DamageTypes.WITHER)) {
                return "被凋零效果夺走了生命";
            } else if (typeKey.equals(DamageTypes.FREEZE)) {
                return "冻僵了，下次记得穿暖和点";
            } else if (typeKey.equals(DamageTypes.MAGIC)) {
                return "被神秘的魔法杀死了";
            } else if (typeKey.equals(DamageTypes.LIGHTNING_BOLT)) {
                return "被闪电劈中，变成了焦炭";
            } else if (typeKey.equals(DamageTypes.FALLING_BLOCK) || typeKey.equals(DamageTypes.FALLING_ANVIL)) {
                return "被从天而降的物体砸死了";
            } else if (typeKey.equals(DamageTypes.THORNS)) {
                return "被荆棘的反伤杀死了";
            } else if (typeKey.equals(DamageTypes.DRAGON_BREATH)) {
                return "被末影龙的龙息融化了";
            } else if (typeKey.equals(DamageTypes.CRAMMING)) {
                return "被挤扁了，太拥挤了";
            } else if (typeKey.equals(DamageTypes.OUTSIDE_BORDER) || typeKey.equals(DamageTypes.GENERIC_KILL)) {
                return "被游戏规则处决了";
            } else if (typeKey.equals(DamageTypes.OUT_OF_WORLD)) {
                return "掉入了虚空，永远消失了";
            }
        }

        // 3. 其他环境伤害的后备选项
        return "被环境杀死了";
    }
}