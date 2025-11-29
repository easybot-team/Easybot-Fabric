package org.easybot.easybotfabric.bridge;

import org.easybot.easybotfabric.bridge.model.PlayerInfo;
import org.easybot.easybotfabric.bridge.model.PlayerInfoWithRaw;
import org.easybot.easybotfabric.bridge.packet.PlayerLoginResultPacket;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class BridgeClient {

    protected BridgeBehavior behavior;
    protected boolean debug = false;

    public BridgeClient(BridgeBehavior behavior) {
        this.behavior = behavior;
    }

    // Lifecycle methods
    public abstract void connect();
    public abstract void disconnect();
    public abstract void shutdown(); // FIX: Add the abstract shutdown() method here

    // Core functionality
    public abstract void sendMessage(String message);
    public abstract boolean isOpen();

    // Packet sending methods
    public abstract CompletableFuture<PlayerLoginResultPacket> login(PlayerInfo playerInfo);
    public abstract void syncMessage(PlayerInfoWithRaw playerInfo, String message, boolean useCommand);
    public abstract void syncEnterExit(PlayerInfoWithRaw playerInfo, boolean isEnter);
    public abstract void syncDeathMessage(PlayerInfoWithRaw playerInfo, String deathMessage, String killerName);
    
    // 跨服聊天功能
    public abstract void syncCrossServerMessage(PlayerInfoWithRaw playerInfo, String message);
    
    // 绑定相关功能
    public abstract CompletableFuture<Map<String, Object>> getSocialAccount(String playerName);
    public abstract CompletableFuture<Map<String, Object>> startBind(String playerName);

    // Setters and Getters
    public void setBehavior(BridgeBehavior behavior) {
        this.behavior = behavior;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}