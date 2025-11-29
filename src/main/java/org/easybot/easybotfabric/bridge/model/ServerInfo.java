package org.easybot.easybotfabric.bridge.model;

/**
 * 通用的服务器信息数据模型。
 * 这个类没有 setter，所有数据在创建时通过构造函数提供。
 */
public class ServerInfo {
    private final String version;
    private final int onlinePlayers;
    private final int maxPlayers;
    private final long startTime;
    private final String motd;

    public ServerInfo(String version, int onlinePlayers, int maxPlayers, long startTime, String motd) {
        this.version = version;
        this.onlinePlayers = onlinePlayers;
        this.maxPlayers = maxPlayers;
        this.startTime = startTime;
        this.motd = motd;
    }

    // 获取器
    public String getVersion() { return version; }
    public int getOnlinePlayers() { return onlinePlayers; }
    public int getMaxPlayers() { return maxPlayers; }
    public long getStartTime() { return startTime; }
    public String getMotd() { return motd; }
}