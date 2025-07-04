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
        LOGGER.info("EasyBot Fabric Mod ({}) is initializing...", MOD_ID);
        instance = this;

        config = EasyBotConfig.load();
        LOGGER.info("Configuration loaded.");

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            EasyBotFabric.minecraftServer = server;
            serverStartTime = System.currentTimeMillis();
            connectWebSocket(server);
            PlayerEventListener.register();
            LOGGER.info("Player event listeners registered.");
        });

        // FIX: Use the new helper method for a clean shutdown on server stop
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            shutdownWebSocket();
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

        if (bridgeClient != null && minecraftServer != null) {
            // FIX: Use the new helper method to fully clean up the old client before reconnecting
            shutdownWebSocket();
            connectWebSocket(minecraftServer);
        } else {
            LOGGER.warn("Cannot restart WebSocket client as the server instance is not available.");
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
            LOGGER.error("An unexpected error occurred during WebSocket initialization", e);
        }
    }

    // FIX: Renamed from disconnectWebSocket to better reflect its purpose
    private void shutdownWebSocket() {
        if (bridgeClient != null) {
            LOGGER.info("Shutting down WebSocket connection...");
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