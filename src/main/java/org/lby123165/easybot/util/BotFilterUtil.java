package org.lby123165.easybot.util;

import net.fabricmc.loader.api.FabricLoader;
import org.lby123165.easybot.EasyBotFabric;
import org.lby123165.easybot.config.EasyBotConfig;

/**
 * 假人过滤工具类
 */
public class BotFilterUtil {
    private static boolean carpetModLoaded = false;
    private static boolean carpetChecked = false;
    
    // 方法已在下方重新实现，此处删除
    
    /**
     * 检查Carpet模组是否已加载
     * @return 如果Carpet模组已加载返回true，否则返回false
     */
    public static boolean isCarpetModLoaded() {
        if (!carpetChecked) {
            carpetChecked = true;
            carpetModLoaded = FabricLoader.getInstance().isModLoaded("carpet");
            if (carpetModLoaded) {
                EasyBotFabric.LOGGER.info("检测到Carpet模组，将根据配置决定是否过滤假人");
            }
        }
        return carpetModLoaded;
    }
    
    /**
     * 检查玩家是否为假人
     * @param playerName 玩家名称
     * @return 如果是假人返回true，否则返回false
     */
    public static boolean isBotPlayer(String playerName) {
        EasyBotConfig config = EasyBotFabric.getConfig();
        
        // 如果假人过滤功能被禁用，直接返回false
        if (config == null || !config.botFilter.enabled) {
            return false;
        }
        
        // 检查是否为Carpet假人
        boolean isCarpetBot = false;
        if (isCarpetModLoaded() && config.botFilter.filterCarpetBots) {
            // 使用配置的Carpet假人前缀
            for (String prefix : config.botFilter.carpetPrefixes) {
                if (playerName.startsWith(prefix)) {
                    EasyBotFabric.LOGGER.debug("检测到Carpet假人: {} (匹配前缀: {})", playerName, prefix);
                    isCarpetBot = true;
                    break;
                }
            }
        }
        
        // 检查配置的前缀
        boolean matchesPrefix = false;
        for (String prefix : config.botFilter.prefixes) {
            if (playerName.startsWith(prefix)) {
                EasyBotFabric.LOGGER.debug("检测到假人: {} (匹配前缀: {})", playerName, prefix);
                matchesPrefix = true;
                break;
            }
        }
        
        return isCarpetBot || matchesPrefix;
    }
    
    /**
     * 记录假人过滤信息
     * @param playerName 玩家名称
     * @param eventType 事件类型
     */
    public static void logBotFiltered(String playerName, String eventType) {
        EasyBotConfig config = EasyBotFabric.getConfig();
        EasyBotFabric.LOGGER.info("过滤假人 {} 的{}事件 (匹配前缀: {})", 
                playerName, eventType, config.botFilter.prefixes);
    }
}