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
import java.net.URISyntaxException;

public class EasyBotFabric implements ModInitializer {
    public static final String MOD_ID = "easybotfabric";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static EasyBotFabric instance;
    private static EasyBotConfig config;
    private static BridgeClient bridgeClient; // 保持抽象类型
    private static long serverStartTime;
    private CommandHandler commandHandler;

    // 新增：用于存储服务器实例，以便在 reload 等方法中调用
    private static MinecraftServer minecraftServer;

    @Override
    public void onInitialize() {
        LOGGER.info("EasyBot Fabric Mod ({}) is initializing...", MOD_ID);
        instance = this;

        config = EasyBotConfig.load();
        LOGGER.info("Configuration loaded.");

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            // 修正：在服务器启动时，保存其实例
            EasyBotFabric.minecraftServer = server;
            serverStartTime = System.currentTimeMillis();
            connectWebSocket(server);
            PlayerEventListener.register();
            LOGGER.info("Player event listeners registered.");
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            disconnectWebSocket();
        });

        this.commandHandler = new CommandHandler(this);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            commandHandler.registerCommands(dispatcher);
        });

        LOGGER.info("EasyBot Fabric Mod initialized.");
    }

    public void reload() {
        LOGGER.info("Reloading EasyBot...");
        config = EasyBotConfig.reload();
        LOGGER.info("Configuration reloaded.");

        // 修正：不再调用不存在的 getServer()，而是使用我们保存的静态实例
        if (bridgeClient != null && minecraftServer != null) {
            disconnectWebSocket();
            connectWebSocket(minecraftServer);
        } else {
            LOGGER.warn("Cannot restart WebSocket client as the server instance is not available.");
        }
    }

    private void connectWebSocket(MinecraftServer server) {
        try {
            URI uri = new URI(config.ws);

            // 1. 先创建 Client，但暂时不传入 Behavior
            // 注意：现在我们 new 的是基于 Jetty 的 FabricBridgeClient
            BridgeClient newBridgeClient = new FabricBridgeClient(uri, null);

            // 2. 创建 Behavior，并将刚创建的 Client 实例传给它
            BridgeBehavior behavior = new FabricBridgeBehavior(server, newBridgeClient);

            // 3. 将创建好的 Behavior 设置回 Client
            newBridgeClient.setBehavior(behavior);

            // 4. 赋值给静态变量并开始连接
            bridgeClient = newBridgeClient;
            bridgeClient.connect();

        } catch (Exception e) {
            LOGGER.error("An unexpected error occurred during WebSocket initialization", e);
        }
    }

    private void disconnectWebSocket() {
        if (bridgeClient != null) {
            LOGGER.info("Closing WebSocket connection...");
            bridgeClient.disconnect();
            bridgeClient = null;
        }
    }

    public static EasyBotFabric getInstance() { return instance; }
    public static EasyBotConfig getConfig() { return config; }
    public static BridgeClient getBridgeClient() { return bridgeClient; }
    public static long getServerStartTime() { return serverStartTime; }
}