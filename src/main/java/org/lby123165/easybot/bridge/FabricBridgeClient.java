package org.lby123165.easybot.bridge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.lby123165.easybot.EasyBotFabric;
import org.lby123165.easybot.bridge.model.PlayerInfoWithRaw;
import org.lby123165.easybot.bridge.packet.*;
import org.lby123165.easybot.config.EasyBotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@WebSocket
public class FabricBridgeClient extends BridgeClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(FabricBridgeClient.class);
    private static final Gson GSON = new GsonBuilder().create();

    private final WebSocketClient jettyClient;
    private final ExecutorService executor;
    private final URI serverUri;

    private final ConcurrentHashMap<String, CompletableFuture<String>> callbackTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService timeoutScheduler = Executors.newSingleThreadScheduledExecutor();

    private Session session;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private ScheduledExecutorService heartbeatScheduler;
    private final AtomicBoolean ready = new AtomicBoolean(false);
    private int heartbeatInterval = 120;

    public FabricBridgeClient(URI serverUri, BridgeBehavior behavior) {
        super(behavior);
        this.serverUri = serverUri;
        this.jettyClient = new WebSocketClient();
        // FIX: The executor is created once and lives for the duration of the client object.
        this.executor = Executors.newSingleThreadExecutor();
        setDebug(EasyBotFabric.getConfig().debug);
    }

    public <T> CompletableFuture<T> sendAndWaitForCallbackAsync(String execOp, Object data, Class<T> responseType) {
        String callbackId = UUID.randomUUID().toString();
        CompletableFuture<String> future = new CompletableFuture<>();
        callbackTasks.put(callbackId, future);

        JsonObject packet = new JsonObject();
        packet.addProperty("op", 4);
        packet.addProperty("exec_op", execOp);
        packet.addProperty("callback_id", callbackId);
        if (data != null) {
            JsonObject dataJson = GSON.toJsonTree(data).getAsJsonObject();
            for (String key : dataJson.keySet()) {
                packet.add(key, dataJson.get(key));
            }
        }
        sendMessage(packet.toString());

        ScheduledFuture<?> timeoutFuture = timeoutScheduler.schedule(() -> {
            CompletableFuture<String> removedFuture = callbackTasks.remove(callbackId);
            if (removedFuture != null) {
                removedFuture.completeExceptionally(new TimeoutException("等待 EasyBot 回调超时！"));
            }
        }, 10, TimeUnit.SECONDS);

        return future.thenApply(result -> {
            timeoutFuture.cancel(false);
            return GSON.fromJson(result, responseType);
        });
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        if (isDebug()) {
            LOGGER.info("收到消息: {}", message);
        }
        try {
            BasePacket basePacket = GSON.fromJson(message, BasePacket.class);
            switch (basePacket.op) {
                case 0: // Hello
                    HelloPacket helloPacket = GSON.fromJson(message, HelloPacket.class);
                    LOGGER.info("收到来自服务器的 Hello (版本: {}, 心跳间隔: {}s)。", helloPacket.version, helloPacket.interval);
                    this.heartbeatInterval = helloPacket.interval;
                    sendIdentifyPacket();
                    break;
                case 2: // Heartbeat
                    if (isDebug()) {
                        LOGGER.info("收到服务器心跳包，连接正常");
                    }
                    // 可以选择回复一个心跳包，或者简单地确认收到
                    // 这里我们不需要额外回复，因为客户端已经在定期发送心跳包
                    break;
                case 3: // IdentifySuccess
                    IdentifySuccessPacket successPacket = GSON.fromJson(message, IdentifySuccessPacket.class);
                    LOGGER.info("身份验证成功！服务器名: '{}'。客户端已就绪。", successPacket.serverName);
                    ready.set(true);
                    startHeartbeat();
                    break;
                case 4: // Packet (服务器指令)
                    if (this.behavior != null) {
                        this.behavior.onMessage(message);
                    }
                    break;
                case 5: // CallBack
                    JsonObject callbackJson = GSON.fromJson(message, JsonObject.class);
                    String callbackId = callbackJson.get("callback_id").getAsString();
                    CompletableFuture<String> future = callbackTasks.remove(callbackId);
                    if (future != null) {
                        future.complete(message);
                    }
                    break;
                default:
                    LOGGER.warn("收到未处理的 OpCode: {}", basePacket.op);
            }
        } catch (Exception e) {
            LOGGER.error("处理消息失败: " + message, e);
        }
    }

    @Override
    public CompletableFuture<PlayerLoginResultPacket> login(org.lby123165.easybot.bridge.model.PlayerInfo playerInfo) {
        return sendAndWaitForCallbackAsync("PLAYER_JOIN", new OnPlayerJoinPacket(playerInfo), PlayerLoginResultPacket.class);
    }

    @Override
    public void syncMessage(PlayerInfoWithRaw playerInfo, String message, boolean useCommand) {
        SyncMessagePacket packet = new SyncMessagePacket(playerInfo, message, useCommand);
        JsonObject json = GSON.toJsonTree(packet).getAsJsonObject();
        json.addProperty("exec_op", "SYNC_MESSAGE");
        json.addProperty("callback_id", "0");
        sendMessage(json.toString());
    }

    @Override
    public void syncEnterExit(PlayerInfoWithRaw playerInfo, boolean isEnter) {
        SyncEnterExitMessagePacket packet = new SyncEnterExitMessagePacket(playerInfo, isEnter);
        JsonObject json = GSON.toJsonTree(packet).getAsJsonObject();
        json.addProperty("exec_op", "SYNC_ENTER_EXIT_MESSAGE");
        json.addProperty("callback_id", "0");
        sendMessage(json.toString());
    }

    @Override
    public void syncDeathMessage(PlayerInfoWithRaw playerInfo, String deathMessage, String killerName) {
        SyncDeathMessagePacket packet = new SyncDeathMessagePacket(playerInfo, deathMessage, killerName);
        JsonObject json = GSON.toJsonTree(packet).getAsJsonObject();
        json.addProperty("exec_op", "SYNC_DEATH_MESSAGE");
        json.addProperty("callback_id", "0");
        sendMessage(json.toString());
    }
    
    @Override
    public void syncCrossServerMessage(PlayerInfoWithRaw playerInfo, String message) {
        CrossServerMessagePacket packet = new CrossServerMessagePacket(playerInfo, message);
        JsonObject json = GSON.toJsonTree(packet).getAsJsonObject();
        sendMessage(json.toString());
        if (isDebug()) {
            LOGGER.info("已发送跨服消息: {}", message);
        }
    }
    
    @Override
    public CompletableFuture<Map<String, Object>> getSocialAccount(String playerName) {
        String callbackId = UUID.randomUUID().toString();
        GetSocialAccountPacket packet = new GetSocialAccountPacket(playerName, callbackId);
        
        CompletableFuture<String> future = new CompletableFuture<>();
        callbackTasks.put(callbackId, future);
        
        sendMessage(GSON.toJson(packet));
        
        ScheduledFuture<?> timeoutFuture = timeoutScheduler.schedule(() -> {
            CompletableFuture<String> removedFuture = callbackTasks.remove(callbackId);
            if (removedFuture != null) {
                removedFuture.completeExceptionally(new TimeoutException("获取社交账号信息超时"));
            }
        }, 10, TimeUnit.SECONDS);
        
        return future.thenApply(result -> {
            timeoutFuture.cancel(false);
            JsonObject json = GSON.fromJson(result, JsonObject.class);
            
            // 将JsonObject转换为Map
            Map<String, Object> resultMap = new HashMap<>();
            for (String key : json.keySet()) {
                if (json.get(key).isJsonPrimitive()) {
                    resultMap.put(key, json.get(key).getAsString());
                }
            }
            
            return resultMap;
        });
    }
    
    @Override
    public CompletableFuture<Map<String, Object>> startBind(String playerName) {
        String callbackId = UUID.randomUUID().toString();
        StartBindPacket packet = new StartBindPacket(playerName, callbackId);
        
        CompletableFuture<String> future = new CompletableFuture<>();
        callbackTasks.put(callbackId, future);
        
        sendMessage(GSON.toJson(packet));
        
        ScheduledFuture<?> timeoutFuture = timeoutScheduler.schedule(() -> {
            CompletableFuture<String> removedFuture = callbackTasks.remove(callbackId);
            if (removedFuture != null) {
                removedFuture.completeExceptionally(new TimeoutException("开始绑定操作超时"));
            }
        }, 10, TimeUnit.SECONDS);
        
        return future.thenApply(result -> {
            timeoutFuture.cancel(false);
            JsonObject json = GSON.fromJson(result, JsonObject.class);
            
            // 将JsonObject转换为Map
            Map<String, Object> resultMap = new HashMap<>();
            for (String key : json.keySet()) {
                if (json.get(key).isJsonPrimitive()) {
                    resultMap.put(key, json.get(key).getAsString());
                }
            }
            
            return resultMap;
        });
    }

    @Override
    public void connect() {
        if (isConnected.get() || executor.isShutdown()) {
            return;
        }
        executor.submit(() -> {
            try {
                if (!jettyClient.isStarted()) {
                    jettyClient.start();
                }
                LOGGER.info("正在连接到服务器: {}", serverUri);
                ClientUpgradeRequest request = new ClientUpgradeRequest();
                jettyClient.connect(this, serverUri, request);
            } catch (Exception e) {
                LOGGER.warn("WebSocket连接失败");
                scheduleReconnect();
            }
        });
    }

    /**
     * 修正：现在这是一种 软 断开连接，用于重新连接。
     * 它只清理特定于连接的资源。
     */
    @Override
    public void disconnect() {
        if (heartbeatScheduler != null && !heartbeatScheduler.isShutdown()) {
            heartbeatScheduler.shutdownNow();
        }
        if (session != null && session.isOpen()) {
            session.close(1000, "客户端重新连接");
        }
    }

    /**
     * 修正：插件禁用时 硬 关闭的新方法。
     * 这将终止所有线程池。
     */
    public void shutdown() {
        LOGGER.info("正在关闭EasyBot桥接客户端...");
        disconnect(); // 先执行软断开连接
        if (timeoutScheduler != null && !timeoutScheduler.isShutdown()) {
            timeoutScheduler.shutdownNow();
        }
        try {
            if (jettyClient.isStarted()) {
                jettyClient.stop();
            }
        } catch (Exception e) {
            LOGGER.error("停止Jetty客户端时出错", e);
        }
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
        LOGGER.info("EasyBot桥接客户端已关闭。");
    }


    @OnWebSocketConnect
    public void onConnect(Session session) {
        LOGGER.info("已连接到服务器: {}", session.getUpgradeRequest().getRequestURI());
        this.session = session;
        this.isConnected.set(true);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        LOGGER.info("连接关闭: {} - {}", statusCode, reason);
        isConnected.set(false);
        ready.set(false);
        disconnect(); // Perform a soft disconnect
        
        // 只有在非正常关闭时才尝试重连
        if (statusCode != 1000 && !executor.isShutdown()) {
            LOGGER.info("检测到异常断开连接，将尝试重连");
            scheduleReconnect();
        }
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        LOGGER.warn("WebSocket连接遇到错误");
        isConnected.set(false);
        ready.set(false);
        disconnect(); // Perform a soft disconnect
        
        // 发生错误时也尝试重连
        if (!executor.isShutdown()) {
            LOGGER.info("连接错误，将尝试重连");
            scheduleReconnect();
        }
    }

    @Override
    public void sendMessage(String message) {
        if (session != null && session.isOpen()) {
            if (isDebug()) {
                LOGGER.info("发送消息: {}", message);
            }
            session.getRemote().sendStringByFuture(message);
        } else {
            LOGGER.warn("WebSocket 未连接，无法发送消息: {}", message);
        }
    }

    private void sendIdentifyPacket() {
        EasyBotConfig config = EasyBotFabric.getConfig();
        String pluginVersion = FabricClientProfile.getPluginVersion();
        String serverDescription = FabricClientProfile.getServerDescription();
        LOGGER.info("收到服务器 Hello，正在回复 Identify 包。");
        IdentifyPacket packet = new IdentifyPacket(config.token, pluginVersion, serverDescription);
        sendMessage(GSON.toJson(packet));
    }

    private void startHeartbeat() {
        if (heartbeatScheduler != null && !heartbeatScheduler.isShutdown()) {
            heartbeatScheduler.shutdownNow();
        }
        heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
        long delay = Math.max(1, heartbeatInterval - 10);
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            if (session != null && session.isOpen() && ready.get()) {
                sendMessage(GSON.toJson(new HeartbeatPacket()));
            }
        }, delay, delay, TimeUnit.SECONDS);
        LOGGER.info("心跳任务已启动，间隔 {} 秒。", delay);
    }

    private void scheduleReconnect() {
        if (executor.isShutdown()) {
            LOGGER.warn("执行器已关闭，无法安排重连。");
            return;
        }
        
        // 使用专门的重连调度器，避免在主执行器中阻塞
        ScheduledExecutorService reconnectScheduler = Executors.newSingleThreadScheduledExecutor();
        reconnectScheduler.schedule(() -> {
            try {
                LOGGER.info("正在尝试重新连接...");
                connect();
            } catch (Exception e) {
                LOGGER.warn("WebSocket重连尝试失败");
                // 如果重连失败，再次安排重连
                if (!executor.isShutdown()) {
                    scheduleReconnect();
                }
            } finally {
                reconnectScheduler.shutdown();
            }
        }, 5, TimeUnit.SECONDS);
        
        LOGGER.info("5秒后将尝试重连...");
    }

    @Override
    public boolean isOpen() {
        return isConnected.get() && ready.get();
    }
}