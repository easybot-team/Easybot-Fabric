package org.lby123165.easybot.bridge;

import net.fabricmc.loader.api.FabricLoader;
import org.lby123165.easybot.EasyBotFabric;

/**
 * 存储并提供 Fabric 客户端环境的静态信息。
 */
public class FabricClientProfile {

    private static int syncMessageMode = 0;
    private static int syncMessageMoney = 0;

    public static String getPluginVersion() {
        return FabricLoader.getInstance()
                .getModContainer(EasyBotFabric.MOD_ID)
                .map(modContainer -> modContainer.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }

    public static String getServerDescription() {
        return EasyBotFabric.getConfig().serverName;
    }

    public static boolean isDebugMode() {
        return EasyBotFabric.getConfig().debug;
    }

    public static boolean isPapiSupported() {
        return FabricLoader.getInstance().isModLoaded("placeholder-api");
    }

    public static boolean hasGeyser() {
        return FabricLoader.getInstance().isModLoaded("geysermc");
    }

    // Getter 和 Setter 用于同步设置
    public static int getSyncMessageMode() {
        return syncMessageMode;
    }

    public static void setSyncMessageMode(int mode) {
        syncMessageMode = mode;
    }

    public static int getSyncMessageMoney() {
        return syncMessageMoney;
    }

    public static void setSyncMessageMoney(int money) {
        syncMessageMoney = money;
    }
}