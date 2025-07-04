package org.lby123165.easybot.bridge;

import com.google.gson.Gson;
import org.lby123165.easybot.bridge.model.PlayerInfo;
import org.lby123165.easybot.bridge.model.PlayerInfoWithRaw;
import org.lby123165.easybot.bridge.packet.PlayerLoginResultPacket;

import java.util.concurrent.CompletableFuture;

public abstract class BridgeClient {

    protected static final Gson GSON = new Gson();
    protected BridgeBehavior behavior;
    private boolean debug = false;

    protected BridgeClient(BridgeBehavior behavior) {
        this.behavior = behavior;
    }

    public void setBehavior(BridgeBehavior behavior) {
        this.behavior = behavior;
    }

    // --- 核心连接方法 ---
    public abstract void connect();
    public abstract void disconnect();
    public abstract void sendMessage(String message);
    public abstract boolean isOpen();

    // --- 新增的事件上报方法 ---
    public abstract CompletableFuture<PlayerLoginResultPacket> login(PlayerInfo playerInfo);
    public abstract void syncMessage(PlayerInfoWithRaw playerInfo, String message, boolean useCommand);
    public abstract void syncEnterExit(PlayerInfoWithRaw playerInfo, boolean isEnter);


    // --- 辅助方法 ---
    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}