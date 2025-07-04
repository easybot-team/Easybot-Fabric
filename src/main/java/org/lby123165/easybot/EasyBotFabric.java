package org.lby123165.easybot;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.lby123165.easybot.bridge.BridgeBehavior;
import org.lby123165.easybot.bridge.BridgeClient;
import org.lby123165.easybot.bridge.FabricBridgeBehavior;
import org.lby123165.easybot.bridge.FabricBridgeClient;
import org.lby123165.easybot.command.CommandHandler;
import org.lby123165.easybot.config.EasyBotConfig;
import org.lby123165.easybot.listener.PlayerEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class EasyBotFabric implements ModInitializer {
    public static final String MOD_ID = "easybotfabric";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static EasyBotFabric instance;
    private static EasyBotConfig config;
    private static BridgeClient bridgeClient;
    private static long serverStartTime;
    private CommandHandler commandHandler;
    private static MinecraftServer minecraftServer;

    @Override
    public void onInitialize() {
        LOGGER.info("EasyBot Fabric Mod ({}) 正在初始化...", MOD_ID);
        instance = this;

        config = EasyBotConfig.load();
        LOGGER.info("配置已加载。");

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            EasyBotFabric.minecraftServer = server;
            serverStartTime = System.currentTimeMillis();
            connectWebSocket(server);
            PlayerEventListener.register();
            LOGGER.info("玩家事件监听器已注册。");
        });

        // FIX: Use the new helper method for a clean shutdown on server stop
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            shutdownWebSocket();
        });

        this.commandHandler = new CommandHandler(this);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            commandHandler.registerCommands(dispatcher);
        });

        LOGGER.info("EasyBot Fabric Mod 初始化完成。");
    }

    public void reload() {
        LOGGER.info("正在重新加载EasyBot...");
        config = EasyBotConfig.reload();
        LOGGER.info("配置已重新加载。");

        if (bridgeClient != null && minecraftServer != null) {
            // FIX: Use the new helper method to fully clean up the old client before reconnecting
            shutdownWebSocket();
            connectWebSocket(minecraftServer);
        } else {
            LOGGER.warn("无法重启WebSocket客户端，服务器实例不可用。");
        }
    }

    private void connectWebSocket(MinecraftServer server) {
        try {
            URI uri = new URI(config.ws);
            BridgeClient newBridgeClient = new FabricBridgeClient(uri, null);
            BridgeBehavior behavior = new FabricBridgeBehavior(server, newBridgeClient);
            newBridgeClient.setBehavior(behavior);
            bridgeClient = newBridgeClient;
            bridgeClient.connect();
        } catch (Exception e) {
            LOGGER.error("WebSocket初始化时发生意外错误", e);
        }
    }

    // FIX: Renamed from disconnectWebSocket to better reflect its purpose
    private void shutdownWebSocket() {
        if (bridgeClient != null) {
            LOGGER.info("正在关闭WebSocket连接...");
            // This now correctly calls the shutdown method we added to the interface
            bridgeClient.shutdown();
            bridgeClient = null;
        }
    }

    public static EasyBotFabric getInstance() { return instance; }
    public static EasyBotConfig getConfig() { return config; }
    public static BridgeClient getBridgeClient() { return bridgeClient; }
    public static long getServerStartTime() { return serverStartTime; }
}