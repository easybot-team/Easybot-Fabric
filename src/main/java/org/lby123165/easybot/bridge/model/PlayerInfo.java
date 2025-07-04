package org.lby123165.easybot.bridge.model;

import com.google.gson.annotations.SerializedName;

public class PlayerInfo {
    @SerializedName("player_name")
    public String playerName;
    @SerializedName("player_uuid")
    public String uuid;
    @SerializedName("display_name")
    public String displayName;
    @SerializedName("latency")
    public int latency;
    @SerializedName("world")
    public String world;
    @SerializedName("x")
    public double x;
    @SerializedName("y")
    public double y;
    @SerializedName("z")
    public double z;

    public PlayerInfo(String playerName, String uuid, String displayName, int latency, String world, double x, double y, double z) {
        this.playerName = playerName;
        this.uuid = uuid;
        this.displayName = displayName;
        this.latency = latency;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // 为所有字段添加公共获取器
    public String getPlayerName() { return playerName; }
    public String getUuid() { return uuid; }
    public String getDisplayName() { return displayName; }
    public int getLatency() { return latency; }
    public String getWorld() { return world; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
}