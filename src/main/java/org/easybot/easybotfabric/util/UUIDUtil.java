package org.easybot.easybotfabric.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.server.MinecraftServer;
import org.easybot.easybotfabric.EasyBotFabric;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * UUID工具类，用于处理玩家UUID相关操作
 */
public class UUIDUtil {
    private static final Gson GSON = new Gson();
    private static final Map<String, String> uuidCache = new HashMap<>();
    
    /**
     * 获取玩家的离线模式UUID
     * @param playerName 玩家名称
     * @return 离线模式UUID
     */
    public static String generateOfflineUUID(String playerName) {
        // 离线模式UUID生成算法
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes(StandardCharsets.UTF_8));
        return uuid.toString();
    }
    
    /**
     * 检查服务器是否为在线模式
     * @param server Minecraft服务器实例
     * @return 如果是在线模式返回true，否则返回false
     */
    public static boolean isOnlineMode(MinecraftServer server) {
        return server != null && server.isOnlineMode();
    }
    
    /**
     * 异步获取玩家的正版UUID
     * @param playerName 玩家名称
     * @return 包含UUID的CompletableFuture
     */
    public static CompletableFuture<String> fetchOnlineUUID(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            // 首先检查缓存
            if (uuidCache.containsKey(playerName)) {
                return uuidCache.get(playerName);
            }
            
            try {
                URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                
                int status = connection.getResponseCode();
                if (status == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    JsonObject json = GSON.fromJson(response.toString(), JsonObject.class);
                    String id = json.get("id").getAsString();
                    
                    // 转换为标准UUID格式
                    String uuid = id.replaceFirst(
                            "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                            "$1-$2-$3-$4-$5"
                    );
                    
                    // 缓存结果
                    uuidCache.put(playerName, uuid);
                    return uuid;
                } else if (status == 204) {
                    EasyBotFabric.LOGGER.warn("玩家 {} 不存在或不是正版账号", playerName);
                    return null;
                } else {
                    EasyBotFabric.LOGGER.error("获取玩家 {} 的UUID时出错，状态码: {}", playerName, status);
                    return null;
                }
            } catch (IOException e) {
                EasyBotFabric.LOGGER.error("获取玩家 {} 的UUID时发生网络错误", playerName, e);
                return null;
            }
        });
    }
    
    /**
     * 更新玩家的UUID缓存
     * @param playerName 玩家名称
     * @param uuid UUID字符串
     */
    public static void updatePlayerUUID(String playerName, String uuid) {
        uuidCache.put(playerName, uuid);
        EasyBotFabric.LOGGER.debug("已更新玩家 {} 的UUID: {}", playerName, uuid);
    }
    
    /**
     * 获取玩家的UUID（根据服务器模式自动选择）
     * @param playerName 玩家名称
     * @param server Minecraft服务器实例
     * @return 包含UUID的CompletableFuture
     */
    public static CompletableFuture<String> getPlayerUUID(String playerName, MinecraftServer server) {
        if (isOnlineMode(server)) {
            return fetchOnlineUUID(playerName).thenApply(uuid -> {
                if (uuid == null) {
                    // 如果无法获取正版UUID，则使用离线UUID作为备用
                    return generateOfflineUUID(playerName);
                }
                return uuid;
            });
        } else {
            return CompletableFuture.completedFuture(generateOfflineUUID(playerName));
        }
    }
    
    /**
     * 清除UUID缓存
     */
    public static void clearCache() {
        uuidCache.clear();
        EasyBotFabric.LOGGER.info("UUID缓存已清除");
    }
}