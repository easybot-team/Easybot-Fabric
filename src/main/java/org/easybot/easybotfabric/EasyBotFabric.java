package org.easybot.easybotfabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.easybot.easybotfabric.bridge.BridgeBehavior;
import org.easybot.easybotfabric.bridge.BridgeClient;
import org.easybot.easybotfabric.bridge.FabricBridgeBehavior;
import org.easybot.easybotfabric.bridge.FabricBridgeClient;
import org.easybot.easybotfabric.command.CommandHandler;
import org.easybot.easybotfabric.config.EasyBotConfig;
import org.easybot.easybotfabric.listener.PlayerEventListener;
import org.easybot.easybotfabric.util.PapiUtil;
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
        
        // 初始化PAPI工具类，检测Text Placeholder API是否可用
        PapiUtil.initialize();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            EasyBotFabric.minecraftServer = server;
            serverStartTime = System.currentTimeMillis();
            connectWebSocket(server);
            PlayerEventListener.register();
            LOGGER.info("玩家事件监听器已注册。");
        });

        // 在服务器完全启动后显示免责声明
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            LOGGER.info("=".repeat(60));
            LOGGER.info("重要声明：本模组为免费模组，主程序为免费闭源应用（暂时）");
            LOGGER.info("所有模组及插件版本均在Github开源");
            LOGGER.info("如果你是通过任何付费渠道获取的本模组或主程序及token请立即联系作者");
            LOGGER.info("Github: https://github.com/Easybot-team/EasyBot-Fabric");
            LOGGER.info("=".repeat(60));
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
        
        // 先保存旧的配置以便回滚
        EasyBotConfig oldConfig = config;
        
        try {
            // 重新加载配置
            config = EasyBotConfig.reload();
            LOGGER.info("配置已重新加载。");

            if (minecraftServer != null) {
                LOGGER.info("正在重启WebSocket连接...");
                
                // 完全关闭旧连接
                shutdownWebSocket();
                
                // 等待一小段时间确保资源完全释放
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    LOGGER.warn("重载过程被中断");
                }
                
                // 建立新连接
                connectWebSocket(minecraftServer);
                
                LOGGER.info("WebSocket连接已重启。");
            } else {
                LOGGER.warn("无法重启WebSocket客户端，服务器实例不可用。");
            }
            
            LOGGER.info("EasyBot重载完成！");
        } catch (Exception e) {
            LOGGER.error("重载过程中发生错误，尝试回滚配置", e);
            config = oldConfig; // 回滚配置
            
            // 尝试恢复旧连接
            if (minecraftServer != null) {
                try {
                    connectWebSocket(minecraftServer);
                } catch (Exception reconnectError) {
                    LOGGER.error("回滚后重连失败", reconnectError);
                }
            }
            
            throw new RuntimeException("重载失败: " + e.getMessage(), e);
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
            LOGGER.warn("WebSocket初始化时发生意外错误");
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
    public static MinecraftServer getMinecraftServer() { return minecraftServer; }
}