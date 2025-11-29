package org.easybot.easybotfabric.bridge;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.easybot.easybotfabric.EasyBotFabric;

/**
 * Fabric客户端配置信息
 */
public class FabricClientProfile {
    private static boolean debugMode = false;
    private static int syncMessageMode = 0;
    private static boolean syncMessageMoney = false;
    private static boolean papiSupported = false;
    private static boolean geyserSupported = false;

    /**
     * 获取插件版本
     * @return 插件版本
     */
    public static String getPluginVersion() {
        return FabricLoader.getInstance()
                .getModContainer(EasyBotFabric.MOD_ID)
                .map(container -> container.getMetadata().getVersion().toString())
                .orElse("unknown");
    }

    /**
     * 获取服务器版本信息
     * @return 服务器版本
     */
    public static String getServerDescription() {
        MinecraftServer server = EasyBotFabric.getMinecraftServer();
        if (server == null) {
            return "Unknown Fabric Server";
        }
        return "Fabric " + server.getVersion();
    }

    /**
     * 是否为调试模式
     * @return 如果是调试模式返回true，否则返回false
     */
    public static boolean isDebugMode() {
        return debugMode;
    }

    /**
     * 设置调试模式
     * @param debug 是否启用调试模式
     */
    public static void setDebugMode(boolean debug) {
        debugMode = debug;
    }

    /**
     * 获取同步消息模式
     * @return 同步消息模式
     */
    public static int getSyncMessageMode() {
        return syncMessageMode;
    }

    /**
     * 设置同步消息模式
     * @param mode 同步消息模式
     */
    public static void setSyncMessageMode(int mode) {
        syncMessageMode = mode;
    }

    /**
     * 是否同步消息金钱
     * @return 如果同步消息金钱返回true，否则返回false
     */
    public static boolean isSyncMessageMoney() {
        return syncMessageMoney;
    }

    /**
     * 设置是否同步消息金钱
     * @param syncMoney 是否同步消息金钱
     */
    public static void setSyncMessageMoney(boolean syncMoney) {
        syncMessageMoney = syncMoney;
    }

    /**
     * 是否支持PAPI
     * @return 如果支持PAPI返回true，否则返回false
     */
    public static boolean isPapiSupported() {
        return papiSupported;
    }

    /**
     * 设置是否支持PAPI
     * @param supported 是否支持PAPI
     */
    public static void setPapiSupported(boolean supported) {
        papiSupported = supported;
    }
    
    /**
     * 是否支持Geyser
     * @return 如果支持Geyser返回true，否则返回false
     */
    public static boolean hasGeyser() {
        return geyserSupported;
    }
    
    /**
     * 设置是否支持Geyser
     * @param supported 是否支持Geyser
     */
    public static void setGeyserSupported(boolean supported) {
        geyserSupported = supported;
    }
}
