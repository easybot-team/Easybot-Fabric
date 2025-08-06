package org.lby123165.easybot.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WhitelistEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.lby123165.easybot.EasyBotFabric;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 白名单工具类，用于处理白名单相关操作
 */
public class WhitelistUtil {
    
    /**
     * 检查白名单是否启用
     * @param server Minecraft服务器实例
     * @return 如果白名单启用返回true，否则返回false
     */
    public static boolean isWhitelistEnabled(MinecraftServer server) {
        return server != null && server.getPlayerManager().isWhitelistEnabled();
    }
    
    /**
     * 添加玩家到白名单
     * @param playerName 玩家名称
     * @param server Minecraft服务器实例
     * @return 操作是否成功
     */
    public static boolean addToWhitelist(String playerName, MinecraftServer server) {
        if (server == null) {
            EasyBotFabric.LOGGER.error("无法添加玩家 {} 到白名单：服务器实例为空", playerName);
            return false;
        }
        
        try {
            // 创建GameProfile
            UUID uuid = UUID.fromString(UUIDUtil.generateOfflineUUID(playerName));
            com.mojang.authlib.GameProfile profile = new com.mojang.authlib.GameProfile(uuid, playerName);
            
            // 检查玩家是否已在白名单中
            if (server.getPlayerManager().isWhitelisted(profile)) {
                EasyBotFabric.LOGGER.info("玩家 {} 已在白名单中", playerName);
                return true;
            }
            
            // 添加到白名单
            server.getPlayerManager().getWhitelist().add(new net.minecraft.server.WhitelistEntry(profile));
            EasyBotFabric.LOGGER.info("已将玩家 {} 添加到白名单", playerName);
            return true;
        } catch (Exception e) {
            EasyBotFabric.LOGGER.error("添加玩家 {} 到白名单时出错", playerName, e);
            return false;
        }
    }
    
    /**
     * 从白名单中移除玩家
     * @param playerName 玩家名称
     * @param server Minecraft服务器实例
     * @return 操作是否成功
     */
    public static boolean removeFromWhitelist(String playerName, MinecraftServer server) {
        if (server == null) {
            EasyBotFabric.LOGGER.error("无法从白名单中移除玩家 {}：服务器实例为空", playerName);
            return false;
        }
        
        try {
            // 创建GameProfile
            UUID uuid = UUID.fromString(UUIDUtil.generateOfflineUUID(playerName));
            com.mojang.authlib.GameProfile profile = new com.mojang.authlib.GameProfile(uuid, playerName);
            
            // 检查玩家是否在白名单中
            if (!server.getPlayerManager().isWhitelisted(profile)) {
                EasyBotFabric.LOGGER.info("玩家 {} 不在白名单中", playerName);
                return true;
            }
            
            // 从白名单中移除
            server.getPlayerManager().getWhitelist().remove(profile);
            EasyBotFabric.LOGGER.info("已将玩家 {} 从白名单中移除", playerName);
            return true;
        } catch (Exception e) {
            EasyBotFabric.LOGGER.error("从白名单中移除玩家 {} 时出错", playerName, e);
            return false;
        }
    }
    
    /**
     * 处理玩家绑定成功后的白名单操作
     * @param playerName 玩家名称
     * @param server Minecraft服务器实例
     */
    public static void handleBindSuccess(String playerName, MinecraftServer server) {
        if (!EasyBotFabric.getConfig().enableWhiteList) {
            return;
        }
        
        if (server == null) {
            EasyBotFabric.LOGGER.error("无法处理玩家 {} 的白名单：服务器实例为空", playerName);
            return;
        }
        
        CompletableFuture.runAsync(() -> {
            try {
                addToWhitelist(playerName, server);
                EasyBotFabric.LOGGER.info("玩家 {} 绑定成功，已添加到白名单", playerName);
            } catch (Exception e) {
                EasyBotFabric.LOGGER.error("处理玩家 {} 绑定成功后的白名单操作时出错", playerName, e);
            }
        });
    }
    
    /**
     * 处理玩家解绑后的白名单操作
     * @param playerName 玩家名称
     * @param server Minecraft服务器实例
     */
    public static void handleUnbind(String playerName, MinecraftServer server) {
        if (!EasyBotFabric.getConfig().enableWhiteList) {
            return;
        }
        
        if (server == null) {
            EasyBotFabric.LOGGER.error("无法处理玩家 {} 的白名单：服务器实例为空", playerName);
            return;
        }
        
        CompletableFuture.runAsync(() -> {
            try {
                removeFromWhitelist(playerName, server);
                EasyBotFabric.LOGGER.info("玩家 {} 解绑，已从白名单中移除", playerName);
            } catch (Exception e) {
                EasyBotFabric.LOGGER.error("处理玩家 {} 解绑后的白名单操作时出错", playerName, e);
            }
        });
    }
}